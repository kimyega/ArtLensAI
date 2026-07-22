// ArtistExplore.jsx

import './ArtistExplore.css';

import {
    Search,
    ArrowRight,
    X,
    MessageSquare,
    Hash,
    Map,
    ExternalLink,
    Image as ImageIcon
} from 'lucide-react';

import {
    useEffect,
    useState
} from 'react';
import axiosInstance from "../../api/axiosInstance.js";

import {FILTERS} from "../../components/data/FilterTags.js";

const API_URL = import.meta.env.VITE_API_URL;
import { useNavigate } from "react-router-dom";




const ArtistExplore = () => {

    const [selectedArtist, setSelectedArtist] =
        useState(null);

    const [selectedFilters, setSelectedFilters] =
        useState({

            STYLE: [],
            COLOR: [],
            LIGHTING: [],
            COMPOSITION: [],
            MOOD: []
        });


    const [openedCategory, setOpenedCategory] =
        useState({

            STYLE: true,
            COLOR: false,
            LIGHTING: false,
            COMPOSITION: false,
            MOOD: false
        });

    const [artists, setArtists] =
        useState([]);

    const [keyword, setKeyword] =
        useState('');

    const navigate = useNavigate();

    const getImagePath = (url) =>
        url
            ?.replace(
                "https://kr.ncloudstorage.com/artlens-bucket/",
                ""
            )
            ?.replace(/\\/g, "/");

    useEffect(() => {

        if (selectedArtist) {

            document.body.style.overflow =
                'hidden';

        } else {

            document.body.style.overflow = '';
        }

    }, [selectedArtist]);

    useEffect(() => {

        const fetchArtists = async () => {

            try {

                const params =
                    new URLSearchParams();

                // =========================
                // keyword
                // =========================
                if (keyword.trim()) {

                    params.append(
                        "keyword",
                        keyword
                    );
                }

                // =========================
                // STYLE
                // =========================
                selectedFilters.STYLE
                    .forEach(tag => {

                        params.append(
                            "style",
                            tag
                        );
                    });

                // =========================
                // COLOR
                // =========================
                selectedFilters.COLOR
                    .forEach(tag => {

                        params.append(
                            "color",
                            tag
                        );
                    });

                // =========================
                // LIGHTING
                // =========================
                selectedFilters.LIGHTING
                    .forEach(tag => {

                        params.append(
                            "lighting",
                            tag
                        );
                    });

                // =========================
                // COMPOSITION
                // =========================
                selectedFilters.COMPOSITION
                    .forEach(tag => {

                        params.append(
                            "composition",
                            tag
                        );
                    });

                // =========================
                // MOOD
                // =========================
                selectedFilters.MOOD
                    .forEach(tag => {

                        params.append(
                            "mood",
                            tag
                        );
                    });

                const res =
                    await axiosInstance.get(

                        `/artist/v1/explore?${params.toString()}`
                    );

                console.log(
                    res.data
                );

                setArtists(
                    res.data
                );

            } catch (e) {

                console.error(e);
            }
        };

        fetchArtists();

    }, [
        keyword,
        JSON.stringify(selectedFilters)
    ]);

    const handleFilterClick = (
        category,
        tag
    ) => {

        setSelectedFilters(prev => {

            const exists =
                prev[category].includes(tag);

            return {

                ...prev,

                [category]: exists

                    ? prev[category].filter(
                        t => t !== tag
                    )

                    : [
                        ...prev[category],
                        tag
                    ]
            };
        });
    };

    const toggleCategory = (
        category
    ) => {

        setOpenedCategory(prev => ({

            ...prev,

            [category]:
                !prev[category]
        }));
    };

    return (

        <div className="artist-explore-page">

            {/* BG */}
            <div className="artist-explore-bg-container">

                <div className="artist-explore-bg-glow artist-explore-glow-1"></div>

                <div className="artist-explore-bg-glow artist-explore-glow-2"></div>

            </div>

            {/* CONTENT */}
            <div className="artist-explore-wrapper">

                {/* HEADER */}
                <div className="artist-explore-page-header">

                    <div className="artist-explore-header-title">

                        <h1>
                            일러스트레이터 탐색
                        </h1>

                        <p>
                            Safebooru 기반
                            아티스트 도감에서
                            취향에 맞는 화풍을
                            탐색해보세요.
                        </p>

                    </div>

                </div>

                {/* SEARCH */}
                <div className="artist-explore-search-filter-bar">

                    <div className="artist-explore-search-box">

                        <Search
                            size={20}
                            className="artist-explore-search-icon"
                        />

                        <input
                            type="text"
                            className="artist-explore-search-input"
                            placeholder="작가 이름 또는 화풍 태그 검색"
                            value={keyword}
                            onChange={(e) =>
                                setKeyword(e.target.value)
                            }
                        />

                    </div>

                    <div className="artist-explore-filter-groups">

                        {Object.entries(FILTERS).map(

                            ([category, tags]) => (

                                <div
                                    key={category}
                                    className="artist-explore-filter-group"
                                >

                                    <div
                                        className="artist-explore-filter-header"
                                        onClick={() =>
                                            toggleCategory(category)
                                        }
                                    >

                                        <h4 className="artist-explore-filter-category">

                                            {category}

                                        </h4>

                                        <span>

            {
                openedCategory[
                    category
                    ]

                    ? '−'
                    : '+'
            }

        </span>

                                    </div>

                                    {openedCategory[
                                        category
                                        ] && (

                                        <div className="artist-explore-filter-tags">

                                            {tags.map(tag => {

                                                const isActive =
                                                    selectedFilters[
                                                        category
                                                        ].includes(tag);

                                                return (

                                                    <div
                                                        key={tag}
                                                        className={`artist-explore-filter-tag ${
                                                            isActive
                                                                ? 'active'
                                                                : ''
                                                        }`}
                                                        onClick={() =>
                                                            handleFilterClick(
                                                                category,
                                                                tag
                                                            )
                                                        }
                                                    >

                                                        #{tag}

                                                    </div>
                                                );
                                            })}

                                        </div>
                                    )}

                                </div>

                            )
                        )}

                    </div>

                </div>

                {/* GRID */}
                <div className="artist-explore-grid">

                    {artists.map((artist) => (

                        <div
                            key={artist.artistId}
                            className="artist-explore-card"
                            onClick={() =>
                                setSelectedArtist(
                                    artist
                                )
                            }
                        >

                            <div className="artist-explore-card-cover">

                                <img
                                    src={`${API_URL}/analysis/v1/api/image/${getImagePath(artist.profileImg)}`}
                                    alt={artist.artistName}
                                    className="artist-explore-cover-img"
                                />

                            </div>

                            <div className="artist-explore-card-body">

                                <div className="artist-explore-artist-header">

                                    <img
                                        src={`${API_URL}/analysis/v1/api/image/${getImagePath(artist.profileImg)}`}
                                        alt={artist.artistName}
                                        className="artist-explore-avatar"
                                    />

                                    <div className="artist-explore-info-short">

                                        <h3 className="artist-explore-name-short">

                                            {artist.artistName}

                                        </h3>

                                        <span className="artist-explore-post-count">

                                            <ImageIcon
                                                size={
                                                    12
                                                }
                                            />

                                            Safebooru{' '}
                                            {
                                                artist.posts
                                            }

                                        </span>

                                    </div>

                                </div>

                                <div className="artist-explore-card-tags">

                                    {artist.tags.map(
                                        (
                                            tag
                                        ) => (

                                            <span
                                                key={
                                                    tag
                                                }
                                                className="artist-explore-tag"
                                            >
                                                {
                                                    tag
                                                }
                                            </span>
                                        )
                                    )}

                                </div>

                                <div className="artist-explore-card-action">

                                    <span className="artist-explore-action-text">

                                        작가 상세 정보

                                    </span>

                                    <ArrowRight
                                        size={16}
                                        className="artist-explore-action-icon"
                                    />

                                </div>

                            </div>

                        </div>
                    ))}

                </div>

            </div>

            {/* MODAL */}
            <div
                className={`artist-explore-modal-overlay ${
                    selectedArtist
                        ? 'show'
                        : ''
                }`}
                onClick={(e) => {

                    if (
                        e.target.classList.contains(
                            'artist-explore-modal-overlay'
                        )
                    ) {

                        setSelectedArtist(
                            null
                        );
                    }
                }}
            >

                {selectedArtist && (

                    <div className="artist-explore-modal">

                        <div
                            className="artist-explore-modal-close"
                            onClick={() =>
                                setSelectedArtist(
                                    null
                                )
                            }
                        >

                            <X size={20} />

                        </div>

                        <img
                            src={`${API_URL}/analysis/v1/api/image/${getImagePath(selectedArtist.profileImg)}`}
                            alt=""
                            className="artist-explore-modal-cover"
                        />

                        <div className="artist-explore-modal-body">

                            <div className="artist-explore-modal-header-info">

                                <img
                                    src={`${API_URL}/analysis/v1/api/image/${getImagePath(selectedArtist.profileImg)}`}
                                    alt={selectedArtist.artistName}
                                    className="artist-explore-modal-avatar"
                                />

                                <div className="artist-explore-modal-name-group">

                                    <h2 className="artist-explore-modal-name">

                                        {
                                            selectedArtist.artistName
                                        }

                                    </h2>

                                    <div className="artist-explore-modal-post-count">

                                        Safebooru 수집 데이터{'AI 기반 화풍 분석'}
                                        {
                                            selectedArtist.posts
                                        }

                                    </div>

                                </div>

                            </div>

                            <div className="artist-explore-modal-section">

                                <div className="artist-explore-modal-section-title">

                                    <MessageSquare
                                        size={18}
                                    />

                                    아티스트 스타일 분석

                                </div>

                                <p className="artist-explore-modal-desc">

                                    {
                                        selectedArtist.styleDesc
                                    }

                                </p>

                            </div>

                            <div className="artist-explore-modal-section">

                                <div className="artist-explore-modal-section-title">

                                    <Hash
                                        size={18}
                                    />

                                    주요 화풍 태그

                                </div>

                                <div className="artist-explore-modal-tags">

                                    {selectedArtist.tags.map(
                                        (
                                            tag
                                        ) => (

                                            <span
                                                key={
                                                    tag
                                                }
                                                className="artist-explore-tag"
                                            >
                                                {
                                                    tag
                                                }
                                            </span>
                                        )
                                    )}

                                </div>

                            </div>

                            <div className="artist-explore-modal-actions">

                                <button
                                    className="artist-explore-btn artist-explore-btn-outline"
                                    onClick={() =>
                                        navigate(
                                            '/style-map',
                                            {
                                                state: {
                                                    artistId: selectedArtist.artistId
                                                }
                                            }
                                        )
                                    }
                                >
                                    <Map size={18}/>
                                    Style Map 위치 보기
                                </button>

                                <button
                                    className="artist-explore-btn artist-explore-btn-primary"
                                    onClick={() =>
                                        window.open(
                                            `https://safebooru.org/index.php?page=post&s=list&tags=${selectedArtist.artistName}`,
                                            '_blank'
                                        )
                                    }
                                >

                                    Safebooru 작품 더보기

                                    <ExternalLink
                                        size={18}
                                    />

                                </button>

                            </div>

                        </div>

                    </div>
                )}

            </div>

        </div>
    );
};

export default ArtistExplore;