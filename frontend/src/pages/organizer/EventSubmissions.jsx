import React, { useEffect, useState } from 'react';
import { useParams, Link, useNavigate } from 'react-router-dom';
import { ArrowLeft, FileCode, CheckCircle, ExternalLink } from 'lucide-react';
import teamService from '../../services/teamService';
import submissionService from '../../services/submissionService';
import Card from '../../components/Card';
import Badge from '../../components/Badge';
import Button from '../../components/Button';

const EventSubmissions = () => {
  const { id } = useParams();
  const navigate = useNavigate();
  
  const [submissions, setSubmissions] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);

  useEffect(() => {
    fetchData();
  }, [id]);

  const fetchData = async () => {
    setLoading(true);
    try {
      // Backend might not have GET /api/events/{eventId}/submissions natively yet.
      // So we fetch teams, then for each team fetch their submission.
      const teams = await teamService.getTeamsByEvent(id);
      
      const allSubs = [];
      for (const team of teams) {
        try {
          const subs = await submissionService.getTeamSubmissions(team.id);
          if (subs.length > 0) {
            allSubs.push({ ...subs[0], teamName: team.name });
          }
        } catch (e) {
          // Ignore if no submission
        }
      }
      setSubmissions(allSubs);
    } catch (err) {
      setError('Failed to load submissions.');
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="space-y-6 max-w-6xl mx-auto">
      <div>
        <Link to="/organizer/events" className="text-sm text-primary-600 hover:underline flex items-center gap-1 mb-4">
          <ArrowLeft className="w-4 h-4" /> Back to Events
        </Link>
        <h1 className="text-3xl font-bold text-slate-900">Event Submissions</h1>
        <p className="mt-2 text-slate-500">Review and evaluate projects submitted by teams.</p>
      </div>

      {error && (
        <Card className="p-6 text-center text-red-600 border border-red-200 bg-red-50">
          <p>{error}</p>
        </Card>
      )}

      {loading ? (
        <div className="p-12 text-center text-slate-500 animate-pulse">Loading submissions...</div>
      ) : submissions.length > 0 ? (
        <div className="grid grid-cols-1 gap-6">
          {submissions.map(sub => (
            <Card key={sub.id} className="p-6 border border-slate-200">
              <div className="flex flex-col md:flex-row justify-between md:items-start mb-6 border-b border-slate-100 pb-4">
                <div>
                  <h3 className="text-xl font-bold text-slate-900 mb-1">{sub.projectTitle}</h3>
                  <p className="text-sm text-slate-500 font-medium">Team: {sub.teamName}</p>
                </div>
                <div className="mt-4 md:mt-0 flex flex-col md:items-end gap-2">
                  <Badge variant={sub.status === 'SUBMITTED' ? 'info' : (sub.status === 'EVALUATED' ? 'success' : 'default')}>
                    {sub.status}
                  </Badge>
                  <p className="text-xs text-slate-400">
                    Submitted: {sub.submittedAt ? new Date(sub.submittedAt).toLocaleDateString() : 'N/A'}
                  </p>
                </div>
              </div>
              
              <div className="grid grid-cols-1 md:grid-cols-3 gap-6 mb-6">
                <div className="md:col-span-2 space-y-4">
                  <div>
                    <h5 className="text-sm font-semibold text-slate-700 mb-1">Description</h5>
                    <p className="text-sm text-slate-600 bg-slate-50 p-3 rounded-md border border-slate-200">
                      {sub.description}
                    </p>
                  </div>
                  {sub.problemStatement && (
                    <div>
                      <h5 className="text-sm font-semibold text-slate-700 mb-1">Problem Statement</h5>
                      <p className="text-sm text-slate-600 bg-slate-50 p-3 rounded-md border border-slate-200">
                        {sub.problemStatement}
                      </p>
                    </div>
                  )}
                </div>
                <div className="space-y-4">
                  <div>
                    <h5 className="text-sm font-semibold text-slate-700 mb-1">Tech Stack</h5>
                    <p className="text-sm text-slate-600 bg-slate-50 p-2 rounded-md border border-slate-200">
                      {sub.technologiesUsed || 'Not specified'}
                    </p>
                  </div>
                  
                  <div className="space-y-2">
                    <h5 className="text-sm font-semibold text-slate-700">Links</h5>
                    {sub.githubUrl && (
                      <a href={sub.githubUrl} target="_blank" rel="noreferrer" className="flex items-center text-sm text-primary-600 hover:underline">
                        <ExternalLink className="w-4 h-4 mr-2" /> GitHub Repo
                      </a>
                    )}
                    {sub.liveDemoUrl && (
                      <a href={sub.liveDemoUrl} target="_blank" rel="noreferrer" className="flex items-center text-sm text-primary-600 hover:underline">
                        <ExternalLink className="w-4 h-4 mr-2" /> Live Demo
                      </a>
                    )}
                    {sub.presentationUrl && (
                      <a href={sub.presentationUrl} target="_blank" rel="noreferrer" className="flex items-center text-sm text-primary-600 hover:underline">
                        <ExternalLink className="w-4 h-4 mr-2" /> Presentation
                      </a>
                    )}
                  </div>
                </div>
              </div>
              
              <div className="flex justify-end pt-4 border-t border-slate-100">
                {sub.status !== 'EVALUATED' ? (
                  <Button onClick={() => navigate(`/organizer/events/${id}/evaluate`, { state: { submission: sub }})}>
                    Evaluate Project
                  </Button>
                ) : (
                  <Button variant="secondary" disabled className="text-green-700 bg-green-50 border-green-200 opacity-100 cursor-default flex items-center gap-2">
                    <CheckCircle className="w-4 h-4" /> Evaluated
                  </Button>
                )}
              </div>
            </Card>
          ))}
        </div>
      ) : (
        <Card className="p-12 text-center border border-slate-200">
          <FileCode className="mx-auto h-12 w-12 text-slate-300 mb-4" />
          <h3 className="text-lg font-medium text-slate-900">No submissions yet</h3>
          <p className="mt-1 text-slate-500">Teams haven't submitted their projects for this event.</p>
        </Card>
      )}
    </div>
  );
};

export default EventSubmissions;
