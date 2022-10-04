import {Button, Intent} from '@blueprintjs/core';
import React, {FC, memo, useCallback, useEffect, useMemo, useState} from 'react';
import {AsyncStatus} from 'src/types';
import {HomePageParagraph} from 'src/ui/blocks/home-page-paragraph/home-page-paragraph';
import {LoadableView} from 'src/ui/blocks/loadable-view/loadable-view';
import {useCppwApiContext} from 'src/ui/contexts/cppw-api-context/cppw-api-context';
import {ELEVEN_MONTHS} from 'src/ui/utils/constants';
import {parseDate} from 'src/ui/utils/parse-date';

export const NewAcademicYear: FC = memo(() => {
    const cppwApi = useCppwApiContext();

    const [status, setStatus] = useState(AsyncStatus.Initial);
    const [lastDate, setLastDate] = useState(new Date(0));

    const nextDate = useMemo(() => new Date(Number(lastDate) + ELEVEN_MONTHS), [lastDate]);
    const disabled = useMemo(() => new Date() < nextDate, [nextDate]);

    const startNewAcademicYear = useCallback(() => {
        if (!disabled) {
            cppwApi &&
                cppwApi.startNewAcademicYear(() => {
                    setLastDate(new Date());
                });
        }
    }, [disabled, cppwApi, setLastDate]);
    const onRetry = useCallback(() => {
        cppwApi &&
            cppwApi.getLastAcademicYearStartDate(
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
                cppwApi.getLastAcademicYearStartDate(
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
                title='Начало учебного года'
                mode={2}
                description={`Последний раз начало учебного года происходило ${parseDate(
                    lastDate
                )} В следующий раз функция будет доступна ${parseDate(nextDate)}`}
            />
            <Button
                className={'new-academic-year__start-button'}
                disabled={disabled}
                onClick={startNewAcademicYear}
                text='Начать учебный год'
                intent={Intent.PRIMARY}
            />
        </LoadableView>
    );
});
