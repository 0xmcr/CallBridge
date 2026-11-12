/**
 * Firebase Admin SDK Service — FCM V1
 * Uses Service Account credentials to send push notifications
 * via Firebase Cloud Messaging HTTP V1 API.
 *
 * Production (Railway): set FIREBASE_SERVICE_ACCOUNT_JSON env var
 * Local development:    set FIREBASE_SERVICE_ACCOUNT_PATH env var
 *                       or place firebase-adminsdk.json in backend root
 */

import { initializeApp, getApps, cert, App } from 'firebase-admin/app';
import { getMessaging, Message } from 'firebase-admin/messaging';
import { logger } from '../logging/logger';
import * as fs from 'fs';
import * as path from 'path';

let firebaseApp: App | null = null;

function initFirebase(): boolean {
  if (firebaseApp || getApps().length > 0) return true;

  try {
    // Priority 1: JSON contents from environment variable (Railway/cloud)
    const jsonEnv = process.env.FIREBASE_SERVICE_ACCOUNT_JSON;
    if (jsonEnv) {
      const serviceAccount = JSON.parse(jsonEnv);
      firebaseApp = initializeApp({ credential: cert(serviceAccount) });
      logger.info('Firebase Admin SDK initialized from FIREBASE_SERVICE_ACCOUNT_JSON env var');
      return true;
    }

    // Priority 2: Path to JSON file (local development)
    const filePath = process.env.FIREBASE_SERVICE_ACCOUNT_PATH
      || path.join(__dirname, '../../firebase-adminsdk.json');

    if (fs.existsSync(filePath)) {
      const serviceAccount = JSON.parse(fs.readFileSync(filePath, 'utf8'));
      firebaseApp = initializeApp({ credential: cert(serviceAccount) });
      logger.info('Firebase Admin SDK initialized from service account file', { filePath });
      return true;
    }

    logger.warn('Firebase Admin SDK not initialized: no service account credentials found');
    return false;
  } catch (err: any) {
    logger.error('Failed to initialize Firebase Admin SDK', { error: err.message });
    return false;
  }
}

/**
 * Send a data-only FCM push notification to an Android device.
 * Used for delivering incoming call notifications when the app is in background/killed.
 */
export async function sendFcmPushNotification(
  fcmToken: string,
  data: Record<string, string>
): Promise<boolean> {
  if (!initFirebase()) {
    logger.warn('FCM push skipped: Firebase not initialized');
    return false;
  }

  try {
    const message: Message = {
      token: fcmToken,
      data,
      android: {
        priority: 'high',
        ttl: 30000, // 30 seconds — call notification expires quickly
      },
    };

    const result = await getMessaging().send(message);
    logger.info('FCM push notification sent', { messageId: result });
    return true;
  } catch (err: any) {
    logger.error('Failed to send FCM push notification', { error: err.message });
    return false;
  }
}

/**
 * Send incoming call notification to CallBridge Android client.
 * Triggered when Twilio receives an inbound call on our number.
 */
export async function notifyIncomingCall(
  fcmToken: string,
  callerNumber: string,
  callSid: string
): Promise<boolean> {
  return sendFcmPushNotification(fcmToken, {
    type: 'incoming_call',
    caller: callerNumber,
    callSid,
    timestamp: new Date().toISOString(),
  });
}

export function isFirebaseReady(): boolean {
  return initFirebase();
}
