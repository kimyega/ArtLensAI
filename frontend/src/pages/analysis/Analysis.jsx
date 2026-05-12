import React, { useState, useRef, useEffect } from 'react';
import './Analysis.css';

const Analysis = () => {
    const [previewSrc, setPreviewSrc] = useState(null);
    const [file, setFile] = useState(null); // 🔥 핵심 추가
    const [isDragOver, setIsDragOver] = useState(false);
    const [isScanning, setIsScanning] = useState(false);
    const [statusText, setStatusText] = useState('');
    const [toast, setToast] = useState({ show: false, message: '' });
    const [loading, setLoading] = useState(false);

    const fileInputRef = useRef(null);

    const analysisSteps = [
        'ArtLens AI가 화풍 특징을 추출하고 있습니다...',
        '작품의 붓터치와 질감 패턴을 분석 중입니다...',
        '지배적인 색감 팔레트를 분석하고 있습니다...',
        '구도 및 거장 데이터와의 유사도를 계산 중입니다...',
        '거의 다 되었습니다! 결과를 정리하고 있습니다...'
    ];

    useEffect(() => {
        let interval;
        if (isScanning) {
            let step = 0;
            setStatusText(analysisSteps[step]);

            interval = setInterval(() => {
                step++;
                if (step < analysisSteps.length) {
                    setStatusText(analysisSteps[step]);
                } else {
                    clearInterval(interval);
                }
            }, 1200);
        }

        return () => clearInterval(interval);
    }, [isScanning]);

    const showToast = (message) => {
        setToast({ show: true, message });
        setTimeout(() => setToast({ show: false, message: '' }), 3000);
    };

    // 🔥 공통 파일 처리 함수 (핵심)
    const processFile = (file) => {
        if (!file) return;

        const ALLOWED_TYPES = ['image/jpeg', 'image/png', 'image/webp'];

        if (!ALLOWED_TYPES.includes(file.type)) {
            showToast('JPG, PNG, WEBP 이미지 파일만 가능합니다.');
            return;
        }

        if (file.size > 10 * 1024 * 1024) {
            showToast('10MB 이하의 이미지만 업로드 가능합니다.');
            return;
        }

        setFile(file); // 🔥 실제 file 저장

        const reader = new FileReader();
        reader.onload = (e) => setPreviewSrc(e.target.result);
        reader.readAsDataURL(file);
    };

    const handleUpload = async () => {

        // 🔥 중복 클릭 방지
        if (loading || isScanning) return;

        if (!file) {
            showToast("파일이 없습니다.");
            return;
        }

        try {
            setLoading(true);
            setIsScanning(true); // 🔥 먼저 잠금 (중복 요청 차단)

            const formData = new FormData();
            formData.append("file", file);

            const res = await fetch("http://localhost:8080/analysis/upload", {
                method: "POST",
                headers: {
                    Authorization: `Bearer ${localStorage.getItem("token")}`
                },
                body: formData
            });

            const data = await res.json();

            if (data.result !== 1) {
                setIsScanning(false);
                showToast("업로드 실패");
                return;
            }

            const uploadId = data.dto;

            console.log("uploadId:", uploadId);

            const aiRes = await fetch("http://localhost:8080/analysis/start", {
                method: "POST",
                headers: {
                    "Content-Type": "application/json",
                    Authorization: `Bearer ${localStorage.getItem("token")}`
                },
                body: JSON.stringify({ uploadId })
            });

            const aiData = await aiRes.json();
            console.log("AI 분석 결과:", aiData);

        } catch (err) {
            console.error(err);
            showToast("분석 요청 실패");
            setIsScanning(false);
        } finally {
            setLoading(false);
        }
    };

    return (
        <div className="analyze-page">

            {/* 토스트 */}
            <div className={`toast-message ${toast.show ? 'show' : ''}`}>
                <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2">
                    <circle cx="12" cy="12" r="10"></circle>
                    <line x1="12" y1="8" x2="12" y2="12"></line>
                    <line x1="12" y1="16" x2="12.01" y2="16"></line>
                </svg>
                <span>{toast.message}</span>
            </div>

            {/* 배경 */}
            <div className="bg-container">
                <div className="bg-glow glow-1"></div>
                <div className="bg-glow glow-2"></div>
                <div className="bg-glow glow-3"></div>
                <div className="bg-prism prism-1"></div>
                <div className="bg-prism prism-2"></div>
                <div className="bg-prism prism-3"></div>
            </div>

            <div className="main-wrapper">

                {!isScanning ? (
                    <main className="main-section">

                        <div className="upload-header">
                            <h1>단 한 장의 이미지, <br /><span>ArtLens ai</span>가 당신의 화풍을 읽어냅니다</h1>
                            <p>정교한 시각 분석 엔진이 작품의 색감, 터치, 구도를 깊이 있게 파악합니다</p>
                        </div>

                        <div className="upload-card">

                            {!previewSrc ? (
                                <div
                                    className={`drop-zone ${isDragOver ? 'dragover' : ''}`}
                                    onDragOver={(e) => { e.preventDefault(); setIsDragOver(true); }}
                                    onDragLeave={() => setIsDragOver(false)}
                                    onDrop={(e) => {
                                        e.preventDefault();
                                        setIsDragOver(false);
                                        processFile(e.dataTransfer.files[0]); // 🔥 수정
                                    }}
                                    onClick={() => fileInputRef.current.click()}
                                >
                                    <div className="upload-icon">
                                        <svg width="28" height="28" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2">
                                            <path d="M21 15v4a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2v-4"></path>
                                            <polyline points="17 8 12 3 7 8"></polyline>
                                            <line x1="12" y1="3" x2="12" y2="15"></line>
                                        </svg>
                                    </div>

                                    <h3>이미지를 이곳에 드래그 앤 드롭하세요</h3>
                                    <p>또는 <span>내 PC에서 파일 선택</span></p>
                                    <p className="sub-desc">JPG, PNG, WEBP (최대 10MB)</p>

                                    <input
                                        type="file"
                                        ref={fileInputRef}
                                        style={{ display: 'none' }}
                                        onChange={(e) => processFile(e.target.files[0])} // 🔥 수정
                                        accept="image/*"
                                    />
                                </div>
                            ) : (
                                <div className="preview-zone">
                                    <button className="btn-reselect" onClick={() => {
                                        setPreviewSrc(null);
                                        setFile(null); // 🔥 초기화
                                    }}>
                                        다른 이미지
                                    </button>

                                    <img src={previewSrc} alt="Preview" className="preview-image" />
                                </div>
                            )}

                            <button
                                className="btn-start"
                                disabled={!file || loading || isScanning}
                                onClick={handleUpload}
                            >
                                {loading ? "처리중..." : "ArtLens ai 분석 시작하기"}
                            </button>

                        </div>

                    </main>
                ) : (
                    <main className="main-section">

                        <div className="scanning-card">

                            <div className="scan-image-frame">
                                <div className="scanner-bar"></div>
                                <img src={previewSrc} alt="Scanning" className="scanning-image" />
                            </div>

                            <div className="loading-status">
                                <div className="loading-spinner"></div>
                                <p className="status-text">{statusText}</p>
                                <p className="status-sub">잠시만 기다려 주세요</p>
                            </div>

                        </div>

                    </main>
                )}

            </div>
        </div>
    );
};

export default Analysis;