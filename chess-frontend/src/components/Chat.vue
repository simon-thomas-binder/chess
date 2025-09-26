<template>
  <div class="card--glass p-3 chat">
    <h6 class="fw-semibold mb-2">Chat</h6>

    <!-- massages -->
    <div class="chat-messages" ref="chatContainer" role="log" aria-live="polite">
      <div
          v-for="(m, idx) in chat"
          :key="m.id"
          :class="['chat-line', m.color, { grouped: isGrouped(idx) }]"
      >
        <!-- Author header -->
        <div v-if="!isGrouped(idx)" class="chat-meta">
          <strong class="chat-name">{{ players.get(m.color ?? 'WHITE')?.displayname ?? '-' }}</strong>
        </div>

        <!-- Text Bubble -->
        <div class="chat-bubble" :title="m.timeFormatted">
          <span class="chat-text" v-text="m.text"></span>
        </div>
      </div>
    </div>

    <!-- Input field -->
    <div class="mt-2 chat-input-row">
      <textarea
          v-model="chatInput"
          class="form-control chat-input"
          @keydown.enter.prevent="emitSend"
          @keydown.shift.enter.stop
          rows="2"
          placeholder="Nachricht…"
      ></textarea>

      <div class="d-flex justify-content-end mt-2">
        <button class="btn btn-accent" @click="emitSend">Senden</button>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, watch, nextTick } from "vue";
import type {Color} from "../models/chess.ts";
import type {User} from "../services/authService.ts";

const props = defineProps<{
  chat: { id: number; text: string; timeFormatted?: string; color?: Color }[];
  players: Map<Color, User>;
}>();

// send event
const emit = defineEmits<{
  (e: "send", text: string): void;
}>();

const chatInput = ref("");
const chatContainer = ref<HTMLElement | null>(null);

function emitSend() {
  const text = chatInput.value.trim();
  if (!text) return;
  emit("send", text);
  chatInput.value = "";
}

function isGrouped(idx: number): boolean {
  if (idx === 0) return false;

  const cur = props.chat[idx];
  const prev = props.chat[idx - 1];
  if (!cur || !prev) return false;

  return cur.color === prev.color;
}

// auto-scroll
watch(
    () => props.chat.length,
    async () => {
      await nextTick();
      if (chatContainer.value) {
        chatContainer.value.scrollTop = chatContainer.value.scrollHeight;
      }
    }
);
</script>

<style scoped>
.chat-input-row { display: flex; flex-direction: column; }
.chat-input { resize: none; min-height: 42px; max-height: 140px; }
.chat-line.is-white .chat-bubble {
  background: rgba(255,255,255,0.08);
  color: #0b0b0b; /* dunkles text für helle bubble */
}
.chat-line.is-black .chat-bubble {
  background: rgba(255,255,255,0.03);
  color: var(--text); /* hellen text */
}
.chat { height: 80vh; display: flex; flex-direction: column; }
.chat-messages { flex: 1; overflow-y: auto; padding: 6px; scroll-behavior: smooth; }


/* eine Nachricht (Block) */
.chat-line {
  display: flex;
  flex-direction: column;
  gap: 6px;
  margin-bottom: 8px;
  max-width: 100%;
}

/* Meta: Name + Zeit */
.chat-meta { display: flex; gap: 8px; align-items: center; color: var(--muted); margin: 5px; }
.chat-name { font-weight: 600; }

.chat-line.grouped { margin-top: -6px; margin-bottom: 6px; }

.chat-bubble {
  padding: 8px 10px;
  border-radius: 10px;
  max-width: 100%;
  white-space: pre-wrap;
  overflow-wrap: anywhere;
  word-break: break-word;
  line-height: 1.3;
  box-shadow: 0 2px 6px rgba(0,0,0,0.25);
  font-size: 0.95rem;
  display: flex;
  justify-content: space-between;
  align-items: center;
}
.chat-text {
  flex: 1;
}

.chat-line {
  margin-bottom: 6px;
  display: block;
  line-height: 1.2;
  padding: 2px 6px;
  border-radius: 6px;
}

</style>