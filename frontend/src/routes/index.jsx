import { createBrowserRouter } from 'react-router-dom';
import MainLayout from '../layouts/MainLayout';
import ProtectedRoute from '../components/ProtectedRoute';

// Public Pages
import Home from '../pages/Home';
import Events from '../pages/Events';
import EventDetails from '../pages/EventDetails';
import Login from '../pages/Login';
import Register from '../pages/Register';

// Participant Pages
import ParticipantDashboard from '../pages/participant/Dashboard';
import MyRegistrations from '../pages/participant/MyRegistrations';
import MyTeams from '../pages/participant/MyTeams';
import MySubmissions from '../pages/participant/MySubmissions';
import Leaderboard from '../pages/participant/Leaderboard';

// Organizer Pages
import OrganizerDashboard from '../pages/organizer/Dashboard';
import ManageEvents from '../pages/organizer/ManageEvents';
import EventForm from '../pages/organizer/EventForm';
import EventRegistrations from '../pages/organizer/EventRegistrations';
import EventTeams from '../pages/organizer/EventTeams';
import EventSubmissions from '../pages/organizer/EventSubmissions';
import EvaluateSubmission from '../pages/organizer/EvaluateSubmission';

// Admin Pages
import AdminDashboard from '../pages/admin/Dashboard';
import AdminUsers from '../pages/admin/AdminUsers';

const NotFound = () => (
  <div className="bg-white rounded-lg shadow px-5 py-12 text-center border border-slate-200">
    <h1 className="text-4xl font-bold text-slate-900 mb-2">404</h1>
    <p className="text-slate-500 mb-6">The page you are looking for does not exist.</p>
    <a href="/" className="text-primary-600 hover:underline">Return to Home</a>
  </div>
);

const router = createBrowserRouter([
  {
    path: '/',
    element: <MainLayout />,
    children: [
      { path: '/', element: <Home /> },
      { path: '/events', element: <Events /> },
      { path: '/events/:id', element: <EventDetails /> },
      { path: '/login', element: <Login /> },
      { path: '/register', element: <Register /> },
      
      // Participant Routes
      {
        element: <ProtectedRoute allowedRoles={['PARTICIPANT']} />,
        children: [
          { path: '/dashboard', element: <ParticipantDashboard /> },
          { path: '/my-registrations', element: <MyRegistrations /> },
          { path: '/my-teams', element: <MyTeams /> },
          { path: '/my-submissions', element: <MySubmissions /> },
          { path: '/leaderboard/:eventId', element: <Leaderboard /> },
        ]
      },

      // Organizer Routes
      {
        element: <ProtectedRoute allowedRoles={['ORGANIZER']} />,
        children: [
          { path: '/organizer', element: <OrganizerDashboard /> },
          { path: '/organizer/events', element: <ManageEvents /> },
          { path: '/organizer/events/create', element: <EventForm /> },
          { path: '/organizer/events/:id/edit', element: <EventForm /> },
          { path: '/organizer/events/:id/registrations', element: <EventRegistrations /> },
          { path: '/organizer/events/:id/teams', element: <EventTeams /> },
          { path: '/organizer/events/:id/submissions', element: <EventSubmissions /> },
          { path: '/organizer/events/:id/evaluate', element: <EvaluateSubmission /> },
        ]
      },

      // Admin Routes
      {
        element: <ProtectedRoute allowedRoles={['ADMIN']} />,
        children: [
          { path: '/admin', element: <AdminDashboard /> },
          { path: '/admin/users', element: <AdminUsers /> },
        ]
      },
      
      // Fallback 404
      { path: '*', element: <NotFound /> }
    ]
  }
]);

export default router;
