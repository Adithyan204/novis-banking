import React, { useEffect, useState } from 'react';
import { getKycStatus, uploadDocument } from '../../api/kyc';
import { KycStatus } from '../../types';
import { Button } from '../../components/Button';
import { Badge } from '../../components/Badge';
import { Alert } from '../../components/Alert';
import { Spinner } from '../../components/Spinner';

export const KycPage = () => {
  const [status, setStatus] = useState<KycStatus | null>(null);
  const [loading, setLoading] = useState(true);
  const [uploading, setUploading] = useState(false);
  const [docType, setDocType] = useState('PASSPORT');
  const [file, setFile] = useState<File | null>(null);
  const [error, setError] = useState('');

  const fetchStatus = async () => {
    try {
      const res = await getKycStatus();
      setStatus(res.data.data);
    } catch (err) {
      console.error(err);
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    fetchStatus();
  }, []);

  const handleUpload = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!file) return;
    setUploading(true);
    setError('');
    try {
      await uploadDocument(docType, file);
      setFile(null);
      await fetchStatus();
    } catch (err: any) {
      setError(err.response?.data?.message || 'Upload failed');
    } finally {
      setUploading(false);
    }
  };

  if (loading) return <Spinner />;

  return (
    <div className="max-w-4xl mx-auto space-y-8">
      <h1 className="text-2xl font-bold text-slate-900">KYC Verification</h1>

      <div className="bg-white p-6 rounded-xl shadow-sm border border-slate-200">
        <h2 className="text-lg font-semibold mb-4">Current Status</h2>
        <Badge variant={
          status?.kycStatus === 'APPROVED' ? 'success' : 
          status?.kycStatus === 'REJECTED' ? 'danger' : 
          status?.kycStatus === 'PENDING' ? 'warning' : 'default'
        } className="text-sm px-4 py-1">
          {status?.kycStatus || 'NONE'}
        </Badge>
      </div>

      <div className="bg-white p-6 rounded-xl shadow-sm border border-slate-200">
        <h2 className="text-lg font-semibold mb-4">Upload Document</h2>
        {error && <Alert variant="danger" className="mb-4">{error}</Alert>}
        <form onSubmit={handleUpload} className="space-y-4">
          <div>
            <label className="block text-sm font-medium text-slate-700 mb-1">Document Type</label>
            <select
              className="w-full max-w-sm rounded-md border border-slate-300 px-3 py-2 text-sm focus:outline-none focus:ring-2 focus:ring-indigo-500"
              value={docType}
              onChange={e => setDocType(e.target.value)}
            >
              <option value="PASSPORT">Passport</option>
              <option value="NATIONAL_ID">National ID</option>
              <option value="DRIVERS_LICENSE">Driver's License</option>
              <option value="UTILITY_BILL">Utility Bill</option>
            </select>
          </div>
          <div>
            <label className="block text-sm font-medium text-slate-700 mb-1">File</label>
            <input 
              type="file" 
              onChange={e => setFile(e.target.files?.[0] || null)}
              className="block w-full text-sm text-slate-500 file:mr-4 file:py-2 file:px-4 file:rounded-md file:border-0 file:text-sm file:font-semibold file:bg-indigo-50 file:text-indigo-700 hover:file:bg-indigo-100"
              required
            />
          </div>
          <Button type="submit" disabled={!file || uploading}>
            {uploading ? 'Uploading...' : 'Upload Document'}
          </Button>
        </form>
      </div>

      <div className="bg-white p-6 rounded-xl shadow-sm border border-slate-200">
        <h2 className="text-lg font-semibold mb-4">Uploaded Documents</h2>
        {status?.documents.length === 0 ? (
          <p className="text-slate-500">No documents uploaded yet.</p>
        ) : (
          <div className="space-y-4">
            {status?.documents.map(doc => (
              <div key={doc.id} className="flex justify-between items-center p-4 border border-slate-100 rounded-lg bg-slate-50">
                <div>
                  <p className="font-medium text-slate-900">{doc.documentType}</p>
                  <p className="text-xs text-slate-500">{new Date(doc.createdAt).toLocaleString()}</p>
                  {doc.rejectionReason && <p className="text-sm text-red-600 mt-1">Reason: {doc.rejectionReason}</p>}
                </div>
                <Badge variant={
                  doc.status === 'APPROVED' ? 'success' : 
                  doc.status === 'REJECTED' ? 'danger' : 'warning'
                }>{doc.status}</Badge>
              </div>
            ))}
          </div>
        )}
      </div>
    </div>
  );
};