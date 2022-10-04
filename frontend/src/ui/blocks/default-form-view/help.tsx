import React, {FC, memo, useCallback} from 'react';

import {FormType} from 'src/types';
import {DefaultFormView} from 'src/ui/blocks/default-form-view/default-form-view';

export const Help: FC = memo(() => {
    const onSubmit = useCallback((res, callback) => {
        callback && callback();
    }, []);

    return (
        <DefaultFormView
            formType={FormType.Help}
            onSubmit={onSubmit}
            errorTitle='Ошибка загрузки формы жалоб и предложений.'
            successTitle='Спасибо, что делаете нас лучше!'
        />
    );
});
