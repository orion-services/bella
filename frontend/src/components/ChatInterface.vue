<template>
  <div class="chat-wrapper">
    <!-- Messages area -->
    <div class="messages-container" ref="chatContainer">
      <div v-if="error" class="mb-4">
        <v-alert type="error" dismissible @click:close="error = null">
          <div class="d-flex align-center">
            <span>{{ error }}</span>
            <v-spacer></v-spacer>
            <v-btn
              v-if="!conversationId"
              small
              color="error"
              text
              @click="initializeChat"
              class="ml-2"
            >
              {{ $t('chat.tryAgain') }}
            </v-btn>
          </div>
        </v-alert>
      </div>
      <div v-if="initializing" class="text-center mt-4">
        <v-progress-circular indeterminate color="primary" aria-hidden="true"></v-progress-circular>
        <div class="mt-2 text-body-2">{{ $t('chat.initializing') }}</div>
      </div>
      <div v-else ref="messagesContent">
        <div 
          v-for="(message, index) in messages" 
          :key="index" 
          :class="['message-container', message.type === 'user' ? 'user-message' : 'assistant-message', { 'message-enter': message.isNew }]"
        >
          <v-card
            v-if="message.type === 'user'"
            class="user-message-bubble pa-3"
            style="max-width: 80%;"
          >
            <div class="text-body-1">
              {{ message.content }}
            </div>
          </v-card>
          <AssistantMessage
            v-else
            :content="message.content"
            :copied="!!message.copied"
            :show-copy="canCopyMessage(message, index)"
            @copy="copyMessage(message)"
          />
        </div>
        <div v-if="isTyping" class="message-container assistant-message">
          <div class="typing-indicator">
            <span></span>
            <span></span>
            <span></span>
          </div>
        </div>
        <div ref="bottomAnchor" class="chat-bottom-anchor" aria-hidden="true"></div>
      </div>
    </div>

    <!-- Message input - Always visible at the bottom -->
    <div class="input-container">
      <v-text-field
        v-model="prompt"
        :label="$t('chat.messageLabel')"
        outlined
        dense
        hide-details
        @keyup.enter="sendMessage"
        :disabled="isLoading || initializing || !conversationId"
        class="input-field"
      ></v-text-field>
      <v-btn
        color="primary"
        icon
        :aria-label="$t('chat.send')"
        @click="sendMessage"
        :disabled="!prompt.trim() || isLoading || initializing || !conversationId"
        :loading="isLoading"
        class="send-button"
      >
        <v-icon aria-hidden="true">mdi-send</v-icon>
      </v-btn>
    </div>
  </div>
</template>

<script>
import { apiService } from '../services/api';
import { authService } from '../services/auth';
import { normalizePersistedMessages } from '../services/messageHistory';
import AssistantMessage from './AssistantMessage.vue';

