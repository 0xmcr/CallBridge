# Security Policy & Architecture Guarantees

CallBridge is engineered around strict security, privacy, and zero-trust telecommunications principles.

---

## 🔒 Security Architecture Guarantees

### 1. Zero Permanent Secrets in Client (Android)
- **No API Keys or Secrets in APK:** The Android application contains **zero permanent Twilio credentials**, API secrets, or sensitive tokens. Reverse-engineering or decompiling the APK reveals no provider secrets.
- **Short-Lived Access Tokens:** The mobile client only receives ephemeral Voice Access Tokens via `POST /api/v1/voice/token` with an enforced 1-hour Time-to-Live (TTL).
- **In-Memory Token Lifecycle:** Tokens are held only in volatile memory during active app sessions and are never persisted to disk, Room database, or shared preferences.

### 2. Backend Security & Webhook Cryptography
- **Strict Webhook Signature Verification:** All incoming TwiML webhooks (`POST /api/v1/voice/outgoing`, `POST /api/v1/voice/status`) are cryptographically verified using Twilio's official HMAC-SHA1 signature verification (`twilio.validateRequest`). Unsigned or forged requests are rejected with `403 Forbidden`.
- **Destination Policy Enforcement:** Outbound calling is strictly restricted server-side to authorized E.164 country codes (`+1` US/Canada). Any attempt to dial premium, international, or unapproved numbers is immediately halted with `<Hangup/>`.
- **Rate Limiting:** Backend endpoints are protected by IP and identity rate limiters to prevent token harvesting or brute-force dialing.

### 3. Data Privacy & Local Persistence
- **100% On-Device Privacy:** All contacts and call history logs are stored purely in a local Room (SQLite) database on the user's Android device.
- **Zero Cloud Leakage:** No personal contact lists, call logs, or audio transcripts are synced to external databases or third-party cloud servers.
- **Log Redaction:** The backend logger automatically masks phone numbers (e.g. `+1202***0143`) and redacts tokens/headers to prevent leakage in server log collectors.

---

## 🚨 Reporting a Vulnerability

If you discover a security vulnerability or credential leak within CallBridge:
1. **Do NOT open a public GitHub issue.**
2. Send an advisory report with details and reproduction steps to:  
   📧 **security@callbridge.internal** (or contact the repository maintainer privately).
3. We take security issues seriously and will respond promptly to investigate and deploy patches.

---

## 🛡️ Developer Environment Checklist

- [x] Never commit `.env` to version control (enforced via `.gitignore`).
- [x] Rotate Twilio API keys immediately if exposed.
- [x] Ensure `PUBLIC_BASE_URL` in production uses HTTPS with valid SSL/TLS certificates.
