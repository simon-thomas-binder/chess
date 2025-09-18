// src/services/authService.ts
import api from "../api";

export interface LoginPayload { username: string; password: string; }
export interface RegisterPayload { username: string; displayname: string; password: string; }

// User structure
export type User = {
    username: String,
    displayname: String,
    createdAt: Date,
    rating: number,
}

//Sends login request and returns token
export async function login(payload: LoginPayload) {
    const { data } = await api.post<{ token: string }>("/auth/login", payload);
    return data;
}

// Sends register request and returns token
export async function register(payload: RegisterPayload) {
    const { data } = await api.post<{ token: string }>("/auth/register", payload);
    return data;
}

// Gets username by decoding token in local storage
export function getUserFromToken(): string | null {
    const token = localStorage.getItem("token");
    if (!token) return null;
    try {
        const payload = JSON.parse(atob(token.split(".")[1]));
        return payload.sub || payload.username || null;
    } catch {
        return null;
    }
}

// Gets user information
export async function getUser(): Promise<User> {
    const { data } = await api.get<User>("/auth");
    return data;
}

export async function getUserWithUsername(username: string): Promise<User> {
    console.log("The user to get has the username: " + username);
    const { data } = await api.get<User>("/auth/get/" + username);
    return data;
}
