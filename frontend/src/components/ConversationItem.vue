<template>
  <v-list-item @click="$emit('select', conversation.id)">
    <v-text-field
      v-if="editing"
      ref="titleField"
      v-model="newTitle"
      density="compact"
      variant="underlined"
      hide-details
      single-line
      autofocus
      :disabled="renaming"
      @click.stop
      @keyup.enter="submitRename"
      @keyup.esc="cancelRename"
      @blur="submitRename"
    />
    <v-list-item-title v-else>{{ conversation.title }}</v-list-item-title>
    <v-list-item-subtitle>
      Created at: {{ formatDate(conversation.startedAt) }}
      <span v-if="conversation.lastActivity">
        | Last activity: {{ formatDate(conversation.lastActivity) }}
      </span>
    </v-list-item-subtitle>

    <template #append>
      <div @click.stop>
        <v-btn
          icon
          variant="text"
          :aria-label="editing ? 'Save title' : 'Rename conversation'"
          :loading="renaming"
          @mousedown.prevent
          @click="editing ? submitRename() : openRename()"
        >
          <v-icon>{{ editing ? 'mdi-check' : 'mdi-pencil' }}</v-icon>
        </v-btn>
        <v-btn
          icon
          variant="text"
          aria-label="Delete conversation"
          @click="confirmDelete"
        >
          <v-icon>mdi-delete</v-icon>
        </v-btn>
      </div>
    </template>
  </v-list-item>
  <v-divider></v-divider>
</template>

<script>
import { apiService } from '../services/api';

export default {
  name: 'ConversationItem',
  props: {
    conversation: {
      type: Object,
      required: true
    }
  },
  emits: ['select', 'delete', 'renamed'],
  data() {
    return {
      editing: false,
      newTitle: '',
      renaming: false
    };
  },
  methods: {
    formatDate(dateString) {
      if (!dateString) return '';
      const date = new Date(dateString);
      if (Number.isNaN(date.getTime())) return '';
      return date.toLocaleDateString('en-US', {
        day: '2-digit',
        month: '2-digit',
        year: 'numeric',
        hour: '2-digit',
        minute: '2-digit'
      });
    },

    openRename() {
      this.newTitle = this.conversation.title || '';
      this.editing = true;
      this.$nextTick(() => {
        this.$refs.titleField?.focus?.();
      });
    },

    cancelRename() {
      this.editing = false;
      this.newTitle = this.conversation.title || '';
    },

    async submitRename() {
      if (!this.editing || this.renaming) {
        return;
      }
      const title = (this.newTitle || '').trim();
      if (!title || title === (this.conversation.title || '').trim()) {
        this.cancelRename();
        return;
      }
      this.renaming = true;
      try {
        await apiService.updateConversationTitle(this.conversation.id, title);
        this.editing = false;
        this.$emit('renamed');
      } catch (e) {
        console.error('Error renaming conversation:', e);
        alert(e.response?.data?.message || e.message || 'Could not rename conversation');
      } finally {
        this.renaming = false;
      }
    },

    confirmDelete() {
      if (confirm('Are you sure you want to delete this conversation?')) {
        this.$emit('delete', this.conversation.id);
      }
    }
  }
};
</script>

