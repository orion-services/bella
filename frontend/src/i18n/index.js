import { createI18n } from 'vue-i18n';
import { browserLocale } from './locale';
import { messages } from './messages';

const locale = browserLocale(
  typeof navigator !== 'undefined' ? navigator.language : 'en'
);

export const i18n = createI18n({
  legacy: true,
  locale,
  fallbackLocale: 'en',
  messages
});
