import {Icon, IconName, Position, Tooltip} from '@blueprintjs/core';
import {Intent} from '@blueprintjs/core/lib/esm/common/intent';
import React, {FC, memo, useCallback, useEffect, useReducer, useState} from 'react';
import {
    AsyncStatus,
    Attribute,
    AttributeValue,
    FormMode,
    FormType,
    ProfileStatus,
    ProfileType,
    ProjectRequestStatus,
    Usage,
    UserRole,
} from 'src/types';
import {FormLayout} from 'src/ui/blocks/form-layout/form-layout';
import {HomePageParagraph} from 'src/ui/blocks/home-page-paragraph/home-page-paragraph';
import {LoadableView} from 'src/ui/blocks/loadable-view/loadable-view';
import {ProfileLists} from 'src/ui/blocks/profile-lists/profile-lists';
import {useCppwApiContext} from 'src/ui/contexts/cppw-api-context/cppw-api-context';
import {getFormattedStringOrLinkValue} from 'src/ui/utils/get-formatted-attribute-value';
import './profile.styl';

const renderTooltip = ({
    content,
    intent,
    icon,
}: {
    content: string;
    intent: Intent;
    icon: IconName;
}) => (
    <Tooltip content={content} position={Position.RIGHT} intent={intent}>
        <Icon icon={icon} className='profile__title-icon' intent={intent} iconSize={30} />
    </Tooltip>
);
const TITLES = {
    [ProfileType.User]: 'Профиль пользователя',
    [ProfileType.Organisation]: 'Профиль организации',
    [ProfileType.Activity]: 'Профиль активности',
    [ProfileType.Project]: 'Профиль проекта',
    [ProfileType.ProjectRequest]: 'Заявка на проект',
};

type ProfileState = {
    reqStatus: AsyncStatus;
    hasWarning: boolean;
    role?: UserRole;
    status: ProfileStatus;
    fields: Attribute[];
    // Comments, Stages or Posts
    firstList: any[];
    fieldsUserId: string;
    schemaContentId: string;
};
const initialState: ProfileState = {
    reqStatus: AsyncStatus.Initial,
    hasWarning: false,
    role: UserRole.Initial,
    status: ProfileStatus.Active,
    fields: [],
    firstList: [],
    fieldsUserId: '',
    schemaContentId: '',
};
const SET_NEW_INFO = 'SET_NEW_INFO';
const reducer = (state: any, action: {type: string; payload: any}) => {
    if (action.type === SET_NEW_INFO) {
        return {...state, ...action.payload};
    } else {
        return state;
    }
};

