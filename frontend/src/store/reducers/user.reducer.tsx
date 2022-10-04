import {Reducer} from 'redux';
import {Actions} from 'src/store/actions/action-type';
import {LOGOUT, SET_USER_DATA} from 'src/store/actions/user.actions';
import {UserData} from 'src/types';

export type UserState =
    | {
          isAuthed: false;
      }
    | ({isAuthed: true} & UserData);

export const user: Reducer<UserState, Actions> = (
    state: UserState = {
        isAuthed: false,
    },
    action: Actions
) => {
    switch (action.type) {
        case SET_USER_DATA: {
            return {
                isAuthed: true,
                ...action.payload,
            };
        }
        case LOGOUT: {
            return {
                isAuthed: false,
            };
        }
        default: {
            return state;
        }
    }
};
