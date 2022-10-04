import React, {FC, memo, useCallback, useEffect, useMemo, useState} from 'react';
import {useSelector} from 'react-redux';
import {State} from 'src/store/reducers';
import {AsyncStatus, Attribute, ProfileOrSearchItem, SearchType, Usage, UserRole} from 'src/types';
import {LoadableView} from 'src/ui/blocks/loadable-view/loadable-view';
import {
    SEARCH_TYPE_TO_BUTTONS_MAP,
    SearchButtons,
} from 'src/ui/blocks/search-buttons/search-buttons';
import {Table} from 'src/ui/blocks/table/table';
import {useCppwApiContext} from 'src/ui/contexts/cppw-api-context/cppw-api-context';
import {
    getFormattedStringValue,
    getLinkOrText,
    getText,
} from 'src/ui/utils/get-formatted-attribute-value';
import {reorderArray} from 'src/ui/utils/reoder-array';

type Props = {
    searchType: SearchType;
    status: AsyncStatus;
    fields: Attribute[];
    records: ProfileOrSearchItem[];
    setMark?: (mark: string, recordIndex: number, fieldName: string) => void;
    profileId?: string;
};
export const SearchResults: FC<Props> = memo(
    ({searchType, status, fields, records, setMark, profileId}) => {
        const cppwApi = useCppwApiContext();
        const {role: userRole} = useSelector((state: State) =>
            state.user.isAuthed
                ? state.user
                : {
                      role: undefined,
                  }
        );
        const [innerFields, setInnerFields] = useState(
            fields
                .filter(({isAdded}) => isAdded)
                .map((field, index) => ({
                    ...field,
                    width: 180 + index * 10,
                }))
        );
        const [recordsState, setRecordsState] = useState(records);

        const hasButtons =
            !!SEARCH_TYPE_TO_BUTTONS_MAP[searchType]?.length &&
            userRole &&
            [
                UserRole.Manager,
                UserRole.Supervisor,
                UserRole.AcademicManager,
                UserRole.OfficeManager,
            ].includes(userRole);
        const columnWidths = useMemo(
            () => [
                recordsState.length.toString().length * 10 + 10,
                ...innerFields.map(({width}) => width),
            ],
            [recordsState.length, innerFields]
        );

        const getCellElement = useCallback(
            (col: number, row: number) => {
                const field = innerFields[col - 1];
                const {realName, name, title} = field;

                if (!row) {
                    return title;
                }
                if (field.modifyAllowed && realName && profileId) {
                    const callback = (e: any) => {
                        e.stopPropagation();
                    };
                    const itemInfo = recordsState[row - 1];
                    const [, stageId, gradeType] = realName.split(':');
                    return (
                        <input
                            className={'table__input'}
                            type='text'
                            defaultValue={getText(
                                itemInfo,
                                searchType,
                                realName || name,
                                field.usage
                            )}
                            onKeyUp={(e) => {
                                if (e.keyCode === 13) {
                                    cppwApi &&
                                        cppwApi.changeMark(
                                            // @ts-ignore
                                            e.target && e.target.value,
                                            profileId,
                                            itemInfo.student.id,
                                            stageId,
                                            gradeType,
                                            (mark) => {
                                                setMark && setMark(mark, row - 1, realName);
                                            }
                                        );
                                }
                            }}
                            onMouseUp={callback}
                            onMouseDown={callback}
                            onClick={callback}
                        />
                    );
                }

                return getLinkOrText(
                    recordsState[row - 1],
                    searchType,
                    realName || name,
                    field.usage
                );
            },
            [profileId, cppwApi, innerFields, recordsState, searchType, setMark]
        );
        const getCellData = useCallback(
            (col, row) => {
                if (!row) {
                    const field = innerFields[col - 1];
                    const {title} = field;
                    return title;
                }
                const item = recordsState[row - 1];
                const field = innerFields[col - 1];
                const {realName} = field;
                if (realName) {
                    return getFormattedStringValue(item[realName], field.usage);
                }
                return getFormattedStringValue(item.content[field.name], field.usage);
            },
            [recordsState, innerFields]
        );
        const handleColumnsReordered = useCallback(
            (oldIndex: number, newIndex: number) => {
                if (oldIndex === newIndex) {
                    return;
                }
                const nextFields = reorderArray(innerFields, oldIndex - 1, newIndex - 1, 1);
                if (nextFields) {
                    setInnerFields(nextFields);
                }
            },
            [innerFields]
        );
        const handleColumnWidthChange = useCallback(
            (columnIndex: number, newWidth: number) => {
                setInnerFields(
                    innerFields.map((field, index) => {
                        if (index === columnIndex) {
                            field.width = newWidth;
                        }
                        return field;
                    })
                );
            },
            [innerFields]
        );
        const sortRecords = useCallback(
            (column, ascending) => {
                setRecordsState(
                    recordsState.sort((a, b) => {
                        const field = innerFields[column - 1];
                        const [aVal, bVal] = [a, b]
                            .map((x) => x[field.realName || ''])
                            .map((x) => x?.name || x);
                        if (aVal === bVal) {
                            return 0;
                        }
                        if (field.usage === Usage.Number) {
                            const isBigger = Number(aVal) > Number(bVal);
                            return ascending === isBigger ? 1 : -1;
                        }

                        const isBigger = aVal > bVal;
                        return ascending === isBigger ? 1 : -1;
                    })
                );
            },
            [recordsState, innerFields]
        );

        useEffect(() => {
            setRecordsState(records);
        }, [records]);
        useEffect(() => {
            setInnerFields(
                fields
                    .filter(({isAdded}) => isAdded)
                    .map((field, index) => ({...field, width: 180 + index * 10}))
            );
        }, [fields]);

        return (
            <LoadableView
                status={status}
                spinnerClassName='spinner-full-height'
                errorTitle='Ошибка загрузки результатов поиска'
                errorSubtitle='Повторите попытку, нажав кнопку Найти'
            >
                <>
                    <SearchButtons searchType={searchType} records={recordsState} fields={fields} />
                    <Table
                        columnWidths={columnWidths}
                        rowCount={recordsState.length}
                        getCellData={getCellData}
                        getCellElement={getCellElement}
                        onColumnsReordered={handleColumnsReordered}
                        onColumnWidthChange={handleColumnWidthChange}
                        className={hasButtons ? '_with-buttons' : ''}
                        onColumnSorted={sortRecords}
                    />
                </>
            </LoadableView>
        );
    }
);
