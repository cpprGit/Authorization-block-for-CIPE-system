import {Icon} from '@blueprintjs/core';
import React, {FC, memo, useCallback, useEffect, useMemo, useState} from 'react';
import {useDispatch, useSelector} from 'react-redux';
import {logout} from 'src/store/actions/user.actions';
import {State} from 'src/store/reducers';
import {ProfileType, UserRole} from 'src/types';
import {useCppwApiContext} from 'src/ui/contexts/cppw-api-context/cppw-api-context';
import './profile-column.styl';

enum PermissionType {
    Edit,
    Block,
    Complaint,
    Marks,
    Notifications,
    Complaints,

    Questionnaires,
    ProjectRequests,
    ProjectApplicationsForProjectProfile,
    ProjectApplicationsForStudentProfile,
    ProjectApplicationsForActivityProfile,
}

const TABS = [
    {
        type: PermissionType.Complaints,
        title: 'Жалобы',
        index: 3,
    },
    {
        type: PermissionType.ProjectRequests,
        title: 'Заявки на проекты',
        index: 4,
    },
    {
        type: PermissionType.ProjectApplicationsForProjectProfile,
        title: 'Заявки студентов',
        index: 5,
    },
    {
        type: PermissionType.ProjectApplicationsForStudentProfile,
        title: 'Заявки на проекты',
        index: 6,
    },
    {
        type: PermissionType.ProjectApplicationsForActivityProfile,
        title: 'Выбор проектов',
        index: 7,
    },
    {
        type: PermissionType.Questionnaires,
        title: 'Опросники',
        index: 8,
    },
];

const getPermission = (
    type: PermissionType,
    {
        userRole,
        profileId,
        userId,
        profileType,
        modifyAllowed,
    }: {
        userRole?: UserRole;
        profileId: string;
        userId?: string;
        profileType: ProfileType;
        modifyAllowed: boolean;
    }
) => {
    switch (type) {
        case PermissionType.Edit: {
            switch (profileType) {
                case ProfileType.Activity: {
                    return userRole && [UserRole.Supervisor, UserRole.Manager].includes(userRole);
                }
                case ProfileType.User: {
                    return (
                        (userRole && [UserRole.Supervisor, UserRole.Manager].includes(userRole)) ||
                        profileId === userId
                    );
                }
                case ProfileType.Organisation: {
                    return (
                        (userRole && [UserRole.Supervisor, UserRole.Manager].includes(userRole)) ||
                        modifyAllowed
                    );
                }
                case ProfileType.Project: {
                    return (
                        (userRole && [UserRole.Supervisor, UserRole.Manager].includes(userRole)) ||
                        modifyAllowed
                    );
                }
                case ProfileType.ProjectRequest: {
                    return (
                        (userRole &&
                            [
                                UserRole.Supervisor,
                                UserRole.Manager,
                                UserRole.AcademicManager,
                            ].includes(userRole)) ||
                        modifyAllowed
                    );
                }
                default:
                    return false;
            }
        }
        case PermissionType.Block: {
            switch (profileType) {
                case ProfileType.Organisation:
                case ProfileType.User: {
                    return userRole === UserRole.Supervisor && profileId !== userId;
                }
                default:
                    return false;
            }
        }
        case PermissionType.Complaint: {
            switch (profileType) {
                case ProfileType.Organisation:
                case ProfileType.User: {
                    return userRole !== UserRole.Supervisor && profileId !== userId;
                }
                default:
                    return false;
            }
        }
        case PermissionType.Complaints: {
            switch (profileType) {
                case ProfileType.User: {
                    return userRole === UserRole.Supervisor && profileId === userId;
                }
                default:
                    return false;
            }
        }
        case PermissionType.Marks: {
            switch (profileType) {
                case ProfileType.Activity: {
                    return (
                        userRole &&
                        [
                            UserRole.Supervisor,
                            UserRole.Manager,
                            UserRole.AcademicManager,
                            UserRole.OfficeManager,
                        ].includes(userRole)
                    );
                }
                case ProfileType.Project: {
                    return (
                        (userRole &&
                            [
                                UserRole.Supervisor,
                                UserRole.Manager,
                                UserRole.AcademicManager,
                                UserRole.OfficeManager,
                            ].includes(userRole)) ||
                        modifyAllowed
                    );
                }
                default:
                    return false;
            }
        }
        case PermissionType.Questionnaires:
        case PermissionType.Notifications: {
            switch (profileType) {
                case ProfileType.User: {
                    return userId === profileId;
                }
                default:
                    return false;
            }
        }
        case PermissionType.ProjectRequests: {
            switch (profileType) {
                case ProfileType.User: {
                    return userRole && [UserRole.Mentor].includes(userRole) && userId === profileId;
                }
                default:
                    return false;
            }
        }
        case PermissionType.ProjectApplicationsForProjectProfile: {
            switch (profileType) {
                case ProfileType.Project: {
                    return (
                        userRole &&
                        [
                            UserRole.Mentor,
                            UserRole.Supervisor,
                            UserRole.Manager,
                            UserRole.AcademicManager,
                            UserRole.OfficeManager,
                        ].includes(userRole) &&
                        modifyAllowed
                    );
                }
                default:
                    return false;
            }
        }
        case PermissionType.ProjectApplicationsForStudentProfile: {
            switch (profileType) {
                case ProfileType.User: {
                    return (
                        userRole && [UserRole.Student].includes(userRole) && userId === profileId
                    );
                }
                default:
                    return false;
            }
        }
        case PermissionType.ProjectApplicationsForActivityProfile: {
            switch (profileType) {
                case ProfileType.Activity: {
                    return userRole && [UserRole.Student].includes(userRole);
                }
                default:
                    return false;
            }
        }
        default:
            return false;
    }
};

