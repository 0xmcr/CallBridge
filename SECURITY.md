# Security Policy & Architecture Guarantees

CallBridge is engineered around strict zero-trust telecommunications principles, cryptographic webhook integrity, and absolute user privacy.

---

## 🔒 Security Architecture Guarantees

### 1. Zero Permanent Client Secrets (Android)
- **Zero API Keys in APK:** The Android application contains **zero permanent Twilio credentials**, Auth Tokens, API secrets, or master passwords. Reverse-engineering or decompiling the APK reveals no telecom secrets.
- **Short-Lived Ephemeral Tokens:** The Android client obtains time-limited Voice Access Tokens via `POST /api/v1/voice/token` with an enforced 1-hour Time-to-Live (TTL).
- **In-Memory Volatile Lifecycle:** Voice tokens are stored exclusively in volatile memory during active calling sessions and are never saved to disk, Room database, or Shared Preferences.

### 2. Backend Security & Cryptographic Verification
- **Twilio Signature Verification:** All incoming webhooks (`POST /api/v1/voice/outgoing`, `POST /api/v1/voice/status`, `POST /api/v1/sms/webhook`) are cryptographically verified using Twilio's HMAC-SHA1 signature verification algorithm (`twilio.validateRequest`). Unauthenticated, forged, or unverified requests are rejected immediately with `403 Forbidden`.
- **E.164 Destination Sanitization:** All destination telephone numbers are validated against strict international E.164 formats (`/^\+[1-9]\d{6,14}$/`) to eliminate command injection, carrier manipulation, and unauthorized dialing.
- **Log Privacy & Redaction:** Sensitive phone numbers (e.g. `+1202***0143`) and authentication tokens are automatically masked and redacted by the backend logging layer to prevent leakage in cloud log monitors.

### 3. User Privacy & Local Persistence
- **100% On-Device Data Sovereignty:** Personal contacts, call history logs, and SMS messages are saved strictly in a local SQLite Room database on the user's Android device.
- **Zero Cloud Leakage:** No personal address books, conversation transcripts, or audio recordings are synced to external databases or third-party servers.

---

## 🚨 Reporting a Vulnerability

If you discover a security vulnerability, credential exposure, or exploit in CallBridge:

1. **Do NOT open a public GitHub issue.** Public disclosure puts users and telecom credentials at risk.
2. Email your advisory report with full reproduction steps to:  
   📧 **[maheshwarchandra.dev@gmail.com](mailto:maheshwarchandra.dev@gmail.com)**
3. Include the following in your report:
   - Detailed description of the vulnerability.
   - Affected component (Android App, Backend Server, or Webhook handler).
   - Minimal proof-of-concept (PoC) or step-by-step reproduction guide.
   - Recommended remediation or mitigation if available.

### Response Timeline
- **Initial Response:** Within 24 hours.
- **Status Assessment & Triage:** Within 48 hours.
- **Patch Deployment & Coordinated Release:** Promptly following verification.

---

## 🛡️ Developer Environment Checklist

- [x] Never commit `.env` or sensitive credentials to Git (enforced via `.gitignore`).
- [x] Rotate Twilio Auth Tokens and API Keys immediately if exposed in public logs.
- [x] Enforce HTTPS / TLS 1.3 for all production backend endpoints (`PUBLIC_BASE_URL`).
- [x] Configure Twilio Voice & SMS Geographic Permissions in Twilio Console to prevent unwanted toll fraud.
