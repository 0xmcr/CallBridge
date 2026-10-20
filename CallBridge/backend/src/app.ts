import express, { Request, Response } from 'express';
import helmet from 'helmet';
import cors from 'cors';
import healthRouter from './routes/health';
import { rateLimiter } from './middleware/rateLimiter';
import { errorHandler } from './middleware/errorHandler';

export const app = express();

// Security headers
app.use(helmet());

// CORS configuration
app.use(cors({
  origin: '*', // Customize per deployment
  methods: ['GET', 'POST', 'OPTIONS'],
}));

// Body parsing with limits
app.use(express.json({ limit: '100kb' }));
app.use(express.urlencoded({ extended: true, limit: '100kb' }));

// Global Request-ID injector
app.use((req, res, next) => {
  if (!req.headers['x-request-id']) {
    req.headers['x-request-id'] = `cb_${Date.now()}_${Math.random().toString(36).substring(2, 8)}`;
  }
  res.setHeader('X-Request-Id', req.headers['x-request-id'] as string);
  next();
});

// Apply rate limiter to API routes
app.use('/api/v1', rateLimiter);

// Routes
app.use('/api/v1', healthRouter);

// 404 Handler
app.use((req: Request, res: Response) => {
  res.status(404).json({
    error: {
      code: 'NOT_FOUND',
      message: `Cannot ${req.method} ${req.path}`,
      requestId: req.headers['x-request-id'] as string,
    },
  });
});

// Centralized safe error handler
app.use(errorHandler);
