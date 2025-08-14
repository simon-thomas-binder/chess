<!-- src/views/Login.vue -->
<template>
  <div class="page-center">
    <div class="container" style="max-width: 420px;">
      <div class="p-4 card--glass">
        <h2 class="mb-3 fw-semibold">Willkommen zurück</h2>
        <p class="text-secondary mb-4">Melde dich an, um loszulegen.</p>

        <form novalidate @submit.prevent="onSubmit">
          <div class="mb-3">
            <label class="form-label">Username</label>
            <input
                v-model.trim="username"
                @blur="touched.username = true"
                :class="['form-control', invalid.username && 'is-invalid']"
                type="text"
                placeholder="dein_username"
                required
                maxlength="64"
                autocomplete="username"
            />
            <div class="invalid-feedback" v-if="invalid.username">
              Bitte gib einen gültigen Username (min. 3 Zeichen) ein.
            </div>
          </div>

          <div class="mb-4">
            <label class="form-label">Passwort</label>
            <input
                v-model="password"
                @blur="touched.password = true"
                :class="['form-control', invalid.password && 'is-invalid']"
                type="password"
                placeholder="••••••••"
                required
                minlength="8"
                maxlength="24"
                autocomplete="current-password"
            />
            <div class="invalid-feedback" v-if="invalid.password">
              Bitte gib ein Passwort mit mindestens 8 Zeichen ein.
            </div>
          </div>

          <div class="d-flex gap-2">
            <button class="btn btn-accent w-100" :disabled="loading">Login</button>
            <router-link class="btn btn-muted w-100" :to="{ name: 'Register' }">Registrieren</router-link>
          </div>
        </form>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed } from "vue";
import { login } from "../services/authService";
import { toast } from "../composables/toast";
import { useRouter, useRoute } from "vue-router";

const router = useRouter();
const route = useRoute();

const username = ref("");
const password = ref("");
const loading = ref(false);
const touched = ref({ username: false, password: false });

const invalid = computed(() => ({
  username: touched.value.username && (username.value.length < 3 || username.value.length > 64),
  password: touched.value.password && (password.value.length < 8 || password.value.length > 128),
}));

async function onSubmit() {
  touched.value.username = true;
  touched.value.password = true;
  if (invalid.value.username || invalid.value.password) return;

  loading.value = true;
  try {
    const { token } = await login({ username: username.value, password: password.value });
    localStorage.setItem("token", token);
    toast.success("Willkommen!");
    const next = (route.query.next as string) || "/lobby";
    router.push(next);
  } catch (e: any) {
    const msg = e?.response?.data?.message || "Login fehlgeschlagen.";
    toast.error(msg);
  } finally {
    loading.value = false;
  }
}
</script>
