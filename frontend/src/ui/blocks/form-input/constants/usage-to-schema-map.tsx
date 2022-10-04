import {Form, FormMode, FormType, Usage} from 'src/types';

const NAME = 'name';
const DESCRIPTION = 'description';
const TITLE = 'title';
const PLACEHOLDER = 'placeholder';
const HINT = 'hint';
const MANDATORY = 'mandatory';

const MIN = 'min';
const MAX = 'max';
const MIN_NUMBER = 'min_number';
const MAX_NUMBER = 'max_number';
const COLUMNS = 'columns';
const ROWS = 'rows';
const HAS_OTHER_VARIANT = 'has_other_variant';

const VARIANTS = 'variants';

const VALIDATORS = 'validators';

const schemaCreator = (usage: Usage, title: string, attributeNames: string[]): Form => ({
    id: usage,
    type: FormType.AttributeCreator,
    title,
    attributes: attributeNames.map((name) => attributesCreator(name, usage)),
    buttonName: 'Сохранить',
    mode: FormMode.Fill,
});
const nameAttributeCreator = (usage: Usage) => ({
    id: `${usage}-${NAME}`,
    name: NAME,
    usage: Usage.ShortText,
    title: 'Наименование данного поля',
    mandatory: true,
    hint: 'Должно быть уникальным для системы и отражать сущность поля.',
    validators: [],
});
const descriptionAttributeCreator = (usage: Usage) => ({
    id: `${usage}-${DESCRIPTION}`,
    name: DESCRIPTION,
    usage: Usage.ShortText,
    title: 'Описание данного поля',
    mandatory: false,
    hint: 'Видно только при создании/редактировании форм. Должно отражать смысл поля.',
    validators: [],
});
const titleAttributeCreator = (usage: Usage) => ({
    id: `${usage}-${TITLE}`,
    name: TITLE,
    usage: Usage.ShortText,
    title: 'Заголовок поля',
    mandatory: true,
    validators: [],
});
const placeholderAttributeCreator = (usage: Usage) => ({
    id: `${usage}-${PLACEHOLDER}`,
    name: PLACEHOLDER,
    usage: Usage.ShortText,
    title: 'Пример вводимого значения',
    mandatory: false,
    hint: 'Будет отображен как плейсхолдер в строке ввода.',
    validators: [],
});
const hintAttributeCreator = (usage: Usage) => ({
    id: `${usage}-${HINT}`,
    name: HINT,
    usage: Usage.ShortText,
    title: 'Текст подсказки',
    mandatory: false,
    hint: 'Этот текст будет отображаться при наведении на иконку ?.',
    validators: [],
});
const mandatoryAttributeCreator = (usage: Usage) => ({
    id: `${usage}-${MANDATORY}`,
    name: MANDATORY,
    usage: Usage.Switch,
    title: 'Обязательное ли поле',
    mandatory: true,
    validators: [],
});
const minAttributeCreator = (usage: Usage) => ({
    id: `${usage}-${MIN}`,
    name: MIN,
    usage: Usage.Number,
    title: 'Минимальное кол-во символов во вводимой строке',
    mandatory: false,
    validators: [],
});
const minNumberAttributeCreator = (usage: Usage) => ({
    id: `${usage}-${MIN}`,
    name: MIN,
    usage: Usage.Number,
    title: 'Минимальное вводимое значение',
    mandatory: false,
    validators: [],
});
const maxAttributeCreator = (usage: Usage) => ({
    id: `${usage}-${MAX}`,
    name: MAX,
    usage: Usage.Number,
    title: 'Максимальное кол-во символов во вводимой строке',
    mandatory: false,
    validators: [],
});
const maxNumberAttributeCreator = (usage: Usage) => ({
    id: `${usage}-${MAX}`,
    name: MAX,
    usage: Usage.Number,
    title: 'Максимальное значение значение',
    mandatory: false,
    validators: [],
});
const variantsAttributeCreator = (usage: Usage) => ({
    id: `${usage}-${VARIANTS}`,
    name: VARIANTS,
    usage: Usage.Variants,
    title: 'Варианты ответа',
    mandatory: true,
    validators: [],
});
const validatorsAttributeCreator = (usage: Usage) => ({
    id: `${usage}-${VALIDATORS}`,
    name: VALIDATORS,
    usage: Usage.Validator,
    title: 'Валидаторы',
    mandatory: false,
    validators: [],
});
const rowsAttributeCreator = (usage: Usage) => ({
    id: `${usage}-${ROWS}`,
    name: ROWS,
    usage: Usage.Variants,
    title: 'Значения по Строке',
    mandatory: true,
    validators: [],
});
const columnsAttributeCreator = (usage: Usage) => ({
    id: `${usage}-${COLUMNS}`,
    name: COLUMNS,
    usage: Usage.Variants,
    title: 'Значения по Столбцу',
    mandatory: true,
    validators: [],
});
const hasOtherVariantAttributeCreator = (usage: Usage) => ({
    id: `${usage}-${HAS_OTHER_VARIANT}`,
    name: HAS_OTHER_VARIANT,
    usage: Usage.Switch,
    title: 'Имеется ли поле со свободным ответом?',
    mandatory: true,
    validators: [],
});

