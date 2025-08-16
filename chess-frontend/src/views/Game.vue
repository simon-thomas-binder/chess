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
import { sendMove, sendChat, offerDraw, resign, type MovePayload } from "../services/gameService";
import { toast } from "../composables/toast";
import type {Color, Piece} from "../models/chess.ts";
import { useMatchStore } from "../stores/matchStore";

// ----- Routing / IDs
const route = useRoute();
const gameId = String(route.params.id);

// ----- WS
const ws = useWs();

const matchStore = useMatchStore();
const match = ref(matchStore.current);

const cells = ref<any[]>([]);
const selected = ref<{row:number;col:number}|null>(null);
const highlights = ref<string[]>([]);
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
function pieceAt(x:number,y:number){ return match.value?.chessboard.pieces.find(p=>p.xPos===x && p.yPos===y) || null; }

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
function isSelected(cell:any){ return selected.value && selected.value.row===cell.row && selected.value.col===cell.col; }
function isHighlighted(cell:any){ return highlights.value.includes(keyOf(cell.row,cell.col)); }

function pieceSprite(p:Piece){
  return `/pieces/${p.color.toLowerCase()}_${p.type}.png`;
}

function onCellClick(cell:any){
  const sel = selected.value;
  if (!sel) {
    if (cell.piece /* && piece.color === myColor */) {
      selected.value = { row: cell.row, col: cell.col };
      // optional: call /api/games/{id}/legal-moves?from=... to highlight
      highlights.value = []; // fill with legal targets if you call the API
    }
    return;
  }

  // second click -> try move via HTTP
  const move: MovePayload = { from: sel, to: { row: cell.row, col: cell.col } };
  sendMove(gameId, move).catch(e => {
    toast.error(e?.response?.data?.message || "Illegaler Zug");
  }).finally(()=>{
    selected.value = null;
    highlights.value = [];
  });
}

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

  // initial state via HTTP
  /*const { data } = await getInitialState(gameId);
  // expected minimal shape:
  // { board:{width,height}, pieces:[{id,type,color,row,col}], turn, clocks:{WHITE:ms,BLACK:ms}, players:{WHITE:{name,avatar},BLACK:{...}}, moves:[...]}
  Object.assign(board, data.board || { width:8, height:8 });
  pieces.value = data.pieces || [];
  turn.value = data.turn || "WHITE";
  white.name = data.players?.WHITE?.name ?? white.name;
  white.avatar = data.players?.WHITE?.avatar ?? white.avatar;
  white.time = data.clocks?.WHITE ?? white.time;
  black.name = data.players?.BLACK?.name ?? black.name;
  black.avatar = data.players?.BLACK?.avatar ?? black.avatar;
  black.time = data.clocks?.BLACK ?? black.time;*/
  //moves.value = data.moves || [];
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
      // payload: { piece:{id,type,color}, from:{r,c}, to:{r,c}, san, turn, clocks:{WHITE,BLACK}, pieces:[...] (optional) }
      if (msg.payload?.pieces) {
        //match.value?.chessboard.pieces = msg.payload.pieces;
      } else {
        // minimal update (if backend nur die Zugänderung sendet)
        //const p = pieces.value.find(x=>x.id===msg.payload.piece.id);
        //if (p) { p.row = msg.payload.to.row; p.col = msg.payload.to.col; }
      }
      moves.value.push({
        piece: msg.payload.piece.type,
        to: msg.payload.to,
        san: msg.payload.san
      });
      turn.value = msg.payload.turn;
      if (msg.payload.clocks) {
        white.time = msg.payload.clocks.WHITE ?? white.time;
        black.time = msg.payload.clocks.BLACK ?? black.time;
      }
      rebuildCells();
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
