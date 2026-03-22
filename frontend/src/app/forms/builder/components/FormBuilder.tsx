'use client';

import { useState, useCallback } from 'react';
import { FormField, FormSchema as FormSchemaType } from '@/types/form';

interface FormBuilderProps {
  initialSchema?: FormSchemaType;
  onSave?: (schema: FormSchemaType) => void;
  onPreview?: (schema: FormSchemaType) => void;
}

type FieldType = 'text' | 'number' | 'date' | 'select' | 'radio' | 'checkbox' | 'file' | 'textarea';

export default function FormBuilder({ initialSchema, onSave, onPreview }: FormBuilderProps) {
  const [fields, setFields] = useState<FormField[]>(initialSchema?.fields || []);
  const [formTitle, setFormTitle] = useState(initialSchema?.title || '');
  const [formDescription, setFormDescription] = useState(initialSchema?.description || '');
  const [selectedField, setSelectedField] = useState<FormField | null>(null);
  const [isPreviewMode, setIsPreviewMode] = useState(false);

  const addField = useCallback((type: FieldType) => {
    const newField: FormField = {
      name: `field_${fields.length + 1}`,
      type,
      label: `Field ${fields.length + 1}`,
      isRequired: false,
    };

    setFields([...fields, newField]);
    setSelectedField(newField);
  }, [fields]);

  const updateField = useCallback((index: number, updates: Partial<FormField>) => {
    const updatedFields = [...fields];
    updatedFields[index] = { ...updatedFields[index], ...updates };
    setFields(updatedFields);
    if (selectedField === fields[index]) {
      setSelectedField(updatedFields[index]);
    }
  }, [fields, selectedField]);

  const removeField = useCallback((index: number) => {
    const updatedFields = fields.filter((_, i) => i !== index);
    setFields(updatedFields);
    setSelectedField(null);
  }, [fields]);

  const handleSave = useCallback(() => {
    const schema: FormSchemaType = {
      title: formTitle,
      description: formDescription,
      fields,
    };

    if (onSave) {
      onSave(schema);
    }
  }, [formTitle, formDescription, fields, onSave]);

  const handlePreview = useCallback(() => {
    const schema: FormSchemaType = {
      title: formTitle,
      description: formDescription,
      fields,
    };

    setIsPreviewMode(true);
    if (onPreview) {
      onPreview(schema);
    }
  }, [formTitle, formDescription, fields, onPreview]);

  if (isPreviewMode) {
    return (
      <div className="space-y-4">
        <div className="flex items-center justify-between p-4 bg-blue-50 border-l-4 border-blue-400 rounded">
          <div className="flex-1">
            <p className="text-sm text-blue-700">
              <strong>Preview Mode</strong> - This is how your form will appear to users
            </p>
          </div>
          <button
            onClick={() => setIsPreviewMode(false)}
            className="px-4 py-2 text-sm font-medium text-blue-600 bg-white border border-blue-300 rounded-md hover:bg-blue-50"
          >
            Exit Preview
          </button>
        </div>
        <div className="border border-gray-300 rounded-lg p-6">
          <h3 className="text-lg font-semibold mb-4">{formTitle}</h3>
          {formDescription && <p className="text-gray-600 mb-6">{formDescription}</p>}
          <div className="space-y-4">
            {fields.map((field) => (
              <div key={field.name} className="border-b pb-4 last:border-0">
                <label className="block text-sm font-medium text-gray-700 mb-1">
                  {field.label}
                  {field.isRequired && <span className="text-red-500 ml-1">*</span>}
                </label>
                {renderFieldPreview(field)}
              </div>
            ))}
          </div>
        </div>
      </div>
    );
  }

  return (
    <div className="space-y-6">
      {/* Form Properties */}
      <div className="bg-white border border-gray-200 rounded-lg p-6">
        <h3 className="text-lg font-semibold text-gray-900 mb-4">Form Properties</h3>
        <div className="grid grid-cols-1 gap-4">
          <div>
            <label className="block text-sm font-medium text-gray-700 mb-1">Form Title</label>
            <input
              type="text"
              value={formTitle}
              onChange={(e) => setFormTitle(e.target.value)}
              className="w-full px-3 py-2 border border-gray-300 rounded-md focus:outline-none focus:ring-2 focus:ring-blue-500"
              placeholder="e.g., Leave Request Form"
            />
          </div>
          <div>
            <label className="block text-sm font-medium text-gray-700 mb-1">Description</label>
            <textarea
              value={formDescription}
              onChange={(e) => setFormDescription(e.target.value)}
              rows={3}
              className="w-full px-3 py-2 border border-gray-300 rounded-md focus:outline-none focus:ring-2 focus:ring-blue-500"
              placeholder="Form description and instructions"
            />
          </div>
        </div>
      </div>

      {/* Field Palette */}
      <div className="bg-white border border-gray-200 rounded-lg p-6">
        <h3 className="text-lg font-semibold text-gray-900 mb-4">Add Fields</h3>
        <div className="grid grid-cols-2 gap-2">
          <button
            onClick={() => addField('text')}
            className="p-3 text-left border border-gray-300 rounded-md hover:bg-gray-50 transition"
          >
            <div className="flex items-center gap-2">
              <svg className="w-5 h-5 text-gray-600" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M11 5H6a2 2 0 00-2 2v11a2 2 0 002 2h11a2 2 0 002-2v-5m-1.414-9.414a2 2 0 112.828 2.828L11.828 15H9v-2.828l8.586-8.586z" />
              </svg>
              <span className="text-sm font-medium">Text</span>
            </div>
          </button>
          <button
            onClick={() => addField('number')}
            className="p-3 text-left border border-gray-300 rounded-md hover:bg-gray-50 transition"
          >
            <div className="flex items-center gap-2">
              <svg className="w-5 h-5 text-gray-600" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M7 20l4-16m2 16l4-4M4 20l4-16M9 4H5a2 2 0 00-2 2v14a2 2 0 002 2h14a2 2 0 002-2V9a2 2 0 00-2-2h-5" />
              </svg>
              <span className="text-sm font-medium">Number</span>
            </div>
          </button>
          <button
            onClick={() => addField('date')}
            className="p-3 text-left border border-gray-300 rounded-md hover:bg-gray-50 transition"
          >
            <div className="flex items-center gap-2">
              <svg className="w-5 h-5 text-gray-600" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M8 7V3m8 4V3m-9 8h10M5 21h14a2 2 0 002-2V7a2 2 0 00-2-2H5a2 2 0 00-2 2v12a2 2 0 002 2z" />
              </svg>
              <span className="text-sm font-medium">Date</span>
            </div>
          </button>
          <button
            onClick={() => addField('select')}
            className="p-3 text-left border border-gray-300 rounded-md hover:bg-gray-50 transition"
          >
            <div className="flex items-center gap-2">
              <svg className="w-5 h-5 text-gray-600" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M19 9l-7 7-7-7" />
              </svg>
              <span className="text-sm font-medium">Dropdown</span>
            </div>
          </button>
          <button
            onClick={() => addField('radio')}
            className="p-3 text-left border border-gray-300 rounded-md hover:bg-gray-50 transition"
          >
            <div className="flex items-center gap-2">
              <svg className="w-5 h-5 text-gray-600" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M9 12l2 2 4-4m6 2a9 9 0 11-18 0 9 9 0 0118 0z" />
              </svg>
              <span className="text-sm font-medium">Radio</span>
            </div>
          </button>
          <button
            onClick={() => addField('checkbox')}
            className="p-3 text-left border border-gray-300 rounded-md hover:bg-gray-50 transition"
          >
            <div className="flex items-center gap-2">
              <svg className="w-5 h-5 text-gray-600" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M5 13l4 4L19 7" />
              </svg>
              <span className="text-sm font-medium">Checkbox</span>
            </div>
          </button>
          <button
            onClick={() => addField('textarea')}
            className="p-3 text-left border border-gray-300 rounded-md hover:bg-gray-50 transition"
          >
            <div className="flex items-center gap-2">
              <svg className="w-5 h-5 text-gray-600" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M4 6h16M4 12h16M4 18h16" />
              </svg>
              <span className="text-sm font-medium">Text Area</span>
            </div>
          </button>
          <button
            onClick={() => addField('file')}
            className="p-3 text-left border border-gray-300 rounded-md hover:bg-gray-50 transition"
          >
            <div className="flex items-center gap-2">
              <svg className="w-5 h-5 text-gray-600" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M4 16v1a3 3 0 003 3h10a3 3 0 003-3v-1m-4-8l-4-4m0 0L8 8m4-4v12" />
              </svg>
              <span className="text-sm font-medium">File</span>
            </div>
          </button>
        </div>
      </div>

      {/* Field List */}
      <div className="bg-white border border-gray-200 rounded-lg p-6">
        <h3 className="text-lg font-semibold text-gray-900 mb-4">Form Fields</h3>
        {fields.length === 0 ? (
          <div className="text-center py-8 text-gray-400">
            <p className="text-sm">No fields added yet. Click a field type above to add one.</p>
          </div>
        ) : (
          <div className="space-y-2">
            {fields.map((field, index) => (
              <div
                key={field.name}
                className={`p-4 border rounded-md cursor-pointer transition ${
                  selectedField === field
                    ? 'border-blue-500 bg-blue-50'
                    : 'border-gray-300 hover:border-gray-400'
                }`}
                onClick={() => setSelectedField(field)}
              >
                <div className="flex items-center justify-between">
                  <div className="flex-1">
                    <div className="flex items-center gap-2">
                      <span className="font-medium text-gray-900">{field.label}</span>
                      {field.isRequired && (
                        <span className="text-xs text-red-500">Required</span>
                      )}
                      <span className="text-xs text-gray-500">({field.type})</span>
                    </div>
                    <p className="text-sm text-gray-600">{field.name}</p>
                  </div>
                  <button
                    onClick={(e) => {
                      e.stopPropagation();
                      removeField(index);
                    }}
                    className="ml-4 text-red-600 hover:text-red-800"
                  >
                    <svg className="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                      <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M19 7l-.867 12.142A2 2 0 0116.138 21H7.862a2 2 0 01-1.995-1.858L5 7m5 4v6m4-6v6m1-10V4a1 1 0 011-1h2a1 1 0 011 1v3M4 7h16" />
                    </svg>
                  </button>
                </div>
              </div>
            ))}
          </div>
        )}
      </div>

      {/* Field Properties */}
      {selectedField && (
        <div className="bg-white border border-gray-200 rounded-lg p-6">
          <h3 className="text-lg font-semibold text-gray-900 mb-4">Field Properties</h3>
          <div className="grid grid-cols-1 gap-4">
            <div>
              <label className="block text-sm font-medium text-gray-700 mb-1">Field Name</label>
              <input
                type="text"
                value={selectedField.name}
                onChange={(e) => {
                  const index = fields.findIndex((f) => f.name === selectedField.name);
                  updateField(index, { name: e.target.value });
                }}
                className="w-full px-3 py-2 border border-gray-300 rounded-md focus:outline-none focus:ring-2 focus:ring-blue-500"
              />
            </div>
            <div>
              <label className="block text-sm font-medium text-gray-700 mb-1">Label</label>
              <input
                type="text"
                value={selectedField.label}
                onChange={(e) => {
                  const index = fields.findIndex((f) => f.name === selectedField.name);
                  updateField(index, { label: e.target.value });
                }}
                className="w-full px-3 py-2 border border-gray-300 rounded-md focus:outline-none focus:ring-2 focus:ring-blue-500"
              />
            </div>
            <div>
              <label className="block text-sm font-medium text-gray-700 mb-1">Placeholder</label>
              <input
                type="text"
                value={selectedField.placeholder || ''}
                onChange={(e) => {
                  const index = fields.findIndex((f) => f.name === selectedField.name);
                  updateField(index, { placeholder: e.target.value });
                }}
                className="w-full px-3 py-2 border border-gray-300 rounded-md focus:outline-none focus:ring-2 focus:ring-blue-500"
              />
            </div>
            <div className="flex items-center gap-2">
              <input
                type="checkbox"
                checked={selectedField.isRequired || false}
                onChange={(e) => {
                  const index = fields.findIndex((f) => f.name === selectedField.name);
                  updateField(index, { isRequired: e.target.checked });
                }}
                className="w-4 h-4 text-blue-600 border-gray-300 rounded focus:ring-blue-500"
              />
              <label className="text-sm font-medium text-gray-700">Required Field</label>
            </div>

            {/* Validation rules (T091) */}
            {(selectedField.type === 'text' || selectedField.type === 'textarea') && (
              <>
                <div>
                  <label className="block text-sm font-medium text-gray-700 mb-1">Min Length</label>
                  <input
                    type="number"
                    value={selectedField.minLength || ''}
                    onChange={(e) => {
                      const index = fields.findIndex((f) => f.name === selectedField.name);
                      updateField(index, { minLength: parseInt(e.target.value) || undefined });
                    }}
                    className="w-full px-3 py-2 border border-gray-300 rounded-md focus:outline-none focus:ring-2 focus:ring-blue-500"
                  />
                </div>
                <div>
                  <label className="block text-sm font-medium text-gray-700 mb-1">Max Length</label>
                  <input
                    type="number"
                    value={selectedField.maxLength || ''}
                    onChange={(e) => {
                      const index = fields.findIndex((f) => f.name === selectedField.name);
                      updateField(index, { maxLength: parseInt(e.target.value) || undefined });
                    }}
                    className="w-full px-3 py-2 border border-gray-300 rounded-md focus:outline-none focus:ring-2 focus:ring-blue-500"
                  />
                </div>
                <div>
                  <label className="block text-sm font-medium text-gray-700 mb-1">Pattern</label>
                  <input
                    type="text"
                    value={selectedField.pattern || ''}
                    onChange={(e) => {
                      const index = fields.findIndex((f) => f.name === selectedField.name);
                      updateField(index, { pattern: e.target.value });
                    }}
                    className="w-full px-3 py-2 border border-gray-300 rounded-md focus:outline-none focus:ring-2 focus:ring-blue-500"
                    placeholder="e.g., ^[a-zA-Z0-9]+$"
                  />
                </div>
              </>
            )}

            {(selectedField.type === 'number') && (
              <>
                <div>
                  <label className="block text-sm font-medium text-gray-700 mb-1">Min Value</label>
                  <input
                    type="number"
                    value={selectedField.min || ''}
                    onChange={(e) => {
                      const index = fields.findIndex((f) => f.name === selectedField.name);
                      updateField(index, { min: parseFloat(e.target.value) || undefined });
                    }}
                    className="w-full px-3 py-2 border border-gray-300 rounded-md focus:outline-none focus:ring-2 focus:ring-blue-500"
                  />
                </div>
                <div>
                  <label className="block text-sm font-medium text-gray-700 mb-1">Max Value</label>
                  <input
                    type="number"
                    value={selectedField.max || ''}
                    onChange={(e) => {
                      const index = fields.findIndex((f) => f.name === selectedField.name);
                      updateField(index, { max: parseFloat(e.target.value) || undefined });
                    }}
                    className="w-full px-3 py-2 border border-gray-300 rounded-md focus:outline-none focus:ring-2 focus:ring-blue-500"
                  />
                </div>
              </>
            )}

            {/* Choices for select/radio */}
            {(selectedField.type === 'select' || selectedField.type === 'radio') && (
              <div>
                <label className="block text-sm font-medium text-gray-700 mb-1">Choices (comma-separated)</label>
                <textarea
                  value={selectedField.choices?.join(', ') || ''}
                  onChange={(e) => {
                    const index = fields.findIndex((f) => f.name === selectedField.name);
                    const choices = e.target.value.split(',').map(c => c.trim()).filter(c => c);
                    updateField(index, { choices });
                  }}
                  rows={3}
                  className="w-full px-3 py-2 border border-gray-300 rounded-md focus:outline-none focus:ring-2 focus:ring-blue-500"
                  placeholder="Option 1, Option 2, Option 3"
                />
              </div>
            )}
          </div>
        </div>
      )}

      {/* Action Buttons */}
      <div className="flex gap-4">
        <button
          onClick={handlePreview}
          className="flex-1 px-4 py-2 text-sm font-medium text-gray-700 bg-white border border-gray-300 rounded-md hover:bg-gray-50"
        >
          Preview Form
        </button>
        <button
          onClick={handleSave}
          className="flex-1 px-4 py-2 text-sm font-medium text-white bg-blue-600 rounded-md hover:bg-blue-700"
        >
          Save Form
        </button>
      </div>
    </div>
  );
}

