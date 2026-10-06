<template>
  <v-container class="fill-height" fluid>
    <v-row align="center" justify="center">
      <v-col cols="12" sm="8" md="6" lg="4">
        <v-card>
          <v-card-title class="text-h5 text-center pa-4">
            {{ $t('recover.title') }}
          </v-card-title>
          <v-card-text>
            <p class="mb-4">{{ $t('recover.intro') }}</p>
            <v-form ref="form" v-model="valid" lazy-validation @submit.prevent="recover">
              <v-text-field
                v-model="email"
                :rules="emailRules"
                :label="$t('auth.email')"
                required
                prepend-inner-icon="mdi-email"
                type="email"
                :disabled="success"
                @keydown.enter.prevent="recover"
              ></v-text-field>

              <v-alert v-if="error" type="error" class="mt-4">
                {{ error }}
              </v-alert>
              <v-alert v-if="success" type="success" class="mt-4">
                {{ $t('recover.success') }}
              </v-alert>

              <v-btn
                type="submit"
                :disabled="!valid || loading || success"
                :loading="loading"
                color="primary"
                block
                class="mt-4"
              >
                {{ $t('recover.submit') }}
              </v-btn>
            </v-form>
          </v-card-text>
          <v-card-actions>
            <v-spacer></v-spacer>
            <v-btn text to="/login">
              {{ $t('recover.backToLogin') }}
            </v-btn>
          </v-card-actions>
        </v-card>
      </v-col>
    </v-row>
  </v-container>
</template>

<script>
import { orionUsersService, extractOrionErrorMessage } from '../services/orionUsers';

export default {
  name: 'RecoverPassword',
  data() {
    return {
      valid: false,
      email: '',
      loading: false,
      error: null,
      success: false
    };
  },
  computed: {
    emailRules() {
      return [
        v => !!v || this.$t('validation.emailRequired'),
        v => /.+@.+\..+/.test(v) || this.$t('validation.emailInvalid')
      ];
    }
  },
  methods: {
    async recover() {
      if (this.loading || this.success) {
        return;
      }
      if (!this.$refs.form.validate()) {
        return;
      }

      this.loading = true;
      this.error = null;

      try {
        await orionUsersService.recoverPassword(this.email);
        this.success = true;
      } catch (error) {
        console.error('Error recovering password:', error);
        this.error = extractOrionErrorMessage(error) || this.$t('recover.error');
      } finally {
        this.loading = false;
      }
    }
  }
};
</script>
