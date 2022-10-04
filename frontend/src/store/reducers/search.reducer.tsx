import {Actions} from 'src/store/actions/action-type';
import {
    ERROR_FULL_SEARCH,
    ERROR_SEARCH,
    RECEIVE_FULL_SEARCH,
    RECEIVE_SEARCH,
    REQUEST_FULL_SEARCH,
    REQUEST_SEARCH,
    SELECT_SEARCH_TYPE,
    SET_FILTER,
} from 'src/store/actions/search.actions';
import {AsyncStatus, Attribute, ProfileOrSearchItem, SearchType} from 'src/types';
import {
    BLOCKED_ATTRIBUTE,
    PROJECT_REQUEST_STATUS_ATTRIBUTE,
    STUDENT_STATUS_ATTRIBUTE,
} from 'src/ui/utils/constants';

export type SearchState = {
    searchFilterStatus: AsyncStatus;
    searchStatus: AsyncStatus;
    searchType: SearchType;
    records: ProfileOrSearchItem[];
    fields: Attribute[];
};

export const search = (
    state: SearchState = {
        searchFilterStatus: AsyncStatus.Initial,
        searchStatus: AsyncStatus.Initial,
        searchType: SearchType.Initial,
        records: [],
        fields: [],
    },
    action: Actions
) => {
    switch (action.type) {
        case RECEIVE_FULL_SEARCH: {
            const {schema, records} = action.payload;
            const newFields =
                state.searchType === SearchType.ProjectRequests
                    ? [PROJECT_REQUEST_STATUS_ATTRIBUTE, ...schema.attributes]
                    : state.searchType === SearchType.Students
                    ? [STUDENT_STATUS_ATTRIBUTE, ...schema.attributes, BLOCKED_ATTRIBUTE]
                    : [
                          SearchType.Representatives,
                          SearchType.Organisations,
                          SearchType.Managers,
                          SearchType.Mentors,
                      ].includes(state.searchType)
                    ? [...schema.attributes, BLOCKED_ATTRIBUTE]
                    : schema.attributes;
            const newRecords = records.map((item: ProfileOrSearchItem) => {
                try {
                    return {...item, content: JSON.parse(item.schemaContent)};
                } catch (e) {
                    return {...item, content: {}};
                }
            });
            return {
                ...state,
                searchFilterStatus: AsyncStatus.Success,
                searchStatus: AsyncStatus.Success,
                records: newRecords,
                fields: newFields,
            };
        }
        case REQUEST_FULL_SEARCH: {
            return {
                ...state,
                searchStatus: AsyncStatus.Pending,
                records: [],
                fields: [],
            };
        }
        case ERROR_FULL_SEARCH: {
            return {
                ...state,
                searchStatus: AsyncStatus.Error,
                records: [],
                fields: [],
            };
        }

        case RECEIVE_SEARCH: {
            const {records} = action.payload;
            const newRecords = records.map((item: ProfileOrSearchItem) => {
                try {
                    return {...item, content: JSON.parse(item.schemaContent)};
                } catch (e) {
                    return {...item, content: {}};
                }
            });
            return {
                ...state,
                searchStatus: AsyncStatus.Success,
                records: newRecords,
            };
        }
        case REQUEST_SEARCH: {
            return {
                ...state,
                searchStatus: AsyncStatus.Pending,
                records: [],
            };
        }
        case ERROR_SEARCH: {
            return {
                ...state,
                searchStatus: AsyncStatus.Error,
                records: [],
            };
        }

        case SELECT_SEARCH_TYPE: {
            return {
                ...state,
                searchType: action.payload,
            };
        }
        case SET_FILTER: {
            return {
                ...state,
                fields: action.payload,
            };
        }

        default: {
            return state;
        }
    }
};
