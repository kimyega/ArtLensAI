import axios from 'axios';

const axiosInstance = axios.create({
    baseURL: 'http://localhost:8080',
    timeout: 5000
});

// =========================
// 요청 인터셉터
// =========================
axiosInstance.interceptors.request.use(
    (config) => {

        const token = localStorage.getItem('token');

        if (token) {
            config.headers.Authorization =
                `Bearer ${token}`;
        }

        return config;
    },
    (error) => Promise.reject(error)
);

// =========================
// 응답 인터셉터
// =========================
axiosInstance.interceptors.response.use(

    // 정상 응답
    (response) => response,

    // 에러 응답
    async (error) => {

        const originalRequest = error.config;

        // AccessToken 만료
        if (
            error.response?.status === 401 &&
            !originalRequest._retry
        ) {

            originalRequest._retry = true;

            try {

                const refreshToken =
                    localStorage.getItem('refreshToken');

                // refresh API 요청
                const response = await axios.post(
                    'http://localhost:8080/user/refresh',
                    { refreshToken }
                );

                const newAccessToken =
                    response.data.token;

                // 새 토큰 저장
                localStorage.setItem(
                    'token',
                    newAccessToken
                );

                // 원래 요청 헤더 교체
                originalRequest.headers.Authorization =
                    `Bearer ${newAccessToken}`;

                // 원래 요청 재시도
                return axiosInstance(originalRequest);

            } catch (refreshError) {

                // refresh도 실패
                localStorage.removeItem('token');
                localStorage.removeItem('refreshToken');

                window.location.href = '/login';

                return Promise.reject(refreshError);
            }
        }

        return Promise.reject(error);
    }
);

export default axiosInstance;