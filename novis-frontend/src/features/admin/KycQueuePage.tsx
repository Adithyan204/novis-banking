import React, { useEffect, useState } from 'react';
import { getPendingKyc, reviewKyc } from '../../api/admin';
import { Table, TableBody, TableCell, TableHead, TableHeader, TableRow } from '../../components/Table';
import { Button } from '../../components/Button';
import { Modal } from '../../components/Modal';
import { Input } from '../../components/Input';
import { Spinner } from '../../components/Spinner';

export const KycQueuePage = () => {
  const [documents, setDocuments] = useState<any[]>([]);
  const [loading, setLoading] = useState(true);
  const [selectedDoc, setSelectedDoc] = useState<number | null>(null);
  const [rejectReason, setRejectReason] = useState('');

  const fetchDocs = async () => {
    try {
      const res = await getPendingKyc();
      setDocuments(res.data.data);
    } catch (err) {
      console.error(err);
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    fetchDocs();
  }, []);

  const handleAction = async (id: number, action: string, reason?: string) => {
    try {
      await reviewKyc(id, { action, rejectionReason: reason });
      setSelectedDoc(null);
      setRejectReason('');
      fetchDocs();
    } catch (err) {
      console.error(err);
    }
  };

  if (loading) return <Spinner />;

  return (
    <div className="space-y-6">
      <h1 className="text-2xl font-bold text-slate-900">KYC Review Queue</h1>
      <Table>
        <TableHeader>
          <TableRow>
            <TableHead>User ID</TableHead>
            <TableHead>Document Type</TableHead>
            <TableHead>Uploaded At</TableHead>
            <TableHead>Actions</TableHead>
          </TableRow>
        </TableHeader>
        <TableBody>
          {documents.map(doc => (
            <TableRow key={doc.id}>
              <TableCell>{doc.userId}</TableCell>
              <TableCell>{doc.documentType}</TableCell>
              <TableCell>{new Date(doc.createdAt).toLocaleString()}</TableCell>
              <TableCell className="space-x-2">
                <Button size="sm" variant="primary" onClick={() => handleAction(doc.id, 'APPROVE')}>Approve</Button>
                <Button size="sm" variant="danger" onClick={() => setSelectedDoc(doc.id)}>Reject</Button>
              </TableCell>
            </TableRow>
          ))}
          {documents.length === 0 && (
            <TableRow>
              <TableCell colSpan={4} className="text-center text-slate-500">No pending documents</TableCell>
            </TableRow>
          )}
        </TableBody>
      </Table>

      <Modal isOpen={!!selectedDoc} onClose={() => setSelectedDoc(null)} title="Reject KYC Document">
        <div className="space-y-4">
          <Input 
            label="Rejection Reason" 
            value={rejectReason} 
            onChange={e => setRejectReason(e.target.value)} 
            placeholder="e.g. Image blurry"
          />
          <div className="flex justify-end space-x-2">
            <Button variant="secondary" onClick={() => setSelectedDoc(null)}>Cancel</Button>
            <Button variant="danger" onClick={() => handleAction(selectedDoc!, 'REJECT', rejectReason)} disabled={!rejectReason}>Reject Document</Button>
          </div>
        </div>
      </Modal>
    </div>
  );
};