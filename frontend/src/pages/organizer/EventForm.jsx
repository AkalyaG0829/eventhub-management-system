import React, { useEffect, useState } from 'react';
import { useNavigate, useParams, Link } from 'react-router-dom';
import { ArrowLeft } from 'lucide-react';
import eventService from '../../services/eventService';
import Card from '../../components/Card';
import Input from '../../components/Input';
import Button from '../../components/Button';

const DateTimeSelect = ({ label, name, value, onChange, required }) => {
  let datePart = '';
  let hourStr = '10';
  let minStr = '00';
  let ampmStr = 'AM';

  if (value && value.length >= 16) {
    datePart = value.substring(0, 10);
    let h = parseInt(value.substring(11, 13), 10);
    const m = value.substring(14, 16);
    if (!isNaN(h)) {
      if (h === 0) {
        hourStr = '12';
        ampmStr = 'AM';
      } else if (h === 12) {
        hourStr = '12';
        ampmStr = 'PM';
      } else if (h > 12) {
        hourStr = String(h - 12).padStart(2, '0');
        ampmStr = 'PM';
      } else {
        hourStr = String(h).padStart(2, '0');
        ampmStr = 'AM';
      }
      minStr = m;
    }
  }

  const updateValue = (d, hStr, mStr, apStr) => {
    if (!d) {
      onChange({ target: { name, value: '' } });
      return;
    }
    let h = parseInt(hStr, 10);
    if (apStr === 'AM' && h === 12) h = 0;
    if (apStr === 'PM' && h !== 12) h += 12;
    const h24 = String(h).padStart(2, '0');
    onChange({ target: { name, value: `${d}T${h24}:${mStr}` } });
  };

  return (
    <div>
      <label className="block text-sm font-medium text-slate-700 mb-1">{label} {required && <span className="text-red-500">*</span>}</label>
      <div className="flex flex-wrap gap-2">
        <input 
          type="date" 
          value={datePart} 
          onChange={(e) => updateValue(e.target.value, hourStr, minStr, ampmStr)} 
          required={required}
          className="flex-1 min-w-[140px] px-3 py-2 border border-slate-300 rounded-md focus:ring-primary-500 focus:border-primary-500 sm:text-sm"
        />
        <div className="flex items-center gap-1">
          <select 
            value={hourStr} 
            onChange={(e) => updateValue(datePart, e.target.value, minStr, ampmStr)}
            required={required}
            className="w-16 px-1 py-2 border border-slate-300 rounded-md focus:ring-primary-500 focus:border-primary-500 sm:text-sm"
          >
            {Array.from({length: 12}, (_, i) => String(i + 1).padStart(2, '0')).map(h => (
              <option key={h} value={h}>{h}</option>
            ))}
          </select>
          <span className="font-bold text-slate-500">:</span>
          <select 
            value={minStr} 
            onChange={(e) => updateValue(datePart, hourStr, e.target.value, ampmStr)}
            required={required}
            className="w-16 px-1 py-2 border border-slate-300 rounded-md focus:ring-primary-500 focus:border-primary-500 sm:text-sm"
          >
            {Array.from({length: 60}, (_, i) => String(i).padStart(2, '0')).map(m => (
              <option key={m} value={m}>{m}</option>
            ))}
          </select>
        </div>
        <select 
          value={ampmStr} 
          onChange={(e) => updateValue(datePart, hourStr, minStr, e.target.value)}
          required={required}
          className="w-16 px-1 py-2 border border-slate-300 rounded-md focus:ring-primary-500 focus:border-primary-500 sm:text-sm"
        >
          <option value="AM">AM</option>
          <option value="PM">PM</option>
        </select>
      </div>
    </div>
  );
};

