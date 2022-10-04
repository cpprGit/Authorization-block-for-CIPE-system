import {Actions} from 'src/store/actions/action-type';
import {ARCHIVE_ADD_FORM, ARCHIVE_REMOVE_FORM} from 'src/store/actions/user-archive-forms.actions';
import {
    ADD_DRAFT_FORM_ATTRIBUTE_PROPERTIES,
    ADD_DRAFT_FORM_ATTRIBUTES,
    ADD_NEW_USER_FORM,
    CHANGE_USER_FORM_MODE,
    DELETE_DRAFT_FORM_ATTRIBUTE,
    DELETE_NEW_USER_FORM,
    EDIT_USER_FORM,
    ERROR_FORM_STATS,
    ERROR_USER_FORM,
    ERROR_USER_FORMS,
    REQUEST_FORM_STATS,
    REQUEST_USER_FORMS,
    SET_DRAFT_FORM_BUTTON_NAME,
    SET_DRAFT_FORM_DESCRIPTION,
    SET_DRAFT_FORM_TITLE,
    SUCCESS_FORM_STATS,
    SUCCESS_USER_FORMS,
} from 'src/store/actions/user-forms.actions';
import {AsyncStatus, Attribute, Form, FormMode, StateForm} from 'src/types';

export type UserFormsState = {
    status: AsyncStatus;
    forms: StateForm[];
};

export const userForms = (
    state: UserFormsState = {
        status: AsyncStatus.Initial,
        forms: [],
    },
    action: Actions
) => {
    switch (action.type) {
        case REQUEST_USER_FORMS: {
            return {
                ...state,
                status: AsyncStatus.Pending,
            };
        }
        case ERROR_USER_FORMS: {
            return {
                ...state,
                status: AsyncStatus.Error,
            };
        }
        case SUCCESS_USER_FORMS: {
            return {
                ...state,
                status: AsyncStatus.Success,
                forms: action.payload,
            };
        }
        case ADD_NEW_USER_FORM: {
            return {
                ...state,
                forms: [...state.forms, action.payload],
            };
        }
        case EDIT_USER_FORM: {
            const {formIndex, form} = action.payload;
            const newForms = state.forms.map((old: Form, index: number) =>
                index === formIndex ? {...old, ...form} : old
            );
            return {
                ...state,
                forms: newForms,
            };
        }
        case DELETE_NEW_USER_FORM: {
            const formIndex = action.payload;
            state.forms.splice(formIndex, 1);
            return {
                ...state,
                forms: [...state.forms],
            };
        }
        case ERROR_USER_FORM: {
            const {formIndex, errors} = action.payload;
            const newForms = state.forms.map((old: StateForm, index: number) =>
                index === formIndex ? {...old, errors} : old
            );
            return {
                ...state,
                forms: newForms,
            };
        }
        case CHANGE_USER_FORM_MODE: {
            const {formIndex, mode} = action.payload;
            const newForms = state.forms.map((old: Form, index: number) => {
                if (mode === FormMode.Edit) {
                    return index === formIndex ? {...old, mode, backup: old} : old;
                } else {
                    return index === formIndex ? {...old, mode, backup: undefined} : old;
                }
            });
            return {
                ...state,
                forms: newForms,
            };
        }

        case SET_DRAFT_FORM_TITLE: {
            const {formIndex, title} = action.payload;
            const newForms = state.forms.map((old: Form, index: number) =>
                index === formIndex ? {...old, title} : old
            );
            return {
                ...state,
                forms: newForms,
            };
        }
        case SET_DRAFT_FORM_DESCRIPTION: {
            const {formIndex, description} = action.payload;
            const newForms = state.forms.map((old: Form, index: number) =>
                index === formIndex ? {...old, description} : old
            );
            return {
                ...state,
                forms: newForms,
            };
        }
        case SET_DRAFT_FORM_BUTTON_NAME: {
            const {formIndex, buttonName} = action.payload;
            const newForms = state.forms.map((old: Form, index: number) =>
                index === formIndex ? {...old, buttonName} : old
            );
            return {
                ...state,
                forms: newForms,
            };
        }
        case ADD_DRAFT_FORM_ATTRIBUTES: {
            const {formIndex, attributes} = action.payload;
            const oldAttributes =
                'attributes' in state.forms[formIndex] ? state.forms[formIndex].attributes : [];

            const newForms = state.forms.map((old: Form, index: number) =>
                index === formIndex
                    ? {
                          ...old,
                          attributes: [...oldAttributes, ...attributes],
                      }
                    : old
            );
            return {
                ...state,
                forms: newForms,
            };
        }
        case DELETE_DRAFT_FORM_ATTRIBUTE: {
            const {formIndex, attributeIndex} = action.payload;
            const oldAttributes =
                'attributes' in state.forms[formIndex] ? state.forms[formIndex].attributes : [];

            const newAttributes = oldAttributes.filter(
                (oldAttribute: Attribute, index: number) => index !== attributeIndex
            );
            const newForms = state.forms.map((old: Form, index: number) =>
                index === formIndex ? {...old, attributes: newAttributes} : old
            );
            return {
                ...state,
                forms: newForms,
            };
        }
        case ADD_DRAFT_FORM_ATTRIBUTE_PROPERTIES: {
            const {formIndex, attributeIndex, attribute} = action.payload;
            const oldAttributes =
                'attributes' in state.forms[formIndex] ? state.forms[formIndex].attributes : [];

            const newAttributes = oldAttributes.map((oldAttribute: Attribute, index: number) =>
                index === attributeIndex ? {...oldAttribute, ...attribute} : oldAttribute
            );
            const newForms = state.forms.map((old: Form, index: number) =>
                index === formIndex ? {...old, attributes: newAttributes} : old
            );
            return {
                ...state,
                forms: newForms,
            };
        }

        case ARCHIVE_REMOVE_FORM: {
            return {
                ...state,
                forms: [...state.forms, action.payload.form],
            };
        }

        case ARCHIVE_ADD_FORM: {
            const {formIndex} = action.payload;
            const newForms = state.forms.filter((old: Form, index: number) => index !== formIndex);
            return {
                ...state,
                forms: newForms,
            };
        }

        case REQUEST_FORM_STATS: {
            const formIndex = action.payload;
            const newForms = state.forms.map((old: Form, index: number) =>
                index === formIndex
                    ? {...old, stats: {status: AsyncStatus.Pending, records: []}}
                    : old
            );
            return {
                ...state,
                forms: newForms,
            };
        }
        case ERROR_FORM_STATS: {
            const formIndex = action.payload;
            const newForms = state.forms.map((old: Form, index: number) =>
                index === formIndex
                    ? {...old, stats: {status: AsyncStatus.Error, records: []}}
                    : old
            );
            return {
                ...state,
                forms: newForms,
            };
        }
        case SUCCESS_FORM_STATS: {
            const {formIndex, records} = action.payload;
            const newForms = state.forms.map((old: Form, index: number) =>
                index === formIndex ? {...old, stats: {status: AsyncStatus.Success, records}} : old
            );
            return {
                ...state,
                forms: newForms,
            };
        }

        default: {
            return state;
        }
    }
};
