import {Classes, InputGroup} from '@blueprintjs/core';
import React, {FC, memo, useCallback, useState} from 'react';
import './search.styl';

type Props = {
    className?: string;
    component?: any;
    searchPropertyName?: string;
    items?: any[];
};

export const Search: FC<Props> = memo(
    ({className, component: Component, items, searchPropertyName}) => {
        const [filter, setFilter] = useState('');
        const onChange = useCallback(
            ({target}: any) => {
                setFilter(String(target.value).toLowerCase());
            },
            [setFilter]
        );
        return (
            <>
                <InputGroup
                    className={`${Classes.ROUND} ${Classes.FILL} search ${
                        className ? className : ''
                    }`}
                    leftIcon='search'
                    value={filter}
                    onChange={onChange}
                    placeholder='Искать...'
                />
                {Component &&
                    items &&
                    searchPropertyName &&
                    items
                        .filter((item) => item[searchPropertyName].toLowerCase().includes(filter))
                        .map((item) => (
                            <Component key={item.id || item[searchPropertyName]} {...item} />
                        ))}
            </>
        );
    }
);
