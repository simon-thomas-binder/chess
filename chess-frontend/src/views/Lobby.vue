<template>
  <div class="lobby-wrapper">
    <div class="lobby container-xxl py-4 lobby-centered">
      <div class="row g-4 align-items-stretch">

        <!-- LINKS: Profil & Quick Actions -->
        <div class="col-12 col-lg-3">
          <div class="card--glass p-3 h-100 d-flex flex-column">
            <div class="avatar-box mb-3">
              <img
                  :src="'/assets/default-avatar.png'"
                  alt="Profilbild"
                  class="avatar-img"
              />
            </div>
            <h4 class="fw-semibold mb-1">{{ user?.displayname }}</h4>
            <div class="text-secondary mb-3">ELO: {{ user?.rating }}</div>

            <div class="d-grid gap-2">
              <button class="btn btn-accent" @click="queueQuickGame" :disabled="loading.quick">
                🔍 Schnelles Spiel
              </button>
              <router-link class="btn btn-muted" :to="{ name: 'CustomGame' }">
                ⚙️ Eigenes Spiel
              </router-link>
              <router-link class="btn btn-muted" :to="{ name: 'Profile' }">
                🧩 Profil & Einstellungen
              </router-link>
              <router-link class="btn btn-muted" :to="{ name: 'Friends' }">
                👥 Freunde verwalten
              </router-link>
            </div>

            <div class="mt-4 small text-secondary">
              <div>Hallo, <span class="text-light">{{ user?.displayname }}</span> 👋</div>
              <div>Bereit für ein Match? Wähle unten ein Preset oder erstelle ein eigenes Spiel.</div>
            </div>
          </div>
        </div>

        <!-- MITTE: Presets + Letzte Spiele (Hauptfokus) -->
        <div class="col-12 col-lg-6">
          <div class="d-flex flex-column gap-4 h-100">

            <!-- Presets / Matchmaking -->
            <div class="card--glass p-3">
              <div class="d-flex justify-content-between align-items-center mb-2">
                <h5 class="fw-semibold m-0">Schnelles Matchmaking</h5>
                <div class="text-secondary small">Klick auf ein Preset, um zu queuen</div>
              </div>

              <div class="row g-3">
                <div
                    v-for="p in presets"
                    :key="p.id"
                    class="col-12 col-sm-6 col-xl-4"
                >
                  <button
                      class="preset-tile w-100"
                      :disabled="loading.queueId === p.id"
                      @click="queuePreset(p)"
                  >
                    <div class="preset-time">{{ p.label }}</div>
                    <div class="preset-sub text-secondary">
                      Board: {{ p.board.width }}×{{ p.board.height }}
                    </div>
                    <div v-if="loading.queueId === p.id" class="preset-loading text-secondary mt-1">
                      Warteschlange …
                    </div>
                  </button>
                </div>

                <!-- Freies „Beliebig“ -->
                <div class="col-12">
                  <button class="btn btn-muted w-100" @click="queueAny" :disabled="loading.any">
                    🎲 Beliebig (System wählt Modus)
                  </button>
                </div>
              </div>
            </div>

            <!-- Letzte Spiele -->
            <div class="card--glass p-3 flex-grow-1 d-flex flex-column">
              <h5 class="fw-semibold mb-3">Zuletzt gespielt</h5>

              <div v-if="recentGames.length" class="list-group list-group-flush flex-grow-1">
                <div
                    v-for="g in recentGames"
                    :key="g.id"
                    class="list-group-item bg-transparent text-light d-flex justify-content-between align-items-center"
                >
                  <div>
                    <div class="fw-semibold">{{ g.opponent }}</div>
                    <div class="small text-secondary">
                      {{ g.date }} • {{ g.time }} • {{ g.result }}
                    </div>
                  </div>
                  <router-link class="btn btn-sm btn-muted" :to="`/game/${g.id}`">
                    ▶ Replay
                  </router-link>
                </div>
              </div>

              <div v-else class="text-secondary text-center py-4">
                Noch keine Spiele – starte eines über die Presets!
              </div>
            </div>
          </div>
        </div>

        <!-- RECHTS: Freunde + kleine Infos -->
        <div class="col-12 col-lg-3">
          <div class="card--glass p-3 h-100 d-flex flex-column">
            <div class="d-flex justify-content-between align-items-center mb-3">
              <h5 class="fw-semibold m-0">Freunde</h5>
              <router-link class="small text-secondary" :to="{ name: 'Friends' }">verwalten →</router-link>
            </div>

            <div v-if="friends.length" class="list-group list-group-flush flex-grow-1">
              <div
                  v-for="f in friends"
                  :key="f.id"
                  class="list-group-item bg-transparent d-flex justify-content-between align-items-center"
              >
                <div>
                  <div class="fw-semibold">{{ f.name }}</div>
                  <div class="small" :class="f.online ? 'text-success' : 'text-secondary'">
                    ● {{ f.online ? 'online' : 'offline' }}
                  </div>
                </div>
                <button class="btn btn-sm btn-accent" @click="inviteFriend(f)">
                  🎯 Einladen
                </button>
              </div>
            </div>
            <div v-else class="text-secondary text-center py-3">
              Noch keine Freunde hinzugefügt.
            </div>

            <div class="mt-3">
              <button class="btn btn-muted w-100" @click="copyFriendLink">🔗 Freundeslink kopieren</button>
            </div>

            <!-- News/Tipp füllt optisch auf -->
            <div class="mt-4 small text-secondary">
              <div class="fw-semibold text-light mb-1">Neu:</div>
              <ul class="m-0 ps-3">
                <li>Custom Boards (Beta)</li>
                <li>Einladungen in Echtzeit</li>
                <li>Dark UI Fine-Tuning</li>
              </ul>
            </div>
          </div>
        </div>
      </div>

      <!-- Einladung Pop-up -->
      <div v-if="incomingInvite" class="invite-toast card--glass p-3">
        <p class="mb-2">
          <strong>{{ incomingInvite.from }}</strong> lädt dich zu einem Spiel ein.
        </p>
        <div class="d-flex gap-2">
          <button class="btn btn-accent" @click="acceptInvite">Annehmen</button>
          <button class="btn btn-muted" @click="declineInvite">Ablehnen</button>
        </div>
      </div>

      <!-- Match gefunden Pop-up -->
      <div v-if="matchFound" class="invite-toast card--glass p-3">
        <p class="mb-2">
          Match gefunden! Du spielst <strong>{{ matchFound.color }}</strong>.
        </p>
        <div class="d-flex gap-2">
          <router-link class="btn btn-accent" :to="`/game/${matchFound.gameId}`">Zum Spiel</router-link>
          <button class="btn btn-muted" @click="matchFound = null">Später</button>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { onMounted, ref} from "vue";
