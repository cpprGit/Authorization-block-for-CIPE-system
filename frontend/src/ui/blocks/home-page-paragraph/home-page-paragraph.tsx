import {H1, H2, H3} from '@blueprintjs/core';

import React, {FC, memo} from 'react';
import './home-page-heading.styl';

type Descriptor = {
    text: string;
    link?: string;
};
type Props = {
    titleClassName?: string;
    title: string;
    description?: string | Descriptor[];
    mode: number;
};

export const HomePageParagraph: FC<Props> = memo(({title, description, mode, titleClassName}) => {
    return (
        <>
            {mode === 1 && (
                <H1 className={`home-page-heading__title ${titleClassName}`}>{title}</H1>
            )}
            {mode === 2 && (
                <H2 className={`home-page-heading__title ${titleClassName}`}>{title}</H2>
            )}
            {mode === 3 && (
                <H3 className={`home-page-heading__title ${titleClassName}`}>{title}</H3>
            )}
            {description && (
                <p className='home-page-heading__description'>
                    {Array.isArray(description)
                        ? description.map(({text, link}) =>
                              link ? (
                                  <a target='_blank' href={link} rel='noopener noreferrer'>
                                      {text}
                                  </a>
                              ) : (
                                  text
                              )
                          )
                        : description}
                </p>
            )}
        </>
    );
});
