import React, { useEffect, useState } from 'react';
import { Link } from 'react-router-dom';
import { Calendar, Users, Trophy, ArrowRight } from 'lucide-react';
import eventService from '../services/eventService';
import Card from '../components/Card';
import Badge from '../components/Badge';

const Home = () => {
  const [featuredEvents, setFeaturedEvents] = useState([]);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    const fetchEvents = async () => {
      try {
        const data = await eventService.getPublishedEvents();
        // Just take first 3 for featured
        setFeaturedEvents(data.events.slice(0, 3));
      } catch (error) {
        console.error("Failed to fetch events", error);
      } finally {
        setLoading(false);
      }
    };
    fetchEvents();
  }, []);

  return (
    <div className="flex flex-col gap-12 pb-12">
      {/* Hero Section */}
      <div className="bg-primary-600 text-white rounded-2xl p-8 sm:p-16 text-center mt-6">
        <h1 className="text-4xl sm:text-5xl font-extrabold mb-6 tracking-tight">
          Manage Hackathons <br /> & Events with Ease
        </h1>
        <p className="text-lg sm:text-xl text-primary-100 max-w-2xl mx-auto mb-10">
          The all-in-one platform for organizers to host, and participants to discover, register, form teams, and submit projects for events globally.
        </p>
        <div className="flex justify-center gap-4">
          <Link to="/events" className="bg-white text-primary-600 hover:bg-slate-50 font-semibold px-6 py-3 rounded-lg shadow-sm transition">
            Browse Events
          </Link>
          <Link to="/register" className="bg-primary-700 text-white hover:bg-primary-800 font-semibold px-6 py-3 rounded-lg shadow-sm transition border border-primary-500">
            Create an Account
          </Link>
        </div>
      </div>

      {/* Features Section */}
      <div className="grid grid-cols-1 md:grid-cols-3 gap-8 text-center px-4">
        <div className="flex flex-col items-center">
          <div className="bg-blue-100 p-4 rounded-full text-blue-600 mb-4">
            <Calendar className="w-8 h-8" />
          </div>
          <h3 className="text-xl font-bold mb-2">Discover Events</h3>
          <p className="text-slate-600">Find the best hackathons and technical events curated just for you.</p>
        </div>
        <div className="flex flex-col items-center">
          <div className="bg-green-100 p-4 rounded-full text-green-600 mb-4">
            <Users className="w-8 h-8" />
          </div>
          <h3 className="text-xl font-bold mb-2">Form Teams</h3>
          <p className="text-slate-600">Connect with other participants, build a team, and collaborate seamlessly.</p>
        </div>
        <div className="flex flex-col items-center">
          <div className="bg-purple-100 p-4 rounded-full text-purple-600 mb-4">
            <Trophy className="w-8 h-8" />
          </div>
          <h3 className="text-xl font-bold mb-2">Compete & Win</h3>
          <p className="text-slate-600">Submit your projects, get evaluated by experts, and climb the leaderboard.</p>
        </div>
      </div>

      {/* Featured Events */}
      <div className="px-4">
        <div className="flex justify-between items-end mb-6">
          <div>
            <h2 className="text-3xl font-bold text-slate-900">Featured Events</h2>
            <p className="text-slate-500 mt-1">Check out some of our upcoming events.</p>
          </div>
          <Link to="/events" className="text-primary-600 hover:text-primary-700 font-medium flex items-center gap-1 hidden sm:flex">
            View all <ArrowRight className="w-4 h-4" />
          </Link>
        </div>

        {loading ? (
          <div className="grid grid-cols-1 md:grid-cols-3 gap-6">
            {[1, 2, 3].map(i => (
              <div key={i} className="animate-pulse bg-white rounded-lg h-64 border border-slate-200"></div>
            ))}
          </div>
        ) : featuredEvents.length > 0 ? (
          <div className="grid grid-cols-1 md:grid-cols-3 gap-6">
            {featuredEvents.map(event => (
              <Card key={event.id} className="flex flex-col border border-slate-200 hover:shadow-lg transition">
                <div className="h-32 bg-slate-100 flex items-center justify-center border-b border-slate-200">
                   {event.bannerUrl ? (
                     <img src={event.bannerUrl} alt={event.title} className="w-full h-full object-cover" />
                   ) : (
                     <Calendar className="w-12 h-12 text-slate-300" />
                   )}
                </div>
                <div className="p-5 flex flex-col flex-grow">
                  <div className="flex justify-between items-start mb-2">
                    <Badge variant={event.type === 'HACKATHON' ? 'info' : 'default'}>{event.type}</Badge>
                    <span className="text-xs text-slate-500">{new Date(event.startDate).toLocaleDateString()}</span>
                  </div>
                  <h3 className="text-lg font-bold text-slate-900 mb-1">{event.title}</h3>
                  <p className="text-sm text-slate-500 line-clamp-2 mb-4">{event.description}</p>
                  <div className="mt-auto">
                    <Link to={`/events/${event.id}`} className="text-primary-600 hover:text-primary-700 text-sm font-semibold flex items-center gap-1">
                      View Details <ArrowRight className="w-4 h-4" />
                    </Link>
                  </div>
                </div>
              </Card>
            ))}
          </div>
        ) : (
          <div className="text-center py-12 bg-white rounded-lg border border-slate-200">
            <p className="text-slate-500">No events found. Check back later!</p>
          </div>
        )}
      </div>
    </div>
  );
};

export default Home;