type Props = {
    profileType: ProfileType;
    id: string;
    selectedIndex: number;
    setSelectedIndex: (selectedIndex: number) => void;
    setIsEditing?: (isEditing: boolean) => void;
    isBlocked?: boolean;
    setIsBlocked?: (isBlocked: boolean) => void;
    modifyAllowed: boolean;
};

export const ProfileColumn: FC<Props> = memo(
    ({
        selectedIndex,
        setSelectedIndex,
        profileType,
        setIsEditing,
        id,
        setIsBlocked,
        isBlocked,
        modifyAllowed,
    }) => {
        const {userId: selfUserId, role} = useSelector((state: State) =>
            'userId' in state.user ? state.user : {userId: undefined, role: undefined}
        );
        const dispatch = useDispatch();
        const cppwApi = useCppwApiContext();
        const [isMenuOpen, setIsMenuOpen] = useState(false);
        const onIconClick = useCallback(() => {
            if (selectedIndex === 0) {
                setIsMenuOpen(!isMenuOpen);
            }
        }, [selectedIndex, isMenuOpen]);
        const select0 = useCallback(() => {
            setSelectedIndex(0);
        }, [setSelectedIndex]);
        const select1 = useCallback(() => {
            setSelectedIndex(1);
        }, [setSelectedIndex]);
        const select2 = useCallback(() => {
            setSelectedIndex(2);
        }, [setSelectedIndex]);
        const permissionOptions = useMemo(
            () => ({
                userRole: role,
                profileId: id,
                userId: selfUserId,
                profileType,
                modifyAllowed,
            }),
            [role, id, selfUserId, profileType, modifyAllowed]
        );
        const options = useMemo(
            () => [
                {
                    title: 'Редактировать',
                    onClick: () => {
                        setIsEditing && setIsEditing(true);
                    },
                    shouldShow: getPermission(PermissionType.Edit, permissionOptions),
                },
                {
                    title: isBlocked ? 'Разблокировать' : 'Заблокировать',
                    onClick: () => {
                        if (setIsBlocked) {
                            if (isBlocked) {
                                cppwApi &&
                                    cppwApi.unblockProfile(id, profileType, () => {
                                        setIsBlocked(false);
                                    });
                            } else {
                                cppwApi &&
                                    cppwApi.blockProfile(id, profileType, () => {
                                        setIsBlocked(true);
                                    });
                            }
                        }
                    },
                    shouldShow: getPermission(PermissionType.Block, permissionOptions),
                },
                {
                    title: 'Пожаловаться',
                    onClick: () => {
                        cppwApi && cppwApi.complaintProfile(id, profileType);
                    },
                    shouldShow: getPermission(PermissionType.Complaint, permissionOptions),
                },
                {
                    title: 'Выйти',
                    onClick: () => {
                        dispatch(logout());
                    },
                    shouldShow: id === selfUserId,
                },
            ],
            [
                permissionOptions,
                profileType,
                isBlocked,
                setIsBlocked,
                cppwApi,
                dispatch,
                id,
                selfUserId,
                setIsEditing,
            ]
        );

        useEffect(() => {
            setIsMenuOpen(false);
        }, [selectedIndex]);
        return (
            <>
                <div
                    className={`profile-column ${selectedIndex === 0 ? '_is-active' : ''}`}
                    key={0}
                    onClick={select0}
                >
                    <div className='profile-column__text'>Профиль</div>
                    <Icon
                        icon='chevron-down'
                        className='profile-column__right-icon'
                        onClick={onIconClick}
                    />
                </div>
                {selectedIndex === 0 && (
                    <div className={`profile-column__menu ${isMenuOpen ? '_opened' : ''}`}>
                        {options.map(({title, onClick, shouldShow}) => {
                            if (shouldShow) {
                                return (
                                    <div
                                        key={title}
                                        className='profile-column__menu-item'
                                        onClick={onClick}
                                    >
                                        {title}
                                    </div>
                                );
                            }
                            return null;
                        })}
                    </div>
                )}
                {getPermission(PermissionType.Notifications, permissionOptions) && (
                    <div
                        className={`profile-column ${selectedIndex === 1 ? '_is-active' : ''}`}
                        key={1}
                        onClick={select1}
                    >
                        <div className='profile-column__text'>Нотификации</div>
                    </div>
                )}
                {getPermission(PermissionType.Marks, permissionOptions) && (
                    <div
                        className={`profile-column ${selectedIndex === 2 ? '_is-active' : ''}`}
                        key={2}
                        onClick={select2}
                    >
                        <div className='profile-column__text'>Ведомость</div>
                    </div>
                )}
                {TABS.map(({type, title, index}) => {
                    return (
                        getPermission(type, permissionOptions) && (
                            <div
                                className={`profile-column ${
                                    selectedIndex === index ? '_is-active' : ''
                                }`}
                                key={index}
                                onClick={() => {
                                    setSelectedIndex(index);
                                }}
                            >
                                <div className='profile-column__text'>{title}</div>
                            </div>
                        )
                    );
                })}
            </>
        );
    }
);
