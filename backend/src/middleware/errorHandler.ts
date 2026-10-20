import { Request, Response, NextFunction } from 'express';
import { logger } from '../logging/logger';

export interface AppError extends Error {
  statusCode?: number;
  code?: string;
}

export function errorHandler(
  err: AppError,
  req: Request,
  res: Response,
  // eslint-disable-next-line @typescript-eslint/no-unused-vars
  next: NextFunction
): void {
  const statusCode = err.statusCode || 500;
  const errorCode = err.code || (statusCode >= 500 ? 'INTERNAL_ERROR' : 'BAD_REQUEST');
  const requestId = (req.headers['x-request-id'] as string) || `req_${Date.now()}`;

  // Log full error internally without leaking to client
  logger.error('Unhandled request error', {
    requestId,
    statusCode,
    errorCode,
    errorMessage: err.message,
    path: req.path,
    method: req.method,
  });

  // Client-safe response
  const clientMessage = statusCode >= 500 && process.env.NODE_ENV === 'production'
    ? 'An unexpected error occurred. Please try again later.'
    : err.message || 'Operation failed';

  res.status(statusCode).json({
    error: {
      code: errorCode,
      message: clientMessage,
      requestId,
    },
  });
}
