import {Icon} from '@blueprintjs/core';
import React from 'react';
import {Link} from 'react-router-dom';
import {AttributeValue, displayPattern, ProfileOrSearchItem, SearchType, Usage} from 'src/types';
import {BACKEND_URL, VALIDATION_PATTERNS_MAP} from 'src/ui/utils/constants';
import {parseDate} from 'src/ui/utils/parse-date';

const getFormattedValue = (value: AttributeValue, usage: Usage) => {
    if (usage === Usage.Date) {
        return value ? parseDate(new Date(value)) : '';
    }
    if (usage === Usage.Switch) {
        if (typeof value !== 'boolean') {
            return '-';
        }
        return value ? 'да' : 'нет';
    }
    if (usage === Usage.Checkbox) {
        if (typeof value === 'string') {
            try {
                return JSON.parse(value).join(', ');
            } catch (e) {
                return value;
            }
        } else {
            if (Array.isArray(value)) {
                return value.join(', ');
            }
        }
    }
    if (usage === Usage.Tel) {
        value = value.split('-').join('').split('(').join('').split(')').join('');
        let displayPatterns: displayPattern[] = [];
        VALIDATION_PATTERNS_MAP.forEach((phonePattern) => {
            let pattern = new RegExp(phonePattern.pattern);

            if (pattern.test(value)) {
                displayPatterns = phonePattern.displayPattern;
            }
        });
        displayPatterns.forEach((displayPattern) => {
            if (value.length === displayPattern.len) {
                value = value.replace(new RegExp(displayPattern.pattern), displayPattern.newSubStr);
            }
        });
    }
    return value;
};

export const getFormattedStringValue = (value: AttributeValue, usage: Usage): string => {
    if (Array.isArray(value)) {
        if (!value.length) {
            return '-';
        }
        return getFormattedStringValue(value[value.length - 1], usage);
    }
    if (typeof value === 'object') {
        if (!(value && 'name' in value)) {
            return '-';
        }
        return value.name;
    }
    return getFormattedValue(value, usage);
};

export const getFormattedStringOrLinkValue = (value: AttributeValue, usage: Usage): any => {
    if (value && typeof value === 'object' && (usage === Usage.File || value.type === 'file')) {
        if (value && 'name' in value && 'id' in value) {
            return (
                <a
                    href={`${BACKEND_URL}/api/v1/${value.type || 'file'}/${value.id}`}
                    download={true}
                >
                    {value.name}
                </a>
            );
        }
    }
    if (value && usage === Usage.Tel) {
        value = getFormattedValue(value, usage);
        return <a href={'tel:' + value}> {value} </a>;
    }
    if (value && Array.isArray(value) && !value.length) {
        return '-';
    }
    if (value && Array.isArray(value)) {
        return (
            <>
                {value.map((val, index) => {
                    return (
                        <>
                            {getFormattedStringOrLinkValue(val, usage)}
                            {index !== value.length - 1 &&
                                (usage === Usage.Organisation ? (
                                    <Icon icon='chevron-right' />
                                ) : (
                                    ', '
                                ))}
                        </>
                    );
                })}
            </>
        );
    }
    if (typeof value === 'object') {
        if (value && 'name' in value && 'type' in value && 'id' in value) {
            return <Link to={`/${value.type || value.type}?id=${value.id}`}>{value.name}</Link>;
        }
    }
    return getFormattedStringValue(value, usage);
};
export const getLinkOrText = (
    item: ProfileOrSearchItem,
    searchType: SearchType,
    field: string,
    usage: Usage
) => {
    const value =
        field in item
            ? item[field]
            : 'content' in item && field in item.content
            ? item.content[field]
            : '-';

    return getFormattedStringOrLinkValue(value, usage);
};
export const getText = (
    item: ProfileOrSearchItem,
    searchType: SearchType,
    field: string,
    usage: Usage
) => {
    const value =
        field in item
            ? item[field]
            : 'content' in item && field in item.content
            ? item.content[field]
            : '-';

    return getFormattedStringValue(value, usage);
};
