import {Icon, IconName, Intent, Position, Tooltip} from '@blueprintjs/core';
import {Color} from 'csstype';
import React, {FC, memo, useCallback, useEffect, useState} from 'react';
import {CppwApi} from 'src/api/api';
import {addNewUserForm, changeUserFormMode} from 'src/store/actions/user-forms.actions';
import {Attribute, AttributeValue, Content, Form, FormMode, FormType} from 'src/types';
import {FormLayout} from 'src/ui/blocks/form-layout/form-layout';
import {SuccessView} from 'src/ui/blocks/success-view/success-view';
import {useCppwApiContext} from 'src/ui/contexts/cppw-api-context/cppw-api-context';
import {DEFAULT_FORM_ID} from 'src/ui/utils/constants';
import './form-card.styl';

type Props = {
    form?: Form;
    title?: string;
    onClick: () => void;
    isPlaceholder?: boolean;
    index?: number;
    isArchive?: boolean;
    isActive?: boolean;
    isEditing?: boolean;
};
const OPTIONS = [
    {
        name: 'Редактировать',
        onClick: (form: Form, cppwApi?: CppwApi, index?: number) => {
            cppwApi &&
                Number(index) > -1 &&
                cppwApi.store.dispatch(changeUserFormMode(Number(index), FormMode.Edit));
        },
        forArchive: false,
    },
    {
        name: 'Дублировать',
        onClick: (form: Form, cppwApi?: CppwApi) => {
            if (cppwApi) {
                const {type, title, attributes, description, buttonName} = form;
                cppwApi.store.dispatch(
                    addNewUserForm({
                        id: DEFAULT_FORM_ID,
                        title: `${title} (2)`,
                        type,
                        attributes,
                        description,
                        buttonName,
                        mode: FormMode.Edit,
                    })
                );
            }
        },
        forArchive: false,
    },
    {
        name: 'Сделать формой по умолчанию',
        onClick: (form: Form, cppwApi?: CppwApi) => {
            cppwApi && cppwApi.updateDefaultForm(form);
        },
        forArchive: false,
    },
    {
        name: 'Статистика',
        onClick: (form: Form, cppwApi?: CppwApi, index?: number) => {
            cppwApi &&
                index !== undefined &&
                index > -1 &&
                cppwApi.store.dispatch(changeUserFormMode(index, FormMode.Stats));
        },
        forArchive: false,
    },
    {
        name: 'В архив',
        onClick: (form: Form, cppwApi?: CppwApi, index?: number) => {
            if (cppwApi && index !== undefined) {
                cppwApi.addToArchive(index, form);
            }
        },
        forArchive: false,
    },
    {
        name: 'В активные формы',
        onClick: (form: Form, cppwApi?: CppwApi, index?: number) => {
            if (cppwApi && index !== undefined) {
                cppwApi.deleteFromArchive(index, form);
            }
        },
        forArchive: true,
    },
];

