import { Link } from 'react-router-dom';
import './LoginRequiredModal.css';

const LoginRequiredModal = ({
    isOpen,
    onClose,
    message = '이 기능을 이용하려면 로그인해 주세요.'
}) => {

    const handleOverlayClick = (e) => {
        if (e.target === e.currentTarget) onClose();
    };

    return (
        <div className={`modal-overlay ${isOpen ? 'active' : ''}`} onClick={handleOverlayClick}>
            <div className="modal-content">
                <button className="btn-close" onClick={onClose}>×</button>
                <span className="modal-icon">🔐</span>
                <h2 className="modal-title">로그인이 필요합니다</h2>
                <p className="modal-sub">{message}</p>
                <Link to="/login" className="btn-modal-login">
                    <span>🎨</span> ArtLens ai 로그인하기
                </Link>
                <div className="modal-footer">
                    아직 계정이 없으신가요? <Link to="/signup">회원가입</Link>
                </div>
            </div>
        </div>
    );
};

export default LoginRequiredModal;
