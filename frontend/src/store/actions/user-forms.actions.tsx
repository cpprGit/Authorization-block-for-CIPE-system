import {Attribute, Content, Form, FormMode} from 'src/types';

export const REQUEST_USER_FORMS = 'REQUEST_USER_FORMS' as const;
export const SUCCESS_USER_FORMS = 'SUCCESS_USER_FORMS' as const;
export const ERROR_USER_FORMS = 'ERROR_USER_FORMS' as const;
export const ADD_NEW_USER_FORM = 'ADD_NEW_USER_FORM' as const;
export const DELETE_NEW_USER_FORM = 'DELETE_NEW_USER_FORM' as const;
export const ERROR_USER_FORM = 'ERROR_USER_FORM' as const;
export const CHANGE_USER_FORM_MODE = 'CHANGE_USER_FORM_MODE' as const;

export const EDIT_USER_FORM = 'EDIT_USER_FORM' as const;

export const SET_DRAFT_FORM_TITLE = 'SET_DRAFT_FORM_TITLE' as const;
export const SET_DRAFT_FORM_DESCRIPTION = 'SET_DRAFT_FORM_DESCRIPTION' as const;
export const SET_DRAFT_FORM_BUTTON_NAME = 'SET_DRAFT_FORM_BUTTON_NAME' as const;
export const ADD_DRAFT_FORM_ATTRIBUTES = 'ADD_DRAFT_FORM_ATTRIBUTES' as const;
export const DELETE_DRAFT_FORM_ATTRIBUTE = 'DELETE_DRAFT_FORM_ATTRIBUTE' as const;
export const ADD_DRAFT_FORM_ATTRIBUTE_PROPERTIES = 'ADD_DRAFT_FORM_ATTRIBUTE_PROPERTIES' as const;

export const REQUEST_FORM_STATS = 'REQUEST_FORM_STATS' as const;
export const SUCCESS_FORM_STATS = 'SUCCESS_FORM_STATS' as const;
export const ERROR_FORM_STATS = 'ERROR_FORM_STATS' as const;

export const requestUserForms = () => ({
    type: REQUEST_USER_FORMS,
});
export const errorUserForms = () => ({
    type: ERROR_USER_FORMS,
});
export const successUserForms = (forms: Form[]) => ({
    type: SUCCESS_USER_FORMS,
    payload: forms,
});
export const addNewUserForm = (form: Form) => ({
    type: ADD_NEW_USER_FORM,
    payload: form,
});
export const deleteNewUserForm = (formIndex: number) => ({
    type: DELETE_NEW_USER_FORM,
    payload: formIndex,
});
export const editUserForm = (formIndex: number, form: Partial<Form>) => ({
    type: EDIT_USER_FORM,
    payload: {form, formIndex},
});
export const errorUserForm = (formIndex: number, errors: string[]) => ({
    type: ERROR_USER_FORM,
    payload: {formIndex, errors},
});
export const changeUserFormMode = (formIndex: number, mode: FormMode) => ({
    type: CHANGE_USER_FORM_MODE,
    payload: {formIndex, mode},
});
export const setDraftFormTitle = (formIndex: number, title: string) => ({
    type: SET_DRAFT_FORM_TITLE,
    payload: {formIndex, title},
});
export const setDraftFormDescription = (formIndex: number, description: string) => ({
    type: SET_DRAFT_FORM_DESCRIPTION,
    payload: {formIndex, description},
});
export const setDraftFormButtonName = (formIndex: number, buttonName: string) => ({
    type: SET_DRAFT_FORM_BUTTON_NAME,
    payload: {formIndex, buttonName},
});
export const addDraftFormAttributes = (formIndex: number, attributes: Attribute[]) => ({
    type: ADD_DRAFT_FORM_ATTRIBUTES,
    payload: {formIndex, attributes},
});
export const deleteDraftFormAttribute = (formIndex: number, attributeIndex: number) => ({
    type: DELETE_DRAFT_FORM_ATTRIBUTE,
    payload: {formIndex, attributeIndex},
});
export const addDraftFormAttributeProperties = (
    formIndex: number,
    attributeIndex: number,
    attribute: Partial<Attribute>
) => ({
    type: ADD_DRAFT_FORM_ATTRIBUTE_PROPERTIES,
    payload: {formIndex, attributeIndex, attribute},
});

export const requestFormStats = (formIndex: number) => ({
    type: REQUEST_FORM_STATS,
    payload: formIndex,
});
export const errorFormStats = (formIndex: number) => ({
    type: ERROR_FORM_STATS,
    payload: formIndex,
});
export const successFormStats = (formIndex: number, records: Content[]) => ({
    type: SUCCESS_FORM_STATS,
    payload: {formIndex, records},
});
