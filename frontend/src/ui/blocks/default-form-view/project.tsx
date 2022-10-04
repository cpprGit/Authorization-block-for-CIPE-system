import React, {FC, memo, useCallback} from 'react';

import {FormType} from 'src/types';
import {DefaultFormView} from 'src/ui/blocks/default-form-view/default-form-view';
import {useCppwApiContext} from 'src/ui/contexts/cppw-api-context/cppw-api-context';

export const Project: FC = memo(() => {
    const cppwApi = useCppwApiContext();
    const onSubmit = useCallback(
        (req, onSubmit) => {
            cppwApi &&
                cppwApi.submitDefaultForm(req, onSubmit, `formatted/${FormType.Project}/`, {
                    status: 'approved',
                });
        },
        [cppwApi]
    );

    return (
        <DefaultFormView
            formType={FormType.Project}
            onSubmit={onSubmit}
            errorTitle='Ошибка загрузки формы создания проекта.'
            successTitle='Проект создан!'
        />
    );
});
