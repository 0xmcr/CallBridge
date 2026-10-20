# 📞 CallBridge — End-to-End Live Calling Verification Guide

This guide walks you through verifying an actual end-to-end outbound telephone call using CallBridge with either a **Twilio Trial Account** or an **Upgraded Paid Account**.

---

## 🏗️ The End-to-End Calling Architecture

```text
[ CallBridge Android App ]
          │
          │ 1. Request Voice Token (HTTPS)
          ▼
[ Node.js Backend Server ]  ── 2. Issue Voice Access Token ──► [ CallBridge App ]
                                                                        │
                                   3. Connect to Twilio (SDK) ──────────┘
                                   │
                                   ▼
                       [ Twilio Voice Platform ]
                                   │
                                   │ 4. HTTP POST Voice Webhook (TwiML App URL)
                                   ▼
                       [ Node.js Backend Server ]
                                   │
                                   │ 5. Return TwiML <Dial callerId="...">+1XXXXXXXXXX</Dial>
                                   ▼
                       [ Twilio Voice Platform ]
                                   │
                                   │ 6. Outbound PSTN Call
                                   ▼
                       [ Destination US Phone Number ]
```

---

## 🛠️ Step 1: Configure Backend Environment Variables

1. Navigate to the backend directory:
   ```bash
   cd "CallBridge/backend"
   ```
2. Copy the sample environment file to `.env`:
   ```bash
   cp .env.example .env
   ```
3. Open `.env` and fill in your Twilio credentials from the [Twilio Console](https://console.twilio.com/):
   ```env
   NODE_ENV=development
   PORT=3000
   PUBLIC_BASE_URL=https://your-public-url.ngrok-free.app

   TWILIO_ACCOUNT_SID=ACxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxx
   TWILIO_API_KEY_SID=SKxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxx
   TWILIO_API_KEY_SECRET=your_api_key_secret_here
   TWILIO_TWIML_APP_SID=APxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxx
   TWILIO_AUTH_TOKEN=your_auth_token_here

   TWILIO_PHONE_NUMBER=+1XXXXXXXXXX
   ALLOWED_CALL_COUNTRY_CODES=+1
   ```

> ⚠️ **Security Warning:** Never commit `.env` to Git. It is already safely listed in `.gitignore`.

---

## 🌐 Step 2: Expose Your Local Backend to the Internet (for Webhooks)

Twilio needs to send HTTP requests to your backend when a call connects. Use a secure tunnel:

```bash
# Using ngrok:
ngrok http 3000
```
Copy the generated forwarding URL (e.g., `https://abc-123.ngrok-free.app`) and set it as `PUBLIC_BASE_URL` in `.env`.

---

## ⚙️ Step 3: Configure TwiML App in Twilio Console

1. In the Twilio Console, go to **Voice > Manage > TwiML apps**.
2. Click **Create TwiML App** (Name: `CallBridge Voice App`).
3. Under **Voice Configuration**:
   - **Request URL:** `https://your-public-url.ngrok-free.app/api/v1/voice/outgoing`
   - **HTTP Method:** `POST`
   - **Status Callback URL:** `https://your-public-url.ngrok-free.app/api/v1/voice/status`
   - **Status Callback Method:** `POST`
4. Click **Save Changes** and copy the **TwiML App SID** (`APxxxxxxxx...`) into your backend `.env` file as `TWILIO_TWIML_APP_SID`.

---

## 📱 Step 4: Verify Twilio Geographic Permissions & Trial Destination

1. In the Twilio Console, navigate to **Voice > Settings > Geographic Permissions**.
2. Ensure **United States (+1)** is checked and enabled.
3. If using a **Trial Account**:
   - In Trial mode, Twilio only allows outbound calls to phone numbers that are verified under **Phone Numbers > Manage > Verified Caller IDs**.
   - Verify your test destination number (e.g., your personal phone number or a US test number) before placing the call.

---

## 🚀 Step 5: Start the Backend & Place the Call

1. Start your backend server:
   ```bash
   cd "CallBridge/backend"
   npm run dev
   ```
2. Open CallBridge in Android Studio and run on your Android device or emulator.
3. Tap **New Call** on the Dashboard.
4. Dial your permitted US test number (e.g., `+1 (202) 555-0143`).
5. Tap the green **Call** button.
6. Grant microphone permission if prompted.
7. Observe the Call Lifecycle:
   - Screen displays `Securing Twilio Access Token...`
   - Moves to `Connecting to US Telecom Gateway...`
   - Transitions to `Ringing...`
   - When answered, transitions to `Connected (Twilio PSTN)` and starts the running timer (`00:01`, `00:02`...).
8. Tap **End Call**:
   - Call disconnects.
   - Screen displays `Call Ended`.
   - Call record is saved once into the local Room database with duration and timestamp.
