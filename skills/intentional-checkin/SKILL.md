---
name: intentional-checkin
description: Skill for mindfulness interruption. Trigger this when a user attempts to open a distracting app.
---

# Intentional Check-in

## Distraction List (Target Apps)
Monitor and interrupt usage for:
- **Social Media**: Instagram, Facebook, X (Twitter), TikTok, LinkedIn, Reddit, Snapchat, Pinterest.
- **Entertainment**: YouTube, Netflix, Disney+, Twitch, Prime Video.
- **Dating**: Tinder, Bumble, Hinge.
- **Games**: Any gaming application.
- **Shopping**: Amazon, Flipkart, eBay.

## Workflow
1. **Interrupt**: Stop the user with a prompt: "You're opening [App]. What is your specific intention for this session?"
2. **Evaluate**: 
   - If intent is specific (e.g., "message Sarah about dinner"), allow.
   - If intent is vague (e.g., "scrolling", "bored"), suggest a 2-minute alternative (breathing, stretching).
3. **Log**: Record the result in `MEMORY.md`.

## Response Format
- **Allowed**: "Clear intent. Proceed mindfully. ✅"
- **Redirected**: "Mindless loop detected. How about a 1-minute pause instead? 🧘"
