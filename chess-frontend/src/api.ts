import axios, {AxiosHeaders} from "axios";
import router from "./router"; // falls du router redirect bei 401 möchtest

const api = axios.create({
    baseURL: import.meta.env.VITE_API_BASE || "/api",
    withCredentials: false // auf true stellen, wenn später HttpOnly-Cookies verwendet werden
});

// Request interceptor: attach Bearer if present
api.interceptors.request.use(config => {
    const token = localStorage.getItem("token");
    if (token) {
        config.headers = new AxiosHeaders({
            ...config.headers,
            Authorization: `Bearer ${token}`
        });
    }
    return config;
}, err => Promise.reject(err));

// Response interceptor: global 401 handling
api.interceptors.response.use(
    (r) => r,
    (err) => {
        if (err.response?.status === 401) {
            localStorage.removeItem("token");
            router.push({
                name: "Login",
                query: { next: router.currentRoute.value.fullPath },
            });
        }
        return Promise.reject(err);
    }
);

export default api;
