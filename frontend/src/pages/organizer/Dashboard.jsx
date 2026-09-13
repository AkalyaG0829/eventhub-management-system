import React, { useEffect, useState } from 'react';
import { Link } from 'react-router-dom';
import { Calendar, Users, FileText, CheckCircle } from 'lucide-react';
import { useAuth } from '../../context/AuthContext';
import eventService from '../../services/eventService';
import Card from '../../components/Card';

const OrganizerDashboard = () => {
  const { user } = useAuth();
  const [events, setEvents] = useState([]);
  const [registrationsCount, setRegistrationsCount] = useState('--');
  const [teamsCount, setTeamsCount] = useState('--');
  const [submissionsCount, setSubmissionsCount] = useState('--');
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    const fetchDashboardData = async () => {
      try {
        const allEvents = await eventService.getOrganizerEvents(user?.id);
        const myEvents = allEvents.events || allEvents;
        setEvents(myEvents);
        
        // Fetch registrations and submissions counts
        let totalRegs = 0;
        let totalTeams = 0;
        let totalSubs = 0;
        
        try {
          const { default: registrationService } = await import('../../services/registrationService');
          const { default: teamService } = await import('../../services/teamService');
          const { default: submissionService } = await import('../../services/submissionService');
          
          for (const ev of myEvents) {
            try {
              const regs = await registrationService.getEventRegistrations(ev.id);
              totalRegs += regs.length;
              
              const teams = await teamService.getTeamsByEvent(ev.id);
              totalTeams += teams.length;
              for (const team of teams) {
                try {
                  const subs = await submissionService.getTeamSubmissions(team.id);
                  totalSubs += subs.length;
                } catch (e) {}
              }
            } catch (e) {}
          }
        } catch (e) {
          console.error(e);
        }
        
        setRegistrationsCount(totalRegs);
        setTeamsCount(totalTeams);
        setSubmissionsCount(totalSubs);
      } catch (error) {
        console.error("Failed to fetch dashboard data", error);
      } finally {
        setLoading(false);
      }
    };
    fetchDashboardData();
  }, [user]);

  const publishedEvents = events.filter(e => e.status === 'PUBLISHED');
  const draftEvents = events.filter(e => e.status === 'DRAFT');

  return (
    <div className="space-y-6">
      <div className="flex justify-between items-center">
        <div>
          <h1 className="text-3xl font-bold text-slate-900">Organizer Dashboard</h1>
          <p className="mt-2 text-slate-500">Welcome back, {user?.name}. Here is an overview of your events.</p>
        </div>
        <Link to="/organizer/events/create" className="inline-flex items-center px-4 py-2 border border-transparent shadow-sm text-sm font-medium rounded-md text-white bg-primary-600 hover:bg-primary-700">
          Create New Event
        </Link>
      </div>

      <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-6">
        <Card className="p-6 flex items-center gap-4 border border-slate-200">
          <div className="p-3 bg-blue-100 text-blue-600 rounded-lg">
            <Calendar className="w-6 h-6" />
          </div>
          <div>
            <p className="text-sm font-medium text-slate-500">Total Events</p>
            <p className="text-2xl font-bold text-slate-900">{events.length}</p>
          </div>
        </Card>
        
        <Card className="p-6 flex items-center gap-4 border border-slate-200">
          <div className="p-3 bg-purple-100 text-purple-600 rounded-lg">
            <Users className="w-6 h-6" />
          </div>
          <div>
            <p className="text-sm font-medium text-slate-500">Total Registrations</p>
            <p className="text-2xl font-bold text-slate-900">{registrationsCount}</p>
            <p className="text-xs text-slate-400">Across all events</p>
          </div>
        </Card>
        
        <Card className="p-6 flex items-center gap-4 border border-slate-200">
          <div className="p-3 bg-green-100 text-green-600 rounded-lg">
            <Users className="w-6 h-6" />
          </div>
          <div>
            <p className="text-sm font-medium text-slate-500">Total Teams</p>
            <p className="text-2xl font-bold text-slate-900">{teamsCount}</p>
            <p className="text-xs text-slate-400">Across all events</p>
          </div>
        </Card>

        <Card className="p-6 flex items-center gap-4 border border-slate-200">
          <div className="p-3 bg-yellow-100 text-yellow-600 rounded-lg">
            <FileText className="w-6 h-6" />
          </div>
          <div>
            <p className="text-sm font-medium text-slate-500">Total Submissions</p>
            <p className="text-2xl font-bold text-slate-900">{submissionsCount}</p>
            <p className="text-xs text-slate-400">Across all events</p>
          </div>
        </Card>
      </div>

      <div className="mt-8">
        <div className="flex justify-between items-center mb-4">
          <h2 className="text-xl font-bold text-slate-900">Your Recent Events</h2>
          <Link to="/organizer/events" className="text-primary-600 hover:underline text-sm font-medium">View all events</Link>
        </div>
        
        <Card className="border border-slate-200 overflow-hidden">
          {loading ? (
            <div className="p-8 text-center text-slate-500 animate-pulse">Loading events...</div>
          ) : events.length > 0 ? (
            <div className="divide-y divide-slate-200">
              {events.slice(0, 5).map(event => (
                <div key={event.id} className="p-4 sm:px-6 hover:bg-slate-50 transition flex flex-col sm:flex-row justify-between items-start sm:items-center gap-4">
                  <div>
                    <h3 className="text-sm font-medium text-primary-600">{event.title}</h3>
                    <p className="text-sm text-slate-500 mt-1">{new Date(event.startDate).toLocaleDateString()} - {new Date(event.endDate).toLocaleDateString()}</p>
                  </div>
                  <div className="flex flex-wrap items-center gap-3">
                    <span className={`inline-flex items-center px-2.5 py-0.5 rounded-full text-xs font-medium ${
                      event.status === 'PUBLISHED' ? 'bg-green-100 text-green-800' : 'bg-slate-100 text-slate-800'
                    }`}>
                      {event.status}
                    </span>
                    <Link to={`/organizer/events/${event.id}/registrations`} className="text-sm text-slate-500 hover:text-primary-600">
                      Manage
                    </Link>
                  </div>
                </div>
              ))}
            </div>
          ) : (
            <div className="p-12 text-center">
              <Calendar className="mx-auto h-12 w-12 text-slate-300 mb-4" />
              <p className="text-slate-500 mb-4">You haven't created any events yet.</p>
              <Link to="/organizer/events/create" className="text-primary-600 font-medium hover:underline">
                Create your first event
              </Link>
            </div>
          )}
        </Card>
      </div>
    </div>
  );
};

export default OrganizerDashboard;
