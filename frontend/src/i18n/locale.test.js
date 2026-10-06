import test from 'node:test';
import assert from 'node:assert/strict';

import { browserLocale, dateLocale, htmlLang } from './locale.js';

test('uses Portuguese when the browser language is Portuguese', () => {
  assert.equal(browserLocale('pt-BR'), 'pt');
  assert.equal(browserLocale('pt-PT'), 'pt');
  assert.equal(browserLocale('PT'), 'pt');
});

test('uses English for every other browser language', () => {
  assert.equal(browserLocale('en-US'), 'en');
  assert.equal(browserLocale('es-ES'), 'en');
  assert.equal(browserLocale(''), 'en');
  assert.equal(browserLocale(undefined), 'en');
});

test('maps the interface locale to the document language and date locale', () => {
  assert.equal(htmlLang('pt'), 'pt-BR');
  assert.equal(htmlLang('en'), 'en');
  assert.equal(dateLocale('pt'), 'pt-BR');
  assert.equal(dateLocale('en'), 'en-US');
});
