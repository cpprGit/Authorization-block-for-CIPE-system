import {Intent} from '@blueprintjs/core';
import React, {FC, memo, useCallback, useEffect, useState} from 'react';
import {ApiResponse, AsyncStatus} from 'src/types';
import {LoadableView} from 'src/ui/blocks/loadable-view/loadable-view';

type Props = {
    intent: Intent;
    inputRef: (ref: HTMLSelectElement | null) => void;
    savedValue?: string;
    defaultValue?: string | {id: string; name: string; type: string};
    requestVariants?: () => Promise<ApiResponse>;
    disabled?: boolean;
    variants?: string[];
};

export const SelectInputView: FC<Props> = memo(
    ({variants = [], inputRef, intent, requestVariants, disabled, savedValue, defaultValue}) => {
        const [vars, setVars] = useState(variants);
        const [status, setStatus] = useState(AsyncStatus.Initial);
        const onRetry = useCallback(() => {
            if (requestVariants) {
                setStatus(AsyncStatus.Pending);
                requestVariants()
                    .then((res: ApiResponse) => {
                        setVars(res.map(({name}: ApiResponse) => name));
                        setStatus(AsyncStatus.Success);
                    })
                    .catch(() => {
                        setStatus(AsyncStatus.Error);
                    });
            }
        }, [requestVariants, setStatus]);
        useEffect(() => {
            if (!requestVariants) {
                if (variants.length) {
                    setStatus(AsyncStatus.Success);
                } else {
                    setStatus(AsyncStatus.Error);
                }
            }
            if (requestVariants && status === AsyncStatus.Initial) {
                setStatus(AsyncStatus.Pending);
                requestVariants()
                    .then((res: ApiResponse) => {
                        setVars(res.map(({name}: ApiResponse) => name));
                        setStatus(AsyncStatus.Success);
                    })
                    .catch(() => {
                        setStatus(AsyncStatus.Error);
                    });
            }
            // eslint-disable-next-line react-hooks/exhaustive-deps
        }, [requestVariants, setStatus]);
        return (
            <LoadableView
                status={status}
                errorSubtitle={'Активности не загрузились'}
                onRetry={onRetry}
            >
                <div className='bp3-select bp3-fill'>
                    <select
                        ref={inputRef}
                        disabled={disabled}
                        defaultValue={
                            savedValue ||
                            (defaultValue &&
                            typeof defaultValue === 'object' &&
                            'name' in defaultValue
                                ? defaultValue.name
                                : defaultValue)
                        }
                    >
                        <option value={''}>...</option>
                        {vars &&
                            vars.map((value: string, ind: number) => (
                                <option key={ind} value={value}>
                                    {value}
                                </option>
                            ))}
                    </select>
                </div>
            </LoadableView>
        );
    }
);
