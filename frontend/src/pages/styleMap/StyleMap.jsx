import "./StyleMap.css";
import { useEffect, useRef, useState } from "react";
import axiosInstance from "../../api/axiosInstance.js";
import * as d3 from "d3";
import { FILTERS } from "../../components/data/FilterTags.js";

import { Eye, EyeOff, ChevronDown, Heart, ExternalLink } from 'lucide-react';
import { useLocation } from 'react-router-dom';
const API_URL = import.meta.env.VITE_API_URL;



const StyleMap = () => {

    const svgRef = useRef(null);
    const gRef = useRef(null);
    const zoomRef = useRef(null);
    const panelRef = useRef(null);

    const mapSize = 4000;
    const centerX = mapSize / 2;
    const centerY = mapSize / 2;

    const [overlayHidden, setOverlayHidden] = useState(false);
    const [uiVisible, setUiVisible] = useState(false);
    const [sidePanelOpen, setSidePanelOpen] = useState(false);

    const [selectedArtist, setSelectedArtist] = useState(null);
    const [selectedTag, setSelectedTag] = useState(null);
    const [activeCategory, setActiveCategory] = useState(null);

    const [artistProfileMap, setArtistProfileMap] = useState({});
    const [flashingArtistId, setFlashingArtistId] = useState(null);
    const [historyDropdownOpen, setHistoryDropdownOpen] = useState(false);
    const [showAllNames, setShowAllNames] = useState(false);
    const [selectedCluster, setSelectedCluster] = useState(null);

    const [likedArtistIds, setLikedArtistIds] = useState(new Set());
    const [onlyLiked, setOnlyLiked] = useState(false);

    const [userPosition, setUserPosition] = useState({
        x: centerX,
        y: centerY,
        visible: false
    });



    const [selectedHistory, setSelectedHistory] = useState(null);

    const clusterColors = {
        cluster_0: "#4aaddf",
        cluster_1: "#f472b6",
        cluster_2: "#fbbf24",
        cluster_3: "#a78bfa",
        cluster_4: "#7dd3fc"
    };

    const clusterLabelsText = {
        cluster_0: "선화 중심 캐주얼 화풍",
        cluster_1: "회화적 질감·수채화 도메인",
        cluster_2: "반실사·입체 명암 화풍",
        cluster_3: "네온·사이버 가상 무드",
        cluster_4: "현대 서브컬처 일러스트"
    };

    const [historyData, setHistoryData] = useState([]);
    const [nodes, setNodes] = useState([]);
    const location = useLocation();

    const incomingArtistId =
        location.state?.artistId;

    const profileImagePath =
        selectedArtist?.profileImg
            ?.replace(
                "https://kr.ncloudstorage.com/artlens-bucket/",
                ""
            )
            ?.replace(/\\/g, "/");

    useEffect(() => {

        if (!incomingArtistId) {
            return;
        }

        if (
            Object.keys(artistProfileMap).length === 0 ||
            nodes.length === 0
        ) {
            return;
        }

        setOverlayHidden(true);
        setUiVisible(true);

        openArtistPanel(
            incomingArtistId,
            800
        );

    }, [
        incomingArtistId,
        artistProfileMap,
        nodes
    ]);

    useEffect(() => {
        if (location.state?.incomingResult && svgRef.current && zoomRef.current) {
            const timer = setTimeout(() => {
                initMapFromSelection(location.state.incomingResult);
            }, 100);
            return () => clearTimeout(timer);
        }
    }, [location.state, nodes]);

    useEffect(() => {
        document.body.style.overflow = 'hidden';
        return () => {
            document.body.style.overflow = '';
        };
    }, []);

    useEffect(() => {
        axiosInstance.get(
            `${API_URL}/style-map/v1/list`,
            { withCredentials: true }
        )
            .then(res => {

                console.log("STYLE MAP =", res.data);
                console.log("DATA =", res.data.data);

                setHistoryData(res.data.data);
            })
            .catch(console.error);
    }, []);

    useEffect(() => {
        loadArtists();
        loadArtistProfiles();
        loadLikedArtists();
    }, []);

    const loadLikedArtists = async () => {
        try {
            const response = await axiosInstance.get("/like/v1/artists");
            setLikedArtistIds(new Set(response.data.data.artistIds.map(String)));
        } catch (e) {
            console.log(e);
        }
    };

    const openUserPanel = () => {
        setSelectedArtist(null);
        setSidePanelOpen(true);
    };

    const handleToggleLike = async (artistId) => {
        const key = String(artistId);
        const nextLiked = !likedArtistIds.has(key);

        setLikedArtistIds(prev => {
            const next = new Set(prev);
            nextLiked ? next.add(key) : next.delete(key);
            return next;
        });

        try {
            if (nextLiked) {
                await axiosInstance.put(`/like/v1/artists/${artistId}`);
            } else {
                await axiosInstance.delete(`/like/v1/artists/${artistId}`);
            }
        } catch (e) {
            console.log(e);
            setLikedArtistIds(prev => {
                const next = new Set(prev);
                nextLiked ? next.delete(key) : next.add(key);
                return next;
            });
        }
    };

    const loadArtists = async () => {
        try {
            const response = await axiosInstance.get("/style-map/v1/artists");
            setNodes(response.data.data);
        } catch (e) {
            console.log(e);
        }
    };

    const loadArtistProfiles = async () => {
        try {
            const response = await axiosInstance.get("/style-map/v1/artistProfiles");
            const map = {};
            response.data.data.forEach(artist => {
                map[artist.artistId] = artist;
            });
            setArtistProfileMap(map);
        } catch (e) {
            console.log(e);
        }
    };

    useEffect(() => {
        if (!svgRef.current || !gRef.current) return;

        const svg = d3.select(svgRef.current);
        const g = d3.select(gRef.current);

        const zoom = d3.zoom()
            .scaleExtent([0.2, 5])
            .on("zoom", (event) => {
                g.attr("transform", event.transform);
            });

        zoomRef.current = zoom;
        svg.call(zoom);

        const initialTransform = d3.zoomIdentity
            .translate(window.innerWidth / 2 - centerX, window.innerHeight / 2 - centerY)
            .scale(1);

        svg.call(zoom.transform, initialTransform);
    }, []);

    const focusOnUser = () => {
        if (!userPosition.visible || !svgRef.current || !zoomRef.current) return;

        const svg = d3.select(svgRef.current);
        const k = 1.2;

        const transform = d3.zoomIdentity
            .translate(window.innerWidth / 2 - userPosition.x * k, window.innerHeight / 2 - userPosition.y * k)
            .scale(k);

        svg.transition()
            .duration(1000)
            .ease(d3.easeCubicOut)
            .call(zoomRef.current.transform, transform);
    };

    const handleZoomIn = () => {
        if (!svgRef.current || !zoomRef.current) return;
        d3.select(svgRef.current)
            .transition()
            .duration(300)
            .call(zoomRef.current.scaleBy, 1.3, [window.innerWidth / 2, window.innerHeight / 2]);
    };

    const handleZoomOut = () => {
        if (!svgRef.current || !zoomRef.current) return;
        d3.select(svgRef.current)
            .transition()
            .duration(300)
            .call(zoomRef.current.scaleBy, 1 / 1.3, [window.innerWidth / 2, window.innerHeight / 2]);
    };

    const initMapFromSelection = async (item) => {
        console.log("📦 선택된 데이터 원래 구조:", item);

        let cleanItem = item;

        if (!item.top5) {
            try {
                const response = await axiosInstance.get(`/analysis/v1/result/${item.id}`);
                cleanItem = response.data?.data ? response.data.data : response.data;
                console.log("🔥 상세 데이터 로드 완료:", cleanItem);
            } catch (e) {
                console.error("🚨 상세 데이터를 가져오는 데 실패했습니다:", e);
            }
        }

        setSelectedHistory(cleanItem);
        setOverlayHidden(true);
        setUiVisible(true);

        const styleMapX = cleanItem.styleMap?.x ?? 0;
        const styleMapY = cleanItem.styleMap?.y ?? 0;

        const targetX = centerX + styleMapX * 80;
        const targetY = centerY - styleMapY * 80;

        setUserPosition({
            x: targetX,
            y: targetY,
            visible: true
        });

        if (svgRef.current && zoomRef.current) {
            const svg = d3.select(svgRef.current);
            const transform = d3.zoomIdentity
                .translate(window.innerWidth / 2 - targetX, window.innerHeight / 2 - targetY)
                .scale(1);

            svg.transition()
                .duration(1200)
                .ease(d3.easeCubicOut)
                .call(zoomRef.current.transform, transform);
        }
    };

    const openArtistPanel = (artistId, delay = 0) => {
        const artist = artistProfileMap[artistId];
        if (!artist) return;

        setSelectedArtist(artist);
        setSidePanelOpen(true);

        setTimeout(() => {
            setFlashingArtistId(artistId);
            setTimeout(() => {
                setFlashingArtistId(null);
            }, 2500);
        }, delay);

        const node = nodes.find(n => String(n.artistId) === String(artistId));
        if (node && svgRef.current && zoomRef.current) {
            let targetX = centerX + node.x * 80;
            let targetY = centerY - node.y * 80;

            const top5Index = selectedHistory?.top5?.findIndex(t =>
                String(t.artistId) === String(node.artistId) ||
                t.artistName === artistProfileMap[node.artistId]?.artistName
            );

            if (userPosition.visible) {
                if (top5Index !== undefined && top5Index !== -1) {
                    const dx = targetX - userPosition.x;
                    const dy = targetY - userPosition.y;
                    const originalAngle = Math.atan2(dy, dx);

                    const seed = Number(node.artistId) || 1;
                    const pseudoRandom1 = ((seed * 12345) % 100) / 100;
                    const pseudoRandom2 = ((seed * 67890) % 100) / 100;

                    const baseDistance = 110 + top5Index * 35;
                    const organicDistance = baseDistance + (pseudoRandom1 * 20 - 10);
                    const angleWobble = (pseudoRandom2 * 0.5) - 0.25;

                    targetX = userPosition.x + Math.cos(originalAngle + angleWobble) * organicDistance;
                    targetY = userPosition.y + Math.sin(originalAngle + angleWobble) * organicDistance;
                } else {
                    const dx = targetX - userPosition.x;
                    const dy = targetY - userPosition.y;
                    const distance = Math.sqrt(dx * dx + dy * dy);
                    const exclusionRadius = 260;

                    if (distance < exclusionRadius && distance > 0) {
                        const seed = Number(node.artistId) || 1;
                        const pseudoRandom = ((seed * 55555) % 100) / 100;
                        const pushDistance = exclusionRadius + (pseudoRandom * 40);
                        targetX = userPosition.x + (dx / distance) * pushDistance;
                        targetY = userPosition.y + (dy / distance) * pushDistance;
                    }
                }
            }

            const k = 1.5;
            const svg = d3.select(svgRef.current);
            const transform = d3.zoomIdentity
                .translate(window.innerWidth / 2 - targetX * k, window.innerHeight / 2 - targetY * k)
                .scale(k);

            svg.transition()
                .duration(1000)
                .ease(d3.easeCubicOut)
                .call(zoomRef.current.transform, transform);
        }
    };

    const formatRelativeTime = (dateString) => {
        const now = new Date();
        const target = new Date(dateString);
        const diff = Math.floor((now - target) / 1000);

        if (diff < 60) return "방금 전";
        if (diff < 3600) return `${Math.floor(diff / 60)}분 전`;
        if (diff < 86400) return `${Math.floor(diff / 3600)}시간 전`;
        if (diff < 604800) return `${Math.floor(diff / 86400)}일 전`;
        return target.toLocaleDateString("ko-KR");
    };

    return (
        <div className="style-map-page">
            <header className="style-map-navbar">
                <a href="/" className="style-map-logo">
                    <div className="style-map-logo-box"></div>
                    <span>ArtLens ai</span>
                </a>
                <ul className="style-map-nav-menu">
                    <li>이미지 분석</li>
                    <li className="active">Style Map</li>
                    <li>일러스트레이터 탐색</li>
                    <li>분석 기록</li>
                </ul>
                <div className="style-map-user-profile">
                    <div className="style-map-avatar">K</div>
                    <span>Karis</span>
                </div>
            </header>

            <div
                className={`style-map-wrapper ${!overlayHidden ? "blurred" : ""}`}
                onClick={() => {
                    setSidePanelOpen(false);
                    setHistoryDropdownOpen(false);
                }}
            >
                <svg ref={svgRef} className="style-map-canvas" width={mapSize} height={mapSize}>
                    <defs>
                        <pattern id="style-map-grid-pattern" width="100" height="100" patternUnits="userSpaceOnUse">
                            <path d="M 100 0 L 0 0 0 100" fill="none" stroke="rgba(255, 255, 255, 0.02)" strokeWidth="1" />
                        </pattern>
                    </defs>

                    <g ref={gRef}>
                        <rect x="0" y="0" width={mapSize} height={mapSize} fill="url(#style-map-grid-pattern)" />

                        {nodes.map((node) => {
                            const rawTags = artistProfileMap[node.artistId]?.tags;

                            let matchTag = selectedTag === null;
                            if (selectedTag !== null && rawTags) {
                                const tagsArray = Array.isArray(rawTags) ? rawTags : typeof rawTags === 'string' ? rawTags.split(',') : [];
                                matchTag = tagsArray.some(t => t.trim() === selectedTag.trim());
                            }

                            let matchCluster = selectedCluster === null;
                            if (selectedCluster !== null) {
                                matchCluster = node.clusterLabel === selectedCluster;
                            }

                            const matchLiked = !onlyLiked || likedArtistIds.has(String(node.artistId));

                            const isHighlighted = matchTag && matchCluster && matchLiked;
                            const nodeOpacity = isHighlighted ? 1 : 0.05;
                            const isFlashing = String(node.artistId) === String(flashingArtistId);
                            const isSelected = sidePanelOpen && selectedArtist && String(selectedArtist.artistId) === String(node.artistId);
                            const isNameVisible = isFlashing || isSelected || showAllNames;

                            const computedRadius = (Number(node.artistId) % 5) + 5;

                            let currentCx = centerX + node.x * 80;
                            let currentCy = centerY - node.y * 80;

                            const top5Index = selectedHistory?.top5?.findIndex(t =>
                                String(t.artistId) === String(node.artistId) ||
                                t.artistName === artistProfileMap[node.artistId]?.artistName
                            );

                            if (userPosition.visible) {
                                if (top5Index !== undefined && top5Index !== -1) {
                                    const dx = currentCx - userPosition.x;
                                    const dy = currentCy - userPosition.y;
                                    const originalAngle = Math.atan2(dy, dx);

                                    const seed = Number(node.artistId) || 1;
                                    const pseudoRandom1 = ((seed * 12345) % 100) / 100;
                                    const pseudoRandom2 = ((seed * 67890) % 100) / 100;

                                    const baseDistance = 110 + top5Index * 35;
                                    const organicDistance = baseDistance + (pseudoRandom1 * 20 - 10);
                                    const angleWobble = (pseudoRandom2 * 0.5) - 0.25;

                                    currentCx = userPosition.x + Math.cos(originalAngle + angleWobble) * organicDistance;
                                    currentCy = userPosition.y + Math.sin(originalAngle + angleWobble) * organicDistance;
                                } else {
                                    const dx = currentCx - userPosition.x;
                                    const dy = currentCy - userPosition.y;
                                    const distance = Math.sqrt(dx * dx + dy * dy);
                                    const exclusionRadius = 260;

                                    if (distance < exclusionRadius && distance > 0) {
                                        const seed = Number(node.artistId) || 1;
                                        const pseudoRandom = ((seed * 55555) % 100) / 100;
                                        const pushDistance = exclusionRadius + (pseudoRandom * 40);

                                        currentCx = userPosition.x + (dx / distance) * pushDistance;
                                        currentCy = userPosition.y + (dy / distance) * pushDistance;
                                    }
                                }
                            }

                            return (
                                <g
                                    key={node.artistId}
                                    className="style-map-artist-node"
                                    onClick={(e) => {
                                        e.stopPropagation();
                                        openArtistPanel(node.artistId, 0);
                                    }}
                                    style={{
                                        opacity: nodeOpacity,
                                        transition: "opacity 0.4s ease",
                                        pointerEvents: isHighlighted ? "auto" : "none"
                                    }}
                                >
                                    <circle
                                        className={`style-map-artist-circle ${isFlashing ? "flare" : ""}`}
                                        cx={currentCx}
                                        cy={currentCy}
                                        r={computedRadius}
                                        fill={clusterColors[node.clusterLabel]}
                                        style={{ color: clusterColors[node.clusterLabel] }}
                                    />

                                    <text
                                        className={`artist-name-label ${isNameVisible ? "active" : ""}`}
                                        x={currentCx}
                                        y={currentCy - 25}
                                        textAnchor="middle"
                                    >
                                        {artistProfileMap[node.artistId]?.artistName}
                                    </text>
                                </g>
                            );
                        })}

                        {userPosition.visible && (
                            <g className="style-map-user-group">
                                <circle className="style-map-user-pulse" cx={userPosition.x} cy={userPosition.y} r="16" />
                                <circle
                                    className="style-map-user-node-svg"
                                    cx={userPosition.x}
                                    cy={userPosition.y}
                                    r="16"
                                    fill="#ffffff"
                                    onClick={(e) => {
                                        e.stopPropagation();
                                        openUserPanel();
                                    }}
                                />
                                <text className="style-map-user-label" x={userPosition.x} y={userPosition.y + 40} textAnchor="middle">
                                    내 화풍
                                </text>
                            </g>
                        )}
                    </g>
                </svg>
            </div>

            {!overlayHidden && (
                <div className="style-map-initial-overlay">
                    <div className="style-map-initial-modal">
                        <h2>어떤 분석 결과를 맵에 띄울까요?</h2>
                        <p>최근 분석했던 기록 중 선택해주세요.</p>
                        <div className="style-map-card-wrapper">
                            <div className="style-map-card-grid">
                                {historyData.map((item, index) => (
                                    <div key={item.id} className="style-map-select-card" onClick={() => initMapFromSelection(item)}>
                                        {index === 0 && <span className="style-map-card-badge">가장 최근</span>}
                                        <img src={`${API_URL}/analysis/v1/api/image/${item.thumbnail}`} alt={item.title} className="style-map-card-image" />
                                        <div className="style-map-card-date">{formatRelativeTime(item.createdAt)}</div>
                                        <div className="style-map-card-title">{item.title}</div>
                                        <div className="style-map-card-tag">{item.mainTag}</div>
                                    </div>
                                ))}
                            </div>
                        </div>
                    </div>
                </div>
            )}

            {uiVisible && (
                <>
                    <div className={`style-map-history-selector ${historyDropdownOpen ? "open" : ""}`} onClick={(e) => e.stopPropagation()}>
                        <div className="style-map-selector-header" onClick={() => setHistoryDropdownOpen(!historyDropdownOpen)}>
                            <div className="style-map-selector-header-info">
                                <h4>현재 표시 중인 분석 결과</h4>
                                <p>{selectedHistory ? selectedHistory.title : "-"}</p>
                            </div>
                            <ChevronDown size={18} className="style-map-selector-chevron" />
                        </div>
                        <div className="style-map-selector-list">
                            {historyData.map((item) => (
                                <div
                                    key={item.id}
                                    className={`style-map-history-item ${selectedHistory?.id === item.id ? "active" : ""}`}
                                    onClick={() => {
                                        initMapFromSelection(item);
                                        setHistoryDropdownOpen(false);
                                    }}
                                >
                                    <img
                                        src={`${API_URL}/analysis/v1/api/image/${item.thumbnail}`}
                                        alt={item.title}
                                        className="style-map-history-thumb"
                                    />
                                    <div className="style-map-history-info">
                                        <span className="style-map-history-tag" style={{ fontSize: "14px", fontWeight: "700" }}>
                                            {item.title}
                                        </span>
                                        <span className="style-map-history-date">
                                            {formatRelativeTime(item.createdAt)} • {item.mainTag}
                                        </span>
                                    </div>
                                </div>
                            ))}
                        </div>
                    </div>

                    <div className="style-map-legend-panel">
                        <div className="style-map-legend-title">스타일 도메인 분류</div>
                        {Object.keys(clusterColors).map((clusterKey) => {
                            const isClusterActive = selectedCluster === clusterKey;
                            return (
                                <div
                                    key={clusterKey}
                                    className="style-map-legend-item"
                                    onClick={() => setSelectedCluster(isClusterActive ? null : clusterKey)}
                                    style={{
                                        cursor: "pointer",
                                        opacity: selectedCluster && !isClusterActive ? 0.35 : 1,
                                        transition: "all 0.2s ease",
                                        display: "flex",
                                        alignItems: "center",
                                        gap: "10px",
                                        marginBottom: "12px"
                                    }}
                                >
                                    <div
                                        className="style-map-legend-color"
                                        style={{
                                            background: clusterColors[clusterKey],
                                            width: "12px",
                                            height: "12px",
                                            borderRadius: "50%",
                                            flexShrink: 0,
                                            boxShadow: isClusterActive
                                                ? `0 0 15px 4px ${clusterColors[clusterKey]}`
                                                : `0 0 10px ${clusterColors[clusterKey]}`
                                        }}
                                    ></div>
                                    <span
                                        style={{
                                            color: isClusterActive ? "#ffffff" : "#a0abba",
                                            fontWeight: isClusterActive ? "800" : "600",
                                            fontSize: "13px"
                                        }}
                                    >
                                        {clusterLabelsText[clusterKey] || `#${clusterKey}`}
                                    </span>
                                </div>
                            );
                        })}
                    </div>

                    <div className="style-map-controls">
                        <button className={`style-map-control-btn ${showAllNames ? "active" : ""}`} onClick={() => setShowAllNames(!showAllNames)} title={showAllNames ? "이름 숨기기" : "이름 전체 보기"}>
                            {showAllNames ? <Eye size={20} /> : <EyeOff size={20} />}
                        </button>
                        <button className="style-map-control-btn" onClick={focusOnUser}>◎</button>
                        <button className="style-map-control-btn" onClick={handleZoomIn}>+</button>
                        <button className="style-map-control-btn" onClick={handleZoomOut}>−</button>
                    </div>

                    <div className="style-map-filter-container">
                        {activeCategory && (
                            <div className="style-map-sub-filter-panel">
                                {FILTERS[activeCategory]?.map(tag => (
                                    <button key={tag} className={`style-map-filter-btn ${selectedTag === tag ? "active" : ""}`} onClick={() => setSelectedTag(selectedTag === tag ? null : tag)}>
                                        #{tag}
                                    </button>
                                ))}
                            </div>
                        )}

                        <div className="style-map-category-bar">
                            <button
                                className={`style-map-category-btn style-map-liked-filter-btn ${onlyLiked ? "active" : ""}`}
                                onClick={() => setOnlyLiked(!onlyLiked)}
                            >
                                <Heart size={14} fill={onlyLiked ? "currentColor" : "none"} />
                                찜한 작가 {likedArtistIds.size}
                            </button>
                            <div className="style-map-category-divider"></div>
                            <button className={`style-map-category-btn ${selectedTag === null ? "active" : ""}`} onClick={() => { setSelectedTag(null); setActiveCategory(null); }}>
                                전체보기
                            </button>
                            <div className="style-map-category-divider"></div>
                            {Object.keys(FILTERS).map(category => {
                                const hasActiveTag = selectedTag && FILTERS[category].includes(selectedTag);
                                return (
                                    /* 🎯 [핵심 보정] 이미 활성화된 카테고리를 다시 클릭하면 null로 토글 차단 연출 */
                                    <button
                                        key={category}
                                        className={`style-map-category-btn ${activeCategory === category || hasActiveTag ? "active" : ""}`}
                                        onClick={() => setActiveCategory(activeCategory === category ? null : category)}
                                    >
                                        {category}
                                    </button>
                                );
                            })}
                        </div>
                    </div>

                    <div ref={panelRef} className={`style-map-side-panel ${sidePanelOpen ? "open" : ""}`}>
                        <button className="style-map-panel-close" onClick={() => setSidePanelOpen(false)}>✕</button>
                        {selectedArtist && (
                            <>

                                <div className="style-map-panel-image-wrap">
                                    <img src={`${API_URL}/analysis/v1/api/image/${profileImagePath}`} alt={selectedArtist.artistName} className="style-map-panel-image" />
                                    <button
                                        className={`style-map-panel-like-btn ${likedArtistIds.has(String(selectedArtist.artistId)) ? "liked" : ""}`}
                                        onClick={() => handleToggleLike(selectedArtist.artistId)}
                                        title={likedArtistIds.has(String(selectedArtist.artistId)) ? "찜 해제" : "찜하기"}
                                    >
                                        <Heart size={18} fill={likedArtistIds.has(String(selectedArtist.artistId)) ? "currentColor" : "none"} />
                                    </button>
                                </div>
                                <h2>{selectedArtist.artistName}</h2>
                                <p>{selectedArtist.styleDesc}</p>
                                <div className="style-map-panel-tags">
                                    {selectedArtist.tags?.map(tag => (
                                        <span key={tag} className="style-map-tag">
                                            {tag?.startsWith('#') ? tag : `#${tag || ''}`}
                                        </span>
                                    ))}
                                </div>

                                <button
                                    className="style-map-panel-safebooru-btn"
                                    onClick={() => window.open(
                                        `https://safebooru.org/index.php?page=post&s=list&tags=${selectedArtist.artistName}`,
                                        '_blank'
                                    )}
                                >
                                    Safebooru 작품 더보기
                                    <ExternalLink size={16} />
                                </button>
                            </>
                        )}

                        {!selectedArtist && selectedHistory && (
                            <>
                                <img src={`${API_URL}/analysis/v1/api/image/${selectedHistory.thumbnail || selectedHistory.imgKey}`} alt={selectedHistory.title} className="style-map-panel-image" />
                                <h2>내 그림</h2>
                                <p>{selectedHistory.title}</p>
                                <div className="style-map-panel-tags">
                                    <span className="style-map-tag">
                                        {selectedHistory.mainTag?.startsWith('#') ? selectedHistory.mainTag : `#${selectedHistory.mainTag || selectedHistory.tags?.[0]?.tag || ''}`}
                                    </span>
                                </div>

                                {selectedHistory.top5?.map((artist, idx) => {

                                    const imagePath =
                                        artist.profileImg
                                            ?.replace(
                                                "https://kr.ncloudstorage.com/artlens-bucket/",
                                                ""
                                            );

                                    return (
                                        <div
                                            key={idx}
                                            className="style-map-top5-item"
                                            onClick={(e) => {
                                                e.stopPropagation();

                                                const targetId =
                                                    artist.artistId ||
                                                    Object.keys(artistProfileMap).find(
                                                        id =>
                                                            artistProfileMap[id].artistName ===
                                                            artist.artistName
                                                    );

                                                if (targetId) {
                                                    openArtistPanel(targetId, 800);
                                                }
                                            }}
                                        >
                                            <img
                                                src={`${API_URL}/analysis/v1/api/image/${imagePath}`}
                                                alt={artist.artistName}
                                                className="style-map-top5-avatar"
                                            />

                                            <div className="style-map-top5-info">
                                                <span className="style-map-top5-name">
                                                    {artist.artistName}
                                                </span>

                                                                                <span className="style-map-top5-tag">
                                                    {artist.mainTag?.startsWith('#')
                                                        ? artist.mainTag
                                                        : `#${artist.mainTag || artist.tags?.[0]?.tag || '태그 없음'}`}
                                                </span>
                                            </div>

                                            <div className="style-map-top5-rate">
                                                {Math.round(artist.similarity * 100)}%
                                            </div>
                                        </div>
                                    );
                                })}
                            </>
                        )}
                    </div>
                </>
            )}
        </div>
    );
};

export default StyleMap;