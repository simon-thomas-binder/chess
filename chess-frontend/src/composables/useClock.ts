import type { Color } from "../models/chess";
import type {Ref} from "vue";
import {reactive} from "vue";

export function useClock(turn: Ref<Color>, end: Ref<{ open: boolean }>) {
    const times = reactive(new Map<Color, number>()) as unknown as Map<Color, number>;

    let ticking: boolean = false;
    let lastTimestamp = 0;
    let rafId = 0;

    function startClock() {
        if (ticking) return;
        ticking = true;
        lastTimestamp = performance.now();
        rafId = requestAnimationFrame(stepClock);
    }

    function stopClock() {
        ticking = false;
        cancelAnimationFrame(rafId);
    }

    function stepClock(ts: number) {
        const dt = ts - lastTimestamp;
        lastTimestamp = ts;

        if (turn.value === "WHITE") {
            const newW = Math.max(0, (times.get('WHITE') ?? 0) - dt);
            times.set('WHITE', newW);
        } else {
            const newB = Math.max(0, (times.get('BLACK') ?? 0) - dt);
            times.set('BLACK', newB);
        }

        if (ticking && !end.value.open && (times.get('WHITE') ?? 0) > 0 && (times.get('BLACK') ?? 0) > 0) {
            rafId = requestAnimationFrame(stepClock);
        } else {
        }
    }

    function syncClockFromWs(remaining?: Record<string, number>) {
        stopClock();
        if (remaining) {
            const w = (remaining as any).white ?? (remaining as any).WHITE ?? (remaining as any).White;
            const b = (remaining as any).black ?? (remaining as any).BLACK ?? (remaining as any).Black;
            if (typeof w === "number") times.set('WHITE', w);
            if (typeof b === "number") times.set('BLACK', b);
        }
        if (!end.value.open) startClock();
    }

    return { times, stopClock, syncClockFromWs }
}