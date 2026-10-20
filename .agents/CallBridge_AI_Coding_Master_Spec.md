# CallBridge --- AI Coding Master Specification

**Document type:** Master specification for AI-assisted development\
**Status:** Initial baseline; verify provider feasibility before paid
deployment\
**Product name:** CallBridge (provisional)\
**Android application ID:** `com.callbridge.personal` (provisional)\
**Primary goal:** Build a personal Android app for authenticated
outbound calling to permitted US phone numbers\
**Last updated:** 2026-10-20

------------------------------------------------------------------------

## 1. Instructions for AI Coding Agents

Before writing code, read this entire document and all six supporting
documents if present:

1.  `01_PRD.md`
2.  `02_TRD.md`
3.  `03_AppFlow.md`
4.  `04_UI_UX_Design_Brief.md`
5.  `05_Backend_Schema.md`
6.  `06_Implementation_Plan.md`

Treat these documents as the project's source of truth. If documents
conflict, stop and report the conflict rather than silently choosing
one.

### Mandatory coding rules

-   Inspect the existing repository before changing files. Never
    overwrite an existing project blindly.
-   Work in small, buildable milestones; do not generate the entire app
    in one pass.
-   Verify exact Twilio Voice Android SDK APIs, versions, and setup
    against current official documentation and the official quickstart.
-   Do not invent method names, dependency versions, API responses, or
    successful test results.
-   Run relevant Gradle builds, backend tests, and static checks after
    each milestone. Report exact commands and actual results.
-   Keep UI, domain logic, persistence, network code, and calling SDK
    integration separated.
-   Do not add unrequested features or permissions.
-   Never put permanent Twilio secrets in Android source, resources,
    `BuildConfig`, the APK, logs, Git, or documentation.
-   Commit `.env.example` with placeholder names only. Keep actual
    `.env` files and credentials out of version control.
-   If Twilio account eligibility or setup is unresolved, use
    mocks/interfaces for development and mark real calling as blocked.
    Never pretend a mocked call is real.
-   Update documentation whenever an approved architectural, API, or
    data decision changes.

## 2. Product Identity

  -----------------------------------------------------------------------
  Setting                             Baseline
  ----------------------------------- -----------------------------------
  App name                            CallBridge

  Product type                        Personal virtual calling app

  Platform                            Android

  Application ID                      `com.callbridge.personal`

  Android language                    Kotlin

  UI framework                        Jetpack Compose + Material 3

  Calling engine                      Official Twilio Voice Android SDK

  Backend                             Node.js + TypeScript

  Local database                      Room

  Optional remote backup              Supabase, only after separate
                                      security/privacy approval

  Initial goal                        Authenticated outbound calling to
                                      permitted US destinations

  Initial output                      Debug APK; signed release APK/AAB
                                      after hardening
  -----------------------------------------------------------------------

The name and application ID are provisional. Check app-store name
availability, trademarks, and namespace conflicts before public release.
Changing the application ID after publication can be disruptive.

## 3. Product Scope and Feature Phases

### Phase 1 --- MVP: Outbound Calling

1.  Splash/launch screen and branding.
2.  Personal authentication or app lock with a defined security model. A
    client-only PIN must not be treated as backend authentication.
3.  Dashboard.
4.  Dial pad and editable phone-number input.
5.  Country-code selector, defaulting to `+1`.
6.  Start/end call and truthful call-state display.
7.  Microphone permission flow.
8.  Mute, speaker routing, and call duration where supported by the
    SDK/device.
9.  Backend endpoint for short-lived Twilio Voice Access Tokens.
10. Clear call errors and network/service status.
11. Local call-history entry for completed and failed attempts.
12. Basic Settings/readiness screen without exposing secrets.

**MVP acceptance:** real outbound calling is accepted only after
provider eligibility, number configuration, destination permission,
caller-ID configuration, and backend setup are verified. A mocked call
does not satisfy this criterion.

### Phase 2 --- Personal CRM

