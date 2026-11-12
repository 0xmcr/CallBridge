import request from 'supertest';
import { app } from '../src/app';

describe('Twilio Voice API Endpoints', () => {
  describe('POST /api/v1/voice/token', () => {
    it('returns access token with no-store cache headers', async () => {
      const res = await request(app)
        .post('/api/v1/voice/token')
        .send({ identity: 'test_operator' });

      expect(res.status).toBe(200);
      expect(res.body.token).toBeDefined();
      expect(res.body.identity).toBe('test_operator');
      expect(res.body.expiresAt).toBeDefined();
      expect(res.headers['cache-control']).toContain('no-store');
    });
  });

  describe('POST /api/v1/voice/outgoing', () => {
    it('generates valid TwiML <Dial> response for authorized US numbers', async () => {
      const res = await request(app)
        .post('/api/v1/voice/outgoing')
        .type('form')
        .send({ To: '+12025550143' });

      expect(res.status).toBe(200);
      expect(res.headers['content-type']).toContain('text/xml');
      expect(res.text).toContain('<Dial');
      expect(res.text).toContain('+12025550143');
      expect(res.text).toContain('statusCallback');
    });

    it('allows valid international E.164 numbers (e.g. UK +44)', async () => {
      const res = await request(app)
        .post('/api/v1/voice/outgoing')
        .type('form')
        .send({ To: '+442071838750' });

      expect(res.status).toBe(200);
      expect(res.headers['content-type']).toContain('text/xml');
      expect(res.text).toContain('<Dial');
      expect(res.text).toContain('+442071838750');
    });

    it('rejects malformed numbers with policy enforcement and hangup', async () => {
      const res = await request(app)
        .post('/api/v1/voice/outgoing')
        .type('form')
        .send({ To: '+000abc' });

      expect(res.status).toBe(200);
      expect(res.headers['content-type']).toContain('text/xml');
      expect(res.text).toContain('cannot be completed');
      expect(res.text).toContain('<Hangup/>');
    });

    it('returns 400 XML when destination number is completely missing', async () => {
      const res = await request(app)
        .post('/api/v1/voice/outgoing')
        .type('form')
        .send({});

      expect(res.status).toBe(400);
      expect(res.headers['content-type']).toContain('text/xml');
      expect(res.text).toContain('<Hangup/>');
    });
  });

  describe('POST /api/v1/voice/incoming', () => {
    it('routes incoming PSTN call to CallBridge Android client', async () => {
      const res = await request(app)
        .post('/api/v1/voice/incoming')
        .type('form')
        .send({ From: '+14155552671' });

      expect(res.status).toBe(200);
      expect(res.headers['content-type']).toContain('text/xml');
      expect(res.text).toContain('<Dial');
      expect(res.text).toContain('<Client');
      expect(res.text).toContain('cb_operator');
    });
  });

  describe('POST /api/v1/voice/status', () => {
    it('acknowledges status callback with 200 OK', async () => {
      const res = await request(app)
        .post('/api/v1/voice/status')
        .type('form')
        .send({
          CallSid: 'CA1234567890abcdef',
          CallStatus: 'completed',
          CallDuration: '125',
        });

      expect(res.status).toBe(200);
      expect(res.text).toBe('OK');
    });
  });
});
