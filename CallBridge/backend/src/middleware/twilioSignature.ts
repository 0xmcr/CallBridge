import { Request, Response, NextFunction } from 'express';
import twilio from 'twilio';
import { config } from '../config/env';
import { logger } from '../logging/logger';

export function verifyTwilioWebhook(
  req: Request,
  res: Response,
  next: NextFunction
): void {
  // In development, if auth token is not set, allow testing
  if (config.NODE_ENV === 'development' && !config.TWILIO_AUTH_TOKEN) {
    logger.debug('Skipping Twilio signature check in development mode');
    next();
    return;
  }

  const twilioSignature = req.headers['x-twilio-signature'] as string;
  const authToken = config.TWILIO_AUTH_TOKEN;

  if (!authToken || !twilioSignature) {
    logger.warn('Webhook rejected: Missing Twilio signature or auth token configuration');
    res.status(403).type('text/xml').send('<Response><Hangup/></Response>');
    return;
  }

  // Construct full external request URL
  const fullUrl = `${config.PUBLIC_BASE_URL}${req.originalUrl}`;
  const isValid = twilio.validateRequest(authToken, twilioSignature, fullUrl, req.body);

  if (!isValid) {
    logger.error('Webhook signature verification failed', {
      url: fullUrl,
    });
    res.status(403).type('text/xml').send('<Response><Hangup/></Response>');
    return;
  }

  next();
}
