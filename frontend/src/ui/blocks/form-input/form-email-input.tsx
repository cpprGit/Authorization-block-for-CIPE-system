import {InputGroup, Intent} from '@blueprintjs/core';
import React, {FC, memo} from 'react';

type Props = {
    intent: Intent;
    inputRef: (ref: HTMLInputElement | null) => void;
    savedValue?: string | undefined;
    placeholder?: string;
    defaultValue?: string;
    disabled?: boolean;
};

export const EmailInputView: FC<Props> = memo(
    ({placeholder, inputRef, savedValue, intent, disabled, defaultValue}) => {
        return (
            <InputGroup
                type='email'
                placeholder={placeholder}
                inputRef={inputRef}
                disabled={disabled}
                intent={intent}
                defaultValue={savedValue || defaultValue}
            />
        );
    }
);
