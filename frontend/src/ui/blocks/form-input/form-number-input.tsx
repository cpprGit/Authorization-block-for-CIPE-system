import {Intent, NumericInput} from '@blueprintjs/core';
import React, {FC, memo, useCallback, useEffect, useState} from 'react';

type Props = {
    intent: Intent;
    savedValue?: string | undefined;
    inputRef: (ref: HTMLInputElement | null) => void;
    placeholder?: string;
    defaultValue?: string;
    disabled?: boolean;
    max?: number;
    min?: number;
};

export const NumberInputView: FC<Props> = memo(
    ({placeholder, intent, inputRef, savedValue, defaultValue, max, min, disabled}) => {
        const [value, setValue] = useState(savedValue || defaultValue || '');
        const handleChange = useCallback((val, textContent: string) => setValue(textContent), [
            setValue,
        ]);

        useEffect(() => {
            // @ts-ignore
            inputRef && inputRef({value});
        }, [inputRef, value]);
        return (
            <NumericInput
                placeholder={placeholder}
                inputRef={inputRef}
                intent={intent}
                disabled={disabled}
                fill={true}
                buttonPosition='right'
                value={value}
                onValueChange={handleChange}
                max={max}
                min={min}
            />
        );
    }
);
