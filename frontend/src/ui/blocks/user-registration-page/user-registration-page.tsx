import React, {FC, memo, useCallback} from 'react';
import {FormType, UserRole} from 'src/types';
import {DefaultFormView} from 'src/ui/blocks/default-form-view/default-form-view';
import {useCppwApiContext} from 'src/ui/contexts/cppw-api-context/cppw-api-context';
import {HomePageParagraph} from "../home-page-paragraph/home-page-paragraph";

const info =[
    {
        title: 'Регистрация работника/ментора проекта',
        mode: 1,
        description: 'Для регистрации в качестве работника или ментора необходимо заполнить форму.',
    }
]
export const UsersRegistrationPage: FC = memo(() => {
    return (
        <>
            {info.map((elem, ind) => {
                return <HomePageParagraph key={ind} {...elem} />;
            })}
            <p className='home-page-heading__description'>
                <a href={'https://forms.yandex.ru/cloud/6329c4174c68bb5b7e152163/'} target='blank' rel='noopener noreferrer'>
                    https://forms.yandex.ru/cloud/6329c4174c68bb5b7e152163/
                </a>
            </p>
        </>
    );
});
