import {Spinner} from '@blueprintjs/core';
import React, {FC, memo, useCallback, useEffect, useRef, useState} from 'react';
import {useDispatch, useSelector} from 'react-redux';
import {errorUserForm} from 'src/store/actions/user-forms.actions';
import {State} from 'src/store/reducers';
import {AsyncStatus, Form, FormMode, FormsList} from 'src/types';
import {Empty} from 'src/ui/blocks/empty/empty';
import {FormLayout} from 'src/ui/blocks/form-layout/form-layout';
import {FormsColumn} from 'src/ui/blocks/forms-column/forms-column';
import {Layout} from 'src/ui/blocks/layout/layout';
import {useCppwApiContext} from 'src/ui/contexts/cppw-api-context/cppw-api-context';

const getFirstSelectedFormIndex = (search: string, forms: Form[], defaultIndex: number) => {
    const selectedFormId = search && search.slice(4).slice(0, 36);

    if (selectedFormId && forms.length) {
        const tab = forms.reduce((index: number, item, ind) => {
            if (item.id === selectedFormId) {
                return ind;
            }
            return index;
        }, -1);
        if (tab !== -1) {
            window.history.replaceState(null, 'ЦППР', window.location.href.replace(search, ''));
            return tab;
        }
    }

    return defaultIndex;
};

type Props = {
    location: {
        search: string;
    };
};
export const FormsPage: FC<Props> = memo(({location}) => {
    const cppwApi = useCppwApiContext();
    const dispatch = useDispatch();

    const [currentTab, setCurrentTab] = useState(FormsList.My);
    const {status, forms} = useSelector((state: State) =>
        currentTab === FormsList.My ? state.userForms : state.userArchiveForms
    );
    const [currentFormIndex, setCurrentFormIndex] = useState(
        getFirstSelectedFormIndex(location.search, forms, 0)
    );
    const currentFormIndexRef = useRef(currentFormIndex);
    currentFormIndexRef.current = currentFormIndex;

    const onSetError = useCallback(
        (newErrors) => {
            dispatch(errorUserForm(currentFormIndex, newErrors));
        },
        [dispatch, currentFormIndex]
    );
    const onFormSubmit = useCallback(() => {}, []);

    const leftComponent = (
        <FormsColumn
            forms={forms}
            status={status}
            currentFormIndex={currentFormIndex}
            setCurrentFormIndex={setCurrentFormIndex}
            currentTab={currentTab}
            setCurrentTab={setCurrentTab}
        />
    );

    useEffect(() => {
        if (status === AsyncStatus.Initial && cppwApi) {
            if (currentTab === FormsList.My) {
                cppwApi.getUserForms();
            } else {
                cppwApi.getUserArchiveForms();
            }
        }
    }, [status, cppwApi, currentTab]);

    useEffect(() => {
        window.onbeforeunload = () => {
            if (forms.some(({mode}: {mode: FormMode}) => mode === FormMode.Edit)) {
                return 'Данные не сохранены. Точно перейти?';
            }
        };
    }, [forms]);
    useEffect(() => {
        setCurrentFormIndex(
            getFirstSelectedFormIndex(location.search, forms, currentFormIndexRef.current || 0)
        );
        // eslint-disable-next-line react-hooks/exhaustive-deps
    }, [location.search, forms]);

    switch (status) {
        case AsyncStatus.Success: {
            if (forms.length === 0) {
                return <Layout leftComponent={leftComponent} />;
            }
            const rightComponent =
                currentFormIndex >= forms.length ? (
                    <Empty />
                ) : (
                    <FormLayout
                        index={currentFormIndex}
                        onSetError={onSetError}
                        onFormSubmit={onFormSubmit}
                        {...forms[currentFormIndex]}
                    />
                );
            return <Layout leftComponent={leftComponent} rightComponent={rightComponent} />;
        }
        case AsyncStatus.Initial:
        case AsyncStatus.Pending: {
            return (
                <Layout
                    leftComponent={leftComponent}
                    rightComponent={<Spinner className='spinner-full-height' />}
                />
            );
        }
        case AsyncStatus.Error:
        default: {
            return <Layout leftComponent={leftComponent} />;
        }
    }
});
