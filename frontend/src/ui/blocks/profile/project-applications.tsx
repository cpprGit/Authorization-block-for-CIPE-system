import React, {FC, memo, useCallback, useEffect, useState} from 'react';
import {AsyncStatus, ProfileOrSearchItem} from 'src/types';
import {LoadableView} from 'src/ui/blocks/loadable-view/loadable-view';
import {useCppwApiContext} from 'src/ui/contexts/cppw-api-context/cppw-api-context';

export const ProjectApplicationsList: FC<{
    id: string;
    path: string;
    component: any;
}> = memo(({id, path, component: Component}) => {
    const cppwApi = useCppwApiContext();

    const [items, setItems] = useState<any[]>([]);
    const editItem = useCallback(
        (itemIndex: number, newInfo: ProfileOrSearchItem) => {
            const res = items.map((item, index) =>
                itemIndex === index ? (newInfo ? {...item, ...newInfo} : newInfo) : item
            );
            setItems(res);
        },
        [items]
    );
    const [status, setStatus] = useState(AsyncStatus.Initial);

    useEffect(() => {
        if (cppwApi && status === AsyncStatus.Initial && path in cppwApi) {
            // @ts-ignore
            cppwApi[path](
                id,
                () => {
                    setStatus(AsyncStatus.Pending);
                },
                (items: any) => {
                    setItems(items);
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
            errorTitle='Ошибка загрузки.'
            spinnerClassName='spinner-full-height'
        >
            {items.map((item, index) => (
                <Component key={index} {...item} editItem={editItem} itemIndex={index} />
            ))}
            {!items.length && <div className='profile-lists__no-items'>Список пока что пуст</div>}
        </LoadableView>
    );
});
