import {Icon, Position, Tooltip} from '@blueprintjs/core';

import React, {FC, memo} from 'react';
import './hint-label.styl';

type Props = {
    text: string;
};

export const HintLabel: FC<Props> = memo(({text}) => (
    <span style={{display: 'inline-block', margin: '0px 5px'}}>
        <Tooltip
            content={<div className='hint-label'>{text}</div>}
            position={Position.RIGHT}
            usePortal={false}
        >
            <Icon icon='help' />
        </Tooltip>
    </span>
));
