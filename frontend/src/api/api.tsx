import {Intent} from '@blueprintjs/core';
import React from 'react';
import {Link} from 'react-router-dom';
import {Store} from 'redux';
import {parseChats, parseHistoryLogs} from 'src/api/api-parser/chat-parser';
import {parseAttribute, parseFields, parseForm, parseForms} from 'src/api/api-parser/form-parser';
import {LinkType, parseLink, parseLinks} from 'src/api/api-parser/link-parser';
import {parsePosts} from 'src/api/api-parser/post-parser';
import {parseProfile, parseUserRole} from 'src/api/api-parser/profile-parser';
import {
    addNewAttribute,
    editAttribute,
    errorAttributeByUsage,
    receiveAttributeByUsage,
    requestAttributeByUsage,
} from 'src/store/actions/attributes.actions';
import {
    addHistoryLogs,
    deleteChat,
    editChat,
    errorChats,
    errorHistoryLogs,
    receiveChats,
    receiveHistoryLogs,
    requestChats,
    requestHistoryLogs,
} from 'src/store/actions/chats.actions';
import {
    errorFormByType,
    receiveFormByType,
    requestFormByType,
} from 'src/store/actions/default-forms.actions';
import {
    errorFullSearch,
    errorSearch,
    receiveFullSearch,
    receiveSearch,
    requestFullSearch,
    requestSearch,
} from 'src/store/actions/search.actions';
import {
    addArchiveForm,
    errorUserArchiveForms,
    removeArchiveForm,
    requestUserArchiveForms,
    successUserArchiveForms,
} from 'src/store/actions/user-archive-forms.actions';
import {
    addDraftFormAttributeProperties,
    editUserForm,
    errorFormStats,
    errorUserForms,
    requestFormStats,
    requestUserForms,
    successFormStats,
    successUserForms,
} from 'src/store/actions/user-forms.actions';
import {setUserData} from 'src/store/actions/user.actions';
import {State} from 'src/store/reducers';
import {Chat, HistoryLog} from 'src/store/reducers/chats.reducer';
import {
    ApiResponse,
    AsyncStatus,
    Attribute,
    AttributesByUserRole,
    AttributeValue,
    Form,
    FormMode,
    FormType,
    ProfileOrSearchItem,
    ProfileStatus,
    ProfileType,
    ProjectApplicationStatus,
    ProjectRequestStatus,
    ToasterFromContext,
    Usage,
    UserAction,
    UserRole,
} from 'src/types';
import {clearAuthCookie, redirect} from 'src/ui/utils/auth';
import {
    BACKEND_URL,
    DEFAULT_ATTRIBUTE_ID,
    DEFAULT_CHAT_ID,
    DEFAULT_FORM_ID,
} from 'src/ui/utils/constants';
import {getCookie, setCookie} from 'src/ui/utils/cookie';
import {parseJWT} from './api-parser/jwt-parser';

export const TOKEN_PARAM_NAME = 'token';
export const getRandomId = () =>
    Math.random().toString(36).substring(2, 15) + Math.random().toString(36).substring(2, 15);

export const formSearchUrl = (fields: Attribute[], startWith: string = '?') => {
    const staticFields = fields
        .filter(({realName}: {realName?: string}) => realName)
        .reduce((res: {[k: string]: AttributeValue}, {realName, defaultValue}: Attribute) => {
            if (
                !realName ||
                (defaultValue !== false &&
                    (!defaultValue || (Array.isArray(defaultValue) && !defaultValue.length)))
            ) {
                return res;
            } else {
                if (typeof defaultValue === 'object') {
                    if ('id' in defaultValue) {
                        return {
                            ...res,
                            [realName]: defaultValue.id,
                        };
                    } else {
                        return res;
                    }
                }
                return {
                    ...res,
                    [realName]: defaultValue,
                };
            }
        }, {});
    const content = fields
        .filter(({realName}: {realName?: string}) => !realName)
        .reduce((res: {[k: string]: AttributeValue}, {name, defaultValue}: Attribute) => {
            if (
                defaultValue !== false &&
                (!defaultValue || (Array.isArray(defaultValue) && !defaultValue.length))
            ) {
                return res;
            } else {
                if (typeof defaultValue === 'object') {
                    if ('id' in defaultValue && defaultValue.id) {
                        return {
                            ...res,
                            [name]: defaultValue.id,
                        };
                    } else {
                        return res;
                    }
                }
                return {
                    ...res,
                    [name]: defaultValue,
                };
            }
        }, {});
    const statics = Object.keys(staticFields)
        .map((key) => `${key}=${staticFields[key]}`)
        .join('&');
    const dynamics = Object.keys(content).length
        ? `&filter_params=${Object.keys(content)
              .map((key: string) => `${key}====${content[key].toString()}`)
              .join('----')}`
        : '';

    return `${startWith}${statics}${dynamics}`;
};

export class CppwApi {
    store: Store<State>;
    toaster: ToasterFromContext;
    token: string = '';

    constructor(store: Store<State>, toaster: ToasterFromContext) {
        this.store = store;
        this.toaster = toaster;

        this.init();
    }

    init() {
        const dispatch = this.store.dispatch;
        const cashedToken = getCookie(TOKEN_PARAM_NAME);
        const urlToken =
            window.location && window.location.search
                ? window.location.search
                      .slice(1)
                      .split('&')
                      .reduce((res, cur) => {
                          if (res) {
                              return res;
                          }
                          const [name, val] = cur.split('=');
                          return name === TOKEN_PARAM_NAME ? val : res;
                      }, '')
                : undefined;

        const parsedCashedToken = cashedToken ? parseJWT(cashedToken) : null;
        const parsedUrlToken = urlToken ? parseJWT(urlToken) : null;

        // смотрим на даты их и выбираем более новый
        const finalParsedData = parsedCashedToken
            ? parsedUrlToken
                ? parsedCashedToken.exp < parsedUrlToken.exp
                    ? parsedCashedToken
                    : parsedUrlToken
                : parsedCashedToken
            : parsedUrlToken
            ? parsedUrlToken
            : null;

        if (finalParsedData) {
            if (finalParsedData?.exp * 1000 < Date.now()) {
                clearAuthCookie();
                redirect();
            } else {
                dispatch(setUserData(finalParsedData));
                setCookie(TOKEN_PARAM_NAME, finalParsedData.token, {
                    expires: finalParsedData.exp,
                });
                this.token = finalParsedData.token;
            }
        }
        if (urlToken !== undefined) {
            window.history.replaceState(
                null,
                'ЦППР',
                window.location.href
                    .replace(`${TOKEN_PARAM_NAME}=${urlToken}&`, '')
                    .replace(`&${TOKEN_PARAM_NAME}=${urlToken}`, '')
                    .replace(`?${TOKEN_PARAM_NAME}=${urlToken}`, '')
            );
        }
    }

    async get(path: string, params: RequestInit = {}) {
        const answer = await fetch(`${BACKEND_URL}/api/v1/${path}`, {
            ...params,
            credentials: process.env.IS_DEV ? 'include' : 'same-origin',
        });
        if (answer.status === 403) {
            clearAuthCookie();
            redirect();
        }
        if (answer.status !== 200 && answer.status !== 201 && answer.status !== 302) {
            throw new Error('');
        }
        return await answer.json();
    }

