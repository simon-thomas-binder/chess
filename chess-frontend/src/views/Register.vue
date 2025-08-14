<!-- src/views/Register.vue -->
<template>
  <div class="page-center">
    <div class="container" style="max-width: 520px;">
      <div class="p-4 card--glass">
        <h2 class="mb-3 fw-semibold">Konto erstellen</h2>
        <p class="text-secondary mb-4">Schnell und einfach – danach geht’s direkt los.</p>

        <form novalidate @submit.prevent="onSubmit">
          <div class="row g-3">
            <div class="col-12 col-md-6">
              <label class="form-label">Username</label>
              <input
                  v-model.trim="username"
                  @blur="touched.username = true"
                  :class="['form-control', invalid.username && 'is-invalid']"
                  type="text"
                  placeholder="dein_username"
                  required
                  minlength="3"
                  maxlength="64"
                  autocomplete="username"
              />
              <div class="invalid-feedback" v-if="invalid.username">
                3–64 Zeichen, nur gültige Zeichen verwenden.
              </div>
            </div>

            <div class="col-12 col-md-6">
              <label class="form-label">Displayname</label>
              <input
                  v-model.trim="displayname"
                  @blur="touched.displayname = true"
                  :class="['form-control', invalid.displayname && 'is-invalid']"
                  type="text"
                  placeholder="Dein Anzeigename"
                  required
                  minlength="3"
                  maxlength="64"
                  autocomplete="nickname"
              />
              <div class="invalid-feedback" v-if="invalid.displayname">
                3–64 Zeichen.
              </div>
            </div>

            <div class="col-12">
              <label class="form-label">Passwort</label>
              <input
                  v-model="password"
                  @blur="touched.password = true"
                  :class="['form-control', invalid.password && 'is-invalid']"
                  type="password"
                  placeholder="Mind. 8 Zeichen, Buchstaben & Zahl"
                  required
                  minlength="8"
                  maxlength="24"
                  autocomplete="new-password"
              />
              <div class="invalid-feedback" v-if="invalid.password">
                - mindestens 8 bis max. 24 Zeichen <br>
                - mindestens eine Zahl (0-9) <br>
                - mindestens einen Buchstaben (a-z, A-Z)
              </div>
            </div>

            <div class="col-12">
              <label class="form-label">Passwort bestätigen</label>
              <input
                  v-model="confirmPassword"
                  @blur="touched.confirmPassword = true"
                  :class="['form-control', invalid.confirmPassword && 'is-invalid']"
                  type="password"
                  minlength="8"
                  maxlength="24"
                  placeholder="Passwort erneut eingeben"
                  required
              />
              <div class="invalid-feedback" v-if="invalid.confirmPassword">
                Die Passwörter stimmen nicht überein.
              </div>
            </div>
          </div>

          <div class="d-flex gap-2 mt-4">
            <button class="btn btn-accent w-100" :disabled="loading">Konto erstellen</button>
            <router-link class="btn btn-muted w-100" :to="{ name: 'Login' }">Schon Konto?</router-link>
          </div>
        </form>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed } from "vue";
import { register } from "../services/authService";
import { toast } from "../composables/toast";
import { useRouter } from "vue-router";

const router = useRouter();

const username = ref("");
const displayname = ref("");
const password = ref("");
const confirmPassword = ref("");
const loading = ref(false);
const touched = ref({ username: false, displayname: false, password: false, confirmPassword: false });

const pwHasLetter = (s: string) => /[A-Za-z]/.test(s);
const pwHasDigit = (s: string) => /\d/.test(s);

const invalid = computed(() => ({
  username: touched.value.username && (username.value.length < 3 || username.value.length > 64),
  displayname: touched.value.displayname && (displayname.value.length < 3 || displayname.value.length > 64),
  password: touched.value.password && !(password.value.length >= 8 && password.value.length <= 24 && pwHasLetter(password.value) && pwHasDigit(password.value)),
  confirmPassword: touched.value.confirmPassword && (confirmPassword.value !== password.value),
}));

async function onSubmit() {
  touched.value.username = true;
  touched.value.displayname = true;
  touched.value.password = true;
  touched.value.confirmPassword = true;
  if (invalid.value.username || invalid.value.displayname || invalid.value.password || invalid.value.confirmPassword) return;

  loading.value = true;
  try {
    const { token } = await register({ username: username.value, displayname: displayname.value, password: password.value });
    localStorage.setItem("token", token);
    toast.success("Konto erstellt – willkommen!");
    router.push("/lobby");
  } catch (e: any) {
    const msg = e?.response?.data?.message || "Registrierung fehlgeschlagen.";
    toast.error(msg);
  } finally {
    loading.value = false;
  }
}
</script>
