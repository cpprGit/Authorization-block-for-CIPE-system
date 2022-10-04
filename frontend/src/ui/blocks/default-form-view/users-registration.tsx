import React, {FC, memo, useCallback} from 'react';
import {FormType, UserRole} from 'src/types';
import {DefaultFormView} from 'src/ui/blocks/default-form-view/default-form-view';
import {useCppwApiContext} from 'src/ui/contexts/cppw-api-context/cppw-api-context';
import {HomePageParagraph} from "../home-page-paragraph/home-page-paragraph";

export const UsersRegistration: FC = memo(() => {
    const cppwApi = useCppwApiContext();
    const onSubmit = useCallback(
        (req, onSubmit) => {
            cppwApi &&
                cppwApi.submitDefaultForm(req, onSubmit, 'signup/user', {
                    role:
                        req.role === 'Ментор'
                            ? UserRole.Mentor
                            : req.role === 'Менеджер'
                            ? UserRole.Manager
                            : req.role === 'Академический руководитель'
                            ? UserRole.AcademicManager
                            : req.role === 'Сотрудник учебного офиса'
                            ? UserRole.OfficeManager
                            : 'representative',
                    hseDepartment: req.hseDepartment ? req.hseDepartment.id : null,
                    organisation: req.organisation ? req.organisation.id : null,
                });
        },
        [cppwApi]
    );

    return (
        <DefaultFormView
            formType={FormType.UserRegistration}
            onSubmit={onSubmit}
            errorTitle='Ошибка загрузки формы регистрации.'
            successTitle='Регистрация прошла успешно!'
        />
    );
});
