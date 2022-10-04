import {Card, Icon} from '@blueprintjs/core';
import React, {FC, memo, useCallback, useEffect, useState} from 'react';
import {Stage} from 'src/types';
import {EditableStageView} from 'src/ui/blocks/form-input/form-stage-input/editable-stage-view';
import {NEW_STAGE} from 'src/ui/utils/constants';

export type EditableStage = Stage & {isEditing?: boolean};
type Props = {
    inputRef: (ref: {value: Stage[]} | null) => void;
    savedValue?: Stage[];
    onChange?: () => void;
    defaultValue?: Stage[];
    disabled?: boolean;
};
export const StageInputView: FC<Props> = memo(
    ({inputRef, savedValue, defaultValue = [], disabled}) => {
        const [stages, setStages] = useState(savedValue || defaultValue);
        const addStage = useCallback(() => setStages([...stages, NEW_STAGE]), [setStages, stages]);
        const deleteStage = useCallback(
            (index: number) => () => {
                const newStages = stages.filter((val: Stage, ind: number) => ind !== index);
                setStages(newStages);
            },
            [setStages, stages]
        );
        const editStage = (index: number) => (newStage: EditableStage) => {
            const newStages = stages.map((stage: EditableStage, ind: number) =>
                ind === index ? newStage : stage
            );
            setStages(newStages);
        };

        useEffect(() => {
            inputRef && inputRef({value: stages});
        }, [inputRef, stages]);

        return (
            <div className={`stage-input ${disabled ? '_disabled' : ''}`}>
                {stages.map((stage: EditableStage, index: number) => (
                    <EditableStageView
                        {...stage}
                        editStage={editStage(index)}
                        deleteStage={deleteStage(index)}
                    />
                ))}
                <Card className='stage-input__plus' onClick={addStage}>
                    <Icon icon='plus' />
                </Card>
            </div>
        );
    }
);
