# CallBridge --- Product Requirements Document (PRD)

**Document status:** Draft for implementation planning\
**Product:** CallBridge (working name; name availability must be
checked)\
**Platform:** Android\
**Initial release:** MVP 1.0\
**Primary market/use case:** Personal use by an operator located in
Bangladesh who needs to place legitimate outbound calls to US phone
numbers through a US-capable telephony provider.\
**Last updated:** 2026-10-20

## 1. Purpose

CallBridge is a personal Android calling client that lets its authorized
user place and manage outbound calls to US telephone numbers, organize
contacts, retain call history, and record follow-up notes. Twilio Voice
is the planned telephony provider, subject to account eligibility,
identity verification, number purchase, destination permissions, and
applicable terms.

This PRD defines user value, scope, workflows, requirements, acceptance
criteria, and release boundaries. The PRD is product-facing; technical
implementation details are specified in the TRD and Backend Schema.

## 2. Product vision

Provide a simple, dependable, secure mobile workspace for calling US
businesses and customers without requiring the user to manage telephony
infrastructure manually.

### Product principles

1.  **Calling first:** the core outbound call journey must be fast and
    reliable.
2.  **Honest status:** distinguish dialing, ringing, connected, ended,
    failed, and unknown states.
3.  **Privacy by default:** keep personal contact data local unless the
    user explicitly enables a supported backup.
4.  **Secure credentials:** never embed Twilio permanent secrets in the
    Android app.
5.  **Incremental delivery:** build and verify the MVP before advanced
    incoming-call or CRM features.
6.  **Compliance-aware:** do not bypass provider verification,
    destination restrictions, caller-ID rules, consent, or applicable
    law.

## 3. Goals and success criteria

### MVP goals

-   Allow an authenticated user to obtain a short-lived Voice Access
    Token from the backend.
-   Place outbound calls to eligible US destinations using the Twilio
    Voice Android SDK.
-   Show call state, duration, mute and speaker controls where
    supported.
-   Save local call history and allow contacts, notes, and follow-up
    dates to be managed.
-   Surface clear errors for network, permissions, authentication,
    provider configuration, and call failures.
-   Keep provider credentials and webhook secrets on the backend.
-   Provide repeatable build and test instructions for AI-assisted
    development.

### MVP success metrics

-   A clean install can complete onboarding and reach the dashboard
    without a crash.
-   A configured and authorized account can complete a test outbound
    call to a destination the account is permitted to call.
-   Every terminal call state produces a local history entry, including
    failed calls where available.
-   No permanent Twilio secret appears in the APK, source control, logs,
    or client configuration.
-   Critical flows have automated tests and a documented manual test
    checklist.

## 4. Users and assumptions

### Primary user

One individual using an Android phone for personal outbound calling to
US businesses/customers.

### Assumptions to validate

-   The user has an Android device and a development computer.
-   The user can access an eligible Twilio account and can complete any
    required identity/business verification honestly.
-   Twilio permits the account's intended use, originating location,
    destination, and number configuration.
-   A stable internet connection is available during calls.
-   The first release is for personal/private distribution, not public
    Google Play launch.
-   Initial calling is outbound-only. Incoming calls are a later
    milestone.

### Risks / external dependencies

Twilio account onboarding, Bangladesh identity-document acceptance,
number availability, destination permissions, regulatory bundles,
emergency-calling requirements, and pricing are provider-controlled and
must be verified before committing to a paid deployment. No document
assumes that Bangladesh NID is accepted or that a US number is
guaranteed to be purchasable.

## 5. Scope

### In scope for MVP

-   App launch and basic onboarding/status screen.
-   Backend health/configuration check without exposing secrets.
-   Dashboard with dial-pad entry point and recent calls.
-   Dial pad with US country code default (+1), manual number entry,
    normalization and validation.
-   Outbound call initiation and active-call screen.
-   Call state and duration display.
-   Mute and speaker controls where SDK/device state supports them.
-   Local contacts: create, edit, view, search, delete with
    confirmation.
