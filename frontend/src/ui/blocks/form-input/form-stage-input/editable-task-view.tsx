import {Card, H3, Icon} from '@blueprintjs/core';
import React, {FC, memo, useCallback, useMemo, useState} from 'react';
import {FormMode, FormType} from 'src/types';
import {EditableTask} from 'src/ui/blocks/form-input/form-stage-input/form-task-input';
import {FormLayout} from 'src/ui/blocks/form-layout/form-layout';
import {StageMode, TaskView} from 'src/ui/blocks/stage-view/stage-view';
import {DEFAULT_TASK_ID, TASK_ATTRIBUTES} from 'src/ui/utils/constants';

type Props = EditableTask & {
    editTask: (task: EditableTask) => void;
    deleteTask: () => void;
};
export const EditableTaskView: FC<Props> = memo(
    ({id, name, description, isUploadable, isEditing, editTask, deleteTask}) => {
        const [errors, setErrors] = useState();
        const attributes = useMemo(() => {
            return [name, description, isUploadable].map((val, ind) => ({
                ...TASK_ATTRIBUTES[ind],
                defaultValue: val,
            }));
        }, [name, description, isUploadable]);
        const handleCancel = useCallback(() => {
            if (id !== DEFAULT_TASK_ID) {
                editTask({id, name, description, isUploadable, isEditing: false});
            } else {
                deleteTask();
            }
        }, [id, name, description, isUploadable, editTask, deleteTask]);
        const handleSave = useCallback(
            (values) => {
                editTask({
                    id: id + '1',
                    name: values[0],
                    description: values[1],
                    isUploadable: values[2],
                    isEditing: false,
                });
            },
            [editTask, id]
        );
        const handleEdit = useCallback(() => {
            editTask({id, name, description, isUploadable, isEditing: true});
        }, [id, name, description, isUploadable, editTask]);

        if (isEditing) {
            return (
                <Card className={`task-input__item`}>
                    <FormLayout
                        id={id}
                        // Ставим какой-то, чтобы удовлетворить TS. Использоваться не будет.
                        type={FormType.Questionnaire}
                        HeaderComponent={H3}
                        title={'Редактирование задания этапа'}
                        attributes={attributes}
                        mode={FormMode.Fill}
                        buttonName='Сохранить'
                        errors={errors}
                        onFormSubmit={handleSave}
                        onSetError={setErrors}
                        handleCancel={handleCancel}
                        index={-1}
                    />
                </Card>
            );
        }
        return (
            <div className='task-input__task'>
                <TaskView
                    id={id}
                    disabled={false}
                    className='task-input__task-view'
                    name={name}
                    description={description}
                    stageMode={StageMode.All}
                    isUploadable={isUploadable}
                />
                <Icon
                    className='task-input__cross'
                    icon='cog'
                    key={`${name}__cog`}
                    onClick={handleEdit}
                />
                <Icon
                    className='task-input__cross'
                    icon='cross'
                    key={`${name}__cross`}
                    onClick={deleteTask}
                />
            </div>
        );
    }
);
