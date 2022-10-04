import {Button, ButtonGroup, Card, H3, Intent} from '@blueprintjs/core';
import React, {FC, memo, useCallback, useEffect, useMemo, useRef, useState} from 'react';
import {HistoryLog} from 'src/store/reducers/chats.reducer';
import {AsyncStatus, Usage} from 'src/types';
import {FormCardLite} from 'src/ui/blocks/form-card/form-card';
import {FormInput} from 'src/ui/blocks/form-input/form-input';
import {LoadableView} from 'src/ui/blocks/loadable-view/loadable-view';
import {Search} from 'src/ui/blocks/search/search';
import {useCppwApiContext} from 'src/ui/contexts/cppw-api-context/cppw-api-context';
import './chat-history.styl';

type Props = {
    chatId: string;
    chatIndex: number;
    logs: HistoryLog[];
    status: AsyncStatus;
    mailTo: string;
};
export const ChatHistory: FC<Props> = memo(({chatId, chatIndex, status, logs, mailTo}) => {
    const cppwApi = useCppwApiContext();
    const [isQuestionnaireSenderOpen, setIsQuestionnaireSenderOpen] = useState(false);
    const [isNotificationSenderOpen, setIsNotificationSenderOpen] = useState(false);
    const onNotificationSenderOpen = useCallback(() => {
        setIsNotificationSenderOpen(true);
    }, [setIsNotificationSenderOpen]);
    const onNotificationSenderClose = useCallback(
        (text: string) => {
            if (text) {
                cppwApi &&
                    cppwApi.sendNotification(chatId, chatIndex, text, () => {
                        setIsNotificationSenderOpen(false);
                    });
            } else {
                setIsNotificationSenderOpen(false);
            }
        },
        [chatIndex, chatId, cppwApi, setIsNotificationSenderOpen]
    );
    const onQuestionnaireSenderOpen = useCallback(() => {
        setIsQuestionnaireSenderOpen(true);
    }, [setIsQuestionnaireSenderOpen]);
    const onQuestionnaireSenderClose = useCallback(() => {
        setIsQuestionnaireSenderOpen(false);
    }, [setIsQuestionnaireSenderOpen]);
    const onCopyText = useCallback(() => {
        const copytext = document.createElement('input');
        copytext.value = mailTo;
        document.body.appendChild(copytext);
        copytext.select();
        document.execCommand('copy');
        document.body.removeChild(copytext);
    }, [mailTo]);

    useEffect(() => {
        if (status === AsyncStatus.Initial) {
            cppwApi && cppwApi.requestChatsHistoryLog(chatId, chatIndex);
        }
        // eslint-disable-next-line react-hooks/exhaustive-deps
    }, [cppwApi, chatId, chatIndex]);
    return (
        <>
            <ButtonGroup className='chat-history__buttons'>
                {mailTo && (
                    <Button onClick={onCopyText}>
                        <a href={`mailto:${mailTo}`}>Написать сообщение</a>
                    </Button>
                )}
                <Button text='Отправить нотификацию' onClick={onNotificationSenderOpen} />
                <Button text='Отправить опрос' onClick={onQuestionnaireSenderOpen} />
            </ButtonGroup>
            <LoadableView status={status}>
                {isQuestionnaireSenderOpen && (
                    <QuestionnaireSender
                        chatId={chatId}
                        onClose={onQuestionnaireSenderClose}
                        chatIndex={chatIndex}
                    />
                )}
                {isNotificationSenderOpen && (
                    <NotificationSender onClose={onNotificationSenderClose} />
                )}
                <div className='chat-history__logs'>
                    {logs.map((log) => (
                        <HistoryLogView key={log.id} {...log} />
                    ))}
                </div>
            </LoadableView>
        </>
    );
});

const HistoryLogView: FC<HistoryLog> = memo(({date, link, title, name}) => {
    return (
        <div className='history-log'>
            <span className='history-log__date'>{date}</span>
            <span className='history-log__title'>
                {title}
                {link && name && (
                    <a
                        className='history-log__link'
                        href={link}
                        rel='noopener noreferrer'
                        target='_blank'
                    >
                        {name}
                    </a>
                )}
            </span>
        </div>
    );
});

type QuestionnaireSenderProps = {
    chatId: string;
    chatIndex: number;
    onClose: () => void;
};
const QuestionnaireSender: FC<QuestionnaireSenderProps> = memo(({chatId, chatIndex, onClose}) => {
    const cppwApi = useCppwApiContext();
    const [status, setStatus] = useState(AsyncStatus.Initial);
    const [forms, setForms] = useState<{id: string; name: string}[]>([]);
    const items = useMemo(
        () =>
            forms.map(({id, name}) => ({
                name,
                onClick: () => {
                    cppwApi && cppwApi.sendForm(chatIndex, chatId, id, name);
                },
            })),
        [forms, cppwApi, chatIndex, chatId]
    );

    useEffect(() => {
        if (status === AsyncStatus.Initial) {
            cppwApi &&
                cppwApi.getAllQuestionnaires(
                    () => setStatus(AsyncStatus.Pending),
                    (result) => {
                        setStatus(AsyncStatus.Success);
                        setForms(result);
                    },
                    () => setStatus(AsyncStatus.Error)
                );
        }
        // eslint-disable-next-line react-hooks/exhaustive-deps
    }, [cppwApi, setStatus, setForms]);

    return (
        <Card className='questionnaire_sender'>
            <H3 className='form-layout__title'>Выберите опрос для отправки</H3>
            <div className='questionnaire_sender__scrollable'>
                <LoadableView status={status}>
                    <Search
                        component={FormCardLite}
                        searchPropertyName='name'
                        items={items}
                        className='side-margin-search'
                    />
                </LoadableView>
            </div>
            <Button text='Готово' intent={Intent.PRIMARY} onClick={onClose} />
        </Card>
    );
});

type NotificationSenderProps = {
    onClose: (text: string) => void;
};
const NotificationSender: FC<NotificationSenderProps> = memo(({onClose}) => {
    const ref = useRef<HTMLInputElement | null>(null);
    const setRef = useCallback(
        (val) => {
            ref.current = val;
        },
        [ref]
    );
    const onClick = useCallback(() => onClose(ref.current ? ref.current.value : ''), [
        onClose,
        ref,
    ]);

    return (
        <Card className='questionnaire_sender'>
            <FormInput
                id={'NotificationSender'}
                name={'NotificationSender'}
                usage={Usage.LongText}
                title='Введите текст нотификации'
                mandatory={true}
                validators={[]}
                inputRef={setRef}
                formIndex={-1}
                index={-1}
            />
            <Button text='Готово' intent={Intent.PRIMARY} onClick={onClick} />
        </Card>
    );
});
