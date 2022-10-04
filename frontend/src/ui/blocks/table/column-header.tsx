import {Icon, Intent} from '@blueprintjs/core';
import React, {CSSProperties, FC, memo, MouseEvent, useCallback} from 'react';

export enum SortKind {
    NONE,
    ASCENDING,
    DESCENDING,
}

type Props = {
    style: CSSProperties;
    sortKind: SortKind;
    onClick: () => void;
    onMouseMove: (e: MouseEvent<HTMLDivElement, any>) => void;
    onMouseUp: (e: MouseEvent<HTMLDivElement, any>) => void;
    startResizing: (pageX: number) => void;
    onSortChanged: () => void;
    startDrag: () => void;
};

export const ColumnHeader: FC<Props> = memo(
    ({
        style,
        onClick,
        onMouseMove,
        startDrag,
        startResizing,
        onMouseUp,
        children,
        sortKind,
        onSortChanged,
    }) => {
        const onMouseDown = useCallback((e) => {
            e.preventDefault();
        }, []);
        const clickAndStartDrag = useCallback(
            (e: MouseEvent<HTMLDivElement, any>) => {
                onClick();
                startDrag();
            },
            [onClick, startDrag]
        );
        const handleStartResize = useCallback(
            (e: MouseEvent<HTMLDivElement, any>) => {
                onClick();
                startResizing(e.pageX);
            },
            [onClick, startResizing]
        );

        return (
            <div
                style={style}
                className='table__header table__column-header'
                onMouseDown={onMouseDown}
                onMouseMove={onMouseMove}
                onMouseUp={onMouseUp}
                onClick={onClick}
            >
                <Icon
                    className='column-header__draggable'
                    icon={'drag-handle-vertical'}
                    onMouseDown={clickAndStartDrag}
                />
                <div className='column-header__title'>{children}</div>
                <Icon
                    className='column-header__sort-button'
                    icon={sortKind === SortKind.DESCENDING ? 'chevron-up' : 'chevron-down'}
                    intent={sortKind === SortKind.NONE ? Intent.NONE : Intent.PRIMARY}
                    onClick={onSortChanged}
                />
                <div className='column-header__resize-handle' onMouseDown={handleStartResize} />
            </div>
        );
    }
);
