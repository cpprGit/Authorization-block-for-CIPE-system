import {Classes} from '@blueprintjs/core';
import React, {FC, memo, useMemo} from 'react';
import {Contacts} from 'src/ui/blocks/contacts/contacts';
import {Help} from 'src/ui/blocks/default-form-view/help';
import {UsersRegistration} from "../../blocks/default-form-view/users-registration";
import {StudentsRegistration} from 'src/ui/blocks/default-form-view/students-registration';
import {Empty} from 'src/ui/blocks/empty/empty';
import {Home} from 'src/ui/blocks/home/home';
import {Layout} from 'src/ui/blocks/layout/layout';
import {Partners} from 'src/ui/blocks/partners/partners';
import {Tab} from 'src/ui/blocks/tab/tab';
import {UsersAuthorization} from "../../blocks/default-form-view/users-authorization";
import {UsersRegistrationPage} from "../../blocks/user-registration-page/user-registration-page";

export enum HomePageLocations {
    Main = '/',
    ForPartners = '/for_partners',
    ForStudents = '/for_students',
    ForWorkers = '/for_workers',
    Authorization = '/authorization',
    Contacts = '/contacts',
    Help = '/help',
}

const linksList = [
    {title: 'О Центре', href: HomePageLocations.Main},
    {title: 'Компаниям-партнерам', href: HomePageLocations.ForPartners},
    {title: 'Студентам', href: HomePageLocations.ForStudents},
    {title: 'Работникам и менторам', href: HomePageLocations.ForWorkers},
    {title: 'Контакты', href: HomePageLocations.Contacts},
    {title: 'Помощь', href: HomePageLocations.Help},
];

const homePageLocationsToRightComponentsMap: {[key: string]: FC<{}>} = {
    [HomePageLocations.Main]: Home,
    [HomePageLocations.ForPartners]: Partners,
    [HomePageLocations.ForStudents]: StudentsRegistration,
    [HomePageLocations.ForWorkers]: UsersRegistrationPage,
    [HomePageLocations.Authorization]: UsersAuthorization,
    [HomePageLocations.Contacts]: Contacts,
    [HomePageLocations.Help]: Help,
    [`${HomePageLocations.ForPartners}/`]: Partners,
    [`${HomePageLocations.ForStudents}/`]: StudentsRegistration,
    [`${HomePageLocations.ForWorkers}/`]: UsersRegistrationPage,
    [`${HomePageLocations.Authorization}/`]: UsersAuthorization,
    [`${HomePageLocations.Contacts}/`]: Contacts,
    [`${HomePageLocations.Help}/`]: Help,
};
const homePageLocationsToIndexMap: {[key: string]: number} = {
    [HomePageLocations.Main]: 0,
    [HomePageLocations.ForPartners]: 1,
    [HomePageLocations.ForStudents]: 2,
    [HomePageLocations.ForWorkers]: 3,
    [HomePageLocations.Contacts]: 4,
    [HomePageLocations.Help]: 5,
    [`${HomePageLocations.ForPartners}/`]: 1,
    [`${HomePageLocations.ForStudents}/`]: 2,
    [HomePageLocations.ForWorkers]: 3,
    [`${HomePageLocations.Contacts}/`]: 4,
    [`${HomePageLocations.Help}/`]: 5,
};

export const HomePage: FC = memo(() => {
    const RightComponent = useMemo(
        () => homePageLocationsToRightComponentsMap[window.location.pathname] || Empty,
        // eslint-disable-next-line react-hooks/exhaustive-deps
        [window.location.pathname]
    );
    const selectedTabId =
        window.location.pathname in homePageLocationsToIndexMap
            ? homePageLocationsToIndexMap[window.location.pathname]
            : -1;

    const leftComponent = (
        <div className={Classes.FILL}>
            {linksList.map(({title, href}, index) => (
                <Tab key={index} isActive={index === selectedTabId} link={href} title={title} />
            ))}
        </div>
    );
    return <Layout leftComponent={leftComponent} rightComponent={<RightComponent />} />;
});