-   Contact company, phone number, notes, and optional follow-up date.
-   Local call history with call outcome, number/contact, timestamp,
    duration, and available error details.
-   Linking a call record to a known contact when possible.
-   Settings: provider readiness status, privacy information,
    diagnostics summary, and app version.
-   Clear loading, empty, offline, permission-denied, and failure
    states.
-   Unit tests, integration-test seams, and release checklist.

### Out of scope for MVP

-   Incoming calls / push notifications / FCM / background call
    presentation.
-   Multi-user organizations, teams, roles, or shared contact databases.
-   Payments, in-app wallet, billing or automatic recharge.
-   Recording calls, transcription, sentiment analysis, or AI-generated
    call summaries.
-   SMS, WhatsApp, campaigns, robocalling, bulk dialing, or
    auto-dialing.
-   CRM synchronization and cloud backup unless separately approved.
-   Emergency calling or a claim that the app is a replacement for a
    normal phone service.
-   Public release until privacy policy, legal/provider requirements,
    and store requirements are reviewed.

## 6. Functional requirements

Priority: **P0** required for MVP; **P1** valuable next; **P2** future.

### FR-001 --- Launch and readiness (P0)

The app shall launch to a stable screen and show whether
internet/backend configuration is reachable. It must not display secret
values.

**Acceptance:** app opens without crash; backend unavailable is shown as
a recoverable status, not a fatal crash.

### FR-002 --- Microphone permission (P0)

Before a call requires microphone access, request Android runtime
permission with a plain-language explanation. If denied, do not start
audio; provide a route to retry or Settings.

**Acceptance:** denied permission never produces a false "connected"
state; permanently denied permission offers Settings guidance.

### FR-003 --- Dial pad (P0)

Provide editable phone-number input and numeric keypad. Default country
calling code is +1, but the user can edit the full international number.
Do not silently rewrite a number in a way that changes its destination.

**Acceptance:** digits, `+`, backspace, clear, paste, and manual editing
work; invalid input is explained before call initiation.

### FR-004 --- Outbound call (P0)

An authorized user can start a call to a valid destination using the
Voice SDK and configured backend/TwiML application. Call initiation is
blocked if token retrieval fails or configuration is not ready.

**Acceptance:** a valid call moves through truthful states; failures are
visible and logged without secrets.

### FR-005 --- Active call controls (P0)

Show the target number/contact, call state, elapsed duration after
connection, end-call control, and mute/speaker controls when supported.

**Acceptance:** end-call is available while dialing and connected; UI
cannot initiate duplicate concurrent calls.

### FR-006 --- Call history (P0)

Save completed and failed call attempts locally. Store call direction,
normalized destination, start/end timestamps where known, duration,
outcome, and provider call identifier if returned. Do not store access
tokens or secret credentials.

**Acceptance:** history remains after app restart; unavailable fields
are nullable rather than fabricated.

### FR-007 --- Contacts (P0)

Create, view, edit, search, and delete contacts. Fields: display name
(required), phone number (required), company (optional), notes
(optional), follow-up date (optional), created/updated timestamps.

**Acceptance:** validation is consistent; delete requires confirmation;
contacts persist locally.

### FR-008 --- Contact-to-call relationship (P0)

When a call is initiated from a contact, associate the local call record
with that contact. When dialed manually, offer matching contact
association when appropriate without blocking the call.

### FR-009 --- Error handling (P0)

Translate technical failures into actionable messages. Include retry
only when safe. Do not show raw stack traces or secrets to the user.

### FR-010 --- Privacy and settings (P0)

Provide a concise privacy notice explaining local storage and any
backend data sent for call setup/status. Show app version and
support/diagnostic information that excludes secrets.

### FR-011 --- Search and filters (P1)

Search call history and filter by outcome/date. Search contacts by name,
company, or number.

### FR-012 --- Export/import (P1)

