import {Card, FormGroup, H3, Icon, Intent} from '@blueprintjs/core';
import React, {FC, memo, useCallback, useMemo, useState} from 'react';
import {useDispatch} from 'react-redux';
import {
    addDraftFormAttributeProperties,
    deleteDraftFormAttribute,
} from 'src/store/actions/user-forms.actions';
import {
    ApiResponse,
    Attribute,
    AttributeRef,
    AttributeValue,
    Form,
    FormMode,
    Usage,
} from 'src/types';
import {OrganisationInputView} from 'src/ui/blocks/form-input/form-organisation-input';
import {StageInputView} from 'src/ui/blocks/form-input/form-stage-input/form-stage-input';
import {TaskInputView} from 'src/ui/blocks/form-input/form-stage-input/form-task-input';
import {StudentInputView} from 'src/ui/blocks/form-input/form-student-suggest-input';
import {ValidatorInputView} from 'src/ui/blocks/form-input/form-validator-input';
import {FormLayout} from 'src/ui/blocks/form-layout/form-layout';
import {HintLabel} from 'src/ui/blocks/hint-label/hint-label';
import {useCppwApiContext} from 'src/ui/contexts/cppw-api-context/cppw-api-context';
import {DEFAULT_ATTRIBUTE_ID} from 'src/ui/utils/constants';
import {USAGE_TO_FORM_MAP} from './constants/usage-to-schema-map';
import {AddableInputView} from './form-addable-input';
import {CheckboxInputView} from './form-checkbox-input';
import {DateInputView} from './form-date-input';
import {EmailInputView} from './form-email-input';
import {FileInputView} from './form-file-input';
import './form-input.styl';
import {LongTextInputView} from './form-long-text-input';
import {SearchInputView} from './form-mentor-suggest-input';
import {NumberInputView} from './form-number-input';
import {PasswordInputView} from './form-password-input';
import {RadioInputView} from './form-radio-input';
import {SelectInputView} from './form-select-input';
import {ShortTextInputView} from './form-short-text-input';
import {SwitchInputView} from './form-switch-input';
import {SwitchTableInputView} from './form-switch-table-input';

import {TelInputView} from './form-tel-input';

const USAGE_TO_INPUT_MAP: {
    [k in Usage]: FC<{
        inputRef: (ref: AttributeValue) => void;
        savedValue?: any;
        id?: string;
        intent: Intent;
        requestVariants?: () => Promise<ApiResponse>;
        placeholder?: string;
        defaultValue?: AttributeValue;
        disabled?: boolean;
        variants?: string[];
        max?: number;
        min?: number;
    }>;
} = {
    [Usage.ShortText]: ShortTextInputView,
    [Usage.Email]: EmailInputView,
    [Usage.Password]: PasswordInputView,
    [Usage.Number]: NumberInputView,
    [Usage.LongText]: LongTextInputView,
    [Usage.File]: FileInputView,
    [Usage.Checkbox]: CheckboxInputView,
    [Usage.Date]: DateInputView,
    [Usage.Radio]: RadioInputView,
    [Usage.StudentGroup]: SelectInputView,
    [Usage.Activity]: SelectInputView,
    [Usage.Stage]: StageInputView,
    // @ts-ignore
    [Usage.Mentor]: SearchInputView,
    [Usage.SwitchTable]: SwitchTableInputView,
    [Usage.Student]: StudentInputView,
    [Usage.Switch]: SwitchInputView,
    [Usage.Variants]: AddableInputView,
    [Usage.Validator]: ValidatorInputView,
    [Usage.Task]: TaskInputView,
    [Usage.Organisation]: OrganisationInputView,
    [Usage.Tel]: TelInputView,
};

const getSwitchTableFromVariants = (data: string[], type: string) => {
    let i: number = 0;
    let result: string[] = [];

    if (!data) {
        return [];
    }

    while (data[i++] !== type + 'Start') {
        if (!data[i]) {
            return [];
        }
    }

    while (data[i] !== type + 'End') {
        result.push(data[i++]);
        if (!data[i]) {
            return [];
        }
    }

    return result;
};
const addDefaultValuesToForm = (form: Form, values: {[k: string]: AttributeValue}) => {
    form.attributes.forEach((attr: Attribute, index: number) => {
        if (attr.name in values) {
            form.attributes[index].defaultValue = values[attr.name];
        } else if (attr.name === 'columns') {
            form.attributes[index].defaultValue = getSwitchTableFromVariants(
                values['variants'],
                attr.name
            );
        } else if (attr.name === 'rows') {
            form.attributes[index].defaultValue = getSwitchTableFromVariants(
                values['variants'],
                attr.name
            );
        }
    });
    return form;
};

type Props = Attribute & {
    inputRef: (ref: AttributeRef) => void;
    savedValue?: string | Array<string> | undefined;
    // Приходит отрицательным (-1) для форм вне списка форм пользователя.
    // Фактически запрещает редактирование
    formIndex: number;

    index: number;
    error?: string;
    onBlur?: () => void;
    onChange?: () => void;
    isEditable?: boolean;
    isDeletable?: boolean;
    isPlaceholder?: boolean;
    disabled?: boolean;

    isAddable?: boolean;
    isAdded?: boolean;
    triggerIsAdded?: () => void;

    clearRef?: (handleDelete: () => void) => void;
    formMode?: FormMode;
};

