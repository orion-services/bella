<template>
  <v-container>
    <v-row>
      <v-col cols="12">
        <v-card>
          <v-card-title>{{ $t('twoFactor.setupTitle') }}</v-card-title>
          <v-card-text>
            <v-alert v-if="message" :type="messageType" class="mb-4">
              {{ message }}
            </v-alert>

            <!-- If 2FA is not enabled -->
            <div v-if="!twoFAEnabled">
              <p class="mb-4">{{ $t('twoFactor.intro') }}</p>
              <ol>
                <li>{{ $t('twoFactor.stepEmail') }}</li>
                <li>{{ $t('twoFactor.stepScan') }}</li>
                <li>{{ $t('twoFactor.stepCode') }}</li>
              </ol>

              <v-form ref="qrForm" v-model="qrFormValid" class="mt-4">
                <v-text-field
                  v-model="qrEmail"
                  :label="$t('auth.email')"
                  required
                  prepend-inner-icon="mdi-email"
                  :rules="emailRules"
                ></v-text-field>

                <v-text-field
                  v-model="qrPassword"
                  :label="$t('auth.password')"
                  required
                  prepend-inner-icon="mdi-lock"
                  :type="showPassword ? 'text' : 'password'"
                  :rules="passwordRules"
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

                <v-btn
                  :disabled="!qrFormValid || loadingQR"
                  :loading="loadingQR"
                  color="primary"
                  @click="generateQRCode"
                >
                  {{ $t('twoFactor.generateQr') }}
                </v-btn>
              </v-form>

              <!-- Display QR Code -->
              <div v-if="qrCodeUrl" class="mt-4 text-center">
                <p class="mb-2">{{ $t('twoFactor.scan') }}</p>
                <img :src="qrCodeUrl" :alt="$t('twoFactor.qrAlt')" style="max-width: 300px;" />
                
                <v-form ref="validateForm" v-model="validateFormValid" class="mt-4">
                  <v-text-field
                    v-model="validationCode"
                    :label="$t('twoFactor.sixDigit')"
                    required
                    prepend-inner-icon="mdi-shield-lock"
                    maxlength="6"
                    :rules="codeRules"
                  ></v-text-field>

                  <v-btn
                    :disabled="!validateFormValid || loadingValidate"
                    :loading="loadingValidate"
                    color="success"
                    @click="validateCode"
                  >
                    {{ $t('twoFactor.validateEnable') }}
                  </v-btn>
                </v-form>
              </div>
            </div>

            <!-- If 2FA is enabled -->
            <div v-else>
              <v-alert type="success" class="mb-4">
                {{ $t('twoFactor.enabled') }}
              </v-alert>

              <v-form ref="settingsForm" v-model="settingsFormValid">
                <v-checkbox
                  v-model="require2FAForBasicLogin"
                  :label="$t('twoFactor.requireBasic')"
                ></v-checkbox>

                <v-checkbox
                  v-model="require2FAForSocialLogin"
                  :label="$t('twoFactor.requireSocial')"
                ></v-checkbox>

                <v-btn
                  :disabled="!settingsFormValid || loadingSettings"
                  :loading="loadingSettings"
                  color="primary"
                  @click="updateSettings"
                >
                  {{ $t('twoFactor.save') }}
                </v-btn>
              </v-form>
            </div>
          </v-card-text>
        </v-card>
      </v-col>
    </v-row>
  </v-container>
</template>

<script>
import { orionUsersService, extractOrionErrorMessage } from '../services/orionUsers';
import { authService } from '../services/auth';

export default {
  name: 'TwoFactorSettings',
  data() {
    return {
      twoFAEnabled: false, // TODO: Check actual 2FA status
      qrFormValid: false,
      validateFormValid: false,
      settingsFormValid: true,
      qrEmail: '',
      qrPassword: '',
      showPassword: false,
      qrCodeUrl: null,
      validationCode: '',
      require2FAForBasicLogin: false,
      require2FAForSocialLogin: false,
      loadingQR: false,
      loadingValidate: false,
      loadingSettings: false,
      message: null,
      messageType: 'info'
    };
  },
  computed: {
    emailRules() {
      return [
        v => !!v || this.$t('validation.emailRequired'),
        v => /.+@.+\..+/.test(v) || this.$t('validation.emailInvalid')
      ];
    },
    passwordRules() {
      return [
        v => !!v || this.$t('validation.passwordRequired')
      ];
    },
    codeRules() {
      return [
        v => !!v || this.$t('validation.codeRequired'),
        v => (v && v.length === 6) || this.$t('validation.codeLength')
      ];
    }
  },
  methods: {
    async generateQRCode() {
      if (!this.$refs.qrForm.validate()) {
        return;
      }

      this.loadingQR = true;
      this.message = null;

      try {
        const blob = await orionUsersService.getQRCode(this.qrEmail, this.qrPassword);
        this.qrCodeUrl = URL.createObjectURL(blob);
        this.message = this.$t('twoFactor.qrSuccess');
        this.messageType = 'success';
      } catch (error) {
        console.error('Error generating QR code:', error);
        this.message = extractOrionErrorMessage(error) || this.$t('twoFactor.qrError');
        this.messageType = 'error';
      } finally {
        this.loadingQR = false;
      }
    },

    async validateCode() {
      if (!this.$refs.validateForm.validate()) {
        return;
      }

      this.loadingValidate = true;
      this.message = null;

      try {
        const response = await orionUsersService.validate2FA(
          this.qrEmail,
          this.qrPassword,
          this.validationCode
        );

        if (response.authentication && response.authentication.token) {
          // 2FA enabled successfully
          this.twoFAEnabled = true;
          this.message = this.$t('twoFactor.enabledSuccess');
          this.messageType = 'success';
          this.qrCodeUrl = null;
          this.validationCode = '';
        } else {
          this.message = this.$t('twoFactor.invalidCode');
          this.messageType = 'error';
        }
      } catch (error) {
        console.error('Error validating code:', error);
        this.message = extractOrionErrorMessage(error) || this.$t('twoFactor.invalidCode');
        this.messageType = 'error';
      } finally {
        this.loadingValidate = false;
      }
    },

    async updateSettings() {
      this.loadingSettings = true;
      this.message = null;

      try {
        const token = authService.getToken();
        const user = authService.getUser();
        const email = user?.email || this.qrEmail;

        await orionUsersService.update2FASettings(
          email,
          this.require2FAForBasicLogin,
          this.require2FAForSocialLogin,
          token
        );

        this.message = this.$t('twoFactor.settingsSaved');
        this.messageType = 'success';
      } catch (error) {
        console.error('Error updating settings:', error);
        this.message = extractOrionErrorMessage(error) || this.$t('twoFactor.settingsError');
        this.messageType = 'error';
      } finally {
        this.loadingSettings = false;
      }
    }
  }
};
</script>

