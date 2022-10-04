import React, {FC, memo, useEffect, useState} from 'react';
import {ProfileType} from 'src/types';
import {Layout} from 'src/ui/blocks/layout/layout';
import {Notifications} from 'src/ui/blocks/notifications/notifications';
import {ProfileColumn} from 'src/ui/blocks/profile-column/profile-column';
import {Complaints} from 'src/ui/blocks/profile/complaints';
import {Profile} from 'src/ui/blocks/profile/profile';
import {ProjectApplicationsList} from 'src/ui/blocks/profile/project-applications';
import {Questionnaires} from 'src/ui/blocks/profile/questionnaires';
import {
    ProjectApplicationCard,
    ProjectApplicationOwnerCard,
    ProjectApplicationOwnerCard2,
    ProjectApplicationOwnerCard3,
} from 'src/ui/blocks/project-application-card/project-application-card';
import {Vedomost} from 'src/ui/blocks/vedomost/vedomost';
import {HomePage} from 'src/ui/routes';

export const USER_PROFILE_PATH = '/user';
export const PROJECT_PROFILE_PATH = '/project';
export const PROJECT_REQUEST_PROFILE_PATH = '/project_request';
export const ACTIVITY_PROFILE_PATH = '/activity';
export const COMPANY_PROFILE_PATH = '/organisation';

export const PATH_TO_PROFILE_TYPE_MAP: {[k: string]: ProfileType} = {
    [USER_PROFILE_PATH]: ProfileType.User,
    [`${USER_PROFILE_PATH}/:id`]: ProfileType.User,
    [PROJECT_PROFILE_PATH]: ProfileType.Project,
    [`${PROJECT_PROFILE_PATH}/:id`]: ProfileType.Project,
    [PROJECT_REQUEST_PROFILE_PATH]: ProfileType.ProjectRequest,
    [`${PROJECT_REQUEST_PROFILE_PATH}/:id`]: ProfileType.ProjectRequest,
    [ACTIVITY_PROFILE_PATH]: ProfileType.Activity,
    [`${ACTIVITY_PROFILE_PATH}/:id`]: ProfileType.Activity,
    [COMPANY_PROFILE_PATH]: ProfileType.Organisation,
    [`${COMPANY_PROFILE_PATH}/:id`]: ProfileType.Organisation,
};

const getIdFromLocation = ({search}: {search: string}) => {
    if (!search) {
        return;
    }

    const searchParts = search.split('&');
    if (!searchParts.length) {
        return;
    }

    const searchPartsWithId = searchParts.filter((part) => part.includes('id='));
    if (!searchPartsWithId.length) {
        return;
    }

    for (let searchPartWithId of searchPartsWithId) {
        const id = searchPartWithId.replace('?', '').replace('id=', '');
        if (id.length === 36) {
            return id;
        }
    }
};
const getProfileRightComponent = (selectedTab: number, props: any) => {
    switch (selectedTab) {
        case 0:
            return <Profile {...props} />;
        case 1:
            return <Notifications />;
        case 2:
            return <Vedomost profileId={props.id} profileType={props.profileType} />;
        case 3:
            return <Complaints />;
        case 4:
            return (
                <ProjectApplicationsList
                    id={props.id}
                    path={'getAllUsersProjectRequests'}
                    component={ProjectApplicationOwnerCard3}
                />
            );
        case 5:
            return (
                <ProjectApplicationsList
                    id={props.id}
                    path={'getAllProjectsApplications'}
                    component={ProjectApplicationCard}
                />
            );
        case 6:
            return (
                <ProjectApplicationsList
                    id={props.id}
                    path={'getAllStudentsProjectApplications'}
                    component={ProjectApplicationOwnerCard}
                />
            );
        case 7:
            return (
                <ProjectApplicationsList
                    id={props.id}
                    path={'getAllActivitiesProjects'}
                    component={ProjectApplicationOwnerCard2}
                />
            );
        case 8:
            return <Questionnaires id={props.id} />;
        default:
            return <Profile {...props} />;
    }
};

type Props = {
    userId: string;
    match: {
        path: string;
        params: {
            id?: string;
        };
    };
    location: {
        search: string;
    };
};
export const ProfilePage: FC<Props> = memo(({match, location}) => {
    const {path} = match;
    const profileType = PATH_TO_PROFILE_TYPE_MAP[path];
    const id = match.params.id || getIdFromLocation(location);
    const [isEditing, setIsEditing] = useState(false);
    const [modifyAllowed, setModifyAllowed] = useState(false);
    const [isBlocked, setIsBlocked] = useState(false);
    const [selectedIndex, setSelectedIndex] = useState(0);

    useEffect(() => {
        setIsEditing(false);
        setSelectedIndex(0);

        if (match.params.id) {
            window.history.replaceState(
                null,
                'ЦППР',
                window.location.href.replace(`${id}`, `?id=${id}`)
            );
        }
        // Для избегания бесконечного ререндера.
        // eslint-disable-next-line react-hooks/exhaustive-deps
    }, [id]);

    if (!profileType || !id) {
        return <HomePage />;
    }
    return (
        <Layout
            rightComponent={getProfileRightComponent(selectedIndex, {
                id,
                modifyAllowed,
                setModifyAllowed,
                isEditing,
                setIsEditing,
                isBlocked,
                setIsBlocked,
                profileType,
            })}
            leftComponent={
                <ProfileColumn
                    profileType={profileType}
                    modifyAllowed={modifyAllowed}
                    selectedIndex={selectedIndex}
                    setSelectedIndex={setSelectedIndex}
                    setIsEditing={setIsEditing}
                    isBlocked={isBlocked}
                    setIsBlocked={setIsBlocked}
                    id={id}
                />
            }
        />
    );
});
