<template>
  <div class="container-xxl py-4">
    <div class="row g-4">
      <div class="col-12 col-xl-8">
        <div class="card--glass p-3">
          <h5 class="fw-semibold mb-3">Eigenes Spiel erstellen</h5>
          <form @submit.prevent="create">
            <div class="row g-3">
              <div class="col-6 col-md-3">
                <label class="form-label">Breite</label>
                <input v-model.number="board.width" type="number" class="form-control" min="4" max="16" />
              </div>
              <div class="col-6 col-md-3">
                <label class="form-label">Höhe</label>
                <input v-model.number="board.height" type="number" class="form-control" min="4" max="16" />
              </div>
              <div class="col-12 col-md-3">
                <label class="form-label">Initial (Sek.)</label>
                <input v-model.number="tc.initial" type="number" class="form-control" min="30" max="7200" />
              </div>
              <div class="col-6 col-md-3">
                <label class="form-label">Increment (Sek.)</label>
                <input v-model.number="tc.increment" type="number" class="form-control" min="0" max="60" />
              </div>
              <div class="col-6 col-md-3">
                <label class="form-label">Delay (Sek.)</label>
                <input v-model.number="tc.delay" type="number" class="form-control" min="0" max="10" />
              </div>
            </div>

            <div class="mt-3 d-flex gap-2">
              <button class="btn btn-accent" :disabled="loading">Erstellen</button>
              <router-link class="btn btn-muted" to="/lobby">Zurück</router-link>
            </div>
          </form>
        </div>
      </div>

      <div class="col-12 col-xl-4">
        <div class="card--glass p-3">
          <h6 class="fw-semibold mb-2">Vorschau</h6>
          <div class="preview-board">
            <div
                v-for="i in board.height"
                :key="`r${i}`"
                class="preview-row"
                :style="{ gridTemplateColumns: `repeat(${board.width}, 1fr)` }"
            >
              <div v-for="j in board.width" :key="`c${i}-${j}`" class="preview-cell" />
            </div>
          </div>
          <div class="small text-secondary mt-2">
            Rastervorschau {{ board.width }}×{{ board.height }}
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref } from "vue";
import { toast } from "../composables/toast";
import api from "../api";

const loading = ref(false);
const board = ref({ width: 8, height: 8 });
const tc = ref({ initial: 180, increment: 2, delay: 0 });

async function create() {
  loading.value = true;
  try {
    await api.post("/chess/create", {
      chessboard: { width: board.value.width, height: board.value.height },
      initial_time: tc.value.initial * 1000,
      increment: tc.value.increment * 1000,
      delay: tc.value.delay * 1000,
    });
    toast.success("Spiel erstellt.");
  } catch (e: any) {
    toast.error(e?.response?.data?.message || "Erstellung fehlgeschlagen.");
  } finally {
    loading.value = false;
  }
}
</script>

<style scoped>
.preview-board {
  display: grid;
  gap: 4px;
}
.preview-row {
  display: grid;
  gap: 4px;
}
.preview-cell {
  aspect-ratio: 1/1;
  background: #2a2d33;
  border: 1px solid rgba(255,255,255,0.06);
  border-radius: 6px;
}
</style>
