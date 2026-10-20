import { Router, Request, Response } from 'express';

const router = Router();

router.get('/health', (req: Request, res: Response) => {
  res.status(200).json({
    status: 'ok',
    service: 'callbridge-api',
    version: '1.0.0',
    timestamp: new Date().toISOString(),
  });
});

export default router;
