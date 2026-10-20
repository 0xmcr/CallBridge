# CallBridge --- Implementation Plan

**Approach:** small, verifiable milestones suitable for Antigravity and
AI-assisted coding\
**Goal:** deliver a secure outbound-calling MVP before expanding scope\
**Last updated:** 2026-10-20

## 1. Execution rules for Antigravity

Use these rules for every coding task: 1. Read all six project documents
before making architectural changes. 2. Treat the PRD as product scope,
the TRD as technical architecture, AppFlow as behavior, UI/UX as
presentation, Backend Schema as API/data contract, and this plan as task
order. 3. Do not silently change a decision that affects more than one
document. Propose the change and update all affected documents. 4. Make
one small, buildable change at a time. 5. Before editing, inspect the
existing repository and report the current structure; do not overwrite
an existing project blindly. 6. Never fabricate SDK APIs. Verify the
current Twilio Voice Android SDK documentation and sample app for exact
method names and versions. 7. Never place permanent Twilio secrets in
Android code, Git, logs, screenshots, or generated docs. 8. Do not add
incoming calls, call recording, cloud contact sync, or other
out-of-scope features during MVP. 9. After each milestone, run the
relevant build/tests and report exact commands and results. Do not claim
a test passed unless it was executed. 10. If provider eligibility or
account setup is blocked, continue with mocks and interfaces, but mark
real-call verification as blocked rather than simulating success.

## 2. Pre-development gates

### Gate A --- Provider feasibility

Before spending money, verify directly with Twilio: - Account creation
and identity-verification path for the user's real country/documents. -
Whether intended use and originating location are permitted. - Whether a
US number can be purchased and what regulatory information is
required. - Outbound destination permissions and caller-ID rules. -
Current number, SDK, per-minute, carrier, regulatory, and tax charges. -
Any applicable terms and usage restrictions.

**Exit criterion:** written/console confirmation of a viable account and
calling configuration, or a documented decision to use mocks until
resolved.

### Gate B --- Development environment

-   Install a compatible JDK, Android Studio, Android SDK, Node.js LTS,
    and Git.
-   Confirm versions against current Gradle/Kotlin/Twilio requirements.
-   Create repository and baseline README.
-   Add `.gitignore` for local secrets, build outputs, IDE files, and
    environment files.

**Exit criterion:** clean Android debug build and backend test server
start.

## 3. Suggested repository structure

``` text
CallBridge/
  README.md
  docs/
    01_PRD.md
    02_TRD.md
    03_AppFlow.md
    04_UI_UX_Design_Brief.md
    05_Backend_Schema.md
    06_Implementation_Plan.md
  android/
    settings.gradle.kts
    build.gradle.kts
    app/
      build.gradle.kts
      src/main/AndroidManifest.xml
      src/main/java/com/callbridge/personal/
        navigation/
        ui/
        domain/
        data/
        calling/
        core/
      src/test/
      src/androidTest/
  backend/
    package.json
    tsconfig.json
    src/
      app.ts
      server.ts
      routes/
      middleware/
      services/
      validation/
      config/
      logging/
    test/
    .env.example
```

Do not create every directory as an empty placeholder if it adds no
value; add packages as features are implemented.

## 4. Milestones

### M0 --- Scope, provider check, and repository baseline

Tasks: - Review all six docs. - Confirm product name/package ID are
provisional. - Verify Twilio account feasibility and pricing. - Create
repository and README. - Add architecture decisions and issue
tracker/task list.

Deliverables: - Baseline repo. - Documented provider gate status. -
Clean Git status and no secrets committed.

Exit criteria: - Scope and dependency risks are acknowledged. - No code
assumes provider approval that has not been verified.

### M1 --- Android shell and design system

Tasks: - Create native Kotlin/Compose project. - Set package ID
`com.callbridge.personal` as a provisional identifier. - Add Material 3
theme, navigation skeleton, dashboard, dial pad, contacts, history,
settings placeholders. - Add previews for core screens. - Add semantic
labels and dark/light theme. - Confirm target/min SDK from current
dependency compatibility.

Exit criteria: - Debug build succeeds. - Navigation works. - No
telephony integration is required yet. - Basic accessibility and empty
states exist.

### M2 --- Local data layer

Tasks: - Define Room entities/DAOs for contacts and call history. -
Implement database and migrations. - Implement repositories and
validation. - Build contact CRUD/search screens. - Add call-history list
with empty state and mock fixture data only in debug/test contexts.

Exit criteria: - Unit/DAO tests pass. - Contact data persists after
restart. - Deleting a contact does not delete historical call records. -
No fake call is represented as a real provider call.

### M3 --- Backend foundation and API contract

Tasks: - Initialize Node.js + TypeScript backend. - Add configuration
validation at startup. - Add `/api/v1/health`. - Add consistent error
envelope, request IDs, structured redacted logging, request limits, and
rate-limiting middleware. - Add authentication design before exposing
token endpoint. - Create `.env.example` with placeholder names only.

Exit criteria: - Backend tests pass. - Startup fails safely when
required production config is missing. - Health endpoint does not expose
secrets. - `.env` is ignored by Git.

### M4 --- Token service and provider webhooks

Tasks: - Integrate official Twilio server SDK. - Implement authenticated
`/api/v1/voice/token`. - Generate short-lived token with required Voice
Grant and configured outgoing application. - Implement TwiML Voice URL
that verifies signatures, validates destination, enforces
allowed-country policy, and returns provider-compatible TwiML. -
Implement optional status callback handling with signature verification
and idempotency. - Add tests using mocks and valid/invalid webhook
signatures. - Verify external URL/proxy settings.

Exit criteria: - No secret leaves backend. - Token endpoint cannot be
called anonymously in production. - Invalid destinations and invalid
webhook signatures are rejected. - TwiML route matches current provider
documentation.

