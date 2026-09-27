// src/pages/analysis/AnalysisResultView.jsx
//
// 분석 결과를 그려주는 순수 표시용 컴포넌트.
// AnalysisResult.jsx(로그인 유저, 데이터 패칭 + 공유 버튼)와
// ShareView.jsx(비로그인 공개 페이지)가 같이 재사용한다.

import './AnalysisResult.css';

import {
    Palette,
    BarChart3,
    Users
} from 'lucide-react';

const API_URL = import.meta.env.VITE_API_URL;

const AnalysisResultView = ({ result, headerActions }) => {

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

                    {headerActions && (

                        <div className="analysis-header-actions">

                            {headerActions}

                        </div>
                    )}

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

export default AnalysisResultView;
