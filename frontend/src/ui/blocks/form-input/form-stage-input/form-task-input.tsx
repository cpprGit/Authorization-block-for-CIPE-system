import {Card, Icon} from '@blueprintjs/core';
import React, {FC, memo, useCallback, useEffect, useState} from 'react';
import {Task} from 'src/types';
import {EditableTaskView} from 'src/ui/blocks/form-input/form-stage-input/editable-task-view';
import {NEW_TASK} from 'src/ui/utils/constants';

export type EditableTask = Task & {isEditing?: boolean};
type Props = {
    inputRef: (ref: {value: Task[]} | null) => void;
    savedValue?: Task[];
    defaultValue?: Task[];
    disabled?: boolean;
};
export const TaskInputView: FC<Props> = memo(
    ({inputRef, disabled, savedValue, defaultValue = []}) => {
        const [tasks, setTasks] = useState(savedValue || defaultValue);
        const addTask = useCallback(() => setTasks([...tasks, NEW_TASK]), [setTasks, tasks]);
        const deleteTask = (index: number) => () => {
            const newTasks = tasks.filter((val: Task, ind: number) => ind !== index);
            setTasks(newTasks);
        };
        const editTask = (index: number) => (newTask: EditableTask) => {
            const newTasks = tasks.map((task: EditableTask, ind: number) =>
                ind === index ? newTask : task
            );
            setTasks(newTasks);
        };

        useEffect(() => {
            inputRef && inputRef({value: tasks});
        }, [inputRef, tasks]);

        return (
            <div className={`task-input ${disabled ? '_disabled' : ''}`}>
                {tasks.map((task: EditableTask, index: number) => (
                    <EditableTaskView
                        {...task}
                        editTask={editTask(index)}
                        deleteTask={deleteTask(index)}
                    />
                ))}
                <Card className='task-input__plus' onClick={addTask}>
                    <Icon icon='plus' />
                </Card>
            </div>
        );
    }
);
