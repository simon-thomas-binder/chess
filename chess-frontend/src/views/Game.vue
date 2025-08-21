<template>
  <div class="container-xxl py-4">
    <div class="row g-4">
      <!-- Links: Spielerinfos -->
      <div class="col-12 col-xl-2">
        <div class="card--glass p-3 text-center mb-3">
          <div class="avatar-box mb-2"><img :src="white.avatar" class="avatar-img" /></div>
          <h6 class="fw-semibold">{{ white.name }}</h6>
          <div class="timer" :class="{ active: turn === 'WHITE' }">{{ formatTime(white.time) }}</div>
        </div>
        <div class="card--glass p-3 text-center">
          <div class="avatar-box mb-2"><img :src="black.avatar" class="avatar-img" /></div>
          <h6 class="fw-semibold">{{ black.name }}</h6>
          <div class="timer" :class="{ active: turn === 'BLACK' }">{{ formatTime(black.time) }}</div>
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
function onResign(){ resign(gameId).catch(()=>{}); }

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

      console.log("Moved played")
      break;
    }
    case "CHAT": {
      // payload: { id, sender, text }
      chat.value.push(msg.payload);
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
</script>

<style scoped>
.board {
  display: grid;
  gap: 4px;
  height: 100%;
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
.cell.sel { outline: 2px solid var(--accent); }
.cell.hl::after {
  content: "";
  width: 30%;
  height: 30%;
  border-radius: 50%;
  background: rgba(20,255,20,0.5);
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
</style>
