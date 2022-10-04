import {Intent} from '@blueprintjs/core';
import React, {FC, memo, useCallback, useEffect, useState} from 'react';
import {SearchInputView} from 'src/ui/blocks/form-input/form-mentor-suggest-input';

type Organisation = {
    name: string;
    id: string;
    type: string;
};

const getDefaultState = (defaultValue?: Organisation | Organisation[]): InputState[] => {
    if (!defaultValue || (Array.isArray(defaultValue) && !defaultValue.length)) {
        return [{query: ''}];
    }

    if (Array.isArray(defaultValue)) {
        const state: InputState[] = defaultValue.map((val, index) => {
            return {
                query: index === 0 ? '' : defaultValue[index - 1].id,
                defaultValue: val,
            };
        });
        state.push({query: defaultValue[defaultValue.length - 1].id});
        return state;
    }
    return [{query: '', defaultValue: defaultValue}];
};

type InputState = {
    query: string;
    defaultValue?: Organisation;
};
type Props = {
    intent: Intent;
    inputRef: (ref: {value?: Organisation} | null) => void;
    placeholder?: string;
    onChange?: () => void;
    defaultValue?: Organisation | Organisation[];
    disabled?: boolean;
};
export const OrganisationInputView: FC<Props> = memo(
    ({intent, inputRef, defaultValue, disabled}) => {
        const [inputStates, setInputStates] = useState<InputState[]>(getDefaultState(defaultValue));
        const onSelect = useCallback(
            (index: number) => (val?: Organisation) => {
                const newInputStates = [];
                for (let i = 0; i <= index; i++) {
                    newInputStates.push(inputStates[i]);
                }

                newInputStates[index].defaultValue = val;
                if (val) {
                    newInputStates.push({query: val.id});
                }
                setInputStates(newInputStates);

                if (newInputStates.length < 2) {
                    inputRef({value: newInputStates[0].defaultValue});
                } else {
                    inputRef({
                        value: newInputStates[newInputStates.length - 2].defaultValue,
                    });
                }
            },
            [inputRef, setInputStates, inputStates]
        );

        useEffect(() => {
            if (defaultValue) {
                if (Array.isArray(defaultValue)) {
                    inputRef({value: defaultValue[defaultValue.length - 1]});
                } else {
                    inputRef({value: defaultValue});
                }
            }
        }, [defaultValue, inputRef]);
        return (
            <>
                {inputStates.map(({query, defaultValue}, ind) => (
                    <SearchInputView
                        onChange={onSelect(ind)}
                        key={query}
                        intent={intent}
                        path={`formatted/organisation/descendants/${query}`}
                        defaultValue={defaultValue}
                        disabled={disabled}
                        placeholder={`Начните вводить наименование ${
                            ind ? 'под' : ''
                        }организации...`}
                    />
                ))}
            </>
        );
    }
);
