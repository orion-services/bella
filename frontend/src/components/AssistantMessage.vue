<template>
  <div class="assistant-message-block">
    <div
      class="assistant-message-content markdown-content"
      v-html="renderedHtml"
    ></div>
    <div v-if="showCopy" class="assistant-message-actions">
      <v-btn
        icon
        variant="text"
        size="small"
        :aria-label="copied ? $t('chat.copied') : $t('chat.copy')"
        @click="$emit('copy')"
      >
        <v-icon aria-hidden="true">{{ copied ? 'mdi-check' : 'mdi-content-copy' }}</v-icon>
      </v-btn>
    </div>
  </div>
</template>

<script>
import { marked } from 'marked';
import hljs from 'highlight.js';
import 'highlight.js/styles/github-dark.css';

marked.use({
  breaks: true,
  gfm: true,
  highlight: function(code, lang) {
    const language = hljs.getLanguage(lang) ? lang : 'plaintext';
    try {
      return hljs.highlight(code, { language }).value;
    } catch (err) {
      return hljs.highlight(code, { language: 'plaintext' }).value;
    }
  }
});

export default {
  name: 'AssistantMessage',
  props: {
    content: {
      type: String,
      default: ''
    },
    copied: {
      type: Boolean,
      default: false
    },
    showCopy: {
      type: Boolean,
      default: false
    }
  },
  emits: ['copy'],
  computed: {
    renderedHtml() {
      return this.renderMarkdown(this.content);
    }
  },
  methods: {
    cleanContent(text) {
      if (!text) return '';
      return text.replace(/^data:\s*/gm, '');
    },

    renderMarkdown(text) {
      try {
        if (!text) return '';

        const cleaned = this.cleanContent(text);
        if (!cleaned) return '';

        const normalized = this.normalizeIncompleteMarkdown(cleaned);
        const processed = this.processLineBreaks(normalized);
        const html = marked.parse(processed);

        this.$nextTick(() => {
          const root = this.$el;
          if (!root) return;
          root.querySelectorAll('pre code').forEach((block) => {
            if (!block.classList.contains('hljs')) {
              hljs.highlightElement(block);
            }
          });
        });

        return html;
      } catch (error) {
        console.error('Error rendering markdown:', error);
        return this.escapeHtml(text);
      }
    },

    processLineBreaks(text) {
      return text
        .replace(/\r\n/g, '\n')
        .replace(/\r/g, '\n');
    },

    normalizeIncompleteMarkdown(text) {
      let normalized = text;
      const codeBlockMatches = normalized.match(/```/g);
      if (codeBlockMatches && codeBlockMatches.length % 2 !== 0) {
        normalized += '\n```';
      }
      return normalized;
    },

    escapeHtml(text) {
      const div = document.createElement('div');
      div.textContent = text;
      return div.innerHTML;
    }
  }
};
</script>

<style scoped>
.assistant-message-block {
  width: 100%;
  max-width: min(100%, 52rem);
  margin: 0 auto;
}

.assistant-message-content {
  text-align: start;
  padding: 0.75rem 1rem;
  word-wrap: break-word;
  overflow-wrap: anywhere;
  font-size: 0.9375rem;
  line-height: 1.6;
  overflow-x: auto;
}

.assistant-message-actions {
  display: flex;
  justify-content: flex-end;
  padding: 0 0.25rem;
}

.markdown-content {
  word-wrap: break-word;
  overflow-wrap: anywhere;
  color: rgba(var(--v-theme-on-surface), 0.87);
}

.markdown-content :deep(p) {
  margin-bottom: 1rem;
  line-height: 1.6;
  min-height: 1.6em;
}

.markdown-content :deep(p:last-child) {
  margin-bottom: 0;
}

.markdown-content :deep(p + p) {
  margin-top: 0.75rem;
}

.markdown-content :deep(br) {
  line-height: 1.6;
}

.markdown-content :deep(h1),
.markdown-content :deep(h2),
.markdown-content :deep(h3),
.markdown-content :deep(h4),
.markdown-content :deep(h5),
.markdown-content :deep(h6) {
  margin-top: 1.5rem;
  margin-bottom: 0.75rem;
  font-weight: 600;
  line-height: 1.25;
  color: rgb(var(--v-theme-on-surface));
}

.markdown-content :deep(h1) {
  font-size: 1.75rem;
  border-bottom: 1px solid rgba(var(--v-theme-on-surface), 0.12);
  padding-bottom: 0.3rem;
}

