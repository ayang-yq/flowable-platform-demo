import { render, screen, fireEvent, waitFor } from '@testing-library/react';
import '@testing-library/jest-dom';
import TaskListContainer from '@/components/tasks/TaskListContainer';
import { TaskDTO, TaskPageResponse } from '@/types/task';

// Mock next/navigation
const mockPush = jest.fn();
jest.mock('next/navigation', () => ({
  useRouter: () => ({ push: mockPush }),
}));

// Mock apiClient
const mockClaimTask = jest.fn();
const mockCompleteTask = jest.fn();
jest.mock('@/lib/api', () => ({
  apiClient: {
    claimTask: (...args: any[]) => mockClaimTask(...args),
    completeTask: (...args: any[]) => mockCompleteTask(...args),
    getTask: jest.fn(),
  },
}));

const baseTask: TaskDTO = {
  id: 'task-1',
  name: 'Review Request',
  description: 'Review the submitted request',
  assignee: undefined,
  createTime: '2026-03-26T10:00:00Z',
  priority: 50,
  processInstanceId: 'proc-1',
};

const baseData: TaskPageResponse = {
  content: [{ ...baseTask }],
  totalElements: 1,
  totalPages: 1,
  size: 20,
  number: 0,
  first: true,
  last: true,
};

describe('TaskListContainer', () => {
  beforeEach(() => {
    jest.clearAllMocks();
  });

  it('renders task cards with task data', () => {
    render(<TaskListContainer initialData={baseData} />);
    expect(screen.getByText('Review Request')).toBeInTheDocument();
    expect(screen.getByText('Claim Task')).toBeInTheDocument();
  });

  it('shows empty state when no tasks', () => {
    const emptyData = { ...baseData, content: [], totalElements: 0 };
    render(<TaskListContainer initialData={emptyData} />);
    expect(screen.getByText('No tasks')).toBeInTheDocument();
  });

  it('shows Claim button for unassigned tasks', () => {
    render(<TaskListContainer initialData={baseData} />);
    expect(screen.getByText('Claim Task')).toBeInTheDocument();
  });

  it('shows Complete button for assigned tasks', () => {
    const assignedData: TaskPageResponse = {
      ...baseData,
      content: [{ ...baseTask, assignee: 'john' }],
    };
    render(<TaskListContainer initialData={assignedData} />);
    expect(screen.getByText('Complete')).toBeInTheDocument();
    expect(screen.queryByText('Claim Task')).not.toBeInTheDocument();
  });

  it('navigates to task detail on View Details click', async () => {
    render(<TaskListContainer initialData={baseData} />);
    fireEvent.click(screen.getByText('View Details'));
    expect(mockPush).toHaveBeenCalledWith('/tasks/task-1');
  });

  it('calls claimTask API on Claim click with optimistic update', async () => {
    mockClaimTask.mockResolvedValue(undefined);
    render(<TaskListContainer initialData={baseData} />);
    fireEvent.click(screen.getByText('Claim Task'));
    // Optimistic update - claim button should disappear
    await waitFor(() => {
      expect(mockClaimTask).toHaveBeenCalledWith('task-1');
    });
  });

  it('reverts optimistic update on claim failure', async () => {
    mockClaimTask.mockRejectedValue(new Error('Already claimed'));
    render(<TaskListContainer initialData={baseData} />);
    fireEvent.click(screen.getByText('Claim Task'));
    await waitFor(() => {
      expect(screen.getByText(/Failed to claim task/)).toBeInTheDocument();
    });
  });

  it('removes task from list on Complete click', async () => {
    const assignedData: TaskPageResponse = {
      ...baseData,
      content: [{ ...baseTask, assignee: 'john' }],
    };
    mockCompleteTask.mockResolvedValue(undefined);
    render(<TaskListContainer initialData={assignedData} />);
    fireEvent.click(screen.getByText('Complete'));
    await waitFor(() => {
      expect(screen.queryByText('Review Request')).not.toBeInTheDocument();
    });
  });

  it('adds task back on complete failure', async () => {
    const assignedData: TaskPageResponse = {
      ...baseData,
      content: [{ ...baseTask, assignee: 'john' }],
    };
    mockCompleteTask.mockRejectedValue(new Error('Completion failed'));
    render(<TaskListContainer initialData={assignedData} />);
    fireEvent.click(screen.getByText('Complete'));
    await waitFor(() => {
      expect(screen.getByText(/Failed to complete task/)).toBeInTheDocument();
      expect(screen.getByText('Review Request')).toBeInTheDocument();
    });
  });
});