const attributesCreator = (name: string, usage: Usage) => {
    switch (name) {
        case NAME: {
            return nameAttributeCreator(usage);
        }
        case DESCRIPTION: {
            return descriptionAttributeCreator(usage);
        }
        case TITLE: {
            return titleAttributeCreator(usage);
        }
        case PLACEHOLDER: {
            return placeholderAttributeCreator(usage);
        }
        case MANDATORY: {
            return mandatoryAttributeCreator(usage);
        }
        case HINT: {
            return hintAttributeCreator(usage);
        }
        case MIN: {
            return minAttributeCreator(usage);
        }
        case MAX: {
            return maxAttributeCreator(usage);
        }
        case MIN_NUMBER: {
            return minNumberAttributeCreator(usage);
        }
        case MAX_NUMBER: {
            return maxNumberAttributeCreator(usage);
        }
        case VARIANTS: {
            return variantsAttributeCreator(usage);
        }
        case VALIDATORS: {
            return validatorsAttributeCreator(usage);
        }
        case ROWS: {
            return rowsAttributeCreator(usage);
        }
        case COLUMNS: {
            return columnsAttributeCreator(usage);
        }
        case HAS_OTHER_VARIANT: {
            return hasOtherVariantAttributeCreator(usage);
        }
        default: {
            return nameAttributeCreator(usage);
        }
    }
};

// name совпадает с НАЗВАНИЕМ ИТОГОВОГО ПОЛЯ
export const USAGE_TO_FORM_MAP: {[k in Usage]: Form | undefined} = {
    [Usage.Email]: schemaCreator(Usage.Email, 'Редактирование поля E-mail', [
        NAME,
        DESCRIPTION,
        TITLE,
        MANDATORY,
        PLACEHOLDER,
        HINT,
        VALIDATORS,
    ]),
    [Usage.Tel]: schemaCreator(Usage.Tel, 'Редактирование поля Телефон', [
        NAME,
        DESCRIPTION,
        TITLE,
        MANDATORY,
        HINT,
        VALIDATORS,
    ]),
    [Usage.ShortText]: schemaCreator(Usage.ShortText, 'Редактирование поля Короткий текст', [
        NAME,
        DESCRIPTION,
        TITLE,
        MANDATORY,
        PLACEHOLDER,
        HINT,
        MIN,
        MAX,
        VALIDATORS,
    ]),
    [Usage.Number]: schemaCreator(Usage.Number, 'Редактирование числового поля', [
        NAME,
        DESCRIPTION,
        TITLE,
        MANDATORY,
        PLACEHOLDER,
        HINT,
        MIN_NUMBER,
        MAX_NUMBER,
    ]),
    [Usage.Checkbox]: schemaCreator(Usage.Checkbox, 'Редактирование поля с множественным выбором', [
        NAME,
        DESCRIPTION,
        TITLE,
        MANDATORY,
        HINT,
        HAS_OTHER_VARIANT,
        VARIANTS,
    ]),
    [Usage.Password]: schemaCreator(Usage.Password, 'Редактирование поля c паролем', [
        NAME,
        DESCRIPTION,
        TITLE,
        MANDATORY,
        HINT,
        PLACEHOLDER,
        MIN,
        MAX,
        VALIDATORS,
    ]),
    [Usage.LongText]: schemaCreator(Usage.LongText, 'Редактирование поля Длинный текст', [
        NAME,
        DESCRIPTION,
        TITLE,
        MANDATORY,
        HINT,
        PLACEHOLDER,
        MIN,
        MAX,
        VALIDATORS,
    ]),
    [Usage.File]: schemaCreator(Usage.File, 'Редактирование файлового поля', [
        NAME,
        DESCRIPTION,
        TITLE,
        MANDATORY,
        HINT,
        PLACEHOLDER,
    ]),
    [Usage.Radio]: schemaCreator(Usage.Radio, 'Редактирование поля с единичным выбором', [
        NAME,
        DESCRIPTION,
        TITLE,
        MANDATORY,
        HINT,
        HAS_OTHER_VARIANT,
        VARIANTS,
    ]),
    [Usage.Date]: schemaCreator(Usage.Date, 'Редактирование поля с датой', [
        NAME,
        DESCRIPTION,
        TITLE,
        MANDATORY,
        HINT,
    ]),
    [Usage.Switch]: schemaCreator(Usage.Switch, 'Редактирование поля с выбором Да/Нет', [
        NAME,
        DESCRIPTION,
        TITLE,
        MANDATORY,
        HINT,
    ]),
    [Usage.Mentor]: schemaCreator(Usage.Mentor, 'Редактирование поля с выбором ментора', [
        NAME,
        DESCRIPTION,
        TITLE,
        MANDATORY,
        HINT,
        PLACEHOLDER,
    ]),
    [Usage.Activity]: schemaCreator(Usage.Activity, 'Редактирование поля с выбором активности', [
        NAME,
        DESCRIPTION,
        TITLE,
        MANDATORY,
        HINT,
    ]),
    [Usage.StudentGroup]: schemaCreator(
        Usage.StudentGroup,
        'Редактирование поля с группы студента',
        [NAME, DESCRIPTION, TITLE, MANDATORY, HINT, PLACEHOLDER]
    ),
    [Usage.Stage]: schemaCreator(Usage.Stage, 'Редактирование поля создания этапов', [
        NAME,
        DESCRIPTION,
        TITLE,
        MANDATORY,
        HINT,
    ]),
    [Usage.Organisation]: schemaCreator(Usage.Stage, 'Редактирование поля ввода организации', [
        NAME,
        DESCRIPTION,
        TITLE,
        MANDATORY,
        HINT,
    ]),
    [Usage.Student]: schemaCreator(Usage.Stage, 'Редактирование поля ввода студента', [
        NAME,
        DESCRIPTION,
        TITLE,
        MANDATORY,
        HINT,
    ]),
    [Usage.SwitchTable]: schemaCreator(Usage.SwitchTable, 'Редактирование поля Таблица выбора', [
        NAME,
        DESCRIPTION,
        TITLE,
        MANDATORY,
        HINT,
        COLUMNS,
        ROWS,
    ]),

    // Мои системные
    [Usage.Task]: undefined,
    [Usage.Variants]: undefined,
    [Usage.Validator]: undefined,
};