    async makeRequest({
        getResult,
        onRequest,
        onReceive,
        onError,
        onRetry,
        errorMessage,
        successMessage,
    }: {
        getResult: () => Promise<ApiResponse>;
        onRequest?: () => void;
        onReceive: (result: ApiResponse) => void;
        onError?: () => void;
        onRetry?: () => void;
        errorMessage?: string;
        successMessage?: string;
    }) {
        const toaster = this.toaster;
        try {
            onRequest && onRequest();
            const result = await getResult();
            onReceive(result);
            if (toaster.current && successMessage) {
                toaster.current.show({
                    icon: 'tick',
                    intent: Intent.SUCCESS,
                    message: successMessage,
                });
            }
        } catch (e) {
            onError && onError();
            console.error(e);
            if (toaster.current && onRetry && errorMessage) {
                toaster.current.show({
                    action: {
                        onClick: onRetry,
                        text: 'Повторить',
                    },
                    icon: 'error',
                    intent: Intent.DANGER,
                    message: errorMessage,
                });
            }
        }
    }

    // На успех - глобальный стейт + тостер, На ошибку - тостер
    async updateDefaultForm(form: Form) {
        const dispatch = this.store.dispatch;
        const {id, type} = form;
        const getResult = async () => {
            return await this.get(`current-schema/update/${type}`, {
                method: 'POST',
                body: JSON.stringify({
                    schemaId: id,
                }),
            });
        };
        const onReceive = () => {
            dispatch(receiveFormByType(type, form));
        };
        const onRetry = () => {
            this.updateDefaultForm(form);
        };
        const successMessage = 'Дефолтная форма задана успешно.';
        const errorMessage = 'Не удалось задать дефолтную форму';
        this.makeRequest({
            getResult,
            onReceive,
            onRetry,
            errorMessage,
            successMessage,
        });
    }

    // На успех - глобальный стейт, На ошибку - глобальный стейт + тостер
    async getDefaultForm(type: FormType) {
        const dispatch = this.store.dispatch;

        const getResult = async () => {
            return await this.get(`formatted/current-schema/${type}`);
        };
        const onRequest = () => {
            dispatch(requestFormByType(type));
        };
        const onReceive = (result: ApiResponse) => {
            const parsedForm = parseForm(result, FormMode.Fill);
            if (parsedForm) {
                dispatch(receiveFormByType(type, parsedForm));
            } else {
                dispatch(errorFormByType(type));
            }
        };
        const onError = () => {
            dispatch(errorFormByType(type));
        };
        const onRetry = () => {
            this.getDefaultForm(type);
        };
        const errorMessage = 'Ошибка загрузки формы.';

        this.makeRequest({
            getResult,
            onRequest,
            onReceive,
            onRetry,
            onError,
            errorMessage,
        });
    }

    // На успех - глобальный стейт, На ошибку - глобальный стейт
    async getUserForms() {
        const dispatch = this.store.dispatch;

        const getResult = async () => {
            return await this.get('formatted/schemas/');
        };
        const onRequest = () => {
            dispatch(requestUserForms());
        };
        const onReceive = (result: ApiResponse) => {
            dispatch(successUserForms(parseForms(result, FormMode.View)));
        };
        const onError = () => {
            dispatch(errorUserForms());
        };

        this.makeRequest({getResult, onRequest, onReceive, onError});
    }

    async getUserArchiveForms() {
        const dispatch = this.store.dispatch;

        const getResult = async () => {
            return await this.get('formatted/schemas/archived');
        };
        const onRequest = () => {
            dispatch(requestUserArchiveForms());
        };
        const onReceive = (result: ApiResponse) => {
            dispatch(successUserArchiveForms(parseForms(result, FormMode.View)));
        };
        const onError = () => {
            dispatch(errorUserArchiveForms());
        };

        this.makeRequest({getResult, onRequest, onReceive, onError});
    }

    async saveAttribute(
        id: string,
        index: number,
        newAttribute: Partial<Attribute>,
        formIndex: number
    ) {
        const dispatch = this.store.dispatch;
        const isCreate = id === DEFAULT_ATTRIBUTE_ID;

        const getResult = async () => {
            return await this.get(`attribute/${isCreate ? '' : id}`, {
                method: 'POST',
                body: JSON.stringify(newAttribute),
            });
        };
        const onReceive = (result: ApiResponse) => {
            const newAttribute: Attribute | undefined = parseAttribute(result);
            if (!newAttribute) {
                throw Error('Wrong api response! (saveAttribute)');
            }
            if (isCreate) {
                dispatch(addNewAttribute(newAttribute.usage, newAttribute));
            } else {
                dispatch(editAttribute(newAttribute.usage, newAttribute));
            }
            newAttribute.isPlaceholder = false;
            dispatch(addDraftFormAttributeProperties(formIndex, index, newAttribute));
        };
        const onRetry = () => {
            this.saveAttribute(id, index, newAttribute, formIndex);
        };
        const errorMessage = 'Ошибка создания атрибута.';

        this.makeRequest({getResult, onReceive, onRetry, errorMessage});
    }

    async getAttributesByUsage(usage: Usage) {
        const dispatch = this.store.dispatch;
        const getResult = async () => {
            return await this.get(`formatted/attributes-by-usage-name/${usage}`);
        };
        const onRequest = () => {
            dispatch(requestAttributeByUsage(usage));
        };
        const onReceive = (result: ApiResponse) => {
            dispatch(receiveAttributeByUsage(usage, (result || []).map(parseAttribute)));
        };
        const onError = () => {
            dispatch(errorAttributeByUsage(usage));
        };
        const onRetry = () => {
            this.getAttributesByUsage(usage);
        };
        const errorMessage = 'Ошибка загрузки атрибутов.';

        this.makeRequest({
            getResult,
            onRequest,
            onReceive,
            onError,
            onRetry,
            errorMessage,
        });
    }

    async saveForm(index: number, form: Partial<Form>, onSuccess: () => void) {
        const dispatch = this.store.dispatch;
        const getResult = async () => {
            return await this.get(
                `formatted/schema/${!form.id || form.id === DEFAULT_FORM_ID ? '' : form.id}`,
                {
                    method: 'POST',
                    body: JSON.stringify({
                        ...form,
                        attributes: form.attributes
                            ? form.attributes.filter(({realName}: Attribute) => !realName)
                            : [],
                    }),
                }
            );
        };
        const onReceive = (result: ApiResponse) => {
            dispatch(
                editUserForm(index, {
                    ...form,
                    id: result.id,
                    attributes: form.attributes,
                })
            );
            onSuccess();
        };
        const onRetry = () => {
            this.saveForm(index, form, onSuccess);
        };
        const errorMessage = 'Ошибка сохранения формы.';

        this.makeRequest({getResult, onReceive, onRetry, errorMessage});
    }

    async fullSearch() {
        const dispatch = this.store.dispatch;
        const state = this.store.getState();
        const {searchType, fields, searchFilterStatus} = state.search;

        const params = formSearchUrl(fields);
        const getResult = async () => {
            return await this.get(`search/${searchType}${params}`);
        };
        const onReceive = (result: ApiResponse) => {
            if (searchFilterStatus !== AsyncStatus.Success) {
                const parsedForm = parseForm(result.schema, FormMode.View);
                if (!parsedForm) {
                    dispatch(errorFullSearch());
                } else {
                    dispatch(receiveFullSearch({records: result.records, schema: parsedForm}));
                }
            } else {
                dispatch(receiveSearch(result));
            }
        };
        const onRequest = () => {
            dispatch(requestSearch());
        };
        const onError = () => {
            dispatch(errorSearch());
        };

        this.makeRequest({getResult, onReceive, onError, onRequest});
    }

