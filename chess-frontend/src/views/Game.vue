<template>
  <div class="container-xxl py-4">
    <div class="row g-4">

      <!-- Left section: game infos -->
      <div class="col-12 col-xl-2">
        <div v-for="([color, user]) in Array.from(players.entries())" :key="color" class="card--glass p-3 text-center mb-3">
          <div class="avatar-box mb-2">
            <img :src="getAvatar(user)" class="avatar-img" />
          </div>
          <h6 class="fw-semibold">{{ color }} - {{ user.displayname }}</h6>
          <div class="timer" :class="{ active: turn === color, low: times.get(color) ?? 0 <= 10000 }">
            {{ formatTime(times.get(color) ?? 0) }}
          </div>
        </div>
        <div class="card--glass p-3 mt-3">
          <div class="d-grid gap-2">
            <button v-if="!draw.showResponseDrawOptions.value" class="btn btn-danger" @click="onResign">Aufgeben</button>
            <DrawButtons :show-offer-draw="draw.showOfferDraw"
                         :show-response-draw-options="draw.showResponseDrawOptions"
                         :on-accept-draw="draw.onAcceptDraw"
                         :on-decline-draw="draw.onDeclineDraw"
                         :on-offer-draw="draw.onOfferDraw"></DrawButtons>
          </div>
        </div>
      </div>

      <!-- Center section: board -->
      <div class="col-12 col-xl-7 d-flex flex-column">
        <div
            class="board card--glass p-2"
            :style="{ gridTemplateColumns: `repeat(${match?.chessboard.width}, 1fr)` }"
        >
          <div
              v-for="cell in cells"
              :key="cell.key"
              class="cell"
              :class="{ dark: cell.dark, sel: cell.sel, hl: cell.hl }"
              @click="onCellClick(cell)"
          >
            <img
                v-if="cell.piece"
                :src="pieceSprite(cell.piece)"
                class="piece"
                draggable="false"
                :alt="cell.piece.color.toLowerCase() + ' ' +  cell.piece.type.toLowerCase()"
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

      <!-- Right section: Chat -->
      <div class="col-12 col-xl-3">
        <Chat :chat="chat" :players="players" @send="onSendChat" />
      </div>

      <!-- End-modal Overlay -->
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
              <img :src="getAvatar(winnerPlayer)" class="avatar-img" />
              <img class="king-overlay" :src="`/pieces/${(end.winner||'white').toLowerCase()}_king.png`" alt="" />
            </div>
            <div class="winner-info">
              <div class="winner-name">{{ winnerPlayer.displayname }}</div>
              <div class="color-pill" :class="end.winner === 'WHITE' ? 'pill-white' : 'pill-black'">
                {{ end.winner === 'WHITE' ? 'Weiß' : 'Schwarz' }}
              </div>
            </div>
          </div>
<!--
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
          </div>-->

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
import {computed, onBeforeUnmount, onMounted, reactive, ref} from "vue";
import {useRoute} from "vue-router";
import {useWs} from "../composables/ws";
import {getMoves, resign, sendChat, sendMove} from "../services/gameService";
import {toast} from "../composables/toast";
import {type Cell, type Chessboard, type Color, type Move, type Piece, positionToString} from "../models/chess.ts";
import {useMatchStore} from "../stores/matchStore";
import ConfirmModal from "../components/ConfirmModal.vue";
import router from "../router";
import {getUser, getUserWithUsername, type User} from "../services/authService.ts";
import Chat from "../components/Chat.vue";
import {useClock} from "../composables/useClock.ts";
import {useDraw} from "../composables/useDraw.ts";
import DrawButtons from "../components/DrawButtons.vue";

// ----- Routing / IDs
const route = useRoute();
const gameId = String(route.params.id);

// ----- WS
const ws = useWs();

const matchStore = useMatchStore();
const match = ref(matchStore.current);

const thisPlayer = ref<Color>("WHITE");

const turn = ref<Color>("WHITE");
const moves = ref<any[]>([]);
const players = ref<Map<Color, User>>(new Map());

const draw = useDraw(gameId, turn, thisPlayer);


// ----- helpers
function keyOf(x: number, y: number): string { return `${x}:${y}`; }
function pieceAt(x: number,y: number): Piece | null { return match.value?.chessboard.pieces.find(p=>p.position.x===x && p.position.y===y) || null; }
function cellAt(x: number, y: number): Cell {
  const cell = cells.value.find((cell) => cell.x === x && cell.y === y);
  if (!cell) {
    throw new Error(`Cell not found at position (${x}, ${y}).`);
  }
  return cell;
}


