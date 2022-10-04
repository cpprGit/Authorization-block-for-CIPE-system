import {Attribute, Usage} from 'src/types';

export const RECEIVE_ATTRIBUTES_BY_USAGE = 'RECEIVE_ATTRIBUTES_BY_USAGE' as const;
export const REQUEST_ATTRIBUTES_BY_USAGE = 'REQUEST_ATTRIBUTES_BY_USAGE' as const;
export const ERROR_ATTRIBUTES_BY_USAGE = 'ERROR_ATTRIBUTES_BY_USAGE' as const;
export const ADD_NEW_ATTRIBUTE = 'ADD_NEW_ATTRIBUTE' as const;
export const EDIT_ATTRIBUTE = 'EDIT_ATTRIBUTE' as const;

// fix name ATTRIBUTES
export const receiveAttributeByUsage = (usage: Usage, attributes: Attribute[]) => ({
    type: RECEIVE_ATTRIBUTES_BY_USAGE,
    payload: {
        usage,
        attributes,
    },
});
export const requestAttributeByUsage = (usage: Usage) => ({
    type: REQUEST_ATTRIBUTES_BY_USAGE,
    payload: {
        usage,
    },
});
export const errorAttributeByUsage = (usage: Usage) => ({
    type: ERROR_ATTRIBUTES_BY_USAGE,
    payload: {
        usage,
    },
});
export const addNewAttribute = (usage: Usage, attribute: Attribute) => ({
    type: ADD_NEW_ATTRIBUTE,
    payload: {
        usage,
        attribute,
    },
});
export const editAttribute = (usage: Usage, attribute: Attribute) => ({
    type: EDIT_ATTRIBUTE,
    payload: {
        usage,
        attribute,
    },
});