13. Create, edit, view, and delete contacts.
14. Store contact name, phone number, company, notes, and optional
    follow-up date.
15. Search and filter contacts.
16. Add/edit notes.
17. Call history with timestamp, duration, and outcome when known.
18. Follow-up date and reminders.
19. User-initiated contact/history import and export.
20. Duplicate phone-number detection.

Keep contact and note data local by default. Exported data may be
sensitive and requires an explicit user action.

### Phase 3 --- Advanced Features

21. Incoming calls and FCM push notifications.
22. Background and locked-screen call handling.
23. Android Telecom integration where appropriate.
24. Voicemail or voicemail notifications, if supported by the chosen
    setup.
25. Estimated calling-cost display using verified current rate data.
26. Encrypted contact/note backup and restore.
27. Light/dark themes.
28. Safe diagnostics and redacted logs.

Phase 3 is not part of the initial MVP. Incoming calls, background
handling, lock-screen UI, and Telecom integration require separate
design, SDK configuration, permission/policy checks, and device testing.
Do not show these as functional until implemented and verified.

## 4. Technology Stack

### Android client

-   Kotlin
-   Jetpack Compose
-   Material 3
-   AndroidX lifecycle/ViewModel
-   Kotlin Coroutines and Flow
-   Room for local persistence
-   Retrofit or Ktor Client for HTTPS API calls; choose one and use it
    consistently
-   Kotlin serialization or another consistent JSON library
-   Official Twilio Voice Android SDK, using a version compatible with
    the project's Android/Gradle/Kotlin toolchain

### Backend

-   Node.js LTS
-   TypeScript
-   Express as the baseline framework unless another choice is
    explicitly approved
-   Official Twilio server SDK
-   Request validation library such as Zod
-   Structured logging with redaction
-   Environment-based configuration and deployment secret storage

### Optional services

Supabase may be evaluated for remote backup only after defining
authentication, encryption, access control, retention, deletion, and
restore requirements. Do not add a cloud database to the MVP merely for
convenience.

### Version policy

Do not guess dependency versions. Check current official Twilio
documentation and compatibility requirements for Android API level, JDK,
Gradle, Kotlin, and the selected SDK. Pin compatible versions and
document them.

## 5. Suggested Project Structure

``` text
CallBridge/
├── README.md
├── docs/
│   ├── 01_PRD.md
│   ├── 02_TRD.md
│   ├── 03_AppFlow.md
│   ├── 04_UI_UX_Design_Brief.md
│   ├── 05_Backend_Schema.md
│   ├── 06_Implementation_Plan.md
│   └── CallBridge_AI_Coding_Master_Spec.md
├── android/
│   ├── settings.gradle.kts
│   ├── build.gradle.kts
│   └── app/
│       ├── build.gradle.kts
│       └── src/
│           ├── main/
│           │   ├── AndroidManifest.xml
│           │   └── java/com/callbridge/personal/
│           │       ├── navigation/
│           │       ├── ui/
│           │       │   ├── dashboard/
│           │       │   ├── dialpad/
│           │       │   ├── activecall/
│           │       │   ├── contacts/
│           │       │   ├── history/
│           │       │   ├── settings/
│           │       │   ├── components/
│           │       │   └── theme/
│           │       ├── calling/
│           │       ├── domain/
│           │       │   ├── model/
│           │       │   ├── repository/
│           │       │   └── usecase/
│           │       ├── data/
│           │       │   ├── local/
│           │       │   ├── remote/
│           │       │   └── repository/
│           │       └── core/
│           │           ├── validation/
│           │           ├── error/
│           │           └── logging/
│           ├── test/
│           └── androidTest/
└── backend/
    ├── package.json
    ├── tsconfig.json
    ├── .env.example
    ├── src/
    │   ├── app.ts
    │   ├── server.ts
    │   ├── routes/
    │   ├── services/
    │   ├── middleware/
    │   ├── validation/
    │   ├── config/
    │   └── logging/
    └── test/
```

