import {FileInput, Icon, Intent, ProgressBar} from '@blueprintjs/core';
import React, {FC, memo, useCallback, useEffect, useState} from 'react';
import {useUpload} from 'react-use-upload';
import {useCppwApiContext} from 'src/ui/contexts/cppw-api-context/cppw-api-context';
import {BACKEND_URL} from 'src/ui/utils/constants';

type Props = {
    inputRef: (ref: any) => void;
    placeholder?: string;
    defaultValue?: {name: string; id: string; type: 'file'};
    savedValue?: {name: string; id: string; type: 'file'};
    disabled?: boolean;
    isMultiple?: boolean;
    clearRef?: (handleDelete: () => void) => void;
    taskId?: string;
    disableLoading?: string;
};
export const FileInputView: FC<Props> = memo(({disableLoading, ...props}) => {
    if (disableLoading) {
        return <FileInputViewInnerWithoutLoading {...props} />;
    }
    return <FileInputViewInnerWithLoading {...props} />;
});

export const FileInputViewInnerWithoutLoading: FC<Props> = memo(
    ({placeholder, inputRef, savedValue, disabled, isMultiple = false}) => {
        const [files, setFiles] = useState();
        const [savedFile, setSavedFile] = useState(savedValue);
        const {loading, progress, response, done} = useUpload(files, {
            path: '/api/v1/do-not-upload',
            method: 'POST',
            name: 'file',
            fields: {name: files && files[0]?.name},
        });
        const onRestart = useCallback(() => {
            setFiles(undefined);
            setSavedFile(undefined);
        }, [setFiles, setSavedFile]);
        const onClick = useCallback((e) => {
            e.preventDefault();
        }, []);

        useEffect(() => {
            if (done && files && response) {
                inputRef({value: {name: files[0].name, id: response.response, type: 'file'}});
            } else {
                inputRef({value: savedFile || null});
            }
        }, [response, inputRef, done, files, savedFile]);

        if (loading && files) {
            return <ProgressBar intent={Intent.PRIMARY} value={progress ? progress / 100 : 0} />;
        }

        if (done && files && response) {
            return (
                <div>
                    {/* eslint-disable-next-line jsx-a11y/anchor-is-valid */}
                    <a onClick={onClick}>{files[0].name}</a>
                    &#160;
                    <Icon icon={'cross'} className='file-input__control' onClick={onRestart} />
                </div>
            );
        }

        if (savedFile) {
            return (
                <div>
                    {/* eslint-disable-next-line jsx-a11y/anchor-is-valid */}
                    <a onClick={onClick}>{savedFile.name}</a>
                    &#160;
                    <Icon icon={'cross'} className='file-input__control' onClick={onRestart} />
                </div>
            );
        }
        return (
            <FileInput
                text={placeholder}
                buttonText={'Выберите файл'}
                fill={true}
                disabled={disabled}
                // @ts-ignore
                onChange={(e) => setFiles(e.target.files)}
            />
        );
    }
);

