import React, {FC, memo} from 'react';
import './layout.styl';

type Props = {
    leftComponent?: JSX.Element | JSX.Element[];
    rightComponent?: JSX.Element | JSX.Element[];
};

export const Layout: FC<Props> = memo(({leftComponent = null, rightComponent = null}) => (
    <div className='layout'>
        <div className='layout__left-column'>{leftComponent}</div>
        <div className='layout__right-column'>{rightComponent}</div>
    </div>
));
