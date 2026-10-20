# CallBridge --- Technical Requirements Document (TRD)

**Status:** Implementation baseline; validate dependency versions and
provider requirements before coding\
**Architecture:** Native Android client + HTTPS backend + Twilio Voice\
**Last updated:** 2026-10-20

## 1. Technical objectives

Build a secure, maintainable Android client for outbound VoIP-to-PSTN
calling using the official Twilio Voice Android SDK. The client must not
contain permanent Twilio credentials. A backend issues short-lived Voice
Access Tokens and supplies the server-side call-control endpoint/TwiML
configuration required by the provider.

## 2. Proposed stack

### Android

-   Kotlin
-   Jetpack Compose
-   Material 3
-   AndroidX ViewModel and lifecycle-aware state collection
-   Kotlin Coroutines and Flow
-   Room for local contacts and call history
-   Retrofit or Ktor Client for HTTPS API calls (choose one; default
    recommendation: Retrofit)
-   Kotlin serialization or Moshi for JSON (choose one consistently;
    default: Kotlin serialization if compatible with selected stack)
-   Hilt or manual dependency injection (MVP can use Hilt if project
    complexity warrants it)
-   Official Twilio Voice Android SDK; verify latest supported version
    and setup instructions before pinning dependency versions.

### Backend

-   Node.js LTS supported by the chosen deployment platform
-   TypeScript
-   Express or Fastify (choose one; default: Express for broad ecosystem
    familiarity)
-   Twilio server SDK
-   Schema validation (Zod or equivalent)
-   Structured logger with redaction
-   Environment-based configuration
-   PostgreSQL only if server-side persistence is enabled; MVP can avoid
    a remote database for personal contacts/history.

### Infrastructure

-   HTTPS endpoint with valid TLS
-   Secret manager or protected environment variables
-   Staging and production configuration separation
-   Optional managed database only for approved remote data features
-   CI checks for formatting, lint, unit tests, and Android build

## 3. High-level architecture

``` text
Android UI (Compose)
  ├── ViewModels / UI state
  ├── Domain use cases
  ├── Repositories
  │    ├── Room (contacts, local call history)
  │    └── HTTPS API client
  └── Twilio Voice Android SDK
             │
             ├── HTTPS: request short-lived Access Token
             ▼
Node.js + TypeScript backend
  ├── Auth/session middleware (required if exposed beyond trusted personal use)
  ├── Token service (Voice Grant, identity)
  ├── Voice webhook / TwiML App endpoint
  ├── Status callback endpoint
  ├── Request validation and rate limiting
  └── Secret configuration
             │
             ▼
       Twilio Voice Platform
             │
             ▼
        US PSTN destination
```

The Android app must call the backend for a token, then use the SDK to
connect. The backend's TwiML App voice URL returns valid TwiML that
connects the call to an allowed destination. The exact Voice SDK
parameter names and TwiML attributes must follow the selected current
SDK/provider documentation.

## 4. Environments and configuration

Use separate `development`, `staging`, and `production` configurations.
Do not commit `.env` files or secrets.

Backend environment variables (illustrative names): - `NODE_ENV` -
`PORT` - `PUBLIC_BASE_URL` - `TWILIO_ACCOUNT_SID` -
`TWILIO_API_KEY_SID` - `TWILIO_API_KEY_SECRET` -
`TWILIO_TWIML_APP_SID` - `TWILIO_AUTH_TOKEN` (only where required for
webhook signature validation / provider operations) -
`ALLOWED_CALL_COUNTRY_CODES` (MVP default policy: `+1`, subject to
explicit product configuration) - `TOKEN_TTL_SECONDS` (short-lived; set
within provider-supported limits) - `RATE_LIMIT_WINDOW_SECONDS` -
`RATE_LIMIT_MAX_REQUESTS` - `LOG_LEVEL`

Only configure variables that the implementation actually uses. Prefer
API Key SID/Secret for token generation rather than using the primary
Account Auth Token for all operations. Protect all secrets in the
deployment platform's secret store.

Android configuration may include: - `API_BASE_URL` per build type -
non-secret feature flags - app version/build number

Never place `TWILIO_AUTH_TOKEN`, API secret, or other permanent
credential in `BuildConfig`, resources, assets, or source code. An API
base URL is not a secret.

## 5. Android modules/packages

Suggested single-app module structure:

