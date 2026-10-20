# CallBridge — AI Coding Prompts

**Purpose:** Copy one prompt at a time into Antigravity or another coding agent. Work in small, verifiable milestones. These prompts complement `.agents/SYSTEM_RULES.md` and `docs/CallBridge_AI_Coding_Master_Spec.md`.

## How to Use This File

1. Open the CallBridge repository in your coding agent.
2. Ensure the agent reads `.agents/SYSTEM_RULES.md` and the project specification before changing code.
3. Start with Prompt 0. Do not skip ahead until the current milestone is reviewed.
4. Ask the agent to show actual command output and explain any failed checks.
5. Never paste real Twilio credentials into prompts, chat, source code, or committed files. Configure secrets locally or in the deployment secret manager.
6. Verify Twilio account eligibility, number availability, regulatory requirements, and pricing before depending on real outbound calls.

---

## Prompt 0 — Inspect the Repository and Make a Plan

```text
You are the senior engineer for CallBridge. Before changing anything, read:
- .agents/SYSTEM_RULES.md
- docs/CallBridge_AI_Coding_Master_Spec.md, if present
- Any relevant project documentation already in the repository

Inspect the repository structure, Git status, Android Gradle configuration, backend package configuration, existing source files, tests, and build scripts.

Do not modify files yet.

Report:
1. What already exists and what is missing.
2. The current Android and backend technologies and versions.
3. Available build, lint, and test commands.
4. Any security risks or accidentally committed secrets (do not print secret values).
5. The current milestone and the smallest safe next step.

Do not assume files, dependencies, APIs, or tests exist. Do not claim a command was run unless you actually ran it. Wait for approval before making a broad architectural change.
```

## Prompt 1 — Establish the Android App Shell

```text
Implement only the next Android shell milestone for CallBridge.

First inspect the existing repository and follow .agents/SYSTEM_RULES.md. Use Kotlin, Jetpack Compose, and Material 3 if consistent with the approved project specification and current repository.

Goal:
- A buildable Android app shell.
- A clear app theme and typography setup.
- Navigation scaffolding for Dashboard, Dial Pad, Call History, Contacts, and Settings, using placeholder screens where features are not implemented.
- Loading, empty, and error placeholders where appropriate.
- Accessible labels and reasonable touch targets.

Rules:
- Preserve application ID com.callbridge.personal if already configured; do not change it without approval.
- Do not implement real calling in this milestone.
- Do not add unnecessary permissions or dependencies.
- Do not fake real call status or show fabricated history.
- Use existing project conventions and compatible dependency versions.
- Do not rewrite unrelated files.

Run the relevant Gradle build and tests using the repository's actual wrapper/tasks. Report changed files, exact commands, results, and anything not verified.
```

## Prompt 2 — Implement Local Data with Room

```text
Implement the approved local persistence milestone for CallBridge using Room, following .agents/SYSTEM_RULES.md and the existing architecture.

Before coding, inspect the current models, repositories, dependency versions, and test setup.

Implement only the models and persistence required by the current specification, prioritizing local call history. Add contacts only if the current milestone explicitly includes them.

Requirements:
- Keep database entities separate from UI models where appropriate.
- Use deliberate schema versions and migrations.
- Store timestamps consistently and format them for display in the UI layer.
- Represent unknown call duration separately from a verified zero duration.
- Prevent duplicate history entries using stable identifiers/idempotent repository logic.
- Never persist Twilio access tokens or credentials.
- Do not reset or delete an existing user database to avoid migration work.
- Add useful unit or database tests.

Do not invent call records. Do not add cloud sync or backup in this milestone.

Run relevant tests and the Android build. Report migrations, changed files, commands, results, and remaining risks.
```

## Prompt 3 — Build the Backend Foundation

```text
Implement the smallest backend foundation for CallBridge using Node.js and TypeScript, consistent with the repository and approved specification.

First inspect the existing backend, package manager, scripts, authentication approach, validation libraries, and tests. Verify dependency versions before adding anything.

Implement only:
- A maintainable application entry point and configuration.
- GET /api/v1/health with non-sensitive health information.
- Centralized request validation and consistent safe error handling.
- Environment-variable loading and validation.
- A safe .env.example containing placeholder values only.
- Appropriate .gitignore rules for secrets, local configuration, and generated output.
- Focused tests for health and configuration/error behavior.

Do not implement Twilio token issuance or real calling in this milestone. Do not invent a production authentication system without documenting the proposed approach and obtaining approval if a decision is needed.

Never add real credentials. Never expose stack traces, secrets, or internal infrastructure details in API responses.

Run the backend's actual typecheck, lint, and test scripts if available. Report exact commands and observed outcomes.
```

