import {Classes} from '@blueprintjs/core';
import React, {FC, memo, useState} from 'react';
import {Admin} from 'src/ui/blocks/admin/admin';
import {Layout} from 'src/ui/blocks/layout/layout';
import {Tab} from 'src/ui/blocks/tab/tab';

const linksList = [
    {title: 'Создать активность'},
    {title: 'Создать проект'},
    {title: 'Создать организацию'},
    {title: 'Зарегистрировать пользователя'},
    {title: 'Начать учебный год'},
    {title: 'Актуализировать контакты пользователей'},
    {title: 'Редактировать активные студенческие группы'},
    {title: 'Добавить студента к активности'},
    {title: 'Изменить статус студента'},
];

export const AdminPage: FC = memo(() => {
    const [selectedTab, setSelectedTab] = useState(0);
    const leftComponent = (
        <div className={Classes.FILL}>
            {linksList.map(({title}, index) => (
                <Tab
                    key={index}
                    isActive={index === selectedTab}
                    title={title}
                    onClick={() => {
                        setSelectedTab(index);
                    }}
                />
            ))}
        </div>
    );
    return (
        <Layout
            rightComponent={<Admin selectedTab={selectedTab} />}
            leftComponent={leftComponent}
        />
    );
});
