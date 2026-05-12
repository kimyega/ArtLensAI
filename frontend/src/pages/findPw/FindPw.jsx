import React, { useState } from 'react';
import { Link } from 'react-router-dom';
import { motion } from 'framer-motion';
import './FindPw.css';

const FindPw = () => {
    const [email, setEmail] = useState('');

    const handleSubmit = (e) => {
        e.preventDefault();
        console.log("비밀번호 재설정 링크 요청:", email);
        // 🚀 나중에 여기에 백엔드로 이메일을 보내는 코드를 작성합니다.
        alert("입력하신 이메일로 비밀번호 재설정 링크를 발송했습니다.");
    };

    return (
        // 🌟 고급스러운 페이드 & 살짝 떠오르기 애니메이션 유지
        <div className="login-wrapper">
            {/* LEFT SECTION */}
            <div className="left-section">
                <Link to="/" className="logo">
                    <div className="logo-box"></div>
                    <span>ArtLens ai</span>
                </Link>
                <div className="visual-content">
                    <h1 className="visual-title">계정 복구,<br /><span>AI</span>와 함께 안전하게</h1>
                    <p className="visual-desc">
                        ArtLens ai는 당신의 소중한 예술적 기록과 계정 안전을 최우선으로 생각합니다.
                        가입하신 이메일로 비밀번호를 재설정하세요.
                    </p>
                </div>
                <div className="visual-footer">
                    SECURE ACCOUNT RECOVERY
                </div>
            </div>

            {/* RIGHT SECTION (Forgot Password Form) */}
            <div className="right-section">
                <div className="login-container">
                    <div className="login-header">
                        <h2>비밀번호 찾기</h2>
                        <p>가입하신 이메일 주소를 입력해 주세요.<br />비밀번호 재설정 링크를 보내드립니다.</p>
                    </div>

                    <form onSubmit={handleSubmit}>
                        <div className="form-group">
                            <input
                                type="email"
                                className="form-input"
                                placeholder="example@artlens.ai"
                                value={email}
                                onChange={(e) => setEmail(e.target.value)}
                                required
                            />
                        </div>

                        <button type="submit" className="btn-login">
                            비밀번호 재설정 링크 받기
                        </button>
                    </form>

                    <div className="auth-links">
                        {/* 🌟 <a> 태그를 <Link>로, SVG 속성들을 camelCase로 변경했습니다 */}
                        <Link to="/login">
                            <svg
                                width="16"
                                height="16"
                                viewBox="0 0 24 24"
                                fill="none"
                                stroke="currentColor"
                                strokeWidth="2"
                                strokeLinecap="round"
                                strokeLinejoin="round"
                            >
                                <line x1="19" y1="12" x2="5" y2="12"></line>
                                <polyline points="12 19 5 12 12 5"></polyline>
                            </svg>
                            로그인으로 돌아가기
                        </Link>
                    </div>
                </div>
            </div>
        </div>
    );
};

export default FindPw;