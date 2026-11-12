# CallBridge — Complete Public Setup & Production Deployment Guide

This guide walks you through setting up **CallBridge** from scratch — **Firebase (FCM V1)**, **Twilio (Voice & SMS)**, **Railway (Backend Hosting)**, and **Android APK Build** — with exact click-by-click instructions.

> **Security Note:** Never commit `google-services.json`, `firebase-adminsdk.json`, `.env`, or any Twilio `Account SID` / `Auth Token` / `API Secret` to GitHub. All of these are already protected in `.gitignore`.

---

## Architecture Overview

```text
Android App (Kotlin / Compose)
   │
   ├──► HTTPS REST (JWT Token, SMS, Balance) ──► Backend (Node.js / Railway)
   │                                                  │
   ├──◄────────────────── WebRTC Audio ───────────────┼──► Twilio Voice & SMS
   │                                                  │
   └──◄────────────── FCM V1 Incoming Ring ───────────┴──► Firebase Cloud Messaging
```

---

## Part 1: Firebase Setup (FCM V1 for Incoming Calls)

Firebase is used **only** for Firebase Cloud Messaging (FCM) push notifications so incoming calls ring on Android. It is **100% free** on the Firebase Spark plan.

### Step 1.1: Create a Firebase Project
1. Go to [Firebase Console](https://console.firebase.google.com/) and click **Create a project** (or **Add project**).
2. Enter Project Name: `CallBridge` (or your preferred name).
3. Disable Google Analytics (optional) and click **Create project**.
4. *(Optional)* In **Project Settings (⚙️) → General → Environment**, set **Environment type** to `Production`. (This is just a console label; it does not change any config files.)

### Step 1.2: Register the Android App & Download `google-services.json`
1. In your Firebase Project overview, click the **Android (`</>`)** icon to add an Android app.
2. Fill in:
   - **Android package name:** `com.callbridge.personal` *(must match `applicationId` in `android/app/build.gradle.kts`)*
   - **App nickname:** `CallBridge Android`
   - **Debug signing certificate SHA-1:** Leave blank.
3. Click **Register app**.
4. Click **Download `google-services.json`** and place it at:
   ```text
   android/app/google-services.json
   ```
5. Click **Next / Skip** through the remaining wizard steps (the Firebase SDK is already configured in Gradle).

### Step 1.3: Generate Firebase Admin SDK Key (FCM V1)
Because Google deprecated the legacy Cloud Messaging Server Key, CallBridge uses **FCM HTTP V1** via a Service Account:
1. In Firebase Console, go to **Project Settings (⚙️) → Service accounts** tab.
2. Under **Firebase Admin SDK**, click **Generate new private key** → **Generate key**.
3. Save the downloaded `.json` file safely on your machine:
   - **For local backend testing:** Rename/copy it to `backend/firebase-adminsdk.json` (ignored by Git).
   - **For cloud hosting (Railway):** You will copy the JSON content into the `FIREBASE_SERVICE_ACCOUNT_JSON` environment variable.

---

## Part 2: Railway Setup (24/7 Backend Hosting)

Twilio requires a public **HTTPS** URL to deliver incoming call and SMS webhooks. [Railway](https://railway.app/) provides always-on Node.js hosting (`$5` trial credit to start, `$5/month` Hobby plan afterward).

### Step 2.1: Create Project & Connect GitHub
1. Log in to [railway.app](https://railway.app/) using your **GitHub** account.
2. Click **+ New** → **Deploy from GitHub repo** and select your `CallBridge` repository.

### Step 2.2: Configure Service Settings
Open your service's **Settings** tab and set:
1. **Source → Root Directory:**
   ```text
   backend
   ```
   *(Railway automatically reads `backend/railway.json` for `npm run build` and `npm start`.)*
2. **Networking → Public Networking → Generate Domain:**
   - Target port: `3000`
   - Copy your generated HTTPS domain (e.g., `https://your-app.up.railway.app`).
   - *(Optional)* Click **Custom Domain** to map a subdomain like `api.yourdomain.com` using a `CNAME` record in your DNS provider.
3. **Deploy → Healthcheck Path:**
   ```text
   /api/v1/health
   ```
   - **Healthcheck Timeout:** `30`
4. **Deploy → Serverless:** Keep this **OFF** so the backend stays awake 24/7 for incoming calls.

### Step 2.3: Set Initial Environment Variables
Go to the **Variables** tab → **Raw Editor**, paste the following (we will add Twilio variables in Part 3), and click **Update Variables**:

```env
NODE_ENV=production
PORT=3000
PUBLIC_BASE_URL=https://your-app.up.railway.app
ALLOWED_ORIGINS=*
FIREBASE_PROJECT_ID=your-firebase-project-id
DEFAULT_CLIENT_IDENTITY=cb_operator
```

Verify your server is live by opening:
```text
https://your-app.up.railway.app/api/v1/health
```
You should see `{"status":"ok","service":"callbridge-api",...}`.

---

## Part 3: Twilio Setup (Voice + SMS)

> **Trial vs. Paid:** New Twilio accounts include free trial credit (`~$15`), which lets you buy a US number and test real calls/SMS to **Verified Caller IDs** at zero personal cost. To call any unverified client number worldwide, upgrade your account (`~$20` minimum top-up).

### Step 3.1: Get Account SID & Auth Token
1. Log in to [Twilio Console](https://console.twilio.com/).
2. On the home dashboard under **Account Info**, copy:
   - **Account SID** (`AC...`)
   - **Auth Token**

### Step 3.2: Buy a US Phone Number (`Voice` + `SMS`)
1. Go to **Phone Numbers → Manage → Buy a number** (`https://console.twilio.com/us1/develop/phone-numbers/manage/search`).
2. Select **Country:** `United States (+1)`.
3. Check **Capabilities:** `Voice` and `SMS`.
4. To search a specific area code (e.g., `512` for Austin/Leander TX), set **Search criteria** to `Number`, **Match to** to `First part of number`, and enter the 3-digit area code.
5. Click **Buy** on your chosen number (e.g., `+13254426667`), accept the Emergency Calling terms, and complete the purchase.

### Step 3.3: Configure Number Webhooks (Incoming Calls & SMS)
After purchasing, click **Configure +1...** (or go to **Phone Numbers → Manage → Active numbers** and click your number):
1. **Voice Configuration:**
   - **A call comes in:** `Webhook` | **HTTP POST** | URL:
     ```text
     https://your-app.up.railway.app/api/v1/voice/incoming
     ```
   - **Call status changes:** **HTTP POST** | URL:
     ```text
     https://your-app.up.railway.app/api/v1/voice/status
     ```
2. **Messaging Configuration:**
   - **A message comes in:** `Webhook` | **HTTP POST** | URL:
     ```text
     https://your-app.up.railway.app/api/v1/sms/incoming
     ```
3. Click **Save configuration**.

### Step 3.4: Create an API Key (`SK...` & `Secret`)
1. Go to **Account → API keys & tokens** (`https://console.twilio.com/us1/account/keys-credentials/api-keys`).
2. Click **Create API key**:
   - **Friendly name:** `CallBridge`
   - **Region:** `United States - Default`
   - **Key type:** `Standard`
3. Click **Create** and immediately copy both:
   - **SID** (`SK...`)
   - **Secret** *(shown only once)*

### Step 3.5: Create a TwiML App (`AP...` for Outbound Calls)
1. Go to **Develop → Voice → Manage → TwiML Apps** (`https://console.twilio.com/us1/develop/voice/manage/twiml-apps`).
2. Click **Create new TwiML App**:
   - **Friendly Name:** `CallBridge`
   - **Voice Configuration → Request URL (`HTTP POST`):**
     ```text
     https://your-app.up.railway.app/api/v1/voice/outgoing
     ```
   - Leave Messaging, Fallback, and Status Callback blank.
3. Click **Create**, then click on **CallBridge** in the list and copy the **TwiML App SID** (`AP...`).

### Step 3.6: (Optional for Trial) Verify Your Test Phone Number
If you are on a Twilio Trial account, add your personal phone number under **Phone Numbers → Manage → Verified Caller IDs** (`https://console.twilio.com/us1/develop/phone-numbers/manage/verified`) so you can test real calls and SMS before upgrading.

### Step 3.7: Add All Twilio Variables to Railway
Go back to **Railway → Variables → Raw Editor**, add your Twilio credentials, and click **Update Variables**:

```env
NODE_ENV=production
PORT=3000
PUBLIC_BASE_URL=https://your-app.up.railway.app
ALLOWED_ORIGINS=*
FIREBASE_PROJECT_ID=your-firebase-project-id
DEFAULT_CLIENT_IDENTITY=cb_operator
TWILIO_ACCOUNT_SID=ACxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxx
TWILIO_AUTH_TOKEN=xxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxx
TWILIO_API_KEY_SID=SKxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxx
TWILIO_API_KEY_SECRET=xxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxx
TWILIO_TWIML_APP_SID=APxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxx
TWILIO_PHONE_NUMBER=+1XXXXXXXXXX
```

Verify live Twilio integration by opening:
```text
https://your-app.up.railway.app/api/v1/account/status
```
You should see `"success": true`, your live `"balance"`, and your purchased `"phoneNumbers"`.

---

## Part 4: Build & Connect the Android App

### Step 4.1: Build the Debug APK
Ensure `android/app/google-services.json` is in place, then run:
```bash
cd android
./gradlew assembleDebug
```
The compiled APK will be output to:
```text
android/app/build/outputs/apk/debug/app-debug.apk
```

### Step 4.2: Connect the App to Your Backend
1. Install the APK on your Android device and grant **Microphone** permission when prompted.
2. Open the **Settings** tab inside CallBridge:
   - **Backend Bridge URL:** `https://your-app.up.railway.app`
   - **Active US Caller ID:** `+1XXXXXXXXXX` (your Twilio number)
3. Tap **Test Connection** — it should turn green and display your live Twilio balance on the **Dashboard**.
4. Open the **Dial Pad**, dial your verified phone number, and place your first VoIP call!
