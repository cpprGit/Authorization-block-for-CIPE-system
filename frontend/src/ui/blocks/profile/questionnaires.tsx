import React, {FC, memo, useEffect, useState} from 'react';
import {AsyncStatus} from 'src/types';
import {FormCard2} from 'src/ui/blocks/form-card/form-card';
import {LoadableView} from 'src/ui/blocks/loadable-view/loadable-view';
import {useCppwApiContext} from 'src/ui/contexts/cppw-api-context/cppw-api-context';

export const Questionnaires: FC<{id: string}> = memo(({id}) => {
    const cppwApi = useCppwApiContext();

    const [questionnaires, setQuestionnaires] = useState<any[]>([]);
    const [status, setStatus] = useState(AsyncStatus.Initial);

    useEffect(() => {
        if (cppwApi && status === AsyncStatus.Initial) {
            cppwApi &&
                cppwApi.getAllUsersQuestionnaires(
                    id,
                    () => {
                        setStatus(AsyncStatus.Pending);
                    },
                    (questionnaires) => {
                        setQuestionnaires(questionnaires);
                        setStatus(AsyncStatus.Success);
                    },
                    () => {
                        setStatus(AsyncStatus.Error);
                    }
                );
        }
        // eslint-disable-next-line react-hooks/exhaustive-deps
    }, [cppwApi, setStatus]);

    return (
        <LoadableView
            status={status}
            errorTitle='Ошибка загрузки опросников.'
            spinnerClassName='spinner-full-height'
        >
            {questionnaires.map((notification) => (
                <FormCard2 key={notification.id} {...notification} />
            ))}
            {!questionnaires.length && (
                <div className='profile-lists__no-items'>Опросников пока что нет</div>
            )}
        </LoadableView>
    );
});
