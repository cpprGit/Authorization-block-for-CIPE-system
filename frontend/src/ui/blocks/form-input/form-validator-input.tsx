import {Card, EditableText, Icon, Intent} from '@blueprintjs/core';
import React, {FC, memo, useCallback, useEffect, useState} from 'react';
import {Validator} from 'src/types';

type Props = {
    intent: Intent;
    inputRef: (ref: {value: Validator[]} | null) => void;
    savedValue?: Validator[];
    defaultValue?: Validator[];
};

export const ValidatorInputView: FC<Props> = memo(
    ({intent, inputRef, savedValue, defaultValue = []}) => {
        const [variants, seVariants] = useState(savedValue || defaultValue);
        const addVariant = useCallback(() => seVariants([...variants, {regexp: '', message: ''}]), [
            seVariants,
            variants,
        ]);
        const deleteVariant = useCallback(
            (index: number) => {
                const newVariants = variants.filter((val: Validator, ind: number) => ind !== index);
                seVariants(newVariants);
            },
            [seVariants, variants]
        );

        useEffect(() => {
            inputRef && inputRef({value: variants});
        }, [inputRef, variants]);
        return (
            <>
                {variants.map(({regexp, message}: Validator, index: number) => (
                    <div className={'validator-input'} key={index}>
                        <div className={'validator-input__text'}>
                            <EditableText
                                onChange={(value) => {
                                    variants[index].regexp = value;
                                }}
                                defaultValue={String(regexp)}
                                minWidth={100}
                                placeholder='Регулярное вырожение...'
                            />
                            <EditableText
                                onChange={(value) => {
                                    variants[index].message = value;
                                }}
                                defaultValue={message}
                                minWidth={100}
                                placeholder='Сообщение об ошибке...'
                            />
                        </div>
                        <Icon
                            className='validator-input__cross'
                            icon='cross'
                            key={`${message}__cross`}
                            onClick={() => {
                                deleteVariant(index);
                            }}
                        />
                    </div>
                ))}
                <Card className='addable-input__plus' onClick={addVariant}>
                    <Icon icon='plus' />
                </Card>
            </>
        );
    }
);
