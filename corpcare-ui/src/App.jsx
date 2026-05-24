import { Routes, Route, Navigate } from 'react-router-dom'
import Landing from './pages/Landing'
import AdminNav from './components/AdminNavbar'
import ClientNav from './components/ClientNavbar'
import HospitalNav from './components/HospitalNavbar'
import EmployeeNav from './components/EmployeeNavbar'
import ToastContainer from './components/Toast'
import ChatBot from './components/ChatBot'
import AdminLogin from './pages/admin/Login'
import AdminDashboard from './pages/admin/Dashboard'
import AdminClients from './pages/admin/Clients'
import AdminHospitals from './pages/admin/Hospitals'
import ClientDashboard from './pages/client/Dashboard'
import ClientEmployees from './pages/client/Employees'
import ClientVitals from './pages/client/EmployeeVitals'
import ClientBook from './pages/client/BookAppointment'
import ClientAppointments from './pages/client/Appointments'
import HospitalDashboard from './pages/hospital/Dashboard'
import HospitalSlots from './pages/hospital/Slots'
import HospitalAppointments from './pages/hospital/Appointments'
import EmployeeLogin from './pages/employee/Login'
import EmployeeDashboard from './pages/employee/Dashboard'
import EmployeeVitals from './pages/employee/Vitals'
import EmployeeBook from './pages/employee/Book'
import EmployeeAppointments from './pages/employee/Appointments'

function ProtectedRoute({ children }) {
  const authed = sessionStorage.getItem('admin_auth')
  if (!authed) return <Navigate to="/admin/login" replace />
  return children
}

function Layout({ nav: Nav, children }) {
  return (<>{Nav && <Nav />}<div className="container">{children}</div></>)
}

export default function App() {
  return (
    <>
      <ToastContainer />
      <ChatBot />
      <Routes>
      <Route path="/" element={<Landing />} />

      {/* Admin Login */}
      <Route path="/admin/login" element={<AdminLogin />} />

      {/* Admin (protected) */}
      <Route path="/admin" element={<ProtectedRoute><Layout nav={AdminNav}><AdminDashboard /></Layout></ProtectedRoute>} />
      <Route path="/admin/clients" element={<ProtectedRoute><Layout nav={AdminNav}><AdminClients /></Layout></ProtectedRoute>} />
      <Route path="/admin/hospitals" element={<ProtectedRoute><Layout nav={AdminNav}><AdminHospitals /></Layout></ProtectedRoute>} />

      {/* Client */}
      <Route path="/client" element={<Layout nav={ClientNav}><ClientDashboard /></Layout>} />
      <Route path="/client/employees" element={<Layout nav={ClientNav}><ClientEmployees /></Layout>} />
      <Route path="/client/employees/:employeeId/vitals" element={<Layout nav={ClientNav}><ClientVitals /></Layout>} />
      <Route path="/client/book" element={<Layout nav={ClientNav}><ClientBook /></Layout>} />
      <Route path="/client/appointments" element={<Layout nav={ClientNav}><ClientAppointments /></Layout>} />

      {/* Hospital */}
      <Route path="/hospital" element={<Layout nav={HospitalNav}><HospitalDashboard /></Layout>} />
      <Route path="/hospital/slots" element={<Layout nav={HospitalNav}><HospitalSlots /></Layout>} />
      <Route path="/hospital/appointments" element={<Layout nav={HospitalNav}><HospitalAppointments /></Layout>} />

      {/* Employee */}
      <Route path="/employee/login" element={<EmployeeLogin />} />
      <Route path="/employee/dashboard" element={<Layout nav={EmployeeNav}><EmployeeDashboard /></Layout>} />
      <Route path="/employee/vitals" element={<Layout nav={EmployeeNav}><EmployeeVitals /></Layout>} />
      <Route path="/employee/book" element={<Layout nav={EmployeeNav}><EmployeeBook /></Layout>} />
      <Route path="/employee/appointments" element={<Layout nav={EmployeeNav}><EmployeeAppointments /></Layout>} />
      </Routes>
    </>  )
}