    async search() {
        const dispatch = this.store.dispatch;
        const state = this.store.getState();

        const getResult = async () => {
            return await this.get(`search/${state.search.searchType}`);
        };
        const onReceive = (result: ApiResponse) => {
            const parsedForm = parseForm(result.schema, FormMode.View);
            if (!parsedForm) {
                dispatch(errorFullSearch());
            } else {
                dispatch(receiveFullSearch({records: result.records, schema: parsedForm}));
            }
        };
        const onRequest = () => {
            dispatch(requestFullSearch());
        };
        const onError = () => {
            dispatch(errorFullSearch());
        };

        this.makeRequest({getResult, onReceive, onError, onRequest});
    }

    async getProfile(
        id: string,
        profileType: ProfileType,
        handleRequest: () => void,
        handleReceive: (profile: {
            fields: Attribute[];
            info: UserRole | ProjectRequestStatus | string;
            firstList: any[];
            hasWarning: boolean;
            schemaContentId: string;
            modifyAllowed: boolean;
            blocked: boolean;
            status: ProfileStatus;
        }) => void,
        handleError: () => void
    ) {
        const user = this.store.getState().user;
        const {userId, role: userRole} = user.isAuthed
            ? user
            : {userId: '', role: UserRole.Initial};
        const getResult = async () => {
            if (!user.isAuthed) {
                throw new Error();
            }
            if (profileType === ProfileType.Project && user.role === UserRole.Student) {
                return await this.get(
                    `formatted/project-for-student?projectId=${id}&studentId=${userId}`
                );
            }
            return await this.get(`formatted/${profileType}-profile/${id}`);
        };
        const onReceive = (result: ApiResponse) => {
            const profile = parseProfile(result, profileType, userRole);
            if (profile) {
                handleReceive(profile);
            } else {
                handleError();
            }
        };

        this.makeRequest({
            getResult,
            onRequest: handleRequest,
            onReceive,
            onError: handleError,
        });
    }

    async authorize(
        req: {[k: string]: AttributeValue},
        onSubmit: () => void,
        path: string,
        additional: {}
    ) {
        const getResult = async () => {
            return await this.get(path, {
                method: 'POST',
                body: JSON.stringify({
                    ...req,
                    content: JSON.stringify(req.content),
                    ...additional,
                }),
            });
        };
        const onReceive = () => {
            onSubmit();
            redirect();
        };
        const onRetry = () => {
            this.submitDefaultForm(req, onSubmit, path, additional);
        };
        const errorMessage = 'Ошибка отправки формы.';

        this.makeRequest({getResult, onReceive, onRetry, errorMessage});
    }

    async saveProfile(id: string, type: string, newProfile: ProfileOrSearchItem) {
        if (type === ProfileType.Activity) {
            newProfile.course = newProfile.course ? Number(newProfile.course) : newProfile.course;
            return await this.get(`formatted/activity/update/${id}`, {
                method: 'POST',
                body: JSON.stringify(newProfile),
            });
        }

        return await this.get(`formatted/${type}-profile/${id}`, {
            method: 'POST',
            body: JSON.stringify(newProfile),
        });
    }

    async submitDefaultForm(
        req: {[k: string]: AttributeValue},
        onSubmit: () => void,
        path: string,
        additional: {}
    ) {
        const getResult = async () => {
            return await this.get(path, {
                method: 'POST',
                body: JSON.stringify({
                    ...req,
                    content: JSON.stringify(req.content),
                    ...additional,
                }),
            });
        };
        const onReceive = () => {
            onSubmit();
        };
        const onRetry = () => {
            this.submitDefaultForm(req, onSubmit, path, additional);
        };
        const errorMessage = 'Ошибка отправки формы.';

        this.makeRequest({getResult, onReceive, onRetry, errorMessage});
    }

    async requestChats(userId: string) {
        const dispatch = this.store.dispatch;

        const getResult = async () => {
            // Все чаты создаются 123e4567-e89b-12d3-a000-000000000000 ?
            return await this.get(`formatted/mail-groups/${userId}`);
        };
        const onRequest = () => {
            dispatch(requestChats());
        };
        const onReceive = (result: ApiResponse) => {
            if (!('mail-fields' in result && 'groups' in result)) {
                dispatch(errorChats());
                return;
            }
            const attributes = Object.keys(result['mail-fields']).reduce(
                (res: {[k in UserRole]?: Attribute[]}, item: string) => {
                    const userRole = parseUserRole(item);
                    res[userRole] = parseFields(result['mail-fields'][userRole]);
                    return res;
                },
                {}
            ) as AttributesByUserRole;
            const chats = parseChats(result.groups);
            dispatch(receiveChats(attributes, chats));
        };
        const onError = () => {
            dispatch(errorChats());
        };

        this.makeRequest({getResult, onRequest, onReceive, onError});
    }

    async requestChatsHistoryLog(chatId: string, chatIndex: number) {
        const dispatch = this.store.dispatch;

        const getResult = async () => {
            return await this.get(`mail-group/history?id=${chatId}`);
        };
        const onRequest = () => {
            dispatch(requestHistoryLogs(chatIndex));
        };
        const onReceive = (result: ApiResponse) => {
            dispatch(receiveHistoryLogs(chatIndex, parseHistoryLogs(result)));
        };
        const onError = () => {
            dispatch(errorHistoryLogs(chatIndex));
        };

        this.makeRequest({getResult, onRequest, onReceive, onError});
    }

    // На успех и на ошибку только изменение глобального состояния
    async requestAllUsersForMailGroups(
        handleRequest: () => void,
        handleReceive: (result: ProfileOrSearchItem[]) => void,
        handleError: () => void
    ) {
        const getResult = async () => {
            return await this.get(`formatted/mail-groups/users`);
        };
        const onRequest = () => {
            handleRequest();
        };
        const onReceive = (result: ApiResponse) => {
            const users = parseLinks(result.users, LinkType.User);
            handleReceive(users);
        };
        const onError = () => {
            handleError();
        };

        this.makeRequest({getResult, onRequest, onReceive, onError});
    }

    async getAllQuestionnaires(
        handleRequest: () => void,
        handleReceive: (result: ProfileOrSearchItem[]) => void,
        handleError: () => void
    ) {
        const getResult = async () => {
            return await this.get('questionnaires/');
        };
        const onRequest = () => {
            handleRequest();
        };
        const onReceive = (result: ApiResponse) => {
            const questionnaires = parseLinks(result, LinkType.Questionnaire);
            handleReceive(questionnaires);
        };
        const onError = () => {
            handleError();
        };

        this.makeRequest({getResult, onRequest, onReceive, onError});
    }

    async getAllUsersQuestionnaires(
        profileId: string,
        handleRequest: () => void,
        handleReceive: (result: ProfileOrSearchItem[]) => void,
        handleError: () => void
    ) {
        const getResult = async () => {
            return await this.get(`questionnaires/${profileId}`);
        };
        const onRequest = () => {
            handleRequest();
        };
        const onReceive = (result: ApiResponse) => {
            handleReceive(
                parseForms(
                    result.records.map(({id, schema, schemaContent}: ApiResponse) => ({
                        ...schema,
                        schemaContent,
                        id,
                    })),
                    FormMode.Fill
                )
            );
        };
        const onError = () => {
            handleError();
        };

        this.makeRequest({getResult, onRequest, onReceive, onError});
    }

    // Эталон: На успех - меняем локальное состояние, на ошибку - тостер
    async submitQuestionnaire(formId: string, content: string, onSuccess: () => void) {
        const getResult = async () => {
            return await this.get(`questionnaires/${formId}`, {
                method: 'POST',
                body: JSON.stringify({schemaContent: content}),
            });
        };
        const onReceive = () => {
            onSuccess();
        };
        const onRetry = () => {
            this.submitQuestionnaire(formId, content, onSuccess);
        };
        const errorMessage = 'Ошибка отправки формы. Повторите позднее';

        this.makeRequest({getResult, onReceive, onRetry, errorMessage});
    }

