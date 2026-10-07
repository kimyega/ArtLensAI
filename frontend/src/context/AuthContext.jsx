import {
    createContext,
    useContext,
    useEffect,
    useState
} from 'react';

import axiosInstance
    from '../api/axiosInstance';

const AuthContext =
    createContext(null);

export const AuthProvider = ({
                                 children
                             }) => {

    const [isLoggedIn, setIsLoggedIn] =
        useState(false);

    const [userInfo, setUserInfo] =
        useState({
            nickname: '',
            initial: ''
        });

    const [loading, setLoading] =
        useState(true);

    const fetchUserInfo =
        async () => {

            try {

                const checkResponse =
                    await axiosInstance.get(
                        '/auth/v1/check'
                    );

                const loggedIn =
                    checkResponse.data.data.loggedIn;

                if (!loggedIn) {

                    setIsLoggedIn(false);

                    setUserInfo({
                        nickname: '',
                        initial: ''
                    });

                    return;
                }

                const userResponse =
                    await axiosInstance.post(
                        '/user/v1/userInfo'
                    );

                const nickname =
                    userResponse.data.data.nickname;

                setIsLoggedIn(true);

                setUserInfo({
                    nickname,
                    initial:
                        nickname
                            ?.charAt(0)
                            .toUpperCase()
                        || 'U'
                });

            } catch (error) {

                console.error(error);

                setIsLoggedIn(false);

                setUserInfo({
                    nickname: '',
                    initial: ''
                });

            } finally {

                setLoading(false);
            }
        };

    const logout =
        async () => {

            try {

                await axiosInstance.post(
                    '/auth/v1/logout'
                );

            } catch (error) {

                console.error(error);

            } finally {

                setIsLoggedIn(false);

                setUserInfo({
                    nickname: '',
                    initial: ''
                });
            }
        };

    useEffect(() => {

        void fetchUserInfo();

    }, []);

    return (
        <AuthContext.Provider
            value={{
                isLoggedIn,
                userInfo,
                loading,
                fetchUserInfo,
                logout
            }}
        >
            {children}
        </AuthContext.Provider>
    );
};

export const useAuth = () =>
    useContext(AuthContext);