import {Card, Icon} from '@blueprintjs/core';
import React, {FC, memo} from 'react';
import './attribute-card.styl';

type Props = {
    name?: string;
    description?: string;
    onClick: () => void;
    isPlaceholder?: boolean;
};
export const AttributeCard: FC<Props> = memo(({name, description, onClick, isPlaceholder}) => {
    if (isPlaceholder) {
        return (
            <Card className='attribute-card _placeholder' onClick={onClick}>
                <Icon icon='plus' />
            </Card>
        );
    }
    return (
        <Card className='attribute-card' onClick={onClick}>
            <div className='attribute-card__title'>{name}</div>
            {description && <div className='attribute-card__description'>{description}</div>}
        </Card>
    );
});
