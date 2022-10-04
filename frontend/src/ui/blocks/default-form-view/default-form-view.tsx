import {Spinner} from '@blueprintjs/core';
import React, {FC, memo, useCallback, useEffect, useState} from 'react';
import {useSelector} from 'react-redux';
import {State} from 'src/store/reducers';
import {AsyncStatus, Attribute, AttributeValue, FormType, Usage} from 'src/types';
import {ErrorView} from 'src/ui/blocks/error-view/error-view';
import {FormLayout} from 'src/ui/blocks/form-layout/form-layout';
import {SuccessView} from 'src/ui/blocks/success-view/success-view';
import {useCppwApiContext} from 'src/ui/contexts/cppw-api-context/cppw-api-context';

type Props = {
    formType: FormType;
    onSubmit: (req: {[k: string]: AttributeValue}, onSubmit: () => void) => void;
    successTitle: string;
    errorTitle: string;
};

export const DefaultFormView: FC<Props> = memo(({formType, onSubmit, successTitle, errorTitle}) => {
    const cppwApi = useCppwApiContext();
    const form = useSelector((state: State) => state.defaultForms[formType]);
    const [errors, setErrors] = useState();
    const [sentStatus, setSentStatus] = useState(false);
    const onRetry = useCallback(() => {
        cppwApi && cppwApi.getDefaultForm(formType);
    }, [cppwApi, formType]);
    const onRetryFilling = useCallback(() => {
        setSentStatus(false);
        setErrors(undefined);
    }, [setSentStatus, setErrors]);
    const wrappedOnSubmit = useCallback(
        (values, attributes) => {
            const req = attributes.reduce(
                (res: {[k: string]: AttributeValue}, attribute: Attribute, index: number) => {
                    const {name, usage, realName} = attribute;
                    const value = values[index];

                    if (realName) {
                        // поле статическое
                        if (usage === Usage.Number) {
                            res[realName] = parseFloat(value);
                        } else if (usage === Usage.Checkbox) {
                            res[realName] = JSON.stringify(value);
                        } else {
                            res[realName] = value;
                        }
                        return res;
                    } else {
                        // поле динамическое
                        res.schemaContent[name] = value;
                        return res;
                    }
                },
                {schemaContent: {}}
            );
            onSubmit(req, () => {
                setSentStatus(true);
            });
        },
        [onSubmit, setSentStatus]
    );

    useEffect(() => {
        if (!form) {
            cppwApi && cppwApi.getDefaultForm(formType);
        }
    }, [cppwApi, form, formType]);

    if (sentStatus) {
        return <>{<SuccessView title={successTitle} onRetry={onRetryFilling} />}</>;
    }
    return (
        <>
            {form && form.status === AsyncStatus.Pending && <Spinner />}
            {form && form.status === AsyncStatus.Error && (
                <ErrorView
                    title={errorTitle}
                    subtitle='Нажмите для повторного запроса.'
                    onRetry={onRetry}
                />
            )}
            {form && form.status === AsyncStatus.Success && (
                <FormLayout
                    {...form.form}
                    index={-1}
                    onFormSubmit={wrappedOnSubmit}
                    errors={errors}
                    onSetError={setErrors}
                />
            )}
        </>
    );
});
