import React, { useState } from 'react';
import { Link } from 'react-router-dom';
import { motion } from 'framer-motion';
import './FindPw.css';

const FindPw = () => {
    const [email, setEmail] = useState('');
    const [emailError, setEmailError] = useState('');
    const [successMsg, setSuccessMsg] = useState('');

    // 이메일 정규식
    const validateEmail = (value = email) => {
        if (!value) {
            setEmailError('이메일을 입력해 주세요.');
            return false;
        }

        const emailRegex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;

        if (!emailRegex.test(value)) {
            setEmailError('올바른 이메일 형식을 입력해 주세요.');
            return false;
        }

        setEmailError('');
        return true;
    };

    // 비밀번호 찾기 요청
    const handleSubmit = async (e) => {
        e.preventDefault();
        setSuccessMsg('');

        const isValid = validateEmail();
        if (!isValid) return;

        try {
            // 1. 이메일 존재 여부 체크
            const res = await fetch("http://localhost:8080/user/checkEmail", {
                method: "POST",
                headers: { "Content-Type": "application/json" },
                body: JSON.stringify({ email })
            });

            const data = await res.json();

            // ❌ 가입 안된 이메일
            if (data.result === 1) {
                setEmailError("가입되지 않은 이메일입니다.");
                return;
            }

            // 2. 가입된 이메일 → 비밀번호 재설정 요청
            const resetRes = await fetch("http://localhost:8080/user/findPw", {
                method: "POST",
                headers: { "Content-Type": "application/json" },
                body: JSON.stringify({ email })
            });

            const resetData = await resetRes.json();

            if (resetData.result === 1) {
                setEmailError('');
                setSuccessMsg("비밀번호 재설정 링크를 이메일로 발송했습니다.");
            } else {
                setEmailError(resetData.msg || "요청 실패");
            }

        } catch (err) {
            setEmailError("서버 오류가 발생했습니다.");
        }
    };

    const handleEmailChange = (e) => {
        setEmail(e.target.value);
        setEmailError('');
        setSuccessMsg('');
    };

    return (
        <div className="findpw-wrapper">
            {/* LEFT */}
            <div className="left-section">
                <Link to="/" className="logo">
                    <div className="logo-box"></div>
                    <span>ArtLens ai</span>
                </Link>

                <div className="visual-content">
                    <h1 className="visual-title">
                        계정 복구,<br /><span>AI</span>와 함께 안전하게
                    </h1>
                    <p className="visual-desc">
                        가입된 이메일로 비밀번호 재설정 링크를 전송합니다.
                    </p>
                </div>

                <div className="visual-footer">SECURE ACCOUNT RECOVERY</div>
            </div>

            {/* RIGHT */}
            <div className="right-section">
                <div className="login-container">
                    <div className="login-header">
                        <h2>비밀번호 찾기</h2>
                        <p>가입한 이메일을 입력해 주세요</p>
                    </div>

                    <form onSubmit={handleSubmit}>
                        <div className="form-group">
                            <input
                                type="text"
                                className={`form-input ${emailError ? 'error' : ''}`}
                                placeholder="example@artlens.ai"
                                value={email}
                                onChange={handleEmailChange}
                                onBlur={() => validateEmail()}
                            />
                            {emailError && (
                                <p className="error-text">{emailError}</p>
                            )}
                            {successMsg && (
                                <p className="success-text">{successMsg}</p>
                            )}
                        </div>

                        <button type="submit" className="btn-login">
                            비밀번호 재설정 링크 받기
                        </button>
                    </form>

                    <div className="auth-links">
                        <Link to="/login">로그인으로 돌아가기</Link>
                    </div>
                </div>
            </div>
        </div>
    );
};

export default FindPw;