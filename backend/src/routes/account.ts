import { Router, Request, Response } from 'express';
import { getTwilioAccountStatus } from '../services/twilioService';

const router = Router();

/**
 * GET /api/v1/account/status
 * Fetches real-time Twilio balance, currency, account status, and phone numbers.
 */
router.get('/status', async (_req: Request, res: Response) => {
  try {
    const statusData = await getTwilioAccountStatus();
    res.setHeader('Cache-Control', 'no-store, no-cache, must-revalidate');
    res.status(200).json(statusData);
  } catch (error: any) {
    res.status(500).json({
      success: false,
      error: error.message || 'Failed to fetch account status',
    });
  }
});

export default router;
