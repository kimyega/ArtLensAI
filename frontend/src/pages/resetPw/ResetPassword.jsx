import React, { useState, useEffect } from 'react';
import { Link, useNavigate, useSearchParams } from 'react-router-dom';
import axios from 'axios';
import './ResetPassword.css';

const ResetPassword = () => {
    const [newPassword, setNewPassword] = useState('');
    const [confirmPassword, setConfirmPassword] = useState('');

    const [passwordError, setPasswordError] = useState('');
    const [confirmError, setConfirmError] = useState('');

    const [modalConfig, setModalConfig] = useState({
        isOpen: false,
        message: '',
        onConfirm: null
    });

    const [loading, setLoading] = useState(true);
    const [checked, setChecked] = useState(false);
    const [isValid, setIsValid] = useState(false);

    const [searchParams] = useSearchParams();
    const navigate = useNavigate();

    const token = searchParams.get("token");

    // 🔥 토큰 검증
    useEffect(() => {
        const run = async () => {

            if (!token) {
                setIsValid(false);
                setChecked(true);
                setLoading(false);
                return;
            }

            try {
                const res = await axios.get(
                    `http://localhost:8080/user/validateResetToken?token=${token}`
                );

                if (res.data.result === 1) {
                    setIsValid(true);
                } else {
                    setIsValid(false);
                }

            } catch (err) {
                setIsValid(false);
            } finally {
                setChecked(true);
                setLoading(false);
            }
        };

        run();
    }, [token]);

    // 🔥 invalid면 자동 이동
    useEffect(() => {
        if (checked && !isValid) {
            navigate('/reset-exp');
        }
    }, [checked, isValid, navigate]);

    const closeModal = () =>
        setModalConfig(prev => ({ ...prev, isOpen: false }));

    // 🔥 비밀번호 검증
    const validatePassword = (value = newPassword) => {
        const regex =
            /^(?=.*[a-zA-Z])(?=.*\d)(?=.*[!@#$%^&*()_+={}\[\]|\\:;"'<>,.?/-]).{8,}$/;

        if (!value) {
            setPasswordError('비밀번호를 입력해 주세요.');
            return false;
        }

        if (!regex.test(value)) {
            setPasswordError('영문, 숫자, 특수문자를 포함해 8자 이상이어야 합니다.');
            return false;
        }

        setPasswordError('');
        return true;
    };

    // 🔥 비밀번호 확인
    const validateConfirm = (value = confirmPassword) => {
        if (!value) {
            setConfirmError('비밀번호 확인을 입력해 주세요.');
            return false;
        }

        if (value !== newPassword) {
            setConfirmError('비밀번호가 일치하지 않습니다.');
            return false;
        }

        setConfirmError('');
        return true;
    };

    const handleSubmit = async (e) => {
        e.preventDefault();

        const isPwValid = validatePassword();
        const isConfirmValid = validateConfirm();

        if (!isPwValid || !isConfirmValid) return;

        try {
            await axios.post('http://localhost:8080/user/resetPassword', {
                token,
                newPassword
            });

            navigate('/reset-suc');

        } catch (err) {
            setModalConfig({
                isOpen: true,
                message: '서버 오류가 발생했습니다.',
                onConfirm: closeModal
            });
        }
    };

    // 🔥 로딩
    if (loading || !checked) {
        return <div className="loading-wrapper">로딩중...</div>;
    }

    // 🔥 정상 아닌 경우 UI 안그림 (redirect는 useEffect에서 처리)
    if (!isValid) return null;

    return (
        <>
            <div className="reset-wrapper">

                <div className="left-section">
                    <Link to="/" className="logo">
                        <div className="logo-box"></div>
                        <span>ArtLens ai</span>
                    </Link>

                    <div className="visual-content">
                        <h1 className="visual-title">
                            새로운 시작,<br /><span>AI</span>와 함께 안전하게
                        </h1>
                        <p className="visual-desc">
                            비밀번호를 안전하게 재설정하세요.
                        </p>
                    </div>

                    <div className="visual-footer">
                        SECURE PASSWORD RESET
                    </div>
                </div>

                <div className="right-section">

                    <div className="login-container">

                        <div className="login-header">
                            <h2>비밀번호 재설정</h2>
                            <p>새 비밀번호를 입력해 주세요</p>
                        </div>

                        <form onSubmit={handleSubmit}>

                            <div className="form-group">
                                <label className="form-label">새 비밀번호</label>
                                <input
                                    type="password"
                                    className={`form-input ${passwordError ? 'error' : ''}`}
                                    value={newPassword}
                                    onChange={(e) => {
                                        setNewPassword(e.target.value);
                                        if (passwordError) validatePassword(e.target.value);
                                    }}
                                    onBlur={() => validatePassword()}
                                />
                                {passwordError && (
                                    <p className="error-text">{passwordError}</p>
                                )}
                            </div>

                            <div className="form-group">
                                <label className="form-label">비밀번호 확인</label>
                                <input
                                    type="password"
                                    className={`form-input ${confirmError ? 'error' : ''}`}
                                    value={confirmPassword}
                                    onChange={(e) => {
                                        setConfirmPassword(e.target.value);
                                        if (confirmError) validateConfirm(e.target.value);
                                    }}
                                    onBlur={() => validateConfirm()}
                                />
                                {confirmError && (
                                    <p className="error-text">{confirmError}</p>
                                )}
                            </div>

                            <button type="submit" className="btn-login">
                                비밀번호 변경하기
                            </button>

                        </form>

                        <div className="auth-links">
                            <Link to="/login">로그인으로 돌아가기</Link>
                        </div>

                    </div>
                </div>
            </div>

            {modalConfig.isOpen && (
                <div className="signup-modal-overlay">
                    <div className="custom-modal">
                        <p>{modalConfig.message}</p>
                        <button className="modal-btn" onClick={modalConfig.onConfirm}>
                            확인
                        </button>
                    </div>
                </div>
            )}
        </>
    );
};

export default ResetPassword;