const EventForm = () => {
  const { id } = useParams();
  const navigate = useNavigate();
  const isEditing = !!id;

  const [formData, setFormData] = useState({
    title: '',
    description: '',
    type: 'HACKATHON',
    location: '',
    startDate: '',
    endDate: '',
    registrationDeadline: '',
    eligibility: '',
    maxParticipants: '',
    teamSizeMin: '',
    teamSizeMax: '',
    registrationFee: '',
    rules: '',
    prizes: '',
    bannerUrl: '',
    participationType: 'INDIVIDUAL',
    workshopMode: 'ONLINE',
    instructor: '',
    capacity: '',
    workshopLevel: 'BEGINNER',
    webinarPlatform: 'GOOGLE_MEET',
    webinarUrl: '',
    registrationType: 'FREE'
  });

  const [loading, setLoading] = useState(isEditing);
  const [submitting, setSubmitting] = useState(false);
  const [error, setError] = useState(null);

  useEffect(() => {
    if (isEditing) {
      fetchEvent();
    }
  }, [id]);

  const fetchEvent = async () => {
    try {
      const data = await eventService.getEventById(id);
      // Format dates for input[type="datetime-local"]
      const formatDateForInput = (dateString) => {
        if (!dateString) return '';
        const d = new Date(dateString);
        return new Date(d.getTime() - d.getTimezoneOffset() * 60000).toISOString().slice(0,16);
      };

      setFormData({
        title: data.title || '',
        description: data.description || '',
        type: data.type || 'HACKATHON',
        location: data.location || '',
        startDate: formatDateForInput(data.startDate),
        endDate: formatDateForInput(data.endDate),
        registrationDeadline: formatDateForInput(data.registrationDeadline),
        eligibility: data.eligibility || '',
        maxParticipants: data.maxParticipants || '',
        teamSizeMin: data.teamSizeMin || '',
        teamSizeMax: data.teamSizeMax || '',
        registrationFee: data.registrationFee || '',
        rules: data.rules || '',
        prizes: data.prizes || '',
        bannerUrl: data.bannerUrl || '',
        participationType: data.participationType || 'INDIVIDUAL',
        workshopMode: data.workshopMode || 'ONLINE',
        instructor: data.instructor || '',
        capacity: data.capacity || '',
        workshopLevel: data.workshopLevel || 'BEGINNER',
        webinarPlatform: data.webinarPlatform || 'GOOGLE_MEET',
        webinarUrl: data.webinarUrl || '',
        registrationType: data.registrationType || 'FREE'
      });
    } catch (err) {
      setError("Failed to load event data.");
    } finally {
      setLoading(false);
    }
  };

  const handleChange = (e) => {
    const { name, value } = e.target;
    setFormData(prev => ({ ...prev, [name]: value }));
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    setSubmitting(true);
    setError(null);
    try {
      const payload = { ...formData };
      
      // Validation
      if (!payload.startDate || payload.startDate.length < 16) {
        throw new Error("Please select a complete Start Date and Time.");
      }
      if (!payload.endDate || payload.endDate.length < 16) {
        throw new Error("Please select a complete End Date and Time.");
      }
      if (!payload.registrationDeadline || payload.registrationDeadline.length < 16) {
        throw new Error("Please select a complete Registration Deadline Date and Time.");
      }
      
      const start = new Date(payload.startDate);
      const end = new Date(payload.endDate);
      const regDeadline = new Date(payload.registrationDeadline);
      
      if (end <= start) {
        throw new Error("End date must be after the start date.");
      }
      if (regDeadline >= start) {
        throw new Error("Registration deadline must be before the event start date.");
      }

      // Parse numbers, fallback to null if empty
      payload.maxParticipants = payload.maxParticipants ? parseInt(payload.maxParticipants) : null;
      if (payload.maxParticipants !== null && payload.maxParticipants < 1) throw new Error("Max Participants must be at least 1.");

      if (payload.type !== 'HACKATHON') {
        payload.participationType = null;
        payload.teamSizeMin = null;
        payload.teamSizeMax = null;
      }

      if (payload.participationType === 'INDIVIDUAL') {
        payload.teamSizeMin = null;
        payload.teamSizeMax = null;
      } else if (payload.participationType === 'TEAM' && payload.type === 'HACKATHON') {
        payload.teamSizeMin = payload.teamSizeMin ? parseInt(payload.teamSizeMin) : null;
        if (payload.teamSizeMin !== null && payload.teamSizeMin < 1) throw new Error("Min Team Size must be at least 1.");
        
        payload.teamSizeMax = payload.teamSizeMax ? parseInt(payload.teamSizeMax) : null;
        if (payload.teamSizeMax !== null && payload.teamSizeMax < 1) throw new Error("Max Team Size must be at least 1.");
        
        if (payload.teamSizeMin !== null && payload.teamSizeMax !== null && payload.teamSizeMax < payload.teamSizeMin) {
          throw new Error("Max Team Size must be greater than or equal to Min Team Size.");
        }
      }

      if (payload.type !== 'WORKSHOP') {
        payload.workshopMode = null;
        payload.instructor = null;
        payload.capacity = null;
        payload.workshopLevel = null;
      } else {
        payload.capacity = payload.capacity ? parseInt(payload.capacity) : null;
      }

      if (payload.type !== 'WEBINAR') {
        payload.webinarPlatform = null;
        payload.webinarUrl = null;
      }

      payload.registrationFee = payload.registrationFee ? parseFloat(payload.registrationFee) : null;
      if (payload.registrationFee !== null && payload.registrationFee < 0) throw new Error("Registration Fee cannot be negative.");

      console.log("CREATE EVENT PAYLOAD:", payload);

      if (isEditing) {
        await eventService.updateEvent(id, payload);
      } else {
        await eventService.createEvent(payload);
      }
      
      // Instead of alerting, just navigate or show a success toast. The prompt asks to navigate and show success.
      alert("Event created successfully");
      navigate('/organizer/events');
    } catch (err) {
      console.error("CREATE EVENT ERROR:", err);
      if (err.response?.data) {
        console.error("Error Details:", err.response.data);
      }
      setError(err.message || err.response?.data?.message || 'Failed to save event. Check your inputs.');
    } finally {
      setSubmitting(false);
    }
  };

  if (loading) return <div className="text-center py-10 animate-pulse">Loading event...</div>;

  return (
    <div className="space-y-6 max-w-4xl mx-auto">
      <div>
        <Link to="/organizer/events" className="text-sm text-primary-600 hover:underline flex items-center gap-1 mb-4">
          <ArrowLeft className="w-4 h-4" /> Back to Events
        </Link>
        <h1 className="text-3xl font-bold text-slate-900">{isEditing ? 'Edit Event' : 'Create New Event'}</h1>
        <p className="mt-2 text-slate-500">Fill out the details for your event below.</p>
      </div>

      {error && (
        <div className="bg-red-50 border-l-4 border-red-400 p-4">
          <p className="text-sm text-red-700">{error}</p>
        </div>
      )}

      <Card className="p-6 border border-slate-200">
        <form onSubmit={handleSubmit} className="space-y-6">
          <div className="grid grid-cols-1 md:grid-cols-2 gap-6">
            <div className="md:col-span-2">
              <Input label="Event Title" name="title" required value={formData.title} onChange={handleChange} />
            </div>
            
            <div className="md:col-span-2">
              <label className="block text-sm font-medium text-slate-700 mb-1">Description</label>
              <textarea 
                name="description" required rows="4" 
                className="w-full px-3 py-2 border border-slate-300 rounded-md focus:ring-primary-500 focus:border-primary-500 sm:text-sm"
                value={formData.description} onChange={handleChange}
              ></textarea>
            </div>

            {/* Registration Type and Fee (Applicable to all events) */}
            <div className="grid grid-cols-1 md:grid-cols-2 gap-6">
              <div>
                <label className="block text-sm font-medium text-slate-700 mb-1">Registration Type <span className="text-red-500">*</span></label>
                <select 
                  className="w-full px-3 py-2 border border-slate-300 rounded-md focus:ring-primary-500 focus:border-primary-500 sm:text-sm"
                  name="registrationType" required
                  value={formData.registrationType} onChange={handleChange}
                >
                  <option value="FREE">Free</option>
                  <option value="PAID">Paid</option>
                </select>
              </div>
              
              {formData.registrationType === 'PAID' && (
                <div>
                  <Input 
                    label="Registration Fee (₹)" 
                    type="number" 
                    name="registrationFee" min="0" step="0.01" 
                    value={formData.registrationFee} onChange={handleChange} 
                    required
                  />
                </div>
              )}
            </div>

            <div>
              <label className="block text-sm font-medium text-slate-700 mb-1">Event Type</label>
              <select
                name="type" required
                value={formData.type} onChange={handleChange}
                className="w-full px-3 py-2 border border-slate-300 rounded-md focus:ring-primary-500 focus:border-primary-500 sm:text-sm"
              >
                <option value="HACKATHON">Hackathon</option>
                <option value="WORKSHOP">Workshop</option>
                <option value="WEBINAR">Webinar</option>
              </select>
            </div>
            
            <Input label="Location" name="location" required value={formData.location} onChange={handleChange} placeholder="e.g. New York, NY or Virtual" />
            
            {formData.type === 'HACKATHON' && (
              <>
                <div>
                  <label className="block text-sm font-medium text-slate-700 mb-1">Participation Type</label>
                  <select
                    name="participationType" required
                    value={formData.participationType} onChange={handleChange}
                    className="w-full px-3 py-2 border border-slate-300 rounded-md focus:ring-primary-500 focus:border-primary-500 sm:text-sm"
                  >
                    <option value="INDIVIDUAL">Individual</option>
                    <option value="TEAM">Team</option>
                  </select>
                </div>
                {formData.participationType === 'TEAM' && (
                  <>
                    <Input label="Min Team Size" type="number" name="teamSizeMin" value={formData.teamSizeMin} onChange={handleChange} />
                    <Input label="Max Team Size" type="number" name="teamSizeMax" value={formData.teamSizeMax} onChange={handleChange} />
                  </>
                )}
              </>
            )}

            {formData.type === 'WORKSHOP' && (
              <>
                <div>
                  <label className="block text-sm font-medium text-slate-700 mb-1">Workshop Mode</label>
                  <select
                    name="workshopMode" required
                    value={formData.workshopMode} onChange={handleChange}
                    className="w-full px-3 py-2 border border-slate-300 rounded-md focus:ring-primary-500 focus:border-primary-500 sm:text-sm"
                  >
                    <option value="ONLINE">Online</option>
                    <option value="OFFLINE">Offline</option>
                    <option value="HYBRID">Hybrid</option>
                  </select>
                </div>
                <div>
                  <label className="block text-sm font-medium text-slate-700 mb-1">Workshop Level</label>
                  <select
                    name="workshopLevel" required
                    value={formData.workshopLevel} onChange={handleChange}
                    className="w-full px-3 py-2 border border-slate-300 rounded-md focus:ring-primary-500 focus:border-primary-500 sm:text-sm"
                  >
                    <option value="BEGINNER">Beginner</option>
                    <option value="INTERMEDIATE">Intermediate</option>
                    <option value="ADVANCED">Advanced</option>
                  </select>
                </div>
                <Input label="Instructor / Speaker" name="instructor" value={formData.instructor} onChange={handleChange} placeholder="Instructor Name" />
                <Input label="Capacity" type="number" name="capacity" value={formData.capacity} onChange={handleChange} placeholder="e.g. 50" />
              </>
            )}

            {formData.type === 'WEBINAR' && (
              <>
                <div>
                  <label className="block text-sm font-medium text-slate-700 mb-1">Webinar Platform</label>
                  <select
                    name="webinarPlatform" required
                    value={formData.webinarPlatform} onChange={handleChange}
                    className="w-full px-3 py-2 border border-slate-300 rounded-md focus:ring-primary-500 focus:border-primary-500 sm:text-sm"
                  >
                    <option value="GOOGLE_MEET">Google Meet</option>
                    <option value="ZOOM">Zoom</option>
                    <option value="MICROSOFT_TEAMS">Microsoft Teams</option>
                    <option value="OTHER">Other</option>
                  </select>
                </div>

                <div className="md:col-span-2">
                  <Input label="Webinar URL" name="webinarUrl" type="url" value={formData.webinarUrl} onChange={handleChange} placeholder="https://..." />
                </div>
              </>
            )}

            <div className="md:col-span-2 space-y-4">
              <DateTimeSelect label="Start Date & Time" name="startDate" required value={formData.startDate} onChange={handleChange} />
              <DateTimeSelect label="End Date & Time" name="endDate" required value={formData.endDate} onChange={handleChange} />
              <DateTimeSelect label="Registration Deadline" name="registrationDeadline" required value={formData.registrationDeadline} onChange={handleChange} />
            </div>
            
            <Input label="Max Participants" type="number" name="maxParticipants" value={formData.maxParticipants} onChange={handleChange} placeholder="Leave empty for unlimited" />
            


            <div className="md:col-span-2">
              <label className="block text-sm font-medium text-slate-700 mb-1">Eligibility Criteria</label>
              <textarea 
                name="eligibility" rows="2" 
                className="w-full px-3 py-2 border border-slate-300 rounded-md focus:ring-primary-500 focus:border-primary-500 sm:text-sm"
                value={formData.eligibility} onChange={handleChange}
              ></textarea>
            </div>
            
            <div className="md:col-span-2">
              <label className="block text-sm font-medium text-slate-700 mb-1">Rules & Guidelines</label>
              <textarea 
                name="rules" rows="3" 
                className="w-full px-3 py-2 border border-slate-300 rounded-md focus:ring-primary-500 focus:border-primary-500 sm:text-sm"
                value={formData.rules} onChange={handleChange}
              ></textarea>
            </div>

            <div className="md:col-span-2">
              <label className="block text-sm font-medium text-slate-700 mb-1">Prizes</label>
              <textarea 
                name="prizes" rows="2" 
                className="w-full px-3 py-2 border border-slate-300 rounded-md focus:ring-primary-500 focus:border-primary-500 sm:text-sm"
                value={formData.prizes} onChange={handleChange}
              ></textarea>
            </div>
            
            <div className="md:col-span-2">
              <Input label="Banner Image URL" name="bannerUrl" type="url" value={formData.bannerUrl} onChange={handleChange} placeholder="https://..." />
            </div>
          </div>

          <div className="flex justify-end gap-4 pt-4 border-t border-slate-200">
            <Link to="/organizer/events">
              <Button type="button" variant="ghost">Cancel</Button>
            </Link>
            <Button type="submit" disabled={submitting}>
              {submitting ? 'Saving...' : 'Save Event'}
            </Button>
          </div>
        </form>
      </Card>
    </div>
  );
};

export default EventForm;
