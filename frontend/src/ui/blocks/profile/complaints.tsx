import {Icon} from '@blueprintjs/core';
import React, {FC, memo, useCallback, useEffect, useState} from 'react';
import {Link} from 'react-router-dom';
import {AsyncStatus, CppwNotification} from 'src/types';
import {LoadableView} from 'src/ui/blocks/loadable-view/loadable-view';
import {useCppwApiContext} from 'src/ui/contexts/cppw-api-context/cppw-api-context';

const ComplaintView: FC<CppwNotification> = memo(
    ({id, from, message, about, read: initialReadState}) => {
        const cppwApi = useCppwApiContext();
        const [read, setRead] = useState(initialReadState);
        const onRead = useCallback(() => {
            cppwApi &&
                cppwApi.setComplaintSeen(id, () => {
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
                    <Link to={`/${from.type}/${from.id}`}>{from.name}</Link> {message}{' '}
                    {about && <Link to={`/${about.type}/${about.id}`}>{about.name}</Link>}
                </div>
            </div>
        );
    }
);

export const Complaints: FC = memo(() => {
    const cppwApi = useCppwApiContext();

    const [notifications, setNotifications] = useState<CppwNotification[]>([]);
    const [status, setStatus] = useState(AsyncStatus.Initial);

    useEffect(() => {
        if (cppwApi && status === AsyncStatus.Initial) {
            cppwApi &&
                cppwApi.getAllComplaints(
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
            errorTitle='Ошибка загрузки жалоб.'
            spinnerClassName='spinner-full-height'
        >
            {notifications.map((notification) => (
                <ComplaintView key={notification.id} {...notification} />
            ))}
            {(!notifications || !notifications.length) && (
                <div className='profile-lists__no-items'>Жалоб нет</div>
            )}
        </LoadableView>
    );
});
