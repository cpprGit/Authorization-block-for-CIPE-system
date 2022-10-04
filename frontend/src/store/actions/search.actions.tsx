import {Attribute, Form, ProfileOrSearchItem, SearchType} from 'src/types';

export const RECEIVE_SEARCH = 'RECEIVE_SEARCH' as const;
export const REQUEST_SEARCH = 'REQUEST_SEARCH' as const;
export const ERROR_SEARCH = 'ERROR_SEARCH' as const;

export const RECEIVE_FULL_SEARCH = 'RECEIVE_FULL_SEARCH' as const;
export const REQUEST_FULL_SEARCH = 'REQUEST_FULL_SEARCH' as const;
export const ERROR_FULL_SEARCH = 'ERROR_FULL_SEARCH' as const;

export const SELECT_SEARCH_TYPE = 'SELECT_SEARCH_TYPE' as const;
export const SET_FILTER = 'SET_FILTER' as const;

export const receiveSearch = (results: {records: ProfileOrSearchItem[]}) => ({
    type: RECEIVE_SEARCH,
    payload: results,
});
export const requestSearch = () => ({
    type: REQUEST_SEARCH,
});
export const errorSearch = () => ({
    type: ERROR_SEARCH,
});

export const receiveFullSearch = (results: {records: {}[]; schema: Form}) => ({
    type: RECEIVE_FULL_SEARCH,
    payload: results,
});
export const requestFullSearch = () => ({
    type: REQUEST_FULL_SEARCH,
});
export const errorFullSearch = () => ({
    type: ERROR_FULL_SEARCH,
});

export const selectSearchType = (type: SearchType) => ({
    type: SELECT_SEARCH_TYPE,
    payload: type,
});
export const setFilter = (filter: Attribute[]) => ({
    type: SET_FILTER,
    payload: filter,
});
