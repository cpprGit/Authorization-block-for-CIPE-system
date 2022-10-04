import {Actions} from 'src/store/actions/action-type';
import {
    ERROR_FORM_BY_TYPE,
    RECEIVE_FORM_BY_TYPE,
    REQUEST_FORM_BY_TYPE,
} from 'src/store/actions/default-forms.actions';
import {AsyncStatus, Form, FormType, Usage} from 'src/types';

export type DefaultFormsState = {
    [k in FormType]?: {status: AsyncStatus; form: Form};
};
export const defaultForms = (state: DefaultFormsState = {}, action: Actions) => {
    switch (action.type) {
        case RECEIVE_FORM_BY_TYPE: {
            const {type, form} = action.payload;
            if (type === FormType.ProjectRequest) {
                form.attributes = form.attributes.filter(({usage}) => usage !== Usage.Activity);
            }
            return {
                ...state,
                [type]: {
                    form,
                    status: AsyncStatus.Success,
                },
            };
        }
        case REQUEST_FORM_BY_TYPE: {
            const {type} = action.payload;
            return {
                ...state,
                [type]: {
                    status: AsyncStatus.Pending,
                },
            };
        }
        case ERROR_FORM_BY_TYPE: {
            const {type} = action.payload;
            return {
                ...state,
                [type]: {
                    status: AsyncStatus.Error,
                },
            };
        }
        default: {
            return state;
        }
    }
};
