// src/components/share/ShareButton.jsx

import { useShare } from '../../hooks/useShare.js';
import ShareModal from './ShareModal.jsx';

const ShareButton = ({
    resultId,
    title,
    imgUrl,
    initialShare,
    skipInitialFetch,
    stopPropagation,
    disabled,
    className,
    children,
    onShareChange
}) => {

    const {
        share,
        shareModalOpen,
        copySuccess,
        openShareModal,
        closeShareModal,
        handleCopyLink,
        handleNativeShare,
        handleKakaoShare,
        handleNaverShare,
        handleFacebookShare,
        handleXShare,
        handleRevokeShare
    } = useShare({ resultId, title, imgUrl, initialShare, skipInitialFetch, onShareChange });

    const handleClick = (e) => {

        if (stopPropagation) {

            e.stopPropagation();
        }

        if (disabled) {

            return;
        }

        openShareModal();
    };

    return (
        <>
            <button
                className={className}
                onClick={handleClick}
            >
                {children}
            </button>

            {shareModalOpen && (

                <ShareModal
                    share={share}
                    onClose={closeShareModal}
                    copySuccess={copySuccess}
                    onCopyLink={handleCopyLink}
                    onKakaoShare={handleKakaoShare}
                    onNaverShare={handleNaverShare}
                    onFacebookShare={handleFacebookShare}
                    onXShare={handleXShare}
                    onNativeShare={handleNativeShare}
                    onRevoke={handleRevokeShare}
                />
            )}
        </>
    );
};

export default ShareButton;
