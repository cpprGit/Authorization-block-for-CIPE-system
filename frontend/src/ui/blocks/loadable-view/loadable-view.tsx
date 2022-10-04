import {Spinner} from '@blueprintjs/core';
import React, {FC, memo} from 'react';
import {AsyncStatus} from 'src/types';
import {ErrorView} from 'src/ui/blocks/error-view/error-view';

type Props = {
    status: AsyncStatus;

    spinnerClassName?: string;

    errorTitle?: string;
    errorSubtitle?: string;
    onRetry?: () => void;
    errorSize?: 's';
};

export const LoadableView: FC<Props> = memo(
    ({children, status, errorTitle, errorSubtitle, onRetry, errorSize, spinnerClassName}) => {
        switch (status) {
            case AsyncStatus.Success:
                return <>{children}</>;
            case AsyncStatus.Pending:
                return <Spinner className={spinnerClassName} />;
            case AsyncStatus.Error:
                if (errorTitle || errorSubtitle || onRetry || errorSize) {
                    return (
                        <ErrorView
                            title={errorTitle}
                            subtitle={errorSubtitle}
                            onRetry={onRetry}
                            size={errorSize}
                        />
                    );
                }
                return null;
            case AsyncStatus.Initial:
            default:
                return null;
        }
    }
);
