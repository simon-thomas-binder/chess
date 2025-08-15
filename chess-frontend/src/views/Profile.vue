<template>
  <div class="container-xxl py-4">
    <div class="row g-4">
      <div class="col-12 col-lg-4">
        <div class="card--glass p-3">
          <h5 class="fw-semibold mb-3">Profilbild</h5>
          <div class="avatar-box mb-3">
            <img :src="avatar || '/assets/default-avatar.png'" class="avatar-img" alt="Avatar" />
          </div>
          <input type="file" class="form-control mb-2" @change="onPickAvatar" accept="image/*" />
          <button class="btn btn-accent w-100" @click="saveAvatar" :disabled="savingAvatar">Speichern</button>
        </div>
      </div>

      <div class="col-12 col-lg-8">
        <div class="card--glass p-3 mb-4">
          <h5 class="fw-semibold mb-3">Profil</h5>
          <form @submit.prevent="saveProfile">
            <div class="mb-3">
              <label class="form-label">Anzeigename</label>
              <input v-model.trim="displayName" class="form-control" maxlength="64" />
            </div>
            <button class="btn btn-accent" :disabled="savingProfile">Änderungen speichern</button>
          </form>
        </div>

        <div class="card--glass p-3">
          <h5 class="fw-semibold mb-3">Passwort ändern</h5>
          <form @submit.prevent="savePassword">
            <div class="row g-3">
              <div class="col-12 col-md-6">
                <label class="form-label">Neues Passwort</label>
                <input v-model="pw1" type="password" class="form-control" minlength="8" maxlength="128" />
              </div>
              <div class="col-12 col-md-6">
                <label class="form-label">Bestätigen</label>
                <input v-model="pw2" type="password" class="form-control" minlength="8" maxlength="128" />
              </div>
            </div>
            <button class="btn btn-accent mt-3" :disabled="savingPw">Passwort speichern</button>
          </form>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref } from "vue";
import { toast } from "../composables/toast";

const avatar = ref<string>("");
const displayName = ref("Spieler123");
const pw1 = ref(""); const pw2 = ref("");

const savingAvatar = ref(false);
const savingProfile = ref(false);
const savingPw = ref(false);

function onPickAvatar(e: Event) {
  const f = (e.target as HTMLInputElement).files?.[0];
  if (!f) return;
  const r = new FileReader();
  r.onload = () => avatar.value = String(r.result);
  r.readAsDataURL(f);
}

async function saveAvatar() {
  savingAvatar.value = true;
  try {
    // await api.post("/user/avatar", { dataUrl: avatar.value })
    toast.success("Avatar gespeichert.");
  } catch (e: any) {
    toast.error(e?.response?.data?.message || "Speichern fehlgeschlagen.");
  } finally {
    savingAvatar.value = false;
  }
}

async function saveProfile() {
  savingProfile.value = true;
  try {
    // await api.post("/user/profile", { displayName: displayName.value })
    toast.success("Profil gespeichert.");
  } catch (e: any) {
    toast.error(e?.response?.data?.message || "Speichern fehlgeschlagen.");
  } finally {
    savingProfile.value = false;
  }
}

async function savePassword() {
  if (pw1.value.length < 8 || pw1.value !== pw2.value) {
    toast.error("Passwörter ungültig oder ungleich.");
    return;
  }
  savingPw.value = true;
  try {
    // await api.post("/user/password", { password: pw1.value })
    toast.success("Passwort geändert.");
    pw1.value = pw2.value = "";
  } catch (e: any) {
    toast.error(e?.response?.data?.message || "Änderung fehlgeschlagen.");
  } finally {
    savingPw.value = false;
  }
}
</script>

<style scoped>
.avatar-box { width: 100%; aspect-ratio: 16/9; background: #2a2d33; display: grid; place-items: center; overflow: hidden; }
.avatar-img { width: 100%; height: 100%; object-fit: cover; }
</style>