export default {
  name: 'ChatInterface',
  components: {
    AssistantMessage
  },
  computed: {
    isTyping() {
      if (!this.isLoading) return false;
      const last = this.messages[this.messages.length - 1];
      return last && last.type === 'assistant' && !last.content;
    }
  },
  data() {
    return {
      prompt: '',
      messages: [],
      isLoading: false,
      initializing: true,
      error: null,
      conversationId: null,
      userId: null
    };
  },
  async mounted() {
    await this.initializeChat();
  },
  beforeUnmount() {
    this.disconnectMessagesObserver();
  },
  watch: {
    isLoading(loading) {
      if (loading) {
        this.observeMessagesContent();
      } else {
        this.disconnectMessagesObserver();
        this.scrollToBottom();
      }
    },
    '$route.params.conversationId': {
      handler(newId, oldId) {
        // Always reinitialize when conversationId changes
        // This includes when changing from an ID to undefined (new conversation)
        if (newId !== oldId) {
          this.initializeChat();
        }
      },
      immediate: false
    },
    '$route.fullPath': {
      handler(newPath, oldPath) {
        // If navigating to /chat without conversationId, create new conversation
        // This ensures that even when already on /chat, a new navigation forces creation
        if (newPath === '/chat' && newPath !== oldPath) {
          this.initializeChat();
        }
      },
      immediate: false
    }
  },
  methods: {
    canCopyMessage(message, index) {
      if (!message.content) return false;
      return !(this.isLoading && index === this.messages.length - 1);
    },

    async copyMessage(message) {
      try {
        await navigator.clipboard.writeText(message.content);
      } catch (error) {
        console.error('Error copying message:', error);
        this.error = this.$t('chat.copyError');
        return;
      }

      message.copied = true;
      if (message.sequence == null || !this.conversationId) {
        return;
      }

      try {
        await apiService.markMessageCopied(this.conversationId, message.sequence);
      } catch (error) {
        console.error('Error saving copied flag:', error);
        message.copied = false;
        this.error = this.$t('chat.copyError');
      }
    },

    async initializeChat() {
      try {
        this.initializing = true;
        this.error = null;
        
        const user = authService.getUser();
        if (!user) {
          console.warn('User not authenticated, redirecting to login');
          this.$router.push('/login');
          return;
        }

        // Use hash as userId (backend syncs automatically via JWT)
        // Orion Users hash is used to map with the Bella system user
        this.userId = user.id || user.hash || user.email;
        
        if (!this.userId) {
          console.error('User without valid identifier:', user);
          this.error = this.$t('chat.missingUser');
          this.initializing = false;
          setTimeout(() => {
            this.$router.push('/login');
          }, 2000);
          return;
        }
        
        console.log('Initializing chat for user:', this.userId);
        const routeConversationId = this.$route.params.conversationId;
        
        // Clear previous state when creating new conversation
        if (!routeConversationId || routeConversationId === 'undefined' || routeConversationId === 'null') {
          this.conversationId = null;
          this.messages = [];
        }
        
        // Verify if conversationId is valid (not undefined, null or empty string)
        if (routeConversationId && routeConversationId !== 'undefined' && routeConversationId !== 'null') {
          this.conversationId = routeConversationId;
          // Load message history
          await this.loadHistory();
        } else {
          // If no conversationId, create new conversation
          console.log('Creating new conversation for user:', this.userId);
          try {
            const conversation = await apiService.createConversation(this.userId, this.$t('conversations.new'));
            console.log('Conversation created:', conversation);
            
            if (conversation && conversation.id) {
              this.conversationId = conversation.id;
              // Usar replace para não adicionar ao histórico de navegação
              await this.$router.replace(`/chat/${this.conversationId}`);
            } else {
              throw new Error(this.$t('chat.missingId'));
            }
          } catch (error) {
            console.error('Error creating conversation:', error);
            const errorMessage = error.message || error.response?.data?.message || this.$t('chat.createError');
            this.error = errorMessage;
            this.initializing = false;
            // Redirect after showing error
            setTimeout(() => {
              this.$router.push('/conversations');
            }, 3000);
            return;
          }
        }

      } catch (error) {
        console.error('Error initializing chat:', error);
        this.error = error.message || this.$t('chat.initError');
      } finally {
        this.initializing = false;
        this.$nextTick(() => {
          this.scrollToBottom();
        });
      }
    },

    async loadHistory() {
      try {
        if (!this.conversationId) {
          return;
        }
        this.messages = [];
        const memory = await apiService.getMemory(this.userId, this.conversationId);
        this.messages = normalizePersistedMessages(memory?.messages);
      } catch (error) {
        console.error('Error loading history:', error);
        // Do not show fatal error, only log
        // User can continue chatting even without history
      }
    },

    scrollToBottom() {
      this.$nextTick(() => {
        requestAnimationFrame(() => {
          const anchor = this.$refs.bottomAnchor;
          const container = this.$refs.chatContainer;
          if (anchor && typeof anchor.scrollIntoView === 'function') {
            anchor.scrollIntoView({ block: 'end' });
          }
          if (container) {
            container.scrollTop = container.scrollHeight;
          }
        });
      });
    },

    observeMessagesContent() {
      this.disconnectMessagesObserver();
      this.$nextTick(() => {
        const content = this.$refs.messagesContent;
        if (!content || typeof ResizeObserver === 'undefined') {
          return;
        }
        this.contentResizeObserver = new ResizeObserver(() => {
          if (this.isLoading) {
            this.scrollToBottom();
          }
        });
        this.contentResizeObserver.observe(content);
      });
    },

    disconnectMessagesObserver() {
      if (this.contentResizeObserver) {
        this.contentResizeObserver.disconnect();
        this.contentResizeObserver = null;
      }
    },

    async sendMessage() {
      if (!this.prompt.trim() || this.isLoading || !this.conversationId) {
        if (!this.conversationId) {
          this.error = this.$t('chat.notInitialized');
        }
        return;
      }

      const userMessage = this.prompt.trim();
      this.prompt = '';
      this.error = null;
      
      // Add user message
      const userMsgIndex = this.messages.length;
      this.messages.push({
        type: 'user',
        content: userMessage,
        isNew: true,
        copied: false,
        sequence: userMsgIndex
      });

      // Remove isNew flag after animation
      this.$nextTick(() => {
        setTimeout(() => {
          if (this.messages[userMsgIndex] && this.messages[userMsgIndex].type === 'user') {
            this.$set(this.messages[userMsgIndex], 'isNew', false);
          }
        }, 300);
      });

      this.scrollToBottom();
      this.isLoading = true;

      // Add assistant message (empty initially)
      const botMessageIndex = this.messages.length;
      this.messages.push({
        type: 'assistant',
        content: '',
        isNew: true,
        copied: false,
        agent: null,
        sequence: botMessageIndex
      });

      try {
        await apiService.createChatbotStream(
          this.conversationId,
          userMessage,
          (data) => {
            // Update bot message incrementally
            if (this.messages[botMessageIndex]) {
              // apiService.createChatbotStream já remove o prefixo "data:" e
              // reagrupa linhas de um mesmo evento SSE com "\n" — este replace
              // é só uma defesa extra caso sobre algum "data:" residual.
              // Não usar trim(): chunks do stream podem terminar em espaço
              // (separador entre palavras) OU ser uma string vazia
              // representando uma quebra de linha isolada — ambos precisam
              // ser preservados, por isso não checamos truthiness aqui.
              const cleanedData = data.replace(/^data:\s*/gm, '').replace(/\r/g, '');
              if (cleanedData !== null && cleanedData !== undefined) {
                this.messages[botMessageIndex].content += cleanedData;
                // Auto-scroll while receiving data
                this.scrollToBottom();
              }
            }
          },
          (error) => {
            console.error('Stream error:', error);
            if (this.messages[botMessageIndex]) {
              this.messages[botMessageIndex].content = this.$t('chat.processError');
            }
            this.error = error.message || this.$t('chat.processConnection');
            this.isLoading = false;
          },
          () => {
            this.isLoading = false;
            // Remove isNew flag from assistant message after animation
            if (this.messages[botMessageIndex]) {
              this.$nextTick(() => {
                setTimeout(() => {
                  this.$set(this.messages[botMessageIndex], 'isNew', false);
                }, 300);
              });
            }
            this.scrollToBottom();
          }
        );
      } catch (error) {
        console.error('Error sending message:', error);
        if (this.messages[botMessageIndex]) {
          this.messages[botMessageIndex].content = this.$t('chat.sendError');
        }
        this.error = error.response?.data?.message || error.message || this.$t('chat.sendConnection');
        this.isLoading = false;
      }
    }
  }
};
</script>

