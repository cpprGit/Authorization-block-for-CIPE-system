import {Attribute} from 'src/types';
import {VALIDATION_PATTERNS_MAP} from 'src/ui/utils/constants';

export const isValidatorPatternFulfilledForTel = (value: string) => {
    let isPassedSuccessfully: boolean = false;

    VALIDATION_PATTERNS_MAP.forEach((phonePattern) => {
        let pattern = new RegExp(phonePattern.pattern);

        if (pattern.test(value)) {
            isPassedSuccessfully = true;
        }
    });

    return isPassedSuccessfully
        ? ''
        : // TODO: Поддерживаемый формат брать из VALIDATION_PATTERNS_MAP
          'Введенный телефонный номер - не поддерживается. Поддерживаемый формат: +7(000)000-00-00';
};

export const isMandatoryFulfilledForTel = (value: string, {mandatory}: Attribute) => {
    if (mandatory && !value) {
        return 'Поле обязательно для заполнения.';
    }
    return '';
};
