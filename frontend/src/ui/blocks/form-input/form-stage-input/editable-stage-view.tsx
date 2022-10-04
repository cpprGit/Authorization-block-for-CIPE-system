import {Card, H3, Icon} from '@blueprintjs/core';
import React, {FC, memo, useCallback, useMemo, useState} from 'react';
import {FormMode, FormType} from 'src/types';
import {EditableStage} from 'src/ui/blocks/form-input/form-stage-input/form-stage-input';
import {FormLayout} from 'src/ui/blocks/form-layout/form-layout';
import {StageMode, StageView} from 'src/ui/blocks/stage-view/stage-view';
import {DEFAULT_STAGE_ID, STAGE_ATTRIBUTES} from 'src/ui/utils/constants';

type Props = EditableStage & {
    deleteStage: () => void;
    editStage: (stage: EditableStage) => void;
};
export const EditableStageView: FC<Props> = memo(
    ({
        id,
        name,
        stageNumber,
        description,
        startDate,
        endDate,
        tasks,
        hasForcedGrade,
        coefficient,
        isEditing,
        editStage,
        deleteStage,
    }) => {
        const [errors, setErrors] = useState();
        const attributes = useMemo(() => {
            return [
                stageNumber,
                name,
                description,
                startDate,
                endDate,
                coefficient,
                hasForcedGrade,
                tasks,
            ].map((val, ind) => ({
                ...STAGE_ATTRIBUTES[ind],
                defaultValue: val,
            }));
        }, [
            stageNumber,
            name,
            description,
            startDate,
            endDate,
            coefficient,
            hasForcedGrade,
            tasks,
        ]);
        const handleCancel = useCallback(() => {
            if (id !== DEFAULT_STAGE_ID) {
                editStage({
                    id,
                    stageNumber,
                    name,
                    description,
                    startDate,
                    endDate,
                    hasForcedGrade,
                    coefficient,
                    tasks,
                    isEditing: false,
                });
            } else {
                deleteStage();
            }
        }, [
            hasForcedGrade,
            stageNumber,
            coefficient,
            id,
            name,
            description,
            startDate,
            endDate,
            tasks,
            editStage,
            deleteStage,
        ]);
        const handleSave = useCallback(
            (values) => {
                editStage({
                    id: id + '1',
                    stageNumber: parseInt(values[0]) || 0,
                    name: values[1],
                    description: values[2],
                    startDate: values[3],
                    endDate: values[4],
                    coefficient: Number(values[5]),
                    hasForcedGrade: values[6],
                    tasks: values[7],
                    isEditing: false,
                });
            },
            [editStage, id]
        );
        if (isEditing) {
            return (
                <Card className={`stage-input__item`} key={id}>
                    <FormLayout
                        id={id}
                        // Ставим какой-то, чтобы удовлетворить TS. Использоваться не будет.
                        type={FormType.Questionnaire}
                        HeaderComponent={H3}
                        title={'Редактирование этапа'}
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
            <div className='stage-input__stage'>
                <StageView
                    id={id}
                    className='stage-input__stage-view'
                    name={name}
                    description={description}
                    endDate={endDate}
                    startDate={startDate}
                    coefficient={coefficient}
                    hasForcedGrade={hasForcedGrade}
                    stageMode={StageMode.All}
                    stageNumber={stageNumber}
                    tasks={tasks}
                />
                <Icon
                    className='stage-input__cross'
                    icon='cog'
                    onClick={() =>
                        editStage({
                            id,
                            stageNumber,
                            name,
                            description,
                            startDate,
                            endDate,
                            tasks,
                            hasForcedGrade,
                            coefficient,
                            isEditing: true,
                        })
                    }
                />
                <Icon className='stage-input__cross' icon='cross' onClick={deleteStage} />
            </div>
        );
    }
);