const FORM_TYPE_TO_NAME_MAP: {[k in FormType]: string} = {
    [FormType.Help]: 'Помощь',
    [FormType.StudentRegistration]: 'Регистрация студента',
    [FormType.WorkerRegistration]: 'Регистрация работника',
    [FormType.UserAuthorization]: 'Авторизация пользователя',
    [FormType.StudentProfileTemplate]: 'Профиль студента',
    [FormType.UserRegistration]: 'Регистрация пользователя',
    [FormType.UserProfileTemplate]: 'Профиль пользователя',
    [FormType.OrgProfile]: 'Профиль организации',
    [FormType.Questionnaire]: 'Опросник',
    [FormType.ProjectRequest]: 'Создание заявки на проект',
    [FormType.Activity]: 'Создание активности',

    // Не должны появляться в списках но на всякий случай, чтоб ничего не поломалось.
    [FormType.AttributeCreator]: 'Создание атрибута',
    [FormType.Project]: 'Создание проекта',
    [FormType.UserProfile]: 'Профиль пользователя',
    [FormType.OrgProfileTemplate]: 'Профиль организации',
    [FormType.StudentProfile]: 'Профиль студента',
    [FormType.Initial]: '',
};
const FORM_TYPE_TO_ICON_MAP: {[k in FormType]: IconName} = {
    [FormType.Help]: 'headset',
    [FormType.StudentRegistration]: 'following',
    [FormType.WorkerRegistration]: 'following',
    [FormType.UserAuthorization]: 'following',
    [FormType.StudentProfileTemplate]: 'mugshot',
    [FormType.UserRegistration]: 'following',
    [FormType.UserProfileTemplate]: 'mugshot',
    [FormType.OrgProfile]: 'people',
    [FormType.Questionnaire]: 'property',
    [FormType.ProjectRequest]: 'annotation',
    [FormType.Activity]: 'globe-network',

    // Не должны появляться в списках но на всякий случай, чтоб ничего не поломалось.
    [FormType.AttributeCreator]: 'clipboard',
    [FormType.Project]: 'clipboard',
    [FormType.UserProfile]: 'clipboard',
    [FormType.OrgProfileTemplate]: 'clipboard',
    [FormType.StudentProfile]: 'clipboard',
    [FormType.Initial]: 'clipboard',
};
const FORM_TYPE_TO_COLOR_MAP: {[k in FormType]: Color} = {
    [FormType.Help]: '#106BA3',
    [FormType.StudentRegistration]: '#0D8050',
    [FormType.WorkerRegistration]: '#0D8050',
    [FormType.UserAuthorization]: '#0D8050',
    [FormType.StudentProfileTemplate]: '#0D8050',
    [FormType.UserRegistration]: '#BF7326',
    [FormType.UserProfileTemplate]: '#BF7326',
    [FormType.OrgProfile]: '#752F75',
    [FormType.Questionnaire]: '#106BA3',
    [FormType.ProjectRequest]: '#106BA3',
    [FormType.Activity]: '#106BA3',

    // Не должны появляться в списках но на всякий случай, чтоб ничего не поломалось.
    [FormType.AttributeCreator]: '#106BA3',
    [FormType.Project]: '#106BA3',
    [FormType.UserProfile]: '#106BA3',
    [FormType.OrgProfileTemplate]: '#106BA3',
    [FormType.StudentProfile]: '#106BA3',
    [FormType.Initial]: '#106BA3',
};

export const FormCard: FC<Props> = memo(
    ({form, title, onClick, isPlaceholder, isArchive, isActive, isEditing, index}) => {
        const cppwApi = useCppwApiContext();
        const [isMenuOpen, setIsMenuOpen] = useState(false);
        const onIconClick = useCallback(() => {
            setIsMenuOpen(!isMenuOpen);
        }, [isMenuOpen]);

        useEffect(() => {
            if (!isActive) {
                setIsMenuOpen(false);
            }
        }, [isActive]);

        if (isPlaceholder) {
            return (
                <div className='form-card _placeholder' onClick={onClick}>
                    <Icon icon='plus' />
                </div>
            );
        }
        return (
            <>
                <div
                    className={`form-card ${isActive ? '_is-active' : ''} ${
                        isEditing ? '_is-editing' : ''
                    }`}
                    onClick={onClick}
                >
                    {form && FORM_TYPE_TO_ICON_MAP[form.type] && (
                        <Tooltip
                            content={FORM_TYPE_TO_NAME_MAP[form.type]}
                            position={Position.BOTTOM}
                        >
                            <Icon
                                className='form-card__left-icon'
                                color={FORM_TYPE_TO_COLOR_MAP[form.type]}
                                icon={FORM_TYPE_TO_ICON_MAP[form.type]}
                                title={form.type}
                            />
                        </Tooltip>
                    )}
                    <div className='form-card__text'> {title}</div>
                    <Icon
                        icon='chevron-down'
                        className='form-card__right-icon'
                        onClick={onIconClick}
                    />
                </div>
                <div
                    className={`form-card__menu ${isMenuOpen ? '_opened' : ''} ${
                        isArchive ? '_is-archive' : ''
                    }`}
                >
                    {OPTIONS.map(({name, onClick, forArchive}) => {
                        if (forArchive === undefined || forArchive === isArchive) {
                            return (
                                <div
                                    key={`${name} ${title}`}
                                    className='form-card__menu-item'
                                    onClick={() => {
                                        onClick && form && onClick(form, cppwApi, index);
                                    }}
                                >
                                    {name}
                                </div>
                            );
                        }
                        return null;
                    })}
                </div>
            </>
        );
    }
);

