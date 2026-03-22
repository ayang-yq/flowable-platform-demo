'use client';

interface AttachmentData {
  id: string;
  fileName: string;
  fileSize: number;
  mimeType: string;
  uploaderName: string;
  createdAt: string;
}

interface AttachmentListProps {
  attachments: AttachmentData[];
}

function formatFileSize(bytes: number): string {
  if (bytes < 1024) return bytes + ' B';
  if (bytes < 1024 * 1024) return (bytes / 1024).toFixed(1) + ' KB';
  return (bytes / (1024 * 1024)).toFixed(1) + ' MB';
}

export default function AttachmentList({ attachments }: AttachmentListProps) {
  return (
    <div className="space-y-4">
      <h3 className="text-lg font-semibold text-gray-900">Attachments</h3>
      {attachments.length === 0 ? (
        <p className="text-sm text-gray-500">No attachments.</p>
      ) : (
        <div className="space-y-2">
          {attachments.map((attachment) => (
            <div
              key={attachment.id}
              className="flex items-center justify-between p-3 bg-gray-50 rounded-lg"
            >
              <div className="flex items-center gap-3">
                <svg className="w-8 h-8 text-gray-400" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                  <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M7 21h10a2 2 0 002-2V9.414a1 1 0 00-.293-.707l-5.414-5.414A1 1 0 0012.586 3H7a2 2 0 00-2 2v14a2 2 0 002 2z" />
                </svg>
                <div>
                  <p className="text-sm font-medium text-gray-900">{attachment.fileName}</p>
                  <p className="text-xs text-gray-500">
                    {formatFileSize(attachment.fileSize)} &middot; {attachment.uploaderName} &middot;{' '}
                    {new Date(attachment.createdAt).toLocaleDateString()}
                  </p>
                </div>
              </div>
              <button className="text-sm text-blue-600 hover:text-blue-800">Download</button>
            </div>
          ))}
        </div>
      )}
    </div>
  );
}
