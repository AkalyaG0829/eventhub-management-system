import React, { useEffect, useState } from 'react';
import { useParams, Link } from 'react-router-dom';
import { Trophy, Medal, ArrowLeft } from 'lucide-react';
import leaderboardService from '../../services/leaderboardService';
import Card from '../../components/Card';
import Badge from '../../components/Badge';

const Leaderboard = () => {
  const { eventId } = useParams();
  const [leaderboard, setLeaderboard] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);

  useEffect(() => {
    const fetchLeaderboard = async () => {
      try {
        const data = await leaderboardService.getLeaderboard(eventId);
        setLeaderboard(data);
      } catch (err) {
        if (err.response?.status === 403) {
          setError("Results for this event have not been published yet.");
        } else {
          setError("Failed to load leaderboard.");
        }
      } finally {
        setLoading(false);
      }
    };
    fetchLeaderboard();
  }, [eventId]);

  return (
    <div className="space-y-6 max-w-4xl mx-auto">
      <div>
        <Link to="/events" className="text-sm text-primary-600 hover:underline flex items-center gap-1 mb-4">
          <ArrowLeft className="w-4 h-4" /> Back to Events
        </Link>
        <h1 className="text-3xl font-bold text-slate-900 flex items-center gap-3">
          <Trophy className="w-8 h-8 text-yellow-500" /> Event Leaderboard
        </h1>
        <p className="mt-2 text-slate-500">Official results and rankings.</p>
      </div>

      {error ? (
        <Card className="p-12 text-center border border-slate-200">
          <Trophy className="mx-auto h-12 w-12 text-slate-300 mb-4" />
          <h3 className="text-lg font-medium text-slate-900">Leaderboard Unavailable</h3>
          <p className="mt-1 text-slate-500">{error}</p>
        </Card>
      ) : loading ? (
        <div className="p-12 text-center text-slate-500 animate-pulse">Loading rankings...</div>
      ) : leaderboard.length > 0 ? (
        <Card className="overflow-hidden border border-slate-200">
          <div className="overflow-x-auto">
            <table className="min-w-full divide-y divide-slate-200">
              <thead className="bg-slate-50">
                <tr>
                  <th scope="col" className="px-6 py-3 text-left text-xs font-medium text-slate-500 uppercase tracking-wider">Rank</th>
                  <th scope="col" className="px-6 py-3 text-left text-xs font-medium text-slate-500 uppercase tracking-wider">Project / Team</th>
                  <th scope="col" className="px-6 py-3 text-left text-xs font-medium text-slate-500 uppercase tracking-wider">Score</th>
                  <th scope="col" className="px-6 py-3 text-left text-xs font-medium text-slate-500 uppercase tracking-wider">Status</th>
                </tr>
              </thead>
              <tbody className="bg-white divide-y divide-slate-200">
                {leaderboard.map((entry, index) => (
                  <tr key={entry.submissionId || index} className={index < 3 ? 'bg-yellow-50' : ''}>
                    <td className="px-6 py-4 whitespace-nowrap">
                      <div className="flex items-center">
                        {index === 0 && <Medal className="w-6 h-6 text-yellow-500 mr-2" />}
                        {index === 1 && <Medal className="w-6 h-6 text-slate-400 mr-2" />}
                        {index === 2 && <Medal className="w-6 h-6 text-amber-600 mr-2" />}
                        <span className={`text-lg font-bold ${index < 3 ? 'text-slate-900' : 'text-slate-600 ml-8'}`}>
                          #{entry.rank}
                        </span>
                      </div>
                    </td>
                    <td className="px-6 py-4">
                      <div className="text-sm font-bold text-slate-900">{entry.projectTitle}</div>
                      <div className="text-sm text-slate-500">Team: {entry.teamName}</div>
                    </td>
                    <td className="px-6 py-4 whitespace-nowrap">
                      <span className="text-xl font-bold text-primary-700">
                        {entry.totalScore.toFixed(1)} <span className="text-sm text-slate-500 font-normal">/ 100</span>
                      </span>
                    </td>
                    <td className="px-6 py-4 whitespace-nowrap">
                      <Badge variant="success">WINNER</Badge>
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        </Card>
      ) : (
        <Card className="p-12 text-center border border-slate-200">
          <Trophy className="mx-auto h-12 w-12 text-slate-300 mb-4" />
          <p className="text-slate-500">No evaluated submissions found for this event.</p>
        </Card>
      )}
    </div>
  );
};

export default Leaderboard;
