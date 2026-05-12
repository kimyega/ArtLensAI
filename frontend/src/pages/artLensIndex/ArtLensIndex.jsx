import React, { useState } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import { motion } from 'framer-motion';
import './ArtLensIndex.css';

const ArtLensIndex = () => {
    const [isLoggedIn, setIsLoggedIn] = useState(false);
    const [isModalOpen, setIsModalOpen] = useState(false);

    // 🌟 2. 페이지 이동을 도와주는 navigate 함수 준비
    const navigate = useNavigate();

    const handleAnalyzeClick = () => {
        if (isLoggedIn) {
            // 🌟 3. window.location.href 대신 navigate 사용 (새로고침 방지)
            navigate('/analyze');
        } else {
            setIsModalOpen(true);
        }
    };

    const closeModal = () => {
        setIsModalOpen(false);
    };

    const handleOverlayClick = (e) => {
        if (e.target === e.currentTarget) {
            closeModal();
        }
    };

    return (
        <motion.div
            className="artlens-wrapper"
            initial={{ opacity: 0, y: 20 }}       // 처음 등장할 때만 작동
            animate={{ opacity: 1, y: 0 }}        // 위로 스르륵!
            transition={{ duration: 0.8, ease: "easeOut" }}
        >
            <nav>
                {/* 🌟 4. <a> 태그를 <Link to="..."> 로 변경 */}
                <Link to="/" className="logo">
                    <div className="logo-box"></div>
                    <span>ArtLens ai</span>
                </Link>
                <div style={{ display: 'flex', gap: '20px' }}>
                    <Link to="/login" style={{ color: '#fff', textDecoration: 'none', fontSize: '14px', opacity: 0.8 }}>
                        로그인
                    </Link>
                    <Link to="/signup" style={{ color: '#fff', textDecoration: 'none', fontSize: '14px', fontWeight: 700 }}>
                        회원가입
                    </Link>
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
                        <button className="btn btn-explore">
                            <span>🔍</span> 작가 탐색하기
                        </button>
                    </div>
                </div>

                <div className="scroll-indicator">SCROLL TO EXPLORE</div>
            </div>

            {/* 모달 영역 */}
            <div
                className={`modal-overlay ${isModalOpen ? 'active' : ''}`}
                onClick={handleOverlayClick}
            >
                <div className="modal-content">
                    <button className="btn-close" onClick={closeModal}>×</button>

                    <span className="modal-icon">🔐</span>
                    <h2 className="modal-title">로그인이 필요합니다</h2>
                    <p className="modal-sub">
                        AI 화풍 분석을 시작하고 당신의 예술적 여정을 기록하려면 로그인해 주세요.
                    </p>

                    {/* 🌟 5. 모달 안의 로그인 버튼도 Link로 변경 */}
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