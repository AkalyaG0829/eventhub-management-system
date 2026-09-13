import React, { useState } from 'react';
import { useLocation, useNavigate, useParams, Link } from 'react-router-dom';
import { ArrowLeft } from 'lucide-react';
import evaluationService from '../../services/evaluationService';
import Card from '../../components/Card';
import Input from '../../components/Input';
import Button from '../../components/Button';

const EvaluateSubmission = () => {
  const { id } = useParams(); // eventId
  const location = useLocation();
  const navigate = useNavigate();
  const submission = location.state?.submission;

  const [formData, setFormData] = useState({
    innovationScore: '',
    technicalScore: '',
    uiUxScore: '',
    impactScore: '',
    presentationScore: '',
    comments: '',
    strengths: '',
    weaknesses: '',
    recommendations: ''
  });

  const [loading, setLoading] = useState(false);
  const [error, setError] = useState(null);

  if (!submission) {
    return (
      <div className="text-center py-20 text-slate-500">
        No submission selected. <Link to={`/organizer/events/${id}/submissions`} className="text-primary-600 hover:underline">Go back.</Link>
      </div>
    );
  }

  const handleChange = (e) => {
    const { name, value } = e.target;
    setFormData(prev => ({ ...prev, [name]: value }));
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    setLoading(true);
    setError(null);
    try {
      // Convert scores to numbers
      const payload = { ...formData };
      ['innovationScore', 'technicalScore', 'uiUxScore', 'impactScore', 'presentationScore'].forEach(key => {
        if (payload[key]) payload[key] = parseFloat(payload[key]);
      });
      
      await evaluationService.evaluateSubmission(submission.id, payload);
      alert("Evaluation saved successfully!");
      navigate(`/organizer/events/${id}/submissions`);
    } catch (err) {
      setError(err.response?.data?.message || 'Failed to submit evaluation.');
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="space-y-6 max-w-4xl mx-auto">
      <div>
        <Link to={`/organizer/events/${id}/submissions`} className="text-sm text-primary-600 hover:underline flex items-center gap-1 mb-4">
          <ArrowLeft className="w-4 h-4" /> Back to Submissions
        </Link>
        <h1 className="text-3xl font-bold text-slate-900">Evaluate Project</h1>
        <p className="mt-2 text-slate-500">Project: {submission.projectTitle} (Team: {submission.teamName})</p>
      </div>

      {error && (
        <div className="bg-red-50 border-l-4 border-red-400 p-4">
          <p className="text-sm text-red-700">{error}</p>
        </div>
      )}

      <Card className="p-6 border border-slate-200">
        <form onSubmit={handleSubmit} className="space-y-6">
          <div>
            <h3 className="text-lg font-bold text-slate-900 mb-4 border-b border-slate-200 pb-2">Scoring (0-20 each)</h3>
            <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6">
              <Input 
                label="Innovation Score" name="innovationScore" type="number" step="0.5" min="0" max="20" 
                required value={formData.innovationScore} onChange={handleChange} 
              />
              <Input 
                label="Technical Score" name="technicalScore" type="number" step="0.5" min="0" max="20" 
                required value={formData.technicalScore} onChange={handleChange} 
              />
              <Input 
                label="UI/UX Score" name="uiUxScore" type="number" step="0.5" min="0" max="20" 
                required value={formData.uiUxScore} onChange={handleChange} 
              />
              <Input 
                label="Impact Score" name="impactScore" type="number" step="0.5" min="0" max="20" 
                required value={formData.impactScore} onChange={handleChange} 
              />
              <Input 
                label="Presentation Score" name="presentationScore" type="number" step="0.5" min="0" max="20" 
                required value={formData.presentationScore} onChange={handleChange} 
              />
            </div>
            <p className="text-xs text-slate-500 mt-2 text-right italic">Note: The system will calculate the final total score automatically.</p>
          </div>

          <div>
            <h3 className="text-lg font-bold text-slate-900 mb-4 border-b border-slate-200 pb-2">Feedback</h3>
            <div className="space-y-4">
              <div>
                <label className="block text-sm font-medium text-slate-700 mb-1">General Comments</label>
                <textarea 
                  name="comments" rows="3" required
                  className="w-full px-3 py-2 border border-slate-300 rounded-md focus:ring-primary-500 focus:border-primary-500 sm:text-sm"
                  value={formData.comments} onChange={handleChange}
                ></textarea>
              </div>
              
              <div>
                <label className="block text-sm font-medium text-slate-700 mb-1">Strengths</label>
                <textarea 
                  name="strengths" rows="2" 
                  className="w-full px-3 py-2 border border-slate-300 rounded-md focus:ring-primary-500 focus:border-primary-500 sm:text-sm"
                  value={formData.strengths} onChange={handleChange}
                ></textarea>
              </div>
              
              <div>
                <label className="block text-sm font-medium text-slate-700 mb-1">Weaknesses</label>
                <textarea 
                  name="weaknesses" rows="2" 
                  className="w-full px-3 py-2 border border-slate-300 rounded-md focus:ring-primary-500 focus:border-primary-500 sm:text-sm"
                  value={formData.weaknesses} onChange={handleChange}
                ></textarea>
              </div>
              
              <div>
                <label className="block text-sm font-medium text-slate-700 mb-1">Recommendations for Improvement</label>
                <textarea 
                  name="recommendations" rows="2" 
                  className="w-full px-3 py-2 border border-slate-300 rounded-md focus:ring-primary-500 focus:border-primary-500 sm:text-sm"
                  value={formData.recommendations} onChange={handleChange}
                ></textarea>
              </div>
            </div>
          </div>

          <div className="flex justify-end gap-4 pt-4 border-t border-slate-200">
            <Link to={`/organizer/events/${id}/submissions`}>
              <Button type="button" variant="ghost">Cancel</Button>
            </Link>
            <Button type="submit" disabled={loading}>
              {loading ? 'Submitting...' : 'Submit Evaluation'}
            </Button>
          </div>
        </form>
      </Card>
    </div>
  );
};

export default EvaluateSubmission;
