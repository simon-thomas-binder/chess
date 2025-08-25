<template>
  <div class="container-xxl py-4">
    <div class="row g-4">
      <!-- Links: Spielerinfos -->
      <div class="col-12 col-xl-2">
        <div class="card--glass p-3 text-center mb-3">
          <div class="avatar-box mb-2"><img :src="white.avatar" class="avatar-img" /></div>
          <h6 class="fw-semibold">{{ white.name }}</h6>
          <div class="timer" :class="{ active: turn === 'WHITE', low: white.time <= 10000 }">
            {{ formatTime(white.time) }}
          </div>
        </div>
        <div class="card--glass p-3 text-center">
          <div class="avatar-box mb-2"><img :src="black.avatar" class="avatar-img" /></div>
          <h6 class="fw-semibold">{{ black.name }}</h6>
          <div class="timer" :class="{ active: turn === 'BLACK', low: black.time <= 10000 }">
            {{ formatTime(black.time) }}
          </div>
        </div>
        <div class="card--glass p-3 mt-3">
          <div class="d-grid gap-2">
            <button class="btn btn-danger" @click="onResign">Aufgeben</button>
            <button class="btn btn-muted" @click="onOfferDraw">Remis anbieten</button>
          </div>
        </div>
      </div>

      <!-- Mitte: Board -->
      <div class="col-12 col-xl-8 d-flex flex-column">
        <div
            class="board card--glass p-2"
            :style="{ gridTemplateColumns: `repeat(${match?.chessboard.width}, 1fr)` }"
        >
          <div
              v-for="cell in cells"
              :key="cell.key"
              class="cell"
              :class="{ dark: cell.dark, sel: isSelected(cell), hl: isHighlighted(cell) }"
              @click="onCellClick(cell)"
          >
            <img
                v-if="cell.piece"
                :src="pieceSprite(cell.piece)"
                class="piece"
                draggable="false"
            />
          </div>
        </div>

        <!-- Move list -->
        <div class="card--glass p-3 mt-3">
          <h6 class="fw-semibold mb-2">Züge</h6>
          <div class="moves">
            <div v-for="(m,i) in moves" :key="i" class="move-row">
              <span class="idx">{{ i+1 }}.</span>
              <span class="san">{{ m.san || `${m.piece}@${m.to.row},${m.to.col}` }}</span>
            </div>
          </div>
        </div>
      </div>

      <!-- Rechts: Chat -->
      <div class="col-12 col-xl-2">
        <div class="card--glass p-3 chat">
          <h6 class="fw-semibold mb-2">Chat</h6>
          <div class="chat-messages">
            <div v-for="m in chat" :key="m.id" class="chat-line">
              <strong>{{ m.sender }}:</strong> {{ m.text }}
            </div>
          </div>
          <div class="input-group mt-2">
            <input v-model="chatInput" class="form-control" @keyup.enter="onSendChat" placeholder="Nachricht…" />
            <button class="btn btn-accent" @click="onSendChat">Senden</button>
          </div>
        </div>
      </div>

      <!-- Endscreen Overlay -->
      <div v-if="end.open" class="end-overlay" @click.self="end.open = false">
        <div class="end-card">
          <!-- Icon + Title -->
          <div class="end-head">
      <span class="end-icon" :class="endUi.iconClass" aria-hidden="true">
        <!-- simple inline SVGs -->
        <svg v-if="endUi.icon==='trophy'" width="28" height="28" viewBox="0 0 24 24" fill="currentColor"><path d="M6 2h12v3h3v3c0 3.31-2.69 6-6 6h-1v2h3v2H7v-2h3v-2h-1C5.69 14 3 11.31 3 8V5h3V2zm0 3H5v3c0 2.21 1.79 4 4 4h1.17A5.99 5.99 0 0 1 6 7V5zm13 0h-1v2a6 6 0 0 1-4.17 5H15c2.21 0 4-1.79 4-4V5z"/></svg>
        <svg v-else-if="endUi.icon==='flag'" width="28" height="28" viewBox="0 0 24 24" fill="currentColor"><path d="M4 4h2v16H4zM8 4h8l-1.5 3L20 9h-8l-1.5 3L8 8z"/></svg>
        <svg v-else-if="endUi.icon==='clock'" width="28" height="28" viewBox="0 0 24 24" fill="currentColor"><path d="M12 1a11 11 0 1 0 0 22 11 11 0 0 0 0-22zm1 6h-2v6l5 3 .997-1.733L13 12.5V7z"/></svg>
        <svg v-else-if="endUi.icon==='handshake'" width="28" height="28" viewBox="0 0 24 24" fill="currentColor"><path d="M6.5 6l3.5 3 2-2 2 2 3.5-3 3.5 3-5 5-2-2-2 2-5-5z"/></svg>
        <svg v-else width="28" height="28" viewBox="0 0 24 24" fill="currentColor"><circle cx="12" cy="12" r="10"/></svg>
      </span>
            <div class="end-titles">
              <h3 class="mb-1">{{ endUi.title }}</h3>
              <p class="text-muted m-0">{{ endUi.subtitle }}</p>
            </div>
          </div>

          <!-- Winner / Draw presentation -->
          <div v-if="endUi.hasWinner" class="winner-wrap">
            <div class="winner-avatar" :class="end.winner === 'WHITE' ? 'is-white' : 'is-black'">
              <img :src="winnerPlayer.avatar" class="avatar-img" />
              <img class="king-overlay" :src="`/pieces/${(end.winner||'white').toLowerCase()}_king.png`" alt="" />
            </div>
            <div class="winner-info">
              <div class="winner-name">{{ winnerPlayer.name }}</div>
              <div class="color-pill" :class="end.winner === 'WHITE' ? 'pill-white' : 'pill-black'">
                {{ end.winner === 'WHITE' ? 'Weiß' : 'Schwarz' }}
              </div>
            </div>
          </div>

          <div v-else class="draw-wrap">
            <div class="draw-player">
              <div class="mini-avatar is-white"><img :src="white.avatar" /></div>
              <div class="mini-name">{{ white.name }}</div>
            </div>
            <div class="draw-center">½–½</div>
            <div class="draw-player">
              <div class="mini-avatar is-black"><img :src="black.avatar" /></div>
              <div class="mini-name">{{ black.name }}</div>
            </div>
          </div>

          <div class="d-flex gap-2 mt-4 justify-content-center">
            <button class="btn btn-accent" @click="onRematch">Revanche</button>
            <button class="btn btn-muted" @click="onBack">Zurück</button>
          </div>
        </div>
      </div>

      <ConfirmModal
          v-model="confirm.open"
          :title="confirm.title"
          :message="confirm.message"
          :confirmText="confirm.confirmText"
          :cancelText="confirm.cancelText"
          :danger="confirm.danger"
          @confirm="onConfirmOk"
          @cancel="onConfirmCancel"
      />

    </div>
  </div>
