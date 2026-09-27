// src/hooks/useShare.js
//
// 분석 결과 공유 로직(생성/조회/취소/각 플랫폼 공유)을 한 곳에 모아둔 훅.
// AnalysisResult.jsx(단일 결과 페이지)와 AnalysisHistory.jsx(목록 카드)가 같이 쓴다.

import { useEffect, useState } from 'react';
import axiosInstance from '../api/axiosInstance.js';

const API_URL = import.meta.env.VITE_API_URL;

export function useShare({
    resultId,
    title,
    imgUrl,
    initialShare = null,
    skipInitialFetch = false
}) {

    const [share, setShare] = useState(initialShare);

    const [shareModalOpen, setShareModalOpen] = useState(false);

    const [copySuccess, setCopySuccess] = useState(false);

    const shareTitle =
        title
            ? `ArtLens AI 분석 결과 - ${title}`
            : 'ArtLens AI 분석 결과';

    // =============================
    // 카카오 SDK 초기화
    // =============================
    useEffect(() => {

        if (window.Kakao && !window.Kakao.isInitialized()) {

            window.Kakao.init(import.meta.env.VITE_KAKAO_JS_KEY);
        }

    }, []);

    // =============================
    // 기존 공유 링크 조회 (배치로 미리 받은 경우엔 스킵)
    // =============================
    useEffect(() => {

        if (skipInitialFetch || !resultId) return;

        const loadShare = async () => {

            try {

                const res =
                    await axiosInstance.get(
                        "/share/v1/mine",
                        { params: { resultId } }
                    );

                if (res.data) {

                    setShare(res.data);
                }

            } catch (e) {

                console.error(e);
            }
        };

        loadShare();

    }, [resultId, skipInitialFetch]);

    const openShareModal = async () => {

        let current = share;

        if (!current) {

            try {

                const res =
                    await axiosInstance.post(
                        "/share/v1",
                        { resultId }
                    );

                current = res.data;

                setShare(current);

            } catch (e) {

                console.error(e);

                return;
            }
        }

        // 버튼은 항상 같은 모달을 연다 — 기기 공유(Web Share API)는
        // 모달 안의 선택지 중 하나일 뿐, 진입점이 기기마다 달라지지 않게
        setShareModalOpen(true);
    };

    const closeShareModal = () => setShareModalOpen(false);

    const handleCopyLink = async () => {

        if (!share) return;

        try {

            await navigator.clipboard.writeText(
                share.shareUrl
            );

            setCopySuccess(true);

            setTimeout(
                () => setCopySuccess(false),
                2000
            );

        } catch (e) {

            console.error(e);
        }
    };

    const handleNativeShare = async () => {

        if (!share || !navigator.share) return;

        try {

            await navigator.share({
                title: shareTitle,
                text: '내 그림 화풍 분석 결과를 확인해보세요!',
                url: share.shareUrl
            });

        } catch (e) {

            // 사용자가 공유창을 취소한 경우(AbortError)는 무시
            if (e?.name !== 'AbortError') {

                console.error(e);
            }
        }
    };

    const handleKakaoShare = () => {

        if (!share || !window.Kakao?.isInitialized()) return;

        window.Kakao.Share.sendDefault({
            objectType: 'feed',
            content: {
                title: shareTitle,
                description: '내 그림 화풍 분석 결과를 확인해보세요!',
                imageUrl: imgUrl ?? `${API_URL}/favicon.svg`,
                link: {
                    mobileWebUrl: share.shareUrl,
                    webUrl: share.shareUrl
                }
            },
            buttons: [
                {
                    title: '분석 결과 보기',
                    link: {
                        mobileWebUrl: share.shareUrl,
                        webUrl: share.shareUrl
                    }
                }
            ]
        });
    };

    const handleNaverShare = () => {

        if (!share) return;

        const shareUrl =
            `https://share.naver.com/web/shareView?url=${encodeURIComponent(share.shareUrl)}&title=${encodeURIComponent(shareTitle)}`;

        window.open(
            shareUrl,
            '_blank',
            'width=600,height=600'
        );
    };

    const handleFacebookShare = () => {

        if (!share) return;

        const shareUrl =
            `https://www.facebook.com/sharer/sharer.php?u=${encodeURIComponent(share.shareUrl)}`;

        window.open(
            shareUrl,
            '_blank',
            'width=600,height=600'
        );
    };

    const handleXShare = () => {

        if (!share) return;

        const shareUrl =
            `https://twitter.com/intent/tweet?url=${encodeURIComponent(share.shareUrl)}&text=${encodeURIComponent(shareTitle)}`;

        window.open(
            shareUrl,
            '_blank',
            'width=600,height=600'
        );
    };

    const handleRevokeShare = async () => {

        if (!share) return;

        try {

            await axiosInstance.delete(
                `/share/v1/${share.shareId}`
            );

            setShare(null);

            setShareModalOpen(false);

        } catch (e) {

            console.error(e);
        }
    };

    return {
        share,
        shareModalOpen,
        copySuccess,
        openShareModal,
        closeShareModal,
        handleCopyLink,
        handleNativeShare,
        handleKakaoShare,
        handleNaverShare,
        handleFacebookShare,
        handleXShare,
        handleRevokeShare
    };
}
