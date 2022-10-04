import {Intent, TextArea} from '@blueprintjs/core';
import {handleStringChange} from '@blueprintjs/docs-theme';
import React, {FC, memo, useCallback, useEffect, useState} from 'react';

type Props = {
    intent: Intent;
    inputRef: (ref: HTMLTextAreaElement | null) => void;
    savedValue?: string | undefined;
    placeholder?: string;
    defaultValue?: string;
    onBlur?: () => void;
    disabled?: boolean;
    clearRef?: (handleDelete: () => void) => void;
};

export const LongTextInputView: FC<Props> = memo(
    ({placeholder, intent, inputRef, savedValue, defaultValue, onBlur, disabled, clearRef}) => {
        const [value, setValue] = useState(savedValue || defaultValue || '');
        const handleChange = useCallback(
            handleStringChange((textContent: string) => setValue(textContent)),
            [setValue]
        );
        useEffect(() => {
            clearRef && clearRef(() => setValue(''));
        }, [clearRef, setValue]);

        useEffect(() => {
            // @ts-ignore
            inputRef && inputRef({value});
        }, [inputRef, value]);
        return (
            <TextArea
                placeholder={placeholder}
                intent={intent}
                fill={true}
                disabled={disabled}
                growVertically={true}
                onChange={handleChange}
                value={value}
                onBlur={onBlur}
            />
        );
    }
);
