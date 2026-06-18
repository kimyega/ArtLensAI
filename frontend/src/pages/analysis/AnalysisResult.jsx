// src/pages/analysis/AnalysisResult.jsx

import './AnalysisResult.css';


import {
    Compass,
    Palette,
    BarChart3,
    Users
} from 'lucide-react';

import {
    useNavigate,
    useParams
} from 'react-router-dom';

import {
    useEffect,
    useState
} from 'react';

const API_URL = import.meta.env.VITE_API_URL;

import axiosInstance
    from '../../api/axiosInstance.js';

const AnalysisResultPage = () => {

    const navigate = useNavigate();

    const { resultId } = useParams();

    const [result, setResult] = useState(null);

    const [loading, setLoading] = useState(true);

    useEffect(() => {

        if (!result) return;

        const sections =
            document.querySelectorAll(
                '.analysis-section-block'
            );

        const observer =
            new IntersectionObserver(

                (entries) => {

                    entries.forEach(entry => {

                        if (entry.isIntersecting) {

                            entry.target.classList.add(
                                'show'
                            );

                        } else {

                            entry.target.classList.remove(
                                'show'
                            );
                        }
                    });

                },

                {
                    threshold: 0.15
                }
            );

        sections.forEach(section =>
            observer.observe(section)
        );

        return () => {

            sections.forEach(section =>
                observer.unobserve(section)
            );
        };

    }, [result]);



    // =============================
    // 결과 조회
    // =============================
    useEffect(() => {

        const fetchResult = async () => {

            try {

                const res =
                    await axiosInstance.get(
                        `/analysis/v1/result/${resultId}`
                    );

                console.log("RESULT API =", res.data);

                setResult(res.data);

            } catch (e) {

                console.error(e);

            } finally {

                setLoading(false);
            }
        };

        fetchResult();

    }, [resultId]);

    // =============================
    // loading
    // =============================
    if (loading) {

        return (

            <div
                style={{
                    color: 'white',
                    padding: '100px',
                    fontSize: '24px'
                }}
            >
                분석 결과 불러오는 중...
            </div>
        );
    }

    // =============================
    // 결과 없음
    // =============================
    if (!result) {

        return (

            <div
                style={{
                    color: 'white',
                    padding: '100px',
                    fontSize: '24px'
                }}
            >
                결과가 존재하지 않습니다.
            </div>
        );
    }

    return (

        <div className="analysis-result-page">

            {/* BG */}
            <div className="analysis-bg-container">

                <div className="analysis-bg-glow analysis-glow-1"></div>

                <div className="analysis-bg-glow analysis-glow-2"></div>

            </div>

            {/* MAIN */}
            <div className="analysis-dashboard-wrapper">

                {/* HEADER */}
                <div className="analysis-dashboard-header">

                    <div className="analysis-header-title">

                        <h1>
                            분석이 완료되었습니다
                        </h1>

                        <p>
                            업로드하신 이미지의 화풍을
                            Safebooru 기반 일러스트레이터
                            데이터와 비교 분석한 결과입니다.
                        </p>

                    </div>

                    <div className="analysis-header-actions">

                        <button
                            className="analysis-btn analysis-btn-outline"
                            onClick={() => navigate('/analysis')}
                        >
                            재분석하기
                        </button>

                        <button
                            className="analysis-btn analysis-btn-primary"
                            onClick={() => navigate('/style-map', { state: { incomingResult: result } })}
                        >

                            <Compass size={16} />

                            Style Map에서 내 위치 보기

                        </button>

                    </div>

                </div>

                {/* HERO */}
                <section className="analysis-hero-section">

                    <div className="analysis-hero-image-wrap">

                        <img
                            src={`${API_URL}/analysis/v1/api/image/${result.imgKey}`}
                            alt="uploaded"
                            className="analysis-hero-image"
                        />

                    </div>

                    <div className="analysis-hero-content">

                        <div className="analysis-hero-label">
                            AI 일러스트 스타일 분석
                        </div>

                        <h1 className="analysis-hero-title">
                            {result.title}
                        </h1>

                        <div
                            className="analysis-comment-html analysis-hero-description"
                            dangerouslySetInnerHTML={{
                                __html: result.comment
                            }}
                        />

                    </div>

                </section>

                {/* PALETTE */}
                <section className="analysis-section-block">

                    <div className="analysis-section-header">

                        <Palette size={20} />

                        주요 컬러 팔레트

                    </div>

                    <div className="analysis-palette-modern">

                        {result.palette?.map((c, idx) => {

                            const hex =
                                `#${(
                                    (1 << 24)
                                    + (c.r << 16)
                                    + (c.g << 8)
                                    + c.b
                                )
                                    .toString(16)
                                    .slice(1)
                                    .toUpperCase()}`;

                            return (

                                <div
                                    key={idx}
                                    className="analysis-palette-card"
                                >

                                    <div
                                        className="analysis-palette-color"
                                        style={{
                                            backgroundColor:
                                                `rgb(${c.r}, ${c.g}, ${c.b})`
                                        }}
                                    />

                                    <span>
                                        {hex}
                                    </span>

                                </div>
                            );
                        })}

                    </div>

                </section>

                {/* TAGS */}
                <section className="analysis-section-block">

                    <div className="analysis-section-header">

                        <BarChart3 size={20} />

                        핵심 화풍 태그

                    </div>

                    <div className="analysis-tag-modern-grid">

                        {result.tags?.map((tag, idx) => (

                            <div
                                key={idx}
                                className="analysis-modern-tag-card"
                            >

                                <div className="analysis-modern-tag-name">

                                    #{tag.tag}

                                </div>

                                <div className="analysis-modern-tag-percent">

                                    {tag.displayPercent}%

                                </div>

                                <div className="analysis-modern-progress-bg">

                                    <div
                                        className="analysis-modern-progress-fill"
                                        style={{
                                            width:
                                                `${tag.displayPercent}%`
                                        }}
                                    />

                                </div>

                            </div>
                        ))}

                    </div>

                </section>

                {/* ARTISTS */}
                <section className="analysis-section-block">

                    <div className="analysis-section-header">

                        <Users size={20} />

                        나와 가장 비슷한 일러스트레이터

                    </div>

                    <div className="analysis-artists-scroll">

                        {result.top5?.map((artist, idx) => {

                            const path = artist.profileImg?.replace(
                                "https://kr.ncloudstorage.com/artlens-bucket/",
                                ""
                            );

                            return (

                                <div
                                    key={idx}
                                    className="analysis-artist-modern-card"
                                >

                                    <img
                                        src={`${API_URL}/analysis/v1/api/image/${path}`}
                                        alt={artist.artistName}
                                        className="analysis-artist-modern-image"
                                        onError={(e) => {
                                            console.log("실패 URL =", e.target.src);
                                        }}
                                    />

                                    <div className="analysis-artist-modern-info">

                                        <div className="analysis-artist-modern-rate">

                                            {Math.round(
                                                artist.similarity * 100
                                            )}%

                                        </div>

                                        <h3>
                                            {artist.artistName}
                                        </h3>

                                        <p>
                                            {artist.mainTag}
                                        </p>

                                    </div>

                                </div>
                            );
                        })}

                    </div>

                </section>

            </div>
        </div>
    );
};

export default AnalysisResultPage;