import {combineReducers} from 'redux';
import {Actions} from 'src/store/actions/action-type';
import {chats, ChatsState} from 'src/store/reducers/chats.reducer';
import {attributes, AttributesState} from './attributes.reducer';
import {defaultForms, DefaultFormsState} from './default-forms.reducer';
import {search, SearchState} from './search.reducer';
import {userArchiveForms} from './user-archive-forms.reducer';
import {userForms, UserFormsState} from './user-forms.reducer';
import {user, UserState} from './user.reducer';

export type State = {
    attributes: AttributesState;
    user: UserState;
    chats: ChatsState;
    search: SearchState;
    userForms: UserFormsState;
    defaultForms: DefaultFormsState;
    userArchiveForms: UserFormsState;
};

export const rootReducer = combineReducers<State, Actions>({
    attributes,
    user,
    chats,
    search,
    userForms,
    defaultForms,
    userArchiveForms,
});