<style scoped>
.chat-wrapper {
  display: flex;
  flex-direction: column;
  /* O router-view aplica height: 100% no elemento raiz e o v-main cresce com
     o conteúdo. Sem uma altura fixa, a lista não rola. */
  height: calc(100vh - var(--v-layout-top, 64px)) !important;
  height: calc(100dvh - var(--v-layout-top, 64px)) !important;
  max-height: calc(100dvh - var(--v-layout-top, 64px));
  min-height: 0 !important;
  width: 100%;
  overflow: hidden;
}

.messages-container {
  flex: 1 1 auto;
  overflow-y: auto;
  padding: 1rem;
  min-height: 0;
}

.chat-bottom-anchor {
  height: 0;
  overflow: hidden;
}

.input-container {
  flex-shrink: 0;
  display: flex;
  align-items: center;
  gap: 0.5rem;
  padding: 1rem;
  border-top: 1px solid rgba(var(--v-theme-on-surface), 0.12);
  background-color: rgb(var(--v-theme-surface));
}

.input-field {
  flex: 1;
}

.send-button {
  flex-shrink: 0;
}

.message-container {
  margin-bottom: 1.5rem;
}

.user-message {
  display: flex;
  justify-content: flex-end;
  margin-left: auto;
}

.assistant-message {
  display: flex;
  justify-content: center;
  width: 100%;
}

.user-message-bubble {
  background-color: rgba(var(--v-theme-on-surface), 0.08) !important;
  border-radius: 18px !important;
  color: rgb(var(--v-theme-on-surface)) !important;
  box-shadow: 0 1px 2px rgba(0, 0, 0, 0.1);
}

.typing-indicator {
  display: inline-flex;
  align-items: center;
  gap: 5px;
  background-color: rgba(var(--v-theme-on-surface), 0.08);
  border-radius: 18px;
  padding: 12px 18px;
  margin: 0 auto;
}

.typing-indicator span {
  width: 8px;
  height: 8px;
  border-radius: 50%;
  background-color: rgba(var(--v-theme-on-surface), 0.45);
  animation: typingBounce 1.2s infinite ease-in-out;
}

.typing-indicator span:nth-child(1) { animation-delay: 0s; }
.typing-indicator span:nth-child(2) { animation-delay: 0.2s; }
.typing-indicator span:nth-child(3) { animation-delay: 0.4s; }

@keyframes typingBounce {
  0%, 60%, 100% {
    transform: translateY(0);
    opacity: 0.5;
  }
  30% {
    transform: translateY(-6px);
    opacity: 1;
  }
}

@keyframes messageEnter {
  from {
    opacity: 0;
    transform: translateY(10px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

.message-enter {
  animation: messageEnter 0.3s ease-out;
}
</style>

