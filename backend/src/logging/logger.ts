export function redactPhoneNumber(phone: string): string {
  if (!phone || phone.length < 6) return '***';
  return phone.slice(0, 5) + '***' + phone.slice(-3);
}

export function redactSecret(secret: string): string {
  if (!secret) return '';
  return secret.slice(0, 4) + '...' + secret.slice(-4);
}

export const logger = {
  info: (message: string, meta: Record<string, unknown> = {}) => {
    console.log(JSON.stringify({
      level: 'info',
      timestamp: new Date().toISOString(),
      message,
      ...meta,
    }));
  },
  warn: (message: string, meta: Record<string, unknown> = {}) => {
    console.warn(JSON.stringify({
      level: 'warn',
      timestamp: new Date().toISOString(),
      message,
      ...meta,
    }));
  },
  error: (message: string, meta: Record<string, unknown> = {}) => {
    console.error(JSON.stringify({
      level: 'error',
      timestamp: new Date().toISOString(),
      message,
      ...meta,
    }));
  },
  debug: (message: string, meta: Record<string, unknown> = {}) => {
    if (process.env.NODE_ENV !== 'production') {
      console.debug(JSON.stringify({
        level: 'debug',
        timestamp: new Date().toISOString(),
        message,
        ...meta,
      }));
    }
  },
};
