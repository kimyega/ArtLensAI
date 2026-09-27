// src/components/share/ShareModal.jsx

import './ShareModal.css';

import { createPortal } from 'react-dom';
import { Copy, Share2, X } from 'lucide-react';

import kakaoShareIcon from '../../assets/kakao-share-icon.png';
import naverShareIcon from '../../assets/naver-share-icon.png';
import facebookShareIcon from '../../assets/facebook-share-icon.png';

const ShareModal = ({
    share,
    onClose,
    copySuccess,
    onCopyLink,
    onKakaoShare,
    onNaverShare,
    onFacebookShare,
    onXShare,
    onNativeShare,
    onRevoke
}) => {

    if (!share) return null;

    // 카드 그리드처럼 :hover 시 transform이 걸리는 조상 안에서 열리면
    // position:fixed 오버레이가 뷰포트가 아니라 그 조상 기준으로 잘려버리므로
    // body로 포탈을 띄워서 항상 화면 전체 기준으로 뜨게 한다
    return createPortal(

        <div
            className="share-modal-overlay"
            onClick={onClose}
        >

            <div
                className="share-modal"
                onClick={(e) => e.stopPropagation()}
            >

                <div className="share-modal-header">

                    <h3>공유하기</h3>

                    <button
                        className="share-modal-close"
                        onClick={onClose}
                    >
                        <X size={18} />
                    </button>

                </div>

                <p className="share-modal-desc">
                    링크를 가진 사람은 로그인 없이도 이 분석 결과를 볼 수 있어요.
                </p>

                <div className="share-modal-url-row">

                    <input
                        type="text"
                        readOnly
                        value={share.shareUrl}
                        onFocus={(e) => e.target.select()}
                    />

                    <button
                        className="share-modal-copy-btn"
                        onClick={onCopyLink}
                        title="링크 복사"
                    >
                        <Copy size={16} />
                    </button>

                </div>

                {copySuccess && (

                    <p className="share-modal-copied">
                        링크가 복사됐어요!
                    </p>
                )}

                <div className="share-modal-icon-row">

                    <button
                        className="share-icon-btn"
                        onClick={onKakaoShare}
                    >
                        <span className="share-icon-tile">
                            <img className="share-icon-img-contain" src={kakaoShareIcon} alt="카카오톡 공유" />
                        </span>
                        <span className="share-icon-label">카카오톡</span>
                    </button>

                    <button
                        className="share-icon-btn"
                        onClick={onNaverShare}
                    >
                        <span className="share-icon-tile">
                            <img className="share-icon-img-cover" src={naverShareIcon} alt="네이버 공유" />
                        </span>
                        <span className="share-icon-label">네이버</span>
                    </button>

                    <button
                        className="share-icon-btn"
                        onClick={onFacebookShare}
                    >
                        <span className="share-icon-tile share-icon-tile-facebook">
                            <img className="share-icon-img-contain" src={facebookShareIcon} alt="페이스북 공유" />
                        </span>
                        <span className="share-icon-label">페이스북</span>
                    </button>

                    <button
                        className="share-icon-btn"
                        onClick={onXShare}
                    >
                        <span className="share-icon-tile share-icon-tile-x">
                            <svg
                                viewBox="0 0 24 24"
                                width="32"
                                height="32"
                                fill="currentColor"
                                aria-hidden="true"
                            >
                                <path d="M18.901 1.153h3.68l-8.04 9.19L24 22.846h-7.406l-5.8-7.584-6.638 7.584H.474l8.6-9.83L0 1.154h7.594l5.243 6.932ZM17.61 20.644h2.039L6.486 3.24H4.298Z" />
                            </svg>
                        </span>
                        <span className="share-icon-label">X</span>
                    </button>

                    {typeof navigator !== 'undefined' && navigator.share && (

                        <button
                            className="share-icon-btn"
                            onClick={onNativeShare}
                        >
                            <span className="share-icon-tile share-icon-tile-native">
                                <Share2 size={30} />
                            </span>
                            <span className="share-icon-label">기기로 공유</span>
                        </button>
                    )}

                </div>

                <button
                    className="share-modal-revoke-link"
                    onClick={onRevoke}
                >
                    공유 취소
                </button>

            </div>

        </div>,

        document.body
    );
};

export default ShareModal;
