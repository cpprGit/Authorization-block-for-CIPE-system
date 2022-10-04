import {TOKEN_PARAM_NAME} from 'src/api/api';
import {UserData} from 'src/types';
import {deleteCookie} from 'src/ui/utils/cookie';

export const SET_USER_DATA = 'SET_USER_DATA' as const;
export const LOGOUT = 'LOGOUT' as const;

export const setUserData = (userData: UserData) => ({
    type: SET_USER_DATA,
    payload: userData,
});
export const logout = () => {
    deleteCookie(TOKEN_PARAM_NAME);
    document.location.replace(window.location.origin);
    return {
        type: LOGOUT,
    };
};
