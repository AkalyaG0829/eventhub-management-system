import React, { useEffect, useState } from 'react';
import { Link } from 'react-router-dom';
import { Calendar, Users, FileText, Activity } from 'lucide-react';
import { useAuth } from '../../context/AuthContext';
import registrationService from '../../services/registrationService';
import Card from '../../components/Card';
import Badge from '../../components/Badge';

const Dashboard = () => {
  const { user } = useAuth();
  const [registrations, setRegistrations] = useState([]);
  const [teamsCount, setTeamsCount] = useState('--');
  const [submissionsCount, setSubmissionsCount] = useState('--');
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    const fetchDashboardData = async () => {
      try {
        const regs = await registrationService.getMyRegistrations();
        setRegistrations(regs);
        
        try {
          const { default: teamService } = await import('../../services/teamService');
          const myTeams = await teamService.getMyTeams();
          setTeamsCount(myTeams.length);
        } catch (e) { console.error(e); }

        try {
          const { default: submissionService } = await import('../../services/submissionService');
          const mySubmissions = await submissionService.getMySubmissions();
          setSubmissionsCount(mySubmissions.length);
        } catch (e) { console.error(e); }

      } catch (error) {
        console.error("Failed to fetch dashboard data", error);
      } finally {
        setLoading(false);
      }
    };
    fetchDashboardData();
  }, []);

  const activeRegistrations = registrations.filter(r => r.status === 'APPROVED' || r.status === 'PENDING');

  return (
    <div className="space-y-6">
      <div>
        <h1 className="text-3xl font-bold text-slate-900">Welcome back, {user?.name}!</h1>
        <p className="mt-2 text-slate-500">Here's an overview of your events and activities.</p>
      </div>

      <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-6">
        <Card className="p-6 flex items-center gap-4 border border-slate-200">
          <div className="p-3 bg-blue-100 text-blue-600 rounded-lg">
            <Calendar className="w-6 h-6" />
          </div>
          <div>
            <p className="text-sm font-medium text-slate-500">Total Registrations</p>
            <p className="text-2xl font-bold text-slate-900">{registrations.length}</p>
          </div>
        </Card>
        
        <Card className="p-6 flex items-center gap-4 border border-slate-200">
          <div className="p-3 bg-green-100 text-green-600 rounded-lg">
            <Activity className="w-6 h-6" />
          </div>
          <div>
            <p className="text-sm font-medium text-slate-500">Active Events</p>
            <p className="text-2xl font-bold text-slate-900">{activeRegistrations.length}</p>
          </div>
        </Card>
        
        <Card className="p-6 flex items-center gap-4 border border-slate-200 opacity-70">
          <div className="p-3 bg-purple-100 text-purple-600 rounded-lg">
            <Users className="w-6 h-6" />
          </div>
          <div>
            <p className="text-sm font-medium text-slate-500">My Teams</p>
            <p className="text-2xl font-bold text-slate-900">{teamsCount}</p>
            <p className="text-xs text-slate-400">View in Teams tab</p>
          </div>
        </Card>

        <Card className="p-6 flex items-center gap-4 border border-slate-200 opacity-70">
          <div className="p-3 bg-yellow-100 text-yellow-600 rounded-lg">
            <FileText className="w-6 h-6" />
          </div>
          <div>
            <p className="text-sm font-medium text-slate-500">Submissions</p>
            <p className="text-2xl font-bold text-slate-900">{submissionsCount}</p>
            <p className="text-xs text-slate-400">View in Submissions</p>
          </div>
        </Card>
      </div>

      <div className="mt-8">
        <h2 className="text-xl font-bold text-slate-900 mb-4">Recent Registrations</h2>
        <Card className="border border-slate-200 overflow-hidden">
          {loading ? (
            <div className="p-8 text-center text-slate-500 animate-pulse">Loading activity...</div>
          ) : registrations.length > 0 ? (
            <div className="divide-y divide-slate-200">
              {registrations.slice(0, 5).map(reg => (
                <div key={reg.id} className="p-4 sm:px-6 hover:bg-slate-50 transition flex flex-col sm:flex-row justify-between items-start sm:items-center gap-4">
                  <div>
                    <h3 className="text-sm font-medium text-primary-600">{reg.eventTitle}</h3>
                    <p className="text-sm text-slate-500 mt-1">Registered on {reg.registeredAt ? new Date(reg.registeredAt).toLocaleDateString() : 'Date unavailable'}</p>
                  </div>
                    <div className="flex flex-col gap-1 items-end">
                      <div className="flex gap-2">
                        <Badge variant={reg.status === 'APPROVED' ? 'success' : (reg.status === 'REJECTED' ? 'danger' : 'warning')}>
                          {reg.status}
                        </Badge>
                        <Badge variant={
                          reg.paymentStatus === 'PAID' ? 'success' : 
                          reg.paymentStatus === 'FAILED' ? 'danger' : 
                          reg.paymentStatus === 'PENDING' ? 'warning' : 'default'
                        }>
                          {reg.paymentStatus || 'NOT_REQUIRED'}
                        </Badge>
                      </div>
                      <Link to={`/events/${reg.eventId}`} className="text-sm text-slate-500 hover:text-primary-600">
                        View Event
                      </Link>
                    </div>
                </div>
              ))}
            </div>
          ) : (
            <div className="p-8 text-center">
              <p className="text-slate-500 mb-4">You haven't registered for any events yet.</p>
              <Link to="/events" className="text-primary-600 font-medium hover:underline">
                Browse Events →
              </Link>
            </div>
          )}
        </Card>
      </div>
    </div>
  );
};

export default Dashboard;
