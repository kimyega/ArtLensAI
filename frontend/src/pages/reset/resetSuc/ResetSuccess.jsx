import React from 'react';
import { Link } from 'react-router-dom';
import './ResetSuccess.css';

const ResetSuccess = () => {
    return (
        <div className="reset-complete-wrapper">
            <div className="left-section">
                <Link to="/" className="logo">
                    <div className="logo-box"></div>
                    <span>ArtLens ai</span>
                </Link>
                <div className="visual-content">
                    <h1 className="visual-title">성공적으로<br /><span>복구</span>되었습니다</h1>
                    <p className="visual-desc">이제 계정이 다시 안전해졌습니다. 새로운 비밀번호로 로그인하고 ArtLens ai의 다채로운 기능을 경험해 보세요.</p>
                </div>
                <div className="visual-footer">
                    ACCOUNT SECURED
                </div>
            </div>

            <div className="right-section">
                <div className="login-container">

                    <div className="success-icon">
                        {/* React 문법에 맞게 stroke-width -> strokeWidth 등으로 수정되었습니다. */}
                        <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2.5" strokeLinecap="round" strokeLinejoin="round">
                            <polyline points="20 6 9 17 4 12"></polyline>
                        </svg>
                    </div>

                    <div className="login-header">
                        <h2>비밀번호 변경 완료</h2>
                        <p>비밀번호가 안전하게 변경되었습니다.<br />새로운 비밀번호로 다시 로그인해 주세요.</p>
                    </div>

                    <Link to="/login" className="btn-primary">
                        로그인하러 가기
                    </Link>

                </div>
            </div>
        </div>
    );
};

export default ResetSuccess;