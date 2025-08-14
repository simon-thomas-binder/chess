<!-- src/components/ToastHost.vue -->
<template>
  <div class="toast-container position-fixed top-0 end-0 p-3">
    <div
        v-for="t in toasts"
        :key="t.id"
        class="toast show border-0 shadow-lg mb-2"
        :class="bgClass(t.type)"
        role="alert"
        aria-live="assertive"
        aria-atomic="true"
    >
      <div class="d-flex">
        <div class="toast-body">
          <strong v-if="t.title" class="me-2">{{ t.title }}</strong>{{ t.message }}
        </div>
        <button type="button" class="btn-close btn-close-white me-2 m-auto" @click="dismiss(t.id)"></button>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { toasts, dismiss } from "../composables/toast";
function bgClass(type: string) {
  // Bootstrap 5 text-bg-* utilities
  switch (type) {
    case "success": return "text-bg-success";
    case "danger":  return "text-bg-danger";
    case "warning": return "text-bg-warning";
    default:        return "text-bg-info";
  }
}
</script>

<style scoped>
.toast-container {
  z-index: 2000;
}
</style>