## Prompt 4 — Design and Implement Authentication

```text
Review the existing CallBridge backend and specification. Propose a secure authentication design appropriate for this personal app before implementing it.

The design must explain:
- How a user signs in or is provisioned.
- How the Android client obtains and presents credentials.
- How the backend verifies the user on every protected endpoint.
- Token/session lifetime, expiration, revocation, and storage considerations.
- Rate limiting and protection against brute-force or token abuse.
- Local development and production configuration.

Do not use a permanent shared secret embedded in the Android APK. Do not invent a fake login that grants production access. Do not expose credentials in logs or responses.

If a product or provider choice is essential, ask one concise question before implementation. Otherwise use a conservative design, document assumptions, implement in a small milestone, and test both authorized and unauthorized requests.

Run relevant checks and report verified behavior and unresolved security concerns.
```

## Prompt 5 — Implement the Twilio Voice Token Endpoint

```text
Implement the CallBridge Twilio Voice Access Token endpoint on the backend only, following .agents/SYSTEM_RULES.md.

Before coding, verify the current official Twilio documentation and the exact server SDK/API versions used by this repository. Do not guess SDK APIs or copy obsolete sample code.

Target endpoint:
POST /api/v1/voice/token

Requirements:
- Require valid user authentication and authorization.
- Issue only the Voice grant and identity required by the approved outbound-calling design.
- Use a short-lived token and appropriate Cache-Control: no-store behavior.
- Keep Twilio API secrets and signing credentials on the server only.
- Read secrets from validated environment configuration or a deployment secret manager.
- Apply rate limits and safe error handling.
- Do not log tokens, credentials, or full sensitive payloads.
- Test unauthorized requests, configuration failures, and successful token generation using test credentials/configuration without placing a real call.
- Document required environment-variable names in .env.example using placeholders only.

Do not implement the Android calling client or TwiML webhook in this milestone unless explicitly requested. Do not print real secrets or generated access tokens in logs or the final report.

Run relevant backend checks and report exact commands and observed results. Clearly distinguish token-generation tests from a real end-to-end call.
```

## Prompt 6 — Implement the Outbound TwiML Webhook

```text
Implement the backend Twilio outbound Voice webhook for CallBridge, following the approved API contract and .agents/SYSTEM_RULES.md.

Before coding, verify current official Twilio webhook-signature validation guidance and the installed SDK version.

Expected route:
POST /api/v1/voice/outgoing

Requirements:
- Validate the Twilio request signature against the correct externally visible request URL and request parameters using the official supported method.
- Reject invalid signatures.
- Validate and normalize the destination number on the server.
- Enforce the approved destination allowlist/policy and authorized caller ID.
- Never trust a client-side validation result as authorization.
- Return valid TwiML in the format Twilio expects, not ordinary JSON.
- Handle malformed input, disallowed destinations, and provider/configuration failures safely.
- Do not expose secrets or internal error details.
- Add tests for invalid signature, invalid/disallowed destination, and valid TwiML output.
- Make local development testing instructions explicit without weakening production signature validation.

Do not bypass provider restrictions, identity verification, regional rules, or applicable laws. Do not claim a real call works based only on unit tests.

Run relevant backend checks and report exact commands and observed results.
```

## Prompt 7 — Implement Twilio Status Callbacks Idempotently

```text
Implement POST /api/v1/voice/status for CallBridge according to the approved backend contract.

Verify the official Twilio callback fields and status semantics for the configured Voice product and SDK before coding.

Requirements:
- Validate Twilio webhook signatures.
- Validate required identifiers and status fields at runtime.
- Handle duplicate, delayed, and out-of-order callbacks safely.
- Make persistence idempotent so retries do not create duplicate history records.
- Keep provider identifiers and internal user IDs correctly associated.
- Do not accept a callback as proof of a user identity beyond what the verified provider request establishes.
- Redact personal data and secrets from logs.
- Add tests for duplicate callbacks, out-of-order updates, malformed payloads, and invalid signatures.
- Document any status mapping and limitations.

Do not assume every callback arrives exactly once or in order. Run backend checks and report verified behavior.
```

## Prompt 8 — Integrate the Android Calling Layer

