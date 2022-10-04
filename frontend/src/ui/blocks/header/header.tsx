import {Alignment, Button, Navbar} from '@blueprintjs/core';

import React, {FC, memo} from 'react';
import {useSelector} from 'react-redux';
import {Link} from 'react-router-dom';
import {State} from 'src/store/reducers';
import {UserRole} from 'src/types';
import {HomePageLocations} from 'src/ui/routes/home/home-page';
import {AUTH0_PARAMS_URL, BACKEND_URL} from 'src/ui/utils/constants';
import './header.styl';

const AUTH = `https://${AUTH0_PARAMS_URL}.eu.auth0.com/authorize?response_type=code&client_id=${
    process.env.IS_DEV ? 'yylyc1ApB4GlR42juTSFfoCQqb4wL0JY' : '60pGdnKNj2GPtLKCqUZdPT6pqF7RnyRJ'
}&redirect_uri=${BACKEND_URL}/api/v1/callback&scope=openid email profile&audience=${BACKEND_URL}&state=`;
const ROLE_TO_MENU: {
    [key: string]: {
        hasForms: boolean;
        hasChats: boolean;
        hasProjects: boolean;
        hasAdmin: boolean;
    };
} = {
    [UserRole.Manager]: {
        hasForms: true,
        hasChats: true,
        hasProjects: false,
        hasAdmin: true,
    },
    [UserRole.Supervisor]: {
        hasForms: true,
        hasChats: true,
        hasProjects: false,
        hasAdmin: true,
    },
    [UserRole.Representative]: {
        hasForms: false,
        hasChats: false,
        hasProjects: true,
        hasAdmin: false,
    },
    [UserRole.Mentor]: {
        hasForms: false,
        hasChats: false,
        hasProjects: true,
        hasAdmin: false,
    },
    [UserRole.Student]: {
        hasForms: false,
        hasChats: false,
        hasProjects: false,
        hasAdmin: false,
    },
    [UserRole.AcademicManager]: {
        hasForms: false,
        hasChats: false,
        hasProjects: true,
        hasAdmin: false,
    },
    [UserRole.OfficeManager]: {
        hasForms: false,
        hasChats: false,
        hasProjects: false,
        hasAdmin: false,
    },
    undefined: {
        hasForms: false,
        hasChats: false,
        hasProjects: false,
        hasAdmin: false,
    },
};

export const Header: FC = memo(() => {
    const {isAuthed, role: userRole, userId} = useSelector((state: State) =>
        state.user.isAuthed
            ? state.user
            : {
                  isAuthed: false,
                  role: undefined,
                  userId: undefined,
              }
    );
    const {hasForms, hasChats, hasProjects, hasAdmin} = ROLE_TO_MENU[String(userRole)];
    return (
        <Navbar className='header'>
            <Navbar.Group align={Alignment.LEFT}>
                <Navbar.Heading className='header__long-title'>
                    <Link className='header__link' to='/'>
                        Центр практик и проектной работы
                    </Link>
                </Navbar.Heading>
                <Navbar.Heading className='header__short-title'>
                    <Link className='header__link' to={HomePageLocations.Main}>
                        ЦППР
                    </Link>
                </Navbar.Heading>
            </Navbar.Group>

            <Navbar.Group align={Alignment.RIGHT}>
                {isAuthed ? (
                    <>
                        <Link className='header__link' to='/search'>
                            <Button className='bp3-minimal' icon='search' />
                        </Link>
                        {hasChats && (
                            <Link className='header__link' to='/chat'>
                                <Button className='bp3-minimal' icon='chat' />
                            </Link>
                        )}
                        {hasForms && (
                            <Link className='header__link' to='/forms'>
                                <Button className='bp3-minimal' icon='clipboard' />
                            </Link>
                        )}

                        {hasProjects && (
                            <Link className='header__link' to={`/new_project_request`}>
                                <Button className='bp3-minimal' icon='plus' />
                            </Link>
                        )}
                        <Link className='header__link' to={`/user?id=${userId}`}>
                            <Button className='bp3-minimal' icon='person' />
                        </Link>
                        {hasAdmin && (
                            <Link className='header__link' to='/admin'>
                                <Button className='bp3-minimal' icon='cog' />
                            </Link>
                        )}
                    </>
                ) : (
                    <>
                        <a className='header__link' href={`${HomePageLocations.Authorization}`}>
                            <Button className='bp3-minimal' icon='log-in' text='Вход' />
                        </a>
                    </>
                )}
            </Navbar.Group>
        </Navbar>
    );
});
