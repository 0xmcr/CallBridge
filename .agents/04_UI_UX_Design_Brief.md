# CallBridge --- UI/UX Design Brief

**Product:** CallBridge Android app\
**Audience:** Personal operator making legitimate outbound calls to US
destinations\
**Platform:** Android, native Compose UI\
**Design direction:** Calm, clear, trustworthy, productivity-focused\
**Last updated:** 2026-10-20

## 1. Design objective

Create an interface that makes placing a call the clearest primary
action while keeping contacts and call history easy to reach. The UI
should feel like a focused personal calling tool, not a complex CRM or a
consumer social app.

## 2. UX principles

1.  **Call action is obvious:** primary action is visible from the
    dashboard.
2.  **Destination is always clear:** show the number/contact before
    dialing and during the call.
3.  **States are truthful:** never imply a call connected before the SDK
    confirms it.
4.  **Errors help users recover:** explain what happened and what to do
    next.
5.  **Keep personal data private:** local-first contacts and notes.
6.  **Accessible by default:** scalable text, adequate contrast,
    screen-reader labels, and touch-friendly controls.
7.  **Avoid visual overload:** no decorative dashboards, fake analytics,
    or invented billing data.

## 3. Information architecture

Bottom navigation (recommended): - Home - Contacts - History - Settings

Dial Pad and Active Call are task screens. Dial Pad is opened from the
primary Home action; Active Call is a dedicated full-screen route and
must not be duplicated in the back stack.

## 4. Visual system

### Color tokens

Use semantic Material 3 color roles and support light/dark themes. Exact
hex values may be selected during implementation, but use a consistent
palette: - Primary: deep blue/indigo for main actions. - Secondary:
muted teal or slate for secondary emphasis. - Success: accessible green
for confirmed connected/completed states. - Warning: amber for
caution/limited readiness. - Error: accessible red for
failure/destructive actions. - Surface/background: neutral, low-glare
backgrounds. - Text: high-contrast on surface.

Do not use color alone to communicate status; pair color with text
and/or icon.

### Typography

-   Use Material 3 type scale.
-   Screen title: `titleLarge`/`headlineSmall` depending on hierarchy.
-   Main dialed number: large, readable, wrapping safely.
-   Body: `bodyLarge` for primary information; `bodyMedium` for
    supporting text.
-   Labels and timestamps: `labelMedium`/`bodySmall`, but do not shrink
    critical call state text excessively.
-   Respect system font scaling.

### Spacing and shape

-   Use an 8dp base spacing rhythm (4dp allowed for fine alignment).
-   Standard card corner radius: 16dp.
-   Buttons: minimum 48dp height; larger primary call control is
    appropriate.
-   Maintain comfortable spacing around destructive actions.
-   Avoid dense tables on phone screens; use cards/list rows.

### Icons

Use familiar Material/Lucide-equivalent icons with accessible content
descriptions. Icons must have labels when meaning is not obvious. Do not
use emoji as primary action icons in the shipped UI.

## 5. Screen specifications

### Screen A --- Dashboard/Home

**Purpose:** entry point and quick access.

Components: - Top app bar with CallBridge wordmark/title and optional
service-status indicator. - Primary "New Call" button. - Secondary
shortcuts: Contacts, History. - Recent calls section (up to a small
handful; exact count tuned during implementation). - Each recent row:
name or formatted number, date/time, outcome icon + text, duration if
known. - Empty state: "No calls yet" plus "Start a call". -
Offline/backend-unavailable banner that does not block access to local
contacts/history.

Interactions: - Tap New Call → Dial Pad. - Tap recent item → Call
Details. - Tap Contacts/History → respective tab. - Service indicator
opens a safe readiness summary.

### Screen B --- Dial Pad

**Purpose:** enter/review a number and initiate a call.

Components: - Top bar with Back and "New Call". - Large editable number
field with country prefix defaulting to `+1`; the complete number
remains editable. - Numeric keypad (0--9, plus/backspace as
appropriate). - Clear and backspace actions. - Optional matching contact
suggestion. - Primary Call button disabled when empty or invalid. -
Inline validation/helper text. - No claim of destination reachability
based only on syntax validation.

Interactions: - Tap digits to append. - Long-press backspace may clear
the input if implemented accessibly. - Paste/manual edit supported. -
Call → validation → permission → token → SDK call. - During setup,
button shows loading and is disabled against repeated taps.

### Screen C --- Active Call

**Purpose:** give the user clear call control and status.

Components: - Contact name if linked; otherwise formatted destination
number. - Explicit call state label: Connecting, Ringing (only if
confirmed), Connected, Ending, Ended, or Failed. - Elapsed timer only
after Connected. - Large End Call button, visually distinct and easy to
tap. - Mute and Speaker controls with clear selected states. - Optional
secondary details hidden behind a disclosure if needed. - Error/retry
content for failures.

