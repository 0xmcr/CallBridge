# CallBridge --- Backend Schema and API Contract

**Version:** MVP 1.0\
**Primary principle:** backend handles provider secrets and call
control; Android stores contacts/history locally by default.\
**Last updated:** 2026-10-20

## 1. Scope and design choice

For MVP, the backend provides: 1. Health/readiness information. 2.
Authenticated issuance of short-lived Twilio Voice Access Tokens. 3. A
TwiML App Voice URL that validates destination and returns
provider-compatible TwiML. 4. A provider status-callback endpoint, if
configured.

Contacts and call history remain in the Android Room database by
default. Do not add a cloud database merely because it is listed in this
document. Remote persistence requires a separate product/privacy
decision.

## 2. Architecture

``` text
Android App ──HTTPS──> API / Token Service
                           │
                           ├── Twilio SDK: create short-lived Voice Access Token
                           ├── Voice URL: validate destination → return TwiML
                           └── Status callback: verify signature → idempotent handling
                                      │
                                      ▼
                                Twilio Voice
```

The token API is not the TwiML voice URL. Token issuance authorizes the
SDK identity; the provider's Voice URL controls how an outgoing call is
routed.

## 3. Identifiers and data conventions

-   API prefix: `/api/v1`
-   JSON uses camelCase.
-   Times use UTC ISO-8601 strings in API payloads; local Android
    storage may use epoch milliseconds consistently.
-   Phone numbers are normalized to E.164 when valid.
-   All IDs are opaque; do not encode phone numbers, names, or other
    personal data into IDs.
-   All non-local production endpoints require HTTPS.
-   Every request should have a server-generated `requestId` for
    diagnostics.

## 4. Authentication model

The production token endpoint must not be an unauthenticated public
token mint. For a personal-use MVP, choose one secure, explicit
mechanism before deployment: - A backend-managed user session with a
login mechanism; or - A narrowly scoped, revocable app credential
exchanged securely for a short-lived session, with abuse controls; or -
A private-network/zero-trust gateway if the deployment is truly private
and the client can use it safely.

Do not embed a permanent shared secret in the APK and treat it as
secure. A value extracted from a mobile app should be considered public.
The selected authentication design must support revocation and rate
limiting.

## 5. Environment variables

Illustrative variables; keep only those actually required by
implementation: - `NODE_ENV` - `PORT` - `PUBLIC_BASE_URL` -
`TWILIO_ACCOUNT_SID` - `TWILIO_API_KEY_SID` - `TWILIO_API_KEY_SECRET` -
`TWILIO_TWIML_APP_SID` - `TWILIO_AUTH_TOKEN` (for webhook signature
verification and only where needed) - `TOKEN_TTL_SECONDS` -
`ALLOWED_CALL_COUNTRY_CODES` - `RATE_LIMIT_WINDOW_SECONDS` -
`RATE_LIMIT_MAX_REQUESTS` - `LOG_LEVEL`

Store these in the deployment platform's secret manager or protected
environment. `.env.example` contains placeholder names only. Never
commit `.env`.

## 6. API endpoints

### 6.1 `GET /api/v1/health`

**Purpose:** basic liveness/readiness without leaking sensitive
configuration.

Example response:

``` json
{
  "status": "ok",
  "service": "callbridge-api",
  "version": "1.0.0",
  "timestamp": "2026-10-20T14:00:00Z"
}
```

Rules: - Do not include account SID, phone number, secrets, raw
environment variables, or full dependency diagnostics. - Consider a
separate internal readiness probe for detailed dependency checks.

### 6.2 `POST /api/v1/voice/token`

**Purpose:** issue a short-lived Access Token containing the required
Voice Grant for an authorized app identity.

Request:

``` json
{
  "deviceId": "opaque-installation-id"
}
```

Success response:

``` json
{
  "token": "<short-lived-access-token>",
  "identity": "cb_<opaque-user-id>",
  "expiresAt": "2026-10-20T15:00:00Z"
}
```

Validation: - Authenticated session required. - `deviceId` is optional
unless device registration is actually implemented; if accepted,
validate length and character set. - Apply per-user and per-IP rate
limits. - Create an identity that is unique and provider-valid but
contains no direct personal data. - Generate a token with only required
Voice capabilities and the configured outgoing application. - Set
`Cache-Control: no-store` and appropriate content type. - Never cache or
log the token. - Return a generic safe error on provider/configuration
failures. - Do not send the Account Auth Token or API secret to the
client.

