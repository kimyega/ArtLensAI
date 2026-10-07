import React, {useState} from 'react';
import './Login.css';
import {Link, useNavigate} from 'react-router-dom';
import axiosInstance from '../../api/axiosInstance';
import {useAuth} from "../../context/AuthContext.jsx";

const Login = () => {
    const [email, setEmail] = useState('');
    const [password, setPassword] = useState('');

    const [emailError, setEmailError] = useState('');
    const [passwordError, setPasswordError] = useState('');

    const navigate = useNavigate();

    const {
        fetchUserInfo
    } = useAuth();

    // =========================
    // 🔥 유효성 검사
    // =========================
    const validateEmail = () => {
        if (!email) {
            setEmailError('이메일을 입력해 주세요.');
            return false;
        }

        const emailRegex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;
        if (!emailRegex.test(email)) {
            setEmailError('올바른 이메일 형식을 입력해 주세요.');
            return false;
        }

        setEmailError('');
        return true;
    };

    const validatePassword = () => {
        if (!password) {
            setPasswordError('비밀번호를 입력해 주세요.');
            return false;
        }

        setPasswordError('');
        return true;
    };

    // =========================
    // 🔥 로그인 요청
    // =========================
    const handleSubmit = async (e) => {
        e.preventDefault();

        const isEmailValid = validateEmail();
        const isPasswordValid = validatePassword();

        if (!isEmailValid || !isPasswordValid) return;

        try {

            const response =
                await axiosInstance.post(
                    '/login/v1/loginProc',
                    {
                        email,
                        password
                    }
                );

            const result =
                response.data.data.result;

            const msg =
                response.data.data.msg;

            if (result === 1) {

                await fetchUserInfo();

                navigate('/');

            } else {

                setPasswordError(
                    msg || '로그인 실패'
                );
            }

        } catch (error) {


            setPasswordError(
                '아이디 또는 비밀번호가 올바르지 않습니다.'
            );
        }
    };

    return (
        <div className="login-wrapper">

            {/* LEFT */}
            <div className="left-section">
                <a href="/" className="logo">
                    <div className="logo-box"></div>
                    <span>ArtLens ai</span>
                </a>

                <div className="visual-content">
                    <h1 className="visual-title">
                        당신의 예술적 여정,<br />
                        <span>AI</span> 렌즈로 기록하다
                    </h1>
                    <p className="visual-desc">
                        로그인하고 이미지 한 장으로 시작되는 스마트 화풍 분석과 거장들과의 만남을 이어가세요.
                    </p>
                </div>

                <div className="visual-footer">ANALYSIS · HISTORY · EXPLORE</div>
            </div>

            {/* RIGHT */}
            <div className="right-section">
                <div className="login-container">

                    <div className="login-header">
                        <h2>로그인</h2>
                        <p>ArtLens ai 커뮤니티에 다시 오신 것을 환영합니다</p>
                    </div>

                    <form onSubmit={handleSubmit}>

                        {/* EMAIL */}
                        <div className="form-group">
                            <input
                                type="text"
                                className={`form-input ${emailError ? 'error' : ''}`}
                                placeholder="이메일 주소"
                                value={email}
                                onChange={(e) => {
                                    setEmail(e.target.value);
                                    setEmailError('');
                                }}
                            />
                            {emailError && <p className="error-text">{emailError}</p>}
                        </div>

                        {/* PASSWORD */}
                        <div className="form-group">
                            <input
                                type="password"
                                className={`form-input ${passwordError ? 'error' : ''}`}
                                placeholder="비밀번호"
                                value={password}
                                onChange={(e) => {
                                    setPassword(e.target.value);
                                    setPasswordError('');
                                }}
                            />
                            {passwordError && <p className="error-text">{passwordError}</p>}
                        </div>

                        <button type="submit" className="btn-login">
                            서비스 시작하기
                        </button>
                    </form>

                    <div className="auth-links">
                        <Link to="/find-pw">비밀번호 찾기</Link>
                        <span className="dot">·</span>
                        <Link to="/signup">회원가입</Link>
                    </div>

                </div>
            </div>
        </div>
    );
};

export default Login;