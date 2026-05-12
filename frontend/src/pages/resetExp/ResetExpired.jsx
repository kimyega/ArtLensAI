import React from 'react';
import { Link } from 'react-router-dom';
import './ResetExpired.css';

const ResetExpired = () => {
    return (
        <div className="link-expired-wrapper">
            <div className="left-section">
                <Link to="/" className="logo">
                    <div className="logo-box"></div>
                    <span>ArtLens ai</span>
                </Link>
                <div className="visual-content">
                    <h1 className="visual-title">계정 보호를 위한<br /><span>보안 조치</span></h1>
                    <p className="visual-desc">ArtLens ai는 회원님의 계정을 안전하게 보호하기 위해 비밀번호 재설정 링크에 유효기간을 적용하고 있습니다.</p>
                </div>
                <div className="visual-footer">
                    SECURITY & PROTECTION
                </div>
            </div>

            <div className="right-section">
                <div className="login-container">

                    <div className="alert-icon">
                        {/* React 문법에 맞게 SVG 속성 수정 (strokeWidth 등) */}
                        <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round">
                            <circle cx="12" cy="12" r="10"></circle>
                            <line x1="12" y1="8" x2="12" y2="12"></line>
                            <line x1="12" y1="16" x2="12.01" y2="16"></line>
                        </svg>
                    </div>

                    <div className="login-header">
                        <h2>링크가 만료되었습니다</h2>
                        <p>보안을 위해 발급된 지 시간이 초과되었거나,<br />이미 사용된 비밀번호 재설정 링크입니다.<br />새로운 링크를 다시 요청해 주세요.</p>
                    </div>

                    {/* 라우터 주소는 실제 비밀번호 찾기 페이지 경로로 맞춰주세요 */}
                    <Link to="/find-pw" className="btn-primary">
                        비밀번호 재설정 다시 요청하기
                    </Link>

                    <div className="auth-links">
                        <Link to="/login">
                            <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round">
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

export default ResetExpired;