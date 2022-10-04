import React, {FC, memo, useEffect, useMemo, useState} from 'react';
import {useSelector} from 'react-redux';
import {State} from 'src/store/reducers';
import {AsyncStatus, AttributesByUserRole} from 'src/types';
import {ChatColumn} from 'src/ui/blocks/chat-column/chat-column';
import {ChatInfo} from 'src/ui/blocks/chat-info/chat-info';
import {Layout} from 'src/ui/blocks/layout/layout';
import {useCppwApiContext} from 'src/ui/contexts/cppw-api-context/cppw-api-context';

export const ChatPage: FC = memo(() => {
    const cppwApi = useCppwApiContext();
    const {userId} = useSelector((state: State) =>
        state.user.isAuthed ? state.user : {userId: ''}
    );
    const {status, chats, attributes} = useSelector((state: State) => state.chats);
    const [activeTabIndex, setActiveTabIndex] = useState(0);
    const isEditing = useMemo(
        () => Boolean(chats && chats[activeTabIndex] && chats[activeTabIndex].isEditing),
        [activeTabIndex, chats]
    );

    useEffect(() => {
        if (status === AsyncStatus.Initial && cppwApi) {
            cppwApi.requestChats(userId);
        }
    }, [userId, status, cppwApi]);

    return (
        <Layout
            leftComponent={
                <ChatColumn
                    status={status}
                    userId={userId}
                    chats={chats}
                    activeTabIndex={Math.min(activeTabIndex, chats.length)}
                    setActiveTabIndex={setActiveTabIndex}
                />
            }
            rightComponent={
                <ChatInfo
                    status={status}
                    isEditing={isEditing}
                    chatIndex={Math.min(activeTabIndex, chats.length)}
                    attributes={attributes as AttributesByUserRole}
                    chat={chats[Math.min(activeTabIndex, chats.length)]}
                />
            }
        />
    );
});