Potential error codes: - `UNAUTHORIZED` - `RATE_LIMITED` -
`CALLING_NOT_CONFIGURED` - `TOKEN_UNAVAILABLE` - `INTERNAL_ERROR`

### 6.3 TwiML App Voice URL --- example `POST /api/v1/voice/outgoing`

**Purpose:** provider callback invoked for an SDK-originated outgoing
call. Exact parameters and method must match the Twilio Voice SDK/TwiML
App configuration.

Processing: 1. Read the raw provider form parameters in the format
expected by the SDK. 2. Verify the provider signature using the official
SDK helper, the exact externally visible URL, and request parameters. 3.
Extract destination from the documented parameter used by the selected
SDK setup. 4. Normalize and validate destination. 5. Enforce the
server-side allowlist and account restrictions. 6. Reject disallowed
destinations with safe provider-compatible behavior. 7. Return TwiML
with the correct `Content-Type: text/xml` (or provider-documented
content type) and a `<Dial>` to the validated destination. 8. Configure
status callback URL/events only as needed. 9. Avoid accepting
caller-controlled arbitrary TwiML or caller-supplied routing
instructions.

Illustrative TwiML only; generate it with the official Twilio library
where possible:

``` xml
<Response>
  <Dial callerId="+1XXXXXXXXXX" answerOnBridge="true">
    <Number statusCallback="https://api.example.com/api/v1/voice/status"
            statusCallbackEvent="initiated ringing answered completed">+1XXXXXXXXXX</Number>
  </Dial>
</Response>
```

The caller ID must be a number or identity the account is authorized to
use and that meets provider rules. Never hardcode the example
placeholder. Verify the actual Voice SDK/TwiML parameter conventions in
the current provider documentation before implementation.

### 6.4 `POST /api/v1/voice/status`

**Purpose:** receive provider status callbacks.

Rules: - Verify signature before trusting any fields. - Treat provider
callbacks as at-least-once: duplicate delivery is possible. - Use
provider call SID plus event/status as an idempotency key if server
persistence is enabled. - Validate status against a known set and
tolerate out-of-order delivery. - Return quickly; avoid long-running
work in the webhook. - Do not trust Android client status as
provider-authoritative. - Store only minimal fields if retention is
necessary. - Do not log full callback bodies when they contain personal
information.

Example minimal internal record (only if persistence is enabled):

``` json
{
  "providerCallSid": "CAxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxx",
  "status": "completed",
  "timestamp": "2026-10-20T14:10:00Z",
  "durationSeconds": 125
}
```

### 6.5 Optional authenticated configuration endpoint

An endpoint such as `GET /api/v1/config` may return non-secret
capabilities:

``` json
{
  "callingEnabled": true,
  "allowedCountryCodes": ["+1"],
  "incomingCallsEnabled": false
}
```

Only add this endpoint if the app needs it. Do not expose internal
configuration or provider credentials.

## 7. Data schema

### MVP: Android local database (Room)

#### `contacts`

  Field           Type          Constraints
  --------------- ------------- ----------------------------------
  `id`            UUID/String   Primary key
  `displayName`   String        Required, trimmed, non-empty
  `phoneE164`     String        Required; normalized where valid
  `company`       String?       Optional
  `notes`         String?       Optional; treat as sensitive
  `followUpAt`    Long?         Optional UTC epoch milliseconds
  `createdAt`     Long          Required
  `updatedAt`     Long          Required

Indexes: `phoneE164`, `displayName`, `updatedAt`.

#### `call_history`

  -----------------------------------------------------------------------
  Field                   Type                    Constraints
  ----------------------- ----------------------- -----------------------
  `id`                    UUID/String             Primary key

  `contactId`             UUID/String?            Nullable foreign key;
                                                  use `SET_NULL` behavior

  `destinationE164`       String                  Required when known

  `direction`             Enum/String             MVP: `OUTGOING`

  `status`                Enum/String             `COMPLETED`, `FAILED`,
                                                  `CANCELLED`, `UNKNOWN`

  `startedAt`             Long?                   Nullable if not known

  `connectedAt`           Long?                   Nullable

  `endedAt`               Long?                   Nullable

  `durationSeconds`       Long?                   Null unless reliably
                                                  known

  `providerCallSid`       String?                 Optional; not a secret

  `errorCode`             String?                 Stable mapped code; no
                                                  stack trace

  `note`                  String?                 Optional only if
                                                  product supports
                                                  call-level notes

  `createdAt`             Long                    Required
  -----------------------------------------------------------------------

