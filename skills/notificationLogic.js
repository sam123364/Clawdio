import fs from 'fs';
import path from 'path';

const NOTIFICATIONS_FILE = path.join(process.cwd(), 'NOTIFICATIONS.md');

/**
 * Basic urgency evaluator.
 * Checks for urgency keywords.
 */
function evaluateUrgency(text) {
  const lowercaseText = (text || '').toLowerCase();
  const urgentKeywords = ['urgent', 'now', 'asap', 'emergency', 'deadline', 'important'];
  return urgentKeywords.some(keyword => lowercaseText.includes(keyword));
}

/**
 * Logs a non-urgent notification into NOTIFICATIONS.md
 */
function logNotification({ app, sender, message }) {
  const now = new Date();
  const dateStr = now.toISOString().split('T')[0];
  const timeStr = now.toTimeString().split(' ')[0].substring(0, 5);

  const safeApp = (app || 'Unknown').replace(/\|/g, '');
  const safeSender = (sender || 'Unknown').replace(/\|/g, '');
  const safeMessage = (message || '').replace(/\|/g, '').replace(/\\n/g, ' ');

  const logEntry = `| ${dateStr} | ${timeStr} | ${safeApp} | ${safeSender} | ${safeMessage} |\n`;

  try {
    fs.appendFileSync(NOTIFICATIONS_FILE, logEntry, 'utf8');
  } catch (error) {
    console.error('Failed to write to NOTIFICATIONS.md', error);
  }
}

/**
 * POST /notification
 * Receives an intercepted notification, evaluates it, and logs if not urgent.
 */
export function handleNotification(req, res) {
  const { app, sender, message } = req.body;
  if (!app || !message) {
    return res.status(400).json({ error: 'App name and message are required' });
  }

  const isUrgent = evaluateUrgency(message);

  if (isUrgent) {
    // Tell Android to trigger a high-priority alert immediately
    return res.json({
      urgent: true,
      action: 'trigger_alert',
      alertMessage: `🚨 URGENT message from ${sender} on ${app}: "${message}"`
    });
  } else {
    // Silently log for the daily summary
    logNotification({ app, sender, message });
    return res.json({
      urgent: false,
      action: 'ignore'
    });
  }
}

/**
 * GET /daily-summary
 * Reads NOTIFICATIONS.md and returns a structured summary of missed notifications.
 */
export function handleDailySummary(req, res) {
  try {
    if (!fs.existsSync(NOTIFICATIONS_FILE)) {
      return res.json({ summary: "No notifications to summarize." });
    }

    const content = fs.readFileSync(NOTIFICATIONS_FILE, 'utf8');
    const lines = content.split('\n');
    
    const dataLines = lines.filter(line => line.startsWith('|') && !line.includes('----') && !line.includes('Date | Time'));
    
    let totalMessages = 0;
    const appCounts = {};

    dataLines.forEach(line => {
      const parts = line.split('|').map(p => p.trim());
      if (parts.length >= 6) {
        const app = parts[3];
        if (app) {
          appCounts[app] = (appCounts[app] || 0) + 1;
        }
        totalMessages++;
      }
    });

    if (totalMessages === 0) {
      return res.json({ summary: "No missed notifications." });
    }

    let summaryText = `You missed ${totalMessages} notifications while you were focusing.\n\nBreakdown:\n`;
    for (const [app, count] of Object.entries(appCounts)) {
      summaryText += `- ${app}: ${count} messages\n`;
    }
    
    res.json({
      totalMessages,
      breakdown: appCounts,
      summary: summaryText
    });

  } catch (error) {
    console.error('Failed to generate daily summary', error);
    res.status(500).json({ error: 'Failed to generate summary' });
  }
}
