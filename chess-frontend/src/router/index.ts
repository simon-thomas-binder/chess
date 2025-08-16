// src/router/index.ts
import { createRouter, createWebHistory } from "vue-router";
import Login from "../views/Login.vue";
import Register from "../views/Register.vue";
import Lobby from "../views/Lobby.vue"; // Dummy-Seite oder später
import NotFound from "../views/NotFound.vue";
import CustomGame from "../views/CustomGame.vue";
import Friends from "../views/Friends.vue";
import Profile from "../views/Profile.vue";

const routes = [
    { path: "/login", name: "Login", component: Login },
    { path: "/register", name: "Register", component: Register },
    { path: "/lobby", name: "Lobby", component: Lobby, meta: { requiresAuth: true } },
    { path: "/custom-game", name: "CustomGame", component: CustomGame },
    { path: "/profile", name: "Profile", component: Profile, meta: { requiresAuth: true } },
    { path: "/friends", name: "Friends", component: Friends, meta: { requiresAuth: true } },
    { path: "/", redirect: "/lobby" },
    { path: "/:pathMatch(.*)*", name: "NotFound", component: NotFound },
];

const router = createRouter({ history: createWebHistory(), routes });

router.beforeEach((to, _from, next) => {
    const token = localStorage.getItem("token");
    if (!token && to.name === "NotFound") {
        return next({ name: "Login" });
    }
    if (to.meta.requiresAuth && !token) {
        return next({ name: "Login", query: { next: to.fullPath } });
    }
    if ((to.name === "Login" || to.name === "Register") && token) {
        return next({ name: "Lobby" });
    }
    next();
});

export default router;
