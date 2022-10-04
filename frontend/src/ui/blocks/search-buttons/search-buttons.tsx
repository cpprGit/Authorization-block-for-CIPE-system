import {
    Button,
    ButtonGroup,
    IconName,
    Intent,
    Menu,
    MenuDivider,
    MenuItem,
    Popover,
    Position,
} from '@blueprintjs/core';
import React, {FC, memo, useMemo} from 'react';
import {Link} from 'react-router-dom';
import {CppwApi, formSearchUrl} from 'src/api/api';
import {changeUserFormMode} from 'src/store/actions/user-forms.actions';
import {Attribute, FormMode, ProfileOrSearchItem, SearchType} from 'src/types';
import {useCppwApiContext} from 'src/ui/contexts/cppw-api-context/cppw-api-context';
import {BACKEND_URL, DEFAULT_CHAT_ID} from 'src/ui/utils/constants';
import './search-buttons.styl';

enum ButtonType {
    Link = 'link',
    Button = 'button',
    Menu = 'menu',
    Divider = 'divider',
}

type ButtonDescriber =
    | {
          type: ButtonType.Link;
          href: string;
          icon: IconName;
          title: string;
          download?: true;
      }
    | {
          type: ButtonType.Menu;
          icon: IconName;
          title: string;
          items: ButtonDescriber[];
      }
    | {
          type: ButtonType.Button;
          onClick: (records: ProfileOrSearchItem[], cppwApi?: CppwApi) => void;
          icon: IconName;
          title: string;
      }
    | {
          type: ButtonType.Divider;
      };

