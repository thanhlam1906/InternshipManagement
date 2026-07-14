import { BrowserRouter as Router, Routes, Route, Navigate } from 'react-router-dom'
import { AuthProvider, useAuth } from '@/context/AuthContext'
import ProtectedRoute from '@/components/ProtectedRoute'
import AppLayout from '@/layouts/AppLayout'
import StudentLayout from '@/layouts/StudentLayout'
import LoginPage from '@/pages/auth/LoginPage'
import DashboardPage from '@/pages/dashboard/DashboardPage'
import StudentDashboardPage from '@/pages/student/StudentDashboardPage'
import StudentProfilePage from '@/pages/student/StudentProfilePage'
import StudentResultsPage from '@/pages/student/StudentResultsPage'
import StudentAssignmentsPage from '@/pages/student/StudentAssignmentsPage'
import StudentMentorsPage from '@/pages/student/StudentMentorsPage'
import StudentPhasesPage from '@/pages/student/StudentPhasesPage'
import UserListPage from '@/pages/users/UserListPage'
import StudentListPage from '@/pages/students/StudentListPage'
import MentorListPage from '@/pages/mentors/MentorListPage'
import PhaseManagementPage from '@/pages/phases/PhaseManagementPage'
import AssignmentListPage from '@/pages/assignments/AssignmentListPage'
import RoundListPage from '@/pages/assessment-rounds/RoundListPage'
import RoundCriterionListPage from '@/pages/round-criteria/RoundCriterionListPage'
import CriterionListPage from '@/pages/evaluation-criteria/CriterionListPage'
import ResultListPage from '@/pages/assessment-results/ResultListPage'
import CVReviewPage from '@/pages/cv-review/CVReviewPage'
import JobSearchPage from '@/pages/job-search/JobSearchPage'
import { Toaster } from 'react-hot-toast'

/** Picks the correct layout based on user role — no redirect, just renders */
function RoleLayout() {
  const { user } = useAuth()
  if (user?.role === 'STUDENT') return <StudentLayout />
  return <AppLayout />
}

/** Picks the correct dashboard based on user role */
function RoleDashboard() {
  const { user } = useAuth()
  if (user?.role === 'STUDENT') return <StudentDashboardPage />
  return <DashboardPage />
}

function App() {
  return (
    <AuthProvider>
      <Router>
        <Routes>
          {/* Public Route */}
          <Route path="/login" element={<LoginPage />} />

          {/* All Protected Routes — layout chosen by role */}
          <Route
            path="/"
            element={
              <ProtectedRoute>
                <RoleLayout />
              </ProtectedRoute>
            }
          >
            {/* Dashboard — role-aware */}
            <Route index element={<RoleDashboard />} />

            {/* ========== ADMIN & MENTOR pages ========== */}
            <Route element={<ProtectedRoute roles={['ADMIN', 'MENTOR']} />}>
              <Route path="students" element={<StudentListPage />} />
              <Route path="assignments" element={<AssignmentListPage />} />
              <Route path="assessment-rounds" element={<RoundListPage />} />
              <Route path="assessment-results" element={<ResultListPage />} />
            </Route>

            {/* ========== ADMIN only pages ========== */}
            <Route element={<ProtectedRoute roles={['ADMIN']} />}>
              <Route path="users" element={<UserListPage />} />
              <Route path="mentors" element={<MentorListPage />} />
              <Route path="phases" element={<PhaseManagementPage />} />
              <Route path="round-criteria" element={<RoundCriterionListPage />} />
              <Route path="evaluation-criteria" element={<CriterionListPage />} />
            </Route>

            {/* ========== STUDENT pages ========== */}
            <Route element={<ProtectedRoute roles={['STUDENT']} />}>
              <Route path="profile" element={<StudentProfilePage />} />
              <Route path="results" element={<StudentResultsPage />} />
              <Route path="assignments" element={<StudentAssignmentsPage />} />
              <Route path="mentors" element={<StudentMentorsPage />} />
              <Route path="phases" element={<StudentPhasesPage />} />
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
