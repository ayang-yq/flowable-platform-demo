import { apiClient } from '@/lib/api';
import { FormSchema } from '@/types/form';
import SurveyFormRenderer from './SurveyFormRenderer';

interface SurveyFormProps {
  formId: string;
  formVersion?: number;
  processInstanceId?: string; // T094: Support fetching form version from process instance
  onComplete?: (data: any) => void;
  readOnly?: boolean;
}

async function getFormSchema(formId: string, version?: number): Promise<FormSchema> {
  try {
    const url = version
      ? `/api/forms/${formId}/version/${version}`
      : `/api/forms/${formId}`;

    const response = await apiClient.get<FormSchema>(url);
    return response.data;
  } catch (error) {
    console.error('Failed to fetch form schema:', error);
    throw error;
  }
}

async function getProcessInstanceFormVersion(processInstanceId: string): Promise<{ formId: string; formVersion: number }> {
  try {
    // Get form version from process instance variables (T094)
    const response = await apiClient.get<any>(`/api/forms/process-instance/${processInstanceId}`);
    return {
      formId: response.data.id,
      formVersion: response.data.version,
    };
  } catch (error) {
    console.error('Failed to fetch process instance form version:', error);
    throw error;
  }
}

export default async function SurveyForm({
  formId,
  formVersion,
  processInstanceId, // T094: New prop for historical form rendering
  onComplete,
  readOnly = false,
}: SurveyFormProps) {
  let formSchema: FormSchema;
  let parsedSchema: any;

  try {
    // If process instance ID is provided, fetch the specific form version used by that instance (T094)
    if (processInstanceId) {
      const { formId: instanceFormId, formVersion: instanceFormVersion } =
        await getProcessInstanceFormVersion(processInstanceId);

      formSchema = await getFormSchema(instanceFormId, instanceFormVersion);
    } else {
      // Fetch form schema with explicit version or latest
      formSchema = await getFormSchema(formId, formVersion);
    }

    // Parse JSON schema
    parsedSchema = JSON.parse(formSchema.schema);
  } catch (error) {
    return (
      <div className="bg-red-50 border-l-4 border-red-400 p-4">
        <div className="flex">
          <div className="flex-shrink-0">
            <svg className="h-5 w-5 text-red-400" viewBox="0 0 20 20" fill="currentColor">
              <path fillRule="evenodd" d="M10 18a8 8 0 100-16 8 8 0 000 16zm1-9a1 1 0 10-2 0v4a1 1 0 102 0V9zm1-5a1 1 0 10-2 0 1 1 0 012 0z" clipRule="evenodd" />
            </svg>
          </div>
          <div className="ml-3">
            <p className="text-sm text-red-700">
              Failed to load form. Please try again later.
            </p>
          </div>
        </div>
      </div>
    );
  }

  return (
    <div className="survey-form-wrapper">
      <div className="mb-4">
        <div className="flex items-center justify-between">
          <div>
            <h2 className="text-xl font-semibold text-gray-900">{parsedSchema.title || formSchema.name}</h2>
            {parsedSchema.description && (
              <p className="text-gray-600 mt-1">{parsedSchema.description}</p>
            )}
          </div>
          {/* T094: Display form version information */}
          <div className="flex items-center gap-2">
            <span className="inline-flex items-center px-2.5 py-0.5 rounded-full text-xs font-medium bg-blue-100 text-blue-800">
              Version {formSchema.version}
            </span>
            {processInstanceId && (
              <span className="inline-flex items-center px-2.5 py-0.5 rounded-full text-xs font-medium bg-purple-100 text-purple-800">
                Historical View
              </span>
            )}
          </div>
        </div>
      </div>

      <SurveyFormRenderer
        schema={parsedSchema}
        onComplete={onComplete}
        readOnly={readOnly}
        formVersion={formSchema.version}
      />
    </div>
  );
}
