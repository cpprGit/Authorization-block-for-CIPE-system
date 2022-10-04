import {Icon, Intent} from '@blueprintjs/core';
import React, {FC, memo} from 'react';
import './error-view.styl';

type Props = {
    title?: string;
    subtitle?: string;
    onRetry?: () => void;
    size?: 's';
};

export const ErrorView: FC<Props> = memo(({title, subtitle, onRetry, size}) => (
    <div
        className={`error-view ${onRetry ? '_is-link' : ''} ${size ? `_size-${size}` : ''}`}
        onClick={onRetry}
    >
        <div className='error-view__block'>
            <Icon
                className='error-view__icon'
                icon={onRetry ? 'refresh' : 'error'}
                intent={Intent.DANGER}
            />
            <div className='error-view__message'>
                {title && <div className='error-view__title'>{title}</div>}
                {subtitle && <div className='error-view__subtitle'>{subtitle}</div>}
            </div>
        </div>
    </div>
));