```text
Implement the next Android calling-integration milestone for CallBridge using the official Twilio Voice Android SDK.

Before coding:
- Inspect the current app architecture and installed SDK version.
- Read the official documentation and matching version examples.
- Confirm the backend token endpoint and webhook configuration contract.
- Do not invent SDK methods or lifecycle callbacks.

Implement a testable calling abstraction and connect it to the UI only as required by the current milestone.

Requirements:
- Request RECORD_AUDIO at the appropriate runtime moment and handle denial gracefully.
- Obtain a short-lived Access Token from the authenticated backend over HTTPS.
- Never place Twilio permanent credentials or a permanent shared backend secret in the client.
- Validate/normalize the destination and also rely on server-side enforcement.
- Reflect actual SDK call states; never fabricate ringing, connection, or success.
- Start duration timing only when the SDK confirms connection.
- Support hang-up and, when supported by the configured SDK, mute and speaker controls.
- Prevent multiple active calls and make state transitions resilient to duplicate/late callbacks.
- Handle offline state, token failure, permission denial, call failure, and app lifecycle changes.
- Avoid unnecessary permissions and sensitive logging.
- Add tests for state mapping and duplicate terminal events where practical.

If SDK integration cannot be verified without account configuration, implement a clearly isolated interface and tests, label the limitation, and do not present a mock as real calling.

Run the actual Android build and relevant tests. Report what is and is not verified.
```

## Prompt 9 — Build the Dial Pad and Number Validation

```text
Implement the CallBridge dial pad and phone-number validation UX according to the existing design and project rules.

Requirements:
- Default country calling code is +1 for the approved US-calling MVP.
- Make the selected country/calling code visible and understandable.
- Normalize user input safely and validate the expected destination policy.
- Do not treat a syntactically valid number as proof that it is reachable, permitted, or authorized.
- Provide clear invalid-number and disallowed-destination messages.
- Prevent repeated taps from starting multiple calls.
- Do not start a call until permission, authentication, and calling-layer preconditions are satisfied.
- Keep network/provider errors distinct from number-validation errors.
- Do not fabricate contacts or recent-call entries.
- Add tests for normalization and validation edge cases.

Inspect existing UI conventions before editing. Run relevant Android tests/build and report actual outcomes.
```

## Prompt 10 — Connect Real Call History to the UI

```text
Connect the CallBridge Call History screen to the actual repository/database and real calling lifecycle events.

Requirements:
- Display only records that exist in persistence.
- Show the best available destination/contact label, date/time, status, and duration.
- Distinguish failed, cancelled, completed, and unknown states according to the approved model.
- Use local time for display and consistent timestamp storage.
- Avoid duplicate rows when SDK events or backend callbacks are retried.
- Provide appropriate loading, empty, and error states.
- Do not show fabricated example records in production UI.
- Do not expose access tokens, secrets, or unnecessary personal data.
- Add repository/UI tests where practical.

Do not add cloud sync. Run the relevant Android tests and build and report results.
```

## Prompt 11 — Add Contacts and CRM

```text
Implement the approved CallBridge contacts/CRM milestone only after reviewing the existing data model and UI.

Features, if included in the approved scope:
- Create, view, edit, and delete contacts.
- Search contacts by name, phone number, or company.
- Store only approved fields, such as name, phone number, company, and notes.
- Normalize phone numbers consistently and detect possible duplicates.
- Associate call history with a contact when matching is reliable.
- Add follow-up reminders only if reminder behavior and required permissions are approved.
- Provide empty states, validation errors, and confirmation for destructive actions.
- Do not request Android Contacts permission unless actual system-contact import is explicitly approved and implemented.
- Do not silently merge or delete duplicate contacts.
- Add database migration and tests for any schema change.

Keep data local unless remote sync has separately passed privacy/security review. Run relevant tests and build.
```

## Prompt 12 — Security Review and Hardening

```text
Perform a security review of the current CallBridge repository before changing code.

Inspect:
- Secrets and configuration handling.
- Android manifest permissions.
- Authentication and authorization.
- Twilio token issuance and webhook signature verification.
- Destination validation and caller-ID policy.
- Rate limiting and abuse controls.
- Logging and error responses.
- Local database and migration behavior.
- Dependency versions and known unsafe configuration.
- Git status and ignore rules.

Do not print secret values. If a possible credential is found, identify its file/location and recommend safe rotation without exposing the value.

Deliver:
1. Findings ranked Critical/High/Medium/Low.
2. Evidence and affected files.
3. A remediation plan.
4. Tests needed to validate fixes.

Fix only clear, in-scope issues in small groups. Ask before destructive changes or broad architectural changes. Run relevant checks and report verified fixes versus unresolved risks. Do not claim a formal security audit or certification.
```

## Prompt 13 — End-to-End Outbound Calling Verification

