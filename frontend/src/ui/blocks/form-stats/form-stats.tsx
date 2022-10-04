import React, {FC, memo, useCallback, useEffect, useMemo} from 'react';
import {AsyncStatus, Attribute, Content, SearchType, Usage} from 'src/types';
import {Layout} from 'src/ui/blocks/layout/layout';
import {SearchColumn} from 'src/ui/blocks/search-column/search-column';
import {SearchResults} from 'src/ui/blocks/search-results/search-results';
import {useCppwApiContext} from 'src/ui/contexts/cppw-api-context/cppw-api-context';

type Props = {
    formId: string;
    formIndex: number;
    status: AsyncStatus;
    records: Content[];
    fields: Attribute[];
};

export const FormStats: FC<Props> = memo(({status, records, fields, formId, formIndex}) => {
    const cppwApi = useCppwApiContext();
    const realFields = useMemo(
        () => [
            {
                id: '1',
                name: 'fillBy',
                realName: 'fillBy',
                usage: Usage.ShortText,
                title: 'Пользователь',
                mandatory: true,
                isAdded: true,
                validators: [],
            },
            {
                id: '2',
                name: 'isFilled',
                realName: 'isFilled',
                usage: Usage.ShortText,
                title: 'Статус',
                mandatory: true,
                isAdded: true,
                validators: [],
            },
            ...fields,
        ],
        [fields]
    );
    const handleSearch = useCallback(
        (filter: Attribute[]) => {
            cppwApi && cppwApi.getFormStats(formIndex, formId, filter);
        },
        [cppwApi, formIndex, formId]
    );
    useEffect(() => {
        if (status === AsyncStatus.Initial) {
            cppwApi && cppwApi.getFormStats(formIndex, formId);
        }
        // Проставлять в зависимости всё остальное не стоит. Ничего происходить не будет, только лишний вызов.
        // eslint-disable-next-line react-hooks/exhaustive-deps
    }, [cppwApi]);

    return (
        <Layout
            leftComponent={
                <SearchColumn
                    status={AsyncStatus.Success}
                    fields={realFields}
                    isColumnsUpdateDisabled={true}
                    handleSearch={handleSearch}
                    searchType={SearchType.Questionnaire}
                />
            }
            rightComponent={
                <SearchResults
                    status={status}
                    records={records}
                    fields={realFields}
                    searchType={SearchType.Questionnaire}
                />
            }
        />
    );
});
