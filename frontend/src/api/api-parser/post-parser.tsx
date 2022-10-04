import {LinkType, parseLink} from 'src/api/api-parser/link-parser';
import {checkOptionalStrings, checkStrings} from 'src/api/api-parser/type-checkes';
import {ApiResponse, Attribute, UserAction} from 'src/types';

const parsePost = ({id, name, text, message, file}: ApiResponse) => {
    try {
        checkStrings([id], ['id']);
        checkOptionalStrings([text, message], ['text', 'message']);

        return {
            id,
            user: parseLink(name, LinkType.User),
            file: file ? parseLink(file, LinkType.File) : null,
            action: message || UserAction.Comment,
            text,
        };
    } catch (e) {
        console.error(`Неверный формат поста: ${e.message}`);
        return;
    }
};

export const parsePosts = (posts: ApiResponse) => {
    try {
        return posts.map(parsePost).filter((item: Attribute | undefined) => !!item);
    } catch (e) {
        console.error(`Wrong api answer! (parsePosts)`);
        return [];
    }
};