const end = ref<{ open: boolean; winner: Color | null; endFlag: string }>({
  open: false,
  winner: null,
  endFlag: "",
});

// --------------------------------------------------------------------------------------------


// ===============================
// Section: Cell Functionality
// ===============================

const cells = ref<Cell[]>([]);
const highlightedMoves = ref<Map<string, Move>>(new Map());
let board: Chessboard;

function buildCells() {
  const arr:Cell[] = [];
  board = match.value?.chessboard || (() => { throw new Error("Board is undefined"); })();
  if (match.value == null) {
    return
  }
  for (let h = 0; h < board.height; h++) {
    for (let x = 0; x < board.width; x++) {
      const y = match.value?.chessboard.height - h - 1;
      const dark = (y + x) % 2 === 1;
      arr.push({
        key: keyOf(x, y),
        y: y, x: x, dark: dark,
        piece: pieceAt(x, y),
        hl: false,
        sel: false,
      });
    }
  }
  cells.value = arr;
}

function clearHighlights() {
  cells.value.forEach((cell) => {cell.hl = false; cell.sel = false})
}

function pieceSprite(p:Piece): string {
  return `/assets/pieces/${p.color.toLowerCase()}_${p.type.toLowerCase()}.svg`;
}

async function onCellClick(cell: Cell) {

  // Click on selected cell
  if (cell.sel) {
    return;
  }

  // Click on highlighted cell
  if (cell.hl) {
    const move = highlightedMoves.value.get(positionToString({x: cell.x, y: cell.y}));
    if (!move) {
      clearHighlights();
      console.error("Error highlighted move does not have a related move object")
      return;
    }

    try {
      await sendMove(gameId, move);
      clearHighlights();
    } catch (e: any) {
      toast.error(e?.response?.data?.message || "Zug konnte nicht gespielt werden");
    }
    return;
  }

  // Click on cell with piece
  if (cell.piece) {
    console.log("The cell: (x: " + cell.x + ", y: " + cell.y + ") has been selected");
    clearHighlights();
    cell.sel = true;
    try {
      const availableMoves: Move[] = await getMoves(match.value?.gameId, {x: cell.x, y: cell.y});

      if (Array.isArray(availableMoves) && availableMoves.length > 0) {
        const moveMap = new Map<string, Move>();
        availableMoves.forEach((move) => {
          moveMap.set(positionToString(move.to), move);
        });
        highlightedMoves.value = moveMap;
        cells.value.map((c) => {if (highlightedMoves.value.has(positionToString({x: c.x, y: c.y}))) {
          c.hl = true;
        }})
      }
      return;
    } catch (error) {
      console.error("Error by fetching available moves:", error);
    }
  }

  // Click empty cell
  clearHighlights();
}

// --------------------------------------------------------------------------------------------


// ===============================
// Section: Clock Functionality2
// ===============================

const { times, stopClock, syncClockFromWs } = useClock(turn, end);


// --------------------------------------------------------------------------------------------

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

// --------------------------------------------------------------------------------------------

// ==============================
// Section: Chat functionality
// ==============================

const chat = ref<{ id: number; text: string; timeFormatted?: string; color?: Color }[]>([]);

function onSendChat(text: string) {
  sendChat(gameId, text).catch(() => {});
}

function formatTime(ms:number){
  const s = Math.floor(ms/1000);
  const m = Math.floor(s/60);
  const ss = (s%60).toString().padStart(2,"0");
  return `${m}:${ss}`;
}

// --------------------------------------------------------------------------------------------

// ====================
// Section: Mounting
// ====================

// ----- WS handling: subscribe topic/game/{gameId}
let offGameHandler: (()=>void)|null = null;


onMounted(async () => {
  const token = localStorage.getItem("token") || undefined;
  ws.ensureConnected(token);
  ws.subscribeGame(gameId);
  offGameHandler = ws.onGameMessage(gameId, handleGameEvent);

  buildCells();
  console.log("mount: rebuild cells")
  await initialisePlayers();
  console.log("mount: initialise players")
});