type FormCardProps = {
    id: string;
    title: string;
    type: FormType;
    attributes: Attribute[];
    mode: FormMode;
    description?: string;
    buttonName?: string;
    content?: Content;
};
export const FormCard2: FC<FormCardProps> = memo(
    ({id, title, type, attributes, mode, buttonName, content}) => {
        const cppwApi = useCppwApiContext();
        const [isMenuOpen, setIsMenuOpen] = useState(false);
        const [isFilledSuccessfully, setIsFilledSuccessfully] = useState(false);
        const [errors, setErrors] = useState();
        const onClick = useCallback(() => {
            if (!content) {
                setIsMenuOpen(!isMenuOpen);
            }
        }, [content, isMenuOpen]);
        const onFormSubmit = useCallback(
            (values: AttributeValue[], attributes: Attribute[]) => {
                const content = attributes.reduce(
                    (res: {[k: string]: AttributeValue}, attribute: Attribute, index: number) => {
                        res[attribute.name] = values[index];
                        return res;
                    },
                    {}
                );
                cppwApi &&
                    cppwApi.submitQuestionnaire(id, JSON.stringify(content), () => {
                        setIsFilledSuccessfully(true);
                    });
            },
            [id, cppwApi]
        );

        return (
            <>
                <div className={`form-card`} onClick={onClick}>
                    {
                        <Icon
                            className='form-card__left-icon'
                            color={FORM_TYPE_TO_COLOR_MAP[type]}
                            icon={FORM_TYPE_TO_ICON_MAP[type]}
                            title={type}
                        />
                    }
                    <div className='form-card__text'> {title}</div>
                    <Icon
                        icon={content ? 'tick' : 'cross'}
                        intent={content ? Intent.SUCCESS : Intent.DANGER}
                        className='form-card__right-icon'
                    />
                </div>
                {isMenuOpen && !isFilledSuccessfully && (
                    <div className={`form-card__men`}>
                        <FormLayout
                            id={id}
                            type={type}
                            buttonName={buttonName}
                            title={title}
                            attributes={attributes}
                            mode={mode}
                            onSetError={setErrors}
                            errors={errors}
                            onFormSubmit={onFormSubmit}
                            index={-1}
                        />
                    </div>
                )}
                {isMenuOpen && isFilledSuccessfully && (
                    <SuccessView title='Опрос пройден! Спасибо!' />
                )}
            </>
        );
    }
);

type LiteProps = {
    name: string;
    onClick: () => void;
};
export const FormCardLite: FC<LiteProps> = memo(({name, onClick}) => (
    <div className={`form-card`} onClick={onClick}>
        {FORM_TYPE_TO_ICON_MAP[FormType.Questionnaire] && (
            <Tooltip
                content={FORM_TYPE_TO_NAME_MAP[FormType.Questionnaire]}
                position={Position.BOTTOM}
            >
                <Icon
                    className='form-card__left-icon'
                    color={FORM_TYPE_TO_COLOR_MAP[FormType.Questionnaire]}
                    icon={FORM_TYPE_TO_ICON_MAP[FormType.Questionnaire]}
                    title={FormType.Questionnaire}
                />
            </Tooltip>
        )}
        <div className='form-card__text'> {name}</div>
    </div>
));
