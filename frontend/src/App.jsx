import React from 'react';
import {BrowserRouter, Route, Routes, useLocation} from 'react-router-dom';

import ArtLensIndex from './pages/artLensIndex/ArtLensIndex.jsx';
import Login from './pages/login/Login.jsx';
import Signup from "./pages/signup/Signup.jsx";
import FindPw from "./pages/findPw/FindPw.jsx";
import ResetPw from './pages/reset/resetPw/ResetPassword.jsx'
import ResetSuccess from './pages/reset/resetSuc/ResetSuccess.jsx'
import ResetExpired from './pages/reset/resetExp/ResetExpired.jsx'
import Header from './components/layout/Header';
import Analysis from './pages/analysis/Analysis.jsx'
import AnalysisResult from "./pages/analysis/AnalysisResult.jsx";
import ArtistExplore from "./pages/artists/ArtistExplore.jsx";
import StyleMap from "./pages/styleMap/StyleMap.jsx";
import History from "./pages/history/AnalysisHistory.jsx"
import MyPage from "./pages/mypage/MyPage.jsx";
import ShareView from "./pages/share/ShareView.jsx";
import TasteTest from "./pages/tastetest/TasteTest.jsx";

// 🌟 주소 변화를 감지해서 애니메이션을 실행해주는 내부 컴포넌트
function AnimatedRoutes() {
    const location = useLocation();

    // 헤더를 숨기고 싶은 경로 (예: 로그인, 회원가입)
    const hideHeaderPaths = ['/', '/login', '/signup', '/find-pw', '/reset-pw', '/reset-suc', '/reset-exp'];
    const shouldShowHeader =
        !hideHeaderPaths.includes(location.pathname) &&
        !location.pathname.startsWith('/share/');
    return (
        <>
            {shouldShowHeader && <Header />}

            <Routes location={location}>
                <Route path="/" element={<ArtLensIndex />} />
                <Route path="/login" element={<Login />} />
                <Route path="/signup" element={<Signup />} />
                <Route path="/find-pw" element={<FindPw />} />
                <Route path="/reset-pw" element={<ResetPw />} />
                <Route path="/reset-suc" element={<ResetSuccess />} />
                <Route path="/reset-exp" element={<ResetExpired />} />
                <Route path="/analysis" element={<Analysis />} />
                <Route
                    path="/analysis/result/:resultId"
                    element={<AnalysisResult />}
                />
                <Route
                    path="/artist"
                    element={<ArtistExplore />}
                />
                <Route path="/style-map" element={<StyleMap/>} />
                <Route path="/taste-test" element={<TasteTest/>} />
                <Route path="/history" element={<History/>} />
                <Route path="/mypage" element={<MyPage/>} />
                <Route path="/share/:token" element={<ShareView/>} />
            </Routes>
        </>
    );
}

function App() {
    return (
        <BrowserRouter>
            <AnimatedRoutes />
        </BrowserRouter>
    );
}

export default App;