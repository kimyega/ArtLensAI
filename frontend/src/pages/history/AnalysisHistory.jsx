import React, { useEffect, useState } from "react";
import { useNavigate } from "react-router-dom";
import { Search, Calendar, ArrowRight, Share2, X, Trash2, CheckSquare, Square } from "lucide-react";
import axiosInstance from "../../api/axiosInstance";
import ShareButton from "../../components/share/ShareButton.jsx";
import "./AnalysisHistory.css";
const API_URL = import.meta.env.VITE_API_URL;

const AnalysisHistory = () => {
    const navigate = useNavigate();
    const [historyData, setHistoryData] = useState([]);
    const [searchTerm, setSearchTerm] = useState("");
    const [loading, setLoading] = useState(true);
    const [selectedImg, setSelectedImg] = useState(null);

    // 🎯 [신규 상태] 현재 어떤 카드 마크에 마우스가 호버되었는지 고유 ID 추적
    const [hoveredItemId, setHoveredItemId] = useState(null);

    const [isDeleteMode, setIsDeleteMode] = useState(false);
    const [checkedIds, setCheckedIds] = useState([]);

    const [deleteModalOpen, setDeleteModalOpen] = useState(false);

    const [deleteTargetIds, setDeleteTargetIds] = useState([]);

    // 카드별 공유 상태를 한 번에 받아둠 (resultId -> ShareLinkDTO)
    const [sharesByResultId, setSharesByResultId] = useState({});

    useEffect(() => {
        axiosInstance.get("/style-map/v1/list", { withCredentials: true })
            .then((res) => {
                setHistoryData(res.data.data || []);
                setLoading(false);
            })
            .catch((err) => {
                console.error("🚨 분석 기록 로드 실패:", err);
                setLoading(false);
            });
    }, []);

    // =============================
    // 공유 상태 배치 조회 (카드마다 개별 호출하지 않도록 한 번에)
    // =============================
    useEffect(() => {

        if (historyData.length === 0) return;

        const params = new URLSearchParams();

        historyData.forEach(item => {
            params.append("resultIds", item.id);
        });

        axiosInstance.get(`/share/v1/mine/batch?${params.toString()}`)
            .then(res => {
                setSharesByResultId(res.data || {});
            })
            .catch(err => {
                console.error("🚨 공유 상태 조회 실패:", err);
            });

    }, [historyData]);

    // 🎯 삭제 처리 함수
    const confirmDelete = async () => {

        try {

            console.log(deleteTargetIds);
            console.log(typeof deleteTargetIds[0]);

            await axiosInstance.delete(
                "/history/v1/delete",
                {
                    data: {
                        ids:
                        deleteTargetIds
                    }
                }
            );

            setHistoryData(
                prev =>
                    prev.filter(
                        item =>
                            !deleteTargetIds.includes(
                                item.historyId
                            )
                    )
            );

            setCheckedIds([]);

            setDeleteModalOpen(false);

            setDeleteTargetIds([]);

            setIsDeleteMode(false);

        } catch (e) {

            alert("삭제 실패");
        }
    };

    const handleDelete = (ids) => {

        const idList =
            Array.isArray(ids)
                ? ids
                : [ids];

        setDeleteTargetIds(
            idList
        );

        setDeleteModalOpen(
            true
        );
    };


    // 체크박스 토글
    const toggleCheck = (id) => {
        setCheckedIds(prev => prev.includes(id) ? prev.filter(i => i !== id) : [...prev, id]);
    };

    const formatRelativeTime = (dateString) => {
        const now = new Date();
        const target = new Date(dateString);
        const diff = Math.floor((now - target) / 1000);

        if (diff < 60) return "방금 전";
        if (diff < 3600) return `${Math.floor(diff / 60)}분 전`;
        if (diff < 86400) return `${Math.floor(diff / 3600)}시간 전`;
        return target.toLocaleDateString("ko-KR", { year: 'numeric', month: 'long', day: 'numeric' });
    };

    const filteredData = historyData.filter((item) => {
        const matchTitle = item.title?.toLowerCase().includes(searchTerm.toLowerCase());
        const matchTag = item.mainTag?.toLowerCase().includes(searchTerm.toLowerCase());
        return matchTitle || matchTag;
    });

    console.log(filteredData[0]);
    console.log(
        filteredData.map(
            item => item.historyId
        )
    );

    const formatDetailedTime = (dateString) => {
        if (!dateString) return "";
        const date = new Date(dateString);
        const year = date.getFullYear();
        const month = date.getMonth() + 1;
        const day = date.getDate();
        const hours = String(date.getHours()).padStart(2, "0");
        const minutes = String(date.getMinutes()).padStart(2, "0");
        return `${year}년 ${month}월 ${day}일 ${hours}:${minutes}`;
    };

    return (
        <div className="analysis-history-page">
            <div className="analysis-history-header-section">
                <div className="analysis-history-title-area">
                    <h2>분석 기록</h2>
                    <p>지금까지 분석한 이미지의 화풍 스타일 리포트 보관함입니다.</p>
                    <div className="history-toolbar">
                        {
                            !isDeleteMode ? (

                                <button
                                    className="history-mode-btn edit"
                                    onClick={() =>
                                        setIsDeleteMode(true)
                                    }
                                >
                                    선택 삭제
                                </button>

                            ) : (

                                <>
                                    <button
                                        className="history-mode-btn"
                                        onClick={() => {

                                            setIsDeleteMode(
                                                false
                                            );

                                            setCheckedIds(
                                                []
                                            );
                                        }}
                                    >
                                        취소
                                    </button>

                                    <button
                                        className="history-mode-btn delete"
                                        onClick={() =>
                                            handleDelete(
                                                checkedIds
                                            )
                                        }
                                        disabled={
                                            checkedIds.length === 0
                                        }
                                    >
                                        삭제 (
                                        {
                                            checkedIds.length
                                        }
                                        )
                                    </button>
                                </>
                            )
                        }
                    </div>
                </div>

                <div className="analysis-history-search-bar">
                    <Search size={18} className="analysis-history-search-icon" />
                    <input
                        type="text"
                        placeholder="작품 이름이나 태그로 검색..."
                        value={searchTerm}
                        onChange={(e) => setSearchTerm(e.target.value)}
                    />
                </div>
            </div>

            {loading ? (
                <div className="analysis-history-loading">데이터를 불러오는 중입니다...</div>
            ) : filteredData.length === 0 ? (
                <div className="analysis-history-empty">
                    <p>보관된 분석 기록이 없습니다.</p>
                    <span>새로운 이미지를 분석하여 화풍을 기록해 보세요.</span>
                </div>
            ) : (
                <div className="analysis-history-card-grid">
                    {filteredData.map((item) => {
                        const imgUrl = `${API_URL}/analysis/v1/api/image/${item.thumbnail}`;

                        return (
                            <div
                                key={item.historyId}
                                className={`
                                    analysis-history-report-card
                                    ${
                                        checkedIds.includes(item.historyId)
                                            ? "selected"
                                            : ""
                                    }`}
                                onClick={() => {

                                    if (!isDeleteMode) {
                                        return;
                                    }

                                    toggleCheck(item.historyId);
                                }}
                            >
                                <div
                                    className="analysis-history-card-image-wrap"
                                    onClick={() => {

                                        if (isDeleteMode) {
                                            return;
                                        }

                                        setSelectedImg(imgUrl);
                                    }}
                                >
                                    {
                                        isDeleteMode && (

                                            <div
                                                className={`
                                                analysis-history-checkbox
                                                ${
                                                    checkedIds.includes(item.historyId)
                                                        ? "checked"
                                                        : ""
                                                    }
                                                `}
                                                onClick={(e) => {

                                                    e.stopPropagation();

                                                    toggleCheck(
                                                        item.historyId
                                                    );
                                                }}
                                            >

                                                {
                                                    checkedIds.includes(
                                                        item.historyId
                                                    )

                                                        ? <CheckSquare size={20}/>

                                                        : <Square size={20}/>
                                                }

                                            </div>
                                        )
                                    }

                                    {!isDeleteMode && (
                                        <button
                                            className="analysis-history-delete-btn"
                                            onClick={(e) => {
                                                e.stopPropagation();
                                                handleDelete(item.historyId);
                                            }}
                                        >
                                            <Trash2 size={16}/>
                                        </button>
                                    )}

                                    {!isDeleteMode && (
                                        <ShareButton
                                            resultId={item.id}
                                            title={item.title}
                                            imgUrl={imgUrl}
                                            initialShare={sharesByResultId[item.id] ?? null}
                                            skipInitialFetch
                                            stopPropagation
                                            className="analysis-history-share-icon-btn"
                                        >
                                            <Share2 size={16} />
                                        </ShareButton>
                                    )}

                                    <img
                                        src={imgUrl}
                                        alt={item.title}
                                        onError={(e) => {
                                            e.target.src = "https://images.unsplash.com/photo-1579783902614-a3fb3927b6a5?w=500";
                                        }}
                                    />
                                    <span className="analysis-history-card-badge">{item.mainTag}</span>
                                </div>

                                <div className="analysis-history-card-content-wrap">
                                    <div className="analysis-history-card-meta">
                                        {/* 🎯 마우스 진입/이탈 이벤트를 캐치하여 커스텀 툴팁을 트리거합니다 */}
                                        <div
                                            className="analysis-history-time-trigger"
                                            onMouseEnter={() => setHoveredItemId(item.historyId)}
                                            onMouseLeave={() => setHoveredItemId(null)}
                                            style={{ cursor: "default" }}
                                        >
                                            <span className="analysis-history-meta-item">
                                                <Calendar size={12} />
                                                {formatRelativeTime(item.createdAt)}
                                            </span>

                                            {/* 호버되었을 때만 0초 만에 튀어나오는 고해상도 테크니컬 툴팁 */}
                                            {hoveredItemId === item.historyId && (
                                                <div className="analysis-history-custom-tooltip">
                                                    {formatDetailedTime(item.createdAt)}
                                                </div>
                                            )}
                                        </div>
                                    </div>
                                    <h3 className="analysis-history-card-title">{item.title}</h3>

                                    <div className="analysis-history-card-action-area">
                                        <button
                                            className="analysis-history-action-btn analysis-history-dashboard-btn"
                                            onClick={(e) => {

                                                e.stopPropagation();

                                                if (isDeleteMode) {
                                                    return;
                                                }

                                                navigate(
                                                    `/analysis/result/${item.id}`
                                                );
                                            }}
                                        >
                                            리포트 보기
                                        </button>
                                        <button
                                            className="analysis-history-action-btn analysis-history-map-btn"
                                            onClick={(e) => {

                                                e.stopPropagation();

                                                if (isDeleteMode) {
                                                    return;
                                                }

                                                navigate(
                                                    "/style-map",
                                                    {
                                                        state: {
                                                            incomingResult: item
                                                        }
                                                    }
                                                );
                                            }}
                                        >
                                            위치 확인 <ArrowRight size={14} />
                                        </button>
                                    </div>
                                </div>
                            </div>
                        );
                    })}
                </div>
            )}

            {selectedImg && (
                <div
                    className="analysis-history-preview-overlay"
                    onClick={() => setSelectedImg(null)}
                >
                    <div className="analysis-history-preview-content" onClick={(e) => e.stopPropagation()}>
                        <button
                            className="analysis-history-preview-close-btn"
                            onClick={() => setSelectedImg(null)}
                        >
                            <X size={24} />
                        </button>
                        <img
                            src={selectedImg}
                            alt="확대 프리뷰"
                            className="analysis-history-preview-image"
                        />
                    </div>
                </div>
            )}

            {
                deleteModalOpen && (

                    <div
                        className="analysis-history-delete-overlay"
                    >

                        <div
                            className="analysis-history-delete-modal"
                        >

                            <h3>
                                기록 삭제
                            </h3>

                            <p>
                                선택한
                                {
                                    deleteTargetIds.length
                                }
                                개의 기록을
                                정말 삭제하시겠습니까?
                            </p>

                            <p
                                className="analysis-history-delete-warning"
                            >
                                삭제된 기록은
                                복구할 수 없습니다.
                            </p>

                            <div
                                className="analysis-history-delete-actions"
                            >

                                <button
                                    onClick={() =>
                                        setDeleteModalOpen(
                                            false
                                        )
                                    }
                                >
                                    아니오
                                </button>

                                <button
                                    className="danger"
                                    onClick={
                                        confirmDelete
                                    }
                                >
                                    예, 삭제합니다
                                </button>

                            </div>

                        </div>

                    </div>
                )
            }
        </div>
    );
};

export default AnalysisHistory;