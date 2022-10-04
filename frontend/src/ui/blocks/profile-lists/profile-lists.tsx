import {Button, ButtonGroup, Divider, Intent, Tab, Tabs} from '@blueprintjs/core';
import React, {FC, memo, useCallback, useEffect, useMemo, useRef, useState} from 'react';
import {useSelector} from 'react-redux';
import {Link} from 'react-router-dom';
import {State} from 'src/store/reducers';
import {
    AsyncStatus,
    AttributeRef,
    ProfileType,
    ProjectRequestStatus,
    Stage,
    Usage,
    UserAction,
    UserRole,
} from 'src/types';
import {CommentView} from 'src/ui/blocks/comment-view/comment-view';
import {FormInput} from 'src/ui/blocks/form-input/form-input';
import {ProfileList} from 'src/ui/blocks/profile-list/profile-list';
import {StageView} from 'src/ui/blocks/stage-view/stage-view';
import {TreeItem} from 'src/ui/blocks/tree-item/tree-item';
import {useCppwApiContext} from 'src/ui/contexts/cppw-api-context/cppw-api-context';
import './profile-lists.styl';

const SimpleCard: FC<{name: string; id: string; type: ProfileType}> = ({name, id, type}) => (
    <Link to={`/${type}?=${id}`} className='profile-lists__link'>
        {name}
    </Link>
);

type ProfileListDescriber = {
    title: string;
    path: string;
    component: FC<any>;
    userRoles?: UserRole[];
};

const LISTS: {[k in ProfileType]: ProfileListDescriber[]} = {
    [ProfileType.User]: [
        {
            title: 'Посты',
            path: '',
            component: CommentView,
        },
        {
            title: 'Активности',
            path: 'getAllStudentsActivities',
            component: SimpleCard,
            userRoles: [UserRole.Student],
        },
        {
            title: 'Активности',
            path: 'getAllMentorsActivities',
            component: SimpleCard,
            userRoles: [UserRole.Mentor],
        },
        {
            title: 'Проекты',
            path: 'getAllStudentsProjects',
            component: SimpleCard,
            userRoles: [UserRole.Student],
        },
        {
            title: 'Менторы',
            path: 'getAllStudentMentors',
            component: SimpleCard,
            userRoles: [UserRole.Student],
        },
        {
            title: 'Проекты',
            path: 'getAllUsersProjects',
            component: SimpleCard,
            userRoles: [UserRole.Mentor],
        },
        {
            title: 'Менти',
            path: 'getAllMentorMenties',
            component: SimpleCard,
            userRoles: [UserRole.Mentor],
        },
    ],
    [ProfileType.Organisation]: [
        {
            title: 'Посты',
            path: '',
            component: CommentView,
        },
        {
            title: 'Структура',
            path: 'getOrganisationStructure',
            component: TreeItem,
        },
        {
            title: 'Сотрудники',
            path: 'getOrganisationMembers',
            component: SimpleCard,
        },
    ],
    [ProfileType.Activity]: [
        {
            title: 'Этапы',
            path: '',
            component: StageView,
        },
        {
            title: 'Материалы',
            path: 'getAllPosts',
            component: CommentView,
        },
    ],
    [ProfileType.Project]: [
        {
            title: 'Этапы',
            path: '',
            component: StageView,
        },
        {
            title: 'Материалы',
            path: 'getAllPosts',
            component: CommentView,
        },
    ],
    [ProfileType.ProjectRequest]: [
        {
            title: 'Комментарии',
            path: '',
            component: CommentView,
        },
    ],
};

