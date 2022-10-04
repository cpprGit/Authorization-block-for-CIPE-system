import React, {FC, memo, useCallback} from 'react';
import {FormType, UserRole} from 'src/types';
import {DefaultFormView} from 'src/ui/blocks/default-form-view/default-form-view';
import {useCppwApiContext} from 'src/ui/contexts/cppw-api-context/cppw-api-context';
import {HomePageParagraph} from "../home-page-paragraph/home-page-paragraph";

export const UsersAuthorization: FC = memo(() => {
    const cppwApi = useCppwApiContext();
    const onSubmit = useCallback(
        (req, onSubmit) => {
            cppwApi &&
            cppwApi.authorize(req, onSubmit, 'sign_in', {});
        },
        [cppwApi]
    );

    return (
        <DefaultFormView
            formType={FormType.UserAuthorization}
            onSubmit={onSubmit}
            errorTitle='Ошибка загрузки формы авторизации.'
            successTitle='Авторизация прошла успешно!'
        />
    );
});
