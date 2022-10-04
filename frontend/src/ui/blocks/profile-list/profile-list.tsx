import React, {FC, memo, useCallback, useEffect} from 'react';
import {ApiResponse, AsyncStatus, ProfileOrSearchItem} from 'src/types';
import {LoadableView} from 'src/ui/blocks/loadable-view/loadable-view';
import {useCppwApiContext} from 'src/ui/contexts/cppw-api-context/cppw-api-context';

type Props = {
    status: AsyncStatus;
    items: ProfileOrSearchItem[];
    editCurrentList: (list: {items: ProfileOrSearchItem[]; status: AsyncStatus}) => void;
    component: FC<ProfileOrSearchItem>;
    path: string;
    profileId: string;
    className: string;
    modifyAllowed?: boolean;
};
export const ProfileList: FC<Props> = memo(
    ({
        status,
        editCurrentList,
        items,
        component: Component,
        path,
        profileId,
        className,
        modifyAllowed,
    }) => {
        const cppwApi = useCppwApiContext();
        const editItem = useCallback(
            (itemIndex: number, newInfo: ProfileOrSearchItem) => {
                const res = items.map((item, index) =>
                    itemIndex === index ? (newInfo ? {...item, ...newInfo} : newInfo) : item
                );
                editCurrentList({
                    status: AsyncStatus.Success,
                    items: res,
                });
            },
            [items, editCurrentList]
        );

        useEffect(() => {
            // Перезапрашиваем данные, если необходимо
            if (cppwApi && status === AsyncStatus.Initial && path in cppwApi) {
                // @ts-ignore
                cppwApi[path](
                    profileId,
                    () => {
                        editCurrentList({status: AsyncStatus.Pending, items: []});
                    },
                    (res: ApiResponse[]) => {
                        editCurrentList({
                            status: AsyncStatus.Success,
                            items: res,
                        });
                    },
                    () => {
                        editCurrentList({status: AsyncStatus.Error, items: []});
                    }
                );
            }
        }, [editCurrentList, status, profileId, cppwApi, path]);

        return (
            <LoadableView
                status={status}
                errorTitle={'Ошибка загрузки списка'}
                spinnerClassName='spinner-full-height'
            >
                <div className={className}>
                    {items &&
                        items.map(
                            (item, index) =>
                                item && (
                                    <Component
                                        key={index}
                                        {...item}
                                        modifyAllowed={modifyAllowed}
                                        editItem={editItem}
                                        itemIndex={index}
                                    />
                                )
                        )}
                    {(!items || !items.filter((item) => !!item).length) && (
                        <div className='profile-lists__no-items'>Список пока что пуст</div>
                    )}
                </div>
            </LoadableView>
        );
    }
);
