# Clawdio - The Intentional AI Assistant 🦞

**A behavior-aware AI agent built for the OpenClaw Hackathon.**

Clawdio helps you build intentional digital habits by acting as a cognitive filter between you and your phone's distractions.

## 🚀 How it Works
1. **The Brain (PC)**: Built using the **OpenClaw framework**. It uses AI (Gemini/GPT) to evaluate your app usage and notifications based on "Skills."
2. **The Body (Android)**: A Kotlin-based client that intercepts app openings and silences notifications, sending them to the PC for evaluation.

## 🧠 OpenClaw Skills
- `intentional-checkin`: Challenges mindless app openings with reflective prompts.
- `notification-triage`: Filters incoming notifications for urgency across ALL apps.
- `morning-briefing`: Summarizes missed items and sets daily focus.
- `weekly-reflection`: Analyzes behavioral patterns and discipline scores.

## 🛠️ Setup
1. Clone this repo.
2. Run `npm install`.
3. Create a `.env` file with your `GEMINI_API_KEY`.
4. Run `node index.js`.
5. Build and install the Android app from the `android-client` directory.

## 🏗️ Architecture
- **Backend**: Node.js, Express, OpenClaw SDK.
- **Frontend**: Kotlin, Android NotificationListenerService, UsageStatsManager.
- **Protocol**: Local REST API (PC-to-Phone bridge).

---
*Built for the OpenClaw Hackathon 2026.*
