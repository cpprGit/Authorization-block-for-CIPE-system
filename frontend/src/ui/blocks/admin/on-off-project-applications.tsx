import React, {FC, memo, useCallback, useState} from 'react';
import {FormMode, FormType} from 'src/types';
import {FormLayout} from 'src/ui/blocks/form-layout/form-layout';
import {SuccessView} from 'src/ui/blocks/success-view/success-view';
import {useCppwApiContext} from 'src/ui/contexts/cppw-api-context/cppw-api-context';
import {ON_OFF_PROJECT_APPLICATIONS_ATTRIBUTES} from 'src/ui/utils/constants';

export const OnOffProjectApplications: FC = memo(() => {
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
                cppwApi.onOffProjectApplications(vals[0], vals[1], () => {
                    setSentStatus(true);
                });
        },
        [cppwApi]
    );

    if (sentStatus) {
        return <SuccessView title={'Изменения внесены!'} onRetry={onRetryFilling} />;
    }

    return (
        <>
            <FormLayout
                type={FormType.Questionnaire}
                buttonName={'Изменить статус подачи заявок студентами'}
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
                attributes={ON_OFF_PROJECT_APPLICATIONS_ATTRIBUTES}
            />
        </>
    );
});
