// src/api/axiosInstance.js
import axios from 'axios';

// 1. axios 인스턴스 생성
const axiosInstance = axios.create({
    baseURL: 'http://localhost:8080', // 백엔드 기본 URL
    timeout: 5000
});

// 2. 요청 인터셉터(Request Interceptor)
axiosInstance.interceptors.request.use(
    (config) => {
        const token = localStorage.getItem('token');
        if (token) {
            config.headers.Authorization = `Bearer ${token}`;
        } else {
            delete config.headers.Authorization;
        }
        return config;
    },
    (error) => Promise.reject(error)
);

export default axiosInstance;