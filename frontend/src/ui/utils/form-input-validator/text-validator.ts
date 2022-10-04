import {Attribute} from 'src/types';

export const isMandatoryFulfilledForText = (value: string, {mandatory}: Attribute) => {
    if (mandatory && !value) {
        return 'Поле обязательно для заполнения.';
    }
    return '';
};
export const isMinLengthFulfilledForText = (value: string, {min, max}: Attribute) => {
    if (min !== undefined && min !== null && value.length < min) {
        return max !== undefined && max !== null
            ? `В поле должно быть не менее ${min} и не более ${max} символов.`
            : `В поле должно быть не менее ${min} символов.`;
    }
    return '';
};
export const isMaxLengthFulfilledForText = (value: string, {min, max}: Attribute) => {
    if (max !== undefined && max !== null && value.length > max) {
        return min !== undefined && min !== null
            ? `В поле должно быть не менее ${min} и не более ${max} символов.`
            : `В поле должно быть не более ${max} символов.`;
    }
    return '';
};
export const isValidatorsFulfilledForText = (value: string, {validators}: Attribute) => {
    if (validators.length) {
        const validatorsWithError = validators.filter((validator) => {
            return !new RegExp(validator.regexp).test(value);
        });

        if (validatorsWithError.length) {
            return validatorsWithError[0].message;
        }
    }
    return '';
};
export const isEmptyForText = (value: string, {mandatory}: Attribute) => !value && !mandatory;
