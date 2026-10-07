import "./MyPage.css";
import {useEffect, useState} from "react";
import axiosInstance from "../../api/axiosInstance.js";
import { useAuth } from "../../context/AuthContext";

const MyPage = () => {

    const [showModal, setShowModal] =
        useState(false);

    const [deleteText, setDeleteText] =
        useState("");

    const [toast, setToast] =
        useState(null);

    const [userInfo, setUserInfo] =
        useState(null);

    const [myStat, setMyStat] =
        useState(null);

    const [nickname, setNickname] =
        useState("");

    const [currentPassword, setCurrentPassword] =
        useState("");

    const [newPassword, setNewPassword] =
        useState("");

    const [confirmPassword, setConfirmPassword] =
        useState("");

    const { fetchUserInfo } = useAuth();

    useEffect(() => {

        if (userInfo) {
            setNickname(
                userInfo.nickname
            );
        }

    }, [userInfo]);

    useEffect(() => {
        getUserInfo();
        getMyStat();
    }, []);


    const getUserInfo = async () => {

        const response =
            await axiosInstance.post(
                "/user/v1/userInfo"
            );

        setUserInfo(
            response.data.data
        );
    };

    const getMyStat = async () => {

        const response =
            await axiosInstance.post(
                "/mypage/v1/stat"
            );

        setMyStat(
            response.data.data
        );
    };

    const handleNicknameUpdate =
        async () => {

            if (
                nickname.trim() === ""
            ) {

                showToast(
                    "닉네임을 입력해주세요."
                );

                return;
            }

            if (
                nickname ===
                userInfo?.nickname
            ) {

                showToast(
                    "변경된 내용이 없습니다."
                );

                return;
            }

            try {

                await axiosInstance.post(
                    "/mypage/v1/nickname",
                    {
                        nickname
                    }
                );

                await getUserInfo();
                await fetchUserInfo();

                showToast(
                    "닉네임이 변경되었습니다.",
                    "success"
                );

            } catch (e) {

                showToast(
                    "닉네임 변경 실패"
                );
            }
        };

    const showToast = (
        message,
        type = "error"
    ) => {

        setToast({
            message,
            type
        });

        setTimeout(() => {

            setToast(null);

        }, 3000);
    };


    const handleDeleteAccount =
        async () => {

            try {

                const response =
                    await axiosInstance.post(
                        "/mypage/v1/withdraw"
                    );

                if (
                    response.data.data > 0
                ) {

                    showToast(
                        "계정이 삭제되었습니다.",
                        "success"
                    );

                    setTimeout(() => {

                        window.location.href =
                            "/";

                    }, 1500);

                } else {

                    showToast(
                        "계정 삭제 실패"
                    );
                }

            } catch (e) {

                showToast(
                    "계정 삭제 실패"
                );
            }
        };

    const validatePassword = () => {

        const regex =
            /^(?=.*[a-zA-Z])(?=.*\d)(?=.*[!@#$%^&*()_+={}\[\]|\\:;"'<>,.?/-]).{8,}$/;

        if (!newPassword) {

            showToast(
                "새 비밀번호를 입력해주세요."
            );

            return false;
        }

        if (!regex.test(newPassword)) {

            showToast(
                "영문, 숫자, 특수문자를 포함해 8자 이상이어야 합니다."
            );

            return false;
        }

        return true;
    };

    const validateConfirm = () => {

        if (
            newPassword !==
            confirmPassword
        ) {

            showToast(
                "비밀번호가 일치하지 않습니다."
            );

            return false;
        }

        return true;
    };

    const handlePasswordChange =
        async () => {

            if (!currentPassword) {

                showToast(
                    "현재 비밀번호를 입력해주세요."
                );

                return;
            }

            if (!validatePassword()) {
                return;
            }

            if (!validateConfirm()) {
                return;
            }

            try {

                const response =
                    await axiosInstance.post(
                        "/mypage/v1/password",
                        {
                            currentPassword,
                            newPassword
                        }
                    );

                if (
                    response.data.data > 0
                ) {

                    showToast(
                        "비밀번호가 변경되었습니다.",
                        "success"
                    );

                    setCurrentPassword("");
                    setNewPassword("");
                    setConfirmPassword("");

                } else {

                    showToast(
                        "현재 비밀번호가 일치하지 않습니다."
                    );
                }

            } catch (e) {

                showToast(
                    "비밀번호 변경 실패"
                );
            }
        };

    return (
        <>

            {toast && (
                <div
                    className={`
            my-page-toast
            ${toast.type}
        `}
                >
                    {toast.message}
                </div>
            )}


            <div className="my-page-page">

                <div className="my-page-wrapper">

                    <div className="my-page-header">

                        <h1>
                            마이페이지
                        </h1>

                        <p>
                            프로필 정보를 수정하거나
                            보안 설정을 관리할 수 있습니다.
                        </p>

                    </div>

                    <div className="my-page-settings-card my-page-profile-summary">

                        <div className="my-page-profile-info">

                            <div className="my-page-profile-avatar">
                                K
                            </div>

                            <div className="my-page-profile-text">

                                <h2>{userInfo?.nickname}</h2>
                                <p>{userInfo?.email}</p>

                            </div>

                        </div>

                        <div className="my-page-profile-stats">

                            <div className="my-page-stat-item">

                    <span className="my-page-stat-label">
                        총 분석 횟수
                    </span>
                                <span className="my-page-stat-value">
                            {myStat?.analysisCount ?? 0}회
                        </span>

                            </div>

                            <div className="my-page-stat-item">

                                <span className="my-page-stat-label">
                                    대표 태그
                                </span>

                                <span className="my-page-stat-value">
                                    #{myStat?.mainTag ?? ""}
                                </span>

                            </div>

                        </div>

                    </div>

                    <div className="my-page-settings-card">

                        <div className="my-page-card-title">
                            프로필 정보
                        </div>

                        <div className="my-page-form-group">

                            <label>
                                이메일 계정
                            </label>

                            <input
                                className="my-page-input"
                                value={userInfo?.email || ""}
                                disabled
                            />

                        </div>

                        <div className="my-page-form-group">

                            <label>
                                닉네임
                            </label>

                            <input
                                className="my-page-input"
                                value={nickname}
                                onChange={(e) =>
                                    setNickname(
                                        e.target.value
                                    )
                                }
                            />

                        </div>

                        <div className="my-page-form-actions">

                            <button
                                className="my-page-btn-primary"
                                onClick={
                                    handleNicknameUpdate
                                }
                            >
                                변경사항 저장
                            </button>

                        </div>

                    </div>

                    <div className="my-page-settings-card">

                        <div className="my-page-card-title">
                            보안 및 비밀번호
                        </div>

                        <div className="my-page-form-group">

                            <label>
                                현재 비밀번호
                            </label>

                            <input
                                type="password"
                                className="my-page-input"
                                value={currentPassword}
                                onChange={(e) =>
                                    setCurrentPassword(
                                        e.target.value
                                    )
                                }
                            />

                        </div>

                        <div className="my-page-form-group">

                            <label>
                                새 비밀번호
                            </label>

                            <input
                                type="password"
                                className="my-page-input"
                                value={newPassword}
                                onChange={(e) =>
                                    setNewPassword(
                                        e.target.value
                                    )
                                }
                            />

                        </div>

                        <div className="my-page-form-group">

                            <label>
                                새 비밀번호 확인
                            </label>

                            <input
                                type="password"
                                className="my-page-input"
                                value={confirmPassword}
                                onChange={(e) =>
                                    setConfirmPassword(
                                        e.target.value
                                    )
                                }
                            />

                        </div>

                        <div className="my-page-form-actions">

                            <button
                                className="my-page-btn-primary"
                                onClick={
                                    handlePasswordChange
                                }
                            >
                                비밀번호 변경
                            </button>

                        </div>

                    </div>

                    <div
                        className="
                my-page-settings-card
                my-page-danger-zone
            "
                    >

                        <div className="my-page-card-title">
                            계정 탈퇴
                        </div>

                        <p className="my-page-danger-desc">

                            계정을 탈퇴하면
                            분석 데이터와 기록이
                            모두 영구 삭제됩니다.

                        </p>

                        <button
                            className="my-page-btn-danger"
                            onClick={() =>
                                setShowModal(true)
                            }
                        >
                            계정 영구 탈퇴
                        </button>

                    </div>

                </div>

            </div>


            {showModal && (

                <div
                    className="my-page-modal-overlay"
                    onClick={() =>
                        setShowModal(false)
                    }
                >

                    <div
                        className="my-page-modal-content"
                        onClick={(e) =>
                            e.stopPropagation()
                        }
                    >

                        <h2>
                            정말 탈퇴하시겠습니까?
                        </h2>

                        <p>
                            계정 삭제를 위해
                            <strong>
                                "ArtLens 탈퇴"
                            </strong>
                            를 입력해주세요.
                        </p>

                        <input
                            value={deleteText}
                            onChange={(e) =>
                                setDeleteText(
                                    e.target.value
                                )
                            }
                            className="my-page-modal-input"
                            placeholder="ArtLens 탈퇴"
                        />

                        {deleteText &&
                            deleteText !== "ArtLens 탈퇴" && (

                                <p className="my-page-delete-error">
                                    정확히 "ArtLens 탈퇴"를 입력해주세요.
                                </p>
                            )}

                        <div
                            className="my-page-modal-actions"
                        >

                            <button
                                className="my-page-btn-outline"
                                onClick={() =>
                                    setShowModal(false)
                                }
                            >
                                취소
                            </button>

                            <button
                                className="my-page-btn-danger"
                                disabled={
                                    deleteText !==
                                    "ArtLens 탈퇴"
                                }
                                onClick={
                                    handleDeleteAccount
                                }
                            >
                                계정 삭제
                            </button>

                        </div>

                    </div>

                </div>
            )}

        </>
    );
};

export default MyPage;