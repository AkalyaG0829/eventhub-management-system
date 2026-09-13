import React, { useEffect, useState, useCallback } from 'react';
import { Link } from 'react-router-dom';
import { Search, Filter, Calendar, MapPin, Users, Video } from 'lucide-react';
import eventService from '../services/eventService';
import Card from '../components/Card';
import Badge from '../components/Badge';
import Input from '../components/Input';
import Button from '../components/Button';

const Events = () => {
  const [events, setEvents] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);
  
  const [searchTerm, setSearchTerm] = useState('');
  const [typeFilter, setTypeFilter] = useState('');
  const [registrationTypeFilter, setRegistrationTypeFilter] = useState('');
  const [sortOption, setSortOption] = useState('startDate,asc');
  
  const [page, setPage] = useState(0);
  const [totalPages, setTotalPages] = useState(0);

  // Debounce search term
  const [debouncedSearchTerm, setDebouncedSearchTerm] = useState(searchTerm);

  useEffect(() => {
    const handler = setTimeout(() => {
      setDebouncedSearchTerm(searchTerm);
      setPage(0); // Reset page on new search
    }, 500);
    return () => clearTimeout(handler);
  }, [searchTerm]);

  const fetchEvents = useCallback(async () => {
    setLoading(true);
    setError(null);
    try {
      const [sortBy, sortDir] = sortOption.split(',');
      const params = {
        page,
        size: 9, // 3x3 grid
        search: debouncedSearchTerm,
        type: typeFilter,
        registrationType: registrationTypeFilter,
        sortBy,
        sortDir
      };
      
      const data = await eventService.getPublishedEvents(params);
      setEvents(data.events);
      setTotalPages(data.totalPages);
    } catch (err) {
      setError('Failed to load events. Please try again later.');
    } finally {
      setLoading(false);
    }
  }, [page, debouncedSearchTerm, typeFilter, registrationTypeFilter, sortOption]);

  useEffect(() => {
    fetchEvents();
  }, [fetchEvents]);

  const handleFilterChange = (setter) => (e) => {
    setter(e.target.value);
    setPage(0); // Reset to first page when changing filters
  };

  const handleClearFilters = () => {
    setSearchTerm('');
    setDebouncedSearchTerm('');
    setTypeFilter('');
    setRegistrationTypeFilter('');
    setSortOption('startDate,asc');
    setPage(0);
  };

  return (
    <div className="space-y-6">
      <div>
        <h1 className="text-3xl font-bold text-slate-900">Discover Events</h1>
        <p className="mt-2 text-slate-500">Find and participate in upcoming hackathons, workshops, and webinars.</p>
      </div>

      <Card className="p-4 flex flex-col md:flex-row gap-4 items-center bg-white shadow-sm border border-slate-200">
        <div className="relative flex-grow w-full md:w-auto">
          <div className="absolute inset-y-0 left-0 pl-3 flex items-center pointer-events-none">
            <Search className="h-5 w-5 text-slate-400" />
          </div>
          <input
            type="text"
            className="focus:ring-primary-500 focus:border-primary-500 block w-full pl-10 sm:text-sm border-slate-300 rounded-md py-2 px-3 border"
            placeholder="Search events by title..."
            value={searchTerm}
            onChange={(e) => setSearchTerm(e.target.value)}
          />
        </div>
        <div className="flex flex-col sm:flex-row gap-4 w-full md:w-auto">
          <div className="relative flex items-center border border-slate-300 rounded-md bg-white">
            <div className="pl-3 pointer-events-none">
              <Filter className="h-4 w-4 text-slate-400" />
            </div>
            <select
              className="focus:ring-primary-500 focus:border-primary-500 block w-full pl-2 pr-8 py-2 sm:text-sm border-none bg-transparent rounded-md"
              value={typeFilter}
              onChange={handleFilterChange(setTypeFilter)}
            >
              <option value="">All Types</option>
              <option value="HACKATHON">Hackathon</option>
              <option value="WORKSHOP">Workshop</option>
              <option value="WEBINAR">Webinar</option>
            </select>
          </div>
          
          <div className="relative flex items-center border border-slate-300 rounded-md bg-white">
            <select
              className="focus:ring-primary-500 focus:border-primary-500 block w-full px-3 py-2 sm:text-sm border-none bg-transparent rounded-md"
              value={registrationTypeFilter}
              onChange={handleFilterChange(setRegistrationTypeFilter)}
            >
              <option value="">Any Cost</option>
              <option value="FREE">Free Only</option>
              <option value="PAID">Paid Only</option>
            </select>
          </div>

          <div className="relative flex items-center border border-slate-300 rounded-md bg-white">
            <select
              className="focus:ring-primary-500 focus:border-primary-500 block w-full px-3 py-2 sm:text-sm border-none bg-transparent rounded-md"
              value={sortOption}
              onChange={handleFilterChange(setSortOption)}
            >
              <option value="startDate,asc">Upcoming</option>
              <option value="registrationDeadline,asc">Registration Closing Soon</option>
            </select>
          </div>
        </div>
      </Card>

      {error && (
        <div className="bg-red-50 border-l-4 border-red-400 p-4 rounded">
           <p className="text-sm text-red-700">{error}</p>
        </div>
      )}

      {loading ? (
        <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6">
          {[1, 2, 3, 4, 5, 6].map(i => (
            <div key={i} className="animate-pulse bg-white rounded-lg h-80 border border-slate-200"></div>
          ))}
        </div>
      ) : events.length > 0 ? (
        <>
          <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6">
            {events.map(event => (
              <Card key={event.id} className="flex flex-col border border-slate-200 hover:shadow-md transition overflow-hidden">
                <div className="h-32 bg-slate-100 flex items-center justify-center border-b border-slate-200 relative">
                   {event.bannerUrl ? (
                     <img src={event.bannerUrl} alt={event.title} className="w-full h-full object-cover" />
                   ) : (
                     <Calendar className="w-12 h-12 text-slate-300" />
                   )}
                   <div className="absolute top-2 right-2">
                     <Badge variant={event.registrationType === 'PAID' ? 'warning' : 'success'}>
                        {event.registrationType === 'PAID' ? `₹${event.registrationFee || 0}` : 'FREE'}
                     </Badge>
                   </div>
                </div>
                <div className="p-5 flex flex-col flex-grow">
                  <div className="flex justify-between items-start mb-2">
                    <Badge variant={event.type === 'HACKATHON' ? 'info' : (event.type === 'WORKSHOP' ? 'success' : 'default')}>
                      {event.type}
                    </Badge>
                    <Badge variant={event.status === 'PUBLISHED' ? 'success' : 'warning'}>
                      {event.status === 'PUBLISHED' ? 'Open' : event.status}
                    </Badge>
                  </div>
                  <h3 className="text-lg font-bold text-slate-900 mb-1">{event.title}</h3>
                  <p className="text-sm text-slate-500 line-clamp-2 mb-4">{event.description}</p>
                  
                  <div className="mt-auto space-y-2">
                    <div className="flex items-center text-xs text-slate-500 gap-1">
                      <Calendar className="w-3 h-3" />
                      <span>{new Date(event.startDate).toLocaleDateString()}</span>
                    </div>
                    
                    <div className="flex justify-between items-center text-xs text-slate-500">
                      {event.type === 'HACKATHON' && (
                        <div className="flex items-center gap-1">
                           <Users className="w-3 h-3" /> {event.participationType}
                        </div>
                      )}
                      {event.type === 'WORKSHOP' && (
                        <div className="flex items-center gap-1">
                           <MapPin className="w-3 h-3" /> {event.workshopMode}
                        </div>
                      )}
                      {event.type === 'WEBINAR' && (
                        <div className="flex items-center gap-1">
                           <Video className="w-3 h-3" /> {event.webinarPlatform}
                        </div>
                      )}
                      <div className="text-red-500 font-medium text-right">
                         Closes: {new Date(event.registrationDeadline).toLocaleDateString()}
                      </div>
                    </div>
                    
                    <Link to={`/events/${event.id}`}>
                      <Button variant="primary" className="w-full mt-3">View Details</Button>
                    </Link>
                  </div>
                </div>
              </Card>
            ))}
          </div>

          {totalPages > 1 && (
            <div className="flex justify-center items-center gap-4 mt-8">
              <Button 
                variant="secondary" 
                onClick={() => setPage(p => Math.max(0, p - 1))}
                disabled={page === 0}
              >
                Previous
              </Button>
              <span className="text-sm text-slate-500">
                Page {page + 1} of {totalPages}
              </span>
              <Button 
                variant="secondary" 
                onClick={() => setPage(p => Math.min(totalPages - 1, p + 1))}
                disabled={page >= totalPages - 1}
              >
                Next
              </Button>
            </div>
          )}
        </>
      ) : (
        <div className="text-center py-16 bg-white rounded-lg border border-slate-200 shadow-sm">
          <Calendar className="mx-auto h-12 w-12 text-slate-300 mb-4" />
          <h3 className="text-lg font-medium text-slate-900">No events found</h3>
          <p className="mt-1 text-slate-500">Try adjusting your search or filters.</p>
          <Button variant="secondary" onClick={handleClearFilters} className="mt-4">
            Clear filters
          </Button>
        </div>
      )}
    </div>
  );
};

export default Events;
