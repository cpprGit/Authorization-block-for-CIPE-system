// возвращает куки с указанным name,
// или undefined, если ничего не найдено
export function getCookie(name: string) {
    let matches = document.cookie.match(
        new RegExp(
            // eslint-disable-next-line no-useless-escape
            '(?:^|; )' + name.replace(/([\.$?*|{}\(\)\[\]\\\/\+^])/g, '\\$1') + '=([^;]*)'
        )
    );
    return matches ? decodeURIComponent(matches[1]) : undefined;
}

export function setCookie(name: string, value: string, options: any = {}) {
    options = {
        path: '/',
        samesite: 'strict',
        expires: new Date(Date.now() + 1000 * 60 * 60 * 24 * 7),
        ...options,
    };

    if (options.expires && typeof options.expires !== 'string' && options.expires.toUTCString) {
        options.expires = options.expires.toUTCString();
    }

    const updatedCookie = Object.keys(options).reduce((sum: string, optionKey) => {
        let optionValue = options[optionKey];
        let update = '';
        if (optionValue) {
            update += '; ' + optionKey;
            if (optionValue !== true) {
                update += '=' + optionValue;
            }
            return sum + update;
        }
        return sum;
    }, encodeURIComponent(name) + '=' + encodeURIComponent(value));

    document.cookie = updatedCookie;
}

export function deleteCookie(name: string) {
    setCookie(name, '', {
        expires: -1,
    });
}
