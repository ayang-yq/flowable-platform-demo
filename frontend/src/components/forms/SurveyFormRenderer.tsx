'use client';

import { useEffect, useState, useCallback } from 'react';
import { Model } from 'survey-core';
import { Survey } from 'survey-react-ui';
import 'survey-core/defaultV2.min.css';
import { FormSchema as FormSchemaType } from '@/types/form';

interface SurveyFormRendererProps {
  schema: any;
  onComplete?: (data: any) => void;
  onDataChange?: (data: any) => void;
  formVersion?: number;
  readOnly?: boolean;
}

export default function SurveyFormRenderer({
  schema,
  onComplete,
  onDataChange,
  formVersion,
  readOnly = false,
}: SurveyFormRendererProps) {
  const [surveyModel, setSurveyModel] = useState<Model | null>(null);
  const [isLoading, setIsLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);

  useEffect(() => {
    try {
      // Create SurveyJS model from JSON schema
      const model = new Model(schema);

      // Configure survey options
      model.locale = 'en';

      // Apply field-level permissions
      if (schema.fieldPermissions) {
        Object.entries(schema.fieldPermissions).forEach(([fieldName, config]: [string, any]) => {
          const question = model.getQuestionByName(fieldName);
          if (question) {
            if (config.permission === 'read-only') {
              question.readOnly = true;
            } else if (config.permission === 'hidden') {
              question.visible = false;
            }
          }
        });
      }

      // Set read-only mode if specified
      if (readOnly) {
        model.mode = 'display';
      }

      // Handle completion
      if (onComplete) {
        model.onComplete.add((sender: any) => {
          onComplete(sender.data);
        });
      }

      // Handle data changes
      if (onDataChange) {
        model.onValueChanged.add((sender: any, options: any) => {
          onDataChange(sender.data);
        });
      }

      setSurveyModel(model);
      setIsLoading(false);
    } catch (err) {
      setError(err instanceof Error ? err.message : 'Failed to load form');
      setIsLoading(false);
    }
  }, [schema, onComplete, onDataChange, readOnly]);

  if (isLoading) {
    return (
      <div className="flex items-center justify-center p-8">
        <div className="animate-spin rounded-full h-8 w-8 border-b-2 border-blue-600"></div>
        <span className="ml-3 text-gray-600">Loading form...</span>
      </div>
    );
  }

  if (error) {
    return (
      <div className="bg-red-50 border-l-4 border-red-400 p-4">
        <div className="flex">
          <div className="flex-shrink-0">
            <svg className="h-5 w-5 text-red-400" viewBox="0 0 20 20" fill="currentColor">
              <path fillRule="evenodd" d="M10 18a8 8 0 100-16 8 8 0 000 16zm1-9a1 1 0 10-2 0v4a1 1 0 102 0V9zm1-5a1 1 0 10-2 0 1 1 0 012 0z" clipRule="evenodd" />
            </svg>
          </div>
          <div className="ml-3">
            <p className="text-sm text-red-700">Error loading form: {error}</p>
          </div>
        </div>
      </div>
    );
  }

  if (!surveyModel) {
    return (
      <div className="text-center p-8 text-gray-500">
        No form data available
      </div>
    );
  }

  return (
    <div className="survey-form-container">
      {formVersion && (
        <div className="mb-4 text-sm text-gray-600">
          Form Version: {formVersion}
        </div>
      )}
      <Survey model={surveyModel} />
    </div>
  );
}
