import {Attribute} from 'src/types';

export const isFiniteFulfilledForNumber = (value: number) => {
    if (!Number.isFinite(value)) {
        return 'Введенное число должно быть не бесконечным.';
    }
    return '';
};
export const isMinLengthFulfilledForNumber = (value: number, {min, max}: Attribute) => {
    if (min !== undefined && min !== null && value < min) {
        return max !== undefined && max !== null
            ? `Число в поле должно быть не менее ${min} и не более ${max}.`
            : `Число в поле должно быть не менее ${min}.`;
    }
    return '';
};
export const isMaxLengthFulfilledForNumber = (value: number, {min, max}: Attribute) => {
    if (max !== undefined && max !== null && value > max) {
        return min !== undefined && min !== null
            ? `Число в поле должно быть не менее ${min} и не более ${max}.`
            : `Число в поле должно быть не более ${max}.`;
    }
    return '';
};