Allow explicit user-initiated export of contacts and call history to a
user-selected file, with a clear warning that exported data may be
sensitive. Import must validate rows and report skipped items. Never
upload exports automatically.

### FR-013 --- Cost visibility (P1)

Show a non-authoritative estimate based on configured, current rate data
only if maintained safely. Clearly label it as an estimate and explain
that provider billing is authoritative. Do not hardcode rates as
guaranteed.

### FR-014 --- Backup (P2)

Optional encrypted backup may be added only after a threat model,
authentication, retention, deletion, and restore strategy are approved.

## 7. Non-functional requirements

-   **Security:** least privilege, TLS for network calls, short-lived
    tokens, webhook signature verification, server-side validation, no
    secrets in app/client logs.
-   **Reliability:** app must recover from offline mode and
    backend/provider errors without data corruption.
-   **Performance:** dashboard and local contacts/history should feel
    responsive on supported mid-range Android devices; avoid unnecessary
    network calls.
-   **Accessibility:** readable contrast, scalable text, meaningful
    labels, touch targets appropriate for mobile use,
    TalkBack-compatible controls.
-   **Maintainability:** modular Kotlin packages, typed API models,
    clear separation of UI/domain/data layers.
-   **Testability:** deterministic unit tests for number validation,
    state mapping, repositories, and API error handling.
-   **Privacy:** collect only necessary data; local data deletion and
    export behavior must be documented.
-   **Compatibility:** align minSdk and dependency versions with the
    currently supported Twilio Voice Android SDK; verify SDK release
    notes before implementation.

## 8. Primary user journeys

1.  **Place a call:** open app → dashboard → dial number or choose
    contact → validate → grant microphone permission if needed → fetch
    short-lived token → start call → see call state → end call → save
    history.
2.  **Call a contact:** contacts → select contact → tap Call →
    confirm/edit number if needed → call flow above.
3.  **Review history:** dashboard/history → select record → view
    outcome, duration, notes/contact link.
4.  **Create follow-up:** open contact or call details → add note/date →
    save locally.
5.  **Recover from failure:** call fails → show actionable reason and
    retry option if appropriate → retain failed attempt in history.

## 9. UX requirements and states

Every network-backed action must have loading, success, and failure
states. Call UI must distinguish: - `Idle` - `RequestingPermission` -
`FetchingToken` - `Connecting` - `Ringing` (only if reliably exposed) -
`Connected` - `Reconnecting` (only if SDK exposes it) - `Ending` -
`Ended` - `Failed`

Do not infer "ringing" or "connected" from elapsed time. Map only
SDK/provider events to states.

## 10. Analytics and diagnostics

MVP should avoid third-party behavioral analytics. Local diagnostic
events may include event name, app version, timestamp, and redacted
error code. Phone numbers must be masked or omitted from diagnostics by
default. Any remote logging requires explicit review and a retention
policy.

## 11. Release acceptance checklist

-   [ ] Build succeeds from documented clean checkout.
-   [ ] No permanent provider secret is present in source, APK, logs, or
    Git history.
-   [ ] Permission flows tested on supported Android versions.
-   [ ] Outbound call tested only with an authorized account and
    permitted test destination.
-   [ ] Call failure, timeout, offline, token failure, and permission
    denial tested.
-   [ ] Contacts and call history survive process/app restart.
-   [ ] Delete/export behavior is clear and intentional.
-   [ ] Provider eligibility, destination permissions, number
    regulations, pricing, and terms reviewed.
-   [ ] Privacy notice and legal review completed before wider
    distribution.

## 12. Decisions and open questions

-   Confirm provider eligibility and exact use case with Twilio before
    paid setup.
-   Confirm whether call history is local-only for MVP (default: yes).
-   Confirm supported Android minSdk against the selected SDK version at
    implementation time.
-   Confirm app distribution model (private APK vs Play Store) before
    release hardening.
-   Confirm final product name/package ID availability before public
    release.
