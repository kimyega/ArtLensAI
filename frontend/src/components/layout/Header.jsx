import React from 'react';
import { Link, useLocation } from 'react-router-dom';
import './Header.css';

const Header = () => {
    const location = useLocation();

    // 현재 경로에 따라 active 클래스를 부여하는 함수
    const isActive = (path) => location.pathname === path ? 'active' : '';

    return (
        <header className="navbar">
            <Link to="/" className="logo">
                <div className="logo-box"></div>
                <span>ArtLens ai</span>
            </Link>

            <ul className="nav-menu">
                <li>
                    <Link to="/analysis" className={isActive('/analysis')}>이미지 분석</Link>
                </li>
                <li>
                    <Link to="/style-map" className={isActive('/style-map')}>
                        Style Map <span className="badge-new">2D</span>
                    </Link>
                </li>
                <li><Link to="/artists" className={isActive('/artists')}>작가 탐색</Link></li>
                <li><Link to="/history" className={isActive('/history')}>분석 기록</Link></li>
            </ul>

            <div className="user-profile">
                <div className="avatar">K</div>
                <span className="user-name">Karis</span>
                <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" className="chevron-icon">
                    <polyline points="6 9 12 15 18 9"></polyline>
                </svg>

                <div className="dropdown-menu">
                    <Link to="/mypage" className="dropdown-item">마이페이지</Link>
                    <div className="dropdown-divider"></div>
                    <Link to="/login" className="dropdown-item logout-text">로그아웃</Link>
                </div>
            </div>
        </header>
    );
};

export default Header;