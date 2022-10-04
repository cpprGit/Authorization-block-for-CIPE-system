import {Chat, ChatState, HistoryLog} from 'src/store/reducers/chats.reducer';
import {AttributesByUserRole, ProfileOrSearchItem} from 'src/types';

export const RECEIVE_CHATS = 'RECEIVE_CHATS' as const;
export const REQUEST_CHATS = 'REQUEST_CHATS' as const;
export const ERROR_CHATS = 'ERROR_CHATS' as const;

export const RECEIVE_HISTORY_LOG = 'RECEIVE_HISTORY_LOG' as const;
export const REQUEST_HISTORY_LOG = 'REQUEST_HISTORY_LOG' as const;
export const ERROR_HISTORY_LOG = 'ERROR_HISTORY_LOG' as const;

export const ADD_CHAT = 'ADD_CHAT' as const;
export const DELETE_CHAT = 'DELETE_CHAT' as const;
export const EDIT_CHAT = 'EDIT_CHAT' as const;
export const ADD_CHAT_USER = 'ADD_CHAT_USER' as const;
export const DELETE_CHAT_USER = 'DELETE_CHAT_USER' as const;
export const ADD_HISTORY_LOGS = 'ADD_HISTORY_LOGS' as const;

export const receiveChats = (attributes: AttributesByUserRole, chats: Chat[]) => ({
    type: RECEIVE_CHATS,
    payload: {attributes, chats},
});

export const errorChats = () => ({
    type: ERROR_CHATS,
});

export const requestChats = () => ({
    type: REQUEST_CHATS,
});

export const receiveHistoryLogs = (chatIndex: number, historyLogs: HistoryLog[]) => ({
    type: RECEIVE_HISTORY_LOG,
    payload: {chatIndex, historyLogs},
});

export const errorHistoryLogs = (chatIndex: number) => ({
    type: ERROR_HISTORY_LOG,
    payload: chatIndex,
});

export const requestHistoryLogs = (chatIndex: number) => ({
    type: REQUEST_HISTORY_LOG,
    payload: chatIndex,
});

export const addChat = (chat: Chat) => ({
    type: ADD_CHAT,
    payload: chat,
});

export const deleteChat = (chatIndex: number) => ({
    type: DELETE_CHAT,
    payload: {chatIndex},
});

export const editChat = (chatIndex: number, newChat: Partial<ChatState>) => ({
    type: EDIT_CHAT,
    payload: {chatIndex, newChat},
});
export const addChatUser = (chatIndex: number, user: ProfileOrSearchItem) => ({
    type: ADD_CHAT_USER,
    payload: {chatIndex, user},
});
export const deleteChatUser = (chatIndex: number, userId: string) => ({
    type: DELETE_CHAT_USER,
    payload: {chatIndex, userId},
});

export const addHistoryLogs = (chatIndex: number, historyLogs: HistoryLog[]) => ({
    type: ADD_HISTORY_LOGS,
    payload: {chatIndex, historyLogs},
});
