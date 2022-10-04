import {Form} from 'src/types';

export const REQUEST_USER_ARCHIVE_FORMS = 'REQUEST_USER_ARCHIVE_FORMS' as const;
export const SUCCESS_USER_ARCHIVE_FORMS = 'SUCCESS_USER_ARCHIVE_FORMS' as const;
export const ERROR_USER_ARCHIVE_FORMS = 'ERROR_USER_ARCHIVE_FORMS' as const;
export const ADD_NEW_USER_ARCHIVE_FORM = 'ADD_NEW_USER_ARCHIVE_FORM' as const;
export const ERROR_USER_ARCHIVE_FORM = 'ERROR_USER_ARCHIVE_FORM' as const;
export const ARCHIVE_ADD_FORM = 'ARCHIVE_ADD_FORM' as const;
export const ARCHIVE_REMOVE_FORM = 'ARCHIVE_REMOVE_FORM' as const;

export const requestUserArchiveForms = () => ({
    type: REQUEST_USER_ARCHIVE_FORMS,
});
export const errorUserArchiveForms = () => ({
    type: ERROR_USER_ARCHIVE_FORMS,
});
export const successUserArchiveForms = (forms: Form[]) => ({
    type: SUCCESS_USER_ARCHIVE_FORMS,
    payload: forms,
});
export const addArchiveForm = (formIndex: number, form: Form) => ({
    type: ARCHIVE_ADD_FORM,
    payload: {formIndex, form},
});
export const removeArchiveForm = (formIndex: number, form: Form) => ({
    type: ARCHIVE_REMOVE_FORM,
    payload: {formIndex, form},
});
export const errorArchiveForm = (formIndex: number, errors: string[]) => ({
    type: ERROR_USER_ARCHIVE_FORM,
    payload: {formIndex, errors},
});