import {useRouter} from "vue-router";
import api from "../api";
import {toast} from "../composables/toast";

// STOMP/SockJS
import {queueChessboard} from "../services/gameService.ts";

const router = useRouter();
const user = ref<User | null>(null);

// Presets for chessboard settings
const presets = ref([
  {
    id: "bullet-1-0",
    label: "1+0 Bullet",
    board: {width: 8, height: 8},
    initial_time: 60_000,
    increment: 0,
    delay: 0,
  },
  {
    id: "blitz-3-2",
    label: "3+2 Blitz",
    board: {width: 8, height: 8},
    initial_time: 180_000,
    increment: 2_000,
    delay: 0,
  },
  {
    id: "rapid-10-0",
    label: "10+0 Rapid",
    board: {width: 8, height: 8},
    initial_time: 600_000,
    increment: 0,
    delay: 0,
  },
]);

onMounted(async () => {
  const token = localStorage.getItem("token") || undefined;
  ws.ensureConnected(token);

  user.value = await getUser()

  ws.onUserMessage((msg) => {
    if (msg.type === "MATCH_FOUND") {
      const { gameId, color, opponents, chessboard } = msg.payload || {};
      if (gameId) {
        console.log(msg.payload)
        matchStore.setMatch({ gameId, color, opponents, chessboard });
        router.push({ name: "Game", params: { id: String(gameId) } });
      }
    }
    if (msg.type === "INVITE") {
      //TODO:
    }
  });
});


async function queuePreset(p: any) {
  loading.value.queueId = p.id;
  try {
    await api.post("/chess/queue", {
      width: p.board.width,
      height: p.board.height,
      initial_time: p.initial_time,
      increment: p.increment,
      delay: p.delay,
      pieces: initialChessboard.pieces,
    });
    toast.success(`${p.label}: Warteschlange betreten.`);
  } catch (e: any) {
    toast.error(e?.response?.data?.message || "Queue fehlgeschlagen.");
  } finally {
    loading.value.queueId = null;
  }
}













