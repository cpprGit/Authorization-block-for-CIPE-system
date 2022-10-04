import {Checkbox, InputGroup} from '@blueprintjs/core';
import {handleStringChange} from '@blueprintjs/docs-theme';
import React, {FC, memo, useCallback, useEffect, useState} from 'react';

type Props = {
    inputRef: (ref: {value: string[]} | null) => void;
    savedValue?: string[];
    defaultValue?: string[];
    hasOtherVariant?: boolean;
    disabled?: boolean;
    variants?: string[];
};

const prepareValue = (value: any): string[] => {
    // "" null undefined
    if (!value) {
        return [];
    }
    // [] ["vze"]
    if (Array.isArray(value)) {
        return value;
    }
    try {
        const val = JSON.parse(value);
        if (Array.isArray(val)) {
            return val;
        }
        return [];
    } catch {
        return [];
    }
};
const OTHER = 'Другое';
export const CheckboxInputView: FC<Props> = memo(
    ({inputRef, savedValue, disabled, defaultValue, hasOtherVariant, variants = []}) => {
        const [values, setValues] = useState<string[]>(
            savedValue ? savedValue : prepareValue(defaultValue)
        );
        // хранение значения в input'e
        const [inputValue, setInputValue] = useState('');
        const onSelect = useCallback(
            (label) => () => {
                if (!values.includes(label)) {
                    values.push(label);
                    setValues([...values]);
                } else {
                    setValues(values.filter((variant) => variant !== label));
                }
            },
            [values, setValues]
        );
        const onInputChange = useCallback(
            handleStringChange((val) => {
                setInputValue(val);
            }),
            [setInputValue]
        );

        // функция для замены значения OTHER на inputValue в values
        const replaceOther = (array: string[], deleteItem: string, addItem: string) => {
            if (array.includes(deleteItem)) {
                array = array.filter((item) => item !== deleteItem);
                array.push(addItem);
            }
            return array;
        };

        useEffect(() => {
            inputRef && inputRef({value: replaceOther(values, OTHER, inputValue)});
        }, [inputRef, values, inputValue]);

        return (
            <>
                {variants.map((variant, index) => (
                    <Checkbox
                        key={index}
                        checked={values.includes(variant)}
                        disabled={disabled}
                        label={variant}
                        onChange={onSelect(variant)}
                    />
                ))}
                {hasOtherVariant && (
                    <Checkbox
                        key={-1}
                        checked={values.includes(OTHER)}
                        disabled={disabled}
                        label={OTHER}
                        onChange={onSelect(OTHER)}
                    />
                )}
                {hasOtherVariant && values.includes(OTHER) && (
                    <InputGroup
                        placeholder='Введите свой вариант ответа'
                        large={false}
                        defaultValue={inputValue}
                        onChange={onInputChange}
                        disabled={disabled}
                    />
                )}
            </>
        );
    }
);
