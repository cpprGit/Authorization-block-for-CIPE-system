import {Actions} from 'src/store/actions/action-type';
import {
    ARCHIVE_ADD_FORM,
    ARCHIVE_REMOVE_FORM,
    ERROR_USER_ARCHIVE_FORM,
    ERROR_USER_ARCHIVE_FORMS,
    REQUEST_USER_ARCHIVE_FORMS,
    SUCCESS_USER_ARCHIVE_FORMS,
} from 'src/store/actions/user-archive-forms.actions';
import {UserFormsState} from 'src/store/reducers/user-forms.reducer';
import {AsyncStatus, Form} from 'src/types';

export const userArchiveForms = (
    state: UserFormsState = {
        status: AsyncStatus.Initial,
        forms: [],
    },
    action: Actions
) => {
    switch (action.type) {
        case REQUEST_USER_ARCHIVE_FORMS: {
            return {
                ...state,
                status: AsyncStatus.Pending,
            };
        }
        case ERROR_USER_ARCHIVE_FORMS: {
            return {
                ...state,
                status: AsyncStatus.Error,
            };
        }
        case SUCCESS_USER_ARCHIVE_FORMS: {
            return {
                ...state,
                status: AsyncStatus.Success,
                forms: action.payload,
            };
        }
        case ARCHIVE_ADD_FORM: {
            return {
                ...state,
                forms: [...state.forms, action.payload.form],
            };
        }

        case ARCHIVE_REMOVE_FORM: {
            const {formIndex} = action.payload;
            const newForms = state.forms.filter((old: Form, index: number) => index !== formIndex);
            return {
                ...state,
                forms: newForms,
            };
        }
        case ERROR_USER_ARCHIVE_FORM: {
            const {formIndex, errors} = action.payload;
            const newForms = state.forms.map((old: Form, index: number) =>
                index === formIndex ? {...old, errors} : old
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