This is a logical target structure, not a requirement to create empty
folders in advance. Add files as features are implemented. Preserve
standard generated Gradle and Android project files.

## 6. Android Permissions

Only request permissions required by features that have actually been
implemented.

  --------------------------------------------------------------------------
  Permission/configuration   Purpose                 MVP?
  -------------------------- ----------------------- -----------------------
  `INTERNET`                 Connect to backend and  Yes
                             Twilio                  

  `RECORD_AUDIO`             Microphone audio during Yes; runtime request
                             a call                  required

  `POST_NOTIFICATIONS`       Notifications on        Only if needed by
                             applicable Android      implemented
                             versions                notifications

  FCM setup                  Incoming-call push      No; Phase 3
                             notifications           

  Android Telecom            System calling          No; evaluate for Phase
  integration                integration and         3
                             advanced handling       

  Contacts permission        Access device address   Not needed for
                             book                    app-owned Room contacts

  Phone/SMS/call-log         Device telephony/SMS    Do not request unless a
  permissions                features                separately approved
                                                     feature needs them
  --------------------------------------------------------------------------

If microphone permission is denied, do not start a microphone-dependent
call. Explain how to retry or open Android Settings if permission is
permanently denied.

## 7. Calling Architecture and Flow

### Architecture

``` text
Android Compose UI
   ↓
ViewModel / use case
   ↓
CallManager abstraction
   ├── HTTPS API client → Backend token endpoint
   ├── Twilio Voice Android SDK → Twilio Voice
   └── Room repository → local call history
```

The backend protects provider credentials, issues Access Tokens,
enforces destination policy, serves TwiML, and validates provider
webhooks. The Android app must never contain permanent Twilio secrets.

### Outbound call flow

1.  User opens the Dial Pad or selects a contact.
2.  The app displays the complete destination for review.
3.  The app normalizes and validates the number. MVP policy defaults to
    US destinations (`+1`), subject to provider/account rules.
4.  The app checks microphone permission and requests it if necessary.
5.  The app requests a short-lived Voice Access Token from the
    authenticated backend.
6.  The backend validates the session, applies rate limits, and creates
    a token with only the required Voice Grant.
7.  The Android calling layer starts the call through the official SDK
    and configured TwiML App.
8.  The backend Voice URL validates the destination and returns
    provider-compatible TwiML.
9.  The UI reflects actual SDK/provider events. The elapsed timer starts
    only after a confirmed connected event.
10. The user can mute, change speaker routing if supported, or end the
    call.
11. On a terminal event, the app saves one local history record and
    releases call resources.
12. On failure, the app shows a safe, actionable message and retains the
    dialed number.

### Call states

Use one explicit state model, for example:

`Idle → RequestingPermission → FetchingToken → Connecting → Ringing (only if reliably exposed) → Connected → Ending → Ended`

Failures may occur during permission, token retrieval, or call setup. Do
not invent ringing or connected states based on elapsed time. Prevent
duplicate concurrent calls and make history persistence idempotent.

## 8. Backend API Contract

Use a versioned API prefix such as `/api/v1`.

### `GET /api/v1/health`

Returns non-sensitive service health and version. Never return
environment variables, secrets, or raw provider configuration.

### `POST /api/v1/voice/token`

Issues a short-lived Access Token for an authenticated app user/device.

Illustrative request:

``` json
{
  "deviceId": "opaque-installation-id"
}
```

Illustrative response:

``` json
{
  "token": "<short-lived-access-token>",
  "identity": "cb_<opaque-user-id>",
  "expiresAt": "2026-10-20T15:00:00Z"
}
```

The response contract may be adjusted to the selected SDK
implementation, but token issuance must be authenticated, rate-limited,
and configured with the required Voice Grant. Set
`Cache-Control: no-store`. Never log the token.

### Outgoing Voice URL / TwiML

