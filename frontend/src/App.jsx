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
            {/* Dashboard available to all logged in users */}
            <Route index element={<DashboardPage />} />

            {/* Admin only routes */}
            <Route
              path="users"
              element={
                <ProtectedRoute roles={['ADMIN']}>
                  <UserListPage />
                </ProtectedRoute>
              }
            />

            {/* Shared routes with role-based component internal restrictions */}
            <Route path="students" element={<StudentListPage />} />
            <Route path="mentors" element={<MentorListPage />} />
            <Route path="phases" element={<PhaseListPage />} />
            <Route path="assignments" element={<AssignmentListPage />} />
            <Route path="assessment-rounds" element={<RoundListPage />} />
            <Route path="round-criteria" element={<RoundCriterionListPage />} />
            <Route path="evaluation-criteria" element={<CriterionListPage />} />
            <Route path="assessment-results" element={<ResultListPage />} />

            {/* Student only features */}
            <Route
              path="cv-review"
              element={
                <ProtectedRoute roles={['STUDENT']}>
                  <CVReviewPage />
                </ProtectedRoute>
              }
            />
            <Route
              path="job-search"
              element={
                <ProtectedRoute roles={['STUDENT']}>
                  <JobSearchPage />
                </ProtectedRoute>
              }
            />
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

