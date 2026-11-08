import twilio from 'twilio';
import { config } from '../config/env';
import { logger, redactPhoneNumber } from '../logging/logger';

export interface SmsRecord {
  id: string;
  from: string;
  to: string;
  body: string;
  timestamp: number;
  direction: 'inbound' | 'outbound';
  status: 'sent' | 'delivered' | 'received' | 'failed';
}

// In-memory persistent cache for messages during server session
const messageHistory: SmsRecord[] = [
  {
    id: 'msg_welcome_demo_01',
    from: config.TWILIO_PHONE_NUMBER || '+12025550199',
    to: '+12025550143',
    body: 'Hello from CallBridge! Your Twilio VoIP & SMS hub is now active.',
    timestamp: Date.now() - 1000 * 60 * 30,
    direction: 'outbound',
    status: 'delivered',
  },
];

/**
 * Sends outbound SMS via Twilio Messages API.
 */
export async function sendOutboundSms(to: string, body: string): Promise<SmsRecord> {
  const accountSid = config.TWILIO_ACCOUNT_SID;
  const authToken = config.TWILIO_AUTH_TOKEN;
  const fromNumber = config.TWILIO_PHONE_NUMBER || '+12025550199';

  const cleanTo = to.trim().startsWith('+') ? to.trim() : `+${to.trim()}`;

  if (!accountSid || !authToken) {
    logger.warn('Twilio credentials not configured; saving mock outbound SMS', {
      to: redactPhoneNumber(cleanTo),
    });

    const mockRecord: SmsRecord = {
      id: `sim_${Date.now()}`,
      from: fromNumber,
      to: cleanTo,
      body,
      timestamp: Date.now(),
      direction: 'outbound',
      status: 'sent',
    };
    messageHistory.unshift(mockRecord);
    return mockRecord;
  }

  try {
    const client = twilio(accountSid, authToken);
    const twilioMsg = await client.messages.create({
      body,
      from: fromNumber,
      to: cleanTo,
    });

    logger.info('Outbound SMS dispatched via Twilio', {
      sid: twilioMsg.sid,
      to: redactPhoneNumber(cleanTo),
    });

    const record: SmsRecord = {
      id: twilioMsg.sid,
      from: fromNumber,
      to: cleanTo,
      body,
      timestamp: Date.now(),
      direction: 'outbound',
      status: (twilioMsg.status as any) || 'sent',
    };
    messageHistory.unshift(record);
    return record;
  } catch (error: any) {
    logger.error('Failed to send outbound SMS via Twilio', {
      error: error.message,
      to: redactPhoneNumber(cleanTo),
    });

    // Fallback record to keep local client in sync
    const failedRecord: SmsRecord = {
      id: `err_${Date.now()}`,
      from: fromNumber,
      to: cleanTo,
      body,
      timestamp: Date.now(),
      direction: 'outbound',
      status: 'failed',
    };
    messageHistory.unshift(failedRecord);
    throw error;
  }
}

/**
 * Handles incoming SMS webhook payload from Twilio.
 */
export function handleInboundSmsWebhook(from: string, to: string, body: string, messageSid?: string): string {
  const record: SmsRecord = {
    id: messageSid || `in_${Date.now()}`,
    from,
    to,
    body,
    timestamp: Date.now(),
    direction: 'inbound',
    status: 'received',
  };

  messageHistory.unshift(record);
  logger.info('Inbound SMS received and logged', {
    from: redactPhoneNumber(from),
    id: record.id,
  });

  const VoiceResponse = twilio.twiml.MessagingResponse;
  const twiml = new VoiceResponse();
  return twiml.toString();
}

/**
 * Returns message records, optionally filtered by phone number address.
 */
export function getMessageHistory(filterAddress?: string): SmsRecord[] {
  if (!filterAddress) {
    return messageHistory.slice(0, 50);
  }

  const cleanFilter = filterAddress.replace(/[^0-9]/g, '');
  return messageHistory.filter((m) => {
    const fromClean = m.from.replace(/[^0-9]/g, '');
    const toClean = m.to.replace(/[^0-9]/g, '');
    return fromClean.includes(cleanFilter) || toClean.includes(cleanFilter);
  });
}
