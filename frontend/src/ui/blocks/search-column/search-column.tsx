import {Button, Classes} from '@blueprintjs/core';

import React, {FC, memo, useCallback, useMemo, useRef} from 'react';
import {useDispatch, useSelector} from 'react-redux';
import {selectSearchType, setFilter} from 'src/store/actions/search.actions';
import {State} from 'src/store/reducers';

import {AsyncStatus, Attribute, AttributeRef, SearchType, Usage, UserRole} from 'src/types';
import {FormInput} from 'src/ui/blocks/form-input/form-input';
import {LoadableView} from 'src/ui/blocks/loadable-view/loadable-view';
import {useCppwApiContext} from 'src/ui/contexts/cppw-api-context/cppw-api-context';
import './search-column.styl';

type Props = {
    status: AsyncStatus;
    fields: Attribute[];
    searchType: SearchType;
    handleSearch?: (filter: Attribute[]) => void;
    isColumnsUpdateDisabled?: boolean;
};

export const SearchColumn: FC<Props> = memo(
    ({status, fields, searchType, handleSearch, isColumnsUpdateDisabled}) => {
        const cppwApi = useCppwApiContext();
        const dispatch = useDispatch();
        const {role: userRole} = useSelector((state: State) =>
            state.user.isAuthed ? state.user : {role: UserRole.Initial}
        );
        const searchTypeRef = useRef<HTMLSelectElement>(null);

        const isAddedArray = useMemo(() => fields.map(({isAdded}) => isAdded), [fields]);
        const refs = useMemo<(null | AttributeRef)[]>(() => fields.map(() => null), [fields]);
        const setInputRefs = useMemo(
            () =>
                fields.map((val, index) => (elem: AttributeRef) => {
                    refs[index] = elem;
                }),
            [refs, fields]
        );

        const onChangeSearchType = useCallback(() => {
            searchTypeRef.current &&
                dispatch(selectSearchType(searchTypeRef.current.value as SearchType));
            searchTypeRef.current && cppwApi && cppwApi.search();
        }, [dispatch, searchTypeRef, cppwApi]);
        const baseSearch =
            SearchType.Questionnaire === searchType ? null : (
                <>
                    <div className='bp3-select bp3-fill search-column__select'>
                        <select
                            defaultValue={searchType}
                            ref={searchTypeRef}
                            onChange={onChangeSearchType}
                        >
                            <option value={SearchType.Initial} disabled>
                                Поиск по...
                            </option>
                            <option value={SearchType.Students}>Студенты</option>
                            <option value={SearchType.Mentors}>Менторы</option>
                            <option value={SearchType.Managers}>Менеджеры</option>
                            <option value={SearchType.Representatives}>Контактные лица</option>
                            <option value={SearchType.Activities}>Активности</option>
                            <option value={SearchType.Projects}>Проекты</option>
                            {[
                                UserRole.Supervisor,
                                UserRole.Manager,
                                UserRole.AcademicManager,
                                UserRole.OfficeManager,
                            ].includes(userRole) && (
                                <option value={SearchType.ProjectRequests}>
                                    Заявки на проекты
                                </option>
                            )}
                            <option value={SearchType.Organisations}>Организации</option>
                        </select>
                    </div>
                </>
            );

        const onSearch = useCallback(() => {
            const filter: Attribute[] = fields.map((item, index) => ({
                ...item,
                defaultValue: refs[index] ? refs[index]?.value : '',
            }));
            if (handleSearch) {
                handleSearch(filter);
            } else {
                dispatch(setFilter(filter));
                searchTypeRef.current && cppwApi && cppwApi.fullSearch();
            }
        }, [handleSearch, dispatch, cppwApi, fields, refs]);
        const onColumnsUpdate = useCallback(() => {
            const filter: Attribute[] = fields.map((item, index) => ({
                ...item,
                isAdded: isAddedArray[index],
            }));
            dispatch(setFilter(filter));
        }, [isAddedArray, dispatch, fields]);

        return (
            <div className={'search-column'}>
                {baseSearch}
                <LoadableView status={status} errorSubtitle='Ошибка загрузки параметров поиска'>
                    <div className='scrollable-tabs'>
                        {fields.map(
                            ({name, title, usage, defaultValue, variants, isAdded}, index) => {
                                if (usage === Usage.File) {
                                    return null;
                                }
                                return (
                                    <FormInput
                                        key={name}
                                        title={title}
                                        name={name}
                                        id={name}
                                        mandatory={false}
                                        validators={[]}
                                        defaultValue={usage === Usage.Switch ? '' : defaultValue}
                                        usage={
                                            (usage === Usage.Checkbox ? Usage.Radio : usage) ||
                                            Usage.ShortText
                                        }
                                        inputRef={setInputRefs[index]}
                                        variants={variants}
                                        formIndex={-1}
                                        index={-1}
                                        isAddable={!isColumnsUpdateDisabled}
                                        isAdded={isAdded}
                                        triggerIsAdded={() => {
                                            isAddedArray[index] = !isAddedArray[index];
                                        }}
                                    />
                                );
                            }
                        )}
                    </div>
                </LoadableView>
                <Button className={`${Classes.FILL}`} text='Найти' onClick={onSearch} />
                {!isColumnsUpdateDisabled && (
                    <Button
                        className={`${Classes.FILL} search-column__button`}
                        text='Обновить состав столбцов'
                        onClick={onColumnsUpdate}
                    />
                )}
            </div>
        );
    }
);
