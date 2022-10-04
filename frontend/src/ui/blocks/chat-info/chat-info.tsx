import {Tab, Tabs} from '@blueprintjs/core';
import React, {FC, memo, useCallback, useEffect, useMemo, useState} from 'react';
import {ChatState} from 'src/store/reducers/chats.reducer';
import {AsyncStatus, AttributesByUserRole} from 'src/types';
import {ChatHistory} from 'src/ui/blocks/chat-history/chat-history';
import 'src/ui/blocks/chat-info/chat-info.styl';
import {ChatUsers} from 'src/ui/blocks/chat-users/chat-users';
import {LoadableView} from 'src/ui/blocks/loadable-view/loadable-view';

type Props = {
    status: AsyncStatus;
    chat: ChatState;
    attributes: AttributesByUserRole;
    chatIndex: number;
    isEditing: boolean;
};

export const ChatInfo: FC<Props> = memo(({status, chat, attributes, chatIndex, isEditing}) => {
    const {status: historyStatus = AsyncStatus.Initial, logs = []} = chat?.history
        ? chat.history
        : {};
    const [selectedTabId, setSelectedTabId] = useState(0);
    const onChangeTab = useCallback((tabId: number) => setSelectedTabId(tabId), [setSelectedTabId]);
    const mailTo = useMemo(() => chat?.users.map(({email}) => email).join(','), [chat]);

    useEffect(() => {
        setSelectedTabId(0);
    }, [isEditing]);

    return (
        <LoadableView status={status} spinnerClassName='spinner-full-height'>
            {chat && (
                <Tabs
                    id='chat-info__tabs'
                    className='chat-info__tabs'
                    selectedTabId={selectedTabId}
                    onChange={onChangeTab}
                    large={true}
                >
                    <Tab key={0} id={0} title='Участники' />
                    {!isEditing && <Tab key={1} id={1} title='История' />}
                </Tabs>
            )}
            {selectedTabId === 0 && chat && (
                <ChatUsers
                    users={chat && chat.users}
                    id={chat && chat.id}
                    chatIndex={chatIndex}
                    title={chat && chat.name}
                    backup={chat && chat.backup}
                    isEditing={isEditing}
                    attributes={attributes}
                />
            )}

            {selectedTabId === 1 && chat && (
                <ChatHistory
                    chatId={chat.id}
                    chatIndex={chatIndex}
                    status={historyStatus}
                    logs={logs}
                    mailTo={mailTo}
                />
            )}
        </LoadableView>
    );
});
