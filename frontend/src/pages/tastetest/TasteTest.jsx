// TasteTest.jsx

import './TasteTest.css';

import { useEffect, useState } from 'react';
import { useNavigate } from 'react-router-dom';
import {
    ArrowRight,
    Check,
    ExternalLink,
    Hash,
    Heart,
    Image as ImageIcon,
    Map as MapIcon,
    MessageSquare,
    RotateCcw,
    Sparkles,
    Users,
    X
} from 'lucide-react';

import axiosInstance from "../../api/axiosInstance.js";

const API_URL = import.meta.env.VITE_API_URL;

const clusterLabelsText = {
    cluster_0: "선화 중심 캐주얼 화풍",
    cluster_1: "회화적 질감·수채화 도메인",
    cluster_2: "반실사·입체 명암 화풍",
    cluster_3: "네온·사이버 가상 무드",
    cluster_4: "현대 서브컬처 일러스트"
};

const clusterColors = {
    cluster_0: "#4aaddf",
    cluster_1: "#f472b6",
    cluster_2: "#fbbf24",
    cluster_3: "#a78bfa",
    cluster_4: "#7dd3fc"
};

const clusterDescriptions = {
    cluster_0: "깔끔한 선화와 단순한 색면이 돋보이는, 캐주얼하고 가벼운 느낌의 화풍이에요.",
    cluster_1: "붓터치와 질감이 살아있는, 수채화처럼 번지고 스며드는 회화적인 화풍이에요.",
    cluster_2: "실사에 가까운 입체적인 명암 표현이 특징인, 묵직하고 디테일한 화풍이에요.",
    cluster_3: "네온 컬러와 사이버틱한 색감이 두드러지는, 가상·미래적인 무드의 화풍이에요.",
    cluster_4: "서브컬처 감성이 강한, 현대적이고 트렌디한 일러스트 화풍이에요."
};

const getImagePath = (url) =>
    url
        ?.replace("https://kr.ncloudstorage.com/artlens-bucket/", "")
        ?.replace(/\\/g, "/");

