'use client';

import { useState, useRef, useCallback } from 'react';

interface CommentInputProps {
  onSubmit: (content: string) => Promise<void>;
  placeholder?: string;
  users?: { username: string; displayName: string }[];
}

export default function CommentInput({ onSubmit, placeholder = 'Add a comment...', users = [] }: CommentInputProps) {
  const [content, setContent] = useState('');
  const [isSubmitting, setIsSubmitting] = useState(false);
  const [showMentions, setShowMentions] = useState(false);
  const [mentionQuery, setMentionQuery] = useState('');
  const textareaRef = useRef<HTMLTextAreaElement>(null);

  const filteredUsers = users.filter(
    (u) => u.username.toLowerCase().includes(mentionQuery.toLowerCase()) ||
           u.displayName.toLowerCase().includes(mentionQuery.toLowerCase())
  );

  const handleChange = useCallback((e: React.ChangeEvent<HTMLTextAreaElement>) => {
    const value = e.target.value;
    setContent(value);

    // Check for @mention trigger
    const cursorPos = e.target.selectionStart;
    const textBeforeCursor = value.slice(0, cursorPos);
    const mentionMatch = textBeforeCursor.match(/@(\w*)$/);
    if (mentionMatch) {
      setMentionQuery(mentionMatch[1]);
      setShowMentions(true);
    } else {
      setShowMentions(false);
    }
  }, []);

  const insertMention = useCallback((username: string) => {
    const textarea = textareaRef.current;
    if (!textarea) return;

    const cursorPos = textarea.selectionStart;
    const textBeforeCursor = content.slice(0, cursorPos);
    const textAfterCursor = content.slice(cursorPos);
    const mentionStart = textBeforeCursor.lastIndexOf('@');
    const newContent = textBeforeCursor.slice(0, mentionStart) + '@' + username + ' ' + textAfterCursor;
    setContent(newContent);
    setShowMentions(false);
  }, [content]);

  const handleSubmit = async () => {
    if (!content.trim() || isSubmitting) return;
    setIsSubmitting(true);
    try {
      await onSubmit(content.trim());
      setContent('');
    } finally {
      setIsSubmitting(false);
    }
  };

  return (
    <div className="relative">
      <textarea
        ref={textareaRef}
        value={content}
        onChange={handleChange}
        placeholder={placeholder}
        rows={3}
        className="w-full px-3 py-2 border border-gray-300 rounded-md focus:outline-none focus:ring-2 focus:ring-blue-500 resize-none"
      />

      {showMentions && filteredUsers.length > 0 && (
        <div className="absolute z-10 w-64 bg-white border border-gray-200 rounded-md shadow-lg mt-1 max-h-48 overflow-y-auto">
          {filteredUsers.map((user) => (
            <button
              key={user.username}
              onClick={() => insertMention(user.username)}
              className="w-full text-left px-3 py-2 text-sm hover:bg-blue-50 focus:bg-blue-50"
            >
              <span className="font-medium">{user.displayName}</span>
              <span className="text-gray-500 ml-1">@{user.username}</span>
            </button>
          ))}
        </div>
      )}

      <div className="mt-2 flex justify-end">
        <button
          onClick={handleSubmit}
          disabled={!content.trim() || isSubmitting}
          className="px-4 py-2 text-sm font-medium text-white bg-blue-600 rounded-md hover:bg-blue-700 disabled:opacity-50 disabled:cursor-not-allowed"
        >
          {isSubmitting ? 'Posting...' : 'Post Comment'}
        </button>
      </div>
    </div>
  );
}
