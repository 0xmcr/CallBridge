# CallBridge

Personal Android app for authenticated outbound calling to permitted US phone numbers using Twilio Voice.

## Project Structure

```
CallBridge/
├── docs/                          # Project documentation
│   ├── 01_PRD.md                  # Product Requirements
│   ├── 02_TRD.md                  # Technical Requirements
│   ├── 03_AppFlow.md              # Navigation & Call Flow
│   ├── 04_UI_UX_Design_Brief.md   # UI/UX Design
│   ├── 05_Backend_Schema.md       # API & Data Schema
│   ├── 06_Implementation_Plan.md  # Milestones & QA
│   ├── CallBridge_AI_Coding_Master_Spec.md
│   └── AI_PROMPTS.md
├── .agents/
│   └── SYSTEM_RULES.md            # AI coding agent rules
├── android/                       # Android app (Kotlin + Compose)
├── backend/                       # Node.js + TypeScript backend
└── .gitignore
```

## Tech Stack

| Layer | Technology |
|-------|-----------|
| Android | Kotlin, Jetpack Compose, Material 3, Room |
| Backend | Node.js, TypeScript, Express |
| Telephony | Twilio Voice SDK |
| Local DB | Room (SQLite) |

## Current Status

- **Phase:** M1 — Android Shell
- **Calling:** Not yet integrated (requires Twilio account upgrade)
- **Distribution:** Private APK only

## Security

- No permanent Twilio credentials in the Android app or source control
- Short-lived Access Tokens issued by the backend only
- All secrets stored in environment variables / secret manager
- See `.agents/SYSTEM_RULES.md` for full security rules

## Important

- Product name and Android package ID are provisional
- Twilio account eligibility, verification, and pricing must be confirmed before paid deployment
- MVP is outbound-call-first; incoming calling is Phase 3
