import {Icon} from '@blueprintjs/core';
import React, {FC, memo, useCallback, useEffect, useState} from 'react';
import {Link} from 'react-router-dom';
import {AsyncStatus, CppwNotification} from 'src/types';
import {LoadableView} from 'src/ui/blocks/loadable-view/loadable-view';
import {useCppwApiContext} from 'src/ui/contexts/cppw-api-context/cppw-api-context';
import './notifications.styl';

const NotificationView: FC<CppwNotification> = memo(
    ({id, from, message, about, read: initialReadState}) => {
        const cppwApi = useCppwApiContext();
        const [read, setRead] = useState(initialReadState);
        const onRead = useCallback(() => {
            cppwApi &&
                cppwApi.setNotificationSeen(id, () => {
                    setRead(true);
                });
        }, [id, cppwApi, setRead]);
        return (
            <div className={`notification ${read ? '_is-read' : ''}`}>
                <Icon
                    className='notification__icon'
                    icon={read ? 'eye-off' : 'eye-open'}
                    onClick={onRead}
                />
                <div className='notification__text'>
                    <Link to={`/${from.type}?id=${from.id}`}>{from.name}</Link> {message}{' '}
                    {about && <Link to={`/${about.type}?id=${about.id}`}>{about.name}</Link>}
                </div>
            </div>
        );
    }
);

export const Notifications: FC = memo(() => {
    const cppwApi = useCppwApiContext();

    const [notifications, setNotifications] = useState<CppwNotification[]>([]);
    const [status, setStatus] = useState(AsyncStatus.Initial);

    useEffect(() => {
        if (cppwApi && status === AsyncStatus.Initial) {
            cppwApi &&
                cppwApi.getAllNotifications(
                    () => {
                        setStatus(AsyncStatus.Pending);
                    },
                    (notifications) => {
                        setNotifications(notifications);
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
            errorTitle='Ошибка загрузки нотификаций.'
            spinnerClassName='spinner-full-height'
        >
            {notifications.map((notification) => (
                <NotificationView key={notification.id} {...notification} />
            ))}
            {(!notifications || !notifications.length) && (
                <div className='profile-lists__no-items'>Нотификаций нет</div>
            )}
        </LoadableView>
    );
});
