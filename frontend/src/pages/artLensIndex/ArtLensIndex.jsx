import React, { useState, useEffect, useRef } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import { motion } from 'framer-motion';

import './ArtLensIndex.css';

import {
    useAuth
} from '../../context/AuthContext';

const ArtLensIndex = () => {
    const [isModalOpen, setIsModalOpen] = useState(false);
    const [isDropdownOpen, setIsDropdownOpen] = useState(false);

    const dropdownRef = useRef(null);
    const navigate = useNavigate();

    const {
        isLoggedIn,
        userInfo,
        loading,
        logout
    } = useAuth();

    // 1. 로그인 사용자 정보 조회


    // 2. 드롭다운 외부 클릭 감지
    useEffect(() => {

        const handleClickOutside = (event) => {

            if (
                dropdownRef.current &&
                !dropdownRef.current.contains(
                    event.target
                )
            ) {

                setIsDropdownOpen(false);
            }
        };

        document.addEventListener(
            'mousedown',
            handleClickOutside
        );

        return () => {

            document.removeEventListener(
                'mousedown',
                handleClickOutside
            );
        };

    }, []);

    if (loading) {
        return null;
    }

    const handleAnalyzeClick = () => {
        if (isLoggedIn) {
            navigate('/analysis');
        } else {
            setIsModalOpen(true);
        }
    };

    // 3. 로그아웃 처리
    const handleLogout =
        async () => {

            await logout();

            setIsDropdownOpen(false);

            navigate('/');
        };

    const closeModal = () => setIsModalOpen(false);

    const handleOverlayClick = (e) => {
        if (e.target === e.currentTarget) closeModal();
    };

    return (
        <motion.div
            className="artlens-wrapper"
            initial={{ opacity: 0, y: 20 }}
            animate={{ opacity: 1, y: 0 }}
            transition={{ duration: 0.8, ease: "easeOut" }}
        >
            <nav>
                <Link to="/" className="logo">
                    <div className="logo-box"></div>
                    <span>ArtLens ai</span>
                </Link>

                <div style={{ display: 'flex', gap: '20px', alignItems: 'center' }}>
                    {isLoggedIn ? (
                        <div className="profile-menu-container" ref={dropdownRef}>
                            <button
                                className="profile-trigger"
                                onClick={() => setIsDropdownOpen(prev => !prev)}
                            >
                                <div className="profile-avatar">{userInfo.initial}</div>
                                <span className="profile-name">{userInfo.nickname}님</span>
                            </button>

                            {isDropdownOpen && (
                                <div className="dropdown-menu-index">
                                    <Link to="/history" className="dropdown-item">
                                        <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round"><rect x="3" y="3" width="18" height="18" rx="2" ry="2"></rect><circle cx="8.5" cy="8.5" r="1.5"></circle><polyline points="21 15 16 10 5 21"></polyline></svg>
                                        내 분석 기록
                                    </Link>
                                    <Link to="/mypage" className="dropdown-item">
                                        <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round"><circle cx="12" cy="12" r="3"></circle><path d="M19.4 15a1.65 1.65 0 0 0 .33 1.82l.06.06a2 2 0 0 1 0 2.83 2 2 0 0 1-2.83 0l-.06-.06a1.65 1.65 0 0 0-1.82-.33 1.65 1.65 0 0 0-1 1.51V21a2 2 0 0 1-2 2 2 2 0 0 1-2-2v-.09A1.65 1.65 0 0 0 9 19.4a1.65 1.65 0 0 0-1.82.33l-.06.06a2 2 0 0 1-2.83 0 2 2 0 0 1 0-2.83l.06-.06a1.65 1.65 0 0 0 .33-1.82 1.65 1.65 0 0 0-1.51-1H3a2 2 0 0 1-2-2 2 2 0 0 1 2-2h.09A1.65 1.65 0 0 0 4.6 9a1.65 1.65 0 0 0-.33-1.82l-.06-.06a2 2 0 0 1 0-2.83 2 2 0 0 1 2.83 0l.06.06a1.65 1.65 0 0 0 1.82.33H9a1.65 1.65 0 0 0 1-1.51V3a2 2 0 0 1 2-2 2 2 0 0 1 2 2v.09a1.65 1.65 0 0 0 1 1.51 1.65 1.65 0 0 0 1.82-.33l.06-.06a2 2 0 0 1 2.83 0 2 2 0 0 1 0 2.83l-.06.06a1.65 1.65 0 0 0-.33 1.82V9a1.65 1.65 0 0 0 1.51 1H21a2 2 0 0 1 2 2 2 2 0 0 1-2 2h-.09a1.65 1.65 0 0 0-1.51 1z"></path></svg>
                                        마이 페이지
                                    </Link>
                                    <div className="dropdown-divider"></div>
                                    <button onClick={handleLogout} className="dropdown-item logout-item">
                                        <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round"><path d="M9 21H5a2 2 0 0 1-2-2V5a2 2 0 0 1 2-2h4"></path><polyline points="16 17 21 12 16 7"></polyline><line x1="21" y1="12" x2="9" y2="12"></line></svg>
                                        로그아웃
                                    </button>
                                </div>
                            )}
                        </div>
                    ) : (
                        <>
                            <Link to="/login" style={{ color: '#fff', textDecoration: 'none', fontSize: '14px', opacity: 0.8 }}>로그인</Link>
                            <Link to="/signup" style={{ color: '#fff', textDecoration: 'none', fontSize: '14px', fontWeight: 700 }}>회원가입</Link>
                        </>
                    )}
                </div>
            </nav>

            <div className="hero-container">
                <div className="hero-content">
                    <div className="hero-badge">✦ AI 기반 정밀 화풍 분석</div>
                    <h1 className="hero-title">
                        내 그림 속 숨은 작가를<br />AI 렌즈로 찾아드립니다
                    </h1>
                    <p className="hero-sub">
                        이미지 한 장으로 시작하는 예술적 여정.<br />지금 당신의 화풍을 분석하거나 2,400명의 작가를 탐색해 보세요.
                    </p>

                    <div className="cta-group">
                        <button className="btn btn-analyze" onClick={handleAnalyzeClick}>
                            <span>✨</span> 지금 분석하기
                        </button>
                        <button className="btn btn-explore" onClick={() => navigate('/artist')}>
                            <span>🔍</span> 작가 탐색하기
                        </button>
                    </div>
                </div>
                <div className="scroll-indicator">SCROLL TO EXPLORE</div>
            </div>

            <div className={`modal-overlay ${isModalOpen ? 'active' : ''}`} onClick={handleOverlayClick}>
                <div className="modal-content">
                    <button className="btn-close" onClick={closeModal}>×</button>
                    <span className="modal-icon">🔐</span>
                    <h2 className="modal-title">로그인이 필요합니다</h2>
                    <p className="modal-sub">AI 화풍 분석을 시작하고 당신의 예술적 여정을 기록하려면 로그인해 주세요.</p>
                    <Link to="/login" className="btn-modal-login">
                        <span>🎨</span> ArtLens ai 로그인하기
                    </Link>
                    <div className="modal-footer">
                        아직 계정이 없으신가요? <Link to="/signup">회원가입</Link>
                    </div>
                </div>
            </div>
        </motion.div>
    );
};

export default ArtLensIndex;