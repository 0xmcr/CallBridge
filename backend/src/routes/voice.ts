import { Router, Request, Response } from 'express';
import { generateVoiceAccessToken, generateOutboundTwiML, generateInboundTwiML } from '../services/twilioService';
import { verifyTwilioWebhook } from '../middleware/twilioSignature';
import { logger, redactPhoneNumber } from '../logging/logger';

const router = Router();

/**
 * POST /api/v1/voice/token
 * Returns short-lived Voice Access Token for CallBridge Android client
 */
router.post('/token', (req: Request, res: Response) => {
  const identity = (req.body.identity as string) || 'cb_operator';
  const tokenData = generateVoiceAccessToken(identity);

  // Prevent caching of tokens
  res.setHeader('Cache-Control', 'no-store, no-cache, must-revalidate, proxy-revalidate');
  res.setHeader('Pragma', 'no-cache');
  res.setHeader('Expires', '0');

  res.status(200).json(tokenData);
});

/**
 * POST /api/v1/voice/outgoing
 * TwiML Voice URL webhook called by Twilio when Android app initiates an outbound call
 */
router.post('/outgoing', verifyTwilioWebhook, (req: Request, res: Response) => {
  // Destination passed from Twilio Android SDK in 'To' parameter
  const destination = (req.body.To as string) || (req.body.to as string) || '';

  if (!destination) {
    logger.warn('Outgoing call request missing destination number');
    res.status(400).type('text/xml').send('<Response><Say>No destination number was provided.</Say><Hangup/></Response>');
    return;
  }

  // Normalize number format: ensure + prefix
  const normalizedNumber = destination.startsWith('+') ? destination : `+${destination}`;
  const twimlXml = generateOutboundTwiML(normalizedNumber);

  res.type('text/xml').send(twimlXml);
});

/**
 * POST /api/v1/voice/incoming
 * TwiML Voice URL webhook called by Twilio when an external caller dials our Twilio number.
 */
router.post('/incoming', verifyTwilioWebhook, (req: Request, res: Response) => {
  const from = (req.body.From as string) || (req.body.Caller as string) || 'Unknown';
  const twimlXml = generateInboundTwiML(from);

  res.type('text/xml').send(twimlXml);
});

/**
 * POST /api/v1/voice/status
 * Twilio status callback endpoint for call lifecycle tracking
 */
router.post('/status', verifyTwilioWebhook, (req: Request, res: Response) => {
  const callSid = req.body.CallSid as string;
  const callStatus = req.body.CallStatus as string;
  const duration = req.body.CallDuration as string;

  logger.info('Call status callback received', {
    callSid,
    callStatus,
    durationSeconds: duration ? parseInt(duration, 10) : undefined,
  });

  // Acknowledge quickly to Twilio
  res.status(200).send('OK');
});

export default router;