export const FormInput: FC<Props> = memo(
    ({
        formIndex,
        index,
        id,
        mandatory,
        title,
        hint,
        isEditable,
        isDeletable,
        error,
        usage,
        name,
        isPlaceholder,
        savedValue,
        hasOtherVariant,
        description,
        placeholder,
        min,
        max,
        variants,
        validators,
        inputRef,
        disabled,
        defaultValue,
        isAddable,
        isAdded: initialIsAdded,
        triggerIsAdded,
        formMode,
        ...props
    }) => {
        const dispatch = useDispatch();
        const cppwApi = useCppwApiContext();
        const [errors, setErrors] = useState();
        const [isAdded, setIsAdded] = useState(initialIsAdded);
        const handleIsAdded = useCallback(() => {
            setIsAdded(!isAdded);
            triggerIsAdded && triggerIsAdded();
        }, [setIsAdded, triggerIsAdded, isAdded]);

        const labelInfo = useMemo(
            () => (
                <span>
                    <span className='label-info'>{mandatory ? '*' : ''}</span>
                    {hint ? <HintLabel text={hint} /> : null}
                </span>
            ),
            [mandatory, hint]
        );
        const label = useMemo(
            () => (
                <>
                    {isAddable ? (
                        <Icon
                            className='form-input__label-icon'
                            icon={isAdded ? 'small-tick' : 'small-plus'}
                            intent={isAdded ? Intent.SUCCESS : Intent.DANGER}
                            onClick={handleIsAdded}
                        />
                    ) : null}
                    {title}
                </>
            ),
            [title, isAddable, isAdded, handleIsAdded]
        );
        const intent = error ? Intent.DANGER : Intent.NONE;
        const Component = USAGE_TO_INPUT_MAP[usage];

        const onSaveAttribute = useCallback(
            (values, attributes) => {
                const newAttribute = attributes
                    ? attributes.reduce(
                          (res: {[k: string]: AttributeValue}, item: Attribute, ind: number) => {
                              if (values && ind in values && values[ind] !== undefined) {
                                  if (item.usage === Usage.Number) {
                                      res[item.name] = parseFloat(values[ind]);
                                  } else if (item.usage === Usage.Variants) {
                                      if (!res['variants']) {
                                          res['variants'] = [];
                                      }
                                      if (item.name === 'columns' || item.name === 'rows') {
                                          res['variants'].push(item.name + 'Start');

                                          values[ind].forEach((item: string) =>
                                              res['variants'].push(item)
                                          );

                                          res['variants'].push(item.name + 'End');
                                      } else {
                                          res[item.name] = values[ind];
                                      }
                                  } else {
                                      res[item.name] = values[ind];
                                  }
                              }
                              return res;
                          },
                          {usage}
                      )
                    : {};

                if (index !== undefined) {
                    newAttribute.isPlaceholder = false;
                    cppwApi &&
                        formIndex > -1 &&
                        cppwApi.saveAttribute(id, index, newAttribute, formIndex);
                }
            },
            [cppwApi, formIndex, id, index, usage]
        );
        const onCancelAttributeEdit = useCallback(() => {
            if (index !== undefined && formIndex > -1) {
                if (id === DEFAULT_ATTRIBUTE_ID) {
                    dispatch(deleteDraftFormAttribute(formIndex, index));
                } else {
                    dispatch(
                        addDraftFormAttributeProperties(formIndex, index, {
                            isPlaceholder: false,
                        })
                    );
                }
            }
        }, [dispatch, id, index, formIndex]);
        const onSetError = useCallback(
            (newErrors) => {
                setErrors(newErrors);
            },
            [setErrors]
        );
        const onEditAttribute = useCallback(() => {
            if (index !== undefined) {
                if (formIndex > -1) {
                    dispatch(
                        addDraftFormAttributeProperties(formIndex, index, {
                            isPlaceholder: !isPlaceholder,
                        })
                    );
                }
            }
        }, [formIndex, isPlaceholder, dispatch, index]);
        const onDeleteAttribute = useCallback(() => {
            if (index !== undefined && formIndex > -1) {
                dispatch(deleteDraftFormAttribute(formIndex, index));
            }
        }, [index, formIndex, dispatch]);
        const onUpload = useCallback(
            () => (cppwApi ? cppwApi.get('formatted/activities/not-finished') : Promise.reject()),
            [cppwApi]
        );

        if (isPlaceholder && formIndex > -1) {
            const form = USAGE_TO_FORM_MAP[usage];
            return form ? (
                <Card className='form-input__plus'>
                    <FormLayout
                        index={-1}
                        {...addDefaultValuesToForm(form, {
                            title,
                            hint,
                            name,
                            mandatory,
                            placeholder,
                            description,
                            min,
                            max,
                            variants,
                            validators,
                            hasOtherVariant,
                        })}
                        HeaderComponent={H3}
                        mode={FormMode.Fill}
                        errors={errors}
                        onSetError={onSetError}
                        handleCancel={onCancelAttributeEdit}
                        onFormSubmit={onSaveAttribute}
                    />
                </Card>
            ) : null;
        }

        return Component ? (
            <FormGroup
                key={id}
                className='form-input'
                label={label}
                labelFor={id}
                labelInfo={labelInfo}
                helperText={error}
                intent={intent}
            >
                <Component
                    intent={intent}
                    requestVariants={usage === Usage.Activity ? onUpload : undefined}
                    inputRef={inputRef}
                    placeholder={placeholder}
                    defaultValue={defaultValue}
                    savedValue={savedValue}
                    disabled={disabled}
                    variants={variants}
                    max={max}
                    min={min}
                    // @ts-ignore
                    disableLoading={formMode === FormMode.View}
                    {...props}
                />
                {isEditable && Boolean(USAGE_TO_FORM_MAP[usage]) && (
                    <Icon className='form-input__cog' icon='cog' onClick={onEditAttribute} />
                )}
                {isDeletable && (
                    <Icon className='form-input__cross' icon='cross' onClick={onDeleteAttribute} />
                )}
            </FormGroup>
        ) : null;
    }
);
