import React, { useEffect, useState } from 'react';
import { useAuth } from '../../context/AuthContext';
import registrationService from '../../services/registrationService';
import teamService from '../../services/teamService';
import Card from '../../components/Card';
import Badge from '../../components/Badge';
import Button from '../../components/Button';
import Input from '../../components/Input';
import Modal from '../../components/Modal';
import { Users, UserPlus, LogOut } from 'lucide-react';

const MyTeams = () => {
  const { user } = useAuth();
  
  const [registrations, setRegistrations] = useState([]);
  const [teamsByEvent, setTeamsByEvent] = useState({});
  const [loading, setLoading] = useState(true);
  
  // Modals state
  const [isCreateModalOpen, setIsCreateModalOpen] = useState(false);
  const [isJoinModalOpen, setIsJoinModalOpen] = useState(false);
  const [selectedEventId, setSelectedEventId] = useState(null);
  
  const [teamName, setTeamName] = useState('');
  const [joinTeamId, setJoinTeamId] = useState('');
  const [actionError, setActionError] = useState(null);

  useEffect(() => {
    fetchData();
  }, []);

  const fetchData = async () => {
    setLoading(true);
    try {
      const regs = await registrationService.getMyRegistrations();
      const approvedRegs = regs.filter(r => r.status === 'APPROVED');
      setRegistrations(approvedRegs);
      
      const myTeams = await teamService.getMyTeams();
      const teamMap = {};
      for (const team of myTeams) {
        teamMap[team.eventId] = team;
      }
      setTeamsByEvent(teamMap);
    } catch (err) {
      console.error(err);
    } finally {
      setLoading(false);
    }
  };

  const handleCreateTeam = async (e) => {
    e.preventDefault();
    setActionError(null);
    try {
      await teamService.createTeam(selectedEventId, { name: teamName });
      setIsCreateModalOpen(false);
      setTeamName('');
      fetchData(); // Reload
    } catch (err) {
      setActionError(err.response?.data?.message || 'Failed to create team');
    }
  };

  const handleJoinTeam = async (e) => {
    e.preventDefault();
    setActionError(null);
    try {
      await teamService.joinTeam(joinTeamId);
      setIsJoinModalOpen(false);
      setJoinTeamId('');
      fetchData(); // Reload
    } catch (err) {
      setActionError(err.response?.data?.message || 'Failed to join team');
    }
  };

  const handleLeaveTeam = async (teamId) => {
    if(window.confirm('Are you sure you want to leave this team?')) {
      try {
        await teamService.leaveTeam(teamId);
        fetchData();
      } catch (err) {
        alert(err.response?.data?.message || 'Failed to leave team');
      }
    }
  };

  const handleRemoveMember = async (teamId, memberId) => {
    if(window.confirm('Remove this member?')) {
      try {
        await teamService.removeMember(teamId, memberId);
        fetchData();
      } catch (err) {
        alert(err.response?.data?.message || 'Failed to remove member');
      }
    }
  };

  return (
    <div className="space-y-6">
      <div>
        <h1 className="text-3xl font-bold text-slate-900">My Teams</h1>
        <p className="mt-2 text-slate-500">Manage your teams for approved event registrations.</p>
      </div>

      {loading ? (
        <div className="p-12 text-center text-slate-500 animate-pulse">Loading teams...</div>
      ) : registrations.length === 0 ? (
        <Card className="p-12 text-center">
          <Users className="mx-auto h-12 w-12 text-slate-300 mb-4" />
          <h3 className="text-lg font-medium text-slate-900">No approved events</h3>
          <p className="mt-1 text-slate-500">You need an approved registration to form a team.</p>
        </Card>
      ) : (
        registrations.map(reg => {
          const team = teamsByEvent[reg.eventId];
          return (
            <Card key={reg.eventId} className="p-6 border border-slate-200">
              <div className="flex flex-col sm:flex-row justify-between sm:items-center mb-6 border-b border-slate-100 pb-4">
                <div>
                  <h3 className="text-lg font-bold text-slate-900">{reg.eventTitle}</h3>
                  <p className="text-sm text-slate-500">Event ID: {reg.eventId}</p>
                </div>
                {!team && (
                  <div className="mt-4 sm:mt-0 flex gap-2">
                    <Button variant="secondary" onClick={() => {
                      setSelectedEventId(reg.eventId);
                      setActionError(null);
                      setIsJoinModalOpen(true);
                    }}>
                      <UserPlus className="w-4 h-4 mr-2" /> Join Team
                    </Button>
                    <Button onClick={() => {
                      setSelectedEventId(reg.eventId);
                      setActionError(null);
                      setIsCreateModalOpen(true);
                    }}>
                      <Users className="w-4 h-4 mr-2" /> Create Team
                    </Button>
                  </div>
                )}
              </div>

              {team ? (
                <div>
                  <div className="flex justify-between items-start mb-4">
                    <div>
                      <h4 className="text-xl font-bold text-primary-700 flex items-center gap-2">
                        {team.name} <Badge variant="info">ID: {team.id}</Badge>
                      </h4>
                      <p className="text-sm text-slate-500 mt-1">Leader: {team.leader.name}</p>
                    </div>
                    {team.leader.id !== user.id && (
                      <Button variant="danger" onClick={() => handleLeaveTeam(team.id)}>
                        <LogOut className="w-4 h-4 mr-2" /> Leave
                      </Button>
                    )}
                  </div>
                  
                  <div className="bg-slate-50 rounded-lg p-4 border border-slate-200">
                    <h5 className="text-sm font-semibold text-slate-700 mb-3 uppercase tracking-wider">Members ({team.members?.length || 0})</h5>
                    <ul className="divide-y divide-slate-200">
                      {team.members?.map(member => (
                        <li key={member.id} className="py-2 flex justify-between items-center">
                          <span className="text-sm font-medium text-slate-900">{member.name} {member.id === team.leader.id ? '(Leader)' : ''}</span>
                          {team.leader.id === user.id && member.id !== user.id && (
                            <button 
                              onClick={() => handleRemoveMember(team.id, member.id)}
                              className="text-xs text-red-600 hover:text-red-800"
                            >
                              Remove
                            </button>
                          )}
                        </li>
                      ))}
                    </ul>
                  </div>
                </div>
              ) : (
                <div className="text-center py-6 text-slate-500 bg-slate-50 rounded-lg border border-dashed border-slate-300">
                  You are not in a team for this event.
                </div>
              )}
            </Card>
          );
        })
      )}

      {/* Create Team Modal */}
      <Modal 
        isOpen={isCreateModalOpen} 
        onClose={() => setIsCreateModalOpen(false)} 
        title="Create a Team"
      >
        <form onSubmit={handleCreateTeam} className="space-y-4">
          {actionError && <div className="text-red-600 text-sm bg-red-50 p-2 rounded">{actionError}</div>}
          <Input 
            label="Team Name" 
            required 
            value={teamName} 
            onChange={(e) => setTeamName(e.target.value)} 
          />
          <div className="flex justify-end gap-2 pt-4">
            <Button type="button" variant="ghost" onClick={() => setIsCreateModalOpen(false)}>Cancel</Button>
            <Button type="submit">Create</Button>
          </div>
        </form>
      </Modal>

      {/* Join Team Modal */}
      <Modal 
        isOpen={isJoinModalOpen} 
        onClose={() => setIsJoinModalOpen(false)} 
        title="Join a Team"
      >
        <form onSubmit={handleJoinTeam} className="space-y-4">
          {actionError && <div className="text-red-600 text-sm bg-red-50 p-2 rounded">{actionError}</div>}
          <Input 
            label="Team ID" 
            type="number"
            required 
            value={joinTeamId} 
            onChange={(e) => setJoinTeamId(e.target.value)} 
            placeholder="Enter the Team ID to join"
          />
          <div className="flex justify-end gap-2 pt-4">
            <Button type="button" variant="ghost" onClick={() => setIsJoinModalOpen(false)}>Cancel</Button>
            <Button type="submit">Join</Button>
          </div>
        </form>
      </Modal>
    </div>
  );
};

export default MyTeams;
