import {TOKEN_PARAM_NAME} from 'src/api/api';
import {UserRole} from 'src/types';
import {deleteCookie} from 'src/ui/utils/cookie';

export const clearAuthCookie = () => {
    deleteCookie(TOKEN_PARAM_NAME);
};

export const redirect = () => {
    if (window.location.href !== '/') {
        window.location.href = '/';
    }
};

const BASE_PATHNAMES = ['', 'for_partners', 'for_students', 'for_workers', 'authorization', 'help', 'contacts'];
const AUTHED_PATHNAMES = [
    'user',
    'project',
    'project_request',
    'activity',
    'organisation',
    'search',
];
const MENTOR_PATHNAMES = ['new_project_request'];
const MANAGER_PATHNAMES = ['chat', 'forms', 'admin'];
export const checkURLPermission = (pathname: string, userRole: UserRole) => {
    const [, origin] = pathname.split('/');
    if (typeof origin !== 'string') {
        redirect();
    }
    switch (userRole) {
        case UserRole.Supervisor:
        case UserRole.Manager: {
            if (
                !BASE_PATHNAMES.includes(origin) &&
                !AUTHED_PATHNAMES.includes(origin) &&
                !MENTOR_PATHNAMES.includes(origin) &&
                !MANAGER_PATHNAMES.includes(origin)
            ) {
                redirect();
            }
            return;
        }
        case UserRole.AcademicManager:
        case UserRole.Representative:
        case UserRole.Mentor: {
            if (
                !BASE_PATHNAMES.includes(origin) &&
                !AUTHED_PATHNAMES.includes(origin) &&
                !MENTOR_PATHNAMES.includes(origin)
            ) {
                redirect();
            }
            return;
        }
        case UserRole.OfficeManager:
        case UserRole.Student: {
            if (!BASE_PATHNAMES.includes(origin) && !AUTHED_PATHNAMES.includes(origin)) {
                redirect();
            }
            return;
        }
        case UserRole.Initial:
        default: {
            if (!BASE_PATHNAMES.includes(origin)) {
                clearAuthCookie();
                redirect();
            }
        }
    }
};