``` text
android/
  app/src/main/
    AndroidManifest.xml
    java/com/callbridge/personal/
      CallBridgeApplication.kt
      MainActivity.kt
      navigation/
      ui/
        dashboard/
        dialpad/
        activecall/
        contacts/
        history/
        settings/
        components/
        theme/
      domain/
        model/
        repository/
        usecase/
      data/
        local/
          dao/
          entity/
          database/
        remote/
          api/
          dto/
        repository/
      calling/
        TwilioCallManager.kt
        CallState.kt
        CallEventMapper.kt
      core/
        validation/
        error/
        logging/
```

Keep UI composables stateless where practical; ViewModels own screen
state and call commands. Avoid coupling UI directly to Room, Retrofit,
or Twilio SDK callbacks.

## 6. Android permissions and lifecycle

-   `INTERNET` permission is required.
-   Request `RECORD_AUDIO` at runtime before enabling a
    microphone-dependent call.
-   Add notification permissions only when a feature requires
    notifications on relevant Android versions.
-   Do not add contacts permission if the app only uses its own Room
    contact book.
-   Avoid requesting phone/SMS/call-log permissions unless a future
    approved feature truly needs them.
-   Handle Activity recreation and app backgrounding without losing call
    state.
-   For MVP, clearly document behavior if the process is killed during a
    call. Do not promise robust incoming/background call behavior until
    Telecom/FCM integration is implemented.
-   End or reconcile active calls carefully during lifecycle changes; do
    not accidentally initiate duplicate calls after recomposition.

## 7. Calling subsystem

Create a `CallManager` abstraction so SDK callbacks do not leak into UI.
Recommended responsibilities: - Request/receive token through a token
repository. - Start an outgoing call with validated destination and
required custom parameters. - Subscribe to SDK call events. - Expose a
`StateFlow<CallUiState>`. - Implement mute, speaker routing if supported
by SDK/device, and end-call. - Enforce one active call at a time. -
Produce a terminal call result for persistence. - Redact sensitive
values from logs.

Suggested state model: `Idle`, `RequestingPermission`, `FetchingToken`,
`Connecting`, `Ringing`, `Connected(connectedAt)`, `Ending`,
`Ended(result)`, `Failed(errorCode, userMessage)`.

Only emit states supported by actual SDK/provider callbacks. Keep SDK
raw objects inside the calling layer. Verify event names, token APIs,
connect options, audio-device APIs, and SDK compatibility against
current Twilio documentation during implementation.

## 8. Backend API contract

Base path: `/api/v1`. All endpoints require HTTPS in non-local
environments.

### `GET /api/v1/health`

Returns non-sensitive service status and version. Must not expose
environment variables or provider credentials.

### `POST /api/v1/voice/token`

Purpose: issue a short-lived Voice Access Token for the authenticated
app user/device.

Request example:

``` json
{
  "deviceId": "opaque-installation-id"
}
```

Response example:

``` json
{
  "token": "<short-lived-access-token>",
  "identity": "cb_<opaque-user-id>",
  "expiresAt": "2026-10-20T15:00:00Z"
}
```

Requirements: - Authenticate caller if endpoint is reachable outside a
strictly controlled local environment. - Apply rate limits. - Generate a
unique, provider-valid identity that does not expose personal data. -
Include the appropriate Voice Grant and outgoing application
configuration. - Do not log token or response body. - Return
`Cache-Control: no-store`. - Token lifetime must be short and consistent
with provider guidance. - Response schema is illustrative and may be
adapted to the SDK/provider contract.

### `POST /api/v1/voice/status`

Purpose: receive provider call-status callbacks if configured. This is a
provider webhook, not a normal client endpoint.

Requirements: - Verify provider webhook signature against the exact URL
and request parameters using the official SDK helper. - Handle
duplicate/out-of-order events idempotently. - Persist only fields
required for operational status if server persistence is enabled. -
Return quickly and safely; never trust unsigned client-supplied status
as authoritative provider state.

### TwiML App Voice URL

A dedicated server endpoint (for example `POST /api/v1/voice/outgoing`)
should validate the destination, enforce allowed destination policy, and
return TwiML that connects the SDK-originated call to the intended PSTN
number. Use the provider's expected form encoding and TwiML response
content type. This endpoint must verify webhook signature, validate
destination, rate-limit, and reject unsupported destinations. The final
route and parameter names must match the actual Twilio Voice SDK/TwiML
application setup.

The token endpoint and TwiML voice URL are different responsibilities.
Do not assume a JSON `/outgoing` API response itself is sufficient for
Twilio call control.

## 9. Security requirements

-   TLS for all remote traffic.
-   Never embed permanent secrets in the client.
-   Use short-lived Access Tokens and narrow Voice Grants.
-   Authenticate token requests; personal use does not justify leaving a
    public token-minting endpoint open.