</template>

<script setup lang="ts">
import { onMounted, onBeforeUnmount, reactive, ref } from "vue";
import { useRoute } from "vue-router";
import { useWs } from "../composables/ws";
import {sendChat, offerDraw, resign, getMoves, sendMove} from "../services/gameService";
import { toast } from "../composables/toast";
import type {Color, Move, Piece, Position} from "../models/chess.ts";
import { useMatchStore } from "../stores/matchStore";

// ----- Routing / IDs
const route = useRoute();
const gameId = String(route.params.id);

// ----- WS
const ws = useWs();

const matchStore = useMatchStore();
const match = ref(matchStore.current);

const cells = ref<any[]>([]);
const selected = ref<{x:number;y:number}|null>(null);
const highlights = ref<string[]>([]);
const highlightedMoves = ref<Record<string, Move>>({});
const turn = ref<Color>("WHITE");

// players & clocks (demo fallback)
const white = reactive({ name: "Weiß", avatar: "/assets/default-avatar.png", time: 300000 });
const black = reactive({ name: "Schwarz", avatar: "/assets/default-avatar.png", time: 300000 });

// move list + chat
const moves = ref<any[]>([]);
const chat = ref<{id:number; sender:string; text:string}[]>([]);
const chatInput = ref("");

// ----- helpers
function keyOf(row:number,col:number){ return `${row}:${col}`; }
function pieceAt(x:number,y:number){ return match.value?.chessboard.pieces.find(p=>p.position.x===x && p.position.y===y) || null; }

// --- Board sizing ---
const boardWrap = ref<HTMLElement | null>(null);
let resizeObs: ResizeObserver | null = null;

const end = reactive<{ open: boolean; winner: Color | null; endFlag: string }>({
  open: false,
  winner: null,
  endFlag: "",
});

let ticking = false;
let rafId = 0;
let lastTs = 0;

