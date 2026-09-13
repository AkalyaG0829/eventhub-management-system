import React, { useEffect, useState } from 'react';
import { ShieldAlert, Users, Calendar, BarChart, CheckCircle, XCircle } from 'lucide-react';
import { useAuth } from '../../context/AuthContext';
import Card from '../../components/Card';
import adminService from '../../services/adminService';

const AdminDashboard = () => {
  const { user } = useAuth();
  
  const [stats, setStats] = useState(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);

  useEffect(() => {
    const fetchAdminData = async () => {
      try {
        const data = await adminService.getSystemStats();
        setStats(data);
      } catch (err) {
        console.error("Failed to load admin stats", err);
        setError("Failed to load admin statistics.");
      } finally {
        setLoading(false);
      }
    };
    
    fetchAdminData();
  }, []);

  return (
    <div className="space-y-6">
      <div>
        <h1 className="text-3xl font-bold text-slate-900 flex items-center gap-3">
          <ShieldAlert className="w-8 h-8 text-primary-600" /> Admin Dashboard
        </h1>
        <p className="mt-2 text-slate-500">Platform overview and management.</p>
      </div>

      {error ? (
        <Card className="p-12 text-center text-red-600 border border-red-200 bg-red-50">
          <p>{error}</p>
        </Card>
      ) : loading ? (
        <div className="p-12 text-center text-slate-500 animate-pulse">Loading platform statistics...</div>
      ) : (
        <div className="space-y-8">
          <section>
            <h2 className="text-lg font-semibold text-slate-800 mb-4">Platform Events</h2>
            <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-6">
              <Card className="p-6 flex items-center gap-4 border border-slate-200">
                <div className="p-3 bg-blue-100 text-blue-600 rounded-lg">
                  <Calendar className="w-6 h-6" />
                </div>
                <div>
                  <p className="text-sm font-medium text-slate-500">Total Events</p>
                  <p className="text-2xl font-bold text-slate-900">{stats.totalEvents}</p>
                </div>
              </Card>
              
              <Card className="p-6 flex items-center gap-4 border border-slate-200">
                <div className="p-3 bg-green-100 text-green-600 rounded-lg">
                  <CheckCircle className="w-6 h-6" />
                </div>
                <div>
                  <p className="text-sm font-medium text-slate-500">Published</p>
                  <p className="text-xl font-bold text-slate-900">{stats.publishedEvents}</p>
                </div>
              </Card>

              <Card className="p-6 flex items-center gap-4 border border-slate-200">
                <div className="p-3 bg-slate-100 text-slate-600 rounded-lg">
                  <Calendar className="w-6 h-6" />
                </div>
                <div>
                  <p className="text-sm font-medium text-slate-500">Drafts</p>
                  <p className="text-xl font-bold text-slate-900">{stats.draftEvents}</p>
                </div>
              </Card>
              
              <Card className="p-6 flex items-center gap-4 border border-slate-200">
                <div className="p-3 bg-red-100 text-red-600 rounded-lg">
                  <XCircle className="w-6 h-6" />
                </div>
                <div>
                  <p className="text-sm font-medium text-slate-500">Cancelled</p>
                  <p className="text-xl font-bold text-slate-900">{stats.cancelledEvents}</p>
                </div>
              </Card>
            </div>
          </section>
          
          <section>
            <h2 className="text-lg font-semibold text-slate-800 mb-4">Users & Engagement</h2>
            <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-5 gap-6">
              <Card className="p-6 flex items-center gap-4 border border-slate-200">
                <div className="p-3 bg-purple-100 text-purple-600 rounded-lg">
                  <Users className="w-6 h-6" />
                </div>
                <div>
                  <p className="text-sm font-medium text-slate-500">Total</p>
                  <p className="text-xl font-bold text-slate-900">{stats.totalUsers}</p>
                </div>
              </Card>

              <Card className="p-6 flex items-center gap-4 border border-slate-200">
                <div className="p-3 bg-indigo-100 text-indigo-600 rounded-lg">
                  <Users className="w-6 h-6" />
                </div>
                <div>
                  <p className="text-sm font-medium text-slate-500">Participants</p>
                  <p className="text-xl font-bold text-slate-900">{stats.totalParticipants}</p>
                </div>
              </Card>

              <Card className="p-6 flex items-center gap-4 border border-slate-200">
                <div className="p-3 bg-pink-100 text-pink-600 rounded-lg">
                  <Users className="w-6 h-6" />
                </div>
                <div>
                  <p className="text-sm font-medium text-slate-500">Organizers</p>
                  <p className="text-xl font-bold text-slate-900">{stats.totalOrganizers}</p>
                </div>
              </Card>

              <Card className="p-6 flex items-center gap-4 border border-slate-200">
                <div className="p-3 bg-green-100 text-green-600 rounded-lg">
                  <CheckCircle className="w-6 h-6" />
                </div>
                <div>
                  <p className="text-sm font-medium text-slate-500">Active</p>
                  <p className="text-xl font-bold text-slate-900">{stats.activeUsers}</p>
                </div>
              </Card>

              <Card className="p-6 flex items-center gap-4 border border-slate-200">
                <div className="p-3 bg-red-100 text-red-600 rounded-lg">
                  <XCircle className="w-6 h-6" />
                </div>
                <div>
                  <p className="text-sm font-medium text-slate-500">Inactive</p>
                  <p className="text-xl font-bold text-slate-900">{stats.inactiveUsers}</p>
                </div>
              </Card>
            </div>
          </section>
          
          <section>
            <h2 className="text-lg font-semibold text-slate-800 mb-4">Platform Activity</h2>
            <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6">
              <Card className="p-6 flex items-center gap-4 border border-slate-200">
                <div className="p-3 bg-teal-100 text-teal-600 rounded-lg">
                  <BarChart className="w-6 h-6" />
                </div>
                <div>
                  <p className="text-sm font-medium text-slate-500">Total Registrations</p>
                  <p className="text-xl font-bold text-slate-900">{stats.totalRegistrations}</p>
                </div>
              </Card>
              
              <Card className="p-6 flex items-center gap-4 border border-slate-200">
                <div className="p-3 bg-orange-100 text-orange-600 rounded-lg">
                  <Users className="w-6 h-6" />
                </div>
                <div>
                  <p className="text-sm font-medium text-slate-500">Total Teams</p>
                  <p className="text-xl font-bold text-slate-900">{stats.totalTeams}</p>
                </div>
              </Card>

              <Card className="p-6 flex items-center gap-4 border border-slate-200">
                <div className="p-3 bg-cyan-100 text-cyan-600 rounded-lg">
                  <BarChart className="w-6 h-6" />
                </div>
                <div>
                  <p className="text-sm font-medium text-slate-500">Total Submissions</p>
                  <p className="text-xl font-bold text-slate-900">{stats.totalSubmissions}</p>
                </div>
              </Card>
            </div>
          </section>
        </div>
      )}
    </div>  );
};

export default AdminDashboard;
