/**
 * Maps the browser language to one of the locales the interface ships.
 * Portuguese (pt, pt-BR, pt-PT) uses Portuguese. Every other language uses English.
 *
 * @param {string | null | undefined} language BCP 47 tag, usually navigator.language
 * @returns {'pt' | 'en'}
 */
export function browserLocale(language) {
  const tag = (language || 'en').toLowerCase();
  return tag.startsWith('pt') ? 'pt' : 'en';
}

/**
 * Value for the document lang attribute.
 *
 * @param {'pt' | 'en'} locale
 * @returns {'pt-BR' | 'en'}
 */
export function htmlLang(locale) {
  return locale === 'pt' ? 'pt-BR' : 'en';
}

/**
 * Locale used by Intl date formatting.
 *
 * @param {'pt' | 'en'} locale
 * @returns {'pt-BR' | 'en-US'}
 */
export function dateLocale(locale) {
  return locale === 'pt' ? 'pt-BR' : 'en-US';
}
