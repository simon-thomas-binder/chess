import {ref} from "vue";
import type {Ref} from "vue";
import {acceptDraw, declineDraw, offerDraw} from "../services/gameService.ts";
import type {Color} from "../models/chess.ts";
import {toast} from "./toast.ts";

export function useDraw(gameId: string, turn: Ref<Color>, player: Ref<Color>) {
    const showOfferDraw = ref<"disabled" | "show" | "waiting" | "invisible">("show");
    const showResponseDrawOptions = ref<boolean>(false);

    async function onOfferDraw() {
        await offerDraw(gameId);
    }

    async function onAcceptDraw() {
        await acceptDraw(gameId);
    }

    async function onDeclineDraw() {
        await declineDraw(gameId);
    }

    function resetDraw() {
        showResponseDrawOptions.value = false;
        if (turn.value == player.value) {
            showOfferDraw.value = "show";
        } else {
            showOfferDraw.value = "disabled";
        }
    }

    function drawEvent() {
        if (turn.value != player.value) {
            showOfferDraw.value = "invisible";
            showResponseDrawOptions.value = true;
            toast.info(`Ihnen wurde ein Remi angeboten`);
        } else {
            showOfferDraw.value = "waiting";
            toast.info(`Sie haben eine Remi angeboten`);
        }
    }

    return {
        showOfferDraw,
        showResponseDrawOptions,
        onOfferDraw,
        onAcceptDraw,
        onDeclineDraw,
        resetDraw,
        drawEvent
    };
}
