import {
    Button,
    ButtonGroup,
    Classes,
    EditableText,
    H1,
    InputGroup,
    Intent,
} from '@blueprintjs/core';
import React, {FC, memo, useCallback, useEffect, useMemo, useRef, useState} from 'react';
import AutoSizer from 'react-virtualized-auto-sizer';
import {FixedSizeList as List} from 'react-window';
import {getRandomId} from 'src/api/api';
import {addChatUser, deleteChat, deleteChatUser, editChat} from 'src/store/actions/chats.actions';
import {ChatState} from 'src/store/reducers/chats.reducer';
import {AsyncStatus, AttributesByUserRole, ProfileOrSearchItem} from 'src/types';
import {SimpleChatUserCard} from 'src/ui/blocks/chat-user-card/chat-user-card';
import {LoadableView} from 'src/ui/blocks/loadable-view/loadable-view';
import {useCppwApiContext} from 'src/ui/contexts/cppw-api-context/cppw-api-context';
import './chat-users.styl';

type Props = {
    chatIndex: number;
    title: string;
    isEditing: boolean;
    attributes: AttributesByUserRole;
    users: ProfileOrSearchItem[];
    id: string;
    backup?: ChatState;
};

export const ChatUsers: FC<Props> = memo(({chatIndex, title, isEditing, id, users, backup}) => {
    const cppwApi = useCppwApiContext();
    const [filter, setFilter] = useState('');
    const logs = useRef<any[]>([]);
    const onChange = useCallback(
        ({target}: any) => {
            setFilter(String(target.value).toLowerCase());
        },
        [setFilter]
    );
    const [allUsersStatus, setAllUsersStatus] = useState(AsyncStatus.Initial);
    const [allUsers, setAllUsers] = useState<ProfileOrSearchItem[]>([]);
    const filteredUsers = useMemo(
        () => users.filter(({name}) => name.toLowerCase().includes(filter)),
        [filter, users]
    );
    const filteredAllUsers = useMemo(
        () => allUsers.filter(({name}) => name.toLowerCase().includes(filter)),
        [filter, allUsers]
    );

    const wrapperStyle = useMemo(() => {
        if (window.innerWidth <= 800) {
            return {height: Math.min(34 * users.length, window.innerHeight / 2)};
        }
        return undefined;
    }, [users.length]);
    const editableWrapperStyle = useMemo(() => {
        if (window.innerWidth <= 800) {
            return {
                height: Math.min(34 * allUsers.length, window.innerHeight / 2),
            };
        }
        return undefined;
    }, [allUsers.length]);

    const onChangeTitle = useCallback(
        (name) => {
            cppwApi && cppwApi.store.dispatch(editChat(chatIndex, {name}));
        },
        [chatIndex, cppwApi]
    );
    const onEditFinished = useCallback(() => {
        cppwApi &&
            cppwApi.saveChat(chatIndex, {id, users, name: title}, logs.current, () => {
                cppwApi && cppwApi.store.dispatch(editChat(chatIndex, {isEditing: false}));
                logs.current = [];
            });
    }, [id, users, title, chatIndex, cppwApi]);
    const onCancel = useCallback(() => {
        if (backup) {
            cppwApi && cppwApi.store.dispatch(editChat(chatIndex, {...backup, isEditing: false}));
        } else {
            cppwApi && cppwApi.store.dispatch(deleteChat(chatIndex));
        }
        logs.current = [];
    }, [backup, chatIndex, cppwApi]);
    const requestAllUsersForMailGroups = useCallback(() => {
        if (
            isEditing &&
            allUsersStatus !== AsyncStatus.Pending &&
            allUsersStatus !== AsyncStatus.Success
        ) {
            cppwApi &&
                cppwApi.requestAllUsersForMailGroups(
                    () => {
                        setAllUsersStatus(AsyncStatus.Pending);
                    },
                    (result) => {
                        setAllUsers(result);
                        setAllUsersStatus(AsyncStatus.Success);
                    },
                    () => {
                        setAllUsersStatus(AsyncStatus.Error);
                    }
                );
        }
    }, [cppwApi, allUsersStatus, isEditing]);
    const Row = useCallback(
        ({index, style}) => (
            <div style={style}>
                <SimpleChatUserCard
                    key={filteredUsers[index].id}
                    userName={filteredUsers[index].name}
                    userId={filteredUsers[index].id}
                />
            </div>
        ),
        [filteredUsers]
    );
    const EditableRow = useCallback(
        ({index, style}) => (
            <div style={style}>
                <SimpleChatUserCard
                    key={filteredAllUsers[index].id}
                    isAdded={users.some(({id}) => id === filteredAllUsers[index].id)}
                    userName={filteredAllUsers[index].name}
                    handleAdd={() => {
                        let wasFiltered = false;
                        logs.current = logs.current.filter(({target}) => {
                            if (filteredAllUsers[index].id === target.id) {
                                wasFiltered = true;
                                return false;
                            } else {
                                return true;
                            }
                        });
                        if (!wasFiltered) {
                            logs.current.push({
                                id: getRandomId(),
                                date: new Date().toISOString(),
                                message: 'Добавлен пользователь',
                                target: {
                                    name: filteredAllUsers[index].name,
                                    id: filteredAllUsers[index].id,
                                    type: 'user',
                                },
                            });
                        }
                        cppwApi &&
                            cppwApi.store.dispatch(addChatUser(chatIndex, filteredAllUsers[index]));
                    }}
                    handleDelete={() => {
                        let wasFiltered = false;
                        logs.current = logs.current.filter(({target}) => {
                            if (filteredAllUsers[index].id === target.id) {
                                wasFiltered = true;
                                return false;
                            } else {
                                return true;
                            }
                        });
                        if (!wasFiltered) {
                            logs.current.push({
                                id: getRandomId(),
                                date: new Date().toISOString(),
                                message: 'Удален пользователь',
                                target: {
                                    name: filteredAllUsers[index].name,
                                    id: filteredAllUsers[index].id,
                                    type: 'user',
                                },
                            });
                        }
                        cppwApi &&
                            cppwApi.store.dispatch(
                                deleteChatUser(chatIndex, filteredAllUsers[index].id)
                            );
                    }}
                    userId={filteredAllUsers[index].id}
                />
            </div>
        ),
        [chatIndex, cppwApi, users, filteredAllUsers]
    );

    useEffect(() => {
        requestAllUsersForMailGroups();
    }, [requestAllUsersForMailGroups]);

    if (isEditing) {
        return (
            <>
                <H1 className='chat-users__title-wrapper'>
                    <EditableText
                        className={'chat-users__title'}
                        onChange={onChangeTitle}
                        placeholder='Наименование группы контактов...'
                        value={title}
                    />
                </H1>
                <LoadableView
                    status={allUsersStatus}
                    onRetry={requestAllUsersForMailGroups}
                    errorTitle='Ошибка загрузки списка всех пользователей'
                    errorSubtitle='Нажмите для повтора.'
                >
                    <InputGroup
                        className={`${Classes.ROUND} ${Classes.FILL} search`}
                        leftIcon='search'
                        value={filter}
                        onChange={onChange}
                        placeholder='Искать...'
                    />
                    <div className='chat-users__editable' style={editableWrapperStyle}>
                        <AutoSizer>
                            {({height, width}) => (
                                <List
                                    height={height}
                                    itemCount={filteredAllUsers.length}
                                    itemSize={34}
                                    width={width}
                                >
                                    {EditableRow}
                                </List>
                            )}
                        </AutoSizer>
                    </div>
                </LoadableView>
                <ButtonGroup className='chat-users__buttons'>
                    <Button text='Сохранить' intent={Intent.PRIMARY} onClick={onEditFinished} />
                    <Button text='Отменить' onClick={onCancel} />
                </ButtonGroup>
            </>
        );
    }

    return (
        <>
            <InputGroup
                className={`${Classes.ROUND} ${Classes.FILL} search`}
                leftIcon='search'
                value={filter}
                onChange={onChange}
                placeholder='Искать...'
            />
            <div className='chat-users' style={wrapperStyle}>
                <AutoSizer>
                    {({height, width}) => (
                        <List
                            height={height}
                            itemCount={filteredUsers.length}
                            itemSize={34}
                            width={width}
                        >
                            {Row}
                        </List>
                    )}
                </AutoSizer>
            </div>
        </>
    );
});
