// src/pages/share/ShareView.jsx
//
// 로그인 없이 누구나 볼 수 있는 공개 공유 페이지.

import { useParams } from 'react-router-dom';
import { useEffect, useState } from 'react';

import axiosInstance from '../../api/axiosInstance.js';
import AnalysisResultView from '../analysis/AnalysisResultView.jsx';

const ShareView = () => {

    const { token } = useParams();

    const [result, setResult] = useState(null);

    const [loading, setLoading] = useState(true);

    const [notFound, setNotFound] = useState(false);

    useEffect(() => {

        const fetchShared = async () => {

            try {

                const res =
                    await axiosInstance.get(
                        `/share/v1/${token}`
                    );

                setResult(res.data);

            } catch (e) {

                console.error(e);

                setNotFound(true);

            } finally {

                setLoading(false);
            }
        };

        fetchShared();

    }, [token]);

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

                            entry.target.classList.add('show');

                        } else {

                            entry.target.classList.remove('show');
                        }
                    });
                },

                { threshold: 0.15 }
            );

        sections.forEach(section => observer.observe(section));

        return () => {

            sections.forEach(section => observer.unobserve(section));
        };

    }, [result]);

    if (loading) {

        return (

            <div
                style={{
                    color: 'white',
                    padding: '100px',
                    fontSize: '24px'
                }}
            >
                공유된 분석 결과를 불러오는 중...
            </div>
        );
    }

    if (notFound || !result) {

        return (

            <div
                style={{
                    color: 'white',
                    padding: '100px',
                    fontSize: '24px',
                    textAlign: 'center'
                }}
            >
                링크를 찾을 수 없거나 만료된 공유예요.
            </div>
        );
    }

    return (
        <AnalysisResultView result={result} headerActions={null} />
    );
};

export default ShareView;
