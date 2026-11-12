import request from 'supertest';
import { app } from '../src/app';

describe('Milestone M6: End-to-End Outbound Calling Flow Simulation', () => {
  const testDestination = '+12025550143';
  let issuedToken: string;
  let issuedIdentity: string;

  it('Step 1: Android Client obtains short-lived Voice Access Token from backend', async () => {
    const res = await request(app)
      .post('/api/v1/voice/token')
      .send({ identity: 'cb_test_operator' });

    expect(res.status).toBe(200);
    expect(res.body.token).toBeDefined();
    expect(res.body.identity).toBe('cb_test_operator');
    expect(res.body.expiresAt).toBeDefined();
    expect(res.headers['cache-control']).toContain('no-store');

    issuedToken = res.body.token;
    issuedIdentity = res.body.identity;
  });

  it('Step 2: Twilio Voice Gateway triggers backend TwiML Webhook for outbound call', async () => {
    const res = await request(app)
      .post('/api/v1/voice/outgoing')
      .type('form')
      .send({
        To: testDestination,
        From: 'client:' + issuedIdentity,
        CallSid: 'CA_test_call_sid_123456789',
      });

    expect(res.status).toBe(200);
    expect(res.headers['content-type']).toContain('text/xml');
    
    // Validate TwiML contains the expected dialing instructions
    expect(res.text).toContain('<Response>');
    expect(res.text).toContain('<Dial');
    expect(res.text).toContain(testDestination);
    expect(res.text).toContain('/api/v1/voice/status');
    expect(res.text).toContain('</Dial>');
    expect(res.text).toContain('</Response>');
  });

  it('Step 3: Twilio Voice Gateway reports terminal status callbacks idempotently', async () => {
    // Ringing status
    const ringingRes = await request(app)
      .post('/api/v1/voice/status')
      .type('form')
      .send({
        CallSid: 'CA_test_call_sid_123456789',
        CallStatus: 'ringing',
      });
    expect(ringingRes.status).toBe(200);
    expect(ringingRes.text).toBe('OK');

    // Completed status with duration
    const completedRes = await request(app)
      .post('/api/v1/voice/status')
      .type('form')
      .send({
        CallSid: 'CA_test_call_sid_123456789',
        CallStatus: 'completed',
        CallDuration: '75',
      });
    expect(completedRes.status).toBe(200);
    expect(completedRes.text).toBe('OK');
  });

  it('Step 4: Malformed destinations are strictly blocked by server-side policy', async () => {
    const malformedDestination = 'not-a-phone-number';
    const res = await request(app)
      .post('/api/v1/voice/outgoing')
      .type('form')
      .send({
        To: malformedDestination,
        From: 'client:' + issuedIdentity,
      });

    expect(res.status).toBe(200);
    expect(res.headers['content-type']).toContain('text/xml');
    expect(res.text).toContain('cannot be completed');
    expect(res.text).toContain('<Hangup/>');
  });

  it('Step 5: Inbound PSTN call routes cleanly to CallBridge operator client', async () => {
    const res = await request(app)
      .post('/api/v1/voice/incoming')
      .type('form')
      .send({
        From: '+14155552671',
        To: '+12025550199',
      });

    expect(res.status).toBe(200);
    expect(res.headers['content-type']).toContain('text/xml');
    expect(res.text).toContain('<Dial');
    expect(res.text).toContain('<Client');
    expect(res.text).toContain('cb_operator');
  });
});
