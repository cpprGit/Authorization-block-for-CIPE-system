import React, {FC, memo, useCallback} from 'react';

import {FormType} from 'src/types';
import {DefaultFormView} from 'src/ui/blocks/default-form-view/default-form-view';
import {useCppwApiContext} from 'src/ui/contexts/cppw-api-context/cppw-api-context';

export const ProjectRequest: FC = memo(() => {
    const cppwApi = useCppwApiContext();
    const onSubmit = useCallback(
        (req, onSubmit) => {
            cppwApi &&
                cppwApi.submitDefaultForm(
                    req,
                    onSubmit,
                    `formatted/${FormType.ProjectRequest}/`,
                    {}
                );
        },
        [cppwApi]
    );

    return (
        <DefaultFormView
            formType={FormType.ProjectRequest}
            onSubmit={onSubmit}
            errorTitle='Ошибка загрузки формы создания проекта.'
            successTitle='Заявка на проект подана!'
        />
    );
});