export const SEARCH_TYPE_TO_BUTTONS_MAP: {
    [k in SearchType]?: ButtonDescriber[];
} = {
    [SearchType.Projects]: [
        {
            type: ButtonType.Menu,
            icon: 'timeline-bar-chart',
            title: 'Отчеты',
            items: [
                {
                    type: ButtonType.Link,
                    title: 'Скачать таблицу',
                    icon: 'folder-shared',
                    href: `${BACKEND_URL}/api/v1/reports/search?searchType=${SearchType.Projects}`,
                    download: true,
                },
                {
                    type: ButtonType.Divider,
                },
                {
                    type: ButtonType.Link,
                    title: 'Анализ распределения проектов по компаниям',
                    icon: 'folder-shared',
                    href: `${BACKEND_URL}/api/v1/reports/projects-info?`,
                    download: true,
                },
            ],
        },
    ],
    [SearchType.Students]: [
        {
            type: ButtonType.Button,
            onClick: (records, cppwApi) => {
                cppwApi &&
                    cppwApi.saveChat(
                        -1,
                        {
                            id: DEFAULT_CHAT_ID,
                            name: 'Студенты: Новая группа контактов',
                            users: records,
                        },
                        [],
                        () => {
                            cppwApi &&
                                cppwApi.toaster.current &&
                                cppwApi.toaster.current.show({
                                    icon: 'tick',
                                    intent: Intent.SUCCESS,
                                    message: 'Новая группа контактов успешно создана',
                                });
                        }
                    );
            },
            icon: 'plus',
            title: 'Создать группу контактов',
        },
        {
            type: ButtonType.Menu,
            icon: 'timeline-bar-chart',
            title: 'Отчеты',
            items: [
                {
                    type: ButtonType.Link,
                    title: 'Скачать таблицу',
                    icon: 'folder-shared',
                    href: `${BACKEND_URL}/api/v1/reports/search?searchType=${SearchType.Students}`,
                    download: true,
                },
            ],
        },
    ],
    [SearchType.Mentors]: [
        {
            type: ButtonType.Button,
            onClick: (records, cppwApi) => {
                cppwApi &&
                    cppwApi.saveChat(
                        -1,
                        {
                            id: DEFAULT_CHAT_ID,
                            name: 'Менторы: Новая группа контактов',
                            users: records,
                        },
                        [],
                        () => {
                            cppwApi &&
                                cppwApi.toaster.current &&
                                cppwApi.toaster.current.show({
                                    icon: 'tick',
                                    intent: Intent.SUCCESS,
                                    message: 'Новая группа контактов успешно создана',
                                });
                        }
                    );
            },
            icon: 'plus',
            title: 'Создать группу контактов',
        },
        {
            type: ButtonType.Menu,
            icon: 'timeline-bar-chart',
            title: 'Отчеты',
            items: [
                {
                    type: ButtonType.Link,
                    title: 'Скачать таблицу',
                    icon: 'folder-shared',
                    href: `${BACKEND_URL}/api/v1/reports/search?searchType=${SearchType.Mentors}`,
                    download: true,
                },
            ],
        },
    ],
    [SearchType.Representatives]: [
        {
            type: ButtonType.Button,
            onClick: (records, cppwApi) => {
                cppwApi &&
                    cppwApi.saveChat(
                        -1,
                        {
                            id: DEFAULT_CHAT_ID,
                            name: 'Представители компаний: Новая группа контактов',
                            users: records,
                        },
                        [],
                        () => {
                            cppwApi &&
                                cppwApi.toaster.current &&
                                cppwApi.toaster.current.show({
                                    icon: 'tick',
                                    intent: Intent.SUCCESS,
                                    message: 'Новая группа контактов успешно создана',
                                });
                        }
                    );
            },
            icon: 'plus',
            title: 'Создать группу контактов',
        },
        {
            type: ButtonType.Menu,
            icon: 'timeline-bar-chart',
            title: 'Отчеты',
            items: [
                {
                    type: ButtonType.Link,
                    title: 'Скачать таблицу',
                    icon: 'folder-shared',
                    href: `${BACKEND_URL}/api/v1/reports/search?searchType=${SearchType.Representatives}`,
                    download: true,
                },
            ],
        },
    ],
    [SearchType.Managers]: [
        {
            type: ButtonType.Button,
            onClick: (records, cppwApi) => {
                cppwApi &&
                    cppwApi.saveChat(
                        -1,
                        {
                            id: DEFAULT_CHAT_ID,
                            name: 'Менеджеры: Новая группа контактов',
                            users: records,
                        },
                        [],
                        () => {
                            cppwApi &&
                                cppwApi.toaster.current &&
                                cppwApi.toaster.current.show({
                                    icon: 'tick',
                                    intent: Intent.SUCCESS,
                                    message: 'Новая группа контактов успешно создана',
                                });
                        }
                    );
            },
            icon: 'plus',
            title: 'Создать группу контактов',
        },
        {
            type: ButtonType.Menu,
            icon: 'timeline-bar-chart',
            title: 'Отчеты',
            items: [
                {
                    type: ButtonType.Link,
                    title: 'Скачать таблицу',
                    icon: 'folder-shared',
                    href: `${BACKEND_URL}/api/v1/reports/search?searchType=${SearchType.Managers}`,
                    download: true,
                },
            ],
        },
    ],
    [SearchType.Activities]: [
        {
            type: ButtonType.Menu,
            icon: 'timeline-bar-chart',
            title: 'Отчеты',
            items: [
                {
                    type: ButtonType.Link,
                    title: 'Скачать таблицу',
                    icon: 'folder-shared',
                    href: `${BACKEND_URL}/api/v1/reports/search?searchType=${SearchType.Activities}`,
                    download: true,
                },
            ],
        },
    ],
    [SearchType.ProjectRequests]: [
        {
            type: ButtonType.Menu,
            icon: 'timeline-bar-chart',
            title: 'Отчеты',
            items: [
                {
                    type: ButtonType.Link,
                    title: 'Скачать таблицу',
                    icon: 'folder-shared',
                    href: `${BACKEND_URL}/api/v1/reports/search?searchType=${SearchType.ProjectRequests}`,
                    download: true,
                },
            ],
        },
    ],
    [SearchType.Organisations]: [
        {
            type: ButtonType.Menu,
            icon: 'timeline-bar-chart',
            title: 'Отчеты',
            items: [
                {
                    type: ButtonType.Link,
                    title: 'Скачать таблицу',
                    icon: 'folder-shared',
                    href: `${BACKEND_URL}/api/v1/reports/search?searchType=${SearchType.Organisations}`,
                    download: true,
                },
            ],
        },
    ],

    [SearchType.Questionnaire]: [
        {
            type: ButtonType.Button,
            onClick: (records, cppwApi) => {
                if (!cppwApi) {
                    return;
                }
                const state = cppwApi.store.getState();

                const {id, index} = state.userForms.forms.reduce(
                    (currentResult: {id: string; index: number}, {id, stats}, index) => {
                        if (!stats) {
                            return currentResult;
                        }
                        const {records: formRecords} = stats;
                        if (records === formRecords) {
                            return {
                                index,
                                id,
                            };
                        }
                        return currentResult;
                    },
                    {id: '', index: -1}
                );

                if (id) {
                    cppwApi.getFormStats(index, id);
                } else {
                    cppwApi.toaster.current &&
                        cppwApi.toaster.current.show({
                            icon: 'error',
                            intent: Intent.DANGER,
                            message: 'Ошибка!',
                        });
                }
            },
            icon: 'refresh',
            title: 'Обновить',
        },
        {
            type: ButtonType.Button,
            onClick: (records, cppwApi) => {
                if (!cppwApi) {
                    return;
                }
                const state = cppwApi.store.getState();

                const {id, index} = state.userForms.forms.reduce(
                    (currentResult: {id: string; index: number}, {id, stats}, index) => {
                        if (!stats) {
                            return currentResult;
                        }
                        const {records: formRecords} = stats;
                        if (records === formRecords) {
                            return {
                                index,
                                id,
                            };
                        }
                        return currentResult;
                    },
                    {id: '', index: -1}
                );

                if (id) {
                    cppwApi.store.dispatch(changeUserFormMode(index, FormMode.View));
                } else {
                    cppwApi.toaster.current &&
                        cppwApi.toaster.current.show({
                            icon: 'error',
                            intent: Intent.DANGER,
                            message: 'Ошибка!',
                        });
                }
            },
            icon: 'cross',
            title: 'Закрыть',
        },
    ],
};