async function initialisePlayers() {
  if (match.value == undefined) {
    throw new Error('Match is not defined!')
  }

  const color = match.value?.color;
  if (color == undefined) {throw new Error('Color is undefined');}
  thisPlayer.value = color;
  const user = await getUser();
  players.value.set(color, user);
  for (const opp of match.value?.opponents) {
    const oppUser: User = await getUserWithUsername(opp.username);
    players.value.set(opp.color, oppUser)
  }

  for (const p of players.value.keys()) {
    times.set(p, match.value.chessboard.initial_time)
  }
}

onBeforeUnmount(() => {
  offGameHandler?.();
  ws.unsubscribeGame(gameId);
  stopClock();
});

// --------------------------------------------------------------------------------------------

// ====================================
// Section: Web socket event handler
// ====================================

function handleGameEvent(msg:any){
  switch (msg.type) {

    case "MOVE_APPLIED": {
      const move: Move = msg.details.move;
      console.log(move)

      const fromCellIndex = cells.value.findIndex(cell => cell.key === keyOf(move.from.x, move.from.y));
      const toCellIndex = cells.value.findIndex(cell => cell.key === keyOf(move.to.x, move.to.y));

      const piece: Piece = {type: move.piece.type, position: {x: move.to.x, y: move.to.y}, color: move.piece.color}

      if (fromCellIndex !== -1 && toCellIndex !== -1) {
        cells.value[fromCellIndex].piece = null;
        cells.value[toCellIndex].piece = piece;
      }

      const color: Color = piece.color;
      let y: number;
      if (color == "WHITE") {
        y = 0;
      } else {
        y = board.height - 1;
      }

      if (move.flag == 'CASTLE_KING') {
        const cellFrom: Cell = cellAt(board.width - 1, y);
        const cellTo: Cell = cellAt(board.width - 3, y);
        if (cellFrom.piece == null) {
          throw new Error("Cell error");
        }
        const rook: Piece = cellFrom.piece;
        cellFrom.piece = null;
        cellTo.piece = rook;
      }

      if (move.flag == 'CASTLE_QUEEN') {
        const cellFrom: Cell = cellAt(0, y);
        const cellTo: Cell = cellAt(3, y);
        if (cellFrom.piece == null) {
          throw new Error("Cell error");
        }
        const rook: Piece = cellFrom.piece;
        cellFrom.piece = null;
        cellTo.piece = rook;
      }

      turn.value = (turn.value === "WHITE") ? "BLACK" : "WHITE";
      syncClockFromWs(msg.details.remainingTime);
      clearHighlights();
      draw.resetDraw();
      console.log("Moved played")
      break;
    }

    case "CHAT": {
      const details = msg.details;
      const text = details.msg;
      const timeRaw = details.time;
      const color = details.color;

      const item = {
        id: Date.now() + Math.floor(Math.random() * 1000),
        text,
        timeFormatted: timeRaw,
        color: color,
      };

      chat.value.push(item);
      break;
    }
    case "GAME_ENDED": {
      const { winner, endFlag } = msg.details || {};
      end.value.open = true;
      end.value.winner = (winner === "WHITE" || winner === "BLACK") ? winner : null;
      end.value.endFlag = typeof endFlag === "string" ? endFlag : "UNSPECIFIED";
      stopClock();
      break;
    }
    case "DRAW_EVENT": {
      console.log("There is a draw event");
      console.log("msg: " + msg.toString());
      console.log("details: " + msg.details.flag.toString());

      if (msg.details.flag == "offer") {
        draw.drawEvent();
      }
      if (msg.details.flag == "decline") {
        draw.resetDraw();
      }
      break;
    }
    default:
      // ignore
  }
}

// --------------------------------------------------------------------------------------------

// ===========================
// Section: End game pop up
// ===========================

const winnerPlayer: any = computed(() => {
  if (end.value.winner === "WHITE") return players.value.get('WHITE');
  if (end.value.winner === "BLACK") return players.value.get('BLACK');
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

const endUi = computed(() => endText(end.value.endFlag, end.value.winner));

function onRematch(){
  // TODO: hier API-Call für Revanche o. Route wechseln
  end.value.open = false;
}
function onBack(){
  router.push('/lobby');
}

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

//TODO:
function getAvatar(user: User): string {
  return String(user.username);
}

// --------------------------------------------------------------------------------------------

</script>

<style scoped>
.board {
  display: grid;
  gap: 4px;
  max-height: 100%;
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


.input-group {
  display: flex;
  flex-direction: column;
}

.form-control {
  width: 100%;
  margin-bottom: 10px;
  resize: none;
}

.button-container {
  display: flex;
  justify-content: flex-end;
}

</style>