-   Verify provider webhook signatures.
-   Validate destination numbers on both client and server.
-   Enforce allowed destinations and rate limits server-side.
-   Avoid open-ended caller-controlled TwiML or arbitrary destination
    bridging.
-   Redact tokens, secrets, full phone numbers, and sensitive notes from
    logs.
-   Keep dependency versions pinned and review security updates.
-   Restrict CORS if browser clients exist; CORS is not authentication.
-   Use request size limits, timeouts, and generic error responses.
-   Store secrets only in deployment secret manager/environment.
-   Do not collect call recordings or audio in MVP.
-   Provide a way to delete local records; document that provider
    billing/telephony records may remain under provider retention
    policies.

## 10. Number validation

Use a maintained phone-number parsing library where practical. Normalize
to E.164 for provider requests. Do not rely on regex alone to prove a
number is reachable. Client validation improves UX; server validation is
authoritative for policy. MVP destination policy is US (`+1`) only
unless explicitly changed after provider and product review. Reject
short codes, emergency numbers, premium/high-risk destinations,
malformed values, and any destination not permitted by account policy.

## 11. Data storage

MVP local Room tables: - `contacts` - `call_history`

Use stable primary keys, indices on normalized phone number and
timestamps, and schema migrations. Use UTC epoch milliseconds or
ISO-8601 consistently for timestamps. Notes may contain sensitive data;
keep them local by default. Remote storage is not required for
contacts/history in MVP.

## 12. Error model

Backend errors should have a stable envelope:

``` json
{
  "error": {
    "code": "TOKEN_UNAVAILABLE",
    "message": "Calling is temporarily unavailable. Please try again.",
    "requestId": "opaque-request-id"
  }
}
```

Do not expose stack traces, provider credentials, internal hostnames, or
raw provider payloads. Android maps stable codes to localized user
messages and keeps diagnostic codes separately.

## 13. Observability

-   Structured logs with request ID and redaction.
-   Health endpoint.
-   Metrics for request latency/error rate without personal data.
-   No tokens or secret-bearing request bodies in logs.
-   Client diagnostics should use redacted phone numbers and
    non-sensitive error codes.
-   Set retention according to deployment needs and privacy review.

## 14. Testing strategy

### Android unit tests

-   Phone-number normalization and allowed-country policy.
-   Call state mapping.
-   ViewModel state transitions.
-   Repository behavior on success, timeout, unauthorized, and server
    error.
-   Room DAO CRUD and migration tests.

### Backend tests

-   Token generation contract with provider SDK mocked where suitable.
-   Authentication and rate-limit behavior.
-   Invalid/blocked destination rejection.
-   Webhook signature validation.
-   Duplicate callback idempotency.
-   Error envelope and secret redaction.

### Manual/device tests

-   Microphone permission granted, denied, and denied permanently.
-   Online/offline transitions.
-   Token endpoint unavailable.
-   Test call to an authorized permitted destination.
-   Call end, remote end, call failure, and app background/foreground.
-   Speaker/mute controls on real hardware.
-   Clean install and upgrade path.

Do not use automated tests to place uncontrolled calls to real phone
numbers. Use provider-supported test credentials/mocks where available
and explicitly authorized test destinations for end-to-end validation.

## 15. Build and release

-   CI: lint, unit tests, backend tests, Android debug build.
-   Separate staging and production secrets/configuration.
-   Keep signing keys out of repository.
-   Build release APK/AAB only after acceptance checklist.
-   Review app distribution and Play policy requirements if publishing
    publicly.
-   Verify current SDK minSdk, Gradle, Kotlin, JDK, and dependency
    compatibility at implementation time.

## 16. Technical decision log

  -----------------------------------------------------------------------
  Decision                Baseline                Rationale / caveat
  ----------------------- ----------------------- -----------------------
  Native client           Kotlin + Compose        Direct integration with
                                                  official Android SDK

  Telephony               Twilio Voice SDK        Subject to provider
                                                  eligibility and
                                                  configuration

  Backend                 Node.js + TypeScript +  Familiar ecosystem and
                          Express                 official SDK support

  Local persistence       Room                    Private,
                                                  offline-friendly MVP

  Cloud contacts/history  Not in MVP              Reduces data exposure
                                                  and backend scope

  Incoming calls          Later milestone         Requires additional
                                                  push/background/call UX
                                                  work

  Authentication          Required for public     Exact mechanism must be
                          endpoint                chosen before
                                                  production
  -----------------------------------------------------------------------
