import {Icon} from '@blueprintjs/core';
import React, {FC, memo, useCallback, useEffect, useMemo, useState} from 'react';
import {editChat} from 'src/store/actions/chats.actions';
import {useCppwApiContext} from 'src/ui/contexts/cppw-api-context/cppw-api-context';
import './chat-card.styl';

type Props = {
    title: string;
    onClick: () => void;
    chatIndex: number;
    isActive: boolean;
    isEditing: boolean;
    id: string;
};

export const ChatCard: FC<Props> = memo(
    ({title, onClick, isActive, isEditing, chatIndex, id: chatId}) => {
        const cppwApi = useCppwApiContext();
        const [isMenuOpen, setIsMenuOpen] = useState(false);
        const onIconClick = useCallback(() => {
            setIsMenuOpen(!isMenuOpen);
        }, [isMenuOpen]);
        const options: {name: string; onClick: () => void}[] = useMemo(
            () => [
                {
                    name: 'Редактировать',
                    onClick: () => {
                        cppwApi && cppwApi.store.dispatch(editChat(chatIndex, {isEditing: true}));
                    },
                },
                {
                    name: 'Удалить',
                    onClick: () => {
                        cppwApi && cppwApi.deleteChat(chatIndex, chatId);
                    },
                },
            ],
            [cppwApi, chatIndex, chatId]
        );

        useEffect(() => {
            if (!isActive) {
                setIsMenuOpen(false);
            }
        }, [isActive]);

        return (
            <>
                <div
                    className={`chat-card ${isActive ? '_is-active' : ''} ${
                        isEditing ? '_is-editing' : ''
                    }`}
                    onClick={onClick}
                >
                    <div className='chat-card__text'> {title}</div>
                    <Icon
                        icon='chevron-down'
                        className='chat-card__right-icon'
                        onClick={onIconClick}
                    />
                </div>
                <div className={`chat-card__menu ${isMenuOpen ? '_opened' : ''}`}>
                    {options.map(({name, onClick}) => (
                        <div
                            key={`${name} ${title}`}
                            className='chat-card__menu-item'
                            onClick={onClick}
                        >
                            {name}
                        </div>
                    ))}
                </div>
            </>
        );
    }
);