    // Эталон: На успех - меняем внешнее состояние, на ошибку - тостер
    async saveChat(chatIndex: number, chat: Chat, logs: HistoryLog[], onSuccess: () => void) {
        const state = this.store.getState();
        const userId = state.user.isAuthed ? state.user.userId : '';
        const dispatch = this.store.dispatch;
        const isCreation = !chat.id || chat.id === DEFAULT_CHAT_ID;
        const getResult = async () => {
            if (!chat.name || !chat.users.length) {
                throw new Error();
            }
            const path = `formatted/mail-group/${isCreation ? '' : chat.id}`;
            const randomIdForLog = getRandomId();

            const hlogs = isCreation
                ? [
                      {
                          id: randomIdForLog,
                          date: new Date().toISOString(),
                          message: 'Группа контактов создана',
                      },
                  ]
                : logs;

            const result = await this.get(path, {
                method: 'POST',
                body: JSON.stringify({
                    createdBy: userId,
                    logs: hlogs,
                    name: chat.name,
                    users: chat.users.map(({id}) => id),
                }),
            });
            return {
                id: isCreation ? result.id : chat.id,
                logs: hlogs,
            };
        };
        const onReceive = ({id, logs}: ApiResponse) => {
            if (chatIndex > -1) {
                if (isCreation) {
                    dispatch(editChat(chatIndex, {id}));
                }
                dispatch(addHistoryLogs(chatIndex, parseHistoryLogs(logs)));
            }

            onSuccess();
        };
        const onRetry = () => {
            this.saveChat(chatIndex, chat, logs, onSuccess);
        };
        const errorMessage = 'Ошибка сохранения группы контактов.';

        this.makeRequest({getResult, onReceive, onRetry, errorMessage});
    }

    // Эталон: Только тостеры, без состояний
    async sendForm(chatIndex: number, chatId: string, formId: string, formName: string) {
        const dispatch = this.store.dispatch;
        const getResult = async () => {
            return await this.get(`questionnaires/`, {
                method: 'POST',
                body: JSON.stringify({schemaId: formId, mailGroupId: chatId}),
            });
        };
        const onReceive = (response: ApiResponse) => {
            dispatch(addHistoryLogs(chatIndex, parseHistoryLogs(response)));
        };
        const onRetry = () => {
            this.sendForm(chatIndex, chatId, formId, formName);
        };
        const errorMessage = 'Ошибка отправки формы.';
        const successMessage = 'Форма отправлена!';

        this.makeRequest({
            getResult,
            onReceive,
            onRetry,
            errorMessage,
            successMessage,
        });
    }

    async getFormStats(formIndex: number, formId: string, filter?: Attribute[]) {
        const dispatch = this.store.dispatch;
        const params = filter ? formSearchUrl(filter) : '';

        const getResult = async () => {
            return await this.get(`search/questionnaires/${formId}${params}`);
        };
        const onRequest = () => {
            dispatch(requestFormStats(formIndex));
        };
        const onReceive = (result: ApiResponse) => {
            dispatch(
                successFormStats(
                    formIndex,
                    result.records.map(({schemaContent, ...rest}: ApiResponse) => ({
                        ...rest,
                        content: schemaContent ? JSON.parse(schemaContent) : {},
                    }))
                )
            );
        };
        const onError = () => {
            dispatch(errorFormStats(formIndex));
        };

        this.makeRequest({getResult, onRequest, onReceive, onError});
    }

    // Эталон: На всё - меняем локальное состояние
    async getAllStudentsProjects(
        profileId: string,
        handleRequest: () => void,
        handleReceive: (result: ProfileOrSearchItem[]) => void,
        handleError: () => void
    ) {
        const getResult = async () => {
            return await this.get(`formatted/projects/student/${profileId}`);
        };
        const onRequest = () => {
            handleRequest();
        };
        const onReceive = (result: ApiResponse) => {
            const res = parseLinks(
                result.map(({projectNameRus}: ApiResponse) => projectNameRus),
                LinkType.Project
            );
            handleReceive(res);
        };
        const onError = () => {
            handleError();
        };

        this.makeRequest({getResult, onRequest, onReceive, onError});
    }

    async getAllStudentsProjectApplications(
        profileId: string,
        handleRequest: () => void,
        handleReceive: (result: ProfileOrSearchItem[]) => void,
        handleError: () => void
    ) {
        const getResult = async () => {
            return await this.get(`formatted/student/applied/projects/${profileId}`);
        };
        const onRequest = () => {
            handleRequest();
        };
        const onReceive = (result: ApiResponse) => {
            const res = result
                .filter(
                    ({projectNameRus}: ApiResponse) =>
                        projectNameRus && 'name' in projectNameRus && 'id' in projectNameRus
                )
                .map(({projectNameRus}: ApiResponse) => ({
                    name: projectNameRus.name,
                    link: `/${ProfileType.Project}?id=${projectNameRus.id}`,
                    status: ProjectApplicationStatus.Waiting,
                    applicationId: projectNameRus.id,
                }));
            handleReceive(res);
        };
        const onError = () => {
            handleError();
        };

        this.makeRequest({getResult, onRequest, onReceive, onError});
    }

    async getAllUsersProjectRequests(
        profileId: string,
        handleRequest: () => void,
        handleReceive: (result: ProfileOrSearchItem[]) => void,
        handleError: () => void
    ) {
        const getResult = async () => {
            return await this.get(`formatted/project_requests/mentor/${profileId}`);
        };
        const onRequest = () => {
            handleRequest();
        };
        const onReceive = (result: ApiResponse) => {
            const res = result
                .filter(({projectNameRus, id}: ApiResponse) => projectNameRus && id)
                .map(({projectNameRus, status, id}: ApiResponse) => ({
                    name: projectNameRus,
                    link: `/${ProfileType.ProjectRequest}?id=${id}`,
                    status: status,
                    applicationId: id,
                    isCanceled: status === ProjectRequestStatus.Rejected,
                }));
            handleReceive(res);
        };
        const onError = () => {
            handleError();
        };

        this.makeRequest({getResult, onRequest, onReceive, onError});
    }

    async getAllUsersProjects(
        profileId: string,
        handleRequest: () => void,
        handleReceive: (result: ProfileOrSearchItem[]) => void,
        handleError: () => void
    ) {
        const getResult = async () => {
            return await this.get(`formatted/projects/mentor/${profileId}`);
        };
        const onRequest = () => {
            handleRequest();
        };
        const onReceive = (result: ApiResponse) => {
            const res = parseLinks(
                result.map(({projectNameRus}: ApiResponse) => projectNameRus),
                LinkType.Project
            );
            handleReceive(res);
        };
        const onError = () => {
            handleError();
        };

        this.makeRequest({getResult, onRequest, onReceive, onError});
    }

    async getAllStudentsActivities(
        profileId: string,
        handleRequest: () => void,
        handleReceive: (result: ProfileOrSearchItem[]) => void,
        handleError: () => void
    ) {
        const getResult = async () => {
            return await this.get(`formatted/activities/student/${profileId}`);
        };
        const onRequest = () => {
            handleRequest();
        };
        const onReceive = (result: ApiResponse) => {
            const res = parseLinks(
                result.map(({name}: ApiResponse) => name),
                LinkType.Activity
            );
            handleReceive(res);
        };
        const onError = () => {
            handleError();
        };

        this.makeRequest({getResult, onRequest, onReceive, onError});
    }

