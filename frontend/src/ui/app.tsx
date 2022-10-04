import {Position, Toaster} from '@blueprintjs/core';
import React, {createRef, memo, useMemo} from 'react';
import {Provider} from 'react-redux';
import {BrowserRouter, Route, Switch} from 'react-router-dom';
import {createXhrClient, UploadProvider} from 'react-use-upload';
import {createStore} from 'redux';
import {CppwApi} from 'src/api/api';
import {rootReducer} from 'src/store/reducers';
import {UserRole} from 'src/types';
import {Footer} from 'src/ui/blocks/footer/footer';
import {Header} from 'src/ui/blocks/header/header';
import {CppwApiContext} from 'src/ui/contexts/cppw-api-context/cppw-api-context';
import {
    AdminPage,
    ChatPage,
    FormsPage,
    HomePage,
    NewProjectRequestPage,
    ProfilePage,
    SearchPage,
} from 'src/ui/routes';
import {checkURLPermission} from 'src/ui/utils/auth';
import {BACKEND_URL} from 'src/ui/utils/constants';
import {
    ACTIVITY_PROFILE_PATH,
    COMPANY_PROFILE_PATH,
    PROJECT_PROFILE_PATH,
    PROJECT_REQUEST_PROFILE_PATH,
    USER_PROFILE_PATH,
} from './routes/profile/profile-page';

const store = createStore(rootReducer);

export const App: React.FC = memo(() => {
    const toasterRef = createRef<Toaster>();
    // eslint-disable-next-line react-hooks/exhaustive-deps
    const cppwApi = useMemo(() => new CppwApi(store, toasterRef), []);
    const user = cppwApi.store.getState().user;
    const {role: userRole} = user.isAuthed ? user : {role: UserRole.Initial};
    checkURLPermission(window.location.pathname, userRole);
    return (
        <UploadProvider client={createXhrClient({baseUrl: BACKEND_URL})}>
            <Provider store={store}>
                <BrowserRouter>
                    <CppwApiContext.Provider value={cppwApi}>
                        <div className='App'>
                            <Header />
                            <Switch>
                                <Route exact path='/' component={HomePage} />
                                <Route exact path='/for_partners' component={HomePage} />
                                <Route exact path='/for_students' component={HomePage} />
                                <Route exact path='/for_workers' component={HomePage} />
                                <Route exact path='/authorization' component={HomePage} />
                                <Route exact path='/help' component={HomePage} />
                                <Route exact path='/contacts' component={HomePage} />

                                <Route
                                    exact
                                    path={`${USER_PROFILE_PATH}/:id`}
                                    component={ProfilePage}
                                />
                                <Route exact path={USER_PROFILE_PATH} component={ProfilePage} />
                                <Route
                                    exact
                                    path={`${PROJECT_PROFILE_PATH}/:id`}
                                    component={ProfilePage}
                                />
                                <Route exact path={PROJECT_PROFILE_PATH} component={ProfilePage} />
                                <Route
                                    exact
                                    path={`${ACTIVITY_PROFILE_PATH}/:id`}
                                    component={ProfilePage}
                                />
                                <Route exact path={ACTIVITY_PROFILE_PATH} component={ProfilePage} />
                                <Route
                                    exact
                                    path={`${COMPANY_PROFILE_PATH}/:id`}
                                    component={ProfilePage}
                                />
                                <Route exact path={COMPANY_PROFILE_PATH} component={ProfilePage} />
                                <Route
                                    exact
                                    path={`${PROJECT_REQUEST_PROFILE_PATH}/:id`}
                                    component={ProfilePage}
                                />
                                <Route
                                    exact
                                    path={PROJECT_REQUEST_PROFILE_PATH}
                                    component={ProfilePage}
                                />
                                <Route exact path='/admin' component={AdminPage} />
                                <Route
                                    exact
                                    path='/new_project_request'
                                    component={NewProjectRequestPage}
                                />

                                <Route exact path='/chat' component={ChatPage} />

                                <Route exact path='/search' component={SearchPage} />

                                <Route exact path='/forms' component={FormsPage} />
                                <Route component={HomePage} />
                            </Switch>
                            <Footer />

                            <Toaster
                                canEscapeKeyClear={true}
                                position={Position.TOP}
                                ref={toasterRef}
                            />
                        </div>
                    </CppwApiContext.Provider>
                </BrowserRouter>
            </Provider>
        </UploadProvider>
    );
});
