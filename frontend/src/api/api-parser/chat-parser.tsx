import {checkStrings} from 'src/api/api-parser/type-checkes';
import {Chat, HistoryLog} from 'src/store/reducers/chats.reducer';
import {ApiResponse} from 'src/types';
import {parseDate} from 'src/ui/utils/parse-date';

const parseHistoryLog = ({id, date, message, target}: ApiResponse) => {
    try {
        checkStrings([id, message, date], ['id', 'message', 'date']);

        return {
            id,
            name: target ? target.name : '',
            title: message,
            link: target && target.type && target.id ? `${target.type}?id=${target.id}` : '',
            date: parseDate(new Date(date)),
        };
    } catch (e) {
        console.error(`Wrong history log ${id} format: ${e.message}`);
    }
};

export const parseHistoryLogs = (logs: ApiResponse): HistoryLog[] => {
    if (!Array.isArray(logs)) {
        console.error(`Wrong api answer! (parseHistoryLogs)`);
        return [];
    }

    return logs
        .map((item: ApiResponse) => parseHistoryLog(item))
        .filter((item: HistoryLog | undefined) => !!item) as HistoryLog[];
};

const parseChat = ({id, name, users}: ApiResponse): Chat | undefined => {
    try {
        checkStrings([id, name], ['id', 'name']);

        return {
            id,
            name,
            users,
        };
    } catch (e) {
        console.error(`Wrong chat ${id} format: ${e.message}`);
    }
};

export const parseChats = (chats: ApiResponse): Chat[] => {
    if (!Array.isArray(chats)) {
        console.error(`Wrong api answer! (parseChats)`);
        return [];
    }

    return chats
        .map((item: ApiResponse) => parseChat(item))
        .filter((item: Chat | undefined) => !!item) as Chat[];
};
