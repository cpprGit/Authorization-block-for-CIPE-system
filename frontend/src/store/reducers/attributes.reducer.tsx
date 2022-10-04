import {Reducer} from 'redux';
import {Actions} from 'src/store/actions/action-type';
import {
    ADD_NEW_ATTRIBUTE,
    EDIT_ATTRIBUTE,
    ERROR_ATTRIBUTES_BY_USAGE,
    RECEIVE_ATTRIBUTES_BY_USAGE,
    REQUEST_ATTRIBUTES_BY_USAGE,
} from 'src/store/actions/attributes.actions';
import {AsyncStatus, Attribute, Usage} from 'src/types';

export type AttributesState = {
    [k in Usage]?: {status: AsyncStatus; attributes: Attribute[]};
};
export const attributes: Reducer<AttributesState, Actions> = (
    state: AttributesState = {},
    action: Actions
) => {
    switch (action.type) {
        case ADD_NEW_ATTRIBUTE: {
            const {usage, attribute} = action.payload;
            const currentState = state[usage];
            const prevAttributes = currentState ? currentState.attributes : [];
            const status = currentState ? currentState.status : [];
            return {
                ...state,
                [usage]: {
                    attributes: [...prevAttributes, attribute],
                    status,
                },
            };
        }
        case EDIT_ATTRIBUTE: {
            const {usage, attribute} = action.payload;

            const currentState = state[usage];
            const prevAttributes = currentState ? currentState.attributes : [];
            const status = currentState ? currentState.status : [];

            return {
                ...state,
                [usage]: {
                    attributes: prevAttributes.map((attr: Attribute) =>
                        attr.id === attribute.id ? attribute : attr
                    ),
                    status,
                },
            };
        }
        case RECEIVE_ATTRIBUTES_BY_USAGE: {
            const {usage, attributes} = action.payload;
            return {
                ...state,
                [usage]: {
                    attributes,
                    status: AsyncStatus.Success,
                },
            };
        }
        case REQUEST_ATTRIBUTES_BY_USAGE: {
            const {usage} = action.payload;
            return {
                ...state,
                [usage]: {
                    status: AsyncStatus.Pending,
                },
            };
        }
        case ERROR_ATTRIBUTES_BY_USAGE: {
            const {usage} = action.payload;
            return {
                ...state,
                [usage]: {
                    status: AsyncStatus.Error,
                },
            };
        }
        default: {
            return state;
        }
    }
};
