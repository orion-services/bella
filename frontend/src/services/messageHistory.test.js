import test from 'node:test';
import assert from 'node:assert/strict';

import {
  normalizePersistedMessages,
  normalizeRoleType,
  parseMessageTime
} from './messageHistory.js';

test('normalizes the current memory API contract', () => {
  const messages = normalizePersistedMessages([
    {
      type: 'USER',
      message: 'Pergunta persistida',
      timestamp: '2026-09-29T12:00:00.000Z'
    },
    {
      type: 'AGENT',
      message: 'Resposta persistida',
      timestamp: '2026-09-29T12:00:01.000Z'
    }
  ]);

  assert.deepEqual(messages, [
    { type: 'user', content: 'Pergunta persistida', isNew: false },
    { type: 'assistant', content: 'Resposta persistida', isNew: false }
  ]);
});

test('keeps compatibility with content and gives precedence to message', () => {
  const messages = normalizePersistedMessages([
    { type: 'HUMAN', content: 'Contrato antigo' },
    { type: { name: 'AGENT' }, message: 'Contrato atual', content: 'Ignorado' }
  ]);

  assert.deepEqual(messages, [
    { type: 'user', content: 'Contrato antigo', isNew: false },
    { type: 'assistant', content: 'Contrato atual', isNew: false }
  ]);
});

test('sorts dated messages and preserves source order for equal or missing dates', () => {
  const messages = normalizePersistedMessages([
    { message: 'segunda', timestamp: 2000 },
    { message: 'primeira', timestamp: 1000 },
    { message: 'mesmo instante A', timestamp: 3000 },
    { message: 'mesmo instante B', timestamp: 3000 },
    { message: 'sem data A' },
    { message: 'sem data B' }
  ]);

  assert.deepEqual(
    messages.map((message) => message.content),
    ['primeira', 'segunda', 'mesmo instante A', 'mesmo instante B', 'sem data A', 'sem data B']
  );
});

test('handles invalid inputs and role defaults', () => {
  assert.deepEqual(normalizePersistedMessages(null), []);
  assert.equal(normalizeRoleType(null), 'assistant');
  assert.equal(parseMessageTime({ timestamp: 'invalid' }), null);
  assert.equal(
    Number.isFinite(parseMessageTime({ timestamp: [2026, 9, 29, 12, 0, 1] })),
    true
  );
});
