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

export function generateOutboundTwiML(destinationNumber: string): string {
  const VoiceResponse = twilio.twiml.VoiceResponse;
  const response = new VoiceResponse();

  // Validate destination starts with +1 (US Policy)
  if (!destinationNumber.startsWith('+1')) {
    logger.warn('Destination rejected by policy (non-US number)', {
      destination: redactPhoneNumber(destinationNumber),
    });
    response.say({ voice: 'Polly.Joanna' }, 'This call cannot be completed. Only United States phone numbers are permitted by CallBridge policy.');
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
