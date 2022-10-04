import {Reducer} from 'redux';
import {Actions} from 'src/store/actions/action-type';
import {
    ADD_CHAT,
    ADD_CHAT_USER,
    ADD_HISTORY_LOGS,
    DELETE_CHAT,
    DELETE_CHAT_USER,
    EDIT_CHAT,
    ERROR_CHATS,
    ERROR_HISTORY_LOG,
    RECEIVE_CHATS,
    RECEIVE_HISTORY_LOG,
    REQUEST_CHATS,
    REQUEST_HISTORY_LOG,
} from 'src/store/actions/chats.actions';
import {AsyncStatus, AttributesByUserRole, ProfileOrSearchItem} from 'src/types';

export type HistoryLog = {
    id: string;
    date: string;
    title: string;
    name?: string;
    link?: string;
};

export type Chat = {
    id: string;
    name: string;
    users: ProfileOrSearchItem[];
};

export type ChatState = Chat & {
    isEditing?: boolean;
    backup?: ChatState;
    history?: {
        status: AsyncStatus;
        logs: HistoryLog[];
    };
};

export type ChatsState =
    | {
          status: AsyncStatus.Initial | AsyncStatus.Pending | AsyncStatus.Error;
          chats: ChatState[];
          attributes: {};
      }
    | {
          status: AsyncStatus.Success;
          chats: ChatState[];
          attributes: AttributesByUserRole;
      };

export const chats: Reducer<ChatsState, Actions> = (
    state: ChatsState = {
        status: AsyncStatus.Initial,
        chats: [],
        attributes: {},
    },
    action: Actions
) => {
    switch (action.type) {
        case RECEIVE_CHATS: {
            const {attributes, chats} = action.payload;
            return {
                status: AsyncStatus.Success,
                chats,
                attributes,
            };
        }
        case REQUEST_CHATS: {
            return {
                status: AsyncStatus.Pending,
                chats: [],
                attributes: {},
            };
        }
        case ERROR_CHATS: {
            return {
                status: AsyncStatus.Error,
                chats: [],
                attributes: {},
            };
        }
        case RECEIVE_HISTORY_LOG: {
            if (!('chats' in state)) {
                return state;
            }
            const {chatIndex, historyLogs} = action.payload;
            const newChats = state.chats.map((chat: ChatState, index: number) =>
                chatIndex === index
                    ? {
                          ...chat,
                          history: {status: AsyncStatus.Success, logs: historyLogs},
                      }
                    : chat
            );

            return {
                ...state,
                chats: newChats,
            };
        }
        case ERROR_HISTORY_LOG: {
            if (!('chats' in state)) {
                return state;
            }
            const chatIndex = action.payload;
            const newChats = state.chats.map((chat: ChatState, index: number) =>
                chatIndex === index
                    ? {
                          ...chat,
                          history: {status: AsyncStatus.Error},
                      }
                    : chat
            );

            return {
                ...state,
                chats: newChats,
            };
        }
        case REQUEST_HISTORY_LOG: {
            if (!('chats' in state)) {
                return state;
            }
            const chatIndex = action.payload;
            const newChats = state.chats.map((chat: ChatState, index: number) =>
                chatIndex === index
                    ? {
                          ...chat,
                          history: {status: AsyncStatus.Pending},
                      }
                    : chat
            );

            return {
                ...state,
                chats: newChats,
            };
        }
        case ADD_CHAT: {
            if (!('chats' in state)) {
                return state;
            }
            const chat = action.payload;

            return {
                ...state,
                chats: [...state.chats, chat],
            };
        }
        case DELETE_CHAT: {
            if (!('chats' in state)) {
                return state;
            }
            const {chatIndex} = action.payload;
            return {
                ...state,
                chats: state.chats.filter((chat, index) => index !== chatIndex),
            };
        }
        case EDIT_CHAT: {
            if (!('chats' in state)) {
                return state;
            }
            const {chatIndex, newChat} = action.payload;
            const newChats = state.chats.map((chat: ChatState, index: number) =>
                chatIndex === index
                    ? {
                          ...chat,
                          ...newChat,
                          backup: newChat.isEditing ? chat : chat.backup,
                      }
                    : chat
            );

            return {
                ...state,
                chats: newChats,
            };
        }
        case ADD_CHAT_USER: {
            if (!('chats' in state)) {
                return state;
            }
            const {chatIndex, user} = action.payload;
            const newChats = state.chats.map((chat: ChatState, index: number) => {
                if (chatIndex === index) {
                    return {
                        ...chat,
                        users: [...chat.users, user],
                    };
                }
                return chat;
            });

            return {
                ...state,
                chats: newChats,
            };
        }
        case DELETE_CHAT_USER: {
            if (!('chats' in state)) {
                return state;
            }
            const {chatIndex, userId} = action.payload;
            const newChats = state.chats.map((chat: ChatState, index: number) => {
                if (chatIndex === index) {
                    return {
                        ...chat,
                        users: chat.users.filter(({id}) => id !== userId),
                    };
                }
                return chat;
            });

            return {
                ...state,
                chats: newChats,
            };
        }
        case ADD_HISTORY_LOGS: {
            if (!('chats' in state)) {
                return state;
            }
            const {chatIndex, historyLogs} = action.payload;
            const newChats = state.chats.map((chat: ChatState, index: number) =>
                chatIndex === index
                    ? {
                          ...chat,
                          history: {
                              ...chat.history,
                              logs: [...(chat.history?.logs || []), ...historyLogs],
                          },
                      }
                    : chat
            );

            return {
                ...state,
                chats: newChats,
            };
        }
        default: {
            return state;
        }
    }
};
