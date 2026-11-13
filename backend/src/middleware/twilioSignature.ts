import { Request, Response, NextFunction } from 'express';
import twilio from 'twilio';
import { config } from '../config/env';
import { logger } from '../logging/logger';

export function verifyTwilioWebhook(
  req: Request,
  res: Response,
  next: NextFunction
): void {
  // In test mode (Jest), skip signature check unless a signature header is explicitly provided
  if (config.NODE_ENV === 'test' && !req.headers['x-twilio-signature']) {
    next();
    return;
  }

  // In development, if auth token is not set, allow testing
  if (config.NODE_ENV === 'development' && !config.TWILIO_AUTH_TOKEN) {
    logger.debug('Skipping Twilio signature check in development mode');
    next();
    return;
  }

  const twilioSignature = req.headers['x-twilio-signature'] as string;
  const authToken = config.TWILIO_AUTH_TOKEN;

  if (!authToken) {
    logger.debug('Skipping Twilio signature check: no auth token configured');
    next();
    return;
  }

  if (!twilioSignature) {
    logger.warn('Webhook request missing x-twilio-signature header');
    if (process.env.ENFORCE_TWILIO_SIGNATURE === 'true') {
      res.status(403).type('text/xml').send('<Response><Hangup/></Response>');
      return;
    }
    next();
    return;
  }

  // Construct full external request URL
  const base = config.PUBLIC_BASE_URL.replace(/\/+$/, '');
  const path = req.originalUrl.startsWith('/') ? req.originalUrl : `/${req.originalUrl}`;
  const fullUrl = `${base}${path}`;
  const isValid = twilio.validateRequest(authToken, twilioSignature, fullUrl, req.body);

  if (!isValid) {
    logger.warn('Twilio signature validation failed (possible reverse proxy URL header mismatch)', {
      url: fullUrl,
    });
    if (process.env.ENFORCE_TWILIO_SIGNATURE === 'true') {
      res.status(403).type('text/xml').send('<Response><Hangup/></Response>');
      return;
    }
  }

  next();
}
