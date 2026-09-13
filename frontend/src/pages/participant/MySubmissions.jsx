import React, { useEffect, useState } from 'react';
import { useAuth } from '../../context/AuthContext';
import teamService from '../../services/teamService';
import submissionService from '../../services/submissionService';
import Card from '../../components/Card';
import Badge from '../../components/Badge';
import Button from '../../components/Button';
import Input from '../../components/Input';
import Modal from '../../components/Modal';
import { FileCode, ExternalLink, Edit } from 'lucide-react';

const MySubmissions = () => {
  const { user } = useAuth();
  const [teams, setTeams] = useState([]);
  const [submissionsByTeam, setSubmissionsByTeam] = useState({});
  const [loading, setLoading] = useState(true);

  // Form State
  const [isModalOpen, setIsModalOpen] = useState(false);
  const [activeTeam, setActiveTeam] = useState(null);
  const [isEditing, setIsEditing] = useState(false);
  const [submitting, setSubmitting] = useState(false);
  const [actionError, setActionError] = useState(null);
  
  const [formData, setFormData] = useState({
    id: null,
    projectTitle: '',
    description: '',
    problemStatement: '',
    solution: '',
    technologiesUsed: '',
    githubUrl: '',
    liveDemoUrl: '',
    presentationUrl: '',
    documentationUrl: ''
  });

  useEffect(() => {
    fetchData();
  }, []);

  const fetchData = async () => {
    setLoading(true);
    try {
      // Find all teams the user is in across all events.
      // This is a bit tricky if there is no GET /api/teams/my endpoint, 
      // but we can look through registrations -> events -> teams, same as MyTeams.
      // Let's assume for simplicity we fetch from registrationService again.
      const { default: registrationService } = await import('../../services/registrationService');
      const regs = await registrationService.getMyRegistrations();
      const approvedRegs = regs.filter(r => r.status === 'APPROVED');
      
      const userTeams = [];
      const subMap = {};
      
      const { default: teamService } = await import('../../services/teamService');
      const myTeams = await teamService.getMyTeams();
      
      for (const myTeam of myTeams) {
        // Attach event title for UI (find from approvedRegs)
        const matchingReg = approvedRegs.find(r => r.eventId === myTeam.eventId);
        if (matchingReg) {
          myTeam.eventTitle = matchingReg.eventTitle;
        } else {
          myTeam.eventTitle = `Event ${myTeam.eventId}`;
        }
        userTeams.push(myTeam);
        
        try {
          const subs = await submissionService.getTeamSubmissions(myTeam.id);
          if (subs.length > 0) {
            subMap[myTeam.id] = subs[0]; // Assume 1 submission per team
          }
        } catch (e) {
          // Ignore 404s if no submission exists
        }
      }
      setTeams(userTeams);
      setSubmissionsByTeam(subMap);
    } catch (err) {
      console.error(err);
    } finally {
      setLoading(false);
    }
  };

  const handleOpenForm = (team, existingSub = null) => {
    setActiveTeam(team);
    setActionError(null);
    if (existingSub) {
      setIsEditing(true);
      setFormData({
        id: existingSub.id,
        projectTitle: existingSub.projectTitle || '',
        description: existingSub.description || '',
        problemStatement: existingSub.problemStatement || '',
        solution: existingSub.solution || '',
        technologiesUsed: existingSub.technologiesUsed || '',
        githubUrl: existingSub.githubUrl || '',
        liveDemoUrl: existingSub.liveDemoUrl || '',
        presentationUrl: existingSub.presentationUrl || '',
        documentationUrl: existingSub.documentationUrl || ''
      });
    } else {
      setIsEditing(false);
      setFormData({
        id: null, projectTitle: '', description: '', problemStatement: '', solution: '',
        technologiesUsed: '', githubUrl: '', liveDemoUrl: '', presentationUrl: '', documentationUrl: ''
      });
    }
    setIsModalOpen(true);
  };

  const handleChange = (e) => {
    const { name, value } = e.target;
    setFormData(prev => ({ ...prev, [name]: value }));
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    setSubmitting(true);
    setActionError(null);
    try {
      if (isEditing) {
        await submissionService.updateSubmission(formData.id, formData);
      } else {
        await submissionService.createSubmission(activeTeam.id, formData);
      }
      setIsModalOpen(false);
      fetchData(); // Reload
    } catch (err) {
      setActionError(err.response?.data?.message || 'Failed to save submission.');
    } finally {
      setSubmitting(false);
    }
  };

  return (
    <div className="space-y-6">
      <div>
        <h1 className="text-3xl font-bold text-slate-900">My Submissions</h1>
        <p className="mt-2 text-slate-500">Manage your project submissions for hackathons and events.</p>
      </div>

      {loading ? (
        <div className="p-12 text-center text-slate-500 animate-pulse">Loading submissions...</div>
      ) : teams.length === 0 ? (
        <Card className="p-12 text-center">
          <FileCode className="mx-auto h-12 w-12 text-slate-300 mb-4" />
          <h3 className="text-lg font-medium text-slate-900">No teams found</h3>
          <p className="mt-1 text-slate-500">You must be part of a team to make a submission.</p>
        </Card>
      ) : (
        teams.map(team => {
          const submission = submissionsByTeam[team.id];
          const isLeader = team.leader.id === user.id;

          return (
            <Card key={team.id} className="p-6 border border-slate-200">
              <div className="flex flex-col sm:flex-row justify-between sm:items-start mb-6">
                <div>
                  <h3 className="text-sm font-medium text-primary-600 mb-1">{team.eventTitle}</h3>
                  <h4 className="text-xl font-bold text-slate-900 flex items-center gap-2">
                    Team: {team.name}
                  </h4>
                </div>
                {!submission && isLeader && (
                  <Button onClick={() => handleOpenForm(team)} className="mt-4 sm:mt-0">
                    Create Submission
                  </Button>
                )}
                {!submission && !isLeader && (
                  <Badge variant="warning" className="mt-4 sm:mt-0">Awaiting Submission</Badge>
                )}
              </div>

              {submission ? (
                <div className="bg-slate-50 rounded-lg p-5 border border-slate-200">
                  <div className="flex justify-between items-start mb-4 border-b border-slate-200 pb-4">
                    <div>
                      <h5 className="text-lg font-bold text-slate-900">{submission.projectTitle}</h5>
                      <div className="flex gap-2 mt-2">
                        <Badge variant={submission.status === 'SUBMITTED' ? 'info' : (submission.status === 'EVALUATED' ? 'success' : 'default')}>
                          {submission.status}
                        </Badge>
                      </div>
                    </div>
                    {isLeader && submission.status !== 'EVALUATED' && (
                      <Button variant="secondary" onClick={() => handleOpenForm(team, submission)}>
                        <Edit className="w-4 h-4 mr-2" /> Edit
                      </Button>
                    )}
                  </div>
                  
                  <div className="prose max-w-none text-sm text-slate-600 mb-4">
                    <p><strong>Description:</strong> {submission.description}</p>
                    <p><strong>Tech Stack:</strong> {submission.technologiesUsed}</p>
                  </div>
                  
                  <div className="flex flex-wrap gap-3">
                    {submission.githubUrl && (
                      <a href={submission.githubUrl} target="_blank" rel="noreferrer" className="inline-flex items-center text-sm text-primary-600 hover:underline">
                        <ExternalLink className="w-4 h-4 mr-1" /> GitHub
                      </a>
                    )}
                    {submission.liveDemoUrl && (
                      <a href={submission.liveDemoUrl} target="_blank" rel="noreferrer" className="inline-flex items-center text-sm text-primary-600 hover:underline">
                        <ExternalLink className="w-4 h-4 mr-1" /> Live Demo
                      </a>
                    )}
                  </div>
                </div>
              ) : (
                <div className="text-center py-8 text-slate-500 bg-slate-50 rounded-lg border border-dashed border-slate-300">
                  No project has been submitted for this team yet.
                  {isLeader && <p className="text-sm mt-1">As the team leader, you can create a submission.</p>}
                </div>
              )}
            </Card>
          );
        })
      )}

      {/* Submission Modal */}
      <Modal 
        isOpen={isModalOpen} 
        onClose={() => setIsModalOpen(false)} 
        title={isEditing ? 'Edit Submission' : 'Create Submission'}
      >
        <form onSubmit={handleSubmit} className="space-y-4">
          {actionError && <div className="text-red-600 text-sm bg-red-50 p-2 rounded">{actionError}</div>}
          
          <Input label="Project Title" name="projectTitle" required value={formData.projectTitle} onChange={handleChange} />
          
          <div>
            <label className="block text-sm font-medium text-slate-700 mb-1">Description</label>
            <textarea 
              name="description" required rows="3" 
              className="w-full px-3 py-2 border border-slate-300 rounded-md focus:ring-primary-500 focus:border-primary-500 sm:text-sm"
              value={formData.description} onChange={handleChange}
            ></textarea>
          </div>
          
          <div>
            <label className="block text-sm font-medium text-slate-700 mb-1">Problem Statement</label>
            <textarea 
              name="problemStatement" rows="2" 
              className="w-full px-3 py-2 border border-slate-300 rounded-md focus:ring-primary-500 focus:border-primary-500 sm:text-sm"
              value={formData.problemStatement} onChange={handleChange}
            ></textarea>
          </div>
          
          <Input label="Technologies Used (comma separated)" name="technologiesUsed" value={formData.technologiesUsed} onChange={handleChange} />
          <Input label="GitHub URL" name="githubUrl" type="url" value={formData.githubUrl} onChange={handleChange} placeholder="https://github.com/..." />
          <Input label="Live Demo URL" name="liveDemoUrl" type="url" value={formData.liveDemoUrl} onChange={handleChange} />
          <Input label="Presentation URL" name="presentationUrl" type="url" value={formData.presentationUrl} onChange={handleChange} />

          <div className="flex justify-end gap-2 pt-4">
            <Button type="button" variant="ghost" onClick={() => setIsModalOpen(false)}>Cancel</Button>
            <Button type="submit" disabled={submitting}>{submitting ? 'Saving...' : 'Submit Project'}</Button>
          </div>
        </form>
      </Modal>
    </div>
  );
};

export default MySubmissions;
