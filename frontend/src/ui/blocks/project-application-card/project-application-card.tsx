import {Button, ButtonGroup, Intent} from '@blueprintjs/core';
import React, {FC, memo, useCallback, useMemo} from 'react';
import {Link} from 'react-router-dom';
import {ProfileOrSearchItem, ProjectApplicationStatus, ProjectRequestStatus} from 'src/types';
import {useCppwApiContext} from 'src/ui/contexts/cppw-api-context/cppw-api-context';
import './project-application-card.styl';

type ProjectApplicationCardProps = {
    name: string;
    link: string;
    status: ProjectApplicationStatus;
    projectId: string;
    studentId: string;
    editItem: (itemIndex: number, newInfo: ProfileOrSearchItem) => void;
    itemIndex: number;
};
export const ProjectApplicationCard: FC<ProjectApplicationCardProps> = memo(
    ({name, link, status, projectId, studentId, editItem, itemIndex}) => {
        const cppwApi = useCppwApiContext();
        const handleAccept = useCallback(() => {
            cppwApi &&
                cppwApi.changeProjectApplicationStatus(
                    projectId,
                    studentId,
                    ProjectApplicationStatus.Accepted,
                    () => {
                        editItem(itemIndex, {status: ProjectApplicationStatus.Accepted});
                    }
                );
        }, [projectId, studentId, cppwApi, editItem, itemIndex]);
        const handleReject = useCallback(() => {
            cppwApi &&
                cppwApi.changeProjectApplicationStatus(
                    projectId,
                    studentId,
                    ProjectApplicationStatus.Rejected,
                    () => {
                        editItem(itemIndex, {status: ProjectApplicationStatus.Rejected});
                    }
                );
        }, [projectId, studentId, cppwApi, editItem, itemIndex]);
        const handleRestart = useCallback(() => {
            cppwApi &&
                cppwApi.changeProjectApplicationStatus(
                    projectId,
                    studentId,
                    ProjectApplicationStatus.Waiting,
                    () => {
                        editItem(itemIndex, {status: ProjectApplicationStatus.Waiting});
                    }
                );
        }, [projectId, studentId, cppwApi, editItem, itemIndex]);

        const cardControls = useMemo(() => {
            switch (status) {
                case ProjectApplicationStatus.Waiting: {
                    return (
                        <ButtonGroup className='project-application-card__buttons'>
                            <Button
                                text='Подтвердить'
                                onClick={handleAccept}
                                intent={Intent.PRIMARY}
                            />
                            <Button
                                text='Отклонить'
                                onClick={handleReject}
                                intent={Intent.DANGER}
                            />
                        </ButtonGroup>
                    );
                }
                case ProjectApplicationStatus.Accepted: {
                    return (
                        <div>
                            <span className='project-application-card__accepted'>
                                Заявка подтверждена.
                            </span>
                            <Button
                                className='project-application-card__buttons'
                                text='Вернуть в рассмотрение'
                                onClick={handleRestart}
                            />
                        </div>
                    );
                }
                case ProjectApplicationStatus.Rejected: {
                    return (
                        <div>
                            <span className='project-application-card__rejected'>
                                Заявка отклонена.
                            </span>
                            <Button
                                className='project-application-card__buttons'
                                text='Вернуть в рассмотрение'
                                onClick={handleRestart}
                            />
                        </div>
                    );
                }
                default:
                    return null;
            }
        }, [handleAccept, handleReject, handleRestart, status]);
        return (
            <div className='project-application-card'>
                <Link to={link} className='project-application-card__link'>
                    {name}
                </Link>
                <div className='project-application-card__controls'>{cardControls}</div>
            </div>
        );
    }
);

