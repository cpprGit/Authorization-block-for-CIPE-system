import {Alignment, Button, Navbar} from '@blueprintjs/core';

import React, {FC, memo} from 'react';
import {Link} from 'react-router-dom';
import './footer.styl';

export const Footer: FC = memo(() => (
    <Navbar>
        <Navbar.Group align={Alignment.LEFT}>
            <Navbar.Heading className='footer__long-title'>
                © {new Date().getFullYear()} ЦППР ФКН НИУ ВШЭ
            </Navbar.Heading>
            <Navbar.Heading className='footer__short-title'>
                © {new Date().getFullYear()} ЦППР{' '}
            </Navbar.Heading>
        </Navbar.Group>

        <Navbar.Group align={Alignment.RIGHT}>
            <Link className='header__link' to='/contacts'>
                <Button className='bp3-minimal' text='Контакты' />
            </Link>
            <Link className='header__link' to='/help'>
                <Button className='bp3-minimal' text='Помощь' />
            </Link>
        </Navbar.Group>
    </Navbar>
));