    async getAllMentorsActivities(
        profileId: string,
        handleRequest: () => void,
        handleReceive: (result: ProfileOrSearchItem[]) => void,
        handleError: () => void
    ) {
        const getResult = async () => {
            return await this.get(`utils/mentor/${profileId}/activities`);
        };
        const onRequest = () => {
            handleRequest();
        };
        const onReceive = (result: ApiResponse) => {
            const res = parseLinks(result, LinkType.Activity);
            handleReceive(res);
        };
        const onError = () => {
            handleError();
        };

        this.makeRequest({getResult, onRequest, onReceive, onError});
    }

    async getAllProjectsApplications(
        profileId: string,
        handleRequest: () => void,
        handleReceive: (result: ProfileOrSearchItem[]) => void,
        handleError: () => void
    ) {
        const getResult = async () => {
            return await this.get(`formatted/project/applied/students/${profileId}`);
        };
        const onRequest = () => {
            handleRequest();
        };
        const onReceive = (result: ApiResponse) => {
            const res = result
                .filter(({name, id}: ApiResponse) => name && 'name' in name && id)
                .map(({name, id}: ApiResponse) => ({
                    name: name.name,
                    link: `/${ProfileType.User}?id=${id}`,
                    projectId: profileId,
                    studentId: id,
                    status: ProjectApplicationStatus.Waiting,
                }));
            handleReceive(res);
        };
        const onError = () => {
            handleError();
        };

        this.makeRequest({getResult, onRequest, onReceive, onError});
    }

    async getAllActivitiesProjects(
        profileId: string,
        handleRequest: () => void,
        handleReceive: (result: ProfileOrSearchItem[]) => void,
        handleError: () => void
    ) {
        const user = this.store.getState().user;
        const {userId: id} = user.isAuthed ? user : {userId: ''};
        const getResult = async () => {
            return await this.get(`formatted/projects/activity/${profileId}?userId=${id}`);
        };
        const onRequest = () => {
            handleRequest();
        };
        const onReceive = (result: ApiResponse) => {
            const res = result
                .filter(
                    ({projectNameRus, id}: ApiResponse) =>
                        projectNameRus && 'name' in projectNameRus && id
                )
                .map(({projectNameRus, id, isApplied}: ApiResponse) => ({
                    name: projectNameRus.name,
                    link: `/${ProfileType.Project}?id=${id}`,
                    isRequested: isApplied,
                    projectId: id,
                }));
            handleReceive(res);
        };
        const onError = () => {
            handleError();
        };

        this.makeRequest({getResult, onRequest, onReceive, onError});
    }

    async changeProjectApplicationStatus(
        projectId: string,
        studentId: string,
        status: ProjectApplicationStatus,
        onSuccess: () => void
    ) {
        const getResult = async () => {
            switch (status) {
                case ProjectApplicationStatus.Waiting: {
                    return;
                }
                case ProjectApplicationStatus.Rejected: {
                    return await this.get(`formatted/student/applied/projects/decline`, {
                        method: 'POST',
                        body: JSON.stringify({projectId, studentId}),
                    });
                }
                case ProjectApplicationStatus.Accepted: {
                    return await this.get(`formatted/student/applied/projects/accept`, {
                        method: 'POST',
                        body: JSON.stringify({projectId, studentId}),
                    });
                }
            }
        };
        const onReceive = () => {
            onSuccess();
        };
        const onRetry = () => {
            this.changeProjectApplicationStatus(projectId, studentId, status, onSuccess);
        };
        const errorMessage = 'Ошибка изменения статуса заявки. Повторите позднее';

        this.makeRequest({getResult, onReceive, onRetry, errorMessage});
    }

    // Отмена заявки на проект студентом.
    async cancelProjectApplication(applicationOrRequestId: string, handleReceive: () => void) {
        const user = this.store.getState().user;
        const {userId: id} = user.isAuthed ? user : {userId: ''};
        const getResult = async () => {
            return await this.get(`formatted/student/applied/projects/cancel`, {
                method: 'POST',
                body: JSON.stringify({
                    studentId: id,
                    projectId: applicationOrRequestId,
                }),
            });
        };
        const onReceive = () => {
            handleReceive();
        };
        const onRetry = () => {
            this.cancelProjectApplication(applicationOrRequestId, handleReceive);
        };
        const errorMessage = 'Ошибка отмены заявки.';
        const successMessage = 'Заявка отменена!';

        this.makeRequest({
            getResult,
            onReceive,
            onRetry,
            errorMessage,
            successMessage,
        });
    }

    async reapplyForProjectApplication(applicationOrRequestId: string, handleReceive: () => void) {
        const user = this.store.getState().user;
        const {userId: id} = user.isAuthed ? user : {userId: ''};
        const getResult = async () => {
            return await this.get(`formatted/student/applied/projects`, {
                method: 'POST',
                body: JSON.stringify({
                    studentId: id,
                    projectId: applicationOrRequestId,
                }),
            });
        };
        const onReceive = () => {
            handleReceive();
        };
        const onRetry = () => {
            this.reapplyForProjectApplication(applicationOrRequestId, handleReceive);
        };
        const errorMessage = 'Ошибка подачи заявки.';
        const successMessage = 'Заявка успешно подана!';

        this.makeRequest({
            getResult,
            onReceive,
            onRetry,
            errorMessage,
            successMessage,
        });
    }

    async cancelProjectRequest(projectRequestId: string, handleReceive: () => void) {
        const getResult = async () => {
            return await this.get(
                `formatted/project_request/set-status?id=${projectRequestId}&status=false`,
                {
                    method: 'POST',
                }
            );
        };
        const onReceive = () => {
            handleReceive();
        };
        const onRetry = () => {
            this.cancelProjectRequest(projectRequestId, handleReceive);
        };
        const errorMessage = 'Ошибка отмены заявки.';
        const successMessage = 'Заявка отменена!';

        this.makeRequest({
            getResult,
            onReceive,
            onRetry,
            errorMessage,
            successMessage,
        });
    }

    async recreateProjectRequest(projectRequestId: string, handleReceive: () => void) {
        const getResult = async () => {
            return await this.get(
                `formatted/project_request/set-status?id=${projectRequestId}&status=true`,
                {
                    method: 'POST',
                }
            );
        };
        const onReceive = () => {
            handleReceive();
        };
        const onRetry = () => {
            this.recreateProjectRequest(projectRequestId, handleReceive);
        };
        const errorMessage = 'Ошибка подачи заявки.';
        const successMessage = 'Заявка успешно подана!';

        this.makeRequest({
            getResult,
            onReceive,
            onRetry,
            errorMessage,
            successMessage,
        });
    }

    async addProjectRequestComment(projectId: string, intent: Intent, onSuccess: () => void) {
        const getResult = async () => {
            switch (intent) {
                case Intent.PRIMARY:
                case Intent.SUCCESS: {
                    // утвердить проект
                    return await this.get(`formatted/project_request/accept/${projectId}`, {
                        method: 'POST',
                    });
                }
                case Intent.DANGER:
                case Intent.WARNING: {
                    // отклонить проект
                    return await this.get(`formatted/project_request/reject/${projectId}`, {
                        method: 'POST',
                    });
                }
                default: {
                    throw new Error('Unknown request type!');
                }
            }
        };
        const onReceive = () => {
            onSuccess();
        };
        const onRetry = () => {
            this.addProjectRequestComment(projectId, intent, onSuccess);
        };
        const errorMessage = 'Ошибка изменения статуса заявки';

        this.makeRequest({getResult, onReceive, onRetry, errorMessage});
    }