type ProjectApplicationOwnerCardProps = {
    name: string;
    link: string;
    status: ProjectApplicationStatus | ProjectRequestStatus;
    isCanceled?: boolean;
    // Иногда он на самом деле projectId:)
    applicationId: string;
    editItem: (itemIndex: number, newInfo: ProfileOrSearchItem) => void;
    itemIndex: number;
};
export const ProjectApplicationOwnerCard: FC<ProjectApplicationOwnerCardProps> = memo(
    ({name, link, status, isCanceled, applicationId, editItem, itemIndex}) => {
        const cppwApi = useCppwApiContext();
        const handleCancel = useCallback(() => {
            if (isCanceled) {
                cppwApi &&
                    cppwApi.reapplyForProjectApplication(applicationId, () => {
                        editItem(itemIndex, {isCanceled: false});
                    });
            } else {
                cppwApi &&
                    cppwApi.cancelProjectApplication(applicationId, () => {
                        editItem(itemIndex, {isCanceled: true});
                    });
            }
        }, [isCanceled, applicationId, cppwApi, editItem, itemIndex]);
        const cardControls = useMemo(() => {
            switch (status) {
                case ProjectRequestStatus.Waiting:
                case ProjectApplicationStatus.Waiting: {
                    return (
                        <div>
                            {!isCanceled && (
                                <span className='project-application-card__waiting'>
                                    Заявка на рассмотрении.
                                </span>
                            )}
                            <Button
                                text={isCanceled ? 'Подать заявку' : 'Отменить заявку'}
                                onClick={handleCancel}
                            />
                        </div>
                    );
                }
                case ProjectRequestStatus.Accepted:
                case ProjectApplicationStatus.Accepted: {
                    return (
                        <div>
                            <span className='project-application-card__accepted _cancel-margin'>
                                Заявка подтверждена.
                            </span>
                        </div>
                    );
                }
                case ProjectRequestStatus.Rejected:
                case ProjectApplicationStatus.Rejected: {
                    return (
                        <div>
                            <span className='project-application-card__rejected _cancel-margin'>
                                Заявка отклонена.
                            </span>
                        </div>
                    );
                }
                default:
                    return null;
            }
        }, [isCanceled, handleCancel, status]);
        return (
            <div className='project-application-card'>
                <Link to={link} className='project-application-card__link'>
                    {name}
                </Link>
                <div className='project-application-card__controls'>{cardControls}</div>
            </div>
        );
    }
);
export const ProjectApplicationOwnerCard3: FC<ProjectApplicationOwnerCardProps> = memo(
    ({name, link, status, isCanceled, applicationId, editItem, itemIndex}) => {
        const cppwApi = useCppwApiContext();
        const handleCancel = useCallback(() => {
            if (isCanceled) {
                cppwApi &&
                    cppwApi.recreateProjectRequest(applicationId, () => {
                        editItem(itemIndex, {isCanceled: false});
                    });
            } else {
                cppwApi &&
                    cppwApi.cancelProjectRequest(applicationId, () => {
                        editItem(itemIndex, {isCanceled: true});
                    });
            }
        }, [isCanceled, applicationId, cppwApi, editItem, itemIndex]);
        const cardControls = useMemo(() => {
            switch (status) {
                case ProjectRequestStatus.Rejected:
                case ProjectApplicationStatus.Rejected:
                case ProjectRequestStatus.Waiting:
                case ProjectApplicationStatus.Waiting: {
                    return (
                        <div>
                            {!isCanceled && (
                                <span className='project-application-card__waiting'>
                                    Заявка на рассмотрении.
                                </span>
                            )}
                            <Button
                                text={isCanceled ? 'Переподать заявку' : 'Отменить заявку'}
                                onClick={handleCancel}
                            />
                        </div>
                    );
                }
                case ProjectRequestStatus.Accepted:
                case ProjectApplicationStatus.Accepted: {
                    return (
                        <div>
                            <span className='project-application-card__accepted _cancel-margin'>
                                Заявка подтверждена.
                            </span>
                        </div>
                    );
                }
                default:
                    return null;
            }
        }, [isCanceled, handleCancel, status]);
        return (
            <div className='project-application-card'>
                <Link to={link} className='project-application-card__link'>
                    {name}
                </Link>
                <div className='project-application-card__controls'>{cardControls}</div>
            </div>
        );
    }
);

type ProjectApplicationOwnerCard2Props = {
    name: string;
    link: string;
    isRequested: boolean;
    projectId: string;
    editItem: (itemIndex: number, newInfo: ProfileOrSearchItem) => void;
    itemIndex: number;
};
export const ProjectApplicationOwnerCard2: FC<ProjectApplicationOwnerCard2Props> = memo(
    ({name, link, isRequested, projectId, editItem, itemIndex}) => {
        const cppwApi = useCppwApiContext();
        const handleRequest = useCallback(() => {
            if (!isRequested) {
                cppwApi &&
                    cppwApi.reapplyForProjectApplication(projectId, () => {
                        editItem(itemIndex, {isRequested: true});
                    });
            } else {
                cppwApi &&
                    cppwApi.cancelProjectApplication(projectId, () => {
                        editItem(itemIndex, {isRequested: false});
                    });
            }
        }, [isRequested, projectId, cppwApi, editItem, itemIndex]);
        return (
            <div className='project-application-card'>
                <Link to={link} className='project-application-card__link'>
                    {name}
                </Link>
                <div className='project-application-card__controls'>
                    <Button
                        text={isRequested ? 'Отменить заявку' : 'Подать заявку'}
                        onClick={handleRequest}
                        intent={isRequested ? Intent.DANGER : Intent.PRIMARY}
                    />
                </div>
            </div>
        );
    }
);
