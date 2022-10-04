import React, {FC, memo, useCallback} from 'react';
import {FormType} from 'src/types';
import {DefaultFormView} from 'src/ui/blocks/default-form-view/default-form-view';
import {HomePageParagraph} from 'src/ui/blocks/home-page-paragraph/home-page-paragraph';
import {useCppwApiContext} from 'src/ui/contexts/cppw-api-context/cppw-api-context';

export const StudentsRegistration: FC = memo(() => {
    const cppwApi = useCppwApiContext();
    const onSubmit = useCallback(
        (req, onSubmit) => {
            cppwApi &&
                cppwApi.submitDefaultForm(req, onSubmit, 'signup/student', {
                    role: 'student',
                });
        },
        [cppwApi]
    );

    return (
            <DefaultFormView
                formType={FormType.StudentRegistration}
                onSubmit={onSubmit}
                errorTitle='Ошибка загрузки формы регистрации.'
                successTitle='Регистрация прошла успешно!'
            />
    );
});
