import React, { useEffect, useState } from 'react';
import { Link } from 'react-router-dom';
import { Search, Calendar, Edit, Users, FileText, CheckCircle, Trash2, Eye } from 'lucide-react';
import eventService from '../../services/eventService';
import leaderboardService from '../../services/leaderboardService';
import Card from '../../components/Card';
import Badge from '../../components/Badge';
import Button from '../../components/Button';
import { useAuth } from '../../context/AuthContext';

const ManageEvents = () => {
  const { user } = useAuth();
  const [events, setEvents] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);
  
  const [searchTerm, setSearchTerm] = useState('');

  useEffect(() => {
    fetchEvents();
  }, [user]);

  const fetchEvents = async () => {
    setLoading(true);
    try {
      // Fetch all events (including drafts) by this specific organizer
      if (user?.id) {
        const response = await eventService.getOrganizerEvents(user.id);
        setEvents(response.events || []);
      }
    } catch (err) {
      setError('Failed to load your events.');
    } finally {
      setLoading(false);
    }
  };

  const handleDelete = async (id) => {
    if (window.confirm("Are you sure you want to delete this event? This action cannot be undone.")) {
      try {
        await eventService.deleteEvent(id);
        fetchEvents();
      } catch (err) {
        alert(err.response?.data?.message || 'Failed to delete event.');
      }
    }
  };

  const handlePublish = async (id) => {
    try {
      await eventService.publishEvent(id);
      fetchEvents();
    } catch (err) {
      alert(err.response?.data?.message || 'Failed to publish event.');
    }
  };
  
  const handlePublishResults = async (id) => {
    if (window.confirm("Are you sure you want to publish the results? Participants will be able to see the leaderboard.")) {
      try {
        await leaderboardService.publishResults(id);
        alert("Results published successfully!");
        fetchEvents();
      } catch (err) {
        alert(err.response?.data?.message || 'Failed to publish results.');
      }
    }
  };

  const filteredEvents = events.filter(event => 
    event.title.toLowerCase().includes(searchTerm.toLowerCase())
  );

  return (
    <div className="space-y-6">
      <div className="flex justify-between items-center">
        <div>
          <h1 className="text-3xl font-bold text-slate-900">Manage Events</h1>
          <p className="mt-2 text-slate-500">Create, edit, and manage all your events.</p>
        </div>
        <Link to="/organizer/events/create">
          <Button>Create Event</Button>
        </Link>
      </div>

      <Card className="p-4 bg-white shadow-sm border border-slate-200">
        <div className="relative">
          <div className="absolute inset-y-0 left-0 pl-3 flex items-center pointer-events-none">
            <Search className="h-5 w-5 text-slate-400" />
          </div>
          <input
            type="text"
            className="focus:ring-primary-500 focus:border-primary-500 block w-full pl-10 sm:text-sm border-slate-300 rounded-md py-2 px-3 border"
            placeholder="Search your events by title..."
            value={searchTerm}
            onChange={(e) => setSearchTerm(e.target.value)}
          />
        </div>
      </Card>

      {error && (
        <div className="bg-red-50 text-red-700 p-4 rounded-md">
          {error}
        </div>
      )}

      {loading ? (
        <div className="p-12 text-center text-slate-500 animate-pulse">Loading events...</div>
      ) : filteredEvents.length > 0 ? (
        <div className="bg-white shadow overflow-hidden sm:rounded-md border border-slate-200">
          <ul className="divide-y divide-slate-200">
            {filteredEvents.map(event => (
              <li key={event.id}>
                <div className="px-4 py-4 sm:px-6">
                  <div className="flex items-center justify-between mb-2">
                    <div className="flex items-center gap-3">
                      <p className="text-lg font-medium text-primary-600 truncate">{event.title}</p>
                      <Badge variant={event.status === 'PUBLISHED' ? 'success' : 'default'}>{event.status}</Badge>
                      {event.resultsPublished && <Badge variant="info">Results Published</Badge>}
                    </div>
                    <div className="flex gap-2">
                      <Link to={`/events/${event.id}`}>
                        <Button variant="ghost" className="p-2" title="Preview Public Page"><Eye className="w-4 h-4"/></Button>
                      </Link>
                      <Link to={`/organizer/events/${event.id}/edit`}>
                        <Button variant="ghost" className="p-2" title="Edit Event"><Edit className="w-4 h-4"/></Button>
                      </Link>
                      <Button variant="ghost" className="p-2 text-red-600 hover:text-red-700 hover:bg-red-50" onClick={() => handleDelete(event.id)} title="Delete Event">
                        <Trash2 className="w-4 h-4"/>
                      </Button>
                    </div>
                  </div>
                  
                  <div className="mt-2 sm:flex sm:justify-between">
                    <div className="sm:flex sm:gap-6">
                      <p className="flex items-center text-sm text-slate-500">
                        <Calendar className="flex-shrink-0 mr-1.5 h-4 w-4 text-slate-400" />
                        {new Date(event.startDate).toLocaleDateString()}
                      </p>
                      <Link to={`/organizer/events/${event.id}/registrations`} className="mt-2 flex items-center text-sm text-slate-500 hover:text-primary-600 sm:mt-0">
                        <Users className="flex-shrink-0 mr-1.5 h-4 w-4 text-slate-400" />
                        Registrations
                      </Link>
                      <Link to={`/organizer/events/${event.id}/teams`} className="mt-2 flex items-center text-sm text-slate-500 hover:text-primary-600 sm:mt-0">
                        <Users className="flex-shrink-0 mr-1.5 h-4 w-4 text-slate-400" />
                        Teams
                      </Link>
                      <Link to={`/organizer/events/${event.id}/submissions`} className="mt-2 flex items-center text-sm text-slate-500 hover:text-primary-600 sm:mt-0">
                        <FileText className="flex-shrink-0 mr-1.5 h-4 w-4 text-slate-400" />
                        Submissions
                      </Link>
                    </div>
                    <div className="mt-4 flex items-center gap-2 sm:mt-0">
                      {event.status === 'DRAFT' && (
                        <Button variant="secondary" onClick={() => handlePublish(event.id)} className="text-xs py-1">
                          Publish Event
                        </Button>
                      )}
                      {event.status === 'PUBLISHED' && !event.resultsPublished && (
                        <Button onClick={() => handlePublishResults(event.id)} className="text-xs py-1 bg-yellow-500 hover:bg-yellow-600 text-white border-transparent">
                          Publish Results
                        </Button>
                      )}
                    </div>
                  </div>
                </div>
              </li>
            ))}
          </ul>
        </div>
      ) : (
        <Card className="p-12 text-center border border-slate-200">
          <Calendar className="mx-auto h-12 w-12 text-slate-300 mb-4" />
          <h3 className="text-lg font-medium text-slate-900">No events found</h3>
          <p className="mt-1 text-slate-500">You haven't created any events matching that search.</p>
        </Card>
      )}
    </div>
  );
};

export default ManageEvents;
