import request from 'supertest';
import { app } from '../src/app';

describe('Backend Foundation & Health API', () => {
  it('GET /api/v1/health returns 200 with service information and no secrets', async () => {
    const res = await request(app).get('/api/v1/health');

    expect(res.status).toBe(200);
    expect(res.body.status).toBe('ok');
    expect(res.body.service).toBe('callbridge-api');
    expect(res.body.version).toBe('1.0.0');
    expect(res.body.timestamp).toBeDefined();

    // Verify absolutely no secrets leaked
    expect(res.body.TWILIO_ACCOUNT_SID).toBeUndefined();
    expect(res.body.TWILIO_AUTH_TOKEN).toBeUndefined();
    expect(res.body.env).toBeUndefined();
  });

  it('Unknown routes return standardized 404 error envelope', async () => {
    const res = await request(app).get('/api/v1/unknown-endpoint');

    expect(res.status).toBe(404);
    expect(res.body.error).toBeDefined();
    expect(res.body.error.code).toBe('NOT_FOUND');
    expect(res.body.error.requestId).toBeDefined();
  });

  it('Injects X-Request-Id header into response', async () => {
    const res = await request(app).get('/api/v1/health');
    expect(res.headers['x-request-id']).toBeDefined();
  });
});
