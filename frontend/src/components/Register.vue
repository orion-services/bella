<template>
  <v-container class="fill-height" fluid>
    <v-row align="center" justify="center">
      <v-col cols="12" sm="8" md="6" lg="4">
        <v-card>
          <v-card-title class="text-h5 text-center pa-4">
            {{ $t('register.title') }}
          </v-card-title>
          <v-card-text>
            <v-form ref="form" v-model="valid" lazy-validation @submit.prevent="register">
              <v-text-field
                v-model="name"
                :rules="nameRules"
                :label="$t('register.name')"
                required
                prepend-inner-icon="mdi-account"
              ></v-text-field>

              <v-text-field
                v-model="email"
                :rules="emailRules"
                :label="$t('auth.email')"
                required
                prepend-inner-icon="mdi-email"
                type="email"
              ></v-text-field>

              <v-text-field
                v-model="password"
                :rules="passwordRules"
                :label="$t('auth.password')"
                required
                prepend-inner-icon="mdi-lock"
                :type="showPassword ? 'text' : 'password'"
                :hint="$t('register.passwordHint')"
                persistent-hint
              >
                <template #append-inner>
                  <v-btn
                    icon
                    variant="text"
                    size="small"
                    :aria-label="showPassword ? $t('auth.hidePassword') : $t('auth.showPassword')"
                    @click="showPassword = !showPassword"
                  >
                    <v-icon aria-hidden="true">{{ showPassword ? 'mdi-eye' : 'mdi-eye-off' }}</v-icon>
                  </v-btn>
                </template>
              </v-text-field>

              <v-text-field
                v-model="confirmPassword"
                :rules="confirmPasswordRules"
                :label="$t('register.confirmPassword')"
                required
                prepend-inner-icon="mdi-lock-check"
                :type="showPassword ? 'text' : 'password'"
                @keydown.enter.prevent="register"
              ></v-text-field>

              <v-alert v-if="error" type="error" class="mt-4">
                {{ error }}
              </v-alert>

              <v-btn
                type="submit"
                :disabled="!valid || loading"
                :loading="loading"
                color="primary"
                block
                class="mt-4"
              >
                {{ $t('register.submit') }}
              </v-btn>

              <template v-if="isGoogleEnabled">
                <v-divider class="my-4">{{ $t('auth.or') }}</v-divider>

                <v-btn
                  :disabled="loading || loadingGoogle"
                  :loading="loadingGoogle"
                  color="white"
                  variant="outlined"
                  block
                  @click="registerWithGoogle"
                >
                  <v-icon left>mdi-google</v-icon>
                  {{ $t('register.withGoogle') }}
                </v-btn>
                
                <!-- Debug message (remove in production) -->
                <v-alert v-if="!googleInitialized && isGoogleEnabled" type="info" density="compact" class="mt-2" variant="tonal">
                  <small>{{ $t('auth.googleInitializing', { status: googleScriptLoaded ? $t('auth.googleScriptLoaded') : $t('auth.googleWaitingScript') }) }}</small>
                </v-alert>
              </template>
            </v-form>
          </v-card-text>
          <v-card-actions>
            <v-spacer></v-spacer>
            <v-btn text to="/login">
              {{ $t('register.hasAccount') }}
            </v-btn>
          </v-card-actions>
        </v-card>
      </v-col>
    </v-row>
  </v-container>
</template>

<script>
import { orionUsersService, extractOrionErrorMessage } from '../services/orionUsers';
import { authService } from '../services/auth';
import { useAuthStore } from '../stores/auth';

