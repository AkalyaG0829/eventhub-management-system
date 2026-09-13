import React, { useEffect, useState } from 'react';
import { useParams, Link } from 'react-router-dom';
import { ArrowLeft, Users, Calendar } from 'lucide-react';
import teamService from '../../services/teamService';
import eventService from '../../services/eventService';
import Card from '../../components/Card';
import Badge from '../../components/Badge';

const EventTeams = () => {
  const { id } = useParams();
  const [teams, setTeams] = useState([]);
  const [event, setEvent] = useState(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);

  useEffect(() => {
    const fetchData = async () => {
      try {
        const [eventData, teamData] = await Promise.all([
          eventService.getEventById(id),
          teamService.getTeamsByEvent(id)
        ]);
        setEvent(eventData);
        setTeams(teamData);
      } catch (err) {
        setError('Failed to load teams.');
      } finally {
        setLoading(false);
      }
    };
    fetchData();
  }, [id]);

  return (
    <div className="space-y-6 max-w-5xl mx-auto">
      <div>
        <Link to="/organizer/events" className="text-sm text-primary-600 hover:underline flex items-center gap-1 mb-4">
          <ArrowLeft className="w-4 h-4" /> Back to Events
        </Link>
        <h1 className="text-3xl font-bold text-slate-900">Event Teams</h1>
        <p className="mt-2 text-slate-500">{event ? `Manage teams for ${event.title}` : 'Loading...'}</p>
      </div>

      {error ? (
        <Card className="p-12 text-center text-red-600 border border-red-200 bg-red-50">
          <p>{error}</p>
        </Card>
      ) : loading ? (
        <div className="p-12 text-center text-slate-500 animate-pulse">Loading teams...</div>
      ) : teams.length > 0 ? (
        <div className="grid grid-cols-1 md:grid-cols-2 gap-6">
          {teams.map(team => (
            <Card key={team.id} className="p-6 border border-slate-200">
              <div className="flex justify-between items-start mb-4">
                <div>
                  <h3 className="text-xl font-bold text-primary-700 flex items-center gap-2">
                    {team.name}
                  </h3>
                  <p className="text-sm text-slate-500 mt-1">Team ID: {team.id}</p>
                </div>
                <Badge variant="info">Size: {team.members?.length || 0}</Badge>
              </div>
              
              <div className="bg-slate-50 rounded-lg p-4 border border-slate-200">
                <h5 className="text-sm font-semibold text-slate-700 mb-3">Members</h5>
                <ul className="divide-y divide-slate-200">
                  <li className="py-2 flex justify-between items-center">
                    <span className="text-sm font-medium text-slate-900">{team.leader.name} <span className="text-xs text-primary-600 ml-2">(Leader)</span></span>
                  </li>
                  {team.members?.filter(m => m.id !== team.leader.id).map(member => (
                    <li key={member.id} className="py-2 flex justify-between items-center">
                      <span className="text-sm font-medium text-slate-900">{member.name}</span>
                    </li>
                  ))}
                </ul>
              </div>
            </Card>
          ))}
        </div>
      ) : (
        <Card className="p-12 text-center border border-slate-200">
          <Users className="mx-auto h-12 w-12 text-slate-300 mb-4" />
          <h3 className="text-lg font-medium text-slate-900">No teams formed yet</h3>
          <p className="mt-1 text-slate-500">Participants haven't created any teams for this event.</p>
        </Card>
      )}
    </div>
  );
};

export default EventTeams;