    async blockProfile(
        profileId: string | undefined,
        profileType: ProfileType | undefined,
        onSuccess: () => void
    ) {
        const getResult = async () => {
            if (!profileId || !profileType) {
                throw Error();
            }
            return await this.get(`utils/block-${profileType}?id=${profileId}&block=${true}`, {
                method: 'POST',
            });
        };
        const onReceive = () => {
            onSuccess();
        };
        const onRetry = () => {
            this.blockProfile(profileId, profileType, onSuccess);
        };
        const errorMessage = 'Ошибка блокировки.';
        const successMessage = 'Блокировка прошла успешно!';

        this.makeRequest({
            getResult,
            onReceive,
            onRetry,
            errorMessage,
            successMessage,
        });
    }

    async unblockProfile(
        profileId: string | undefined,
        profileType: ProfileType | undefined,
        onSuccess: () => void
    ) {
        const getResult = async () => {
            if (!profileId || !profileType) {
                throw Error();
            }
            return await this.get(`utils/block-${profileType}?id=${profileId}&block=${false}`, {
                method: 'POST',
            });
        };
        const onReceive = () => {
            onSuccess();
        };
        const onRetry = () => {
            this.unblockProfile(profileId, profileType, onSuccess);
        };
        const errorMessage = 'Ошибка разблокировки.';
        const successMessage = 'Разблокировка прошла успешно!';

        this.makeRequest({
            getResult,
            onReceive,
            onRetry,
            errorMessage,
            successMessage,
        });
    }

    async complaintProfile(profileId: string | undefined, profileType: ProfileType | undefined) {
        const getResult = async () => {
            if (!profileId || !profileType) {
                throw Error();
            }
            return await this.get(`complaint/send`, {
                method: 'POST',
                body: JSON.stringify({
                    profile: {
                        id: profileId,
                        name: 'Кто-то',
                        type: profileType,
                    },
                    complaint: ' прислал(а) жалобу на профиль ',
                }),
            });
        };
        const onReceive = (response: ApiResponse) => {};
        const onRetry = () => {
            this.complaintProfile(profileId, profileType);
        };
        const errorMessage = 'Жалоба не отправлена.';
        const successMessage = 'Жалоба отправлена!';

        this.makeRequest({
            getResult,
            onReceive,
            onRetry,
            errorMessage,
            successMessage,
        });
    }

    async getOrganisationMembers(
        profileId: string,
        handleRequest: () => void,
        handleReceive: (result: ProfileOrSearchItem[]) => void,
        handleError: () => void
    ) {
        const getResult = async () => {
            return await this.get(`formatted/organisation/${profileId}/employers`);
        };
        const onRequest = () => {
            handleRequest();
        };
        const onReceive = (result: ApiResponse) => {
            const res = parseLinks(result, LinkType.User);
            handleReceive(res);
        };
        const onError = () => {
            handleError();
        };

        this.makeRequest({getResult, onRequest, onReceive, onError});
    }

    async getOrganisationStructure(
        profileId: string,
        handleRequest: () => void,
        handleReceive: (result: ProfileOrSearchItem[]) => void,
        handleError: () => void
    ) {
        const getResult = async () => {
            return await this.get(`formatted/organisation/descendants/${profileId}`);
        };
        const onRequest = () => {
            handleRequest();
        };
        const onReceive = (result: ApiResponse) => {
            handleReceive([
                {
                    items: result
                        .filter(({id, name}: ApiResponse) => id && name)
                        .map(({id, name, type, hasChildren}: ApiResponse) => ({
                            id,
                            label: (
                                <Link
                                    to={`/${type || LinkType.Organisation}?id=${id}`}
                                    className='profile-lists__link'
                                >
                                    {name}
                                </Link>
                            ),
                            hasCaret: hasChildren,
                        })),
                },
            ]);
        };
        const onError = () => {
            handleError();
        };

        this.makeRequest({getResult, onRequest, onReceive, onError});
    }

    async deleteComment(commentId: string, onSuccess: () => void) {
        const getResult = async () => {
            return await this.get(`post/delete?postId=${commentId}`, {
                method: 'POST',
            });
        };
        const onReceive = () => {
            onSuccess();
        };
        const onRetry = () => {
            this.deleteComment(commentId, onSuccess);
        };
        const errorMessage = 'Ошибка удаления поста.';
        const successMessage = 'Пост удален!';

        this.makeRequest({
            getResult,
            onReceive,
            onRetry,
            errorMessage,
            successMessage,
        });
    }

    async editComment(commentId: string, newComment: ProfileOrSearchItem, onSuccess: () => void) {
        const getResult = async () => {
            const req: ProfileOrSearchItem = {};
            if (newComment.text) {
                req.text = newComment.text;
            }
            if (newComment.file && newComment.file.id) {
                req.file = newComment.file.id;
            }
            return await this.get(`post/update?postId=${commentId}`, {
                method: 'POST',
                body: JSON.stringify(req),
            });
        };
        const onReceive = () => {
            onSuccess();
        };
        const onRetry = () => {
            this.editComment(commentId, newComment, onSuccess);
        };
        const errorMessage = 'Ошибка редактирования поста.';

        this.makeRequest({getResult, onReceive, onRetry, errorMessage});
    }

    async startNewAcademicYear(onSuccess: () => void) {
        const getResult = async () => {
            localStorage.setItem('startNewAcademicYear', new Date().toISOString());
            return await this.get(`utils/start-academic-year`, {
                method: 'POST',
            });
        };
        const onReceive = () => {
            onSuccess();
        };
        const onRetry = () => {
            this.startNewAcademicYear(onSuccess);
        };
        const errorMessage = 'Попытка начать новый учебный год завершилась с ошибкой.';
        const successMessage = 'Учебный год начат!';

        this.makeRequest({
            getResult,
            onReceive,
            onRetry,
            errorMessage,
            successMessage,
        });
    }

    async getLastAcademicYearStartDate(
        handleRequest: () => void,
        handleReceive: (result: number) => void,
        handleError: () => void
    ) {
        const getResult = async () => {
            return {
                date: localStorage.getItem('startNewAcademicYear') || new Date().toISOString(),
            };
        };
        const onRequest = () => {
            handleRequest();
        };
        const onReceive = ({date}: ApiResponse) => {
            handleReceive(date);
        };
        const onError = () => {
            handleError();
        };

        this.makeRequest({getResult, onRequest, onReceive, onError});
    }

    async getStudentGroupsList(
        handleRequest: () => void,
        handleReceive: (result: string[]) => void,
        handleError: () => void
    ) {
        const getResult = async () => {
            return await this.get(`utils/student-groups`);
        };
        const onRequest = () => {
            handleRequest();
        };
        const onReceive = (groups: ApiResponse) => {
            handleReceive(groups);
        };
        const onError = () => {
            handleError();
        };

        this.makeRequest({getResult, onRequest, onReceive, onError});
    }

    async setNewStudentGroupsList(newList: string[], onSuccess: () => void) {
        const getResult = async () => {
            return await this.get(`utils/student-group/update`, {
                method: 'POST',
                body: JSON.stringify(newList),
            });
        };
        const onReceive = (response: ApiResponse) => {
            onSuccess();
        };
        const onRetry = () => {
            this.setNewStudentGroupsList(newList, onSuccess);
        };
        const errorMessage = 'Ошибка сохранения нового списка групп.';

        this.makeRequest({getResult, onReceive, onRetry, errorMessage});
    }

