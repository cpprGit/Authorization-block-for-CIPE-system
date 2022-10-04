import {Icon, Intent} from '@blueprintjs/core';
import React, {FC, memo} from 'react';
import './success-view.styl';

type Props = {
    title: string;
    onRetry?: () => void;
    size?: 's';
};

export const SuccessView: FC<Props> = memo(({title, onRetry, size}) => (
    <div className={`success-view ${size ? `_size-${size}` : ''}`} onClick={onRetry}>
        <div className='success-view__block'>
            <Icon className='success-view__icon' icon='tick' intent={Intent.SUCCESS} />
            {title && <div className='success-view__title'>{title}</div>}
            {onRetry && <div className='success-view__subtitle'>Заполнить повторно.</div>}
        </div>
    </div>
));
