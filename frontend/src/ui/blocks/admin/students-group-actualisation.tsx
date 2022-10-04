import {Button, ButtonGroup, Intent} from '@blueprintjs/core';
import React, {FC, memo, useCallback, useEffect, useMemo, useRef, useState} from 'react';
import {AsyncStatus, Usage} from 'src/types';
import {FormInput} from 'src/ui/blocks/form-input/form-input';
import {HomePageParagraph} from 'src/ui/blocks/home-page-paragraph/home-page-paragraph';
import {LoadableView} from 'src/ui/blocks/loadable-view/loadable-view';
import {useCppwApiContext} from 'src/ui/contexts/cppw-api-context/cppw-api-context';

export const StudentsGroupActualisation: FC = memo(() => {
    const cppwApi = useCppwApiContext();

    const [status, setStatus] = useState(AsyncStatus.Initial);
    const [isEditing, setIsEditing] = useState(false);
    const [groups, setGroups] = useState<string[]>([]);

    const groupsRef = useRef<{value: string[]} | null>(null);

    const validators = useMemo(() => [], []);

    const setGroupsRef = useCallback((val: any) => {
        groupsRef.current = val;
    }, []);
    const startEditing = useCallback(() => {
        setIsEditing(true);
    }, [setIsEditing]);
    const finishEditing = useCallback(() => {
        groupsRef.current &&
            cppwApi &&
            cppwApi.setNewStudentGroupsList(groupsRef.current.value, () => {
                if (groupsRef.current) {
                    setGroups(groupsRef.current.value);
                    setIsEditing(false);
                } else {
                    throw new Error();
                }
            });
    }, [cppwApi, setIsEditing]);
    const cancelEditing = useCallback(() => {
        setIsEditing(false);
    }, [setIsEditing]);
    const onRetry = useCallback(() => {
        cppwApi &&
            cppwApi.getStudentGroupsList(
                () => {
                    setStatus(AsyncStatus.Pending);
                },
                (groups: string[]) => {
                    setGroups(groups);
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
                cppwApi.getStudentGroupsList(
                    () => {
                        setStatus(AsyncStatus.Pending);
                    },
                    (groups: string[]) => {
                        setGroups(groups);
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
            errorTitle='Группы студентов не загрузились'
            errorSubtitle={'Нажмите для повторного запроса'}
            onRetry={onRetry}
        >
            <HomePageParagraph title='Обновление списка активных студенческих групп' mode={2} />
            {!isEditing &&
                groups.map((name, index) => (
                    <div key={index} className='students-group-actualisation__item'>
                        {name}
                    </div>
                ))}
            {isEditing && (
                <div className='students-group-actualisation__input'>
                    <FormInput
                        title=''
                        key={'groups'}
                        name={'groups'}
                        id={'groups'}
                        mandatory={false}
                        validators={validators}
                        usage={Usage.Variants}
                        inputRef={setGroupsRef}
                        formIndex={-1}
                        defaultValue={groups}
                        index={-1}
                    />
                </div>
            )}
            <ButtonGroup className='students-group-actualisation__buttons'>
                {!isEditing && (
                    <Button text='Редактировать' intent={Intent.PRIMARY} onClick={startEditing} />
                )}
                {isEditing && (
                    <Button text='Сохранить' intent={Intent.PRIMARY} onClick={finishEditing} />
                )}
                {isEditing && <Button text='Отменить' onClick={cancelEditing} />}
            </ButtonGroup>
        </LoadableView>
    );
});
