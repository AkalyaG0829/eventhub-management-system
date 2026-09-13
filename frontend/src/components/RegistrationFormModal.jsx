import React, { useState, useEffect } from 'react';
import Modal from './Modal';
import Button from './Button';
import Input from './Input';
import { useAuth } from '../context/AuthContext';
import registrationService from '../services/registrationService';
import { useNavigate } from 'react-router-dom';

const RegistrationFormModal = ({ isOpen, onClose, event, onRegisterSuccess }) => {
  const { user } = useAuth();
  const navigate = useNavigate();
  const [registering, setRegistering] = useState(false);
  const [error, setError] = useState(null);
  const [isDuplicate, setIsDuplicate] = useState(false);

  const isTeam = event?.participationType === 'TEAM';
  const minSize = event?.teamSizeMin || 1;
  const maxSize = event?.teamSizeMax || 1;

  const [teamSize, setTeamSize] = useState(minSize);
  const [teamName, setTeamName] = useState('');
  const [members, setMembers] = useState([]);

  useEffect(() => {
    if (isOpen && user) {
      // Initialize with Leader (current user)
      const initialMembers = [
        { name: user.name || '', email: user.email || '', phone: user.phone || '', isLeader: true }
      ];
      
      // Fill the rest with empty strings
      for (let i = 1; i < teamSize; i++) {
        initialMembers.push({ name: '', email: '', phone: '', isLeader: false });
      }
      setMembers(initialMembers);
      
      // Also track participant phone for individual registration if missing
      setParticipantPhone(user.phone || '');
      setError(null);
      setIsDuplicate(false);
    }
  }, [isOpen, user, teamSize]);

  const [participantPhone, setParticipantPhone] = useState('');

  const handleMemberChange = (index, field, value) => {
    const updated = [...members];
    updated[index][field] = value;
    setMembers(updated);
  };

  const handleSizeChange = (e) => {
    const size = parseInt(e.target.value);
    setTeamSize(size);
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    setRegistering(true);
    setError(null);

    try {
      if (isTeam) {
        if (!teamName) throw new Error("Team Name is required");
        for (let i = 0; i < members.length; i++) {
          const m = members[i];
          if (!m.name || !m.email || !m.phone) {
            throw new Error(`Please fill all details for Participant ${i + 1}`);
          }
        }
        
        const payload = {
          teamName,
          teamSize,
          members: members.map(m => ({
            name: m.name,
            email: m.email,
            phone: m.phone
          }))
        };
        await registrationService.registerForEvent(event.id, payload);
      } else {
        const payload = { participantPhone };
        if (!user.phone && !participantPhone) {
            throw new Error("Phone number is required");
        }
        await registrationService.registerForEvent(event.id, payload);
      }
      
      onRegisterSuccess();
    } catch (err) {
      const errMsg = err.response?.data?.message || err.message || "Registration failed.";
      if (errMsg.toLowerCase().includes("already registered")) {
        setIsDuplicate(true);
      } else {
        setError(errMsg);
      }
    } finally {
      setRegistering(false);
    }
  };

  if (!event || !user) return null;

  return (
    <Modal isOpen={isOpen} onClose={onClose} title={`Register for ${event.title}`}>
      {isDuplicate ? (
        <div className="text-center py-6">
          <div className="w-16 h-16 bg-blue-100 text-blue-600 rounded-full flex items-center justify-center mx-auto mb-4">
            <svg className="w-8 h-8" fill="none" stroke="currentColor" viewBox="0 0 24 24" xmlns="http://www.w3.org/2000/svg">
              <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M13 16h-1v-4h-1m1-4h.01M21 12a9 9 0 11-18 0 9 9 0 0118 0z" />
            </svg>
          </div>
          <h3 className="text-lg font-medium text-slate-900 mb-2">You are already registered for this event.</h3>
          <p className="text-slate-500 mb-6">You cannot register for the same event more than once.</p>
          <div className="flex justify-center gap-3">
            <Button variant="secondary" onClick={onClose} type="button">Close</Button>
            <Button onClick={() => { onClose(); navigate('/my-registrations'); }} type="button">
              View My Registrations
            </Button>
          </div>
        </div>
      ) : (
        <form onSubmit={handleSubmit} className="space-y-4">
          {error && <div className="bg-red-50 text-red-700 p-3 rounded text-sm">{error}</div>}

          {isTeam ? (
          <>
            <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
              <div>
                <label className="block text-sm font-medium text-slate-700 mb-1">Team Size</label>
                <select 
                  className="w-full border-slate-300 rounded-md shadow-sm focus:border-primary-500 focus:ring-primary-500 p-2 border"
                  value={teamSize}
                  onChange={handleSizeChange}
                  disabled={registering}
                >
                  {Array.from({ length: maxSize - minSize + 1 }, (_, i) => minSize + i).map(size => (
                    <option key={size} value={size}>{size}</option>
                  ))}
                </select>
              </div>
              <Input
                label="Team Name"
                required
                value={teamName}
                onChange={(e) => setTeamName(e.target.value)}
                disabled={registering}
              />
            </div>

            <div className="space-y-6 mt-4 border-t pt-4">
              {members.map((member, idx) => (
                <div key={idx} className="bg-slate-50 p-4 rounded-md border border-slate-200 space-y-3">
                  <h4 className="font-semibold text-slate-800">
                    {idx === 0 ? 'Participant 1 (Team Leader)' : `Participant ${idx + 1}`}
                  </h4>
                  <div className="grid grid-cols-1 md:grid-cols-3 gap-3">
                    <Input
                      label="Full Name"
                      required
                      value={member.name}
                      onChange={(e) => handleMemberChange(idx, 'name', e.target.value)}
                      disabled={registering || member.isLeader}
                    />
                    <Input
                      label="Email"
                      type="email"
                      required
                      value={member.email}
                      onChange={(e) => handleMemberChange(idx, 'email', e.target.value)}
                      disabled={registering || member.isLeader}
                    />
                    <Input
                      label="Phone Number"
                      required
                      value={member.phone}
                      onChange={(e) => handleMemberChange(idx, 'phone', e.target.value)}
                      disabled={registering || (member.isLeader && !!user.phone)}
                    />
                  </div>
                  {member.isLeader && !!user.phone && (
                    <p className="text-xs text-slate-500">Leader details are automatically populated from your account.</p>
                  )}
                  {member.isLeader && !user.phone && (
                    <p className="text-xs text-orange-500">Please provide your phone number to complete registration.</p>
                  )}
                </div>
              ))}
            </div>
          </>
        ) : (
          <div className="space-y-4">
            <p className="text-slate-600">Please confirm your details for registration:</p>
            <div className="bg-slate-50 p-4 rounded-md border border-slate-200 grid grid-cols-1 md:grid-cols-3 gap-3">
              <Input label="Full Name" value={user.name} disabled />
              <Input label="Email" value={user.email} disabled />
              <Input 
                label="Phone Number" 
                required
                value={participantPhone} 
                onChange={(e) => setParticipantPhone(e.target.value)}
                disabled={registering || !!user.phone} 
              />
            </div>
            {!user.phone && (
              <p className="text-xs text-orange-500">Please provide your phone number to complete registration.</p>
            )}
          </div>
        )}

        <div className="flex justify-end gap-3 pt-4">
          <Button variant="secondary" onClick={onClose} disabled={registering} type="button">Cancel</Button>
          <Button type="submit" disabled={registering}>
            {registering ? 'Processing...' : 'Confirm Registration'}
          </Button>
        </div>
      </form>
      )}
    </Modal>
  );
};

export default RegistrationFormModal;
