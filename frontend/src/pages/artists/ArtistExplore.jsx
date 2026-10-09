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
    useRef,
    useState
} from 'react';
import axiosInstance from "../../api/axiosInstance.js";

import {FILTERS} from "../../components/data/FilterTags.js";

const API_URL = import.meta.env.VITE_API_URL;
import { useNavigate } from "react-router-dom";

// 태그 -> 카테고리 역매핑 (자동완성 시 태그가 속한 카테고리를 찾기 위함)
const TAG_CATEGORY_MAP = Object.entries(FILTERS)
    .reduce((acc, [category, tags]) => {

        tags.forEach(tag => {
            acc[tag] = category;
        });

        return acc;

    }, {});

const ALL_TAGS = Object.values(FILTERS).flat();


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

            STYLE: false,
            COLOR: false,
            LIGHTING: false,
            COMPOSITION: false,
            MOOD: false
        });

    const [artists, setArtists] =
        useState([]);

    const [query, setQuery] =
        useState('');

    const [suggestions, setSuggestions] =
        useState([]);

    const [showSuggestions, setShowSuggestions] =
        useState(false);

    const [activeSuggestionIndex, setActiveSuggestionIndex] =
        useState(-1);

    const [selectedArtistNames, setSelectedArtistNames] =
        useState([]);

    const searchBoxRef = useRef(null);

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
                // artistName (선택된 작가 칩)
                // =========================
                selectedArtistNames
                    .forEach(name => {

                        params.append(
                            "artistName",
                            name
                        );
                    });

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
        JSON.stringify(selectedArtistNames),
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

    // =========================
    // 자동완성 (태그는 로컬 사전, 작가는 서버 조회)
    // =========================
    useEffect(() => {

        const trimmed = query.trim();

        if (!trimmed) {

            setSuggestions([]);
            setShowSuggestions(false);
            setActiveSuggestionIndex(-1);
            return;
        }

        const timer = setTimeout(async () => {

            const lower = trimmed.toLowerCase();

            const tagSuggestions = ALL_TAGS
                .filter(tag =>
                    tag.toLowerCase().includes(lower) &&
                    !selectedFilters[TAG_CATEGORY_MAP[tag]].includes(tag)
                )
                .slice(0, 5)
                .map(tag => ({
                    type: "TAG",
                    value: tag,
                    category: TAG_CATEGORY_MAP[tag]
                }));

            let artistSuggestions = [];

            try {

                const res = await axiosInstance.get(

                    `/artist/v1/explore/suggest?keyword=${encodeURIComponent(trimmed)}`
                );

                artistSuggestions = (res.data || [])
                    .filter(name =>
                        !selectedArtistNames.includes(name)
                    )
                    .slice(0, 5)
                    .map(name => ({
                        type: "ARTIST",
                        value: name
                    }));

            } catch (e) {

                console.error(e);
            }

            setSuggestions([
                ...artistSuggestions,
                ...tagSuggestions
            ]);

            setShowSuggestions(true);
            setActiveSuggestionIndex(-1);

        }, 250);

        return () => clearTimeout(timer);

    }, [
        query,
        JSON.stringify(selectedFilters),
        JSON.stringify(selectedArtistNames)
    ]);

    // =========================
    // 검색창 바깥 클릭 시 자동완성 닫기
    // =========================
    useEffect(() => {

        const handleClickOutside = (e) => {

            if (
                searchBoxRef.current &&
                !searchBoxRef.current.contains(e.target)
            ) {

                setShowSuggestions(false);
            }
        };

        document.addEventListener(
            "mousedown",
            handleClickOutside
        );

        return () =>
            document.removeEventListener(
                "mousedown",
                handleClickOutside
            );

    }, []);

    const handleSelectSuggestion = (suggestion) => {

        if (suggestion.type === "ARTIST") {

            setSelectedArtistNames(prev =>

                prev.includes(suggestion.value)
                    ? prev
                    : [...prev, suggestion.value]
            );

        } else {

            handleFilterClick(
                suggestion.category,
                suggestion.value
            );
        }

        setQuery('');
        setSuggestions([]);
        setShowSuggestions(false);
        setActiveSuggestionIndex(-1);
    };

    const handleSearchKeyDown = (e) => {

        if (!showSuggestions || suggestions.length === 0) {

            return;
        }

        if (e.key === 'ArrowDown') {

            e.preventDefault();

            setActiveSuggestionIndex(prev =>
                (prev + 1) % suggestions.length
            );

        } else if (e.key === 'ArrowUp') {

            e.preventDefault();

            setActiveSuggestionIndex(prev =>
                (prev - 1 + suggestions.length) % suggestions.length
            );

        } else if (e.key === 'Enter') {

            if (activeSuggestionIndex >= 0) {

                e.preventDefault();

                handleSelectSuggestion(
                    suggestions[activeSuggestionIndex]
                );
            }

        } else if (e.key === 'Escape') {

            setShowSuggestions(false);
            setActiveSuggestionIndex(-1);
        }
    };

    const handleRemoveArtistChip = (name) => {

        setSelectedArtistNames(prev =>
            prev.filter(n => n !== name)
        );
    };

    const selectedTagChips =
        Object.entries(selectedFilters)
            .flatMap(([category, tags]) =>
                tags.map(tag => ({
                    category,
                    tag
                }))
            );

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

                    <div
                        className="artist-explore-search-box-wrapper"
                        ref={searchBoxRef}
                    >

                        <div className="artist-explore-search-box">

                            <Search
                                size={20}
                                className="artist-explore-search-icon"
                            />

                            {selectedArtistNames.map((name) => (

                                <div
                                    key={`artist-${name}`}
                                    className="artist-explore-search-chip artist"
                                >

                                    {name}

                                    <X
                                        size={14}
                                        onClick={() =>
                                            handleRemoveArtistChip(name)
                                        }
                                    />

                                </div>
                            ))}

                            {selectedTagChips.map(
                                ({category, tag}) => (

                                    <div
                                        key={`tag-${category}-${tag}`}
                                        className="artist-explore-search-chip tag"
                                    >

                                        #{tag}

                                        <X
                                            size={14}
                                            onClick={() =>
                                                handleFilterClick(
                                                    category,
                                                    tag
                                                )
                                            }
                                        />

                                    </div>
                                )
                            )}

                            <input
                                type="text"
                                className="artist-explore-search-input"
                                placeholder={
                                    selectedArtistNames.length > 0 ||
                                    selectedTagChips.length > 0
                                        ? ""
                                        : "작가 이름 또는 화풍 태그 검색"
                                }
                                value={query}
                                onChange={(e) =>
                                    setQuery(e.target.value)
                                }
                                onKeyDown={handleSearchKeyDown}
                                onFocus={() => {

                                    if (suggestions.length > 0) {

                                        setShowSuggestions(true);
                                    }
                                }}
                            />

                        </div>

                        {showSuggestions &&
                            suggestions.length > 0 && (

                            <ul className="artist-explore-suggestions">

                                {suggestions.map((s, index) => (

                                    <li
                                        key={`${s.type}-${s.value}`}
                                        className={`artist-explore-suggestion-item ${
                                            index === activeSuggestionIndex
                                                ? 'active'
                                                : ''
                                        }`}
                                        onMouseEnter={() =>
                                            setActiveSuggestionIndex(index)
                                        }
                                        onClick={() =>
                                            handleSelectSuggestion(s)
                                        }
                                    >

                                        <span
                                            className={`artist-explore-suggestion-type ${
                                                s.type === "ARTIST"
                                                    ? "artist"
                                                    : "tag"
                                            }`}
                                        >
                                            {s.type === "ARTIST"
                                                ? "작가"
                                                : "태그"}
                                        </span>

                                        <span className="artist-explore-suggestion-value">
                                            {s.type === "ARTIST"
                                                ? s.value
                                                : `#${s.value}`}
                                        </span>

                                    </li>
                                ))}

                            </ul>
                        )}

                    </div>

                    <div className="artist-explore-filter-groups">

                        <div className="artist-explore-filter-category-row">

                            {Object.entries(FILTERS).map(

                                ([category, tags]) => {

                                    const count =
                                        selectedFilters[category].length;

                                    return (

                                        <button
                                            key={category}
                                            type="button"
                                            className={`artist-explore-filter-chip ${
                                                openedCategory[category]
                                                    ? 'open'
                                                    : ''
                                            } ${
                                                count > 0
                                                    ? 'has-selection'
                                                    : ''
                                            }`}
                                            onClick={() =>
                                                toggleCategory(category)
                                            }
                                        >

                                            {category}

                                            {count > 0 && (
                                                <span className="artist-explore-filter-count">
                                                    {count}
                                                </span>
                                            )}

                                        </button>
                                    );
                                }
                            )}

                        </div>

                        {Object.entries(FILTERS).map(

                            ([category, tags]) =>

                                openedCategory[category] && (

                                    <div
                                        key={category}
                                        className="artist-explore-filter-tags"
                                    >

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