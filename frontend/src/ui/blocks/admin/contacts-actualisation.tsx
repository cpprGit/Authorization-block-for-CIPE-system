import {Button, Intent} from '@blueprintjs/core';
import React, {FC, memo, useCallback, useEffect, useState} from 'react';
import {AsyncStatus} from 'src/types';
import {HomePageParagraph} from 'src/ui/blocks/home-page-paragraph/home-page-paragraph';
import {LoadableView} from 'src/ui/blocks/loadable-view/loadable-view';
import {useCppwApiContext} from 'src/ui/contexts/cppw-api-context/cppw-api-context';
import {parseDate} from 'src/ui/utils/parse-date';

export const ContactsActualisation: FC = memo(() => {
    const cppwApi = useCppwApiContext();

    const [status, setStatus] = useState(AsyncStatus.Initial);
    const [lastDate, setLastDate] = useState(new Date(0));

    const startNewAcademicYear = useCallback(() => {
        cppwApi &&
            cppwApi.actualizeContacts(() => {
                setLastDate(new Date());
            });
    }, [cppwApi, setLastDate]);
    const onRetry = useCallback(() => {
        cppwApi &&
            cppwApi.getLastContactsActualisationDate(
                () => {
                    setStatus(AsyncStatus.Pending);
                },
                (date: number) => {
                    setLastDate(new Date(date));
                    setStatus(AsyncStatus.Success);
                },
                () => {
                    setStatus(AsyncStatus.Error);
                }
            );
    }, [cppwApi, setStatus]);

    useEffect(() => {
        if (status === AsyncStatus.Initial) {
            cppwApi &&
                cppwApi.getLastContactsActualisationDate(
                    () => {
                        setStatus(AsyncStatus.Pending);
                    },
                    (date: number) => {
                        setLastDate(new Date(date));
                        setStatus(AsyncStatus.Success);
                    },
                    () => {
                        setStatus(AsyncStatus.Error);
                    }
                );
        }
        // Не стоит добавлять в зависимости статус. Если он изменился, значит данный useEffect отработал и выполнил свою функцию.
        // eslint-disable-next-line react-hooks/exhaustive-deps
    }, [cppwApi, setStatus]);

    return (
        <LoadableView
            status={status}
            errorTitle='Функция временно недоступна'
            onRetry={onRetry}
            errorSubtitle={'Нажмите для повторного запроса.'}
        >
            <HomePageParagraph
                title='Актуализация контактов'
                mode={2}
                description={`Последний раз актуализация контактов производилась ${parseDate(
                    lastDate
                )}`}
            />
            <Button
                className={'new-academic-year__start-button'}
                onClick={startNewAcademicYear}
                text='Актуализировать контакты'
                intent={Intent.PRIMARY}
            />
        </LoadableView>
    );
});
