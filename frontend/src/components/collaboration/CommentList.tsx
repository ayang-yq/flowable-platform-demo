'use client';

interface CommentData {
  id: string;
  authorName: string;
  content: string;
  mentions: string[] | null;
  createdAt: string;
}

interface CommentListProps {
  taskId?: string;
  processInstanceId?: string;
  comments: CommentData[];
}

export default function CommentList({ comments }: CommentListProps) {
  return (
    <div className="space-y-4">
      <h3 className="text-lg font-semibold text-gray-900">Comments</h3>
      {comments.length === 0 ? (
        <p className="text-sm text-gray-500">No comments yet.</p>
      ) : (
        <div className="space-y-3">
          {comments.map((comment) => (
            <div key={comment.id} className="bg-gray-50 rounded-lg p-4">
              <div className="flex items-center justify-between mb-2">
                <span className="text-sm font-medium text-gray-900">{comment.authorName}</span>
                <span className="text-xs text-gray-500">
                  {new Date(comment.createdAt).toLocaleString()}
                </span>
              </div>
              <p className="text-sm text-gray-700 whitespace-pre-wrap">{comment.content}</p>
            </div>
          ))}
        </div>
      )}
    </div>
  );
}
