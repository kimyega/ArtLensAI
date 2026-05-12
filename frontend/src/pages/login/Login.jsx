import React, { useState } from 'react';
import './Login.css';
import { motion } from 'framer-motion';
import { Link } from 'react-router-dom';

const Login = () => {
    // 사용자가 입력한 이메일과 비밀번호를 저장할 State
    const [email, setEmail] = useState('');
    const [password, setPassword] = useState('');

    // 폼 제출(로그인 버튼 클릭) 시 실행될 함수
    const handleSubmit = (e) => {
        e.preventDefault(); // 새로고침 방지
        console.log("로그인 시도:", email, password);
        // 🚀 나중에 여기에 Spring Boot로 데이터를 보내는 코드가 들어갈 자리입니다!
    };

    return (
        <div className="login-wrapper">
            {/* LEFT SECTION (Visual & Branding) */}
            <div className="left-section">
                <a href="/" className="logo">
                    <div className="logo-box"></div>
                    <span>ArtLens ai</span>
                </a>
                <div className="visual-content">
                    <h1 className="visual-title">당신의 예술적 여정,<br /><span>AI</span> 렌즈로 기록하다</h1>
                    <p className="visual-desc">로그인하고 이미지 한 장으로 시작되는 스마트 화풍 분석과 거장들과의 만남을 이어가세요.</p>
                </div>
                <div className="visual-footer">
                    ANALYSIS · HISTORY · EXPLORE
                </div>
            </div>

            {/* RIGHT SECTION (Login Form) */}
            <div className="right-section">
                <div className="login-container">
                    <div className="login-header">
                        <h2>로그인</h2>
                        <p>ArtLens ai 커뮤니티에 다시 오신 것을 환영합니다</p>
                    </div>

                    {/* onSubmit 이벤트 연결 */}
                    <form onSubmit={handleSubmit}>
                        <div className="form-group">
                            <input
                                type="email"
                                className="form-input"
                                placeholder="이메일 주소"
                                value={email}
                                onChange={(e) => setEmail(e.target.value)}
                                required
                            />
                        </div>
                        <div className="form-group">
                            <input
                                type="password"
                                className="form-input"
                                placeholder="비밀번호"
                                value={password}
                                onChange={(e) => setPassword(e.target.value)}
                                required
                            />
                        </div>

                        <button type="submit" className="btn-login">서비스 시작하기</button>
                    </form>

                    {/* 오른쪽 회원가입 버튼 부분 */}
                    <div className="auth-links">
                        <Link to="/find-pw">비밀번호 찾기</Link>
                        <span className="dot">·</span>
                        <Link to="/signup">회원가입</Link>
                    </div>

                    <div className="divider">
                        <span>간편 로그인</span>
                    </div>

                    <div className="social-group">
                        <button className="btn-social social-google">
                            <svg viewBox="0 0 488 512">
                                <path d="M488 261.8C488 403.3 391.1 504 248 504 110.8 504 0 393.2 0 256S110.8 8 248 8c66.8 0 123 24.5 166.3 64.9l-67.5 64.9C258.5 52.6 94.3 116.6 94.3 256c0 86.5 69.1 156.6 153.7 156.6 98.2 0 135-70.4 140.8-106.9H248v-85.3h236.1c2.3 12.7 3.9 24.9 3.9 41.4z"/>
                            </svg>
                            Google
                        </button>
                        <button className="btn-social social-kakao">
                            <svg viewBox="0 0 24 24" fill="currentColor">
                                <path d="M12 3c-5.523 0-10 3.582-10 8c0 2.91 1.848 5.462 4.602 6.917l-.872 3.195a.5.5 0 0 0 .736.568l3.766-2.511a11.97 11.97 0 0 0 1.768.131c5.523 0 10-3.582 10-8s-4.477-8-10-8Z"/>
                            </svg>
                            카카오톡
                        </button>
                    </div>
                </div>
            </div>
        </div>
    );
};

export default Login;