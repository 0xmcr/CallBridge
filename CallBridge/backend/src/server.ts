import { app } from './app';
import { config } from './config/env';
import { logger } from './logging/logger';

const server = app.listen(config.PORT, () => {
  logger.info(`🚀 CallBridge Backend Server running`, {
    port: config.PORT,
    environment: config.NODE_ENV,
    publicUrl: config.PUBLIC_BASE_URL,
  });
});

process.on('SIGTERM', () => {
  logger.info('SIGTERM received. Shutting down gracefully...');
  server.close(() => {
    logger.info('Server closed');
    process.exit(0);
  });
});
