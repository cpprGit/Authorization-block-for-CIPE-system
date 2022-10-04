import {Switch} from '@blueprintjs/core';
import {Alignment} from '@blueprintjs/core/lib/esm/common/alignment';
import React, {FC, memo, useCallback, useEffect, useState} from 'react';

type Props = {
    inputRef: (ref: {value: boolean} | null) => void;
    savedValue?: boolean;
    placeholder?: string;
    defaultValue?: boolean;
    disabled?: boolean;
};

export const SwitchInputView: FC<Props> = memo(
    ({placeholder, inputRef, savedValue, defaultValue = false, disabled}) => {
        const [value, setValue] = useState(savedValue || defaultValue);
        const handleChange = useCallback(() => setValue(!value), [setValue, value]);
        useEffect(() => {
            inputRef && inputRef({value});
        }, [inputRef, value]);
        return (
            <Switch
                alignIndicator={Alignment.LEFT}
                disabled={disabled}
                innerLabelChecked='да'
                innerLabel='нет'
                checked={value}
                onChange={handleChange}
            />
        );
    }
);
