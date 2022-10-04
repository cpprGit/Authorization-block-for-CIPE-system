import {Checkbox} from '@blueprintjs/core';
import React, {FC, memo, useCallback, useEffect, useState} from 'react';
import './form-input.styl';

type Props = {
    inputRef: (ref: {value: string[]} | null) => void;
    defaultValue?: string[];
    disabled?: boolean;
    variants?: string[];
};

const getSwitchTableFromVariants = (data: string[], type: string) => {
    let i: number = 0;
    let result: string[] = [];

    while (data[i++] !== type + 'Start') {
        if (!data[i]) return [];
    }

    while (data[i] !== type + 'End') {
        result.push(data[i++]);
        if (!data[i]) return [];
    }

    return result;
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
export const SwitchTableInputView: FC<Props> = memo(
    ({inputRef, disabled, defaultValue, variants = []}) => {
        const columns: string[] = getSwitchTableFromVariants(variants, 'columns');
        const rows: string[] = getSwitchTableFromVariants(variants, 'rows');
        const [values, setValues] = useState<string[]>(prepareValue(defaultValue));
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
        useEffect(() => {
            inputRef && inputRef({value: values});
        }, [inputRef, values]);

        return (
            <div className={'switch-table switch-table__viewer'}>
                <table className={'switch-table__wrapper'}>
                    <tbody className={'switch-table__content'}>
                        <tr className={'switch-table__row-top'}>
                            <th />
                            {columns.map((column) => (
                                <th>{column}</th>
                            ))}
                        </tr>
                        {rows.map((row) => (
                            <tr>
                                <th>{row}</th>
                                {columns.map((column: string) => (
                                    <td>
                                        <Checkbox
                                            key={row + ',' + column}
                                            checked={values.includes(row + ',' + column)}
                                            disabled={disabled}
                                            onChange={onSelect(row + ',' + column)}
                                        />
                                    </td>
                                ))}
                            </tr>
                        ))}
                    </tbody>
                </table>
            </div>
        );
    }
);
