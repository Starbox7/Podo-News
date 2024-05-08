import { Routes, Route, HashRouter } from "react-router-dom";


import Pages from "../domains";

function Router() {
  return (
    /**
     * June. render of routing in SSR refactoring
     * + 개발용으로 바로 홈으로 가도록 임시 라우팅
     */
    <HashRouter>
      <Routes>
        <Route path="/" element={<Pages.Home />} />
        <Route path="/first" element={<Pages.FistLogin />} />
        <Route path="/home" element={<Pages.Home />} />
        <Route path="/scanner" element={<Pages.Scanner />} />
        <Route path="/prescription/edit" element={<Pages.PrescriptionEditPage />} />
        <Route path="/prescription" element={<Pages.Prescription />} />
        <Route path="/prescription/detail" element={<Pages.PrescriptionDetailPage />} />
        <Route path="/health" element={<Pages.HealthPage />} />
        <Route path="/profile" element={<Pages.MyPage />} />
        <Route path="/profile/guide" element={<Pages.GuidePage />} />
        <Route path="/profile/terms" element={<Pages.TermsPage />} />
        <Route path="/profile/privacy" element={<Pages.PrivacyPage />} />
        <Route path="/profile/edit" element={<Pages.ProfileEditPage />} />

      </Routes>
    </HashRouter>
  );
}

export default Router;