const renderMenu = (
    {title, icon, items}: {title: string; icon: IconName; items: ButtonDescriber[]},
    ind: number,
    reportParams: string
) => {
    const menu = (
        <Menu key={ind}>
            {items.map((item: ButtonDescriber, i: number) => {
                switch (item.type) {
                    case ButtonType.Link: {
                        return (
                            <MenuItem
                                text={item.title}
                                key={i}
                                icon={item.icon}
                                href={`${item.href}${item.download ? reportParams : ''}`}
                                download={item.download}
                            />
                        );
                    }
                    case ButtonType.Divider: {
                        return <MenuDivider key={i} />;
                    }
                    default: {
                        return null;
                    }
                }
            })}
        </Menu>
    );
    return (
        <Popover content={menu} position={Position.BOTTOM_LEFT}>
            <Button rightIcon='caret-down' icon={icon} text={title} />
        </Popover>
    );
};

type Props = {
    searchType: SearchType;
    records: ProfileOrSearchItem[];
    fields: Attribute[];
};
export const SearchButtons: FC<Props> = memo(({searchType, records, fields}) => {
    const cppwApi = useCppwApiContext();
    const items: ButtonDescriber[] | undefined = SEARCH_TYPE_TO_BUTTONS_MAP[searchType];
    const reportParams = useMemo(() => {
        return `${formSearchUrl(fields, '&')}&fields=${fields
            .filter(({isAdded}) => isAdded)
            .map(({realName, name}) => (realName ? realName : name))
            .join(',')}`;
    }, [fields]);

    if (!items) {
        return null;
    }

    return (
        <ButtonGroup className='search-buttons'>
            {items.map((item: ButtonDescriber, ind: number) => {
                switch (item.type) {
                    case ButtonType.Link: {
                        return (
                            <Link to={`${item.href}${item.download ? reportParams : ''}`} key={ind}>
                                <Button icon={item.icon} text={item.title} />
                            </Link>
                        );
                    }
                    case ButtonType.Button: {
                        return (
                            <Button
                                icon={item.icon}
                                key={ind}
                                text={item.title}
                                onClick={() => item.onClick(records, cppwApi)}
                            />
                        );
                    }
                    case ButtonType.Menu: {
                        return renderMenu(item, ind, reportParams);
                    }
                    default: {
                        return null;
                    }
                }
            })}
        </ButtonGroup>
    );
});
