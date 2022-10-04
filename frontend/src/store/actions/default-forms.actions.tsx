import {Form, FormType} from 'src/types';

export const RECEIVE_FORM_BY_TYPE = 'RECEIVE_FORM_BY_TYPE' as const;
export const REQUEST_FORM_BY_TYPE = 'REQUEST_FORM_BY_TYPE' as const;
export const ERROR_FORM_BY_TYPE = 'ERROR_FORM_BY_TYPE' as const;

export const receiveFormByType = (type: FormType, form: Form) => ({
    type: RECEIVE_FORM_BY_TYPE,
    payload: {
        type,
        form,
    },
});
export const requestFormByType = (type: FormType) => ({
    type: REQUEST_FORM_BY_TYPE,
    payload: {
        type,
    },
});
export const errorFormByType = (type: FormType) => ({
    type: ERROR_FORM_BY_TYPE,
    payload: {
        type,
    },
});
