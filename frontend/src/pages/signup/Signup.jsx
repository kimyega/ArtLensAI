import React, { useState, useEffect } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import './Signup.css';

const Signup = () => {
    console.log("🔥 Signup 렌더됨");
    const [email, setEmail] = useState('');
    const [nickname, setNickname] = useState('');
    const [password, setPassword] = useState('');
    const [confirmPassword, setConfirmPassword] = useState('');
    const [agreeTerms, setAgreeTerms] = useState(false);

    const [emailError, setEmailError] = useState('');
    const [nicknameError, setNicknameError] = useState('');
    const [passwordError, setPasswordError] = useState('');
    const [confirmPasswordError, setConfirmPasswordError] = useState('');

    const [isEmailSent, setIsEmailSent] = useState(false);
    const [verificationCode, setVerificationCode] = useState('');
    const [verificationError, setVerificationError] = useState('');
    const [isEmailVerified, setIsEmailVerified] = useState(false);
    const [timeLeft, setTimeLeft] = useState(180);

    const [modalConfig, setModalConfig] = useState({
        isOpen: false,
        message: '',
        onConfirm: null
    });

    const navigate = useNavigate();

    const closeModal = () =>
        setModalConfig(prev => ({ ...prev, isOpen: false }));

    // 타이머 로직
    useEffect(() => {
        let timer;
        if (isEmailSent && !isEmailVerified && timeLeft > 0) {
            timer = setInterval(() => {
                setTimeLeft((prev) => prev - 1);
            }, 1000);
        } else if (timeLeft === 0 && !isEmailVerified) {
            setVerificationError('인증 시간이 만료되었습니다. 재전송해 주세요.');
        }
        return () => clearInterval(timer);
    }, [isEmailSent, isEmailVerified, timeLeft]);

    const formatTime = (seconds) => {
        const m = Math.floor(seconds / 60).toString().padStart(2, '0');
        const s = (seconds % 60).toString().padStart(2, '0');
        return `${m}:${s}`;
    };

    // 유효성 검사
    const validateEmail = (targetEmail = email) => {
        const emailRegex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;
        if (!emailRegex.test(targetEmail)) {
            setEmailError('올바른 이메일 형식을 입력해주세요.');
            return false;
        }
        setEmailError('');
        return true;
    };

    const validateNickname = (targetNickname = nickname) => {
        if (targetNickname.length < 2) {
            setNicknameError('닉네임을 2자 이상 입력해 주세요.');
            return false;
        }
        setNicknameError('');
        return true;
    };

    const validatePassword = (targetPassword = password) => {
        const passwordRegex = /^(?=.*[a-zA-Z])(?=.*\d)(?=.*[!@#$%^&*()_+={}\[\]|\\:;"'<>,.?/-]).{8,}$/;
        if (!passwordRegex.test(targetPassword)) {
            setPasswordError('영문, 숫자, 특수문자를 포함해 8자 이상이어야 합니다.');
            return false;
        }
        setPasswordError('');
        return true;
    };

    const validateConfirmPassword = (targetConfirm = confirmPassword) => {
        if (password !== targetConfirm) {
            setConfirmPasswordError('비밀번호가 일치하지 않습니다.');
            return false;
        }
        setConfirmPasswordError('');
        return true;
    };

    // 핸들러
    // (기존 코드 중 handleSendEmail 함수만 아래 내용으로 교체해 주세요)

    const checkEmailDuplicate = async (email) => {
        const res = await fetch("http://localhost:8080/user/checkEmail", {
            method: "POST",
            headers: { "Content-Type": "application/json" },
            body: JSON.stringify({ email })
        });

        return await res.json();
    };

    const sendVerificationCode = async (email) => {
        const res = await fetch("http://localhost:8080/user/sendCode", {
            method: "POST",
            headers: { "Content-Type": "application/json" },
            body: JSON.stringify({ email })
        });

        return await res.json();
    };

    const handleSendEmail = async () => {
        if (!validateEmail()) return;

        try {
            // 1. 중복 체크
            const checkData = await checkEmailDuplicate(email);

            if (checkData.code === "EMAIL_DUPLICATE") {
                setEmailError("이미 가입된 이메일입니다.");
                return;
            }

            // 2. 인증번호 발송
            const data = await sendVerificationCode(email);

            if (data.result === 1) {
                setModalConfig({
                    isOpen: true,
                    message: "인증번호가 발송되었습니다.",
                    onConfirm: () => {
                        setIsEmailSent(true);
                        setTimeLeft(180);
                        setVerificationError('');
                        closeModal();
                    }
                });
            } else {
                setModalConfig({
                    isOpen: true,
                    message: data.msg,
                    onConfirm: closeModal
                });
            }

        } catch (e) {
            setModalConfig({
                isOpen: true,
                message: "서버 오류",
                onConfirm: closeModal
            });
        }
    };

    const handleVerifyCode = async () => {

        if (timeLeft === 0) {
            setVerificationError("인증 시간이 만료되었습니다.");
            return;
        }

        try {
            const res = await fetch("http://localhost:8080/user/verifyCode", {
                method: "POST",
                headers: {
                    "Content-Type": "application/json"
                },
                body: JSON.stringify({
                    email,
                    code: verificationCode
                })
            });

            const data = await res.json();

            if (data.result === 1) {
                setIsEmailVerified(true);
                setVerificationError('');
            } else {
                setVerificationError("인증번호가 일치하지 않습니다.");
            }

        } catch (e) {
            setVerificationError("서버 오류");
        }
    };

    const handleSubmit = async (e) => {
        e.preventDefault();

        const isEmailValid = validateEmail();
        const isNicknameValid = validateNickname();
        const isPasswordValid = validatePassword();
        const isConfirmValid = validateConfirmPassword();

        if (!isEmailValid || !isNicknameValid || !isPasswordValid || !isConfirmValid) return;

        if (!isEmailVerified) {
            setModalConfig({ isOpen: true, message: '이메일 인증을 완료해 주세요.', onConfirm: closeModal });
            return;
        }

        if (!agreeTerms) {
            setModalConfig({ isOpen: true, message: '이용약관 및 개인정보처리방침에\n동의해 주세요.', onConfirm: closeModal });
            return;
        }

        try {
            const res = await fetch("http://localhost:8080/user/signup", {
                method: "POST",
                headers: {
                    "Content-Type": "application/json"
                },
                body: JSON.stringify({
                    email,
                    password,
                    nickname
                })
            });

            const data = await res.json();

            if (data.result === 1) {
                setModalConfig({
                    isOpen: true,
                    message: 'ArtLens ai 회원가입이\n성공적으로 완료되었습니다! 🎉',
                    onConfirm: () => {
                        closeModal();
                        navigate('/login');
                    }
                });
            } else {

                // 🔥 코드 기반 처리
                if (data.code === "EMAIL_DUPLICATE") {
                    setEmailError("이미 가입된 이메일입니다.");
                    return;
                }

                setModalConfig({
                    isOpen: true,
                    message: data.msg,
                    onConfirm: closeModal
                });
            }

        } catch (error) {
            setModalConfig({
                isOpen: true,
                message: '서버 오류가 발생했습니다.',
                onConfirm: closeModal
            });
        }
    };

    const resetEmailVerification = () => {
        setIsEmailSent(false);
        setIsEmailVerified(false);
        setVerificationCode('');
        setVerificationError('');
        setTimeLeft(180);
    };

    const handleEmailChange = (e) => {
        const value = e.target.value;

        setEmail(value);
        setEmailError('');
        resetEmailVerification();
    };

    return (
        <>
        <div className="login-wrapper">
            <div className="left-section">
                <Link to="/" className="logo">
                    <div className="logo-box"></div>
                    <span>ArtLens ai</span>
                </Link>
                <div className="visual-content">
                    <h1 className="visual-title">새로운 <span>예술적 관점</span>을<br />발견할 시간입니다</h1>
                    <p className="visual-desc">간단한 가입만으로 수천 명의 작가 데이터 기반 화풍 분석과 나만의 히스토리 기록을 시작할 수 있습니다.</p>
                </div>
                <div className="visual-footer">CREATE YOUR ACCOUNT</div>
            </div>

            <div className="right-section">
                <div className="login-container">
                    <div className="login-header">
                        <h2>회원가입</h2>
                        <p>ArtLens ai의 멤버가 되어보세요</p>
                    </div>

                    <form onSubmit={handleSubmit}>
                        <div className="form-group">
                            <label className="form-label">이메일 주소</label>
                            <div className="input-btn-group">
                                <input
                                    type="email"
                                    className={`form-input ${emailError ? 'error' : ''} ${isEmailVerified ? 'disabled-input' : ''}`}
                                    placeholder="example@artlens.ai"
                                    value={email}
                                    onChange={handleEmailChange}
                                    disabled={isEmailVerified}
                                    maxLength="100"
                                />
                                <button
                                    type="button"
                                    className={`btn-side ${isEmailVerified ? 'btn-disabled' : ''}`}
                                    onClick={handleSendEmail}
                                    disabled={isEmailVerified}
                                >
                                    {isEmailSent ? '재전송' : '전송'}
                                </button>
                            </div>
                            {emailError && <p className="error-text">{emailError}</p>}
                        </div>

                        {isEmailSent && (
                            <div className="form-group">
                                <div className="input-btn-group">
                                    <div className={`verify-input-wrapper ${verificationError ? 'error' : ''} ${isEmailVerified ? 'disabled-wrapper' : ''}`}>
                                        <input
                                            type="text"
                                            className="verify-input"
                                            placeholder="인증번호 6자리 입력"
                                            value={verificationCode}
                                            onChange={(e) => setVerificationCode(e.target.value)}
                                            disabled={isEmailVerified}
                                            maxLength="6"
                                        />
                                        {!isEmailVerified && <span className="timer">{formatTime(timeLeft)}</span>}
                                    </div>
                                    <button type="button" className={`btn-side ${isEmailVerified ? 'btn-disabled' : ''}`} onClick={handleVerifyCode} disabled={isEmailVerified}>
                                        {isEmailVerified ? '완료됨' : '인증하기'}
                                    </button>
                                </div>
                                {verificationError && <p className="error-text">{verificationError}</p>}
                                {isEmailVerified && <p className="success-text">이메일 인증이 완료되었습니다.</p>}
                            </div>
                        )}

                        <div className="form-group">
                            <label className="form-label">닉네임</label>
                            <input
                                type="text"
                                className={`form-input ${nicknameError ? 'error' : ''}`}
                                placeholder="예: Karis"
                                value={nickname}
                                onChange={(e) => { setNickname(e.target.value); if (nicknameError) validateNickname(e.target.value); }}
                                onBlur={() => validateNickname()}
                                maxLength="50"
                            />
                            {nicknameError && <p className="error-text">{nicknameError}</p>}
                        </div>

                        <div className="form-group">
                            <label className="form-label">비밀번호</label>
                            <input
                                type="password"
                                className={`form-input ${passwordError ? 'error' : ''}`}
                                placeholder="8자 이상 (영문, 숫자, 특수문자 포함)"
                                value={password}
                                onChange={(e) => { setPassword(e.target.value); if (passwordError) validatePassword(e.target.value); }}
                                onBlur={() => validatePassword()}
                            />
                            {passwordError && <p className="error-text">{passwordError}</p>}
                        </div>

                        <div className="form-group">
                            <label className="form-label">비밀번호 확인</label>
                            <input
                                type="password"
                                className={`form-input ${confirmPasswordError ? 'error' : ''}`}
                                placeholder="비밀번호를 다시 입력해 주세요"
                                value={confirmPassword}
                                onChange={(e) => { setConfirmPassword(e.target.value); if (confirmPasswordError) validateConfirmPassword(e.target.value); }}
                                onBlur={() => validateConfirmPassword()}
                            />
                            {confirmPasswordError && <p className="error-text">{confirmPasswordError}</p>}
                        </div>

                        <label className="terms-group">
                            <input type="checkbox" checked={agreeTerms} onChange={(e) => setAgreeTerms(e.target.checked)} />
                            <span>ArtLens ai의 <Link to="#">이용약관</Link> 및 <Link to="#">개인정보처리방침</Link>에 동의합니다.</span>
                        </label>

                        <button type="submit" className="btn-login">계정 만들기</button>
                    </form>

                    <div className="auth-links">
                        이미 계정이 있으신가요? <Link to="/login">로그인</Link>
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

export default Signup;