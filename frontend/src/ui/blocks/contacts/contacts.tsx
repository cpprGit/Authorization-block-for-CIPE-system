import React, {FC, memo} from 'react';
import {HomePageParagraph} from 'src/ui/blocks/home-page-paragraph/home-page-paragraph';

const info = [
    {
        title: 'Контакты',
        mode: 1,
        description: '',
    },
    {
        title: 'Адрес',
        mode: 3,
        description: '109028, г. Москва, Покровский бульвар, д. 11',
    },
    {
        title: 'E-mail',
        mode: 3,
        description: 'cppr.cs@hse.ru',
    },
    {
        title: 'Руководство',
        mode: 3,
        description: '',
    },
];

const contacts = [
    {
        position: 'Руководитель центра',
        name: 'Ахметсафина Римма Закиевна',
        link: 'https://www.hse.ru/staff/rakhmetsafina',
    },
    {
        position: 'Менеджер центра',
        name: 'Ченцова Элина Александровна',
        link: 'https://www.hse.ru/staff/elinachentsova',
    },
    {
        position: 'Менеджер по работе со студентами ',
        name: 'Ге́тун Юлия Александровна',
        link: 'https://www.hse.ru/org/persons/325138530',
    },
];

export const Contacts: FC = memo(() => {
    return (
        <>
            {info.map((elem, ind) => {
                return <HomePageParagraph key={ind} {...elem} />;
            })}
            {contacts.map(({position, name, link}, ind) => (
                <p key={ind} className='home-page-heading__description'>
                    {position} -{' '}
                    <a href={link} target='blank' rel='noopener noreferrer'>
                        {name}
                    </a>
                </p>
            ))}
        </>
    );
});
