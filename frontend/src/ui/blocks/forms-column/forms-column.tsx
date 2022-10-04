import {Classes, Spinner, Tab, Tabs} from '@blueprintjs/core';

import React, {FC, useCallback, useMemo} from 'react';
import {useDispatch} from 'react-redux';
import {addNewUserForm} from 'src/store/actions/user-forms.actions';
import {AsyncStatus, Form, FormMode, FormsList} from 'src/types';
import {ErrorView} from 'src/ui/blocks/error-view/error-view';

import {FormCard} from 'src/ui/blocks/form-card/form-card';
import {Search} from 'src/ui/blocks/search/search';
import {useCppwApiContext} from 'src/ui/contexts/cppw-api-context/cppw-api-context';
import {NEW_FORM} from 'src/ui/utils/constants';
import './forms-column.styl';

type Props = {
    forms: Form[];
    status: AsyncStatus;
    currentFormIndex: number;
    setCurrentFormIndex: (index: number) => void;
    currentTab: FormsList;
    setCurrentTab: (index: FormsList) => void;
};
export const FormsColumn: FC<Props> = ({
    forms,
    status,
    currentFormIndex,
    setCurrentFormIndex,
    currentTab,
    setCurrentTab,
}) => {
    const dispatch = useDispatch();
    const cppwApi = useCppwApiContext();
    const items = useMemo(
        () =>
            forms.map((form: Form, index: number) => ({
                isActive: currentFormIndex === index,
                isEditing: form.mode === FormMode.Edit,
                isArchive: currentTab === FormsList.Archive,
                index,
                title: form.title,
                form,
                onClick: () => {
                    setCurrentFormIndex(index);
                },
            })),
        [forms, setCurrentFormIndex, currentTab, currentFormIndex]
    );

    const onRetry = useCallback(() => {
        if (cppwApi) {
            if (currentTab === FormsList.My) {
                cppwApi.getUserForms();
            } else {
                cppwApi.getUserArchiveForms();
            }
        }
    }, [currentTab, cppwApi]);
    const onCreateNewForm = useCallback(() => {
        dispatch(addNewUserForm(NEW_FORM));
        setCurrentFormIndex(forms.length);
    }, [dispatch, forms.length, setCurrentFormIndex]);
    const onSelectTab = useCallback(
        (val) => {
            setCurrentTab(String(val) as FormsList);
        },
        [setCurrentTab]
    );

    return (
        <>
            <Tabs
                id='forms-column__navbar'
                className='forms-column__navbar'
                selectedTabId={currentTab}
                onChange={onSelectTab}
                large={true}
            >
                <Tab key={FormsList.My} id={FormsList.My} title='Активные формы' />
                <Tab key={FormsList.Archive} id={FormsList.Archive} title='Архив форм' />
            </Tabs>
            <div className={`${Classes.FILL} scrollable-tabs`}>
                <Search searchPropertyName='title' component={FormCard} items={items} />
                {(status === AsyncStatus.Pending || status === AsyncStatus.Initial) && (
                    <Spinner className='spinner-full-width' />
                )}
                {status === AsyncStatus.Error && (
                    <ErrorView
                        title='Ошибка загрузки списка форм.'
                        subtitle='Нажмите для повторного запроса.'
                        onRetry={onRetry}
                        size='s'
                    />
                )}
                {currentTab === FormsList.My && status === AsyncStatus.Success && (
                    <FormCard key={'last'} isPlaceholder={true} onClick={onCreateNewForm} />
                )}
            </div>
        </>
    );
};