type Props = {
    id: string;
    profileType: ProfileType;
    modifyAllowed: boolean;
    setModifyAllowed: (modifyAllowed: boolean) => void;
    isEditing: boolean;
    setIsEditing: (isEditing: boolean) => void;
    isBlocked: boolean;
    setIsBlocked: (isBlocked: boolean) => void;
};
export const Profile: FC<Props> = memo(
    ({
        id,
        profileType,
        setIsEditing,
        isEditing,
        setIsBlocked,
        isBlocked,
        modifyAllowed,
        setModifyAllowed,
    }) => {
        const cppwApi = useCppwApiContext();

        const [profileState, dispatch] = useReducer(reducer, initialState);
        const {
            firstList,
            reqStatus,
            hasWarning,
            fields,
            fieldsUserId,
            schemaContentId,
            status,
        } = profileState;
        // для профиля пользователя - роль, для заявки на проект - статус.
        const [info, setInfo] = useState<any>('');

        const [errors, setErrors] = useState();

        const onSave = useCallback(
            (values, attributes) => {
                const req = attributes.reduce(
                    (res: {[k: string]: AttributeValue}, attribute: Attribute, index: number) => {
                        const {name, usage, realName} = attribute;
                        const value = values[index];

                        if (realName) {
                            // поле статическое
                            if (usage === Usage.Number) {
                                res[realName] = parseFloat(value);
                            } else if (usage === Usage.Checkbox) {
                                res[realName] = JSON.stringify(value);
                            } else {
                                res[realName] = value;
                            }
                            return res;
                        } else {
                            // поле динамическое
                            res.schemaContent[name] = value === undefined ? null : value;
                            return res;
                        }
                    },
                    {schemaContent: {}}
                );

                cppwApi &&
                    cppwApi
                        .saveProfile(id, profileType, {
                            ...req,
                            schemaContent: JSON.stringify(req.schemaContent),
                        })
                        .then(() => {
                            dispatch({
                                type: SET_NEW_INFO,
                                payload: {
                                    fields: attributes.map(
                                        (attribute: Attribute, index: number) => ({
                                            ...attribute,
                                            defaultValue: values[index],
                                        })
                                    ),
                                    hasWarning: false,
                                },
                            });
                            setIsEditing(false);
                        })
                        .catch(() => {
                            cppwApi &&
                                cppwApi.toaster.current &&
                                cppwApi.toaster.current.show({
                                    icon: 'error',
                                    intent: Intent.DANGER,
                                    message: 'Ошибка сохранения профиля.',
                                });
                        });
            },
            [id, profileType, cppwApi, setIsEditing]
        );
        const onCancel = useCallback(() => {
            setIsEditing(false);
        }, [setIsEditing]);

        useEffect(() => {
            if (
                reqStatus === AsyncStatus.Initial ||
                (reqStatus !== AsyncStatus.Pending && fieldsUserId !== id)
            ) {
                cppwApi &&
                    cppwApi.getProfile(
                        id,
                        profileType,
                        () => {
                            dispatch({
                                type: SET_NEW_INFO,
                                payload: {
                                    hasWarning: false,
                                    reqStatus: AsyncStatus.Pending,
                                    fieldsUserId: id,
                                },
                            });
                        },
                        ({
                            fields,
                            info,
                            firstList,
                            hasWarning,
                            schemaContentId,
                            modifyAllowed,
                            blocked,
                            status,
                        }) => {
                            dispatch({
                                type: SET_NEW_INFO,
                                payload: {
                                    hasWarning,
                                    reqStatus: AsyncStatus.Success,
                                    fields,
                                    firstList,
                                    schemaContentId,
                                    status,
                                },
                            });
                            setIsBlocked(blocked);
                            setModifyAllowed(modifyAllowed);
                            setInfo(info);
                        },
                        () => {
                            dispatch({
                                type: SET_NEW_INFO,
                                payload: {
                                    reqStatus: AsyncStatus.Error,
                                },
                            });
                        }
                    );
            }
        }, [
            setIsBlocked,
            hasWarning,
            cppwApi,
            id,
            reqStatus,
            profileType,
            fieldsUserId,
            setModifyAllowed,
        ]);

        return (
            <LoadableView
                status={reqStatus}
                errorTitle={'Ошибка загрузки профиля'}
                spinnerClassName='spinner-full-height'
            >
                <div className='profile'>
                    {isEditing ? (
                        <>
                            <FormLayout
                                id={id}
                                // Ставим какой-то, чтобы удовлетворить TS. Использоваться не будет.
                                type={FormType.Questionnaire}
                                title={'Редактирование профиля'}
                                attributes={fields.filter(({usage}: Attribute) => !!usage)}
                                mode={FormMode.Fill}
                                buttonName='Сохранить'
                                shouldIgnoreSaveData={true}
                                errors={errors}
                                onFormSubmit={onSave}
                                onSetError={setErrors}
                                handleCancel={onCancel}
                                index={-1}
                            />
                        </>
                    ) : (
                        <>
                            <div className='profile__title'>
                                <HomePageParagraph title={TITLES[profileType]} mode={1} />
                                {hasWarning && !isBlocked && (
                                    <Tooltip
                                        content='Необходимо отредактировать профиль.'
                                        position={Position.RIGHT}
                                        intent={Intent.WARNING}
                                    >
                                        <Icon
                                            icon='warning-sign'
                                            className='profile__title-icon'
                                            intent={Intent.WARNING}
                                            iconSize={30}
                                        />
                                    </Tooltip>
                                )}
                                {isBlocked
                                    ? renderTooltip({
                                          content: 'Профиль заблокирован',
                                          intent: Intent.DANGER,
                                          icon: 'error',
                                      })
                                    : profileType === ProfileType.ProjectRequest &&
                                      renderTooltip(
                                          info === ProjectRequestStatus.Accepted
                                              ? {
                                                    content: 'Заявка подтверждена!',
                                                    intent: Intent.SUCCESS,
                                                    icon: 'tick-circle',
                                                }
                                              : info === ProjectRequestStatus.Rejected
                                              ? {
                                                    content: 'Заявка отклонена.',
                                                    intent: Intent.DANGER,
                                                    icon: 'error',
                                                }
                                              : {
                                                    content: 'Заявка находится на рассмотрении.',
                                                    intent: Intent.PRIMARY,
                                                    icon: 'info-sign',
                                                }
                                      )}
                                {status &&
                                    profileType === ProfileType.User &&
                                    renderTooltip({
                                        content: status,
                                        intent: Intent.PRIMARY,
                                        icon: 'info-sign',
                                    })}
                            </div>
                            {fields.map(({title, defaultValue, usage, name}: Attribute) => {
                                const value =
                                    !defaultValue || usage === Usage.Stage
                                        ? ''
                                        : getFormattedStringOrLinkValue(defaultValue, usage);
                                if (!value) {
                                    return null;
                                }

                                return (
                                    <div className='profile__item' key={name}>
                                        <div className='profile__item-title'>{title}:</div>
                                        <div className='profile__item-value'>{value}</div>
                                    </div>
                                );
                            })}
                            <ProfileLists
                                profileType={profileType}
                                id={id}
                                info={info}
                                firstList={firstList}
                                setInfo={setInfo}
                                schemaContentId={schemaContentId}
                                modifyAllowed={modifyAllowed}
                            />
                        </>
                    )}
                </div>
            </LoadableView>
        );
    }
);