function startClock() {
  if (ticking) return;
  ticking = true;
  lastTs = performance.now();
  rafId = requestAnimationFrame(stepClock);
}
function stopClock() {
  ticking = false;
  cancelAnimationFrame(rafId);
}
function stepClock(ts: number) {
  const dt = ts - lastTs;
  lastTs = ts;
  if (turn.value === "WHITE") white.time = Math.max(0, white.time - dt);
  else if (turn.value === "BLACK") black.time = Math.max(0, black.time - dt);
  if (ticking && !end.open && white.time > 0 && black.time > 0) {
    rafId = requestAnimationFrame(stepClock);
  }
}

function syncClockFromWs(remaining?: Record<string, number>, nextTurn?: Color) {
  if (remaining) {
    const w = (remaining as any).white ?? (remaining as any).WHITE ?? (remaining as any).White;
    const b = (remaining as any).black ?? (remaining as any).BLACK ?? (remaining as any).Black;
    if (typeof w === "number") white.time = w;
    if (typeof b === "number") black.time = b;
  }
  if (nextTurn === "WHITE" || nextTurn === "BLACK") turn.value = nextTurn;
  stopClock();
  if (!end.open) startClock();
}

function rebuildCells() {
  const arr:any[] = [];
  if (match.value == null) {
    return
  }
  for (let r = 0; r < match.value?.chessboard.height; r++) {
    for (let c = 0; c < match.value?.chessboard.width; c++) {
      const dark = (r+c)%2===1;
      arr.push({
        key: keyOf(r, c),
        row:r, col:c, dark,
        piece: pieceAt(c, r)
      });
    }
  }
  cells.value = arr;
}

function isSelected(cell:any){ return selected.value && selected.value.x===cell.row && selected.value.y===cell.col; }

function isHighlighted(cell:any){ return highlights.value.includes(keyOf(cell.row,cell.col)); }

function clearHighlights() {
  highlights.value = [];
  highlightedMoves.value = {};
  selected.value = null;
}

function pieceSprite(p:Piece){
  return `/pieces/${p.color.toLowerCase()}_${p.type}.png`;
}

async function onCellClick(cell: any) {
  // Bereits selektiert? egal → keine Aktion
  if (isSelected(cell)) {
    return;
  }

  // Klick auf eine HIGHLIGHT-Zelle → Move spielen
  if (isHighlighted(cell)) {
    const key = keyOf(cell.row, cell.col);
    const move = highlightedMoves.value[key];
    if (!move) {
      // Fallback: sollte nicht passieren, aber wir räumen auf
      clearHighlights();
      return;
    }

    try {
      await sendMove(gameId, move);
      // UI-Aufräumen; Brett-Update kommt per WS
      clearHighlights();
    } catch (e: any) {
      toast.error(e?.response?.data?.message || "Zug konnte nicht gespielt werden");
    }
    return;
  }

  // Klick auf eigene Figur → mögliche Züge laden & highlighten
  if (cell.piece) {
    // Zelle merken (UI-Selektionsstil)
    selected.value = { x: cell.row, y: cell.col };

    const position: Position = {
      x: cell.col,
      y: (match.value?.chessboard?.height ?? 0) - 1 - cell.row,
    };

    try {
      // WICHTIG: nicht 'moves' nennen → nicht die ref überschreiben
      const availableMoves: Move[] = await getMoves(match.value?.gameId, position);

      if (Array.isArray(availableMoves) && availableMoves.length > 0) {
        // Map neu befüllen
        const map: Record<string, Move> = {};
        for (const mv of availableMoves) {
          const key = toGridKeyFromChess(mv.to);
          map[key] = mv;
        }
        highlightedMoves.value = map;
        highlights.value = Object.keys(map);
      } else {
        clearHighlights();
      }
    } catch {
      clearHighlights();
    }
  } else {
    // Klick auf leeres Feld ohne Highlight → deselektieren
    clearHighlights();
  }
}

function switchPos(num: number): number {
  return (match.value?.chessboard?.height ?? 0) - 1 - num;
}

function toGridKeyFromChess(pos: Position) {
  const row = switchPos(pos.y);
  const col = pos.x;
  return keyOf(row, col);
}

