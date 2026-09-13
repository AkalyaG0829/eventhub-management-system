import React, { useEffect, useState } from 'react';
import { useParams, Link } from 'react-router-dom';
import { ArrowLeft, Users, Calendar } from 'lucide-react';
import registrationService from '../../services/registrationService';
import eventService from '../../services/eventService';
import Card from '../../components/Card';
import Badge from '../../components/Badge';

const EventRegistrations = () => {
  const { id } = useParams();
  const [registrations, setRegistrations] = useState([]);
  const [event, setEvent] = useState(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);

  useEffect(() => {
    const fetchData = async () => {
      try {
        const [eventData, regData] = await Promise.all([
          eventService.getEventById(id),
          registrationService.getEventRegistrations(id)
        ]);
        setEvent(eventData);
        setRegistrations(regData);
      } catch (err) {
        setError('Failed to load registrations.');
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
        <h1 className="text-3xl font-bold text-slate-900">Event Registrations</h1>
        <p className="mt-2 text-slate-500">{event ? `Manage registrations for ${event.title}` : 'Loading...'}</p>
      </div>

      {error ? (
        <Card className="p-12 text-center text-red-600 border border-red-200 bg-red-50">
          <p>{error}</p>
        </Card>
      ) : loading ? (
        <div className="p-12 text-center text-slate-500 animate-pulse">Loading registrations...</div>
      ) : registrations.length > 0 ? (
        <Card className="overflow-hidden border border-slate-200">
          <div className="px-6 py-4 border-b border-slate-200 bg-slate-50 flex justify-between items-center">
            <span className="text-sm font-medium text-slate-700">Total: {registrations.length} registrations</span>
          </div>
          <div className="overflow-x-auto">
            <table className="min-w-full divide-y divide-slate-200">
              <thead className="bg-slate-50">
                <tr>
                  <th scope="col" className="px-6 py-3 text-left text-xs font-medium text-slate-500 uppercase tracking-wider">Participant / Team</th>
                  <th scope="col" className="px-6 py-3 text-left text-xs font-medium text-slate-500 uppercase tracking-wider">Email</th>
                  <th scope="col" className="px-6 py-3 text-left text-xs font-medium text-slate-500 uppercase tracking-wider">Registration Date</th>
                  <th scope="col" className="px-6 py-3 text-left text-xs font-medium text-slate-500 uppercase tracking-wider">Status</th>
                  <th scope="col" className="px-6 py-3 text-left text-xs font-medium text-slate-500 uppercase tracking-wider">Payment</th>
                </tr>
              </thead>
              <tbody className="bg-white divide-y divide-slate-200">
                {registrations.map(reg => (
                  <tr key={reg.id}>
                    <td className="px-6 py-4">
                      <div className="flex items-start">
                        <div className="flex-shrink-0 h-10 w-10 bg-slate-100 rounded-full flex items-center justify-center text-slate-400 mt-1">
                          <Users className="h-5 w-5" />
                        </div>
                        <div className="ml-4 flex-1">
                          <div className="text-sm font-medium text-slate-900">
                            {reg.teamName ? `Team: ${reg.teamName}` : (reg.participantName || `User ID: ${reg.participantId}`)}
                            {reg.teamSize && <Badge variant="info" className="ml-2 text-xs">Size: {reg.teamSize}</Badge>}
                          </div>
                          {reg.teamMembers && reg.teamMembers.length > 0 && (
                            <div className="mt-2 space-y-1">
                              <p className="text-xs font-semibold text-slate-500 uppercase">Members:</p>
                              {reg.teamMembers.map((m, idx) => (
                                <div key={idx} className="text-xs text-slate-600 flex justify-between border-b border-slate-100 pb-1">
                                  <span>{m.role === 'LEADER' ? '👑 ' : ''}{m.memberName}</span>
                                  <span className="text-slate-400">{m.memberEmail} • {m.memberPhone}</span>
                                </div>
                              ))}
                            </div>
                          )}
                        </div>
                      </div>
                    </td>
                    <td className="px-6 py-4 whitespace-nowrap text-sm text-slate-500 align-top pt-5">
                      {reg.participantEmail || 'N/A'}
                    </td>
                    <td className="px-6 py-4 whitespace-nowrap text-sm text-slate-500 align-top pt-5">
                      {reg.registeredAt ? new Date(reg.registeredAt).toLocaleDateString() : 'Date unavailable'}
                    </td>
                    <td className="px-6 py-4 whitespace-nowrap align-top pt-5">
                      <Badge variant={reg.status === 'APPROVED' ? 'success' : (reg.status === 'REJECTED' ? 'danger' : 'warning')}>
                        {reg.status}
                      </Badge>
                    </td>
                    <td className="px-6 py-4 whitespace-nowrap align-top pt-5">
                      <div className="flex flex-col gap-1 items-start">
                        <Badge variant={
                          reg.paymentStatus === 'PAID' ? 'success' : 
                          reg.paymentStatus === 'FAILED' ? 'danger' : 
                          reg.paymentStatus === 'PENDING' ? 'warning' : 'default'
                        }>
                          {reg.paymentStatus || 'NOT_REQUIRED'}
                        </Badge>
                        {reg.paymentAmount > 0 && <span className="text-xs text-slate-500">₹{reg.paymentAmount}</span>}
                      </div>
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        </Card>
      ) : (
        <Card className="p-12 text-center border border-slate-200">
          <Calendar className="mx-auto h-12 w-12 text-slate-300 mb-4" />
          <h3 className="text-lg font-medium text-slate-900">No registrations yet</h3>
          <p className="mt-1 text-slate-500">Participants haven't registered for this event.</p>
        </Card>
      )}
    </div>
  );
};

export default EventRegistrations;
