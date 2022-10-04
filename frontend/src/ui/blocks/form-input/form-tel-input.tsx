import {InputGroup, Intent} from '@blueprintjs/core';
import React, {FC, memo} from 'react';

type Props = {
    intent: Intent;
    inputRef: (ref: HTMLInputElement | null) => void;
    placeholder?: string;
    defaultValue?: string;
    disabled?: boolean;
};

const phoneFormatter = (e: React.FormEvent<HTMLInputElement>) => {
    if (e.currentTarget.value === '8') {
        e.currentTarget.value = '+7';
    }

    //TODO: Будущее автозаполнение по локализованным паттернам.
};

export const TelInputView: FC<Props> = memo(({inputRef, intent, disabled, defaultValue}) => {
    return (
        <InputGroup
            type='tel'
            placeholder='+7(___)___-__-__'
            inputRef={inputRef}
            disabled={disabled}
            onChange={phoneFormatter}
            intent={intent}
            defaultValue={defaultValue}
        />
    );
});
