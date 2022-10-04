import {Checkbox, H5, H6, Icon, Position, Tooltip} from '@blueprintjs/core';
import React, {FC, memo, useCallback, useMemo, useRef, useState} from 'react';
import {Stage, Task, Usage} from 'src/types';
import {FormInput} from 'src/ui/blocks/form-input/form-input';
import {parseDate} from 'src/ui/utils/parse-date';
import './stage-view.styl';

export enum StageMode {
    // Для всех пользователей в активности
    All = 'all',
    // Для студента, исполняющего этот проект. Для загрузки материалов и просмотра оценки
    Student = 'student',
}

type TaskViewProps = Task & {
    stageMode: StageMode;
    className?: string;
    disabled: boolean;
};
export const TaskView: FC<TaskViewProps> = memo(
    ({id, name, description, file, isUploadable, stageMode, className, disabled}) => {
        const fileRef = useRef<{
            value: {id: string; name: string; type: 'file'} | null;
        }>(file ? {value: file} : null);
        const validators = useMemo(() => [], []);
        const setFileRef = useCallback((val) => {
            // @ts-ignore
            fileRef.current = val;
        }, []);

        return (
            <div className={`task-view ${className}`}>
                <Checkbox labelElement={<H6 className='task-view__title'>{name}</H6>}>
                    <div className='task-view__label'>
                        {description && <p className='task-view__description'>{description}</p>}
                        {isUploadable &&
                            (stageMode === StageMode.Student ? (
                                <FormInput
                                    key={'filik'}
                                    title=''
                                    name={'file'}
                                    id={'filik'}
                                    mandatory={false}
                                    validators={validators}
                                    usage={Usage.File}
                                    inputRef={setFileRef}
                                    formIndex={-1}
                                    index={-1}
                                    defaultValue={file}
                                    disabled={disabled}
                                    // @ts-ignore
                                    taskId={id}
                                    placeholder='Нажмите для выбора файла...'
                                />
                            ) : (
                                <Tooltip
                                    className='task-view__upload-icon'
                                    content={
                                        'Для выполнения задания будет необходимо загрузить файл с результатами.'
                                    }
                                    position={Position.LEFT}
                                >
                                    <Icon icon='import' />
                                </Tooltip>
                            ))}
                    </div>
                </Checkbox>
            </div>
        );
    }
);
const now = new Date();

type StageViewProps = Stage & {stageMode: StageMode; className?: string};
export const StageView: FC<StageViewProps> = memo(
    ({name, description, startDate, endDate, coefficient, tasks, grade, stageMode, className}) => {
        const [isOpen, setIsOpen] = useState(
            new Date(startDate) <= now && new Date(endDate) >= now
        );
        const isActivePeriod = useMemo(() => new Date(endDate) < now, [endDate]);
        const handleOpen = useCallback(() => setIsOpen(!isOpen), [setIsOpen, isOpen]);
        return (
            <div className={`stage-view ${className}`}>
                <div className='stage-view__header' onClick={handleOpen}>
                    <H5 className='stage-view__title'>{name}</H5>
                    <Icon icon='chevron-down' className='stage-view__arrow' />
                </div>
                {
                    <div className={`stage-view__collapse ${isOpen ? '_is-open' : ''}`}>
                        <p className='stage-view__description'>
                            {parseDate(new Date(startDate))} - {parseDate(new Date(endDate))}
                        </p>
                        {description && <p className='stage-view__description'>{description}</p>}
                        <p className='stage-view__description'>
                            Коэффицент этапа в итоговой оценке: {coefficient}
                        </p>
                        {tasks && !tasks.length && (
                            <p className='stage-view__description'>В данном этапе нет заданий.</p>
                        )}
                        {tasks &&
                            tasks.map((task, index) => (
                                <TaskView
                                    key={index}
                                    {...task}
                                    stageMode={stageMode}
                                    disabled={isActivePeriod}
                                />
                            ))}
                        {stageMode !== StageMode.All && (
                            <div>
                                <p className='stage-view__description'>
                                    Оценка за этап от ментора:{' '}
                                    {grade && grade.mentorGrade ? grade.mentorGrade : '-'}
                                </p>
                                <p className='stage-view__description'>
                                    Итоговая ценка за этап:{' '}
                                    {grade && grade.managerGrade ? grade.managerGrade : '-'}
                                </p>
                            </div>
                        )}
                    </div>
                }
            </div>
        );
    }
);
