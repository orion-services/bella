import test from 'node:test';
import assert from 'node:assert/strict';

import { unvalidatedEmailMessage, EMAIL_NOT_VALIDATED_KEY } from './orionUsers.js';

test('asks the user to validate the email when the account is still unconfirmed', () => {
  assert.equal(
    unvalidatedEmailMessage({ email: 'ana@example.com', emailValid: false }),
    EMAIL_NOT_VALIDATED_KEY
  );
});

test('allows login when the email is already validated', () => {
  assert.equal(unvalidatedEmailMessage({ emailValid: true }), null);
});

test('allows login when the payload does not say whether the email was validated', () => {
  assert.equal(unvalidatedEmailMessage({ email: 'ana@example.com' }), null);
  assert.equal(unvalidatedEmailMessage(null), null);
});
