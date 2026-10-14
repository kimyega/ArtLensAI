// src/pages/analysis/AnalysisResult.jsx

import {
    Compass,
    Eye,
    Share2
} from 'lucide-react';

import {
    useNavigate,
    useParams
} from 'react-router-dom';

import {
    useEffect,
    useState
} from 'react';

import axiosInstance
    from '../../api/axiosInstance.js';

import AnalysisResultView from './AnalysisResultView.jsx';
import ShareButton from '../../components/share/ShareButton.jsx';

const API_URL = import.meta.env.VITE_API_URL;

const AnalysisResultPage = () => {

    const navigate = useNavigate();

    const { resultId } = useParams();

    const [result, setResult] = useState(null);

    const [loading, setLoading] = useState(true);

    const [shareInfo, setShareInfo] = useState(null);

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

        <AnalysisResultView
            result={result}
            headerActions={
                <>
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

                    <ShareButton
                        resultId={resultId}
                        title={result.title}
                        imgUrl={`${API_URL}/analysis/v1/api/image/${result.imgKey}`}
                        className="analysis-btn analysis-btn-outline"
                        onShareChange={setShareInfo}
                    >
                        <Share2 size={16} />
                        공유하기
                    </ShareButton>

                    <span className="analysis-view-count">
                        <Eye size={16} />
                        {shareInfo?.viewCount ?? 0}
                    </span>
                </>
            }
        />
    );
};

export default AnalysisResultPage;
