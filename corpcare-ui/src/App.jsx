import { Routes, Route, Navigate } from 'react-router-dom'
import Landing from './pages/Landing'
import AdminNav from './components/AdminNavbar'
import ClientNav from './components/ClientNavbar'
import HospitalNav from './components/HospitalNavbar'
import EmployeeNav from './components/EmployeeNavbar'
import ToastContainer from './components/Toast'
import ChatBot from './components/ChatBot'
import ErrorBoundary from './components/ErrorBoundary'
import ThemeToggle from './components/ThemeToggle'
import AdminLogin from './pages/admin/Login'
import AdminDashboard from './pages/admin/Dashboard'
import AdminClients from './pages/admin/Clients'
import AdminHospitals from './pages/admin/Hospitals'
import ClientLogin from './pages/client/Login'
import ClientDashboard from './pages/client/Dashboard'
import ClientEmployees from './pages/client/Employees'
import ClientVitals from './pages/client/EmployeeVitals'
import ClientBook from './pages/client/BookAppointment'
import ClientAppointments from './pages/client/Appointments'
import HospitalLogin from './pages/hospital/Login'
import HospitalDashboard from './pages/hospital/Dashboard'
import HospitalSlots from './pages/hospital/Slots'
import HospitalAppointments from './pages/hospital/Appointments'
import EmployeeLogin from './pages/employee/Login'
import EmployeeDashboard from './pages/employee/Dashboard'
import EmployeeVitals from './pages/employee/Vitals'
import EmployeeBook from './pages/employee/Book'
import EmployeeAppointments from './pages/employee/Appointments'
import HealthReport from './pages/employee/HealthReport'
import ReportAnalyzer from './pages/employee/ReportAnalyzer'

function ProtectedRoute({ children, portal }) {
  const token = sessionStorage.getItem('token')
  if (!token) return <Navigate to={`/${portal}/login`} replace />
  return children
}

function Layout({ nav: Nav, children }) {
  return (<>{Nav && <Nav />}<div className="container">{children}</div></>)
}

export default function App() {
  return (
    <>
      <ToastContainer />
      <ThemeToggle />
      <ChatBot />
      <ErrorBoundary>
      <Routes>
      <Route path="/" element={<Landing />} />

      {/* Admin Login */}
      <Route path="/admin/login" element={<AdminLogin />} />

      {/* Admin (protected) */}
      <Route path="/admin" element={<ProtectedRoute portal="admin"><Layout nav={AdminNav}><AdminDashboard /></Layout></ProtectedRoute>} />
      <Route path="/admin/clients" element={<ProtectedRoute portal="admin"><Layout nav={AdminNav}><AdminClients /></Layout></ProtectedRoute>} />
      <Route path="/admin/hospitals" element={<ProtectedRoute portal="admin"><Layout nav={AdminNav}><AdminHospitals /></Layout></ProtectedRoute>} />

      {/* Client Login */}
      <Route path="/client/login" element={<ClientLogin />} />

      {/* Client (protected) */}
      <Route path="/client" element={<ProtectedRoute portal="client"><Layout nav={ClientNav}><ClientDashboard /></Layout></ProtectedRoute>} />
      <Route path="/client/employees" element={<ProtectedRoute portal="client"><Layout nav={ClientNav}><ClientEmployees /></Layout></ProtectedRoute>} />
      <Route path="/client/employees/:employeeId/vitals" element={<ProtectedRoute portal="client"><Layout nav={ClientNav}><ClientVitals /></Layout></ProtectedRoute>} />
      <Route path="/client/book" element={<ProtectedRoute portal="client"><Layout nav={ClientNav}><ClientBook /></Layout></ProtectedRoute>} />
      <Route path="/client/appointments" element={<ProtectedRoute portal="client"><Layout nav={ClientNav}><ClientAppointments /></Layout></ProtectedRoute>} />

      {/* Hospital Login */}
      <Route path="/hospital/login" element={<HospitalLogin />} />

      {/* Hospital (protected) */}
      <Route path="/hospital" element={<ProtectedRoute portal="hospital"><Layout nav={HospitalNav}><HospitalDashboard /></Layout></ProtectedRoute>} />
      <Route path="/hospital/slots" element={<ProtectedRoute portal="hospital"><Layout nav={HospitalNav}><HospitalSlots /></Layout></ProtectedRoute>} />
      <Route path="/hospital/appointments" element={<ProtectedRoute portal="hospital"><Layout nav={HospitalNav}><HospitalAppointments /></Layout></ProtectedRoute>} />

      {/* Employee Login */}
      <Route path="/employee/login" element={<EmployeeLogin />} />

      {/* Employee (protected) */}
      <Route path="/employee/dashboard" element={<ProtectedRoute portal="employee"><Layout nav={EmployeeNav}><EmployeeDashboard /></Layout></ProtectedRoute>} />
      <Route path="/employee/vitals" element={<ProtectedRoute portal="employee"><Layout nav={EmployeeNav}><EmployeeVitals /></Layout></ProtectedRoute>} />
      <Route path="/employee/book" element={<ProtectedRoute portal="employee"><Layout nav={EmployeeNav}><EmployeeBook /></Layout></ProtectedRoute>} />
      <Route path="/employee/appointments" element={<ProtectedRoute portal="employee"><Layout nav={EmployeeNav}><EmployeeAppointments /></Layout></ProtectedRoute>} />
      <Route path="/employee/health-report" element={<ProtectedRoute portal="employee"><Layout nav={EmployeeNav}><HealthReport /></Layout></ProtectedRoute>} />
      <Route path="/employee/report-analyzer" element={<ProtectedRoute portal="employee"><Layout nav={EmployeeNav}><ReportAnalyzer /></Layout></ProtectedRoute>} />
      </Routes>
      </ErrorBoundary>
    </>  )
}
