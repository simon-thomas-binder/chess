<template>
  <div class="container-xxl py-4">
    <div class="row g-4">
      <div class="col-12 col-lg-5">
        <div class="card--glass p-3 h-100">
          <h5 class="fw-semibold mb-3">Freunde</h5>
          <div class="input-group mb-3">
            <input class="form-control" v-model="q" placeholder="Suche nach Name…" />
            <button class="btn btn-muted" @click="q = ''">Leeren</button>
          </div>

          <div v-if="filtered.length" class="list-group list-group-flush">
            <div
                v-for="f in filtered"
                :key="f.id"
                class="list-group-item bg-transparent d-flex justify-content-between align-items-center"
            >
              <div>
                <div class="fw-semibold">{{ f.name }}</div>
                <div class="small" :class="f.online ? 'text-success' : 'text-secondary'">
                  ● {{ f.online ? 'online' : 'offline' }}
                </div>
              </div>
              <div class="d-flex gap-2">
                <button class="btn btn-sm btn-accent" @click="inviteFriend(f)">Einladen</button>
                <button class="btn btn-sm btn-muted" @click="removeFriend(f)">Entfernen</button>
              </div>
            </div>
          </div>
          <div v-else class="text-secondary text-center py-4">
            Keine Treffer.
          </div>
        </div>
      </div>

      <div class="col-12 col-lg-7">
        <div class="card--glass p-3 mb-4">
          <h5 class="fw-semibold mb-2">Freund hinzufügen</h5>
          <div class="mb-2 small text-secondary">
            Sende deinen persönlichen Link oder füge den Link eines Freundes ein.
          </div>
          <div class="input-group mb-2">
            <input class="form-control" :value="myLink" readonly />
            <button class="btn btn-accent" @click="copy(myLink)">Kopieren</button>
          </div>
          <form class="input-group" @submit.prevent="addByLink">
            <input class="form-control" v-model.trim="friendLink" placeholder="Freundeslink einfügen…" />
            <button class="btn btn-accent">Hinzufügen</button>
          </form>
        </div>

        <div class="card--glass p-3">
          <h5 class="fw-semibold mb-2">Offene Einladungen</h5>
          <div v-if="invites.length" class="list-group list-group-flush">
            <div
                v-for="i in invites"
                :key="i.id"
                class="list-group-item bg-transparent d-flex justify-content-between align-items-center"
            >
              <div>{{ i.from }} → {{ i.to }}</div>
              <div class="d-flex gap-2">
                <button class="btn btn-sm btn-accent" @click="accept(i)">Annehmen</button>
                <button class="btn btn-sm btn-muted" @click="decline(i)">Ablehnen</button>
              </div>
            </div>
          </div>
          <div v-else class="text-secondary">Keine offenen Einladungen.</div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { computed, ref } from "vue";
import { toast } from "../composables/toast";

const friends = ref([
  { id: 1, name: "Max", online: true },
  { id: 2, name: "Lena", online: false },
  { id: 3, name: "Chris", online: true },
]);
const invites = ref<{ id: number; from: string; to: string }[]>([]);
const q = ref("");
const friendLink = ref("");
const myLink = `${location.origin}/friends/add?ref=mein-username`;

const filtered = computed(() =>
    friends.value.filter(f => f.name.toLowerCase().includes(q.value.toLowerCase()))
);

function inviteFriend(f: any) {
  toast.success(`Einladung an ${f.name} gesendet`);
}
function removeFriend(f: any) {
  friends.value = friends.value.filter(x => x.id !== f.id);
  toast.info(`${f.name} entfernt`);
}
function copy(s: string) {
  navigator.clipboard.writeText(s);
  toast.success("Link kopiert.");
}
function addByLink() {
  if (!friendLink.value) return;
  toast.success("Freund hinzugefügt (Demo).");
  friendLink.value = "";
}
function accept(i: any) {
  toast.success("Einladung angenommen.");
  invites.value = invites.value.filter(x => x.id !== i.id);
}
function decline(i: any) {
  toast.info("Einladung abgelehnt.");
  invites.value = invites.value.filter(x => x.id !== i.id);
}
</script>
