<template>
  <teleport to="body">
    <div v-if="modelValue" class="cm-overlay" @click.self="$emit('update:modelValue', false)">
      <div class="cm-card" role="dialog" aria-modal="true" :aria-labelledby="id + '-title'">
        <div class="cm-head">
          <span class="cm-icon" :class="danger ? 'danger' : 'neutral'">
            <svg v-if="danger" width="24" height="24" viewBox="0 0 24 24" fill="currentColor"><path d="M1 21h22L12 2 1 21zm12-3h-2v2h2v-2zm0-6h-2v5h2v-5z"/></svg>
            <svg v-else width="24" height="24" viewBox="0 0 24 24" fill="currentColor"><path d="M1 21h22L12 2 1 21zm11-3h-2v2h2v-2zm0-6h-2v5h2v-5z"/></svg>
          </span>
          <h4 class="m-0" :id="id + '-title'">{{ title }}</h4>
        </div>
        <p class="cm-text">{{ message }}</p>
        <div class="cm-actions">
          <button class="btn btn-muted" @click="$emit('cancel')">{{ cancelText }}</button>
          <button class="btn" :class="danger ? 'btn-danger' : 'btn-accent'" @click="$emit('confirm')">
            {{ confirmText }}
          </button>
        </div>
      </div>
    </div>
  </teleport>
</template>

<script setup lang="ts">
import { computed } from "vue";

const props = defineProps<{
  modelValue: boolean;
  title?: string;
  message?: string;
  confirmText?: string;
  cancelText?: string;
  danger?: boolean;
  id?: string;
}>();
defineEmits(["update:modelValue","confirm","cancel"]);

const id = computed(() => props.id || "confirm-modal");
</script>

<style scoped>
.cm-overlay{
  position: fixed; inset:0; z-index:10000;
  background: rgba(10,10,14,.6); backdrop-filter: blur(6px);
  display:grid; place-items:center;
}
.cm-card{
  background: linear-gradient(180deg, rgba(30,32,38,.95), rgba(24,26,32,.95));
  border: 1px solid rgba(255,255,255,.08);
  border-radius: 1rem;
  padding: 1.25rem;
  width: min(92vw, 440px);
  color: #f1f2f4;
  box-shadow: 0 10px 40px rgba(0,0,0,.5);
}
.cm-head{ display:flex; align-items:center; gap:.75rem; margin-bottom:.5rem; }
.cm-icon{ width:40px; height:40px; border-radius:10px; display:grid; place-items:center; color:#fff; }
.cm-icon.neutral{ background: linear-gradient(135deg,#1565c0,#0d47a1); }
.cm-icon.danger{ background: linear-gradient(135deg,#c62828,#8e0000); }
.cm-text{ color: var(--secondary); margin:.5rem 0 1rem; }
.cm-actions{ display:flex; justify-content:flex-end; gap:.5rem; }
</style>
