<template>
  <button
      v-if="props.showOfferDraw.value !== 'invisible'"
      class="btn btn-muted"
      :disabled="props.showOfferDraw.value === 'disabled' || props.showOfferDraw.value === 'waiting'"
      @click="onOfferDraw"
  >
    <span v-if="props.showOfferDraw.value === 'show'">Remis anbieten</span>
    <span v-else-if="props.showOfferDraw.value === 'waiting'">Warten...</span>
    <span v-else>Remis anbieten</span>
  </button>
  <div v-if="showResponseDrawOptions.value" class="flex flex-col gap-2">
    <p class="text-center">Möchten Sie das Remis annehmen?</p>
    <div class="flex gap-2">
      <button class="btn btn-success flex-1" @click="onAcceptDraw">
        Annehmen
      </button>
      <button class="btn btn-danger flex-1" @click="onDeclineDraw">
        Ablehnen
      </button>
    </div>
  </div>
</template>

<script setup lang="ts">
import type { Ref } from "vue";

const props = defineProps<{
  showOfferDraw: Ref<"disabled" | "show" | "waiting" | "invisible">;
  showResponseDrawOptions: Ref<boolean>;
  onAcceptDraw: () => void;
  onDeclineDraw: () => void;
  onOfferDraw: () => void;
}>();
</script>

<style scoped>

.flex-1 {
  width: 100%;
  margin-top: 10px;
}

.text-center {
  text-align: center;
  margin-bottom: 5px;
}

</style>