/*function toGridCoords(pos: Position) {
  return {
    row: (match.value?.chessboard?.height ?? 0) - 1 - pos.y,
    col: pos.x,
  };
}*/


  // second click -> try move via HTTP
  // sendMove(gameId, move).catch(e => {
  //   toast.error(e?.response?.data?.message || "Illegaler Zug");
  // }).finally(() => {
  //   selected.value = null;
  //   highlights.value = [];
  // });

function onSendChat(){
  console.log(match.value)
  const text = chatInput.value.trim();
  if (!text) return;
  sendChat(gameId, text).catch(()=>{}); // Errors hier sind nicht kritisch
  chatInput.value = "";
}

function onOfferDraw(){ offerDraw(gameId).catch(()=>{}); }

async function onResign(){
  const ok = await askConfirm({
    title: "Aufgeben?",
    message: "Bist du sicher, dass du die Partie aufgeben möchtest?",
    confirmText: "Ja, aufgeben",
    cancelText: "Weiter spielen",
    danger: true
  });
  if (!ok) return;
  resign(gameId).catch(e => {
    toast.error(e?.response?.data?.message || "Aufgeben fehlgeschlagen");
  });
}

function formatTime(ms:number){
  const s = Math.floor(ms/1000);
  const m = Math.floor(s/60);
  const ss = (s%60).toString().padStart(2,"0");
  return `${m}:${ss}`;
}

// ----- WS handling: subscribe topic/game/{gameId}
let offGameHandler: (()=>void)|null = null;

onMounted(async () => {
  const token = localStorage.getItem("token") || undefined;
  ws.ensureConnected(token);
  ws.subscribeGame(gameId);
  offGameHandler = ws.onGameMessage(gameId, handleGameEvent);

  rebuildCells();
  console.log("mount: rebuild cells")
});

onBeforeUnmount(() => {
  offGameHandler?.();
  ws.unsubscribeGame(gameId);
});

onBeforeUnmount(() => {
  offGameHandler?.();
  ws.unsubscribeGame(gameId);
  if (resizeObs && boardWrap.value?.parentElement) {
    //resizeObs.unobserve(boardWrap.value.parentElement);
  }
  stopClock();
});


function handleGameEvent(msg:any){
  switch (msg.type) {
    case "MOVE_APPLIED": {
      const move: Move = msg.move;
      console.log(move)

      const fromCellIndex = cells.value.findIndex(cell => cell.key === keyOf(switchPos(move.from.y), move.from.x));
      const toCellIndex = cells.value.findIndex(cell => cell.key === keyOf(switchPos(move.to.y), move.to.x));

      const piece: Piece = {type: move.piece.type, position: {x: move.to.x, y: move.to.y}, color: move.piece.color}

      console.log("Piece: " + piece)

      if (fromCellIndex !== -1 && toCellIndex !== -1) {
        cells.value[fromCellIndex] = { ...cells.value[fromCellIndex], piece: null };
        cells.value[toCellIndex] = { ...cells.value[toCellIndex], piece: piece };
      }

      turn.value = (turn.value === "WHITE") ? "BLACK" : "WHITE";
      syncClockFromWs(msg.remainingTime, turn.value );
      clearHighlights();
      console.log("Moved played")
      break;
    }
    case "CHAT": {
      // payload: { id, sender, text }
      chat.value.push(msg.payload);
      break;
    }
    case "GAME_ENDED": {
      const { winner, endFlag } = msg.details || {};
      end.open = true;
      end.winner = (winner === "WHITE" || winner === "BLACK") ? winner : null;
      end.endFlag = typeof endFlag === "string" ? endFlag : "UNSPECIFIED";
      stopClock();
      break;
    }
    case "CLOCK_SYNC": {
      const { WHITE, BLACK } = msg.payload || {};
      if (typeof WHITE === "number") white.time = WHITE;
      if (typeof BLACK === "number") black.time = BLACK;
      break;
    }
    case "GAME_OVER": {
      // payload: { reason: "checkmate"|"resign"|"draw", winner: "WHITE"|"BLACK"|null }
      toast.info(`Spielende: ${msg.payload.reason}`);
      break;
    }
    default:
      // ignore
  }
}

import { computed } from "vue";

const winnerPlayer = computed(() => {
  if (end.winner === "WHITE") return white;
  if (end.winner === "BLACK") return black;
  return { name: "—", avatar: "/assets/default-avatar.png" } as any;
});

type EndUi = { title: string; subtitle: string; icon: 'trophy'|'flag'|'clock'|'handshake'|'dot'; iconClass: string; hasWinner: boolean; };

