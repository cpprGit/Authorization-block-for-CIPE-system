import {Icon, Intent} from '@blueprintjs/core';
import React, {FC, memo, useCallback} from 'react';
import {Link} from 'react-router-dom';
import {Attribute, ProfileOrSearchItem} from 'src/types';
import './chat-user-card.styl';

type SimpleProps = {
    userName: string;
    userId: string;
    handleAdd?: () => void;
    handleDelete?: () => void;
    isAdded?: boolean;
};
export const SimpleChatUserCard: FC<SimpleProps> = memo(
    ({userName, userId, isAdded, handleAdd, handleDelete}) => {
        const onIconClick = useCallback(() => {
            if (isAdded) {
                handleDelete && handleDelete();
            } else {
                handleAdd && handleAdd();
            }
        }, [isAdded, handleAdd, handleDelete]);

        return (
            <div className='chat-user-card'>
                {handleAdd && handleDelete && (
                    <Icon
                        className='chat-user-card__icon'
                        icon={isAdded ? 'small-tick' : 'small-plus'}
                        intent={isAdded ? Intent.SUCCESS : Intent.DANGER}
                        onClick={onIconClick}
                    />
                )}
                <div className='chat-user-card__texts'>
                    <Link className='chat-user-card__main' to={`/user/${userId}`}>
                        {userName}
                    </Link>
                </div>
            </div>
        );
    }
);

type Props = {
    user: ProfileOrSearchItem;
    attributes: Attribute[];
    handleAdd?: () => void;
    handleDelete?: () => void;
    isAdded?: boolean;
};
export const ChatUserCard: FC<Props> = memo(
    ({user, attributes, isAdded, handleAdd, handleDelete}) => {
        const onIconClick = useCallback(() => {
            if (isAdded) {
                handleDelete && handleDelete();
            } else {
                handleAdd && handleAdd();
            }
        }, [isAdded, handleAdd, handleDelete]);

        return (
            <div className='chat-user-card'>
                {handleAdd && handleDelete && (
                    <Icon
                        className='chat-user-card__icon'
                        icon={isAdded ? 'small-tick' : 'small-plus'}
                        intent={isAdded ? Intent.SUCCESS : Intent.DANGER}
                        onClick={onIconClick}
                    />
                )}
                <div className='chat-user-card__texts'>
                    <Link className='chat-user-card__main' to={`/user/${user.id}`}>
                        {user.name}
                    </Link>
                    {attributes.map(
                        ({realName, title}: Attribute) =>
                            realName !== 'name' &&
                            realName && (
                                <div className='chat-user-card__minor'>{`${title}: ${user[realName]}`}</div>
                            )
                    )}
                </div>
            </div>
        );
    }
);
