import axios from "axios";
const API_URL = import.meta.env.VITE_API_URL;

const axiosInstance = axios.create({
    baseURL: API_URL,
    timeout: 120000,
    withCredentials: true
});

axiosInstance.interceptors.response.use(

    response => response,

    async error => {

        if (!error.response) {
            return Promise.reject(error);
        }

        const originalRequest = error.config;

        const isExcluded = [
            "/user/login",
            "/login/v1/loginProc",
            "/auth/v1/refresh",
            "/auth/v1/password/validate"
        ].some(url =>
            originalRequest.url?.includes(url)
        );

        if (
            error.response.status === 401 &&
            !originalRequest._retry &&
            !isExcluded
        ) {

            originalRequest._retry = true;

            try {

                await refreshClient.post(
                    "/auth/v1/refresh"
                );

                return axiosInstance(
                    originalRequest
                );

            } catch (e) {

                window.location.href = "/login";

                return Promise.reject(e);
            }
        }

        return Promise.reject(error);
    }
);

export default axiosInstance;