function endText(flag: string, winner: Color | null): EndUi {
  const w = winner === "WHITE" ? "Weiß" : winner === "BLACK" ? "Schwarz" : null;
  switch (flag) {
    case "CHECKMATE":
      return { title: w ? `${w} gewinnt!` : "Schachmatt", subtitle: "durch Schachmatt", icon: "trophy", iconClass: "ico-win", hasWinner: true };
    case "TIMEOUT":
      return { title: w ? `${w} gewinnt!` : "Zeit abgelaufen", subtitle: "Zeitablauf", icon: "clock", iconClass: "ico-time", hasWinner: true };
    case "RESIGNATION":
      return { title: w ? `${w} gewinnt!` : "Aufgabe", subtitle: "durch Aufgabe", icon: "flag", iconClass: "ico-flag", hasWinner: true };
    case "FORFEIT":
      return { title: w ? `${w} gewinnt!` : "Forfeit", subtitle: "durch Forfait/Regelverstoß", icon: "flag", iconClass: "ico-flag", hasWinner: true };
    case "STALEMATE":
      return { title: "Patt", subtitle: "Unentschieden (Pattstellung)", icon: "handshake", iconClass: "ico-draw", hasWinner: false };
    case "DRAW_AGREEMENT":
      return { title: "Remis", subtitle: "einvernehmlich", icon: "handshake", iconClass: "ico-draw", hasWinner: false };
    default:
      return { title: w ? `${w} gewinnt!` : "Partie beendet", subtitle: flag || "—", icon: "dot", iconClass: "ico-neutral", hasWinner: !!w };
  }
}

const endUi = computed(() => endText(end.endFlag, end.winner));

function onRematch(){
  // TODO: hier API-Call für Revanche o. Route wechseln
  end.open = false;
}
function onBack(){
  // TODO: z.B. Router zurück
  end.open = false;
}

import ConfirmModal from "../components/ConfirmModal.vue";

const confirm = reactive({
  open: false,
  title: "Aktion bestätigen",
  message: "",
  confirmText: "Bestätigen",
  cancelText: "Abbrechen",
  danger: false,
  _resolve: (v: boolean) => {}
});

function askConfirm(opts: Partial<typeof confirm>): Promise<boolean> {
  return new Promise(resolve => {
    Object.assign(confirm, {
      title: "Aktion bestätigen",
      message: "",
      confirmText: "Bestätigen",
      cancelText: "Abbrechen",
      danger: false,
      ...opts,
      open: true,
      _resolve: resolve
    });
  });
}
function onConfirmOk(){
  confirm.open = false;
  confirm._resolve(true);
}
function onConfirmCancel(){
  confirm.open = false;
  confirm._resolve(false);
}

</script>

