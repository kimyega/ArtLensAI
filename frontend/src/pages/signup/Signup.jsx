import React, { useState } from 'react';
import { Link } from 'react-router-dom';
import { motion } from 'framer-motion';
import './Signup.css';

const Signup = () => {
    // 회원가입 폼 데이터 상태 관리
    const [email, setEmail] = useState('');
    const [nickname, setNickname] = useState('');
    const [password, setPassword] = useState('');
    const [confirmPassword, setConfirmPassword] = useState('');
    const [agreeTerms, setAgreeTerms] = useState(false);

    // 폼 제출 핸들러 (나중에 스프링 부트로 보낼 곳)
    const handleSubmit = (e) => {
        e.preventDefault();
        if (password !== confirmPassword) {
            alert("비밀번호가 일치하지 않습니다.");
            return;
        }
        console.log("회원가입 시도:", { email, nickname, password, agreeTerms });
    };

    return (
        <div className="login-wrapper">
            {/* LEFT SECTION */}
            <div className="left-section">
                <Link to="/" className="logo">
                    <div className="logo-box"></div>
                    <span>ArtLens ai</span>
                </Link>
                <div className="visual-content">
                    <h1 className="visual-title">새로운 <span>예술적 관점</span>을<br />발견할 시간입니다</h1>
                    <p className="visual-desc">간단한 가입만으로 수천 명의 작가 데이터 기반 화풍 분석과 나만의 히스토리 기록을 시작할 수 있습니다.</p>
                </div>
                <div className="visual-footer">
                    CREATE YOUR ACCOUNT
                </div>
            </div>

            {/* RIGHT SECTION (Sign Up Form) */}
            <div className="right-section">
                <div className="login-container">
                    <div className="login-header">
                        <h2>회원가입</h2>
                        <p>ArtLens ai의 멤버가 되어보세요</p>
                    </div>

                    <form onSubmit={handleSubmit}>
                        <div className="form-group">
                            <label className="form-label">이메일 주소</label>
                            <input
                                type="email"
                                className="form-input"
                                placeholder="example@artlens.ai"
                                value={email}
                                onChange={(e) => setEmail(e.target.value)}
                                required
                                maxLength="100"
                            />
                        </div>

                        <div className="form-group">
                            <label className="form-label">닉네임</label>
                            <input
                                type="text"
                                className="form-input"
                                placeholder="예: Karis"
                                value={nickname}
                                onChange={(e) => setNickname(e.target.value)}
                                required
                                maxLength="50"
                            />
                        </div>

                        <div className="form-group">
                            <label className="form-label">비밀번호</label>
                            <input
                                type="password"
                                className="form-input"
                                placeholder="8자 이상 (영문, 숫자, 특수문자 포함)"
                                value={password}
                                onChange={(e) => setPassword(e.target.value)}
                                required
                            />
                        </div>

                        <div className="form-group">
                            <label className="form-label">비밀번호 확인</label>
                            <input
                                type="password"
                                className="form-input"
                                placeholder="비밀번호를 다시 입력해 주세요"
                                value={confirmPassword}
                                onChange={(e) => setConfirmPassword(e.target.value)}
                                required
                            />
                        </div>

                        <label className="terms-group">
                            <input
                                type="checkbox"
                                checked={agreeTerms}
                                onChange={(e) => setAgreeTerms(e.target.checked)}
                                required
                            />
                            <span>ArtLens ai의 <Link to="#">이용약관</Link> 및 <Link to="#">개인정보처리방침</Link>에 동의합니다.</span>
                        </label>

                        <button type="submit" className="btn-login">계정 만들기</button>
                    </form>

                    <div className="divider">
                        <span>간편 회원가입</span>
                    </div>

                    <div className="social-group">
                        <button className="btn-social social-google">
                            <svg viewBox="0 0 488 512"><path d="M488 261.8C488 403.3 391.1 504 248 504 110.8 504 0 393.2 0 256S110.8 8 248 8c66.8 0 123 24.5 166.3 64.9l-67.5 64.9C258.5 52.6 94.3 116.6 94.3 256c0 86.5 69.1 156.6 153.7 156.6 98.2 0 135-70.4 140.8-106.9H248v-85.3h236.1c2.3 12.7 3.9 24.9 3.9 41.4z"/></svg>
                            Google
                        </button>
                        <button className="btn-social social-kakao">
                            <svg viewBox="0 0 24 24" fill="currentColor"><path d="M12 3c-5.523 0-10 3.582-10 8c0 2.91 1.848 5.462 4.602 6.917l-.872 3.195a.5.5 0 0 0 .736.568l3.766-2.511a11.97 11.97 0 0 0 1.768.131c5.523 0 10-3.582 10-8s-4.477-8-10-8Z"/></svg>
                            카카오톡
                        </button>
                    </div>

                    <div className="auth-links">
                        이미 계정이 있으신가요? <Link to="/login">로그인</Link>
                    </div>
                </div>
            </div>
        </div>
    );
};

export default Signup;