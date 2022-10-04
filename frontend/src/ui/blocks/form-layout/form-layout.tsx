import {Button, ButtonGroup, EditableText, H1, H2, H3, Intent} from '@blueprintjs/core';

import React, {FC, memo, useCallback, useEffect, useMemo, useState} from 'react';
import {useDispatch} from 'react-redux';
import {parseFields} from 'src/api/api-parser/form-parser';
import {
    addDraftFormAttributes,
    changeUserFormMode,
    deleteNewUserForm,
    editUserForm,
    setDraftFormButtonName,
    setDraftFormDescription,
    setDraftFormTitle,
} from 'src/store/actions/user-forms.actions';
import {
    AsyncStatus,
    Attribute,
    AttributeRef,
    AttributeValue,
    Form,
    FormMode,
    FormType,
} from 'src/types';
import {AttributeCreator} from 'src/ui/blocks/attribute-creator/attribute-creator';
import {FormInput} from 'src/ui/blocks/form-input/form-input';
import {FormStats} from 'src/ui/blocks/form-stats/form-stats';
import {useCppwApiContext} from 'src/ui/contexts/cppw-api-context/cppw-api-context';
import {validateAllAttributes} from 'src/ui/utils/form-input-validator/validator';
import './form-layout.styl';

type Props = Form & {
    mode: FormMode;
    errors?: string[];
    onSetError: (newErrors: string[]) => void;
    HeaderComponent?: typeof H1 | typeof H2 | typeof H3;
    shouldIgnoreSaveData?: boolean;
    onFormSubmit: (values: AttributeValue[], attributes: Attribute[]) => void;
    // Приходит отрицательным (-1) для форм вне списка форм пользователя.
    // Фактически запрещает редактирование
    index: number;
    backup?: Form;
    className?: string;
    handleCancel?: () => void;
};

