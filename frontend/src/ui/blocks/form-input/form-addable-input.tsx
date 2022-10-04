import {Card, EditableText, Icon, Intent} from '@blueprintjs/core';
import React, {FC, memo, useCallback, useEffect, useState} from 'react';

type Props = {
    intent: Intent;
    inputRef: (ref: {value: string[]} | null) => void;
    savedValue?: string[];
    defaultValue?: string[];
};

export const AddableInputView: FC<Props> = memo(
    ({intent, inputRef, savedValue, defaultValue = []}) => {
        const [variants, seVariants] = useState(savedValue || defaultValue);
        const addVariant = useCallback(() => seVariants([...variants, 'Новый вариант']), [
            seVariants,
            variants,
        ]);
        const deleteVariant = useCallback(
            (index: number) => {
                const newVariants = variants.filter((val: string, ind: number) => ind !== index);
                seVariants(newVariants);
            },
            [seVariants, variants]
        );

        useEffect(() => {
            inputRef && inputRef({value: variants});
        }, [inputRef, variants]);
        return (
            <>
                {variants.map((variant: string, index: number) => (
                    <>
                        <EditableText
                            key={variant}
                            className={'addable-input__text'}
                            onChange={(value) => {
                                variants[index] = value;
                            }}
                            defaultValue={variant}
                            minWidth={100}
                            placeholder='Вариант ответа...'
                        />
                        <Icon
                            className='addable-input__cross'
                            icon='cross'
                            key={`${variant}__cross`}
                            onClick={() => {
                                deleteVariant(index);
                            }}
                        />
                    </>
                ))}
                <Card className='addable-input__plus' onClick={addVariant}>
                    <Icon icon='plus' />
                </Card>
            </>
        );
    }
);
