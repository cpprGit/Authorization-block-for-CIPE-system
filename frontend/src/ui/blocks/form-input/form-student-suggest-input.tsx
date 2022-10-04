import {Intent} from '@blueprintjs/core';
import React, {FC, memo} from 'react';
import {SearchInputView} from 'src/ui/blocks/form-input/form-mentor-suggest-input';

type Student = {
    name: string;
    id: string;
    type: string;
};
type Props = {
    intent: Intent;
    inputRef: (ref: {value: Student} | null) => void;
    savedValue?: {name: string; id: string; type: string};
    placeholder?: string;
    onChange?: () => void;
    defaultValue?: Student;
    disabled?: boolean;
};
export const StudentInputView: FC<Props> = memo(
    ({intent, inputRef, savedValue, defaultValue, disabled}) => {
        return (
            <SearchInputView
                key={'query'}
                inputRef={inputRef}
                intent={intent}
                path={'search/students'}
                defaultValue={savedValue || defaultValue}
                disabled={disabled}
                placeholder={`Начните имя студента...`}
            />
        );
    }
);