export const FormLayout: FC<Props> = memo(
    ({
        id,
        attributes,
        title,
        type,
        description,
        buttonName,
        mode,
        backup,
        errors,
        onSetError,
        onFormSubmit,
        shouldIgnoreSaveData,
        index: formIndex,
        HeaderComponent = H1,
        className,
        handleCancel,
        stats,
    }) => {
        const dispatch = useDispatch();
        const cppwApi = useCppwApiContext();

        const refs = useMemo<AttributeRef[]>(() => attributes.map(() => null), [attributes]);
        const unparsedFormValues = useMemo<string | null>(
            () => localStorage.getItem(`ccpr_draft_form_${id}`),
            [id]
        );

        let saveFormValues: {[k: string]: string} = unparsedFormValues
            ? JSON.parse(unparsedFormValues)
            : {};

        const refsToAttributesIDs: Array<string> = [];
        const setInputRefs = useMemo<((elem: AttributeRef) => void)[]>(
            () =>
                attributes.map((val, index: number) => (elem: AttributeRef) => {
                    refs[index] = elem;
                }),
            // eslint-disable-next-line react-hooks/exhaustive-deps
            [attributes]
        );
        const savedValues = useMemo<(string | string[] | undefined)[]>(
            () =>
                attributes.map((val, index: number) => {
                    refsToAttributesIDs[index] = val.id;
                    if (saveFormValues) {
                        if (saveFormValues[val.id]) {
                            return saveFormValues[val.id];
                        }
                    }
                    return undefined;
                }),
            [attributes, refsToAttributesIDs, saveFormValues]
        );

        const [formType, setFormType] = useState(type || '');

        const onChangeTitle = useCallback(
            (value) => {
                if (formIndex > -1) {
                    dispatch(setDraftFormTitle(formIndex, value));
                }
            },
            [dispatch, formIndex]
        );
        const onChangeDescription = useCallback(
            (value) => {
                if (formIndex > -1) {
                    dispatch(setDraftFormDescription(formIndex, value));
                }
            },
            [dispatch, formIndex]
        );
        const onChangeButtonName = useCallback(
            (value) => {
                if (formIndex > -1) {
                    dispatch(setDraftFormButtonName(formIndex, value));
                }
            },
            [dispatch, formIndex]
        );
        const onEditFinished = useCallback(() => {
            if (attributes.some(({isPlaceholder}) => isPlaceholder)) {
                cppwApi &&
                    cppwApi.toaster.current &&
                    cppwApi.toaster.current.show({
                        icon: 'error',
                        intent: Intent.DANGER,
                        message: 'Пожалуйста, завершите редактирование всех полей формы.',
                    });
                return;
            }
            if (!formType) {
                cppwApi &&
                    cppwApi.toaster.current &&
                    cppwApi.toaster.current.show({
                        icon: 'error',
                        intent: Intent.DANGER,
                        message: 'Пожалуйста, задайте тип формы.',
                    });
                return;
            }
            if (formIndex > -1) {
                cppwApi &&
                    cppwApi.saveForm(
                        formIndex,
                        {
                            id: id,
                            attributes: [...attributes],
                            title,
                            description,
                            buttonName,
                            type: formType as FormType,
                        },
                        () => dispatch(changeUserFormMode(formIndex, FormMode.View))
                    );
            }
        }, [
            id,
            cppwApi,
            formIndex,
            formType,
            dispatch,
            attributes,
            title,
            description,
            buttonName,
        ]);
        const onCancel = useCallback(() => {
            if (backup) {
                dispatch(editUserForm(formIndex, backup));
            } else {
                dispatch(deleteNewUserForm(formIndex));
            }
        }, [dispatch, backup, formIndex]);
        const onSubmit = useCallback(() => {
            const values = refs.map((elem) => {
                return elem && elem.value;
            });
            console.log(values);
            const newErrors = validateAllAttributes(attributes, values);
            onSetError(newErrors);

            if (mode === FormMode.Fill && newErrors.every((elem) => !Boolean(elem))) {
                onFormSubmit(values, attributes);
                localStorage.removeItem(`ccpr_draft_form_${id}`);
                for (let i = 0; i < refsToAttributesIDs.length; i++) {
                    localStorage.removeItem(`ccpr_organisation_${refsToAttributesIDs[i]}`);
                }
            }
        }, [onFormSubmit, attributes, refs, mode, onSetError, id, refsToAttributesIDs]);

        const onChangeFormType = useCallback(
            (val) => {
                const newFormType = val.target.value;
                cppwApi &&
                    cppwApi
                        .get(`formatted/default-fields/${newFormType}`)
                        .then((res) => {
                            cppwApi &&
                                cppwApi.store.dispatch(
                                    addDraftFormAttributes(formIndex, parseFields(res))
                                );
                            setFormType(newFormType);
                        })
                        .catch((e) => {
                            console.error(e);
                            setFormType(FormType.Initial);
                            cppwApi.toaster.current &&
                                cppwApi.toaster.current.show({
                                    icon: 'error',
                                    intent: Intent.DANGER,
                                    message: 'Ошибка загрузки дефолтных атрибутов.',
                                });
                        });
            },
            [formIndex, cppwApi]
        );

        useEffect(() => {
            setFormType(type || '');
        }, [type]);
        useEffect(() => {
            const formValues: {[k: string]: string} = {};

            const timer: ReturnType<typeof setInterval> = setInterval(() => {
                refs.forEach((elem: AttributeRef, index) => {
                    if (elem) {
                        formValues[refsToAttributesIDs[index]] = elem.value;
                    }
                });

                localStorage.setItem(`ccpr_draft_form_${id}`, JSON.stringify(formValues));
            }, 2000);

            return () => clearInterval(timer);
        }, [attributes, id, refs, refsToAttributesIDs]);

        if (mode === FormMode.Stats && formIndex > -1) {
            return (
                <FormStats
                    formId={id}
                    status={stats ? stats.status : AsyncStatus.Initial}
                    records={stats ? stats.records : []}
                    fields={attributes}
                    formIndex={formIndex}
                />
            );
        }

        if (mode === FormMode.Edit && formIndex > -1) {
            return (
                <div className={`form-layout ${className ? className : ''}`}>
                    <div className={`bp3-select bp3-fill form-layout__select`}>
                        <select
                            disabled={attributes.length > 0}
                            value={formType}
                            onChange={onChangeFormType}
                        >
                            <option value={''}>Выберите тип формы</option>
                            <option value={FormType.Questionnaire}>Форма-опросник</option>
                            <option value={FormType.StudentRegistration}>
                                Форма регистрации для студентов
                            </option>
                            <option value={FormType.WorkerRegistration}>
                                Форма регистрации для работников
                            </option>
                            <option value={FormType.UserAuthorization}>
                                Форма авторизации пользователя
                            </option>
                            <option value={FormType.StudentProfileTemplate}>
                                Форма профиля для студентов
                            </option>
                            <option value={FormType.UserRegistration}>
                                Форма регистрации для НЕ студентов
                            </option>
                            <option value={FormType.UserProfileTemplate}>
                                Форма профиля для НЕ студентов
                            </option>
                            <option value={FormType.OrgProfile}>
                                Форма профиля для организаций
                            </option>
                            <option value={FormType.ProjectRequest}>Форма создания проекта</option>
                            <option value={FormType.Activity}>Форма активности</option>
                            <option value={FormType.Help}>Форма помощи</option>
                        </select>
                    </div>
                    {formType && (
                        <>
                            <H1>
                                <EditableText
                                    className={'form-layout__title'}
                                    onChange={onChangeTitle}
                                    placeholder='Наименование формы...'
                                    value={title}
                                />
                            </H1>
                            <EditableText
                                className={'form-layout__description _mode-edit'}
                                minLines={2}
                                multiline={true}
                                value={description}
                                placeholder='Описание формы...'
                                onChange={onChangeDescription}
                            />
                            {attributes.map((attribute, index) => (
                                <FormInput
                                    {...attribute}
                                    key={index}
                                    // кажется они тут не нужны
                                    inputRef={setInputRefs[index]}
                                    error={errors ? errors[index] : ''}
                                    isEditable={mode === FormMode.Edit}
                                    isDeletable={!attribute.realName}
                                    index={index}
                                    formIndex={formIndex}
                                    disabled={true}
                                />
                            ))}
                            <AttributeCreator className='form-layout__plus' formIndex={formIndex} />
                            <p>
                                <EditableText
                                    className='form-layout__button-name'
                                    onChange={onChangeButtonName}
                                    value={buttonName}
                                    placeholder='Надпись на кнопке отправки формы...'
                                />
                            </p>
                        </>
                    )}
                    <ButtonGroup className='form-layout__buttons'>
                        {formType && (
                            <Button
                                text='Сохранить'
                                intent={Intent.PRIMARY}
                                onClick={onEditFinished}
                            />
                        )}
                        <Button text='Отменить' onClick={onCancel} />
                    </ButtonGroup>
                </div>
            );
        }

        return (
            <div className={`form-layout ${className ? className : ''}`}>
                <HeaderComponent className='form-layout__title'>{title}</HeaderComponent>
                <p>{description}</p>
                {attributes.map((attribute, index) => (
                    <FormInput
                        formIndex={formIndex}
                        {...attribute}
                        id={refsToAttributesIDs[index]}
                        key={index}
                        inputRef={setInputRefs[index]}
                        savedValue={shouldIgnoreSaveData ? undefined : savedValues[index]}
                        error={errors ? errors[index] : ''}
                        index={index}
                        formMode={mode}
                    />
                ))}
                <ButtonGroup className='form-layout__buttons'>
                    <Button
                        text={buttonName || 'Отправить'}
                        intent={handleCancel ? Intent.PRIMARY : Intent.NONE}
                        onClick={onSubmit}
                    />
                    {handleCancel && <Button text='Отменить' onClick={handleCancel} />}
                </ButtonGroup>
            </div>
        );
    }
);