// Helper function to render field preview
function renderFieldPreview(field: FormField) {
  switch (field.type) {
    case 'text':
      return (
        <input
          type="text"
          placeholder={field.placeholder}
          className="w-full px-3 py-2 border border-gray-300 rounded-md"
        />
      );
    case 'number':
      return (
        <input
          type="number"
          placeholder={field.placeholder}
          className="w-full px-3 py-2 border border-gray-300 rounded-md"
        />
      );
    case 'date':
      return (
        <input
          type="date"
          className="w-full px-3 py-2 border border-gray-300 rounded-md"
        />
      );
    case 'textarea':
      return (
        <textarea
          placeholder={field.placeholder}
          rows={3}
          className="w-full px-3 py-2 border border-gray-300 rounded-md"
        />
      );
    case 'select':
      return (
        <select className="w-full px-3 py-2 border border-gray-300 rounded-md">
          <option value="">Select...</option>
          {field.choices?.map((choice) => (
            <option key={choice} value={choice}>
              {choice}
            </option>
          ))}
        </select>
      );
    case 'radio':
      return (
        <div className="space-y-2">
          {field.choices?.map((choice) => (
            <label key={choice} className="flex items-center gap-2">
              <input type="radio" name={field.name} value={choice} />
              <span>{choice}</span>
            </label>
          ))}
        </div>
      );
    case 'checkbox':
      return (
        <label className="flex items-center gap-2">
          <input type="checkbox" name={field.name} />
          <span>{field.label}</span>
        </label>
      );
    case 'file':
      return (
        <input
          type="file"
          className="w-full px-3 py-2 border border-gray-300 rounded-md"
        />
      );
    default:
      return <div className="text-gray-400">Unknown field type</div>;
  }
}
