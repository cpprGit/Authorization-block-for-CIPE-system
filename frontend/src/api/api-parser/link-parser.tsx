import {checkStrings} from 'src/api/api-parser/type-checkes';
import {ApiResponse, Attribute} from 'src/types';

export enum LinkType {
    User = 'user',
    ProjectRequest = 'project_request',
    Activity = 'activity',
    Project = 'project',
    Organisation = 'organisation',
    File = 'file',
    Questionnaire = 'questionnaire',
}

export const parseLink = (
    {id, name, type}: ApiResponse,
    defaultType?: LinkType
): {id: string; name: string; type: LinkType} | undefined => {
    if (defaultType) {
        try {
            checkStrings([id, name], ['id', 'name']);
            return {
                id,
                name,
                type: defaultType,
            };
        } catch {
            console.error('Ошибка парсинга ссылки');
            return;
        }
    }
    if (Object.values(LinkType).includes(type as LinkType)) {
        try {
            checkStrings([id, name], ['id', 'name']);
            return {
                id,
                name,
                type,
            };
        } catch {
            console.error('Ошибка парсинга ссылки');
            return;
        }
    }
    console.error('Ошибка парсинга ссылки');
    return;
};

export const parseLinks = (links: ApiResponse, defaultType?: LinkType) => {
    try {
        return links
            .map((link: ApiResponse) => parseLink(link, defaultType))
            .filter((item: Attribute | undefined) => !!item);
    } catch (e) {
        console.error(`Wrong api answer! (parseLinks)`);
        return [];
    }
};
