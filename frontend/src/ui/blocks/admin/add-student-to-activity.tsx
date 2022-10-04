import React, {FC, memo, useCallback, useState} from 'react';
import {FormMode, FormType} from 'src/types';
import {FormLayout} from 'src/ui/blocks/form-layout/form-layout';
import {SuccessView} from 'src/ui/blocks/success-view/success-view';
import {useCppwApiContext} from 'src/ui/contexts/cppw-api-context/cppw-api-context';
import {ADD_STUDENTS_TO_ACTIVITY_ATTRIBUTES} from 'src/ui/utils/constants';

export const AddStudentToActivity: FC = memo(() => {
    const cppwApi = useCppwApiContext();
    const [errors, setErrors] = useState();
    const [sentStatus, setSentStatus] = useState(false);
    const onRetryFilling = useCallback(() => {
        setSentStatus(false);
        setErrors(undefined);
    }, [setSentStatus, setErrors]);

    const onFormSubmit = useCallback(
        (vals) => {
            cppwApi &&
                cppwApi.addStudentToActivity(vals[0].id, vals[1], () => {
                    setSentStatus(true);
                });
        },
        [cppwApi]
    );

    if (sentStatus) {
        return <SuccessView title={'Студент добавлен!'} onRetry={onRetryFilling} />;
    }

    return (
        <>
            <FormLayout
                type={FormType.Questionnaire}
                buttonName={'Добавить'}
                id={''}
                title=''
                description='Добавление студента в активность'
                mode={FormMode.Fill}
                errors={errors}
                onSetError={setErrors}
                onFormSubmit={onFormSubmit}
                // Приходит отрицательным (-1) для форм вне списка форм пользователя.
                // Фактически запрещает редактирование
                index={-1}
                attributes={ADD_STUDENTS_TO_ACTIVITY_ATTRIBUTES}
            />
        </>
    );
});