Rules: - Do not display a running duration while only connecting. - End
Call is available while connecting. - Disable unsupported controls
rather than showing nonfunctional affordances. - Avoid full-screen
alarming red except for destructive/ending emphasis.

### Screen D --- Contacts List

Components: - Search field. - Add Contact action. - Alphabetical or
recently updated list; choose one and remain consistent. - Rows show
name, company if available, and phone number. - Empty state with Add
Contact action. - Search empty state with clear-search action.

### Screen E --- Contact Details

Components: - Name, company, phone. - Primary Call action. - Edit action
and secondary Delete action. - Notes and follow-up date. - Related
recent calls if supported without overcomplicating MVP. - Phone number
should be tappable/copyable only through deliberate action.

### Screen F --- Add/Edit Contact

Fields: - Name (required) - Phone number (required) - Company
(optional) - Notes (optional) - Follow-up date (optional) - Save /
Cancel

Validation: - Required fields clearly indicated. - Preserve user input
when validation fails. - Explain invalid number format without claiming
reachability. - Confirm discard if the form has unsaved changes and the
user navigates away.

### Screen G --- Call History

Components: - Newest-first list. - Outcome label: Completed, Failed,
Cancelled/Not connected, or Unknown as supported by real data. -
Number/contact, timestamp, duration if known. - Optional filters/search
in a later P1 iteration. - Empty state leads to Dial Pad.

### Screen H --- Call Details

Components: - Destination/contact. - Start/end time and duration when
available. - Outcome and safe error category. - Call back action
(requires user confirmation on Dial Pad). - Link to contact and local
note/follow-up if supported. - Never expose provider raw payloads or
internal diagnostics.

### Screen I --- Settings / Service Readiness

Components: - Backend reachability. - Calling configuration status,
expressed as Ready / Not ready / Unknown. - App version. - Privacy/data
storage explanation. - Diagnostic ID or safe error code where useful. -
Export/delete controls only when implemented and tested.

Never show account secrets, access tokens, API keys, webhook signatures,
or unredacted environment configuration.

## 6. Component behavior

### Buttons

-   Primary action: one per screen where possible.
-   Loading action: show progress and disable duplicate submission.
-   Destructive action: confirmation required.
-   Disabled buttons must remain understandable (helper text can explain
    why).

### Lists

-   Clear visual separation without heavy borders.
-   Show empty, loading, and error states.
-   Preserve scroll position when navigating back.
-   Avoid showing full phone numbers in diagnostic contexts.

### Forms

-   Inline validation and keyboard-appropriate input types.
-   Support IME actions and scrolling when the keyboard is open.
-   Never erase entered values because an API request failed.

### Banners and dialogs

-   Use banners for non-blocking service status.
-   Use dialogs only for decisions that need confirmation.
-   Permission rationale should precede the system prompt when
    appropriate and must not misrepresent the permission.

## 7. Accessibility

-   Minimum practical touch target of 48dp.
-   Semantic labels for dial pad digits, call controls, and status
    changes.
-   TalkBack order follows visual hierarchy.
-   Text supports system scaling without clipping.
-   Contrast meets current Android/Material accessibility guidance.
-   Do not encode outcome solely with color.
-   Provide a non-color label for mute/speaker active states.
-   Test on small screens and large font settings.

## 8. Responsive behavior and orientation

Primary target is portrait phone use. Layouts must remain usable on
narrow screens and with keyboard visible. Tablet support can use wider
content bounds and two-pane contacts/history layouts later. Do not
require landscape orientation for calling.

## 9. Motion and feedback

-   Use subtle Material motion for navigation and state changes.
-   Avoid long animations on call-critical screens.
-   Use haptic feedback only for deliberate keypad/control actions where
    appropriate and respectful of system settings.
-   Keep elapsed timer updates efficient; Compose recomposition should
    not restart SDK operations.

## 10. UI state inventory

Every screen should define: - Initial/loading state - Content state -
Empty state - Recoverable error state - Offline state where applicable -
Permission-denied state where applicable - Disabled-action state with
explanation

## 11. Design handoff checklist

-   [ ] Tokens and typography mapped to Material 3 theme.
-   [ ] Light and dark themes reviewed.
-   [ ] Every screen has loading/empty/error states.
-   [ ] Dial Pad handles invalid, pasted, and long numbers safely.
-   [ ] Active Call timer starts only on confirmed connection.
-   [ ] All call controls have accessible labels.
-   [ ] Contact forms preserve data on validation/network failure.
-   [ ] No secret, raw provider payload, or sensitive phone number
    appears in diagnostics.
-   [ ] Compose previews created for key states.
-   [ ] Final layouts tested with TalkBack, large font, and narrow
    screens.
