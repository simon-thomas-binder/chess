// src/composables/ws.ts
import { Client } from "@stomp/stompjs";
import type { StompSubscription, IMessage } from "@stomp/stompjs";
import SockJS from "sockjs-client";


type Handler = (msg: any) => void;

class WsBus {
    private client: Client | null = null;
    private userSub: StompSubscription | null = null;
    private gameSubs = new Map<string, StompSubscription>();
    private onUserHandlers: Handler[] = [];
    private onGameHandlers = new Map<string, Handler[]>();
    private base = (import.meta as any).env?.VITE_API_BASE || "http://localhost:8080/api";

    private backendBase() {
        return this.base.replace(/\/api\/?$/, "");
    }

    ensureConnected(token?: string) {
        if (this.client) return;

        const wsUrl = `${this.backendBase()}/ws`;

        this.client = new Client({
            webSocketFactory: () => new SockJS(wsUrl),
            reconnectDelay: 3000,
            connectHeaders: token ? { Authorization: `Bearer ${token}` } : {},
            debug: () => {}
        });

        this.client.onConnect = () => {
            // optional: auto-resubscribe on reconnect
            const username = this.getUsernameFromToken(token);
            if (username) this.subscribeUser(username);
            // Re-attach game subs after reconnect
            for (const gameId of this.onGameHandlers.keys()) {
                this.subscribeGame(gameId);
            }
        };

        this.client.activate();
    }

    disconnect() {
        this.userSub?.unsubscribe();
        this.userSub = null;
        for (const sub of this.gameSubs.values()) sub.unsubscribe();
        this.gameSubs.clear();
        this.client?.deactivate();
        this.client = null;
    }

    private getUsernameFromToken(t?: string | null) {
        const tok = t ?? localStorage.getItem("token");
        if (!tok) return null;
        try {
            const payload = JSON.parse(atob(tok.split(".")[1]));
            return payload.sub || payload.username || null;
        } catch {
            return null;
        }
    }

    subscribeUser(username?: string) {
        if (!this.client || !username) return;
        if (this.userSub) this.userSub.unsubscribe();
        this.userSub = this.client.subscribe(`/topic/user/${username}`, (frame: IMessage) => {
            try {
                const msg = JSON.parse(frame.body);
                this.onUserHandlers.forEach(h => h(msg));
            } catch (e) {
                console.error("WS user parse error", e);
            }
        });
    }

    onUserMessage(handler: Handler) {
        this.onUserHandlers.push(handler);
        return () => {
            this.onUserHandlers = this.onUserHandlers.filter(h => h !== handler);
        };
    }

    subscribeGame(gameId: string) {
        if (!this.client) return;
        // de-dupe
        if (this.gameSubs.has(gameId)) return;
        const sub = this.client.subscribe(`/topic/game/${gameId}`, (frame: IMessage) => {
            try {
                const msg = JSON.parse(frame.body);
                const hs = this.onGameHandlers.get(gameId) || [];
                hs.forEach(h => h(msg));
            } catch (e) {
                console.error("WS game parse error", e);
            }
        });
        this.gameSubs.set(gameId, sub);
    }

    unsubscribeGame(gameId: string) {
        this.gameSubs.get(gameId)?.unsubscribe();
        this.gameSubs.delete(gameId);
        this.onGameHandlers.delete(gameId);
    }

    onGameMessage(gameId: string, handler: Handler) {
        const hs = this.onGameHandlers.get(gameId) || [];
        hs.push(handler);
        this.onGameHandlers.set(gameId, hs);
        return () => {
            const list = (this.onGameHandlers.get(gameId) || []).filter(h => h !== handler);
            if (list.length) this.onGameHandlers.set(gameId, list);
            else this.onGameHandlers.delete(gameId);
        };
    }
}

const bus = new WsBus();

export function useWs() {
    return bus;
}
