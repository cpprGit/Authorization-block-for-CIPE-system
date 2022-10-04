import React, {FC, memo} from 'react';
import {useSelector} from 'react-redux';
import {State} from 'src/store/reducers';
import {Layout} from 'src/ui/blocks/layout/layout';
import {SearchColumn} from 'src/ui/blocks/search-column/search-column';
import {SearchResults} from 'src/ui/blocks/search-results/search-results';

export const SearchPage: FC = memo(() => {
    const {searchFilterStatus, searchStatus, fields, records, searchType} = useSelector(
        (state: State) => state.search
    );
    return (
        <Layout
            leftComponent={
                <SearchColumn status={searchFilterStatus} fields={fields} searchType={searchType} />
            }
            rightComponent={
                <SearchResults
                    status={searchStatus}
                    records={records}
                    fields={fields}
                    searchType={searchType}
                />
            }
        />
    );
});