Indexes: `startedAt`, `contactId`, `destinationE164`, `status`.

Schema rules: - Do not delete history when a contact is deleted; set
`contactId` to null. - Keep call history insertion idempotent at the
repository/use-case layer. - Use migrations; never drop user data as an
automatic migration shortcut. - Keep notes local unless remote storage
is explicitly approved. - Consider whether provider call SID retention
is necessary and document it.

### Optional server persistence (not MVP by default)

If future remote reporting/backup is approved, a relational schema may
include: - `users(id, auth_subject, created_at, status)` -
`devices(id, user_id, device_id_hash, created_at, revoked_at)` -
`provider_call_events(id, provider_call_sid, event_status, event_time, duration_seconds, created_at)` -
`idempotency_keys(key, scope, expires_at)` -
`audit_events(id, actor_id, event_type, request_id, created_at, redacted_metadata)`

Do not mirror contacts, notes, or full numbers to the server without an
explicit product/privacy decision. Define access controls, deletion,
retention, backups, and breach response before enabling these tables.

## 8. Validation and policy

-   Parse and normalize phone numbers using a maintained library.
-   MVP policy is US destinations (`+1`) only, unless explicitly changed
    after provider/legal review.
-   Syntax validation does not prove a number is assigned, reachable, or
    permitted.
-   Reject emergency numbers, short codes, malformed values, and
    destinations prohibited by provider/account policy.
-   Enforce destination restrictions on the server, not only in the UI.
-   Add request body size limits and timeouts.
-   Do not let a client select arbitrary caller ID or arbitrary TwiML.

## 9. Webhook security

-   Use official provider signature-validation helpers.
-   Validate against the exact externally visible URL; reverse-proxy URL
    rewriting must be configured correctly.
-   Do not disable signature checks in production.
-   Reject malformed or missing signatures.
-   Keep webhook secrets protected.
-   Test valid signature, invalid signature, missing signature, URL
    mismatch, and duplicate callbacks.
-   Webhook endpoints should not use normal mobile-session auth if the
    provider cannot provide that session; signature verification is the
    relevant trust mechanism.

## 10. Error response format

``` json
{
  "error": {
    "code": "DESTINATION_NOT_ALLOWED",
    "message": "This destination is not supported by the current calling configuration.",
    "requestId": "req_opaque"
  }
}
```

Client-safe messages must not expose internal details. Use HTTP status
codes consistently: - `400` invalid request - `401` unauthenticated -
`403` authenticated but not permitted - `404` unknown route/resource -
`429` rate limited - `500` unexpected server error - `503` calling
provider/config unavailable

Webhook responses should follow the provider's expected contract rather
than returning a generic JSON envelope when TwiML is required.

## 11. Logging and retention

-   Structured logs: timestamp, requestId, route, status code, latency,
    stable error code.
-   Redact Authorization headers, tokens, secrets, full phone numbers,
    notes, and provider payloads.
-   Set a limited log retention period.
-   Never log raw token responses.
-   Define data deletion and backup retention before enabling
    server-side personal data.
-   Keep client crash reports free of sensitive call payloads.

## 12. Deployment checklist

-   [ ] HTTPS and public URL configured.
-   [ ] Secret manager/environment variables configured.
-   [ ] Token endpoint authentication and rate limiting enabled.
-   [ ] TwiML App Voice URL points to correct route.
-   [ ] Status callback URL is correct if used.
-   [ ] Provider webhook signature verification passes.
-   [ ] Destination allowlist is enforced server-side.
-   [ ] Health endpoint exposes no secrets.
-   [ ] Staging call verified with authorized account and permitted
    destination.
-   [ ] Production configuration is separated from staging.
-   [ ] Provider eligibility, number regulatory requirements, and
    pricing reviewed.
