import React, {FC, memo} from 'react';
import {Link} from 'react-router-dom';
import './tab.styl';

type Props = {
    link?: string;
    title: string;
    onClick?: () => void;
    isActive: boolean;
};

export const Tab: FC<Props> = memo(({link, title, onClick, isActive}) => (
    <div className={`tab ${isActive ? '_is-active' : ''}`} onClick={onClick}>
        {link ? (
            <Link to={link} className='tab__text' onClick={onClick}>
                {title}
            </Link>
        ) : (
            <div className='tab__text'> {title}</div>
        )}
    </div>
));
