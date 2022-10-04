import {Card, H3} from '@blueprintjs/core';
import React, {FC, memo, useCallback, useMemo, useState} from 'react';
import {useDispatch, useSelector} from 'react-redux';
import {addDraftFormAttributes} from 'src/store/actions/user-forms.actions';
import {State} from 'src/store/reducers';
import {AsyncStatus, Attribute, Usage} from 'src/types';
import {AttributeCard} from 'src/ui/blocks/attribute-card/attribute-card';
import {Search} from 'src/ui/blocks/search/search';
import {useCppwApiContext} from 'src/ui/contexts/cppw-api-context/cppw-api-context';
import {NEW_ATTRIBUTES_MAP} from 'src/ui/utils/constants';
import './attribute-creator.styl';

type Props = {
    className?: string;
    formIndex: number;
};

export const AttributeCreator: FC<Props> = memo(({className, formIndex}) => {
    const dispatch = useDispatch();
    const cppwApi = useCppwApiContext();
    const attributes = useSelector((state: State) => state.attributes);
    const [usage, setUsage] = useState<Usage | ''>('');
    const items: any = useMemo(() => {
        // @ts-ignore
        const currAttributes = attributes[usage];
        return usage && currAttributes && currAttributes.attributes
            ? currAttributes.attributes.map((attribute: Attribute) => ({
                  ...attribute,
                  onClick: () => {
                      dispatch(addDraftFormAttributes(formIndex, [attribute]));
                  },
              }))
            : [];
    }, [usage, attributes, dispatch, formIndex]);
    const onUsageSelected = useCallback(
        (e) => {
            if (e.target && e.target.value && e.target.value !== usage) {
                cppwApi && cppwApi.getAttributesByUsage(e.target.value as Usage);
                setUsage(e.target.value as Usage);
            }
        },
        [cppwApi, setUsage, usage]
    );

    return (
        <Card className={className}>
            <H3 className='form-layout__title'>Добавление поля</H3>
            <div className='bp3-select bp3-fill'>
                <select onChange={onUsageSelected} value={usage}>
                    <option value=''>Выберите тип поля</option>
                    <option value={Usage.ShortText}>Короткий текст</option>
                    <option value={Usage.LongText}>Длинный текст</option>
                    <option value={Usage.Email}>E-mail</option>
                    <option value={Usage.Password}>Пароль</option>
                    <option value={Usage.Number}>Число</option>
                    <option value={Usage.File}>Файл</option>
                    <option value={Usage.Checkbox}>Множественный выбор</option>
                    <option value={Usage.Radio}>Единичный выбор</option>
                    <option value={Usage.Date}>Дата</option>
                    <option value={Usage.SwitchTable}>Таблица с выбором</option>
                    <option value={Usage.Tel}>Телефон</option>

                    <option value={Usage.Switch}>Да/Нет</option>
                    <option value={Usage.StudentGroup}>Группа</option>
                    <option value={Usage.Mentor}>Ментор</option>
                    <option value={Usage.Student}>Студент</option>
                    <option value={Usage.Activity}>Активность</option>
                    <option value={Usage.Organisation}>Организация</option>
                </select>
            </div>
            {
                <div className='attribute-creator__scrollable'>
                    {usage &&
                        attributes[usage]?.status === AsyncStatus.Success &&
                        attributes[usage] && (
                            <Search
                                component={AttributeCard}
                                searchPropertyName='name'
                                items={items}
                                className='side-margin-search'
                            />
                        )}

                    {usage && NEW_ATTRIBUTES_MAP[usage] && (
                        <AttributeCard
                            {...NEW_ATTRIBUTES_MAP[usage]}
                            onClick={() => {
                                const newAttribute = NEW_ATTRIBUTES_MAP[usage];
                                newAttribute &&
                                    dispatch(addDraftFormAttributes(formIndex, [newAttribute]));
                            }}
                        />
                    )}
                </div>
            }
        </Card>
    );
});
