// src/services/socketService.ts
import { Client } from "@stomp/stompjs";
import SockJS from "sockjs-client";

let client: Client | null = null;

function getBackendBase(): string {
    const base = (import.meta as any).env?.VITE_API_BASE || "http://localhost:8080/api";
    return base.replace(/\/api\/?$/, "");
}

function getToken(): string | null {
    return localStorage.getItem("token");
}

/**
 * connectLobbySocket(onMessage)
 * - schaltet STOMP Client mit SockJS ein
 * - übergibt Authorization Header beim CONNECT
 */
export function connectLobbySocket(onMessage: (msg: any) => void) {
    const token = getToken();
    if (!token) {
        console.warn("[socket] no token, not connecting WS");
        return;
    }

    const wsUrl = `${getBackendBase()}/ws`;

    // falls schon verbunden: first disconnect
    if (client && client.active) {
        client.deactivate();
        client = null;
    }

    client = new Client({
        webSocketFactory: () => new SockJS(wsUrl),
        reconnectDelay: 3000,
        // send token as STOMP connect headers (JwtStompChannelInterceptor liest Authorization)
        connectHeaders: {
            Authorization: `Bearer ${token}`
        },
        onConnect: (frame) => {
            // frame.headers / frame.user available
            console.log("[socket] connected", frame);

            // subscribe to per-user topic; extract username from token payload (or frame.user if server set it)
            let username: string | null = null;
            try {
                const payload = JSON.parse(atob(token.split(".")[1]));
                username = payload.sub || payload.username || null;
            } catch (e) {
                username = null;
            }
            if (username) {
                client!.subscribe(`/topic/user/${username}`, (message) => {
                    try {
                        const body = message.body ? JSON.parse(message.body) : null;
                        onMessage(body);
                    } catch (err) {
                        console.error("[socket] parse error", err);
                    }
                });
            } else {
                console.warn("[socket] no username extracted from token — no user subscription");
            }
        },
        onStompError: (frame) => {
            console.error("[socket] STOMP error", frame);
        },
        onDisconnect: () => {
            console.log("[socket] disconnected");
        }
    });

    client.activate();
}

export function disconnectSocket() {
    if (client) {
        client.deactivate();
        client = null;
    }
}
