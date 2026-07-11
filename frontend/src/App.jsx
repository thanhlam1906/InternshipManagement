import { BrowserRouter as Router, Routes, Route, Navigate } from 'react-router-dom'
import { AuthProvider } from '@/context/AuthContext'
import ProtectedRoute from '@/components/ProtectedRoute'
import AppLayout from '@/layouts/AppLayout'
import LoginPage from '@/pages/auth/LoginPage'
import DashboardPage from '@/pages/dashboard/DashboardPage'
import UserListPage from '@/pages/users/UserListPage'
import StudentListPage from '@/pages/students/StudentListPage'
import MentorListPage from '@/pages/mentors/MentorListPage'
import PhaseListPage from '@/pages/phases/PhaseListPage'
import AssignmentListPage from '@/pages/assignments/AssignmentListPage'
import RoundListPage from '@/pages/assessment-rounds/RoundListPage'
import RoundCriterionListPage from '@/pages/round-criteria/RoundCriterionListPage'
import CriterionListPage from '@/pages/evaluation-criteria/CriterionListPage'
import ResultListPage from '@/pages/assessment-results/ResultListPage'
import CVReviewPage from '@/pages/cv-review/CVReviewPage'
import JobSearchPage from '@/pages/job-search/JobSearchPage'
import { Toaster } from 'react-hot-toast'

function App() {
  return (
    <AuthProvider>
      <Router>
        <Routes>
          {/* Public Route */}
          <Route path="/login" element={<LoginPage />} />

          {/* Protected Routes */}
          <Route
            path="/"
            element={
              <ProtectedRoute>
                <AppLayout />
              </ProtectedRoute>
            }
          >
            {/* Pages accessible to all authenticated users */}
            <Route element={<ProtectedRoute roles={['ADMIN', 'MENTOR', 'STUDENT']} />}>
              <Route index element={<DashboardPage />} />
            </Route>

            {/* Admin + Mentor routes */}
            <Route element={<ProtectedRoute roles={['ADMIN', 'MENTOR']} />}>
              <Route path="students" element={<StudentListPage />} />
              <Route path="assignments" element={<AssignmentListPage />} />
              <Route path="assessment-rounds" element={<RoundListPage />} />
              <Route path="assessment-results" element={<ResultListPage />} />
            </Route>

            {/* Admin only routes */}
            <Route element={<ProtectedRoute roles={['ADMIN']} />}>
              <Route path="users" element={<UserListPage />} />
              <Route path="mentors" element={<MentorListPage />} />
              <Route path="phases" element={<PhaseListPage />} />
              <Route path="round-criteria" element={<RoundCriterionListPage />} />
              <Route path="evaluation-criteria" element={<CriterionListPage />} />
            </Route>

            {/* Student only routes */}
            <Route element={<ProtectedRoute roles={['STUDENT']} />}>
              <Route path="cv-review" element={<CVReviewPage />} />
              <Route path="job-search" element={<JobSearchPage />} />
            </Route>
          </Route>

          {/* Fallback */}
          <Route path="*" element={<Navigate to="/" replace />} />
        </Routes>
      </Router>
      <Toaster position="top-right" />
    </AuthProvider>
  )
}

export default App

