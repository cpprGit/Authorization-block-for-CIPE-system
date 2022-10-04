import {Button, ButtonGroup, Icon, Intent} from '@blueprintjs/core';
import React, {FC, memo, useCallback, useMemo, useRef, useState} from 'react';
import {Link} from 'react-router-dom';
import {AttributeRef, Usage, UserAction} from 'src/types';
import {FormInput} from 'src/ui/blocks/form-input/form-input';
import {useCppwApiContext} from 'src/ui/contexts/cppw-api-context/cppw-api-context';
import {BACKEND_URL} from 'src/ui/utils/constants';
import './comment-view.styl';

type Props = {
    // id комментария, по которому будет осуществляться редактирование иудаление
    id: string;
    action: UserAction;
    text: string;
    user: {
        id: string;
        name: string;
        type: 'user';
    };
    file: {
        id: string;
        name: string;
        type: 'file';
    } | null;
    editItem: (itemIndex: number, newItem: any) => void;
    itemIndex: number;
    modifyAllowed?: boolean;
};
export const CommentView: FC<Props> = memo(
    ({id, action, user, text, file, editItem, itemIndex, modifyAllowed}) => {
        const cppwApi = useCppwApiContext();

        const [isEditing, setIsEditing] = useState(false);

        const commentRef = useRef<AttributeRef>(null);
        const fileRef = useRef<{
            value: {id: string; name: string; type: 'file'} | null;
        }>(null);

        const validators = useMemo(() => [], []);

        const setCommentRef = useCallback((val) => {
            commentRef.current = val;
        }, []);
        const setFileRef = useCallback((val) => {
            // @ts-ignore
            fileRef.current = val;
        }, []);
        const onDelete = useCallback(() => {
            cppwApi &&
                cppwApi.deleteComment(id, () => {
                    editItem(itemIndex, null);
                });
        }, [id, cppwApi, editItem, itemIndex]);
        const onStartEditing = useCallback(() => {
            setIsEditing(true);
        }, [setIsEditing]);
        const onFinishEditing = useCallback(() => {
            const newComment = {
                file: fileRef.current ? fileRef.current.value : undefined,
                text: commentRef.current ? commentRef.current.value : undefined,
            };
            cppwApi &&
                cppwApi.editComment(id, newComment, () => {
                    editItem(itemIndex, newComment);
                    setIsEditing(false);
                });
        }, [editItem, itemIndex, id, cppwApi, setIsEditing]);
        const onCancelEditing = useCallback(() => {
            setIsEditing(false);
        }, [setIsEditing]);
        return (
            <div className={'comment-view'}>
                <div className={'comment-view__title'}>
                    <Link to={`user?id=${user.id}`}>{user.name}</Link>
                    {` ${action}`}
                </div>
                {isEditing ? (
                    <FormInput
                        title=''
                        key={'comment'}
                        name={'net'}
                        id={'net-name'}
                        defaultValue={text}
                        mandatory={false}
                        validators={validators}
                        placeholder={''}
                        usage={Usage.LongText}
                        inputRef={setCommentRef}
                        formIndex={-1}
                        index={-1}
                    />
                ) : (
                    <div>{text}</div>
                )}
                {file && !isEditing && (
                    <div>
                        <a href={`${BACKEND_URL}/api/v1/${file.type}/${file.id}`} download={true}>
                            {file.name}
                        </a>
                    </div>
                )}
                {isEditing && (
                    <FormInput
                        key={'filik'}
                        title=''
                        name={'file'}
                        id={'filik'}
                        defaultValue={file}
                        mandatory={false}
                        validators={validators}
                        usage={Usage.File}
                        inputRef={setFileRef}
                        formIndex={-1}
                        index={-1}
                        placeholder='Нажмите для выбора файла...'
                    />
                )}
                {!isEditing && modifyAllowed && (
                    <>
                        <Icon icon='cross' className='comment-view__cross' onClick={onDelete} />
                        <Icon icon='cog' className='comment-view__cog' onClick={onStartEditing} />
                    </>
                )}
                {isEditing && (
                    <ButtonGroup>
                        <Button
                            text='Сохранить'
                            intent={Intent.PRIMARY}
                            onClick={onFinishEditing}
                        />
                        <Button text='Отменить' onClick={onCancelEditing} />
                    </ButtonGroup>
                )}
            </div>
        );
    }
);
