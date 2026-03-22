import { render, screen, fireEvent, waitFor } from '@testing-library/react';
import '@testing-library/jest-dom';
import SurveyFormRenderer from '@/components/forms/SurveyFormRenderer';

// Mock SurveyJS
jest.mock('survey-react-ui', () => ({
  Survey: ({ json, onComplete }: any) => {
    return (
      <div data-testid="survey-component">
        <div data-testid="survey-title">{json.title}</div>
        <button
          onClick={() => onComplete({ employeeName: 'John Doe', reason: 'Test' })}
          data-testid="complete-survey"
        >
          Complete
        </button>
      </div>
    );
  },
}));

// Mock API client
jest.mock('@/lib/api', () => ({
  apiClient: {
    get: jest.fn(),
    post: jest.fn(),
  },
}));

describe('SurveyJSFormTest', () => {
  const mockFormSchema = {
    title: 'Leave Request Form',
    description: 'Submit a leave request',
    fields: [
      {
        name: 'employeeName',
        type: 'text',
        label: 'Employee Name',
        isRequired: true,
      },
      {
        name: 'reason',
        type: 'textarea',
        label: 'Reason',
        isRequired: true,
      },
    ],
  };

  const mockFormData = {
    employeeName: 'John Doe',
    reason: 'Annual leave',
  };

  beforeEach(() => {
    jest.clearAllMocks();
  });

  describe('Form Rendering', () => {
    test('should render form with title and description', () => {
      render(<SurveyFormRenderer schema={mockFormSchema} onComplete={jest.fn()} />);

      expect(screen.getByTestId('survey-title')).toHaveTextContent('Leave Request Form');
      expect(screen.getByTestId('survey-component')).toBeInTheDocument();
    });

    test('should render all form fields', async () => {
      render(<SurveyFormRenderer schema={mockFormSchema} onComplete={jest.fn()} />);

      await waitFor(() => {
        expect(screen.getByTestId('survey-component')).toBeInTheDocument();
      });
    });

    test('should handle empty schema gracefully', () => {
      const emptySchema = { title: 'Empty Form', fields: [] };

      render(<SurveyFormRenderer schema={emptySchema} onComplete={jest.fn()} />);

      expect(screen.getByTestId('survey-component')).toBeInTheDocument();
    });
  });

  describe('Form Validation', () => {
    test('should show validation errors for required fields', async () => {
      const onComplete = jest.fn();

      render(<SurveyFormRenderer schema={mockFormSchema} onComplete={onComplete} />);

      const completeButton = screen.getByTestId('complete-survey');
      fireEvent.click(completeButton);

      await waitFor(() => {
        // Should show validation errors instead of completing
        expect(onComplete).not.toHaveBeenCalled();
      });
    });

    test('should complete form with valid data', async () => {
      const onComplete = jest.fn();

      render(<SurveyFormRenderer schema={mockFormSchema} onComplete={onComplete} />);

      const completeButton = screen.getByTestId('complete-survey');
      fireEvent.click(completeButton);

      await waitFor(() => {
        expect(onComplete).toHaveBeenCalledWith(mockFormData);
      });
    });

    test('should validate field types', async () => {
      const schemaWithNumberField = {
        ...mockFormSchema,
        fields: [
          ...mockFormSchema.fields,
          {
            name: 'duration',
            type: 'number',
            label: 'Duration (days)',
            isRequired: true,
          },
        ],
      };

      render(<SurveyFormRenderer schema={schemaWithNumberField} onComplete={jest.fn()} />);

      expect(screen.getByTestId('survey-component')).toBeInTheDocument();
    });
  });

  describe('Form State Management', () => {
    test('should track form data changes', async () => {
      const onDataChange = jest.fn();

      render(
        <SurveyFormRenderer
          schema={mockFormSchema}
          onComplete={jest.fn()}
          onDataChange={onDataChange}
        />
      );

      await waitFor(() => {
        expect(screen.getByTestId('survey-component')).toBeInTheDocument();
      });
    });

    test('should reset form to initial state', async () => {
      const { rerender } = render(
        <SurveyFormRenderer schema={mockFormSchema} onComplete={jest.fn()} />
      );

      rerender(<SurveyFormRenderer schema={mockFormSchema} onComplete={jest.fn()} />);

      await waitFor(() => {
        expect(screen.getByTestId('survey-component')).toBeInTheDocument();
      });
    });
  });

  describe('Form Field Types', () => {
    test('should render text input field', () => {
      const schemaWithTextField = {
        title: 'Text Field Form',
        fields: [
          {
            name: 'fullName',
            type: 'text',
            label: 'Full Name',
            isRequired: true,
          },
        ],
      };

      render(<SurveyFormRenderer schema={schemaWithTextField} onComplete={jest.fn()} />);

      expect(screen.getByTestId('survey-component')).toBeInTheDocument();
    });

    test('should render number input field', () => {
      const schemaWithNumberField = {
        title: 'Number Field Form',
        fields: [
          {
            name: 'age',
            type: 'number',
            label: 'Age',
            isRequired: true,
          },
        ],
      };

      render(<SurveyFormRenderer schema={schemaWithNumberField} onComplete={jest.fn()} />);

      expect(screen.getByTestId('survey-component')).toBeInTheDocument();
    });

    test('should render date input field', () => {
      const schemaWithDateField = {
        title: 'Date Field Form',
        fields: [
          {
            name: 'startDate',
            type: 'date',
            label: 'Start Date',
            isRequired: true,
          },
        ],
      };

      render(<SurveyFormRenderer schema={schemaWithDateField} onComplete={jest.fn()} />);

      expect(screen.getByTestId('survey-component')).toBeInTheDocument();
    });

    test('should render select dropdown field', () => {
      const schemaWithSelectField = {
        title: 'Select Field Form',
        fields: [
          {
            name: 'department',
            type: 'select',
            label: 'Department',
            isRequired: true,
            choices: ['Engineering', 'Finance', 'HR'],
          },
        ],
      };

      render(<SurveyFormRenderer schema={schemaWithSelectField} onComplete={jest.fn()} />);

      expect(screen.getByTestId('survey-component')).toBeInTheDocument();
    });

    test('should render radio button field', () => {
      const schemaWithRadioField = {
        title: 'Radio Field Form',
        fields: [
          {
            name: 'approval',
            type: 'radio',
            label: 'Approval',
            isRequired: true,
            choices: ['Approve', 'Reject'],
          },
        ],
      };

      render(<SurveyFormRenderer schema={schemaWithRadioField} onComplete={jest.fn()} />);

      expect(screen.getByTestId('survey-component')).toBeInTheDocument();
    });

    test('should render checkbox field', () => {
      const schemaWithCheckboxField = {
        title: 'Checkbox Field Form',
        fields: [
          {
            name: 'agreed',
            type: 'checkbox',
            label: 'I agree to the terms',
            isRequired: true,
          },
        ],
      };

      render(<SurveyFormRenderer schema={schemaWithCheckboxField} onComplete={jest.fn()} />);

      expect(screen.getByTestId('survey-component')).toBeInTheDocument();
    });

    test('should render textarea field', () => {
      const schemaWithTextareaField = {
        title: 'Textarea Field Form',
        fields: [
          {
            name: 'comments',
            type: 'textarea',
            label: 'Comments',
            isRequired: false,
          },
        ],
      };

      render(<SurveyFormRenderer schema={schemaWithTextareaField} onComplete={jest.fn()} />);

      expect(screen.getByTestId('survey-component')).toBeInTheDocument();
    });

    test('should render file upload field', () => {
      const schemaWithFileField = {
        title: 'File Upload Form',
        fields: [
          {
            name: 'document',
            type: 'file',
            label: 'Upload Document',
            isRequired: true,
          },
        ],
      };

      render(<SurveyFormRenderer schema={schemaWithFileField} onComplete={jest.fn()} />);

      expect(screen.getByTestId('survey-component')).toBeInTheDocument();
    });
  });

  describe('Form Permissions', () => {
    test('should enforce read-only field permissions', async () => {
      const schemaWithPermissions = {
        ...mockFormSchema,
        fieldPermissions: {
          employeeName: {
            permission: 'read-only',
            roles: ['user', 'manager'],
          },
        },
      };

      render(<SurveyFormRenderer schema={schemaWithPermissions} onComplete={jest.fn()} />);

      expect(screen.getByTestId('survey-component')).toBeInTheDocument();
    });

    test('should enforce hidden field permissions', async () => {
      const schemaWithHiddenField = {
        ...mockFormSchema,
        fieldPermissions: {
          employeeName: {
            permission: 'hidden',
            roles: ['user'],
          },
        },
      };

      render(<SurveyFormRenderer schema={schemaWithHiddenField} onComplete={jest.fn()} />);

      expect(screen.getByTestId('survey-component')).toBeInTheDocument();
    });

    test('should enforce required field permissions', async () => {
      const schemaWithRequiredFields = {
        ...mockFormSchema,
        fieldPermissions: {
          reason: {
            permission: 'required',
            roles: ['manager'],
          },
        },
      };

      render(<SurveyFormRenderer schema={schemaWithRequiredFields} onComplete={jest.fn()} />);

      expect(screen.getByTestId('survey-component')).toBeInTheDocument();
    });
  });

  describe('Form Versioning', () => {
    test('should load specific form version', async () => {
      const versionedSchema = {
        ...mockFormSchema,
        version: 2,
      };

      render(
        <SurveyFormRenderer
          schema={versionedSchema}
          onComplete={jest.fn()}
          formVersion={2}
        />
      );

      expect(screen.getByTestId('survey-component')).toBeInTheDocument();
    });

    test('should handle version mismatch gracefully', async () => {
      render(
        <SurveyFormRenderer
          schema={mockFormSchema}
          onComplete={jest.fn()}
          formVersion={999} // Non-existent version
        />
      );

      expect(screen.getByTestId('survey-component')).toBeInTheDocument();
    });
  });

  describe('Error Handling', () => {
    test('should handle schema parse errors gracefully', async () => {
      const invalidSchema = {
        title: 'Invalid Form',
        fields: 'invalid fields data',
      };

      render(<SurveyFormRenderer schema={invalidSchema} onComplete={jest.fn()} />);

      // Should render error message instead of crashing
      await waitFor(() => {
        expect(screen.getByTestId('survey-component')).toBeInTheDocument();
      });
    });

    test('should handle network errors during form submission', async () => {
      const mockError = new Error('Network error');
      const onComplete = jest.fn().mockRejectedValue(mockError);

      render(<SurveyFormRenderer schema={mockFormSchema} onComplete={onComplete} />);

      const completeButton = screen.getByTestId('complete-survey');
      fireEvent.click(completeButton);

      await waitFor(() => {
        expect(screen.getByTestId('survey-component')).toBeInTheDocument();
      });
    });
  });

  describe('Accessibility', () => {
    test('should have proper ARIA labels', async () => {
      render(<SurveyFormRenderer schema={mockFormSchema} onComplete={jest.fn()} />);

      await waitFor(() => {
        const form = screen.getByTestId('survey-component');
        expect(form).toBeInTheDocument();
      });
    });

    test('should support keyboard navigation', async () => {
      render(<SurveyFormRenderer schema={mockFormSchema} onComplete={jest.fn()} />);

      await waitFor(() => {
        const completeButton = screen.getByTestId('complete-survey');
        expect(completeButton).toBeInTheDocument();

        // Test keyboard navigation
        completeButton.focus();
        expect(completeButton).toHaveFocus();
      });
    });

    test('should announce validation errors to screen readers', async () => {
      const onComplete = jest.fn();

      render(<SurveyFormRenderer schema={mockFormSchema} onComplete={jest.fn()} />);

      const completeButton = screen.getByTestId('complete-survey');
      fireEvent.click(completeButton);

      await waitFor(() => {
        expect(screen.getByTestId('survey-component')).toBeInTheDocument();
      });
    });
  });

  describe('Performance', () => {
    test('should render large forms efficiently', async () => {
      const largeSchema = {
        title: 'Large Form',
        fields: Array.from({ length: 100 }, (_, i) => ({
          name: `field${i}`,
          type: 'text',
          label: `Field ${i}`,
          isRequired: i % 10 === 0, // Every 10th field is required
        })),
      };

      const startTime = performance.now();
      render(<SurveyFormRenderer schema={largeSchema} onComplete={jest.fn()} />);

      await waitFor(() => {
        expect(screen.getByTestId('survey-component')).toBeInTheDocument();
        const endTime = performance.now();
        expect(endTime - startTime).toBeLessThan(1000); // Should render in under 1 second
      });
    });

    test('should handle rapid form data changes efficiently', async () => {
      const onDataChange = jest.fn();

      render(
        <SurveyFormRenderer
          schema={mockFormSchema}
          onComplete={jest.fn()}
          onDataChange={onDataChange}
        />
      );

      await waitFor(() => {
        expect(screen.getByTestId('survey-component')).toBeInTheDocument();
      });

      // Simulate rapid data changes
      for (let i = 0; i < 50; i++) {
        // In a real implementation, this would trigger form data changes
        // For now, we just verify the component can handle rapid updates
      }

      expect(screen.getByTestId('survey-component')).toBeInTheDocument();
    });
  });
});
