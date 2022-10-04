import {AUTH0_PARAMS_URL} from 'src/ui/utils/constants';

export const parseJWT = (token: string) => {
    try {
        const base64Url = token.split('.')[1];
        const base64 = base64Url.replace(/-/g, '+').replace(/_/g, '/');
        const jsonPayload = decodeURIComponent(
            atob(base64)
                .split('')
                .map(function (c) {
                    return '%' + ('00' + c.charCodeAt(0).toString(16)).slice(-2);
                })
                .join('')
        );

        const res = JSON.parse(jsonPayload);
        return {
            userId: res[`user_id`],
            role: res[`role`],
            name: res[`name`] || 'Пользователь',
            exp: res.exp,
            email: res.email,
            email_verified: res.email_verified,
            token,
        };
    } catch (e) {
        console.error('Parse jwt error: ', e, token);
        return null;
    }
};
