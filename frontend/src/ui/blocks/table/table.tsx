import {ContextMenu, Intent, Menu, MenuItem} from '@blueprintjs/core';
import React, {
    CSSProperties,
    FC,
    memo,
    MouseEvent,
    useCallback,
    useEffect,
    useMemo,
    useRef,
    useState,
} from 'react';
import AutoSizer from 'react-virtualized-auto-sizer';
import {VariableSizeGrid as Grid} from 'react-window';
import {ColumnHeader, SortKind} from 'src/ui/blocks/table/column-header';
import {useCppwApiContext} from 'src/ui/contexts/cppw-api-context/cppw-api-context';
import {Clipboard} from 'src/ui/utils/clipboard';
import {throttle} from 'src/ui/utils/throttle';
import './table.styl';

const DEFAULT_ROW_HEIGHT = 20;
const MIN_COLUMN_WIDTH = 70;

type Props = {
    columnWidths: number[];
    rowCount: number;
    getCellData: (columnIndex: number, rowIndex: number) => string;
    getCellElement: (columnIndex: number, rowIndex: number) => string | JSX.Element;
    onColumnsReordered: (oldIndex: number, newIndex: number) => void;
    onColumnWidthChange: (columnIndex: number, newWidth: number) => void;
    onColumnSorted: (columnIndex: number, ascending: boolean) => void;
    className: string;
};
type SelectedCell = {row: number; column: number};
type PortalParams = {
    style: CSSProperties;
    start: SelectedCell;
    end: SelectedCell;
};
type PartialPortalParams = {
    style: CSSProperties;
    start: SelectedCell;
};
export const Table: FC<Props> = memo(
    ({
        columnWidths,
        rowCount,
        getCellElement,
        getCellData,
        onColumnsReordered,
        onColumnWidthChange,
        className,
        onColumnSorted,
    }) => {
        const cppwApi = useCppwApiContext();
        const columnCount = columnWidths.length - 1;

        const [portalParams, setPortalParams] = useState<PortalParams>({
            style: {top: -10, height: 0, left: -10, width: 0},
            start: {row: -1, column: -1},
            end: {row: -1, column: -1},
        });
        const [draggablePortalParams, setDraggablePortalParams] = useState<PartialPortalParams>({
            style: {top: 0, left: 0},
            start: {row: 0, column: 0},
        });
        // selected column to drag
        const [selectedColumn, setSelectedColumn] = useState(0);

        const [sortedColumn, setSortedColumn] = useState(0); // positive for ascending order, negative for descending

        const [, setIsContextMenuOpen] = useState(false);

        const [resizingState, setResizingState] = useState({
            column: 0,
            initialX: 0,
            initialPortalPosition: 0,
            initialWidth: 0,
        });

        const gridRef = useRef<any>(null);
        const portalParamsRef = useRef<PartialPortalParams | null>(null);

        const rowHeights = useMemo(() => [30, ...new Array(rowCount).fill(DEFAULT_ROW_HEIGHT)], [
            rowCount,
        ]);
        const fullHeight = useMemo(() => rowHeights.reduce((a, b) => a + b), [rowHeights]);
        const wrapperStyle = useMemo(() => {
            if (window.innerWidth <= 800) {
                return {height: Math.min(fullHeight, window.innerHeight)};
            }
            return undefined;
        }, [fullHeight]);

        const onCopy = useCallback(() => {
            const sparse: string[][] = [];
            const firstRow = Math.min(portalParams.start.row, portalParams.end.row);
            const lastRow = Math.max(portalParams.start.row, portalParams.end.row);
            const firstColumn = Math.min(portalParams.start.column, portalParams.end.column);
            const lastColumn = Math.max(portalParams.start.column, portalParams.end.column);
            if (firstRow < 0 || firstColumn < 0) {
                cppwApi &&
                    cppwApi.toaster.current &&
                    cppwApi.toaster.current.show({
                        icon: 'error',
                        intent: Intent.WARNING,
                        message: 'Необходимо выделить область копирования.',
                    });
                return;
            }
            for (let i = firstRow; i <= lastRow; i++) {
                sparse.push([]);
                for (let j = firstColumn; j <= lastColumn; j++) {
                    if (j === 0) {
                        continue;
                    }
                    sparse[sparse.length - 1].push(getCellData(j, i));
                }
            }
            const success = Clipboard.copyCells(sparse);
            if (!success) {
                cppwApi &&
                    cppwApi.toaster.current &&
                    cppwApi.toaster.current.show({
                        icon: 'error',
                        intent: Intent.DANGER,
                        message: 'Ошибка копирования.',
                    });
            } else {
                cppwApi &&
                    cppwApi.toaster.current &&
                    cppwApi.toaster.current.show({
                        icon: 'tick',
                        intent: Intent.SUCCESS,
                        message: 'Копирование выполнено успешно!',
                    });
            }
        }, [cppwApi, getCellData, portalParams]);
        const showContextMenu = useCallback(
            (e: React.MouseEvent<HTMLDivElement>) => {
                e.preventDefault();
                ContextMenu.show(
                    <Menu>
                        <MenuItem text='Копировать' onClick={onCopy} />
                    </Menu>,
                    {left: e.clientX, top: e.clientY},
                    () => setIsContextMenuOpen(false)
                );
                setIsContextMenuOpen(true);
            },
            [onCopy, setIsContextMenuOpen]
        );
        const setDragDestination = useCallback(
            (columnIndex: number) => {
                if (columnIndex > selectedColumn) {
                    columnIndex--;
                }
                if (selectedColumn && columnIndex !== draggablePortalParams.start.column) {
                    setDraggablePortalParams({
                        style: {
                            top: 0,
                            left: columnWidths
                                .slice(0, columnIndex + (columnIndex > selectedColumn ? 1 : 0))
                                .reduce((a: number, b: number) => a + b),
                            width: 0,
                            height: fullHeight,
                        },
                        start: {
                            row: 0,
                            column: columnIndex,
                        },
                    });
                }
            },
            [
                columnWidths,
                setDraggablePortalParams,
                selectedColumn,
                draggablePortalParams.start.column,
                fullHeight,
            ]
        );
        const Cell = useCallback(
            ({
                columnIndex,
                rowIndex,
                style,
            }: {
                columnIndex: number;
                rowIndex: number;
                style: CSSProperties;
            }) => {
                const reorder = () => {
                    if (selectedColumn) {
                        const nextColumn = draggablePortalParams.start.column;
                        onColumnsReordered(selectedColumn, nextColumn);
                        let newLeft = 0;
                        for (let i = 0; i <= nextColumn; i++) {
                            newLeft += i === selectedColumn ? 0 : columnWidths[i];
                        }

                        setPortalParams({
                            style: {
                                ...portalParams.style,
                                left:
                                    selectedColumn < nextColumn
                                        ? newLeft
                                        : draggablePortalParams.style.left,
                            },
                            start: {
                                row: 0,
                                column: nextColumn,
                            },
                            end: {
                                row: rowCount,
                                column: nextColumn,
                            },
                        });
                        setSelectedColumn(0);
                    }
                };
                const onMouseMove = (e: MouseEvent<HTMLDivElement, any>) => {
                    const columnToInsert =
                        columnIndex +
                        (e.nativeEvent.offsetX > columnWidths[columnIndex] / 2 ? 1 : 0);
                    setDragDestination(columnToInsert);
                };
                const reshapePortal = (
                    e: MouseEvent<HTMLDivElement, any>,
                    shouldStop: boolean = false
                ) => {
                    if (e.button !== 0 || !portalParamsRef.current) {
                        // if user has already started selecting an area, do not start over
                        return;
                    }

                    let {top, left, width, height} = e.currentTarget.style;
                    setTimeout(() => {
                        if (!portalParamsRef.current) {
                            return;
                        }
                        const startTop = Number(portalParamsRef.current.style.top);
                        const startLeft = Number(portalParamsRef.current.style.left);
                        const startWidth = Number(portalParamsRef.current.style.width);
                        const startHeight = Number(portalParamsRef.current.style.height);

                        const start = portalParamsRef.current.start;
                        if (shouldStop) {
                            portalParamsRef.current = null;
                        }
                        setPortalParams({
                            style: {
                                top: Math.min(startTop, parseFloat(top)),
                                left: Math.min(startLeft, parseFloat(left)),
                                width:
                                    Math.abs(startLeft - parseFloat(left)) +
                                    (parseFloat(left) > startLeft ? parseFloat(width) : startWidth),
                                height:
                                    Math.abs(startTop - parseFloat(top)) +
                                    (parseFloat(top) > startTop ? parseFloat(height) : startHeight),
                            },
                            start: start,
                            end: {
                                row: rowIndex,
                                column: columnIndex,
                            },
                        });
                    }, 0);
                };

                // Это хедер ряда
                if (!columnIndex) {
                    return (
                        <div
                            style={style}
                            className={`table__header ${
                                rowIndex ? 'table__row-header' : 'table__common-header'
                            }`}
                            onMouseDown={(e) => {
                                e.preventDefault();
                            }}
                            onClick={(e) => {
                                reorder();
                                const {top, left} = e.currentTarget.style;
                                if (rowIndex) {
                                    // Выделяем ряд
                                    setPortalParams({
                                        style: {
                                            top: parseFloat(top),
                                            left: parseFloat(left),
                                            width: columnWidths.reduce(
                                                (a: number, b: number) => a + b
                                            ),
                                            height: rowHeights[rowIndex],
                                        },
                                        start: {
                                            row: rowIndex,
                                            column: 0,
                                        },
                                        end: {
                                            row: rowIndex,
                                            column: columnCount,
                                        },
                                    });
                                } else {
                                    // Выделяем всю таблицу
                                    setPortalParams({
                                        style: {
                                            top: parseFloat(top),
                                            left: parseFloat(left),
                                            width: columnWidths.reduce(
                                                (a: number, b: number) => a + b
                                            ),
                                            height: rowHeights.reduce(
                                                (a: number, b: number) => a + b
                                            ),
                                        },
                                        start: {
                                            row: rowIndex,
                                            column: 0,
                                        },
                                        end: {
                                            row: rowCount,
                                            column: columnCount,
                                        },
                                    });
                                }
                            }}
                        >
                            {rowIndex || (
                                <>
                                    <div className='table__portal' style={portalParams.style} />
                                    {(Boolean(selectedColumn) || Boolean(resizingState.column)) && (
                                        <div
                                            className='table__portal'
                                            style={draggablePortalParams.style}
                                        />
                                    )}
                                </>
                            )}
                        </div>
                    );
                }
                if (!rowIndex) {
                    return (
                        <ColumnHeader
                            style={style}
                            onClick={() => {
                                const {top, left} = style;
                                setTimeout(() => {
                                    setPortalParams({
                                        style: {
                                            // @ts-ignore
                                            top: parseFloat(top),
                                            // @ts-ignore
                                            left: parseFloat(left),
                                            width: columnWidths[columnIndex],
                                            height: rowHeights.reduce(
                                                (a: number, b: number) => a + b
                                            ),
                                        },
                                        start: {
                                            row: 0,
                                            column: columnIndex,
                                        },
                                        end: {
                                            row: rowCount,
                                            column: columnIndex,
                                        },
                                    });
                                }, 0);
                            }}
                            startDrag={() => {
                                setDraggablePortalParams({
                                    style: {
                                        top: 0,
                                        // @ts-ignore
                                        left: parseFloat(style.left),
                                        width: 0,
                                        height: rowHeights.reduce((a: number, b: number) => a + b),
                                    },
                                    start: {
                                        row: rowIndex,
                                        column: columnIndex,
                                    },
                                });
                                setSelectedColumn(columnIndex);
                            }}
                            startResizing={(x) => {
                                const initialPortalPosition = columnWidths
                                    .slice(0, columnIndex + 1)
                                    .reduce((a: number, b: number) => a + b);
                                setResizingState({
                                    column: columnIndex,
                                    initialX: x,
                                    initialPortalPosition,
                                    initialWidth: columnWidths[columnIndex],
                                });

                                setDraggablePortalParams({
                                    style: {
                                        top: 0,
                                        left: initialPortalPosition,
                                        width: 0,
                                        height: fullHeight,
                                    },
                                    start: {
                                        row: 0,
                                        column: columnIndex,
                                    },
                                });
                            }}
                            onMouseMove={onMouseMove}
                            onMouseUp={reorder}
                            onSortChanged={() => {
                                const wasAscending = sortedColumn === columnIndex;

                                onColumnSorted(columnIndex, !wasAscending);
                                setSortedColumn(columnIndex * (wasAscending ? -1 : 1));
                            }}
                            sortKind={
                                sortedColumn === columnIndex
                                    ? SortKind.ASCENDING
                                    : sortedColumn === -columnIndex
                                    ? SortKind.DESCENDING
                                    : SortKind.NONE
                            }
                        >
                            {getCellElement(columnIndex, rowIndex)}
                        </ColumnHeader>
                    );
                }
                const elem = getCellElement(columnIndex, rowIndex);
                return (
                    <div
                        style={style}
                        className='table__cell'
                        onMouseDown={(e) => {
                            if (e.button !== 0 || portalParamsRef.current) {
                                return;
                            }

                            const {top, left, width, height} = e.currentTarget.style;
                            portalParamsRef.current = {
                                style: {
                                    top: parseFloat(top),
                                    left: parseFloat(left),
                                    width: parseFloat(width),
                                    height: parseFloat(height),
                                },
                                start: {row: rowIndex, column: columnIndex},
                            };
                        }}
                        onMouseUp={(e) => {
                            if (selectedColumn) {
                                reorder();
                            } else {
                                reshapePortal(e, true); // in case onMouseEnter was not fired after onMouseDown
                            }
                        }}
                        onMouseEnter={reshapePortal}
                        onMouseMove={onMouseMove}
                        onContextMenu={typeof elem === 'object' ? undefined : showContextMenu}
                    >
                        {elem}
                    </div>
                );
            },
            [
                setDragDestination,
                onColumnsReordered,
                selectedColumn,
                columnCount,
                rowCount,
                setDraggablePortalParams,
                getCellElement,
                showContextMenu,
                rowHeights,
                setPortalParams,
                columnWidths,
                fullHeight,
                sortedColumn,
                onColumnSorted,
                draggablePortalParams.start.column,
                draggablePortalParams.style,
                portalParams,
                resizingState,
            ]
        );
        const getColumnWidth = useCallback((index) => columnWidths[index], [columnWidths]);
        const getRawHeight = useCallback((index) => rowHeights[index], [rowHeights]);
        const finishResizing = useCallback(
            (e: MouseEvent<HTMLDivElement, any>) => {
                if (resizingState.column) {
                    const deltaX = e.pageX - resizingState.initialX;
                    const newWidth = Math.max(
                        resizingState.initialWidth + deltaX,
                        MIN_COLUMN_WIDTH
                    );
                    onColumnWidthChange(resizingState.column - 1, newWidth);
                    gridRef.current.resetAfterColumnIndex(resizingState.column);
                    setResizingState({
                        column: 0,
                        initialX: 0,
                        initialPortalPosition: 0,
                        initialWidth: 0,
                    });
                    setPortalParams({
                        ...portalParams,
                        style: {
                            ...portalParams.style,
                            width: newWidth,
                        },
                    });
                }
            },
            [portalParams, onColumnWidthChange, resizingState]
        );
        const handleMouseMoveThrottled = useCallback(
            throttle((newX: number) => {
                if (resizingState.column) {
                    let deltaX = newX - resizingState.initialX;
                    if (-deltaX > resizingState.initialWidth - MIN_COLUMN_WIDTH) {
                        deltaX = MIN_COLUMN_WIDTH - resizingState.initialWidth;
                    }

                    setDraggablePortalParams({
                        style: {
                            top: 0,
                            left: resizingState.initialPortalPosition + deltaX,
                            width: 0,
                            height: fullHeight,
                        },
                        start: {
                            row: 0,
                            column: resizingState.column,
                        },
                    });
                }
            }, 10),
            [resizingState, fullHeight]
        );
        const handleMouseMove = useCallback(
            (e: MouseEvent<HTMLDivElement, any>) => handleMouseMoveThrottled(e.pageX),
            [handleMouseMoveThrottled]
        );

        useEffect(() => {
            gridRef.current && gridRef.current.resetAfterColumnIndex(0);
        }, [columnWidths]);

        return (
            <div
                className={'table-wrapper ' + className}
                onMouseLeave={finishResizing}
                onMouseUp={finishResizing}
                onMouseMove={handleMouseMove}
                style={wrapperStyle}
            >
                <AutoSizer
                    className={`table ${selectedColumn ? '_draggable' : ''} ${
                        resizingState.column ? '_resizing' : ''
                    }`}
                >
                    {({height, width}) => (
                        <Grid
                            ref={gridRef}
                            columnCount={columnCount + 1}
                            columnWidth={getColumnWidth}
                            rowCount={rowCount + 1}
                            rowHeight={getRawHeight}
                            height={height}
                            width={width}
                            overscanRowCount={100}
                            overscanColumnCount={columnCount}
                        >
                            {Cell}
                        </Grid>
                    )}
                </AutoSizer>
            </div>
        );
    }
);
