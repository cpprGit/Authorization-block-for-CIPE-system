import {Intent, Position} from '@blueprintjs/core';
import {DateInput} from '@blueprintjs/datetime';
import '@blueprintjs/datetime/lib/css/blueprint-datetime.css';
import moment from 'moment';

import React, {FC, memo, useCallback, useEffect, useMemo, useState} from 'react';

type Props = {
    intent: Intent;
    inputRef: (ref: {value: string | null}) => void;
    savedValue?: Date | undefined;
    placeholder?: string;
    defaultValue?: string;
    disabled?: boolean;
};

const FORMAT = 'DD-MM-YYYY';
const formatDate = (date: Date) => moment(date).format(FORMAT);
const parseDate = (str: string) => moment(str, FORMAT).toDate();

export const DateInputView: FC<Props> = memo(
    ({placeholder, savedValue, inputRef, disabled, defaultValue}) => {
        const defVal = useMemo(
            () =>
                (savedValue && new Date(savedValue)) ||
                (defaultValue ? new Date(defaultValue) : undefined),
            [defaultValue, savedValue]
        );

        const [date, setDate] = useState<Date | undefined>(defVal);
        const onChange = useCallback((date: Date) => setDate(date), [setDate]);
        const popoverProps = useMemo(() => ({position: Position.BOTTOM}), []);

        useEffect(() => {
            inputRef && inputRef({value: date ? date.toISOString() : null});
        }, [inputRef, date]);

        return (
            <>
                <DateInput
                    formatDate={formatDate}
                    parseDate={parseDate}
                    placeholder={`ДД-MM-ГГГГ`}
                    disabled={disabled}
                    fill={true}
                    closeOnSelection={true}
                    defaultValue={defVal}
                    onChange={onChange}
                    popoverProps={popoverProps}
                />
            </>
        );
    }
);