.markdown-content :deep(h2) {
  font-size: 1.5rem;
  border-bottom: 1px solid rgba(var(--v-theme-on-surface), 0.12);
  padding-bottom: 0.3rem;
}

.markdown-content :deep(h3) {
  font-size: 1.25rem;
}

.markdown-content :deep(h4) {
  font-size: 1.1rem;
}

.markdown-content :deep(h5) {
  font-size: 1rem;
}

.markdown-content :deep(h6) {
  font-size: 0.9rem;
  color: rgba(var(--v-theme-on-surface), 0.6);
}

.markdown-content :deep(h1:first-child),
.markdown-content :deep(h2:first-child),
.markdown-content :deep(h3:first-child) {
  margin-top: 0;
}

.markdown-content :deep(code) {
  background-color: rgba(var(--v-theme-on-surface), 0.08);
  padding: 0.2em 0.4em;
  border-radius: 3px;
  font-family: 'Courier New', Courier, monospace;
  font-size: 0.9em;
  color: #e83e8c;
}

.markdown-content :deep(pre) {
  background-color: #1e1e1e;
  padding: 1rem;
  border-radius: 6px;
  overflow-x: auto;
  margin: 1rem 0;
  line-height: 1.45;
  border: 1px solid rgba(0, 0, 0, 0.1);
}

.markdown-content :deep(pre code) {
  background-color: transparent;
  padding: 0;
  color: #d4d4d4;
  font-size: 0.9em;
  display: block;
  overflow-x: auto;
}

.markdown-content :deep(ul),
.markdown-content :deep(ol) {
  margin: 0.75rem 0;
  padding-left: 2rem;
  line-height: 1.6;
}

.markdown-content :deep(li) {
  margin: 0.25rem 0;
}

.markdown-content :deep(ul ul),
.markdown-content :deep(ol ol),
.markdown-content :deep(ul ol),
.markdown-content :deep(ol ul) {
  margin-top: 0.25rem;
  margin-bottom: 0.25rem;
}

.markdown-content :deep(input[type="checkbox"]) {
  margin-right: 0.5rem;
}

.markdown-content :deep(blockquote) {
  margin: 1rem 0;
  padding: 0.5rem 1rem;
  border-left: 4px solid rgba(var(--v-theme-on-surface), 0.2);
  background-color: rgba(var(--v-theme-on-surface), 0.04);
  color: rgba(var(--v-theme-on-surface), 0.7);
  font-style: italic;
}

.markdown-content :deep(blockquote p:last-child) {
  margin-bottom: 0;
}

.markdown-content :deep(table) {
  border-collapse: collapse;
  margin: 1rem 0;
  width: 100%;
  display: table;
  table-layout: auto;
}

.markdown-content :deep(thead) {
  background-color: rgba(var(--v-theme-on-surface), 0.06);
}

.markdown-content :deep(th),
.markdown-content :deep(td) {
  border: 1px solid rgba(var(--v-theme-on-surface), 0.2);
  padding: 0.5rem 0.75rem;
  text-align: left;
}

.markdown-content :deep(th) {
  font-weight: 600;
  background-color: rgba(var(--v-theme-on-surface), 0.06);
}

.markdown-content :deep(tr:nth-child(even)) {
  background-color: rgba(var(--v-theme-on-surface), 0.04);
}

.markdown-content :deep(a) {
  color: rgb(var(--v-theme-primary));
  text-decoration: none;
}

.markdown-content :deep(a:hover) {
  text-decoration: underline;
}

.markdown-content :deep(a:visited) {
  color: rgb(var(--v-theme-primary));
}

.markdown-content :deep(img) {
  max-width: 100%;
  height: auto;
  border-radius: 4px;
  margin: 1rem 0;
  display: block;
}

.markdown-content :deep(hr) {
  border: none;
  border-top: 1px solid rgba(var(--v-theme-on-surface), 0.12);
  margin: 1.5rem 0;
}

.assistant-message-content.markdown-content :deep(strong),
.assistant-message-content.markdown-content :deep(b),
.markdown-content :deep(strong),
.markdown-content :deep(b) {
  font-weight: 700 !important;
  color: rgb(var(--v-theme-on-surface)) !important;
  display: inline;
}

.markdown-content :deep(em),
.markdown-content :deep(i) {
  font-style: italic;
}

.markdown-content :deep(del),
.markdown-content :deep(s) {
  text-decoration: line-through;
  opacity: 0.7;
}
</style>
