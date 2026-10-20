# CallBridge --- Application Flow (AppFlow)

**Version:** MVP 1.0 flow baseline\
**Related documents:** PRD, TRD, UI/UX Design Brief, Backend Schema\
**Last updated:** 2026-10-20

## 1. Flow principles

-   One active call at a time.
-   Never display a call state that is not supported by SDK/provider
    events.
-   Do not start a call until number validation, microphone permission,
    and token acquisition succeed.
-   Preserve local contacts and call history across app restarts.
-   Fail safely and explain recovery steps.
-   MVP supports outbound calls only; incoming-call flows are explicitly
    deferred.

## 2. App navigation map

``` text
App Launch
  └── Dashboard
       ├── Dial Pad
       │    ├── Number Entry
       │    ├── Validation
       │    ├── Permission Check
       │    ├── Token Request
       │    └── Active Call
       │          ├── Mute / Speaker
       │          ├── End Call
       │          └── Call Result → History / Contact
       ├── Contacts
       │    ├── Contact List / Search
       │    ├── Contact Details
       │    │    ├── Call
       │    │    ├── Edit
       │    │    └── Notes / Follow-up
       │    └── Add / Edit Contact
       ├── Call History
       │    └── Call Details
       │         ├── Call Back
       │         ├── Link Contact
       │         └── Add Note / Follow-up
       └── Settings
            ├── Service Readiness
            ├── Privacy Information
            ├── Diagnostics (redacted)
            └── About / Version
```

## 3. Launch and dashboard flow

1.  Android starts `MainActivity`.
2.  Compose navigation initializes.
3.  Load local contacts/recent calls from Room.
4.  Request a lightweight health/readiness check only when appropriate;
    do not block local navigation if backend is offline.
5.  Render dashboard with:
    -   Primary "New Call" action
    -   Recent calls
    -   Contact shortcut
    -   Service status indicator
6.  If backend is unavailable, keep the dial pad accessible but explain
    that calls cannot begin until service is restored.

**Failure paths** - Room initialization/migration failure: show a safe
recovery error; do not silently erase data. - Health endpoint timeout:
mark status "Unavailable" and allow local features. - Empty history:
show an actionable empty state.

## 4. Outbound call flow

### Main path

1.  User taps New Call or a contact's Call action.
2.  Dial Pad opens with the selected number prefilled if applicable.
3.  User reviews destination and taps Call.
4.  Client normalizes the number to E.164 and validates formatting and
    MVP country policy.
5.  If invalid, remain on dial pad and show a concise validation
    message.
6.  Check microphone permission.
7.  If not granted, show Android runtime permission request with
    rationale.
8.  If permission denied, stop the flow and show retry/Settings
    guidance.
9.  Set UI state to `FetchingToken`; disable duplicate call actions.
10. Request a short-lived Voice Access Token over HTTPS.
11. If token request fails, show an actionable error and retain the
    entered number.
12. Start the SDK call using the configured outgoing call/TwiML app
    setup.
13. Update state from real SDK/provider events (`Connecting`, optionally
    `Ringing`, `Connected`).
14. Once connected, start elapsed-time display based on the connected
    timestamp.
15. User may mute/unmute, change speaker routing if supported, or end
    the call.
16. On terminal event, stop timer, release call resources, and save a
    local history record.
17. Show the final state briefly, then allow return to dashboard,
    history, or contact details.

### Mermaid sequence diagram

``` mermaid
sequenceDiagram
  actor User
  participant UI as Compose UI
  participant VM as ViewModel
  participant API as Backend API
  participant SDK as Twilio Voice SDK
  participant TW as Twilio Platform
  participant DB as Room

  User->>UI: Tap Call
  UI->>VM: startCall(number)
  VM->>VM: Validate number and permission
  VM->>API: POST /api/v1/voice/token
  API-->>VM: Short-lived Access Token
  VM->>SDK: Connect(destination, token)
  SDK->>TW: Establish voice session
  TW-->>SDK: Call events
  SDK-->>VM: State callbacks
  VM-->>UI: Connecting / Connected / Failed
  User->>UI: End call
  UI->>VM: endCall()
  VM->>SDK: Disconnect
  SDK-->>VM: Terminal call result
  VM->>DB: Save call history
  VM-->>UI: Call ended / summary
```

## 5. Call-state transitions