<style scoped>
.board {
  display: grid;
  gap: 4px;
}
.cell {
  width: 100%;
  height: 100%;
  aspect-ratio: 1/1;
  background: #f0d9b5;
  border-radius: 0.5rem;
  position: relative;
  display: grid;
  place-items: center;
}
.cell.dark { background: #b58863; }

/* moderne Highlights & Auswahl */
.cell.hl { position: relative; isolation: isolate; }
.cell.hl::after {
  content: "";
  position: absolute; inset: 18%;
  border-radius: 50%;
  background: rgba(220, 220, 220, 0.8);
  box-shadow:
      0 0 0 3px rgba(0, 170, 136, 0.75),
      0 0 20px rgba(0, 170, 136, 0.55);
  z-index: 1; pointer-events: none; mix-blend-mode: screen;
}

.cell:not(.dark).hl::after {
  background: rgb(73, 44, 22);
  box-shadow:
      0 0 0 3px color-mix(in srgb, var(--accent) 80%, transparent),
      0 0 20px color-mix(in srgb, var(--accent) 80%, transparent);
}

@supports (background: color-mix(in srgb, red 50%, transparent)) {
  .cell.hl::after {
    background: color-mix(in srgb, var(--accent) 80%, transparent);
    box-shadow:
        0 0 0 3px color-mix(in srgb, var(--accent) 90%, transparent),
        0 0 20px color-mix(in srgb, var(--accent) 80%, transparent);
  }
}
.cell.sel {
  outline: none;
  box-shadow:
      0 0 0 3px color-mix(in srgb, var(--accent) 80%, transparent),
      0 0 25px color-mix(in srgb, var(--accent) 80%, transparent);
  z-index: 2;
}
.piece { max-width: 88%; max-height: 88%; user-select: none; }

.avatar-box { width: 100%; aspect-ratio: 1/1; background: #2a2d33; overflow: hidden; display: grid; place-items: center; }
.avatar-img { width: 100%; height: 100%; object-fit: cover; }
.timer { font-weight: 700; margin-top: 4px; }
.timer.active { color: var(--accent); }

.chat { height: 80vh; display: flex; flex-direction: column; }
.chat-messages { flex: 1; overflow-y: auto; }
.chat-line { margin-bottom: 6px; }
.moves { max-height: 25vh; overflow-y: auto; }
.move-row { display:flex; gap:.5rem; }
.idx { width: 2.5rem; color: var(--secondary); }

/* Endscreen */
.end-overlay {
  position: fixed; inset: 0; z-index: 9999;
  background: rgba(10,10,14,0.6); backdrop-filter: blur(6px);
  display: grid; place-items: center;
  margin-top: 0;
}
.end-card {
  background: linear-gradient(180deg, rgba(30,32,38,.9), rgba(24,26,32,.9));
  border: 1px solid rgba(255,255,255,0.08);
  border-radius: 1rem;
  padding: 1.5rem;
  min-width: min(92vw, 420px);
  text-align: center;
  box-shadow: 0 10px 40px rgba(0,0,0,.5);
}
.text-muted { color: var(--secondary); }

.end-head { display:flex; align-items:center; gap:.75rem; justify-content:center; margin-bottom: .75rem; }
.end-icon { width:44px; height:44px; border-radius:12px; display:grid; place-items:center; }
.end-icon.ico-win { background: linear-gradient(135deg, #2e7d32, #1b5e20); color:#c8f7c5; }
.end-icon.ico-flag { background: linear-gradient(135deg, #8e24aa, #5e35b1); color:#e6d6ff; }
.end-icon.ico-time { background: linear-gradient(135deg, #1565c0, #0d47a1); color:#d6e9ff; }
.end-icon.ico-draw { background: linear-gradient(135deg, #616161, #424242); color:#eee; }
.end-icon.ico-neutral { background: linear-gradient(135deg, #455a64, #263238); color:#e0f7fa; }
.end-titles h3 { line-height:1.15; }

.winner-wrap { display:flex; align-items:center; justify-content:center; gap:1rem; margin-top:.5rem; }
.winner-avatar { position:relative; width:96px; height:96px; border-radius:50%; overflow:hidden; border:2px solid rgba(255,255,255,.1); box-shadow: 0 10px 30px rgba(0,0,0,.35); }
.winner-avatar.is-white { outline: 3px solid rgba(255,255,255,.85); background: radial-gradient(ellipse at top, #f5f5f5, #cccccc); }
.winner-avatar.is-black { outline: 3px solid rgba(0,0,0,.85); background: radial-gradient(ellipse at top, #333, #111); }
.winner-avatar img { width:100%; height:100%; object-fit: cover; opacity:.9; }
.king-overlay { position:absolute; right:-6px; bottom:-6px; width:42px; height:42px; filter: drop-shadow(0 2px 10px rgba(0,0,0,.6)); }

.winner-info { text-align:left; }
.winner-name { font-weight:700; font-size:1.1rem; }
.color-pill { display:inline-block; font-size:.75rem; padding:.25rem .5rem; border-radius:.5rem; margin-top:.25rem; }
.pill-white { background: rgba(255,255,255,.9); color:#111; border:1px solid rgba(0,0,0,.1); }
.pill-black { background: rgba(0,0,0,.85); color:#f5f5f5; border:1px solid rgba(255,255,255,.08); }

.draw-wrap { display:grid; grid-template-columns: 1fr auto 1fr; align-items:center; gap:1rem; margin-top:.5rem; }
.draw-player { text-align:center; }
.mini-avatar { width:72px; height:72px; border-radius:50%; overflow:hidden; border:2px solid rgba(255,255,255,.08); margin:0 auto .25rem; }
.mini-avatar.is-white { outline: 2px solid rgba(255,255,255,.75); }
.mini-avatar.is-black { outline: 2px solid rgba(0,0,0,.75); }
.mini-avatar img { width:100%; height:100%; object-fit:cover; }
.mini-name { font-size:.9rem; opacity:.9; }
.draw-center { font-weight:800; font-size:1.25rem; opacity:.9; }

</style>
