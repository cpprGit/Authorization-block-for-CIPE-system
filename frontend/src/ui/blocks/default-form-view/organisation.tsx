import React, {FC, memo, useCallback} from 'react';
import {FormType} from 'src/types';
import {DefaultFormView} from 'src/ui/blocks/default-form-view/default-form-view';
import {useCppwApiContext} from 'src/ui/contexts/cppw-api-context/cppw-api-context';

export const Organisation: FC = memo(() => {
    const cppwApi = useCppwApiContext();
    const onSubmit = useCallback(
        (req, onSubmit) => {
            cppwApi &&
                cppwApi.submitDefaultForm(req, onSubmit, `formatted/organisation/`, {
                    parent: req.parent ? req.parent.id : null,
                    createdBy: '123e4567-e89b-12d3-a000-000000000000',
                    lastModifiedBy: '123e4567-e89b-12d3-a000-000000000000',
                    lastModifiedTime: '2014-06-01T03:02:13.552+00:00',
                    type: 'IT',
                    status: 'approved',
                });
        },
        [cppwApi]
    );

    return (
        <DefaultFormView
            formType={FormType.OrgProfile}
            onSubmit={onSubmit}
            errorTitle='Ошибка загрузки формы регистрации компании.'
            successTitle='Регистрация компании прошла успешно!'
        />
    );
});