export default {
  name: 'Register',
  data() {
    // Debug: verify if environment variable is being loaded
    const rawClientId = import.meta.env.VITE_GOOGLE_CLIENT_ID;
    const trimmedClientId = (rawClientId || '').trim();
    
    console.log('🔍 Debug Register Component:');
    console.log('  import.meta.env.VITE_GOOGLE_CLIENT_ID (raw):', rawClientId);
    console.log('  import.meta.env.VITE_GOOGLE_CLIENT_ID (trimmed):', trimmedClientId);
    console.log('  Type:', typeof rawClientId);
    console.log('  Length:', trimmedClientId.length);
    console.log('  All VITE_* variables:', Object.keys(import.meta.env).filter(k => k.startsWith('VITE_')));
    
    return {
      valid: false,
      name: '',
      email: '',
      password: '',
      confirmPassword: '',
      showPassword: false,
      loading: false,
      loadingGoogle: false,
      error: null,
      googleClientId: trimmedClientId,
      googleScriptLoaded: false,
      googleInitialized: false
    };
  },
  computed: {
    nameRules() {
      return [
        v => !!v || this.$t('validation.nameRequired'),
        v => (v && v.length >= 3) || this.$t('validation.nameMin')
      ];
    },
    emailRules() {
      return [
        v => !!v || this.$t('validation.emailRequired'),
        v => /.+@.+\..+/.test(v) || this.$t('validation.emailInvalid')
      ];
    },
    // Keep these in sync with Orion Users' server-side password policy
    // (min 8 chars, uppercase, lowercase, special char — no digit required).
    passwordRules() {
      return [
        v => !!v || this.$t('validation.passwordRequired'),
        v => !v || (v && v.length >= 8) || this.$t('validation.passwordMin'),
        v => !v || (v && /[A-Z]/.test(v)) || this.$t('validation.passwordUpper'),
        v => !v || (v && /[a-z]/.test(v)) || this.$t('validation.passwordLower'),
        v => !v || (v && /[^A-Za-z0-9]/.test(v)) || this.$t('validation.passwordSpecial')
      ];
    },
    confirmPasswordRules() {
      return [
        v => !!v || this.$t('validation.confirmRequired'),
        v => v === this.password || this.$t('validation.passwordMismatch')
      ];
    },
    isGoogleEnabled() {
      const enabled = !!this.googleClientId && this.googleClientId.length > 0;
      console.log('🔍 isGoogleEnabled:', enabled, '| googleClientId:', this.googleClientId ? `"${this.googleClientId.substring(0, 20)}..."` : '(vazio)');
      return enabled;
    }
  },
  mounted() {
    if (this.isGoogleEnabled) {
      this.waitForGoogleScript();
    } else {
      console.warn('Google Client ID not configured. Google registration disabled.');
    }
  },
  methods: {
    async register() {
      if (this.loading) {
        return;
      }
      if (!this.$refs.form.validate()) {
        return;
      }

      this.loading = true;
      this.error = null;

      try {
        // Use createAuthenticate for automatic authentication
        const response = await orionUsersService.createAndAuthenticate(
          this.name,
          this.email,
          this.password
        );

        // Check if 2FA is required
        if (response.requires2FA) {
          this.error = this.$t('register.twoFactorRequired');
          this.loading = false;
          return;
        }

        // Login bem-sucedido
        if (response.authentication && response.authentication.token) {
          const token = response.authentication.token;
          const user = response.authentication.user; // User is inside authentication
          
          // Create user object with id based on hash
          const userData = user ? {
            ...user,
            id: user.hash || user.email // Usar hash como id, ou email como fallback
          } : null;
          
          console.log('Registration successful. Token:', token ? 'received' : 'not received');
          console.log('User:', userData);
          
          // Update authService (localStorage)
          authService.setToken(token);
          if (userData) {
            authService.setUser(userData);
          }
          
          // Update authStore (Pinia) to sync with navigation guard
          const authStore = useAuthStore();
          authStore.setToken(token);
          if (userData) {
            authStore.setUser(userData);
          }
          
          // Wait a bit to ensure store was updated
          await this.$nextTick();
          
          // Redirecionar para conversas
          this.$router.push('/conversations');
        } else {
          this.error = this.$t('register.createError');
        }
      } catch (error) {
        console.error('Error registering:', error);
        this.error = extractOrionErrorMessage(error) || this.$t('register.createError');
      } finally {
        this.loading = false;
      }
    },

    waitForGoogleScript() {
      // Check if script is already loaded
      if (typeof window.google !== 'undefined' && window.google.accounts) {
        this.googleScriptLoaded = true;
        this.initializeGoogleSignIn();
        return;
      }

      // Wait for script to load (max 10 seconds)
      let attempts = 0;
      const maxAttempts = 50; // 50 tentativas x 200ms = 10 segundos
      
      const checkInterval = setInterval(() => {
        attempts++;
        
        if (typeof window.google !== 'undefined' && window.google.accounts) {
          this.googleScriptLoaded = true;
          clearInterval(checkInterval);
          this.initializeGoogleSignIn();
        } else if (attempts >= maxAttempts) {
          clearInterval(checkInterval);
          console.error('Google Identity Services did not load after 10 seconds');
          this.error = this.$t('auth.googleLoadError');
        }
      }, 200);
    },

    initializeGoogleSignIn() {
      if (!this.isGoogleEnabled) {
        console.warn('Google Client ID não configurado');
        return;
      }

      if (typeof window.google === 'undefined' || !window.google.accounts) {
        console.error('Google Identity Services não disponível');
        return;
      }

      try {
        window.google.accounts.id.initialize({
          client_id: this.googleClientId,
          callback: this.handleGoogleCredentialResponse,
          auto_select: false,
          cancel_on_tap_outside: true
        });
        this.googleInitialized = true;
        console.log('Google Sign In inicializado com sucesso');
      } catch (error) {
        console.error('Erro ao inicializar Google Sign In:', error);
        this.error = this.$t('auth.googleInitError');
      }
    },

    async registerWithGoogle() {
      if (!this.isGoogleEnabled) {
        this.error = this.$t('auth.googleClientMissing');
        return;
      }

      // If not initialized, try to initialize first
      if (!this.googleInitialized) {
        console.log('Google Sign In not initialized. Attempting to initialize...');
        if (typeof window.google === 'undefined' || !window.google.accounts) {
          // Script has not loaded yet, wait
          this.error = this.$t('auth.googleWaiting');
          this.waitForGoogleScript();
          // Try again after a delay
          setTimeout(() => {
            if (typeof window.google !== 'undefined' && window.google.accounts) {
              this.initializeGoogleSignIn();
              // Try registration again after initialization
              setTimeout(() => this.registerWithGoogle(), 500);
            }
          }, 2000);
          return;
        } else {
          // Script loaded but not initialized, initialize now
          this.initializeGoogleSignIn();
          // Wait a bit and try again
          setTimeout(() => this.registerWithGoogle(), 500);
          return;
        }
      }

      if (!this.googleScriptLoaded || typeof window.google === 'undefined' || !window.google.accounts) {
        this.error = this.$t('auth.googleNotLoaded');
        // Tentar recarregar
        this.waitForGoogleScript();
        return;
      }

      this.loadingGoogle = true;
      this.error = null;

      try {
        // Try Google One Tap first (returns idToken directly)
        window.google.accounts.id.prompt((notification) => {
          if (notification.isNotDisplayed()) {
            // One Tap not available, use rendered button
            this.renderGoogleButton();
          } else if (notification.isSkippedMoment() || notification.isDismissedMoment()) {
            // User dismissed One Tap, use rendered button
            this.renderGoogleButton();
          }
        });
      } catch (error) {
        console.error('Error trying Google One Tap:', error);
        // Fallback to rendered button
        this.renderGoogleButton();
      }
    },

    renderGoogleButton() {
      // Create container for Google button
      const buttonContainer = document.createElement('div');
      buttonContainer.id = 'google-signin-button';
      buttonContainer.style.position = 'fixed';
      buttonContainer.style.top = '50%';
      buttonContainer.style.left = '50%';
      buttonContainer.style.transform = 'translate(-50%, -50%)';
      buttonContainer.style.zIndex = '9999';
      document.body.appendChild(buttonContainer);

      try {
        window.google.accounts.id.renderButton(
          buttonContainer,
          {
            type: 'standard',
            theme: 'outline',
            size: 'large',
            text: 'signin_with',
            shape: 'rectangular',
            logo_alignment: 'left',
            width: '300'
          }
        );

        // The handleGoogleCredentialResponse callback will be called automatically
        // when the user clicks the button and authenticates
      } catch (error) {
        console.error('Error rendering Google button:', error);
        this.error = this.$t('auth.googleButtonError');
        this.loadingGoogle = false;
        if (buttonContainer.parentNode) {
          buttonContainer.parentNode.removeChild(buttonContainer);
        }
      }
    },

    async handleGoogleCredentialResponse(response) {
      // Remove Google button if it exists
      const buttonContainer = document.getElementById('google-signin-button');
      if (buttonContainer && buttonContainer.parentNode) {
        buttonContainer.parentNode.removeChild(buttonContainer);
      }

      if (response.credential) {
        await this.processGoogleLogin(response.credential);
      } else if (response.error) {
        this.error = this.$t('auth.googleAuthError', { error: response.error });
        this.loadingGoogle = false;
      }
    },

    async processGoogleLogin(token) {
      // Remove Google button if it exists
      const buttonContainer = document.getElementById('google-signin-button');
      if (buttonContainer && buttonContainer.parentNode) {
        buttonContainer.parentNode.removeChild(buttonContainer);
      }

      try {
        const response = await orionUsersService.loginWithGoogle(token);

        // Check if 2FA is required
        if (response.requires2FA) {
          this.error = this.$t('register.twoFactorRequired');
          this.loadingGoogle = false;
          return;
        }

        // Login successful (social login creates user automatically if not exists)
        if (response.authentication && response.authentication.token) {
          const jwtToken = response.authentication.token;
          const user = response.authentication.user; // User is inside authentication
          
          // Create user object with id based on hash
          const userData = user ? {
            ...user,
            id: user.hash || user.email
          } : null;
          
          console.log('Google registration/login successful. Token:', jwtToken ? 'received' : 'not received');
          console.log('User:', userData);
          
          // Update authService (localStorage)
          authService.setToken(jwtToken);
          if (userData) {
            authService.setUser(userData);
          }
          
          // Atualizar authStore (Pinia)
          const authStore = useAuthStore();
          authStore.setToken(jwtToken);
          if (userData) {
            authStore.setUser(userData);
          }
          
          await this.$nextTick();
          
          // Redirecionar para conversas
          this.$router.push('/conversations');
        } else {
          this.error = this.$t('register.googleError');
        }
      } catch (error) {
        console.error('Error registering/signing in with Google:', error);
        this.error = extractOrionErrorMessage(error) || this.$t('register.googleError');
      } finally {
        this.loadingGoogle = false;
      }
    }
  }
};
</script>

