# 🤖 CallBridge — AI Agent Workspace & Document Index

This folder (`.agents/`) contains all authoritative specifications, rules, prompts, and architectural blueprints for CallBridge. Any AI coding assistant working on this project must consult these files before implementing code.

---

## 📑 Core Specifications & Roles

| File | Purpose | Key Focus |
|---|---|---|
| [`SYSTEM_RULES.md`](file:///.agents/SYSTEM_RULES.md) | **Mandatory Agent Operating Rules** | Absolute rules for code safety, secret redaction, and no-fake-calling policies. |
| [`01_PRD.md`](file:///.agents/01_PRD.md) | **Product Requirements** | Outbound calling MVP scope, user journeys, success criteria. |
| [`02_TRD.md`](file:///.agents/02_TRD.md) | **Technical Requirements** | Architecture (Kotlin/Compose + Room + Node.js/Express + Twilio). |
| [`03_AppFlow.md`](file:///.agents/03_AppFlow.md) | **Application & Call Flow** | Screen navigation map, call state machine, error recovery states. |
| [`04_UI_UX_Design_Brief.md`](file:///.agents/04_UI_UX_Design_Brief.md) | **UI/UX Design Specifications** | Material 3 themes, typography, accessibility, touch targets. |
| [`05_Backend_Schema.md`](file:///.agents/05_Backend_Schema.md) | **Backend API & Data Contracts** | Token service, TwiML App Webhook, Room schema, error shapes. |
| [`06_Implementation_Plan.md`](file:///.agents/06_Implementation_Plan.md) | **Milestone Roadmap & QA** | M0 to M8 step-by-step implementation milestones. |
| [`CallBridge_AI_Coding_Master_Spec.md`](file:///.agents/CallBridge_AI_Coding_Master_Spec.md) | **Master Engineering Spec** | Complete consolidated engineering specifications. |
| [`AI_PROMPTS.md`](file:///.agents/AI_PROMPTS.md) | **Milestone Prompts** | Step-by-step modular task prompts for AI assistants. |
| [`IMPROVEMENT_PLAN.md`](file:///.agents/IMPROVEMENT_PLAN.md) | **Active Execution & Improvement Plan** | Live progress tracker and tactical engineering improvements. |

---

## 🔒 Crucial Reminders for AI Agents

1. **No Permanent Secrets:** Never place Twilio Auth Tokens, API secrets, or private keys in the Android APK, repository, logs, or comments.
2. **Deterministic States:** Never fake a connected call or fabricate call duration. Durations start only when confirmed by the SDK.
3. **Verified Dependencies:** Always check and use compatible, stable dependency versions.