A dedicated provider callback endpoint, for example
`POST /api/v1/voice/outgoing`, must: - Accept the request format
expected by the selected Twilio Voice SDK/TwiML setup. - Verify the
provider signature with the official SDK helper. - Validate and
normalize the destination. - Enforce allowed destinations on the
server. - Return valid TwiML with the appropriate content type. - Use
only a caller ID the account is authorized to use. - Reject arbitrary
client-controlled routing or TwiML.

This endpoint is a provider call-control webhook, not merely a normal
JSON endpoint.

### `POST /api/v1/voice/status`

Receives provider status callbacks, verifies the signature, handles
duplicate/out-of-order events safely, and stores only the minimum
required data if remote persistence is explicitly enabled.

These paths are proposed API contracts. Confirm actual parameter names,
HTTP methods, TwiML requirements, and SDK behavior against current
official Twilio documentation.

## 9. Data Storage

### MVP local data

Use Room for: - **Contacts:** ID, display name, E.164 phone number,
company, notes, optional follow-up date, created/updated timestamps. -
**Call history:** ID, nullable contact ID, destination, direction,
outcome, start/connected/end timestamps when known, duration when
reliable, provider call SID if returned, stable error code, created
timestamp.

Requirements: - Contacts and notes remain local by default. - Deleting a
contact must not delete its call history; set the historical contact
reference to null. - Use database migrations; never silently erase data
to resolve schema changes. - Keep history insertion idempotent. - Do not
fabricate unknown duration or timestamps.

### Optional remote backup

Remote sync/backup is not MVP. Before adding Supabase or another remote
service, specify authentication, authorization, encryption, retention,
deletion, backup recovery, and consent.

## 10. Security Requirements

1.  Never place Twilio Auth Token, API Secret, or permanent credentials
    in the Android client.
2.  Generate short-lived Access Tokens only on the backend.
3.  Authenticate the token endpoint; an APK-embedded shared secret is
    not secure authentication.
4.  Verify provider webhook signatures.
5.  Validate and restrict destination numbers on both client and server.
6.  Apply rate limits and reasonable request timeouts.
7.  Redact tokens, secrets, full phone numbers, notes, and sensitive
    payloads from logs.
8.  Use HTTPS outside local development.
9.  Separate staging and production credentials.
10. Keep `.env` out of Git; commit `.env.example` with placeholders
    only.
11. Do not add call recording, bulk dialing, or automated campaigns to
    the MVP.
12. Follow provider terms, caller-ID rules, applicable laws, and
    destination restrictions.

## 11. Error Handling and Diagnostics

The UI should distinguish at least: - Invalid number - Destination not
allowed - Microphone permission denied - No internet - Token request
failed - Calling service not configured - Call setup failed - Call
ended - Unknown terminal state

Use stable internal error codes and safe user-facing messages. Do not
display stack traces, raw provider payloads, credentials, or
secret-bearing configuration. Diagnostics should use request IDs and
redacted error categories.

## 12. Testing Requirements

### Android

-   Phone-number normalization and destination policy
-   ViewModel and call-state transitions
-   Token success/failure/timeout
-   Room CRUD and migrations
-   One-active-call guard
-   Duplicate terminal callback handling
-   Permission granted/denied/permanently denied
-   Offline/backend failure behavior

### Backend

-   Authentication and rate limiting
-   Token generation contract
-   Invalid/blocked destination rejection
-   Valid and invalid webhook signature
-   Duplicate callback idempotency
-   Error response shape
-   Secret/log redaction

### Device/manual

-   Authorized test call to a permitted destination
-   Connecting, connected, local hangup, remote hangup, and failed call
-   Mute/speaker behavior on real hardware
-   App background/foreground behavior for MVP
-   Small screen, large text, TalkBack, light/dark theme

Do not run automated tests that call arbitrary real phone numbers. Use
mocks/test facilities where available and only explicitly authorized
destinations for real end-to-end testing.

## 13. Development Milestones

### M0 --- Feasibility and repository baseline