```text
Prepare and, only when the account and environment are properly configured, verify a real outbound CallBridge call.

Before any live test:
- Confirm the user has explicitly approved a real call.
- Confirm Twilio account eligibility, required regulatory information, number configuration, authorized caller ID, destination policy, and expected charges.
- Use only an authorized destination and follow applicable law and provider rules.
- Never expose credentials in chat, source code, logs, or the report.
- If any prerequisite is unclear, stop and report what must be verified first.

Verification checklist:
1. Android user authentication succeeds.
2. The backend issues a short-lived Voice Access Token.
3. The app requests microphone permission correctly.
4. The SDK starts the call and reports actual state changes.
5. The server validates the Twilio webhook and destination policy.
6. Valid TwiML is returned.
7. Connection and terminal states are reflected correctly.
8. Timer starts on actual connection and stops at termination.
9. Call history is written once, without duplicate entries.
10. Failure cases produce safe, understandable errors.

Separate unit/integration test results from real-call observations. Never claim end-to-end success unless a real, authorized test was completed and observed. Do not make repeated test calls without approval.
```

## Prompt 14 — Release Readiness Review

```text
Review CallBridge for release readiness without publishing or deploying anything.

Check:
- App ID, app name, versioning, signing configuration, and build variants.
- Debug logging and accidental secret exposure.
- Android permissions and privacy disclosures.
- Backend environment configuration and deployment instructions.
- Authentication, rate limiting, webhook signature verification, and destination policy.
- Data retention, deletion, export, and privacy documentation.
- Accessibility and UI states.
- Unit tests, backend tests, lint/typecheck, and release build.
- Dependency compatibility and known upgrade risks.
- Setup documentation and safe .env.example.
- Whether all advertised features are actually implemented and verified.

Do not invent a privacy policy, regulatory approval, Play Store approval, provider eligibility, or production test result. Do not publish, deploy, sign with a production key, or incur charges without explicit authorization.

Return a checklist with PASS / FAIL / NOT VERIFIED and evidence for each item. List blocking issues and the smallest next steps.
```

## Prompt 15 — Debug a Build or Test Failure

```text
Debug the following CallBridge failure using .agents/SYSTEM_RULES.md.

First reproduce the issue with the narrowest relevant command. Inspect the full error output and relevant source/configuration before editing.

Rules:
- Identify the root cause rather than hiding the symptom.
- Do not delete tests, disable validation, downgrade security, or replace real functionality with a mock just to get a green build.
- Make the smallest targeted change.
- Add or update a regression test where practical.
- Re-run the failing command and the relevant broader checks.
- Distinguish pre-existing failures from failures caused by your change when evidence permits.
- Do not claim success unless the command actually passes.

Failure details:
[PASTE ERROR OUTPUT HERE]

Report root cause, files changed, exact commands, observed results, and remaining uncertainty.
```

## Prompt 16 — Review an AI-Generated Change Before Accepting It

```text
Review the current uncommitted CallBridge changes as a strict senior code reviewer. Do not modify files yet.

Inspect the diff and relevant surrounding code. Check for:
- Violations of .agents/SYSTEM_RULES.md or the approved specification.
- Compile errors, incorrect SDK APIs, and incompatible dependencies.
- Security/privacy issues, secrets, unsafe logs, or unnecessary permissions.
- Missing validation, incorrect call-state transitions, duplicate writes, or data-loss risks.
- Missing tests, misleading placeholders, fake success states, and undocumented assumptions.
- Unrelated formatting or scope creep.

Return findings ranked by severity, with file paths and concrete evidence. If no issues are found, state which checks you actually performed and what remains unverified. Wait for approval before applying substantial fixes.
```

---

## Suggested Milestone Order

| Milestone | Prompt |
|---|---|
| Repository assessment | 0 |
| Android shell | 1 |
| Local persistence | 2 |
| Backend foundation | 3 |
| Authentication design/implementation | 4 |
| Twilio token endpoint | 5 |
| Outbound TwiML webhook | 6 |
| Status callbacks | 7 |
| Android calling integration | 8 |
| Dial pad and validation | 9 |
| Call history | 10 |
| Contacts/CRM | 11 |
| Security hardening | 12 |
| Real outbound call verification | 13 |
| Release readiness | 14 |
| Debugging | 15 |
| Code review | 16 |

## Final Reminder

A generated code change is not proof of a working feature. Require actual build/test evidence, verify provider-sensitive APIs against official documentation, keep credentials out of the client, and obtain explicit approval before live calls, paid services, destructive changes, deployment, or release.
