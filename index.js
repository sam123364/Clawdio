import express from 'express';
import cors from 'cors';
import { OpenClawAgent } from 'openclaw'; // Assuming standard OpenClaw export
import path from 'path';
import fs from 'fs';

const app = express();
const PORT = 3000;

app.use(cors());
app.use(express.json());

// Initialize the OpenClaw Agent using our project workspace
const agent = new OpenClawAgent({
  workspace: path.resolve('./'),
  agentConfig: path.resolve('./AGENTS.md'),
  skillsDir: path.resolve('./skills')
});

console.log('OpenClaw Backend Starting...');
console.log('Workspace: e:/OpenClaw Hackathon');

/**
 * OpenClaw Execution Flow: 
 * User Request -> Agent.invoke() -> Skill Execution -> Response
 */

// Intent Check Endpoint
app.post('/intent-check', async (req, res) => {
  const { appName, userContext } = req.body;
  
  // Invoke the 'intentional-checkin' skill through the agent
  const response = await agent.invoke('intentional-checkin', {
    app: appName,
    context: userContext
  });
  
  res.json(response);
});

// Notification Endpoint
app.post('/notification', async (req, res) => {
  const { app, sender, message } = req.body;
  
  // Invoke the 'notification-triage' skill through the agent
  const response = await agent.invoke('notification-triage', {
    app: app,
    sender: sender,
    message: message
  });

  // Log to NOTIFICATIONS.md if not urgent
  if (!response.urgent) {
    const timestamp = new Date().toISOString().replace('T', ' ').substring(0, 19);
    const date = timestamp.split(' ')[0];
    const time = timestamp.split(' ')[1].substring(0, 5);
    const logEntry = `| ${date} | ${time} | ${app} | ${sender} | ${message} |\n`;
    fs.appendFileSync(path.resolve('./NOTIFICATIONS.md'), logEntry);
  }
  
  res.json(response);
});

// Get Notifications Log Endpoint
app.get('/get-notifications', (req, res) => {
  try {
    const content = fs.readFileSync(path.resolve('./NOTIFICATIONS.md'), 'utf-8');
    res.json({ content });
  } catch (err) {
    res.status(500).json({ error: 'Could not read notifications' });
  }
});

// Automation / Heartbeat Manual Trigger (For App Testing)
app.get('/morning-briefing', async (req, res) => {
  const response = await agent.invoke('morning-briefing');
  res.json(response);
});

app.get('/weekly-reflection', async (req, res) => {
  const response = await agent.invoke('weekly-reflection');
  res.json(response);
});

app.listen(PORT, () => {
  console.log(`✅ OpenClaw Gateway active on port ${PORT}`);
});
