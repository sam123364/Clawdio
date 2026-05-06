import fs from 'fs';
import path from 'path';

const MEMORY_FILE = path.join(process.cwd(), 'MEMORY.md');

/**
 * Logs an interaction into MEMORY.md
 */
export function logIntent({ app, intent, type, response }) {
  const now = new Date();
  const dateStr = now.toISOString().split('T')[0];
  const timeStr = now.toTimeString().split(' ')[0].substring(0, 5); // HH:mm

  // Escape pipes for markdown table
  const safeApp = app.replace(/\|/g, '');
  const safeIntent = intent.replace(/\|/g, '');
  const safeResponse = response.replace(/\|/g, '');

  const logEntry = `| ${dateStr} | ${timeStr} | ${safeApp} | ${safeIntent} | ${type} | ${safeResponse} |\n`;

  try {
    fs.appendFileSync(MEMORY_FILE, logEntry, 'utf8');
  } catch (error) {
    console.error('Failed to write to MEMORY.md', error);
  }
}

/**
 * Parses MEMORY.md and returns a summary object
 */
export function getSummary() {
  try {
    if (!fs.existsSync(MEMORY_FILE)) {
      return { message: "No memory file found." };
    }

    const content = fs.readFileSync(MEMORY_FILE, 'utf8');
    const lines = content.split('\n');
    
    // Skip headers
    const dataLines = lines.filter(line => line.startsWith('|') && !line.includes('----') && !line.includes('Date | Time'));
    
    let totalLogs = 0;
    let vagueCount = 0;
    const appCounts = {};

    dataLines.forEach(line => {
      const parts = line.split('|').map(p => p.trim());
      if (parts.length >= 7) { // empty start/end because of leading/trailing pipes
        const app = parts[3];
        const type = parts[5];

        if (app) {
          appCounts[app] = (appCounts[app] || 0) + 1;
        }
        if (type.toLowerCase() === 'vague') {
          vagueCount++;
        }
        totalLogs++;
      }
    });

    if (totalLogs === 0) {
      return { message: "Not enough data yet for a summary." };
    }

    // Find most frequent app
    let mostFrequentApp = null;
    let maxCount = 0;
    for (const [app, count] of Object.entries(appCounts)) {
      if (count > maxCount) {
        maxCount = count;
        mostFrequentApp = app;
      }
    }

    const vaguePercentage = Math.round((vagueCount / totalLogs) * 100);

    const report = `Weekly Report:
- Most frequently used app: ${mostFrequentApp || 'None'}
- Total interactions: ${totalLogs}
- Vague intention percentage: ${vaguePercentage}%

Suggestion: You used ${mostFrequentApp} frequently. With ${vaguePercentage}% of your usage lacking clear intention, consider limiting usage of this app when you don't have a specific goal.`;

    return {
      totalLogs,
      vaguePercentage,
      mostFrequentApp,
      report
    };

  } catch (error) {
    console.error('Failed to generate summary from MEMORY.md', error);
    return { error: 'Failed to generate summary' };
  }
}
