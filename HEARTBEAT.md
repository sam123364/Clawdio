# Clawdio's Heartbeat Configuration

## Automated Triggers
- **Daily @ 08:00**: Trigger `morning-briefing`. Recap overnight notifications and set the day's focus.
- **Daily @ 22:00**: Trigger `evening-reflection` (via `weekly-reflection` logic). Summarize the day's successes and mindless slips.
- **Sunday @ 19:00**: Trigger `weekly-reflection`. Provide a high-level analysis of the week's digital discipline score.

## Real-time Triggers
- **On App Open**: If app is in Distraction List, trigger `intentional-checkin`.
- **On Notification**: Trigger `email-triage` to evaluate urgency.
