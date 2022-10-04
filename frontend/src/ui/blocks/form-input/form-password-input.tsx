import {InputGroup, Intent} from '@blueprintjs/core';
import React, {FC, memo} from 'react';

type Props = {
    intent: Intent;
    inputRef: (ref: HTMLInputElement | null) => void;
    placeholder?: string;
    defaultValue?: string;
    disabled?: boolean;
};

export const PasswordInputView: FC<Props> = memo(
    ({placeholder, intent, inputRef, defaultValue, disabled}) => {
        return (
            <InputGroup
                type={'password'}
                disabled={disabled}
                placeholder={placeholder}
                inputRef={inputRef}
                intent={intent}
                defaultValue={defaultValue}
            />
        );
    }
);
