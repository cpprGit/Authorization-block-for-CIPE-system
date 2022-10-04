import {Icon} from '@blueprintjs/core';
import React, {FC, memo, useCallback, useMemo} from 'react';
import {addChat} from 'src/store/actions/chats.actions';
import {ChatState} from 'src/store/reducers/chats.reducer';
import {AsyncStatus} from 'src/types';
import {ChatCard} from 'src/ui/blocks/chat-card/chat-card';
import {LoadableView} from 'src/ui/blocks/loadable-view/loadable-view';
import {Search} from 'src/ui/blocks/search/search';
import {useCppwApiContext} from 'src/ui/contexts/cppw-api-context/cppw-api-context';
import {NEW_CHAT} from 'src/ui/utils/constants';

type Props = {
    userId: string;
    status: AsyncStatus;
    chats: ChatState[];
    activeTabIndex: number;
    setActiveTabIndex: (activeTabIndex: number) => void;
};
export const ChatColumn: FC<Props> = memo(
    ({status, chats, activeTabIndex, setActiveTabIndex, userId}) => {
        const cppwApi = useCppwApiContext();
        const items = useMemo(
            () =>
                chats.map((chat, index) => ({
                    title: chat.name,
                    chatIndex: index,
                    id: chat.id,
                    onClick: () => {
                        setActiveTabIndex(index);
                    },
                    isEditing: Boolean(chat.isEditing),
                    isActive: index === activeTabIndex,
                })),
            [chats, setActiveTabIndex, activeTabIndex]
        );

        const onClick = useCallback(() => {
            cppwApi && cppwApi.store.dispatch(addChat(NEW_CHAT));
            setActiveTabIndex(chats.length);
        }, [chats.length, cppwApi, setActiveTabIndex]);
        const onRetry = useCallback(() => {
            cppwApi && cppwApi.requestChats(userId);
        }, [userId, cppwApi]);

        return (
            <>
                <LoadableView
                    status={status}
                    errorSize='s'
                    errorTitle='Ошибка загрузки контактов.'
                    errorSubtitle='Нажмите для повтора'
                    onRetry={onRetry}
                >
                    <Search component={ChatCard} searchPropertyName='title' items={items} />
                    <div className='chat-card _placeholder' onClick={onClick}>
                        <Icon icon='plus' />
                    </div>
                </LoadableView>
            </>
        );
    }
);