type Props = {
    schemaContentId: string;
    profileType: ProfileType;
    id: string;
    // Или массив постов
    firstList: Stage[];
    info: any;
    setInfo: (info: any) => void;
    modifyAllowed: boolean;
};
export const ProfileLists: FC<Props> = memo(
    ({schemaContentId, profileType, id, firstList, info, setInfo, modifyAllowed}) => {
        const cppwApi = useCppwApiContext();

        const {userId, role: userRole, name} = useSelector((state: State) =>
            state.user.isAuthed
                ? state.user
                : {
                      role: UserRole.Initial,
                      userId: '',
                      name: '',
                  }
        );

        const commentRef = useRef<AttributeRef>(null);
        const fileRef = useRef<{
            value: {id: string; name: string; type: 'file'} | null;
        }>(null);
        const fileClearStateRef = useRef<() => void>(() => {});
        const commentClearStateRef = useRef<() => void>(() => {});

        const [selectedTabId, setSelectedTabId] = useState(0);
        const validators = useMemo(() => [], []);
        const isPost = useMemo(
            () =>
                modifyAllowed &&
                LISTS[profileType] &&
                LISTS[profileType][selectedTabId] &&
                ['Материалы', 'Комментарии', 'Посты'].includes(
                    LISTS[profileType][selectedTabId].title
                ),
            [modifyAllowed, profileType, selectedTabId]
        );
        const [lists, setLists] = useState(
            LISTS[profileType].map(({path, component, title}, index) => ({
                status: index === 0 ? AsyncStatus.Success : AsyncStatus.Initial,
                items: index === 0 ? firstList : [],
                component,
                path: `${path}`,
                profileId: ['Материалы', 'Комментарии', 'Посты'].includes(title)
                    ? schemaContentId
                    : id,
            }))
        );
        const onChangeTab = useCallback((tabId: number) => setSelectedTabId(tabId), [
            setSelectedTabId,
        ]);
        const editCurrentList = useCallback(
            ({status, items}) => {
                const newLists = lists.map((list, index) => {
                    if (index === selectedTabId) {
                        return {...list, status, items};
                    }
                    return list;
                });
                setLists(newLists);
            },
            [lists, selectedTabId]
        );
        console.log(lists[selectedTabId].items);
        const comment = useCallback(
            (userAction, id) => {
                editCurrentList({
                    status: AsyncStatus.Success,
                    items: [
                        ...lists[selectedTabId].items,
                        {
                            id,
                            user: {
                                name: name,
                                id: userId,
                                type: 'user',
                            },
                            action: userAction,
                            text: commentRef.current ? commentRef.current.value : '',
                            file: fileRef.current ? fileRef.current.value : null,
                        },
                    ],
                });
                if (commentRef.current) {
                    commentRef.current.value = '';
                    commentClearStateRef.current();
                }
                if (fileRef.current) {
                    // @ts-ignore
                    fileRef.current = null;
                    fileClearStateRef.current();
                }
            },
            [name, userId, editCurrentList, selectedTabId, lists]
        );
        const onAccept = useCallback(() => {
            cppwApi &&
                cppwApi.addProjectRequestComment(id, Intent.PRIMARY, () => {
                    setInfo(ProjectRequestStatus.Accepted);
                    if (
                        !(commentRef.current && commentRef.current.value) &&
                        !(fileRef.current && fileRef.current.value)
                    ) {
                        return;
                    }
                    cppwApi &&
                        cppwApi.addPost(
                            schemaContentId,
                            profileType,
                            UserAction.Approve,
                            commentRef.current ? commentRef.current.value : '',
                            fileRef.current ? fileRef.current.value : null,
                            (id) => {
                                comment(UserAction.Approve, id);
                            }
                        );
                });
        }, [profileType, schemaContentId, cppwApi, id, comment, setInfo]);
        const onReject = useCallback(() => {
            cppwApi &&
                cppwApi.addProjectRequestComment(id, Intent.DANGER, () => {
                    setInfo(ProjectRequestStatus.Rejected);
                    if (
                        !(commentRef.current && commentRef.current.value) &&
                        !(fileRef.current && fileRef.current.value)
                    ) {
                        return;
                    }
                    cppwApi &&
                        cppwApi.addPost(
                            schemaContentId,
                            profileType,
                            UserAction.Reject,
                            commentRef.current ? commentRef.current.value : '',
                            fileRef.current ? fileRef.current.value : null,
                            (id) => {
                                comment(UserAction.Reject, id);
                            }
                        );
                });
        }, [profileType, schemaContentId, cppwApi, id, comment, setInfo]);
        const onComment = useCallback(() => {
            if (
                !(commentRef.current && commentRef.current.value) &&
                !(fileRef.current && fileRef.current.value)
            ) {
                return;
            }
            cppwApi &&
                cppwApi.addPost(
                    schemaContentId,
                    profileType,
                    UserAction.Comment,
                    commentRef.current ? commentRef.current.value : '',
                    fileRef.current ? fileRef.current.value : null,
                    (id) => {
                        comment(UserAction.Comment, id);
                    }
                );
        }, [profileType, schemaContentId, cppwApi, comment]);

        const setCommentRef = useCallback((val) => {
            commentRef.current = val;
        }, []);
        const setFileRef = useCallback((val) => {
            // @ts-ignore
            fileRef.current = val;
        }, []);
        const setFileClearStateRef = useCallback((val) => {
            fileClearStateRef.current = val;
        }, []);
        const setCommentClearStateRef = useCallback((val) => {
            commentClearStateRef.current = val;
        }, []);

        // Обновим списки при переходе в другой профиль.
        useEffect(() => {
            setLists(
                LISTS[profileType].map(({path, component, title}: ProfileListDescriber, index) => ({
                    status: index === 0 ? AsyncStatus.Success : AsyncStatus.Initial,
                    items: index === 0 ? firstList : [],
                    component,
                    path,
                    profileId: ['Материалы', 'Комментарии', 'Посты'].includes(title)
                        ? schemaContentId
                        : id,
                }))
            );
        }, [schemaContentId, profileType, firstList, id]);

        return (
            <>
                <Divider />
                <Tabs
                    id='profile-tabs'
                    className='profile__navbar'
                    selectedTabId={selectedTabId}
                    onChange={onChangeTab}
                >
                    {LISTS[profileType] &&
                        LISTS[profileType].map(
                            ({title, userRoles}: ProfileListDescriber, index: number) => {
                                if (profileType === ProfileType.User && info && userRoles) {
                                    if (!userRoles.includes(info)) {
                                        return null;
                                    }
                                }
                                return <Tab key={index} id={index} title={title} />;
                            }
                        )}
                </Tabs>
                <Divider />
                {isPost && (
                    <div className='profile-lists__comments'>
                        <FormInput
                            title=''
                            key={'comment'}
                            name={'net'}
                            id={'net-name'}
                            mandatory={false}
                            validators={validators}
                            placeholder={
                                ProfileType.ProjectRequest === profileType
                                    ? 'Комментировать...'
                                    : 'Написать...'
                            }
                            usage={Usage.LongText}
                            inputRef={setCommentRef}
                            clearRef={setCommentClearStateRef}
                            formIndex={-1}
                            index={-1}
                        />
                        <FormInput
                            key={'filik'}
                            title=''
                            name={'file'}
                            id={'filik'}
                            mandatory={false}
                            validators={validators}
                            usage={Usage.File}
                            inputRef={setFileRef}
                            formIndex={-1}
                            index={-1}
                            placeholder='Нажмите для выбора файла...'
                            clearRef={setFileClearStateRef}
                        />
                        <ButtonGroup>
                            <Button
                                text={
                                    ProfileType.ProjectRequest === profileType
                                        ? 'Комментировать'
                                        : 'Написать'
                                }
                                onClick={onComment}
                            />
                            {ProfileType.ProjectRequest === profileType &&
                                [
                                    UserRole.Manager,
                                    UserRole.Supervisor,
                                    UserRole.AcademicManager,
                                    UserRole.OfficeManager,
                                ].includes(userRole) &&
                                info !== ProjectRequestStatus.Accepted && (
                                    <Button
                                        text='Утвердить'
                                        intent={Intent.PRIMARY}
                                        onClick={onAccept}
                                    />
                                )}
                            {ProfileType.ProjectRequest === profileType &&
                                [
                                    UserRole.Manager,
                                    UserRole.Supervisor,
                                    UserRole.AcademicManager,
                                    UserRole.OfficeManager,
                                ].includes(userRole) &&
                                info !== ProjectRequestStatus.Accepted && (
                                    <Button
                                        text='Отклонить'
                                        intent={Intent.DANGER}
                                        onClick={onReject}
                                    />
                                )}
                        </ButtonGroup>
                    </div>
                )}
                <ProfileList
                    {...lists[selectedTabId]}
                    editCurrentList={editCurrentList}
                    modifyAllowed={modifyAllowed}
                    className={isPost ? 'profile-lists__reversed-list' : ''}
                />
            </>
        );
    }
);
