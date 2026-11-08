# 🤝 Contributing to CallBridge

Thank you for your interest in contributing to **CallBridge**! We welcome community contributions to build the most private, reliable, and accessible international telecommunications suite.

---

## 🏛️ Code of Conduct

CallBridge is an open-source engineering project architected by **Maheshwar Chandra Roy ([@0xmcr](https://github.com/0xmcr))**. We expect all contributors and maintainers to foster a welcoming, professional, collaborative, and inclusive environment.

---

## 🛠️ Development Setup

### 1. Prerequisites
- **Android Development:**
  - Android Studio Ladybug (or newer)
  - JDK 21 (configured in Android Studio JBR)
  - Android SDK 35 (API 35) with Platform Tools
- **Backend Development:**
  - Node.js 18+ (Node 20+ recommended)
  - npm or pnpm
  - Twilio Account (for testing live VoIP / SMS)

### 2. Fork & Clone
```bash
git clone https://github.com/<your-username>/CallBridge.git
cd CallBridge
```

### 3. Backend Setup
```bash
cd backend
cp .env.example .env
npm install
npm run build
npm start
```
Configure your Twilio credentials in `backend/.env` for local testing.

### 4. Android Client Setup
Open the `/android` directory in Android Studio:
```bash
cd android
./gradlew assembleDebug
```
Install the generated APK on your device or emulator:
```bash
adb install app/build/outputs/apk/debug/app-debug.apk
```

---

## 🌿 Git & Branching Workflow

We adhere to standard GitHub Flow and Conventional Commits:

1. **Create a Feature Branch:**
   ```bash
   git checkout -b feat/your-feature-name
   # or for bug fixes:
   git checkout -b fix/issue-description
   ```

2. **Commit Message Conventions:**
   - `feat(...)`: A new feature (e.g. `feat(sms): add quick templates selector`)
   - `fix(...)`: A bug fix (e.g. `fix(ui): eliminate bottom navigation inset clipping`)
   - `docs(...)`: Documentation updates
   - `refactor(...)`: Code refactoring without behavioral change
   - `test(...)`: Adding or updating test suites

3. **Code Quality Standards:**
   - Maintain strict Kotlin code formatting and Android Architecture components (Jetpack Compose, Room, MVVM/Repository).
   - Backend code must be typed in TypeScript and pass `npm run build` without compiler warnings.
   - **Never hardcode secrets, API keys, or private tokens in any client or repository file.**

---

## 🚀 Pull Request Checklist

Before opening a Pull Request to `main`:

- [ ] Ensure the Android app compiles cleanly with `./gradlew assembleDebug`.
- [ ] Ensure backend builds with `npm run build` and tests pass.
- [ ] Do not commit `.env`, local keystores, IDE caches, or temporary build files.
- [ ] Provide a clear PR title and descriptive summary of changes with screenshots if modifying UI.
- [ ] Link any related issues or discussions.

All Pull Requests require review and approval from repository maintainer **[@0xmcr](https://github.com/0xmcr)** before merging.