-   Verify Twilio account eligibility, identity verification, US number
    purchase requirements, caller-ID rules, outbound destination
    permissions, current pricing, and applicable terms.
-   Inspect/create repository and README.
-   Confirm package ID/name remain provisional.
-   Do not spend money until feasibility is understood.

### M1 --- Android project shell

-   Create Kotlin/Compose project.
-   Set provisional application ID.
-   Implement Material 3 theme and navigation skeleton.
-   Add dashboard, dial pad, contacts, history, and settings screens
    with appropriate empty states.
-   Prove a clean debug build.

### M2 --- Local data

-   Implement Room entities, DAOs, database, repositories, migrations.
-   Implement contacts CRUD/search and local call-history screen.
-   Add tests and ensure contact deletion preserves history.

### M3 --- Backend foundation

-   Create Node.js/TypeScript service.
-   Add configuration validation, health route, request IDs, safe error
    handling, structured redacted logging, and rate limiting.
-   Choose and implement a real authentication strategy before exposing
    token issuance.

### M4 --- Twilio backend

-   Add token generation with required Voice Grant.
-   Add signature-verified TwiML Voice URL.
-   Add destination validation and allowlist.
-   Add optional status callback with signature verification and
    idempotency.
-   Test with mocks and verify configuration against official docs.

### M5 --- Android calling layer

-   Add typed API client and token repository.
-   Integrate the official Voice Android SDK behind a `CallManager`.
-   Implement lifecycle-safe call state, one-active-call guard, and
    supported mute/speaker/end-call controls.
-   Add unit tests.

### M6 --- Real outbound calling

-   Connect dial pad to permission, token, and SDK call flow.
-   Implement active-call UI.
-   Save terminal call result once.
-   Verify a real call only after provider setup and permissions are
    confirmed.

### M7 --- CRM and polish

-   Link calls to contacts.
-   Complete history details, search, follow-up, and safe diagnostics.
-   Remove placeholder actions from MVP screens.
-   Test persistence and UX states.

### M8 --- Hardening and release

-   Run all tests, lint, static checks, and clean build.
-   Inspect APK/source/logs for secrets.
-   Review privacy and data retention.
-   Review provider and distribution requirements.
-   Create secure signing and release process.

## 14. Definition of Done

A task is complete only when: - Agreed acceptance criteria are met. -
Relevant tests have been added and actually executed. - The build passes
where applicable. - Loading, empty, error, and permission states are
addressed. - No permanent secret or sensitive personal data has leaked
into source control/logs. - Documentation is updated if an interface or
decision changes. - The AI reports changed files, commands run, actual
results, and remaining limitations.

## 15. Official References

Use current versions of these official resources during
implementation: - Twilio Voice Android SDK:
https://www.twilio.com/docs/voice/sdks/android - Twilio Android
quickstart repository:
https://github.com/twilio/voice-quickstart-android - Twilio US Voice
pricing: https://www.twilio.com/en-us/voice/pricing/us - Twilio
phone-number regulatory guidance:
https://www.twilio.com/docs/phone-numbers/regulatory - Twilio Help
Center: https://help.twilio.com/

Confirm country-specific eligibility, accepted identity documents,
number availability, regulatory requirements, and exact pricing with
Twilio. This document does not assume that a Bangladesh NID is accepted
or that an account/US number is guaranteed.

------------------------------------------------------------------------

## 16. First Prompt to Give Antigravity

> Read `docs/CallBridge_AI_Coding_Master_Spec.md` and all six supporting
> documents in `docs/` before coding. Inspect the existing repository
> and report its current state, available tools, dependency versions,
> missing prerequisites, and any contradictions. Do not change files
> until you have presented a short plan for M0 and M1. Preserve the
> provisional application ID `com.callbridge.personal` unless I
> explicitly approve a change. Do not invent Twilio SDK APIs or claim
> real calling works before provider setup and an authorized test call.
> Never put permanent secrets in the Android app or source control.
> Implement one small milestone at a time, run the relevant build/tests,
> and report actual results.
