import React, { useEffect, useState } from 'react';
import { Link } from 'react-router-dom';
import registrationService from '../../services/registrationService';
import Card from '../../components/Card';
import Badge from '../../components/Badge';
import { Calendar } from 'lucide-react';

const MyRegistrations = () => {
  const [registrations, setRegistrations] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);

  useEffect(() => {
    fetchRegistrations();
  }, []);

  const fetchRegistrations = async () => {
    try {
      const data = await registrationService.getMyRegistrations();
      setRegistrations(data);
    } catch (err) {
      setError('Failed to load your registrations.');
    } finally {
      setLoading(false);
    }
  };

  const handleSimulatePayment = async (eventId, registrationId, success) => {
    try {
      await registrationService.simulatePayment(eventId, registrationId, success);
      fetchRegistrations(); // Refresh data
    } catch (err) {
      setError('Failed to simulate payment: ' + (err.response?.data?.message || err.message));
    }
  };

  return (
    <div className="space-y-6">
      <div>
        <h1 className="text-3xl font-bold text-slate-900">My Registrations</h1>
        <p className="mt-2 text-slate-500">Track the status of events you have applied to.</p>
      </div>

      {error && (
        <div className="bg-red-50 text-red-700 p-4 rounded-md">
          {error}
        </div>
      )}

      {loading ? (
        <div className="p-12 text-center text-slate-500 animate-pulse">Loading registrations...</div>
      ) : registrations.length > 0 ? (
        <Card className="overflow-hidden border border-slate-200">
          <div className="overflow-x-auto">
            <table className="min-w-full divide-y divide-slate-200">
              <thead className="bg-slate-50">
                <tr>
                  <th scope="col" className="px-6 py-3 text-left text-xs font-medium text-slate-500 uppercase tracking-wider">Event</th>
                  <th scope="col" className="px-6 py-3 text-left text-xs font-medium text-slate-500 uppercase tracking-wider">Registration Date</th>
                  <th scope="col" className="px-6 py-3 text-left text-xs font-medium text-slate-500 uppercase tracking-wider">Status</th>
                  <th scope="col" className="px-6 py-3 text-left text-xs font-medium text-slate-500 uppercase tracking-wider">Payment</th>
                  <th scope="col" className="px-6 py-3 text-left text-xs font-medium text-slate-500 uppercase tracking-wider">Actions</th>
                </tr>
              </thead>
              <tbody className="bg-white divide-y divide-slate-200">
                {registrations.map(reg => (
                  <tr key={reg.id}>
                    <td className="px-6 py-4 whitespace-nowrap">
                      <div className="flex items-center">
                        <div className="flex-shrink-0 h-10 w-10 bg-slate-100 rounded-md flex items-center justify-center text-slate-400">
                          <Calendar className="h-5 w-5" />
                        </div>
                        <div className="ml-4">
                          <div className="text-sm font-medium text-slate-900">{reg.eventTitle}</div>
                          <div className="text-sm text-slate-500">Event ID: {reg.eventId}</div>
                        </div>
                      </div>
                    </td>
                    <td className="px-6 py-4 whitespace-nowrap text-sm text-slate-500">
                      {reg.registeredAt ? new Date(reg.registeredAt).toLocaleDateString() : 'Date unavailable'}
                    </td>
                    <td className="px-6 py-4 whitespace-nowrap">
                      <Badge variant={reg.status === 'APPROVED' ? 'success' : (reg.status === 'REJECTED' ? 'danger' : 'warning')}>
                        {reg.status}
                      </Badge>
                    </td>
                    <td className="px-6 py-4 whitespace-nowrap">
                      <div className="flex flex-col gap-1">
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
                    <td className="px-6 py-4 whitespace-nowrap text-sm font-medium">
                      <Link to={`/events/${reg.eventId}`} className="text-primary-600 hover:text-primary-900 mr-4">View Event</Link>
                      {reg.paymentStatus === 'PENDING' && (
                        <div className="flex flex-col mt-2 p-2 bg-yellow-50 border border-yellow-200 rounded text-xs">
                          <span className="font-bold text-yellow-800 mb-1">DEV/TEST: Simulate Gateway</span>
                          <div className="flex gap-2">
                            <button onClick={() => handleSimulatePayment(reg.eventId, reg.id, true)} className="text-green-700 hover:bg-green-100 border border-green-600 bg-white rounded px-2 py-1">Simulate Success</button>
                            <button onClick={() => handleSimulatePayment(reg.eventId, reg.id, false)} className="text-red-700 hover:bg-red-100 border border-red-600 bg-white rounded px-2 py-1">Simulate Failure</button>
                          </div>
                        </div>
                      )}
                      {reg.status === 'APPROVED' && (
                        <Link to={`/my-teams`} className="text-green-600 hover:text-green-900">Manage Team</Link>
                      )}
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
          <h3 className="text-lg font-medium text-slate-900">No registrations found</h3>
          <p className="mt-1 text-slate-500">You haven't registered for any events yet.</p>
          <div className="mt-6">
            <Link to="/events" className="inline-flex items-center px-4 py-2 border border-transparent shadow-sm text-sm font-medium rounded-md text-white bg-primary-600 hover:bg-primary-700">
              Browse Events
            </Link>
          </div>
        </Card>
      )}
    </div>
  );
};

export default MyRegistrations;
