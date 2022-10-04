import React, {FC, memo, useCallback, useState} from 'react';
import {FormMode, FormType} from 'src/types';
import {FormLayout} from 'src/ui/blocks/form-layout/form-layout';
import {SuccessView} from 'src/ui/blocks/success-view/success-view';
import {useCppwApiContext} from 'src/ui/contexts/cppw-api-context/cppw-api-context';
import {SET_STUDENT_STATUS_ATTRIBUTES} from 'src/ui/utils/constants';

export const SetStudentStatus: FC = memo(() => {
    const cppwApi = useCppwApiContext();
    const [errors, setErrors] = useState();
    const [sentStatus, setSentStatus] = useState(false);
    const onRetryFilling = useCallback(() => {
        setSentStatus(false);
        setErrors(undefined);
    }, [setSentStatus, setErrors]);

    const onFormSubmit = useCallback(
        (vals) => {
            // Студент, а потом статус.
            cppwApi &&
                cppwApi.setStudentStatus(vals[0].id, vals[1], () => {
                    setSentStatus(true);
                });
        },
        [cppwApi]
    );

    if (sentStatus) {
        return <SuccessView title={'Статус студента изменен!'} onRetry={onRetryFilling} />;
    }

    return (
        <>
            <FormLayout
                type={FormType.Questionnaire}
                buttonName={'Изменить'}
                id={''}
                title=''
                description=''
                mode={FormMode.Fill}
                errors={errors}
                onSetError={setErrors}
                onFormSubmit={onFormSubmit}
                // Приходит отрицательным (-1) для форм вне списка форм пользователя.
                // Фактически запрещает редактирование
                index={-1}
                attributes={SET_STUDENT_STATUS_ATTRIBUTES}
            />
        </>
    );
});
