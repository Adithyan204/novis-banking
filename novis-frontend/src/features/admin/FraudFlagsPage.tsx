import React, { useEffect, useState } from 'react';
import { getFraudFlags, resolveFlag } from '../../api/admin';
import { Table, TableBody, TableCell, TableHead, TableHeader, TableRow } from '../../components/Table';
import { Button } from '../../components/Button';
import { Badge } from '../../components/Badge';
import { Modal } from '../../components/Modal';
import { Input } from '../../components/Input';
import { Spinner } from '../../components/Spinner';

export const FraudFlagsPage = () => {
  const [flags, setFlags] = useState<any[]>([]);
  const [loading, setLoading] = useState(true);
  const [selectedFlag, setSelectedFlag] = useState<number | null>(null);
  const [actionType, setActionType] = useState<'APPROVE' | 'REJECT' | null>(null);
  const [reviewNote, setReviewNote] = useState('');

  const fetchFlags = async () => {
    try {
      const res = await getFraudFlags();
      setFlags(res.data.data);
    } catch (err) {
      console.error(err);
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    fetchFlags();
  }, []);

  const handleResolve = async () => {
    if (!selectedFlag || !actionType) return;
    try {
      await resolveFlag(selectedFlag, { action: actionType, reviewNote });
      setSelectedFlag(null);
      setReviewNote('');
      fetchFlags();
    } catch (err) {
      console.error(err);
    }
  };

  if (loading) return <Spinner />;

  return (
    <div className="space-y-6">
      <h1 className="text-2xl font-bold text-slate-900">Fraud Flags Review</h1>
      <Table>
        <TableHeader>
          <TableRow>
            <TableHead>Tx ID</TableHead>
            <TableHead>Rule Triggered</TableHead>
            <TableHead>Severity</TableHead>
            <TableHead>Created At</TableHead>
            <TableHead>Actions</TableHead>
          </TableRow>
        </TableHeader>
        <TableBody>
          {flags.map(flag => (
            <TableRow key={flag.id}>
              <TableCell>{flag.transactionId}</TableCell>
              <TableCell className="font-medium">{flag.ruleTriggered}</TableCell>
              <TableCell>
                <Badge variant={
                  flag.severity === 'HIGH' ? 'danger' : 
                  flag.severity === 'MEDIUM' ? 'warning' : 'info'
                }>{flag.severity}</Badge>
              </TableCell>
              <TableCell>{new Date(flag.createdAt).toLocaleString()}</TableCell>
              <TableCell className="space-x-2">
                <Button size="sm" variant="primary" onClick={() => { setSelectedFlag(flag.id); setActionType('APPROVE'); }}>Approve Tx</Button>
                <Button size="sm" variant="danger" onClick={() => { setSelectedFlag(flag.id); setActionType('REJECT'); }}>Reject Tx</Button>
              </TableCell>
            </TableRow>
          ))}
          {flags.length === 0 && (
            <TableRow>
              <TableCell colSpan={5} className="text-center text-slate-500">No pending fraud flags</TableCell>
            </TableRow>
          )}
        </TableBody>
      </Table>

      <Modal isOpen={!!selectedFlag} onClose={() => setSelectedFlag(null)} title={`Confirm ${actionType === 'APPROVE' ? 'Approval' : 'Rejection'}`}>
        <div className="space-y-4">
          <Input 
            label="Review Note (Optional)" 
            value={reviewNote} 
            onChange={e => setReviewNote(e.target.value)} 
          />
          <div className="flex justify-end space-x-2">
            <Button variant="secondary" onClick={() => setSelectedFlag(null)}>Cancel</Button>
            <Button variant={actionType === 'APPROVE' ? 'primary' : 'danger'} onClick={handleResolve}>
              Confirm
            </Button>
          </div>
        </div>
      </Modal>
    </div>
  );
};