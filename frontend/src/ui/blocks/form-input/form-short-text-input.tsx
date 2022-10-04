import {InputGroup, Intent} from '@blueprintjs/core';
import React, {FC, memo} from 'react';

type Props = {
    intent: Intent;
    inputRef: (ref: HTMLInputElement | null) => void;
    onChange?: () => void;
    placeholder?: string;
    large?: boolean;
    onBlur?: () => void;
    defaultValue?: string;
    savedValue?: string;
    disabled?: boolean;
};

export const ShortTextInputView: FC<Props> = memo(
    ({
        onChange,
        placeholder,
        onBlur,
        intent,
        inputRef,
        large,
        savedValue,
        defaultValue,
        disabled,
    }) => {
        return (
            <InputGroup
                placeholder={placeholder}
                inputRef={inputRef}
                intent={intent}
                large={large}
                defaultValue={savedValue || defaultValue}
                onBlur={onBlur}
                onChange={onChange}
                disabled={disabled}
            />
        );
    }
);