export const FileInputViewInnerWithLoading: FC<Props> = memo(
    ({
        placeholder,
        inputRef,
        disabled,
        defaultValue,
        savedValue,
        clearRef,
        taskId,
        isMultiple = false,
    }) => {
        const cppwApi = useCppwApiContext();
        const [files, setFiles] = useState();
        const [shouldIgnoreDefaultOrSaved, setShouldIgnoreDefaultOrSaved] = useState(false);
        const [savedFile, setSavedFile] = useState(savedValue);
        const {loading, progress, error, response, done} = useUpload(files, {
            path: taskId ? `/api/v1/upload?taskId=${taskId}` : '/api/v1/upload',
            method: 'POST',
            name: 'file',
            withCredentials: Boolean(process.env.IS_DEV),
            fields: {name: files && files[0]?.name},
        });
        const onRestart = useCallback(() => {
            if (taskId && (response || defaultValue)) {
                cppwApi &&
                    cppwApi.deleteFileFromTask(
                        taskId,
                        !shouldIgnoreDefaultOrSaved && defaultValue
                            ? defaultValue.id
                            : response.response,
                        () => {
                            setFiles(undefined);
                        }
                    );
            } else if (taskId && (response || savedFile)) {
                cppwApi &&
                    cppwApi.deleteFileFromTask(
                        taskId,
                        !shouldIgnoreDefaultOrSaved && savedFile ? savedFile.id : response.response,
                        () => {
                            setFiles(undefined);
                        }
                    );
            } else {
                setFiles(undefined);
                setSavedFile(undefined);
            }
        }, [
            shouldIgnoreDefaultOrSaved,
            response,
            defaultValue,
            cppwApi,
            taskId,
            setFiles,
            savedFile,
            setSavedFile,
        ]);
        const onDeleteDefaultOrSaved = useCallback(() => {
            if (taskId && (response || defaultValue)) {
                cppwApi &&
                    cppwApi.deleteFileFromTask(
                        taskId,
                        !shouldIgnoreDefaultOrSaved && defaultValue
                            ? defaultValue.id
                            : response.response,
                        () => {
                            setShouldIgnoreDefaultOrSaved(true);
                        }
                    );
            } else if (taskId && (response || savedFile)) {
                cppwApi &&
                    cppwApi.deleteFileFromTask(
                        taskId,
                        !shouldIgnoreDefaultOrSaved && savedFile ? savedFile.id : response.response,
                        () => {
                            setShouldIgnoreDefaultOrSaved(true);
                        }
                    );
            } else {
                setShouldIgnoreDefaultOrSaved(true);
                setSavedFile(undefined);
            }
        }, [
            shouldIgnoreDefaultOrSaved,
            response,
            defaultValue,
            cppwApi,
            taskId,
            savedFile,
            setShouldIgnoreDefaultOrSaved,
        ]);

        useEffect(() => {
            if (done && files && response && !response.error) {
                inputRef({value: {name: files[0].name, id: response.response, type: 'file'}});
            } else {
                inputRef({
                    value: (shouldIgnoreDefaultOrSaved ? null : savedFile || defaultValue) || null,
                });
            }
        }, [
            response,
            defaultValue,
            shouldIgnoreDefaultOrSaved,
            inputRef,
            done,
            files,
            error,
            savedFile,
        ]);
        useEffect(() => {
            clearRef && clearRef(onRestart);
        }, [clearRef, onRestart]);

        if (loading && files) {
            return <ProgressBar intent={Intent.PRIMARY} value={progress ? progress / 100 : 0} />;
        }

        if (done && files && response) {
            if (response.error) {
                return (
                    <div>
                        <Icon
                            className='file-input__control'
                            icon={'refresh'}
                            intent={Intent.DANGER}
                            onClick={onRestart}
                        />{' '}
                        Ошибка загрузки файла.
                    </div>
                );
            }
            return (
                <div>
                    <a href={`${BACKEND_URL}/api/v1/file/${response.response}`} download={true}>
                        {files[0].name}
                    </a>
                    &#160;
                    {!disabled && (
                        <Icon icon={'cross'} className='file-input__control' onClick={onRestart} />
                    )}
                </div>
            );
        }

        if (savedFile) {
            return (
                <div>
                    <a
                        href={`${BACKEND_URL}/api/v1/${savedFile.type}/${savedFile.id}`}
                        download={true}
                    >
                        {savedFile.name}
                    </a>
                    &#160;
                    {!disabled && (
                        <Icon
                            icon={'cross'}
                            className='file-input__control'
                            onClick={onDeleteDefaultOrSaved}
                        />
                    )}
                </div>
            );
        }

        if (
            !files &&
            !done &&
            !loading &&
            !response &&
            defaultValue &&
            !shouldIgnoreDefaultOrSaved
        ) {
            return (
                <div>
                    <a href={`/${defaultValue.type}/${defaultValue.id}`} download={true}>
                        {defaultValue.name}
                    </a>
                    &#160;
                    {!disabled && (
                        <Icon
                            icon={'cross'}
                            className='file-input__control'
                            onClick={onDeleteDefaultOrSaved}
                        />
                    )}
                </div>
            );
        }

        return (
            <FileInput
                text={placeholder}
                buttonText={'Выберите файл'}
                fill={true}
                disabled={disabled}
                // @ts-ignore
                onChange={(e) => setFiles(e.target.files)}
            />
        );
    }
);
