import { logIntent, getSummary } from './memoryManager.js';

/**
 * Basic intent evaluator.
 * A real version would use an LLM (like Claude or GPT) via the OpenClaw framework.
 * For this demo, we use heuristic keywords.
 */
function evaluateIntent(intentText) {
  const text = (intentText || '').toLowerCase();
  
  const vagueKeywords = ['just', 'nothing', 'scrolling', 'bored', 'timepass', 'idk', 'don\'t know'];
  
  const isVague = vagueKeywords.some(keyword => text.includes(keyword)) || text.length < 10;
  
  if (isVague) {
    return {
      type: 'Vague',
      decision: 'redirect',
      message: 'Do you want to take a short break or do something productive instead?'
    };
  } else {
    return {
      type: 'Specific',
      decision: 'allow',
      message: 'Sounds like a plan. Go ahead and stay focused!'
    };
  }
}

/**
 * Handles the POST /intent-check endpoint
 */
export function handleIntentCheck(req, res) {
  const { app } = req.body;
  if (!app) {
    return res.status(400).json({ error: 'App name is required' });
  }

  // Interruption prompt
  res.json({
    message: `Before you open ${app} — what are you hoping to do?`
  });
}

/**
 * Handles the POST /intent-response endpoint
 */
export function handleIntentResponse(req, res) {
  const { app, intent } = req.body;
  if (!app || !intent) {
    return res.status(400).json({ error: 'App name and intent are required' });
  }

  const evaluation = evaluateIntent(intent);
  
  // Log to memory
  logIntent({
    app,
    intent,
    type: evaluation.type,
    response: evaluation.decision
  });

  res.json({
    decision: evaluation.decision,
    message: evaluation.message,
    type: evaluation.type
  });
}

/**
 * Handles the GET /summary endpoint
 */
export function handleSummary(req, res) {
  const summary = getSummary();
  res.json(summary);
}

/**
 * Handles the GET /email-check endpoint (mock data)
 */
export function handleEmailCheck(req, res) {
  res.json({
    message: 'You have 5 new emails, 2 are important including one from your professor.'
  });
}