### M5 --- Android API client and calling abstraction

Tasks: - Implement typed API models and token repository. - Implement
error mapping and timeouts. - Add `CallManager` abstraction and
call-state model. - Integrate the official Twilio Voice Android SDK
following current quickstart/docs. - Wire lifecycle-safe state
collection into ViewModel. - Implement one-active-call guard and safe
terminal-event handling.

Exit criteria: - Android build passes. - Unit tests cover token
success/failure and state transitions. - No raw SDK objects leak into
UI. - No duplicate calls after rapid taps/recomposition.

### M6 --- Outbound call UI and real test

Tasks: - Connect dial pad to validation, permission, token request, and
SDK call initiation. - Build Active Call screen and real event-driven
states. - Add mute/speaker/end-call controls supported by SDK/device. -
Save terminal call result once to Room. - Test permitted destination
using an authorized provider account. - Test call failures and network
interruptions.

Exit criteria: - End-to-end call works with approved
account/configuration. - Call timer starts only on connected event. -
Failure paths are clear. - Real call test is documented with no
sensitive data.

### M7 --- History, contact association, polish

Tasks: - Link calls to contacts when launched from contact. - Implement
call detail screen and call-back flow. - Improve search, sorting,
validation, loading and empty states. - Add settings/readiness screen. -
Add redacted diagnostics and privacy text. - Add export only if it fits
the release scope and is tested.

Exit criteria: - Core journeys match AppFlow. - No placeholder buttons
remain in MVP screens. - Local data survives app restarts and upgrades.

### M8 --- Hardening and release candidate

Tasks: - Run full Android/backend test suites. - Run lint/static
analysis and dependency review. - Test small screens, large text,
TalkBack, light/dark themes. - Test permission denied, offline, token
errors, rejected call, local/remote hangup. - Review logging and APK
contents for secrets. - Review provider terms, number regulatory
requirements, privacy notice, and distribution policy. - Create release
build/signing process securely.

Exit criteria: - All P0 acceptance criteria pass. - No known critical
security issue. - Release checklist signed off. - Real provider setup
blockers are explicitly listed.

## 5. Test matrix

  ------------------------------------------------------------------------
  Area                    Test                    Expected result
  ----------------------- ----------------------- ------------------------
  Dial pad                Empty/invalid number    Call disabled or
                                                  validation shown

  Dial pad                Valid E.164 US number   Passes syntax/policy
                                                  validation

  Permission              Microphone denied       Call not started;
                                                  recovery shown

  API                     Token endpoint timeout  Safe error; number
                                                  retained

  API                     401/429/503             Correct mapped message;
                                                  no retry storm

  Calling                 Rapid double tap        One call attempt only

  Calling                 Connected event         Timer starts

  Calling                 Call fails before       No fabricated duration
                          connection              

  Calling                 End during connecting   SDK
                                                  disconnected/cancelled
                                                  safely

  Data                    Add/edit/delete contact Correct Room persistence

  Data                    Delete contact          History retained with
                                                  nullable contact link

  Data                    Duplicate terminal      One history entry
                          callbacks               

  Security                Inspect APK/source/logs No permanent provider
                                                  secret

  Webhook                 Invalid signature       Request rejected

  Webhook                 Repeated status         Idempotent processing
                          callback                

  UX                      Large text/TalkBack     Controls remain
                                                  understandable and
                                                  reachable
  ------------------------------------------------------------------------

## 6. AI coding task template

Use this template for each Antigravity task:

> **Context:** Read `docs/01_PRD.md` through
> `docs/06_Implementation_Plan.md`.\
> **Task:** Implement only \[one concrete milestone/task\].\
> **Constraints:** Preserve the documented architecture and API
> contracts. Do not add out-of-scope features. Do not invent Twilio SDK
> APIs; verify current official documentation. Never put permanent
> secrets in client code or source control.\
> **Before coding:** Inspect the current repository, identify relevant
> files, state a short plan, and list assumptions.\
> **Implementation:** Make the smallest coherent change. Add/update
> tests and documentation.\
> **Verification:** Run exact build/test/lint commands available in this
> environment.\
> **Report:** List files changed, behavior implemented, tests actually
> run and results, remaining issues, and any decision that would require
> updating the six documents. Never claim success for a command that was
> not run.

## 7. Recommended implementation order

Do not ask the AI to "build the whole app at once." Use this order: 1.
Read docs and inspect repo. 2. Create project shell and prove clean
build. 3. Implement local contacts and Room. 4. Build backend
health/security foundation. 5. Confirm provider account and configure
credentials securely. 6. Implement token + TwiML endpoints and tests. 7.
Integrate Voice SDK behind `CallManager`. 8. Wire dial pad and
active-call UI. 9. Persist history and polish error states. 10. Run
security and release checklist.

## 8. Definition of Done

A task is done only when: - Requirements and acceptance criteria are
met. - Code is readable and follows project conventions. - Relevant
tests are added and executed. - No secrets or sensitive personal data
are introduced into logs/source control. - Loading, empty, and failure
states are addressed where relevant. - Documentation is updated if a
contract or decision changed. - The AI reports actual results and known
limitations.

## 9. Post-MVP roadmap (separate scope)

Potential later milestones, each requiring fresh design/security
review: - Incoming calls using provider push integration and FCM. -
Android Telecom / CallStyle / lock-screen behavior. - Encrypted backup
and restore. - Call-cost estimates based on current authoritative rate
data. - Contact/call export and import. - Optional cloud sync with
explicit consent and deletion policy. - Additional countries only after
provider/account/policy verification.

Call recording, automated campaigns, bulk dialing, or other higher-risk
features are not part of this roadmap by default and require separate
legal, privacy, and provider-policy review.