async function queueQuickGame() {
  loading.value.quick = true;
  try {
    const p = presets.value[0];
    const payload: Chessboard = {
      ...initialChessboard,
      initial_time: p.initial_time,
      increment: p.increment,
      delay: p.delay
    };
    await queueChessboard(payload);
    toast.success("Du bist in der Warteschlange.");
    console.log(payload)
  } catch (e: any) {
    toast.error(e?.response?.data?.message || "Queue fehlgeschlagen.");
  } finally {
    loading.value.quick = false;
  }
}











// ---- Dummy/User State (später über Store ersetzen)
// Freunde (kurze Liste)

const friends = ref([
  {id: 1, name: "Max", online: true},
  {id: 2, name: "Lena", online: false},
  {id: 3, name: "Chris", online: true},
]);
// Zuletzt gespielt (Demo)



const recentGames = ref([
  {id: 101, opponent: "Chris", result: "Sieg", date: "14.08.2025", time: "3+2"},
  {id: 102, opponent: "Alex", result: "Niederlage", date: "13.08.2025", time: "5+0"},
]);
// UI State
const loading = ref<{ quick?: boolean; any?: boolean; queueId?: string | null }>({
  quick: false,
  any: false,
  queueId: null,
});
const incomingInvite = ref<null | { from: string; gameId: number }>(null);

const matchFound = ref<null | { gameId: number; color: string }>(null);

async function queueAny() {
  loading.value.any = true;
  try {
    await api.post("/chess/queue", {any: true});
    toast.success("Beliebige Warteschlange betreten.");
  } catch (e: any) {
    toast.error(e?.response?.data?.message || "Queue fehlgeschlagen.");
  } finally {
    loading.value.any = false;
  }
}

function inviteFriend(f: { id: number; name: string }) {
  // später: api.post("/friends/invite", { friendId: f.id })
  toast.success(`Einladung an ${f.name} gesendet.`);
}

function copyFriendLink() {
  const link = `${location.origin}/friends/add?ref=${user?.value?.username || "user"}`;
  navigator.clipboard.writeText(link);
  toast.success("Freundeslink kopiert.");
}

function acceptInvite() {
  const g = incomingInvite.value;
  if (!g) return;
  toast.success("Einladung angenommen.");
  router.push(`/game/${g.gameId}`);
  incomingInvite.value = null;
}

function declineInvite() {
  toast.info("Einladung abgelehnt.");
  incomingInvite.value = null;
}
import { useMatchStore } from "../stores/matchStore";
import { useWs } from "../composables/ws";
import {type Chessboard, initialChessboard} from "../models/chess.ts";

import {getUser, type User} from "../services/authService.ts";
const ws = useWs();

const matchStore = useMatchStore();

</script>

<style scoped>
.lobby :deep(.list-group-item) {
  border-color: rgba(255, 255, 255, 0.06);
  color: var(--text);
}

/* Avatar als rechteckiger Kasten, kein Rahmen */
.avatar-box {
  width: 100%;
  aspect-ratio: 16/9;
  background: #2a2d33;
  display: grid;
  place-items: center;
  overflow: hidden;
}

.avatar-img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

/* Preset-Kacheln */
.preset-tile {
  border: 1px solid rgba(255, 255, 255, 0.08);
  background: #262a31;
  color: var(--text);
  border-radius: 12px;
  padding: 0.9rem 1rem;
  text-align: left;
  width: 100%;
}

.preset-tile:hover {
  filter: brightness(1.05);
}

.preset-time {
  font-weight: 600;
  line-height: 1.1;
}

.preset-sub {
  font-size: 0.9rem;
}

.preset-loading {
  font-size: 0.85rem;
}

.lobby-wrapper {
  min-height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 2rem;
  box-sizing: border-box;
}

.lobby-centered {
  width: 100%;
  max-width: 1300px;
}

/* Invite/Match Toast */
.invite-toast {
  position: fixed;
  bottom: 24px;
  right: 24px;
  max-width: 340px;
  animation: slideIn .25s ease;
  z-index: 1050;
}

@keyframes slideIn {
  from {
    transform: translateY(10px);
    opacity: 0;
  }
  to {
    transform: translateY(0);
    opacity: 1;
  }
}
</style>
