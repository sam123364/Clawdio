/**
 * Bouncer Skill for OpenClaw
 * This skill handles the intent evaluation and chat logic natively.
 */
export const skill = {
  name: 'bouncer',
  description: 'Guards against distracting app usage',
  
  // This skill handles the "create_flow" action from webhooks
  handleWebhook: async (api, { action, goal, app, data, intent }) => {
    if (app && !intent) {
      // Intent Check Trigger
      return {
        message: `Before you open ${app} — what are you hoping to do?`
      };
    }
    
    if (app && intent) {
      // Intent Evaluation
      const prompt = `User wants to open ${app} with intent: "${intent}". 
      Is this SPECIFIC (productive) or VAGUE (mindless)?
      Return JSON: {"decision": "allow"|"redirect", "message": "your brief response"}`;
      
      const response = await api.agent.think(prompt);
      const jsonMatch = response.text.match(/\{.*\}/s);
      return jsonMatch ? JSON.parse(jsonMatch[0]) : { decision: 'redirect', message: 'Be more specific.' };
    }

    if (goal) {
      // Chat Bot
      const response = await api.agent.think(goal);
      return { reply: response.text };
    }

    if (data) {
      // Notification Triage
      const prompt = `Triage this notification from ${data.app} (Sender: ${data.sender}): "${data.message}".
      Is it URGENT or CASUAL? 
      Return JSON: {"urgent": true|false, "alertMessage": "short summary if urgent"}`;
      
      const response = await api.agent.think(prompt);
      const jsonMatch = response.text.match(/\{.*\}/s);
      return jsonMatch ? JSON.parse(jsonMatch[0]) : { urgent: false };
    }

    return { error: 'Unknown bouncer action' };
  }
};
