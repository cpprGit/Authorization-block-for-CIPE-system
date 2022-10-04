import {InputGroup, Intent, Radio, RadioGroup} from '@blueprintjs/core';
import {handleStringChange} from '@blueprintjs/docs-theme';
import React, {FC, memo, useCallback, useEffect, useState} from 'react';

type Props = {
    intent: Intent;
    inputRef: (ref: {value: string} | null) => void;
    hasOtherVariant?: boolean;
    savedValue?: string | undefined;
    placeholder?: string;
    defaultValue?: string;
    disabled?: boolean;
    variants?: string[];
};

const OTHER = 'Другое';

export const RadioInputView: FC<Props> = memo(
    ({
        variants = [],
        placeholder,
        intent,
        inputRef,
        savedValue,
        defaultValue,
        hasOtherVariant,
        disabled,
    }) => {
        const [value, setValue] = useState(savedValue ? savedValue : defaultValue);
        // отвечает за отображение input'a под "Другое"
        const [showInput, setShowInput] = useState(false);
        // хранение значения в input'e
        const [inputValue, setInputValue] = useState('');

        // нажатие на radio
        const onChange = useCallback(
            handleStringChange((val) => {
                setShowInput(val === OTHER);
                setValue(val);
            }),
            [setValue]
        );
        // ввод в input
        const onInputChange = useCallback(
            handleStringChange((val) => {
                setInputValue(val);
            }),
            [setInputValue]
        );

        // обработка пришедшего hasOtherVariant и defaultValue
        useEffect(() => {
            // если defaultValue приходит с бэка не пустое. пытаемся присвоить значение одному из radio.
            // если такого radio нет, пихаем в input.
            if (defaultValue && !variants.includes(defaultValue)) {
                hasOtherVariant = true;
                setShowInput(true);
                setValue(OTHER);
                setInputValue(defaultValue);
            } else if (defaultValue) {
                setValue(defaultValue);
            }
        }, [defaultValue, variants, hasOtherVariant]);

        // выбор значения для отправки
        useEffect(() => {
            inputRef &&
                inputRef({
                    value: (value === OTHER ? (hasOtherVariant ? inputValue : '') : value) || '',
                });
        }, [inputRef, value, inputValue]);

        return (
            <RadioGroup inline={false} onChange={onChange} selectedValue={value}>
                {variants.map((str, ind) => {
                    return <Radio disabled={disabled} label={str} value={str} key={ind} />;
                })}
                {hasOtherVariant && (
                    <Radio disabled={disabled} label={OTHER} value={OTHER} key={-1} />
                )}
                {hasOtherVariant && showInput && (
                    <InputGroup
                        placeholder='Введите свой вариант ответа'
                        inputRef={inputRef}
                        intent={intent}
                        large={false}
                        defaultValue={inputValue}
                        onChange={onInputChange}
                        disabled={disabled}
                    />
                )}
            </RadioGroup>
        );
    }
);
