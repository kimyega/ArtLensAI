import React from 'react';
import {
    Link,
    useLocation,
    useNavigate
} from 'react-router-dom';

import './Header.css';
import axiosInstance from '../../api/axiosInstance';

import {
    useAuth
} from '../../context/AuthContext';

const Header = () => {

    const location =
        useLocation();

    const navigate =
        useNavigate();

    // 현재 경로에 따라 active 클래스 부여
    const isActive = (path) =>
        location.pathname.startsWith(path)
            ? 'active'
            : '';

    const {
        isLoggedIn,
        userInfo,
        logout
    } = useAuth();

    const handleLogout =
        async () => {

            await logout();

            navigate('/');
        };

    return (
        <header className="navbar">

            <Link
                to="/"
                className="logo"
            >
                <div className="logo-box"></div>
                <span>ArtLens ai</span>
            </Link>

            <ul className="nav-menu">

                <li>
                    <Link
                        to="/analysis"
                        className={isActive('/analysis')}
                    >
                        이미지 분석
                    </Link>
                </li>

                <li>
                    <Link
                        to="/style-map"
                        className={isActive('/style-map')}
                    >
                        Style Map
                        <span className="badge-new">
                            2D
                        </span>
                    </Link>
                </li>

                <li>
                    <Link
                        to="/artist"
                        className={isActive('/artist')}
                    >
                        작가 탐색
                    </Link>
                </li>

                <li>
                    <Link
                        to="/history"
                        className={isActive('/history')}
                    >
                        분석 기록
                    </Link>
                </li>

            </ul>

            {isLoggedIn ? (

                <div className="user-profile">

                    <div className="avatar">
                        {userInfo.initial}
                    </div>

                    <span className="user-name">
                        {userInfo.nickname}
                    </span>

                    <svg
                        width="14"
                        height="14"
                        viewBox="0 0 24 24"
                        fill="none"
                        stroke="currentColor"
                        strokeWidth="2"
                        className="chevron-icon"
                    >
                        <polyline
                            points="6 9 12 15 18 9"
                        />
                    </svg>

                    <div className="dropdown-menu">

                        <Link
                            to="/mypage"
                            className="dropdown-item"
                        >
                            마이페이지
                        </Link>

                        <div className="dropdown-divider"></div>

                        <button
                            onClick={handleLogout}
                            className="dropdown-item logout-text"
                        >
                            로그아웃
                        </button>

                    </div>

                </div>

            ) : (

                <div className="auth-buttons">

                    <Link to="/login">
                        로그인
                    </Link>

                    <Link to="/signup">
                        회원가입
                    </Link>

                </div>

            )}

        </header>
    );
};

export default Header;