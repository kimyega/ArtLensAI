// ArtistLikeButton.jsx

import { useState } from 'react';
import { Heart } from 'lucide-react';

const ArtistLikeButton = ({ artistId, liked, onToggle }) => {

    const [popping, setPopping] = useState(false);

    const handleClick = (e) => {

        // 카드 전체 클릭(상세 모달 오픈)으로 이벤트가 전파되지 않게 막음
        e.stopPropagation();

        setPopping(true);
        onToggle(artistId, !liked);
    };

    return (
        <button
            type="button"
            className={`artist-explore-like-btn ${liked ? 'liked' : ''} ${popping ? 'pop' : ''}`}
            onClick={handleClick}
            onAnimationEnd={() => setPopping(false)}
            aria-label={liked ? '찜 해제' : '찜하기'}
        >
            <Heart size={16} fill={liked ? 'currentColor' : 'none'} />
        </button>
    );
};

export default ArtistLikeButton;
