import twilio from 'twilio';
import { config } from '../config/env';
import { logger, redactPhoneNumber } from '../logging/logger';

const AccessToken = twilio.jwt.AccessToken;
const VoiceGrant = AccessToken.VoiceGrant;

export interface VoiceTokenResult {
  token: string;
  identity: string;
  expiresAt: string;
}

export function generateVoiceAccessToken(identity: string = 'cb_operator'): VoiceTokenResult {
  const accountSid = config.TWILIO_ACCOUNT_SID;
  const apiKeySid = config.TWILIO_API_KEY_SID;
  const apiKeySecret = config.TWILIO_API_KEY_SECRET;
  const twimlAppSid = config.TWILIO_TWIML_APP_SID;

  if (!accountSid || !apiKeySid || !apiKeySecret || !twimlAppSid) {
    logger.warn('Twilio credentials not fully configured; using development mock token for testing');
    const expiry = new Date(Date.now() + config.TOKEN_TTL_SECONDS * 1000).toISOString();
    return {
      token: `mock_jwt_token_${Date.now()}`,
      identity,
      expiresAt: expiry,
    };
  }

  // Create an Access Token instance
  const token = new AccessToken(accountSid, apiKeySid, apiKeySecret, {
    identity,
    ttl: config.TOKEN_TTL_SECONDS,
  });

  // Grant access to Twilio Voice
  const voiceGrant = new VoiceGrant({
    outgoingApplicationSid: twimlAppSid,
    incomingAllow: false, // MVP is strictly outbound
  });

  token.addGrant(voiceGrant);

  const jwt = token.toJwt();
  const expiresAt = new Date(Date.now() + config.TOKEN_TTL_SECONDS * 1000).toISOString();

  logger.info('Voice Access Token issued', {
    identity,
    ttl: config.TOKEN_TTL_SECONDS,
  });

  return {
    token: jwt,
    identity,
    expiresAt,
  };
}

export interface AccountStatusResult {
  success: boolean;
  balance: string;
  currency: string;
  status: string;
  accountName: string;
  phoneNumbers: string[];
}

export async function getTwilioAccountStatus(): Promise<AccountStatusResult> {
  const accountSid = config.TWILIO_ACCOUNT_SID;
  const authToken = config.TWILIO_AUTH_TOKEN;

  if (!accountSid || !authToken) {
    logger.warn('Twilio credentials not configured; returning mock account telemetry');
    return {
      success: true,
      balance: '14.50',
      currency: 'USD',
      status: 'active (demo)',
      accountName: 'CallBridge Telephony',
      phoneNumbers: [config.TWILIO_PHONE_NUMBER || '+12025550199'],
    };
  }

  try {
    const client = twilio(accountSid, authToken);
    const account = await client.api.v2010.accounts(accountSid).fetch();

    let balanceAmount = '14.50';
    let currency = 'USD';
    try {
      const balanceObj = await client.balance.fetch();
      balanceAmount = balanceObj.balance;
      currency = balanceObj.currency;
    } catch {
      // Trial accounts or specific auth setups may not expose balance endpoint
      balanceAmount = 'Active';
    }

    let numbers: string[] = [];
    try {
      const incomingNumbers = await client.incomingPhoneNumbers.list({ limit: 5 });
      numbers = incomingNumbers.map((n) => n.phoneNumber);
    } catch {
      // Fallback
    }

    if (numbers.length === 0 && config.TWILIO_PHONE_NUMBER) {
      numbers.push(config.TWILIO_PHONE_NUMBER);
    }

    return {
      success: true,
      balance: balanceAmount,
      currency,
      status: account.status,
      accountName: account.friendlyName || 'Twilio Account',
      phoneNumbers: numbers,
    };
  } catch (err: any) {
    logger.error('Failed to fetch Twilio account telemetry', { error: err.message });
    return {
      success: false,
      balance: '--',
      currency: 'USD',
      status: 'offline',
      accountName: 'Twilio Gateway Disconnected',
      phoneNumbers: config.TWILIO_PHONE_NUMBER ? [config.TWILIO_PHONE_NUMBER] : [],
    };
  }
}

export function generateOutboundTwiML(destinationNumber: string): string {
  const VoiceResponse = twilio.twiml.VoiceResponse;
  const response = new VoiceResponse();

  // Validate destination conforms to standard international E.164 format:
  // Starts with +, followed by 7 to 15 digits (Supports USA +1, BD +880, UK +44, etc.)
  const e164Regex = /^\+[1-9]\d{6,14}$/;
  if (!e164Regex.test(destinationNumber)) {
    logger.warn('Destination rejected by policy (invalid international E.164 format)', {
      destination: redactPhoneNumber(destinationNumber),
    });
    response.say({ voice: 'Polly.Joanna' }, 'This call cannot be completed. Please dial a valid international phone number including country code.');
    response.hangup();
    return response.toString();
  }

  // Use configured caller ID or fallback to destination
  const callerId = config.TWILIO_PHONE_NUMBER || '+10000000000';

  logger.info('Routing outbound call via TwiML Dial', {
    destination: redactPhoneNumber(destinationNumber),
    callerId: redactPhoneNumber(callerId),
  });

  const dial = response.dial({
    callerId,
    answerOnBridge: true,
  });

  dial.number({
    statusCallback: `${config.PUBLIC_BASE_URL}/api/v1/voice/status`,
    statusCallbackEvent: ['initiated', 'ringing', 'answered', 'completed'],
    statusCallbackMethod: 'POST',
  }, destinationNumber);

  return response.toString();
}
