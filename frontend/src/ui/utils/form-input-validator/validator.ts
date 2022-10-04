import {Attribute, AttributeValue, Usage} from 'src/types';
import {
    isFiniteFulfilledForNumber,
    isMaxLengthFulfilledForNumber,
    isMinLengthFulfilledForNumber,
} from './number-validator';
import {isMandatoryFulfilledForTel, isValidatorPatternFulfilledForTel} from './tel-validator';
import {
    isEmptyForText,
    isMandatoryFulfilledForText,
    isMaxLengthFulfilledForText,
    isMinLengthFulfilledForText,
    isValidatorsFulfilledForText,
} from './text-validator';

export const validateAllAttributes = (attributes: Attribute[], values: AttributeValue[]) =>
    attributes.map((attribute, i) => {
        switch (attribute.usage) {
            case Usage.Password:
            case Usage.ShortText:
            case Usage.LongText: {
                const value = (values[i] || '').toString().trim();
                const isEmpty = isEmptyForText(value, attribute);
                if (isEmpty) {
                    return '';
                }
                return (
                    isMandatoryFulfilledForText(value, attribute) ||
                    isMinLengthFulfilledForText(value, attribute) ||
                    isMaxLengthFulfilledForText(value, attribute) ||
                    isValidatorsFulfilledForText(value, attribute)
                );
            }
            case Usage.File: {
                const value = values[i];
                if (attribute.mandatory && !value) {
                    return 'Поле обязательно для заполнения.';
                }
                return '';
            }
            case Usage.Email: {
                const value = (values[i] || '').toString().trim();
                if (isEmptyForText(value, attribute)) {
                    return '';
                }
                return (
                    isMandatoryFulfilledForText(value, attribute) ||
                    isValidatorsFulfilledForText(value, attribute)
                );
            }
            case Usage.Tel: {
                const value = values[i];
                if (!attribute.mandatory && !value) {
                    return '';
                }
                return (
                    isMandatoryFulfilledForTel(value, attribute) ||
                    isValidatorPatternFulfilledForTel(value)
                );
            }
            case Usage.Activity:
            case Usage.StudentGroup: {
                const value = (values[i] || '').toString().trim();
                if (isEmptyForText(value, attribute)) {
                    return '';
                }
                return isMandatoryFulfilledForText(value, attribute);
            }
            case Usage.Stage:
            case Usage.SwitchTable:
            case Usage.Checkbox: {
                const value = values[i];
                if (attribute.mandatory && !value.length) {
                    return 'Поле обязательно для заполнения.';
                }
                return '';
            }
            case Usage.Radio: {
                const value = values[i];
                if (attribute.mandatory && !value) {
                    return 'Поле обязательно для заполнения.';
                }
                return '';
            }
            case Usage.Date:
            case Usage.Organisation:
            case Usage.Student:
            case Usage.Mentor: {
                const value = values[i];
                if (attribute.mandatory && !value) {
                    return 'Поле обязательно для заполнения.';
                }
                return '';
            }
            case Usage.Number: {
                const value = parseFloat(values[i]);
                const isEmpty = isEmptyForText(values[i].toString().trim(), attribute);
                if (isEmpty) {
                    return '';
                }
                return (
                    isMandatoryFulfilledForText(values[i].toString().trim(), attribute) ||
                    isFiniteFulfilledForNumber(value) ||
                    isMinLengthFulfilledForNumber(value, attribute) ||
                    isMaxLengthFulfilledForNumber(value, attribute)
                );
            }
            case Usage.Switch:
            case Usage.Validator:
            case Usage.Task:
            case Usage.Variants:
            default: {
                return '';
            }
        }
    });
