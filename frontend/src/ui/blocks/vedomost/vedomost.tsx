import React, {FC, memo, useCallback, useEffect, useState} from 'react';
import {AsyncStatus, Attribute, ProfileOrSearchItem, ProfileType, SearchType} from 'src/types';
import {SearchResults} from 'src/ui/blocks/search-results/search-results';
import {useCppwApiContext} from 'src/ui/contexts/cppw-api-context/cppw-api-context';

type Props = {
    profileId: string;
    profileType: ProfileType;
};
export const Vedomost: FC<Props> = memo(({profileId, profileType}) => {
    const cppwApi = useCppwApiContext();

    const [status, setStatus] = useState(AsyncStatus.Initial);
    const [result, setResult] = useState<{
        fields: Attribute[];
        records: ProfileOrSearchItem[];
    }>({fields: [], records: []});
    const setMark = useCallback(
        (mark, recordIndex, fieldName) => {
            result.records[recordIndex][fieldName] = mark;
            setResult(result);
        },
        [result, setResult]
    );

    useEffect(() => {
        if (cppwApi && status === AsyncStatus.Initial) {
            switch (profileType) {
                case ProfileType.Activity: {
                    cppwApi &&
                        cppwApi.getActivityVedomost(
                            profileId,
                            () => {
                                setStatus(AsyncStatus.Pending);
                            },
                            (result) => {
                                setResult(result);
                                setStatus(AsyncStatus.Success);
                            },
                            () => {
                                setStatus(AsyncStatus.Error);
                            }
                        );
                    return;
                }
                case ProfileType.Project: {
                    cppwApi &&
                        cppwApi.getProjectVedomost(
                            profileId,
                            () => {
                                setStatus(AsyncStatus.Pending);
                            },
                            (result) => {
                                setResult(result);
                                setStatus(AsyncStatus.Success);
                            },
                            () => {
                                setStatus(AsyncStatus.Error);
                            }
                        );
                    return;
                }
            }
        }
        // eslint-disable-next-line react-hooks/exhaustive-deps
    }, [profileType, profileId, cppwApi, setStatus]);

    return (
        <SearchResults
            searchType={SearchType.Initial}
            status={status}
            fields={result.fields}
            records={result.records}
            setMark={setMark}
            profileId={profileId}
        />
    );
});
