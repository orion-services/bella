import { createApp } from 'vue';
import { createPinia } from 'pinia';
import { createVuetify } from 'vuetify';
import { pt } from 'vuetify/locale';
import { createRouter, createWebHistory } from 'vue-router';
import 'vuetify/styles';
import '@mdi/font/css/materialdesignicons.css';
import './style.css';

import App from './App.vue';
import { useAuthStore } from './stores/auth';
import router from './router';

function initialTheme() {
  const saved = localStorage.getItem('twr-theme');
  if (saved === 'light' || saved === 'dark') {
    return saved;
  }
  if (window.matchMedia && window.matchMedia('(prefers-color-scheme: dark)').matches) {
    return 'dark';
  }
  return 'light';
}

const brandColors = {
  primary: '#6A3D09',
  'on-primary': '#FFFFFF',
  secondary: '#A56A2A',
  'on-secondary': '#FFFFFF'
};

// Configurar Vuetify. O html é pt-BR; o locale do Vuetify alinha rótulos internos (limpar, fechar).
const vuetify = createVuetify({
  locale: {
    locale: 'pt',
    fallback: 'en',
    messages: { pt }
  },
  theme: {
    defaultTheme: initialTheme(),
    themes: {
      light: { colors: brandColors },
      dark: {
        colors: {
          ...brandColors,
          // No fundo escuro o marrom puro some; este tom é o mesmo matiz, mais claro.
          primary: '#C4843A',
          'on-primary': '#1A1208',
          secondary: '#E2B070',
          'on-secondary': '#1A1208'
        }
      }
    }
  }
});

// Criar Pinia
const pinia = createPinia();

// Guard de autenticação
router.beforeEach((to, from, next) => {
  const authStore = useAuthStore();
  
  // Verificar se a rota requer autenticação
  if (to.meta.requiresAuth) {
    // Verificar token no localStorage também (pode estar mais atualizado)
    const token = localStorage.getItem('jwt_token');
    const hasToken = !!token;
    
    // Se não há token e store não está autenticado, redirecionar para login
    if (!hasToken && !authStore.isAuthenticated) {
      console.log('Acesso negado: não autenticado, redirecionando para login');
      next('/login');
    } else {
      // Sincronizar store se necessário
      if (hasToken && !authStore.isAuthenticated) {
        authStore.setToken(token);
        const userData = localStorage.getItem('user_data');
        if (userData) {
          try {
            authStore.setUser(JSON.parse(userData));
          } catch (e) {
            console.error('Erro ao parsear user_data:', e);
          }
        }
      }
      next();
    }
  } else {
    // Rota pública, permitir acesso
    next();
  }
});

// Criar e montar aplicação
const app = createApp(App);
app.use(pinia);
app.use(router);
app.use(vuetify);

app.mount('#app');

