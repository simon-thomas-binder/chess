// src/services/authService.ts
import api from "../api";

export interface LoginPayload { username: string; password: string; }
export interface RegisterPayload { username: string; displayname: string; password: string; }

export async function login(payload: LoginPayload) {
    // Backend: POST /api/auth/login -> { token }
    const { data } = await api.post<{ token: string }>("/auth/login", payload);
    return data;
}

export async function register(payload: RegisterPayload) {
    // Backend: POST /api/auth/register -> { token }
    const { data } = await api.post<{ token: string }>("/auth/register", payload);
    return data;
}

