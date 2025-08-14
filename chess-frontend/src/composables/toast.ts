// src/composables/toast.ts
import { ref } from "vue";

export type ToastType = "success" | "danger" | "info" | "warning";
export interface Toast {
    id: number;
    type: ToastType;
    title?: string;
    message: string;
    timeout?: number; // ms
}

const toasts = ref<Toast[]>([]);
let idCounter = 1;

function push(toast: Omit<Toast, "id">) {
    const id = idCounter++;
    const t: Toast = { id, timeout: 4000, ...toast };
    toasts.value.push(t);
    if (t.timeout && t.timeout > 0) {
        setTimeout(() => dismiss(id), t.timeout);
    }
}

function dismiss(id: number) {
    toasts.value = toasts.value.filter((t) => t.id !== id);
}

// Convenience helpers
const toast = {
    success: (msg: string, title = "Erfolg") => push({ type: "success", message: msg, title }),
    error:   (msg: string, title = "Fehler")  => push({ type: "danger",  message: msg, title }),
    info:    (msg: string, title = "Info")    => push({ type: "info",    message: msg, title }),
    warn:    (msg: string, title = "Hinweis") => push({ type: "warning", message: msg, title }),
};

export { toasts, push, dismiss, toast };