    async actualizeContacts(onSuccess: () => void) {
        const getResult = async () => {
            localStorage.setItem('actualizeContacts', new Date().toISOString());
            return [];
        };
        const onReceive = () => {
            onSuccess();
        };
        const onRetry = () => {
            this.actualizeContacts(onSuccess);
        };
        const errorMessage = 'Попытка актуализировать контакты завершилась с ошибкой.';
        const successMessage = 'Запрос на актуализацию контактов отправлен!';

        this.makeRequest({
            getResult,
            onReceive,
            onRetry,
            errorMessage,
            successMessage,
        });
    }

    async getLastContactsActualisationDate(
        handleRequest: () => void,
        handleReceive: (result: number) => void,
        handleError: () => void
    ) {
        const getResult = async () => {
            return {
                date: localStorage.getItem('actualizeContacts') || new Date().toISOString(),
            };
        };
        const onRequest = () => {
            handleRequest();
        };
        const onReceive = ({date}: ApiResponse) => {
            handleReceive(date);
        };
        const onError = () => {
            handleError();
        };

        this.makeRequest({getResult, onRequest, onReceive, onError});
    }

    async addToArchive(index: number, form: Form) {
        const getResult = async () => {
            return await this.get(`formatted/schema/set-archived?id=${form.id}&archived=${true}`, {
                method: 'POST',
            });
        };
        const onReceive = () => {
            this.store.dispatch(addArchiveForm(index, form));
        };
        const onRetry = () => {
            this.addToArchive(index, form);
        };
        const errorMessage = 'Не удалось архивировать форму.';
        const successMessage = 'Форма архивирована!';

        this.makeRequest({
            getResult,
            onReceive,
            onRetry,
            errorMessage,
            successMessage,
        });
    }

    async deleteFromArchive(index: number, form: Form) {
        const getResult = async () => {
            return await this.get(`formatted/schema/set-archived?id=${form.id}&archived=${false}`, {
                method: 'POST',
            });
        };
        const onReceive = () => {
            this.store.dispatch(removeArchiveForm(index, form));
        };
        const onRetry = () => {
            this.deleteFromArchive(index, form);
        };
        const errorMessage = 'Не удалось разархивировать форму.';
        const successMessage = 'Форма разархивирована!';

        this.makeRequest({
            getResult,
            onReceive,
            onRetry,
            errorMessage,
            successMessage,
        });
    }

    async setNotificationSeen(notificationId: string, onSuccess: () => void) {
        const getResult = async () => {
            return await this.get(`notification/${notificationId}`);
        };
        const onReceive = () => {
            onSuccess();
        };
        const onRetry = () => {
            this.setNotificationSeen(notificationId, onSuccess);
        };
        const errorMessage = 'Не удалось пометить нотификацию как просмотренную.';

        this.makeRequest({getResult, onReceive, onRetry, errorMessage});
    }

    async getAllNotifications(
        handleRequest: () => void,
        handleReceive: (result: ApiResponse[]) => void,
        handleError: () => void
    ) {
        const state = this.store.getState();
        const userId = state.user.isAuthed ? state.user.userId : '';
        const getResult = async () => {
            return await this.get(`notifications/user/${userId}`);
        };
        const onRequest = () => {
            handleRequest();
        };
        const onReceive = (notifications: ApiResponse[]) => {
            const res = notifications
                .filter(
                    ({id, createdBy}: ApiResponse) =>
                        id && createdBy && parseLink(createdBy, LinkType.User)
                )
                .map(({id, createdBy, action, text, isRead, target}: ApiResponse) => ({
                    id,
                    from: parseLink(createdBy, LinkType.User),
                    message: `${action || ''} ${text || ''}`,
                    read: isRead || false,
                    about: target ? parseLink(target) : null,
                }));
            handleReceive(res);
        };
        const onError = () => {
            handleError();
        };

        this.makeRequest({getResult, onRequest, onReceive, onError});
    }

    // На успех - глобальный стейт + тостер, На ошибку - тостер
    async deleteChat(chatIndex: number, chatId: string) {
        const dispatch = this.store.dispatch;
        const getResult = async () => {
            return await this.get(`formatted/mail-group/${chatId}/delete`, {
                method: 'POST',
            });
        };
        const onReceive = () => {
            dispatch(deleteChat(chatIndex));
        };
        const onRetry = () => {
            this.deleteChat(chatIndex, chatId);
        };
        const successMessage = 'Группа контактов удалена!';
        const errorMessage = 'Не удалось удалить группу контактов';
        this.makeRequest({
            getResult,
            onReceive,
            onRetry,
            errorMessage,
            successMessage,
        });
    }

    async getAllStudentMentors(
        profileId: string,
        handleRequest: () => void,
        handleReceive: (result: ProfileOrSearchItem[]) => void,
        handleError: () => void
    ) {
        const getResult = async () => {
            return await this.get(`utils/student/mentors?id=${profileId}`);
        };
        const onRequest = () => {
            handleRequest();
        };
        const onReceive = (result: ApiResponse) => {
            const res = parseLinks(result, LinkType.User);
            handleReceive(res);
        };
        const onError = () => {
            handleError();
        };

        this.makeRequest({getResult, onRequest, onReceive, onError});
    }

    async getAllMentorMenties(
        profileId: string,
        handleRequest: () => void,
        handleReceive: (result: ProfileOrSearchItem[]) => void,
        handleError: () => void
    ) {
        const getResult = async () => {
            return await this.get(`utils/mentor/students?id=${profileId}`);
        };
        const onRequest = () => {
            handleRequest();
        };
        const onReceive = (result: ApiResponse) => {
            const res = parseLinks(result, LinkType.User);
            handleReceive(res);
        };
        const onError = () => {
            handleError();
        };

        this.makeRequest({getResult, onRequest, onReceive, onError});
    }

    // Эталон: На успех - меняем локальное и глобальное состояние и показываем тостер, на ошибку - тостер
    async sendNotification(
        mailGroupId: string,
        mailGroupIndex: number,
        text: string,
        onSuccess: () => void
    ) {
        const dispatch = this.store.dispatch;
        const getResult = async () => {
            return await this.get(`formatted/mail-groups/send-notification/${mailGroupId}`, {
                method: 'POST',
                body: JSON.stringify({message: text}),
            });
        };
        const onReceive = (log: ApiResponse[]) => {
            onSuccess();
            dispatch(addHistoryLogs(mailGroupIndex, parseHistoryLogs([log])));
        };
        const onRetry = () => {
            this.sendNotification(mailGroupId, mailGroupIndex, text, onSuccess);
        };
        const errorMessage = 'Ошибка отправки нотификации. Повторите позднее';
        const successMessage = 'Нотификация отправлена';

        this.makeRequest({
            getResult,
            onReceive,
            onRetry,
            errorMessage,
            successMessage,
        });
    }

    async addPost(
        profileId: string,
        profileType: ProfileType,
        message: UserAction,
        text: string,
        file: {id: string; name: string; type: 'file'} | null,
        onSuccess: (id: string) => void
    ) {
        const state = this.store.getState();
        const {isAuthed} = state.user;
        if (!isAuthed) {
            return;
        }
        // @ts-ignore
        const {userId} = state.user;
        const getResult = async () => {
            return await this.get(`post/create?profileId=${profileId}`, {
                method: 'POST',
                body: JSON.stringify({
                    name: userId,
                    text,
                    file: file && file.id,
                    message,
                    profileType,
                }),
            });
        };
        const onReceive = ({id}: ApiResponse) => {
            onSuccess(id);
        };
        const onRetry = () => {
            this.addPost(profileId, profileType, message, text, file, onSuccess);
        };
        const errorMessage = 'Ошибка отправки комментария.';

        this.makeRequest({getResult, onReceive, onRetry, errorMessage});
    }

