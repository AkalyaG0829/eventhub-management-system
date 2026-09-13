import React, { useEffect, useState } from 'react';
import { useParams, Link } from 'react-router-dom';
import { Calendar, MapPin, Users, Info, DollarSign, Clock, Trophy } from 'lucide-react';
import eventService from '../services/eventService';
import registrationService from '../services/registrationService';
import { useAuth } from '../context/AuthContext';
import Card from '../components/Card';
import Badge from '../components/Badge';
import Button from '../components/Button';
import RegistrationFormModal from '../components/RegistrationFormModal';

const EventDetails = () => {
  const { id } = useParams();
  const { isAuthenticated, user, hasRole } = useAuth();
  
  const [event, setEvent] = useState(null);
  const [isRegistered, setIsRegistered] = useState(false);
  
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);
  const [successMsg, setSuccessMsg] = useState(null);
  const [showModal, setShowModal] = useState(false);

  useEffect(() => {
    fetchEventDetails();
  }, [id, isAuthenticated]);

  const fetchEventDetails = async () => {
    setLoading(true);
    try {
      const data = await eventService.getEventById(id);
      setEvent(data);
      
      if (isAuthenticated && user?.role === 'PARTICIPANT') {
        await checkRegistrationStatus(id);
      }
    } catch (err) {
      setError(err.response?.data?.message || 'Failed to load event details.');
    } finally {
      setLoading(false);
    }
  };

  const checkRegistrationStatus = async (eventId) => {
    try {
      const myRegs = await registrationService.getMyRegistrations();
      const registered = myRegs.some(r => r.eventId === parseInt(eventId));
      setIsRegistered(registered);
    } catch (e) {
      console.error("Could not check registration status", e);
    }
  };

  const handleRegisterClick = () => {
    setShowModal(true);
  };

  const handleRegisterSuccess = () => {
    setShowModal(false);
    setIsRegistered(true);
    setSuccessMsg("Successfully registered for the event!");
  };

  if (loading) {
    return <div className="text-center py-20 text-slate-500 animate-pulse">Loading event details...</div>;
  }

  if (!event) {
    return (
      <div className="bg-red-50 text-red-700 p-6 rounded-lg text-center shadow-sm">
        <h2 className="text-xl font-bold">Event Not Found</h2>
        <p className="mt-2">{error}</p>
        <Link to="/events" className="mt-4 inline-block text-red-600 hover:underline">← Back to Events</Link>
      </div>
    );
  }

  const isRegistrationOpen = event.status === 'PUBLISHED' && new Date() < new Date(event.registrationDeadline);

  return (
    <div className="space-y-6 max-w-5xl mx-auto">
      {/* Header / Banner */}
      <div className="bg-white rounded-xl shadow-sm border border-slate-200 overflow-hidden">
        {event.bannerUrl ? (
          <div className="h-48 sm:h-64 w-full">
            <img src={event.bannerUrl} alt={event.title} className="w-full h-full object-cover" />
          </div>
        ) : (
          <div className="h-32 bg-primary-600"></div>
        )}
        
        <div className="p-6 sm:p-8">
          <div className="flex flex-col sm:flex-row sm:items-start justify-between gap-4">
            <div>
              <div className="flex items-center gap-2 mb-3">
                <Badge variant={event.type === 'HACKATHON' ? 'info' : 'default'}>{event.type}</Badge>
                <Badge variant={event.status === 'PUBLISHED' ? 'success' : 'warning'}>{event.status}</Badge>
              </div>
              <h1 className="text-3xl font-bold text-slate-900 mb-2">{event.title}</h1>
              <p className="text-slate-500 flex items-center gap-2">
                <MapPin className="w-4 h-4" /> {event.location || 'Virtual / TBD'}
              </p>
            </div>
            
            <div className="flex-shrink-0">
              {isAuthenticated ? (
                hasRole('PARTICIPANT') ? (
                  isRegistered ? (
                    <Button variant="secondary" disabled className="w-full sm:w-auto text-green-700 bg-green-50 border-green-200">
                      Already Registered
                    </Button>
                  ) : (
                    <Button 
                      onClick={handleRegisterClick} 
                      disabled={!isRegistrationOpen} 
                      className="w-full sm:w-auto"
                    >
                      {!isRegistrationOpen ? 'Registration Closed' : 'Register Now'}
                    </Button>
                  )
                ) : (
                  <Button variant="secondary" disabled className="w-full sm:w-auto">
                    {user?.role}s cannot register
                  </Button>
                )
              ) : (
                <Link to="/login">
                  <Button className="w-full sm:w-auto">Login to Register</Button>
                </Link>
              )}
            </div>
          </div>
          
          {error && <div className="mt-4 bg-red-50 text-red-700 p-3 rounded text-sm">{error}</div>}
          {successMsg && <div className="mt-4 bg-green-50 text-green-700 p-3 rounded text-sm">{successMsg}</div>}
        </div>
      </div>

      <div className="grid grid-cols-1 lg:grid-cols-3 gap-6">
        {/* Left Column - Main Details */}
        <div className="lg:col-span-2 space-y-6">
          <Card className="p-6">
            <h2 className="text-xl font-bold text-slate-900 mb-4 flex items-center gap-2">
              <Info className="w-5 h-5 text-slate-400" /> About the Event
            </h2>
            <div className="prose max-w-none text-slate-600 whitespace-pre-wrap">
              {event.description}
            </div>
          </Card>

          {event.rules && (
            <Card className="p-6">
              <h2 className="text-xl font-bold text-slate-900 mb-4">Rules & Guidelines</h2>
              <div className="prose max-w-none text-slate-600 whitespace-pre-wrap">
                {event.rules}
              </div>
            </Card>
          )}

          {event.prizes && (
            <Card className="p-6">
              <h2 className="text-xl font-bold text-slate-900 mb-4 flex items-center gap-2">
                <Trophy className="w-5 h-5 text-yellow-500" /> Prizes
              </h2>
              <div className="prose max-w-none text-slate-600 whitespace-pre-wrap">
                {event.prizes}
              </div>
            </Card>
          )}
        </div>

        {/* Right Column - Meta Details */}
        <div className="space-y-6">
          <Card className="p-6">
            <h3 className="text-lg font-bold text-slate-900 mb-4">Event Details</h3>
            
            <div className="space-y-4">
              <div className="flex items-start gap-3">
                <Calendar className="w-5 h-5 text-slate-400 mt-0.5" />
                <div>
                  <p className="text-sm font-medium text-slate-900">Dates</p>
                  <p className="text-sm text-slate-500">
                    {new Date(event.startDate).toLocaleDateString()} - {new Date(event.endDate).toLocaleDateString()}
                  </p>
                </div>
              </div>

              <div className="flex items-start gap-3">
                <Clock className="w-5 h-5 text-slate-400 mt-0.5" />
                <div>
                  <p className="text-sm font-medium text-slate-900">Registration Deadline</p>
                  <p className="text-sm text-slate-500">{new Date(event.registrationDeadline).toLocaleDateString()}</p>
                </div>
              </div>

              {event.participationType && (
                <div className="flex items-start gap-3">
                  <Users className="w-5 h-5 text-slate-400 mt-0.5" />
                  <div>
                    <p className="text-sm font-medium text-slate-900">Participation Type</p>
                    <p className="text-sm text-slate-500">{event.participationType}</p>
                  </div>
                </div>
              )}

              {event.participationType === 'TEAM' && (
                <div className="flex items-start gap-3">
                  <Users className="w-5 h-5 text-slate-400 mt-0.5" />
                  <div>
                    <p className="text-sm font-medium text-slate-900">Team Size</p>
                    <p className="text-sm text-slate-500">
                      {event.teamSizeMin && event.teamSizeMax 
                        ? `${event.teamSizeMin} - ${event.teamSizeMax} members` 
                        : (event.teamSizeMin ? `Min ${event.teamSizeMin}` : 'Not specified')}
                    </p>
                  </div>
                </div>
              )}

              {event.type === 'WORKSHOP' && (
                <>
                  <div className="flex items-start gap-3">
                    <Info className="w-5 h-5 text-slate-400 mt-0.5" />
                    <div>
                      <p className="text-sm font-medium text-slate-900">Workshop Details</p>
                      <p className="text-sm text-slate-500">Mode: {event.workshopMode || 'N/A'}</p>
                      <p className="text-sm text-slate-500">Level: {event.workshopLevel || 'N/A'}</p>
                      {event.instructor && <p className="text-sm text-slate-500">Instructor: {event.instructor}</p>}
                    </div>
                  </div>
                </>
              )}

              {event.type === 'WEBINAR' && (
                <>
                  <div className="flex items-start gap-3">
                    <Info className="w-5 h-5 text-slate-400 mt-0.5" />
                    <div>
                      <p className="text-sm font-medium text-slate-900">Webinar Details</p>
                      <p className="text-sm text-slate-500">Platform: {event.webinarPlatform || 'N/A'}</p>
                      {event.webinarUrl && (
                        <p className="text-sm text-slate-500 text-primary-600 hover:underline">
                          <a href={event.webinarUrl} target="_blank" rel="noreferrer">Join Link</a>
                        </p>
                      )}
                    </div>
                  </div>
                </>
              )}

              <div className="flex items-start gap-3">
                <Users className="w-5 h-5 text-slate-400 mt-0.5" />
                <div>
                  <p className="text-sm font-medium text-slate-900">Max Participants</p>
                  <p className="text-sm text-slate-500">{event.maxParticipants || 'Unlimited'}</p>
                </div>
              </div>

              <div className="flex items-start gap-3">
                <Info className="w-5 h-5 text-slate-400 mt-0.5" />
                <div>
                  <p className="text-sm font-medium text-slate-900">Registration</p>
                  <p className="text-sm text-slate-500">
                    {event.registrationType === 'PAID' 
                      ? `Paid - ₹${event.registrationFee || 0}`
                      : 'Free'}
                  </p>
                </div>
              </div>
            </div>
          </Card>

          {event.eligibility && (
            <Card className="p-6">
              <h3 className="text-lg font-bold text-slate-900 mb-2">Eligibility</h3>
              <p className="text-sm text-slate-600 whitespace-pre-wrap">{event.eligibility}</p>
            </Card>
          )}
          
          <Card className="p-6">
            <h3 className="text-lg font-bold text-slate-900 mb-2">Organizer</h3>
            <p className="text-sm text-slate-600">{event.organizerName || 'Platform Organizer'}</p>
          </Card>
        </div>
      </div>
      
      <RegistrationFormModal 
        isOpen={showModal} 
        onClose={() => setShowModal(false)} 
        event={event} 
        onRegisterSuccess={handleRegisterSuccess} 
      />
    </div>
  );
};

export default EventDetails;