const TasteTest = () => {

    const navigate = useNavigate();

    const [phase, setPhase] = useState('loading'); // loading | question | finished | error
    const [testId, setTestId] = useState(null);
    const [questionNo, setQuestionNo] = useState(1);
    const [totalQuestions, setTotalQuestions] = useState(10);
    const [options, setOptions] = useState([]);
    const [submitting, setSubmitting] = useState(false);
    const [result, setResult] = useState(null);
    const [likedIds, setLikedIds] = useState(new Set());
    const [pickedImgId, setPickedImgId] = useState(null);
    const [detailArtist, setDetailArtist] = useState(null);
    const [pickedHistory, setPickedHistory] = useState([]);
    const [lightboxImg, setLightboxImg] = useState(null);

    useEffect(() => {

        startTest();

    }, []);

    const startTest = async () => {

        setPhase('loading');
        setResult(null);
        setPickedHistory([]);
        setLightboxImg(null);
        setDetailArtist(null);

        try {

            const res = await axiosInstance.post("/taste-test/v1/start");

            setTestId(res.data.testId);
            setQuestionNo(res.data.questionNo);
            setTotalQuestions(res.data.totalQuestions);
            setOptions(res.data.options);
            setPhase('question');

        } catch (e) {

            console.error(e);
            setPhase('error');
        }
    };

    const handlePick = async (imgId) => {

        if (submitting) {

            return;
        }

        setSubmitting(true);
        setPickedImgId(imgId);

        const pickedOption = options.find(o => o.imgId === imgId);

        if (pickedOption) {

            setPickedHistory(prev => [...prev, pickedOption]);
        }

        try {

            const res = await axiosInstance.post(
                `/taste-test/v1/${testId}/answer`,
                {
                    shownImgIds: options.map(o => o.imgId),
                    pickedImgId: imgId
                }
            );

            // 선택 카드 이펙트를 충분히 보여준 뒤 다음 질문으로 넘어감
            await new Promise(resolve => setTimeout(resolve, 650));

            if (res.data.finished) {

                setResult(res.data);
                setPhase('finished');

            } else {

                setQuestionNo(res.data.questionNo);
                setOptions(res.data.options);
            }

        } catch (e) {

            console.error(e);
            setPhase('error');

        } finally {

            setPickedImgId(null);
            setSubmitting(false);
        }
    };

    const openArtistDetail = async (artistId) => {

        try {

            const res = await axiosInstance.get(`/artist/v1/explore?artistIds=${artistId}`);

            if (res.data?.[0]) {

                setDetailArtist(res.data[0]);
            }

        } catch (e) {

            console.error(e);
        }
    };

    const handleToggleLike = async (artistId) => {

        const nextLiked = !likedIds.has(artistId);

        setLikedIds(prev => {

            const next = new Set(prev);
            nextLiked ? next.add(artistId) : next.delete(artistId);
            return next;
        });

        try {

            if (nextLiked) {

                await axiosInstance.put(`/like/v1/artists/${artistId}`);

            } else {

                await axiosInstance.delete(`/like/v1/artists/${artistId}`);
            }

        } catch (e) {

            console.error(e);

            setLikedIds(prev => {

                const next = new Set(prev);
                nextLiked ? next.delete(artistId) : next.add(artistId);
                return next;
            });
        }
    };

    if (phase === 'loading') {

        return (
            <div className="taste-test-page taste-test-center">
                <div className="taste-test-spinner" />
            </div>
        );
    }

    if (phase === 'error') {

        return (
            <div className="taste-test-page taste-test-center">
                <p className="taste-test-error-text">문제를 불러오지 못했어요.</p>
                <button className="taste-test-btn-primary" onClick={startTest}>
                    다시 시도
                </button>
            </div>
        );
    }

    if (phase === 'finished' && result) {

        const clusterTitle = clusterLabelsText[result.clusterLabel] || "독특한 화풍";
        const accentColor = clusterColors[result.clusterLabel] || '#4aaddf';

        return (
            <div className="taste-test-page" style={{ '--taste-test-accent': accentColor }}>

                <div className="taste-test-bg-glow taste-test-glow-1" />
                <div className="taste-test-bg-glow taste-test-glow-2" />

                <div className="taste-test-result-wrapper">

                    <div className="taste-test-result-hero">

                        <div className="taste-test-result-badge">
                            <Sparkles size={26} />
                        </div>

                        <p className="taste-test-result-label">당신의 취향 유형</p>
                        <h1 className="taste-test-result-title">{clusterTitle}</h1>
                        <p className="taste-test-result-desc">
                            {clusterDescriptions[result.clusterLabel] || "고르신 그림들을 기반으로 분석한 당신만의 화풍이에요."}
                        </p>

                        <div className="taste-test-result-actions">

                            <button
                                className="taste-test-btn-outline"
                                onClick={() => navigate('/style-map', {
                                    state: { artistId: result.topArtists?.[0]?.artistId }
                                })}
                            >
                                <MapIcon size={18} />
                                Style Map에서 내 취향 위치 보기
                            </button>

                            <button className="taste-test-btn-outline" onClick={startTest}>
                                <RotateCcw size={18} />
                                다시 하기
                            </button>
                        </div>
                    </div>

                    <div className="taste-test-result-section">

                        <div className="taste-test-section-header">
                            <Users size={18} />
                            <h2>가장 가까운 작가 Top 5</h2>
                        </div>

                        <div className="taste-test-artist-list">

                            {result.topArtists?.map(artist => (

                                <div key={artist.artistId} className="taste-test-artist-row">

                                    <img
                                        src={`${API_URL}/analysis/v1/api/image/${getImagePath(artist.profileImg)}`}
                                        alt={artist.artistName}
                                        className="taste-test-artist-thumb"
                                    />

                                    <div className="taste-test-artist-info">
                                        <span className="taste-test-artist-name">{artist.artistName}</span>
                                        <span className="taste-test-artist-percent">{artist.similarityPercent}% 일치</span>
                                    </div>

                                    <div className="taste-test-artist-actions">

                                        <button
                                            className={`taste-test-like-btn ${likedIds.has(artist.artistId) ? 'liked' : ''}`}
                                            onClick={() => handleToggleLike(artist.artistId)}
                                        >
                                            <Heart size={16} fill={likedIds.has(artist.artistId) ? 'currentColor' : 'none'} />
                                            찜하기
                                        </button>

                                        <button
                                            className="taste-test-detail-btn"
                                            onClick={() => openArtistDetail(artist.artistId)}
                                        >
                                            작가 상세보기
                                            <ArrowRight size={14} />
                                        </button>
                                    </div>
                                </div>
                            ))}
                        </div>
                    </div>

                    <div className="taste-test-result-section">

                        <div className="taste-test-section-header">
                            <ImageIcon size={18} />
                            <h2>내가 고른 그림들</h2>
                            <span className="taste-test-section-count">{pickedHistory.length}장</span>
                        </div>

                        <div className="taste-test-history-grid">

                            {pickedHistory.map((opt, idx) => (

                                <button
                                    key={`${opt.imgId}-${idx}`}
                                    className="taste-test-history-thumb-btn"
                                    onClick={() => setLightboxImg(opt)}
                                >
                                    <img
                                        src={`${API_URL}/analysis/v1/api/image/${getImagePath(opt.imgUrl)}`}
                                        alt=""
                                        className="taste-test-history-thumb"
                                    />
                                </button>
                            ))}
                        </div>
                    </div>

                </div>

                <div
                    className={`taste-test-lightbox-overlay ${lightboxImg ? 'show' : ''}`}
                    onClick={(e) => {

                        if (e.target.classList.contains('taste-test-lightbox-overlay')) {

                            setLightboxImg(null);
                        }
                    }}
                >

                    {lightboxImg && (

                        <div className="taste-test-lightbox">

                            <button
                                className="taste-test-modal-close"
                                onClick={() => setLightboxImg(null)}
                            >
                                <X size={20} />
                            </button>

                            <img
                                src={`${API_URL}/analysis/v1/api/image/${getImagePath(lightboxImg.imgUrl)}`}
                                alt=""
                                className="taste-test-lightbox-img"
                            />

                            <button
                                className="taste-test-btn-primary taste-test-lightbox-action"
                                onClick={() => {

                                    const artistId = lightboxImg.artistId;
                                    setLightboxImg(null);
                                    openArtistDetail(artistId);
                                }}
                            >
                                이 작가 작품 더 보기
                                <ArrowRight size={16} />
                            </button>
                        </div>
                    )}
                </div>

                <div
                    className={`taste-test-modal-overlay ${detailArtist ? 'show' : ''}`}
                    onClick={(e) => {

                        if (e.target.classList.contains('taste-test-modal-overlay')) {

                            setDetailArtist(null);
                        }
                    }}
                >

                    {detailArtist && (

                        <div className="taste-test-modal">

                            <button
                                className="taste-test-modal-close"
                                onClick={() => setDetailArtist(null)}
                            >
                                <X size={20} />
                            </button>

                            <img
                                src={`${API_URL}/analysis/v1/api/image/${getImagePath(detailArtist.profileImg)}`}
                                alt={detailArtist.artistName}
                                className="taste-test-modal-cover"
                            />

                            <div className="taste-test-modal-body">

                                <h2 className="taste-test-modal-name">{detailArtist.artistName}</h2>

                                <div className="taste-test-modal-section">

                                    <div className="taste-test-modal-section-title">
                                        <MessageSquare size={16} />
                                        아티스트 스타일 분석
                                    </div>

                                    <p className="taste-test-modal-desc">{detailArtist.styleDesc}</p>
                                </div>

                                <div className="taste-test-modal-section">

                                    <div className="taste-test-modal-section-title">
                                        <Hash size={16} />
                                        주요 화풍 태그
                                    </div>

                                    <div className="taste-test-modal-tags">

                                        {detailArtist.tags?.map(tag => (
                                            <span key={tag} className="taste-test-tag">{tag}</span>
                                        ))}
                                    </div>
                                </div>

                                <div className="taste-test-modal-actions">

                                    <button
                                        className={`taste-test-like-btn ${likedIds.has(detailArtist.artistId) ? 'liked' : ''}`}
                                        onClick={() => handleToggleLike(detailArtist.artistId)}
                                    >
                                        <Heart size={16} fill={likedIds.has(detailArtist.artistId) ? 'currentColor' : 'none'} />
                                        찜하기
                                    </button>

                                    <button
                                        className="taste-test-btn-outline"
                                        onClick={() => navigate('/style-map', {
                                            state: { artistId: detailArtist.artistId }
                                        })}
                                    >
                                        <MapIcon size={16} />
                                        Style Map 위치 보기
                                    </button>

                                    <button
                                        className="taste-test-btn-primary"
                                        onClick={() => window.open(
                                            `https://safebooru.org/index.php?page=post&s=list&tags=${detailArtist.artistName}`,
                                            '_blank'
                                        )}
                                    >
                                        Safebooru 작품 더보기
                                        <ExternalLink size={16} />
                                    </button>
                                </div>

                            </div>
                        </div>
                    )}
                </div>

            </div>
        );
    }

    return (
        <div className="taste-test-page">

            <div className="taste-test-bg-glow taste-test-glow-1" />
            <div className="taste-test-bg-glow taste-test-glow-2" />

            <div className="taste-test-question-wrapper">

                <div className="taste-test-progress">
                    <div className="taste-test-progress-track">
                        <div
                            className="taste-test-progress-fill"
                            style={{ width: `${((questionNo - 1) / totalQuestions) * 100}%` }}
                        />
                    </div>
                    <span className="taste-test-progress-text">{questionNo} / {totalQuestions}</span>
                </div>

                <h2 className="taste-test-question-title">더 마음에 드는 그림을 골라주세요</h2>

                <div className="taste-test-options" key={questionNo}>

                    {options.map((opt, idx) => (

                        <button
                            key={opt.imgId}
                            className={`taste-test-option-card ${
                                pickedImgId === null
                                    ? ''
                                    : pickedImgId === opt.imgId
                                        ? 'picked'
                                        : 'unpicked'
                            }`}
                            style={{ animationDelay: `${idx * 0.08}s` }}
                            disabled={submitting}
                            onClick={() => handlePick(opt.imgId)}
                        >
                            <div
                                className="taste-test-option-backdrop"
                                style={{
                                    backgroundImage: `url(${API_URL}/analysis/v1/api/image/${getImagePath(opt.imgUrl)})`
                                }}
                            />

                            <img
                                src={`${API_URL}/analysis/v1/api/image/${getImagePath(opt.imgUrl)}`}
                                alt=""
                                className="taste-test-option-img"
                            />

                            {pickedImgId === opt.imgId && (

                                <div className="taste-test-picked-overlay">
                                    <div className="taste-test-picked-badge">
                                        <Check size={28} strokeWidth={3} />
                                    </div>
                                </div>
                            )}
                        </button>
                    ))}
                </div>

                <div className="taste-test-step-dots">

                    {Array.from({ length: totalQuestions }, (_, i) => i + 1).map(n => (

                        <span
                            key={n}
                            className={`taste-test-step-dot ${
                                n < questionNo ? 'done' : n === questionNo ? 'active' : ''
                            }`}
                        />
                    ))}
                </div>

                <p className="taste-test-question-hint">
                    {questionNo <= 4
                        ? "다양한 화풍을 먼저 보여드릴게요"
                        : "고르신 그림과 닮은 화풍으로 점점 좁혀가는 중이에요"}
                </p>

            </div>
        </div>
    );
};

export default TasteTest;
