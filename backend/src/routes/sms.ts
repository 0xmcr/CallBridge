import { Router, Request, Response } from 'express';
import { sendOutboundSms, handleInboundSmsWebhook, getMessageHistory } from '../services/smsService';
import { logger } from '../logging/logger';

const router = Router();

/**
 * POST /api/v1/sms/send
 * Sends an outbound SMS message to any international destination number.
 */
router.post('/send', async (req: Request, res: Response) => {
  const { to, body } = req.body;

  if (!to || !body || typeof body !== 'string' || body.trim().length === 0) {
    res.status(400).json({
      success: false,
      error: 'Missing recipient "to" or non-empty "body"',
    });
    return;
  }

  try {
    const record = await sendOutboundSms(to, body);
    res.status(200).json({
      success: true,
      message: record,
    });
  } catch (error: any) {
    res.status(500).json({
      success: false,
      error: error.message || 'Failed to dispatch SMS',
    });
  }
});

/**
 * POST /api/v1/sms/incoming
 * Twilio Webhook URL called when an incoming SMS arrives at the Twilio phone number.
 */
router.post('/incoming', (req: Request, res: Response) => {
  const from = (req.body.From as string) || (req.body.from as string) || '';
  const to = (req.body.To as string) || (req.body.to as string) || '';
  const body = (req.body.Body as string) || (req.body.body as string) || '';
  const messageSid = (req.body.MessageSid as string) || (req.body.messageSid as string);

  logger.info('Incoming SMS webhook triggered', { from, to });

  const twiml = handleInboundSmsWebhook(from, to, body, messageSid);
  res.type('text/xml').send(twiml);
});

/**
 * GET /api/v1/sms/messages
 * Retrieves conversation messages for the mobile client.
 */
router.get('/messages', (req: Request, res: Response) => {
  const address = req.query.address as string | undefined;
  const messages = getMessageHistory(address);

  res.setHeader('Cache-Control', 'no-store, no-cache, must-revalidate');
  res.status(200).json({
    success: true,
    messages,
  });
});

export default router;
