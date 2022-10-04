import {Intent, MenuItem} from '@blueprintjs/core';
import {Suggest} from '@blueprintjs/select';
import React, {FC, memo, MouseEventHandler, useCallback, useEffect, useState} from 'react';
import {ApiResponse, AsyncStatus} from 'src/types';
import {LoadableView} from 'src/ui/blocks/loadable-view/loadable-view';
import {useCppwApiContext} from 'src/ui/contexts/cppw-api-context/cppw-api-context';

type Props = {
    path?: string;
    intent: Intent;
    inputRef?: (ref: {value: any} | null) => void;
    savedValue?: {id: string; name: string; type: string};
    placeholder?: string;
    onChange?: (val: IMentor) => void;
    defaultValue?: {id: string; name: string; type: string};
    disabled?: boolean;
};

const filterMentor = (query: string, mentor: IMentor, _index?: number, exactMatch?: boolean) => {
    const normalizedTitle = mentor.name.toLowerCase();
    const normalizedQuery = query.toLowerCase();

    if (exactMatch) {
        return normalizedTitle === normalizedQuery;
    } else {
        return `${normalizedTitle} ${mentor.id}`.indexOf(normalizedQuery) >= 0;
    }
};

const escapeRegExpChars = (text: string) => {
    // eslint-disable-next-line no-useless-escape
    return text.replace(/([.*+?^=!:${}()|\[\]\/\\])/g, '\\$1');
};

const highlightText = (text: string, query: string) => {
    let lastIndex = 0;
    const words = query
        .split(/\s+/)
        .filter((word) => word.length > 0)
        .map(escapeRegExpChars);
    if (words.length === 0) {
        return [text];
    }
    const regexp = new RegExp(words.join('|'), 'gi');
    const tokens: React.ReactNode[] = [];
    while (true) {
        const match = regexp.exec(text);
        if (!match) {
            break;
        }
        const length = match[0].length;
        const before = text.slice(lastIndex, regexp.lastIndex - length);
        if (before.length > 0) {
            tokens.push(before);
        }
        lastIndex = regexp.lastIndex;
        tokens.push(<strong key={lastIndex}>{match[0]}</strong>);
    }
    const rest = text.slice(lastIndex);
    if (rest.length > 0) {
        tokens.push(rest);
    }
    return tokens;
};

export const renderItem = (
    mentor: IMentor,
    {
        handleClick,
        modifiers,
        query,
    }: {
        handleClick: MouseEventHandler<HTMLElement>;
        index?: number;
        modifiers: {
            active: boolean;
            disabled: boolean;
            matchesPredicate: boolean;
        };
        query: string;
    }
) => {
    if (!modifiers.matchesPredicate) {
        return null;
    }
    const text = `${mentor.name}`;
    return (
        <MenuItem
            active={modifiers.active}
            disabled={modifiers.disabled}
            labelElement={
                mentor.id ? (
                    <a
                        className={'form-mentor-suggest-input__link'}
                        href={`/${mentor.type || 'user'}?id=${mentor.id}`}
                        target='_blank'
                        rel='noopener noreferrer'
                        onClick={(e) => {
                            e.stopPropagation();
                        }}
                    >
                        Подробнее...
                    </a>
                ) : null
            }
            key={mentor.id}
            onClick={handleClick}
            text={highlightText(text, query)}
        />
    );
};

export interface IMentor {
    name: string;
    id: string;
    type: string;
}

const renderInputValue = (mentor: IMentor) => mentor.name;

export function areFilmsEqual(mentorA: IMentor, mentorB: IMentor) {
    return mentorA.name.toLowerCase() === mentorB.name.toLowerCase();
}

const MentorSuggest = Suggest.ofType<IMentor>();
export const SearchInputView: FC<Props> = memo(
    ({onChange, placeholder, inputRef, savedValue, defaultValue, disabled, path}) => {
        const cppwApi = useCppwApiContext();
        const [vars, setVars] = useState<IMentor[]>([]);
        const [selectedValue, setSelectedValue] = useState(savedValue || defaultValue);
        const [status, setStatus] = useState(AsyncStatus.Initial);
        const onSelect = useCallback(
            (val) => {
                const value = val && val.type === 'placeholder' ? undefined : val;
                setSelectedValue(val);
                onChange && onChange(value);
            },
            [onChange, setSelectedValue]
        );
        const onRetry = useCallback(() => {
            if (cppwApi) {
                setStatus(AsyncStatus.Pending);
                cppwApi
                    .get(path ? path : 'utils/mentors?name=')
                    .then((res: ApiResponse) => {
                        const result =
                            'records' in res
                                ? res.records.map((record: any) => record.name)
                                : res.map((record: any) => ({
                                      ...record,
                                      type: record.type || 'user',
                                  }));
                        setVars([{id: '', name: '-', type: 'placeholder'}, ...result]);
                        setStatus(AsyncStatus.Success);
                    })
                    .catch(() => {
                        setStatus(AsyncStatus.Error);
                    });
            }
        }, [path, cppwApi]);
        useEffect(() => {
            if (status === AsyncStatus.Initial) {
                onRetry();
            }
        }, [status, onRetry]);
        useEffect(() => {
            const value =
                selectedValue && selectedValue.type === 'placeholder' ? undefined : selectedValue;
            inputRef && inputRef({value});
        }, [inputRef, selectedValue]);

        return (
            <LoadableView
                status={status}
                errorSubtitle='Список вариантов не загрузился'
                onRetry={onRetry}
            >
                <MentorSuggest
                    itemPredicate={filterMentor}
                    itemRenderer={renderItem}
                    items={vars}
                    closeOnSelect={true}
                    fill={true}
                    disabled={disabled}
                    defaultSelectedItem={savedValue || defaultValue}
                    inputValueRenderer={renderInputValue}
                    itemsEqual={areFilmsEqual}
                    noResults={<MenuItem disabled={true} text='Ничего не найдено.' />}
                    onItemSelect={onSelect}
                    popoverProps={{
                        minimal: true,
                        position: 'bottom',
                        popoverClassName: 'form-input-mentor',
                    }}
                    inputProps={{placeholder: placeholder || 'Начните вводить ФИО...'}}
                />
            </LoadableView>
        );
    }
);