``` text
Idle
  → RequestingPermission (only if needed)
  → FetchingToken
  → Connecting
  → Ringing (only if SDK exposes a reliable event)
  → Connected
  → Ending
  → Ended
```

Failure transitions can occur from permission, token, or call setup:
`RequestingPermission → Failed`, `FetchingToken → Failed`,
`Connecting → Failed`, `Connected → Ending → Ended`.

Rules: - Do not start a second call while any call is active or
connecting. - The timer starts only when a connected event is
received. - A failed call must not be recorded as connected. - If call
status is ambiguous after a process interruption, record the last known
state and mark it unknown/failed according to implementation policy;
never fabricate duration. - Make terminal persistence idempotent to
avoid duplicate history rows from multiple callbacks.

## 6. Contact management flow

### Add contact

Contacts → Add Contact → enter required name and phone, optional
company/notes/follow-up → validate → Save → return to details/list →
confirm success.

### Edit contact

Contact details → Edit → prefilled form → Save → validate → update
`updatedAt` → refresh list/details.

### Delete contact

Contact details → Delete → confirmation dialog → delete contact. Call
history records should remain; set `contactId` to null or retain a
deliberate soft-link policy rather than deleting historical calls.

### Search

Contacts → search field → query name/company/normalized phone → display
matching results → clear query to restore list.

## 7. Call history flow

1.  Dashboard/history screen reads Room records ordered newest first.
2.  Each row shows contact/name if linked, otherwise formatted
    destination, timestamp, duration if known, and outcome.
3.  Tap row to open details.
4.  Actions:
    -   Call back (prefill destination; user confirms call)
    -   Link to existing contact
    -   Add/edit local note or follow-up if supported by selected schema
5.  Empty history offers "Make your first call".
6.  Failed calls show failure category when available; never expose
    internal stack trace.

## 8. Error and recovery flows

  -----------------------------------------------------------------------
  Condition               UI response             Recovery
  ----------------------- ----------------------- -----------------------
  Invalid number          Inline validation       Edit number

  Destination not allowed Explain current         Choose allowed
                          destination policy      destination

  Microphone denied       Permission message      Retry or Android
                                                  Settings

  No internet             Offline banner/error    Retry after connection
                                                  returns

  Token endpoint          Session/configuration   Re-authenticate or
  unauthorized            message                 review setup

  Token endpoint          Temporary service error Retry with backoff
  unavailable                                     

  Twilio                  Generic calling-service Admin/developer checks
  credentials/config      error                   backend logs
  invalid                                         

  Call rejected/failed    Provider/SDK mapped     Retry only if safe
                          message                 

  User ends call while    End action remains      Cancel/disconnect
  connecting              available               safely

  App backgrounds during  Maintain SDK/session    Return to active-call
  call                    state per SDK lifecycle UI
                          guidance                

  App process killed      Do not promise session  Reconcile when possible
                          restoration             and preserve last known
                                                  local data
  -----------------------------------------------------------------------

## 9. Settings/readiness flow

Settings displays: - App version/build. - Backend reachability
(available/unavailable). - Calling configuration readiness (ready/not
ready/unknown; do not expose account SID or secret). - Privacy
information and data storage explanation. - Local data export/delete
controls only if implemented. - Diagnostics summary with redacted
values.

Never expose token values, account auth tokens, API secrets, webhook
signing data, or full server environment variables.

## 10. Future incoming-call flow (not MVP)

Future scope may require: - Twilio push credential and FCM
configuration. - Incoming call push handling. - Notification permission
and full-screen intent behavior subject to Android policy. - CallStyle
notifications/Telecom integration where appropriate. - Lock-screen
answer/reject controls. - Background lifecycle and process-death
recovery. - Token refresh and push registration management.

Do not scaffold incomplete incoming-call UI as if it works. Track it as
a separate milestone after outbound calling is stable.

## 11. Flow QA checklist

-   [ ] New call from dashboard.
-   [ ] New call from contact details.
-   [ ] Invalid number prevents call initiation.
-   [ ] Permission granted/denied/permanently denied.
-   [ ] Token request success, timeout, and server error.
-   [ ] Connecting, connected, remote end, local end, and failed call
    states.
-   [ ] No duplicate calls on rapid taps or recomposition.
-   [ ] Call history persisted once per attempt.
-   [ ] Contact delete does not destroy historical call records.
-   [ ] Backend offline does not break local contacts/history.
-   [ ] UI remains usable with large text and TalkBack.