    async getAllPosts(
        profileId: string,
        handleRequest: () => void,
        handleReceive: (result: ProfileOrSearchItem[]) => void,
        handleError: () => void
    ) {
        const getResult = async () => {
            return await this.get(`posts/get?profileId=${profileId}`);
        };
        const onRequest = () => {
            handleRequest();
        };
        const onReceive = (result: ApiResponse) => {
            handleReceive(parsePosts(result));
        };
        const onError = () => {
            handleError();
        };

        this.makeRequest({getResult, onRequest, onReceive, onError});
    }

    async getActivityVedomost(
        profileId: string,
        handleRequest: () => void,
        handleReceive: (result: ProfileOrSearchItem) => void,
        handleError: () => void
    ) {
        const getResult = async () => {
            return await this.get(`sheet/activity/${profileId}`, {
                method: 'GET',
            });
        };
        const onRequest = () => {
            handleRequest();
        };
        const onReceive = (result: ApiResponse) => {
            const fields = parseFields(
                result.fields.map(
                    ({name, title}: {name: string; title: string}, index: number) => ({
                        name,
                        title,
                        attribute: {
                            id: String(index),
                            usage: 'short_text',
                            name,
                            description: '',
                            title,
                            placeholder: '',
                            hint: '',
                            mandatory: true,
                            validators: [],
                        },
                    })
                )
            );
            if (!fields.length) {
                handleError();
            } else {
                handleReceive({records: result.records, fields});
            }
        };
        const onError = () => {
            handleError();
        };

        this.makeRequest({getResult, onRequest, onReceive, onError});
    }

    async getProjectVedomost(
        profileId: string,
        handleRequest: () => void,
        handleReceive: (result: ProfileOrSearchItem) => void,
        handleError: () => void
    ) {
        const getResult = async () => {
            return await this.get(`sheet/project/${profileId}`);
        };
        const onRequest = () => {
            handleRequest();
        };
        const onReceive = (result: ApiResponse) => {
            const fields = parseFields(
                result.fields.map(
                    (
                        {
                            name,
                            title,
                            isModifyAllowed,
                        }: {name: string; title: string; isModifyAllowed: boolean},
                        index: number
                    ) => ({
                        name,
                        title,
                        modifyAllowed: isModifyAllowed,
                        attribute: {
                            id: String(index),
                            usage: 'short_text',
                            name,
                            description: '',
                            title,
                            placeholder: '',
                            hint: '',
                            mandatory: true,
                            validators: [],
                        },
                    })
                )
            );
            if (!fields.length) {
                handleError();
            } else {
                handleReceive({records: result.records, fields});
            }
        };
        const onError = () => {
            handleError();
        };

        this.makeRequest({getResult, onRequest, onReceive, onError});
    }

    async changeMark(
        mark: string,
        projectId: string,
        studentId: string,
        stageId: string,
        gradeType: string,
        onSuccess: (mark: string) => void
    ) {
        const getResult = async () => {
            const grade = mark === '' ? null : parseInt(mark);
            if (grade !== null && (grade > 10 || grade < 0)) {
                throw new Error();
            }
            if (!grade && grade !== null && grade !== 0) {
                throw new Error();
            }

            await this.get(
                `sheet/project/update?projectId=${projectId}&studentId=${studentId}&stageId=${stageId}&gradeType=${gradeType}&grade=${mark}`,
                {
                    method: 'POST',
                }
            );
            return grade;
        };
        const onReceive = (grade: ApiResponse) => {
            onSuccess(grade === null ? '' : String(grade));
        };
        const onRetry = () => {
            this.changeMark(mark, projectId, studentId, stageId, gradeType, onSuccess);
        };
        const errorMessage = 'Ошибка задания оценки.';
        const successMessage = 'Оценка выствлена!';

        this.makeRequest({
            getResult,
            onReceive,
            onRetry,
            errorMessage,
            successMessage,
        });
    }

    async addStudentToActivity(studentId: string, activityName: string, onSuccess: () => void) {
        const getResult = async () => {
            return await this.get(
                `utils/activity/add-student?studentId=${studentId}&activityName=${activityName}`,
                {
                    method: 'POST',
                }
            );
        };
        const onReceive = () => {
            onSuccess();
        };
        const onRetry = () => {
            this.addStudentToActivity(studentId, activityName, onSuccess);
        };
        const errorMessage = 'Не удалось добавить студента к активности';

        this.makeRequest({getResult, onReceive, onRetry, errorMessage});
    }

    async deleteFileFromTask(taskId: string, fileId: string, onSuccess: () => void) {
        const getResult = async () => {
            return await this.get(`file/${fileId}`, {
                method: 'DELETE',
            });
        };
        const onReceive = () => {
            onSuccess();
        };
        const onRetry = () => {
            this.deleteFileFromTask(taskId, fileId, onSuccess);
        };
        const errorMessage = 'Ошибка удаления файла. Обновите страницу';
        const successMessage = 'Файл удален из системы';

        this.makeRequest({
            getResult,
            onReceive,
            onRetry,
            errorMessage,
            successMessage,
        });
    }

    async onOffProjectApplications(activityName: string, status: boolean, onSuccess: () => void) {
        const getResult = async () => {
            return await this.get(
                `formatted/project-apply-period?name=${activityName}&status=${status}`,
                {
                    method: 'POST',
                }
            );
        };
        const onReceive = () => {
            onSuccess();
        };
        const onRetry = () => {
            this.onOffProjectApplications(activityName, status, onSuccess);
        };
        const errorMessage = 'Не удалось применить изменения';

        this.makeRequest({getResult, onReceive, onRetry, errorMessage});
    }

    async setStudentStatus(studentId: string, status: string, onSuccess: () => void) {
        const getResult = async () => {
            return await this.get(`utils/set-student-status?id=${studentId}&status=${status}`, {
                method: 'POST',
            });
        };
        const onReceive = () => {
            onSuccess();
        };
        const onRetry = () => {
            this.setStudentStatus(studentId, status, onSuccess);
        };
        const errorMessage = 'Не удалось изменить статус студента';

        this.makeRequest({getResult, onReceive, onRetry, errorMessage});
    }

    async setComplaintSeen(notificationId: string, onSuccess: () => void) {
        const getResult = async () => {
            return await this.get(`complaint/set-viewed?id=${notificationId}&viewed=${true}`, {
                method: 'POST',
            });
        };
        const onReceive = (response: ApiResponse) => {
            onSuccess();
        };
        const onRetry = () => {
            this.setComplaintSeen(notificationId, onSuccess);
        };
        const errorMessage = 'Не удалось пометить жалобу как просмотренную.';

        this.makeRequest({getResult, onReceive, onRetry, errorMessage});
    }

    async getAllComplaints(
        handleRequest: () => void,
        handleReceive: (result: ApiResponse[]) => void,
        handleError: () => void
    ) {
        const getResult = async () => {
            return await this.get(`complaints`);
        };
        const onRequest = () => {
            handleRequest();
        };
        const onReceive = (notifications: ApiResponse[]) => {
            const res = notifications
                .filter(
                    ({id, createdBy, profile}: ApiResponse) =>
                        id && parseLink(createdBy, LinkType.User) && parseLink(profile)
                )
                .map(({id, createdBy, complaint, isViewed, profile}: ApiResponse) => ({
                    id,
                    from: parseLink(createdBy, LinkType.User),
                    message: `${complaint || ' прислал(а) жалобу на профиль '}`,
                    read: isViewed || false,
                    about: parseLink(profile),
                }));
            handleReceive(res);
        };
        const onError = () => {
            handleError();
        };

        this.makeRequest({getResult, onRequest, onReceive, onError});
    }
}
