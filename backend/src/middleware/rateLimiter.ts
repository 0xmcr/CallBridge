import { Request, Response, NextFunction } from 'express';
import { config } from '../config/env';

interface ClientRecord {
  count: number;
  resetTime: number;
}

const clientMap = new Map<string, ClientRecord>();

export function rateLimiter(
  req: Request,
  res: Response,
  next: NextFunction
): void {
  const ip = req.ip || req.socket.remoteAddress || 'unknown';
  const now = Date.now();
  const windowMs = config.RATE_LIMIT_WINDOW_SECONDS * 1000;

  const record = clientMap.get(ip);

  if (!record || now > record.resetTime) {
    clientMap.set(ip, {
      count: 1,
      resetTime: now + windowMs,
    });
    next();
    return;
  }

  if (record.count >= config.RATE_LIMIT_MAX_REQUESTS) {
    res.status(429).json({
      error: {
        code: 'RATE_LIMITED',
        message: 'Too many requests. Please slow down and try again later.',
        requestId: (req.headers['x-request-id'] as string) || `req_${Date.now()}`,
      },
    });
    return;
  }

  record.count += 1;
  next();
}
