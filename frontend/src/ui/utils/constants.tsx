import {Attribute, FormMode, FormType, PhonePattern, Usage} from 'src/types';
import {EditableStage} from 'src/ui/blocks/form-input/form-stage-input/form-stage-input';
import {EditableTask} from 'src/ui/blocks/form-input/form-stage-input/form-task-input';

export const DEFAULT_CHAT_ID = 'DEFAULT_CHAT_ID';
export const BACKEND_URL = process.env.IS_DEV ? 'http://localhost:8090' : 'http://92.242.58.179';
export const AUTH0_PARAMS_URL = process.env.IS_DEV ? 'dev-cppr' : 'cppw-prd';
export const DEFAULT_ATTRIBUTE_ID = 'ididid';
export const NEW_ATTRIBUTES_MAP: {[K in Usage]: Attribute | undefined} = {
    [Usage.ShortText]: {
        id: DEFAULT_ATTRIBUTE_ID,
        name: '',
        usage: Usage.ShortText,
        title: '',
        mandatory: false,
        validators: [],
        isPlaceholder: true,
    },
    [Usage.Email]: {
        id: DEFAULT_ATTRIBUTE_ID,
        name: '',
        usage: Usage.Email,
        title: '',
        mandatory: false,
        validators: [],
        isPlaceholder: true,
    },
    [Usage.Tel]: {
        id: DEFAULT_ATTRIBUTE_ID,
        name: '',
        usage: Usage.Tel,
        title: '',
        mandatory: false,
        validators: [],
        isPlaceholder: true,
    },
    [Usage.Password]: {
        id: DEFAULT_ATTRIBUTE_ID,
        name: '',
        usage: Usage.Password,
        title: '',
        mandatory: false,
        validators: [],
        isPlaceholder: true,
    },
    [Usage.Number]: {
        id: DEFAULT_ATTRIBUTE_ID,
        name: '',
        usage: Usage.Number,
        title: '',
        mandatory: false,
        validators: [],
        isPlaceholder: true,
    },
    [Usage.LongText]: {
        id: DEFAULT_ATTRIBUTE_ID,
        name: '',
        usage: Usage.LongText,
        title: '',
        mandatory: false,
        validators: [],
        isPlaceholder: true,
    },
    [Usage.Date]: {
        id: DEFAULT_ATTRIBUTE_ID,
        name: '',
        usage: Usage.Date,
        title: '',
        mandatory: false,
        validators: [],
        isPlaceholder: true,
    },
    [Usage.File]: {
        id: DEFAULT_ATTRIBUTE_ID,
        name: '',
        usage: Usage.File,
        title: '',
        mandatory: false,
        validators: [],
        isPlaceholder: true,
    },
    [Usage.Checkbox]: {
        id: DEFAULT_ATTRIBUTE_ID,
        name: '',
        usage: Usage.Checkbox,
        title: '',
        mandatory: false,
        validators: [],
        variants: [],
        isPlaceholder: true,
    },
    [Usage.Radio]: {
        id: DEFAULT_ATTRIBUTE_ID,
        name: '',
        usage: Usage.Radio,
        title: '',
        mandatory: false,
        validators: [],
        variants: [],
        isPlaceholder: true,
    },
    [Usage.Mentor]: {
        id: DEFAULT_ATTRIBUTE_ID,
        name: '',
        usage: Usage.Mentor,
        title: '',
        mandatory: false,
        validators: [],
        isPlaceholder: true,
    },
    [Usage.Switch]: {
        id: DEFAULT_ATTRIBUTE_ID,
        name: '',
        usage: Usage.Switch,
        title: '',
        mandatory: false,
        validators: [],
        isPlaceholder: true,
    },
    [Usage.Organisation]: {
        id: DEFAULT_ATTRIBUTE_ID,
        name: '',
        usage: Usage.Organisation,
        title: '',
        mandatory: false,
        validators: [],
        isPlaceholder: true,
    },
    [Usage.Student]: {
        id: DEFAULT_ATTRIBUTE_ID,
        name: '',
        usage: Usage.Student,
        title: '',
        mandatory: false,
        validators: [],
        isPlaceholder: true,
    },
    [Usage.SwitchTable]: {
        id: DEFAULT_ATTRIBUTE_ID,
        name: '',
        usage: Usage.SwitchTable,
        title: '',
        mandatory: false,
        validators: [],
        isPlaceholder: true,
    },

    // Не имеют возможности создания нового поля.
    [Usage.StudentGroup]: undefined,
    [Usage.Activity]: undefined,
    [Usage.Stage]: undefined,
    [Usage.Task]: undefined,
    [Usage.Variants]: undefined,
    [Usage.Validator]: undefined,

    // [Usage.Url]: undefined,
    // [Usage.Time]: undefined,
    // [Usage.Tel]: undefined,
};
export const ADD_STUDENTS_TO_ACTIVITY_ATTRIBUTES = [
    {
        id: `${DEFAULT_ATTRIBUTE_ID}${DEFAULT_ATTRIBUTE_ID}$-student`,
        name: 'student',
        usage: Usage.Student,
        title: 'ФИО студента',
        mandatory: true,
        validators: [],
        isPlaceholder: true,
    },
    {
        id: `${DEFAULT_ATTRIBUTE_ID}${DEFAULT_ATTRIBUTE_ID}$-activity`,
        name: 'activity',
        usage: Usage.Activity,
        title: 'Наименование активности',
        mandatory: true,
        validators: [],
        isPlaceholder: true,
    },
];
export const ON_OFF_PROJECT_APPLICATIONS_ATTRIBUTES = [
    {
        id: `${DEFAULT_ATTRIBUTE_ID}-activity`,
        name: 'activity',
        usage: Usage.Activity,
        title: 'Наименование активности',
        mandatory: true,
        validators: [],
        isPlaceholder: true,
    },
    {
        id: `${DEFAULT_ATTRIBUTE_ID}-student`,
        name: 'student',
        usage: Usage.Switch,
        title: 'Должна ли быть включена подача заявок для данной активности?',
        mandatory: true,
        validators: [],
        isPlaceholder: true,
    },
];
export const ELEVEN_MONTHS = 11 * 30 * 24 * 60 * 60 * 1000;
export const NEW_CHAT = {
    id: DEFAULT_CHAT_ID,
    users: [],
    name: '',
    isEditing: true,
    backup: undefined,
};
export const STAGE_ATTRIBUTES: Attribute[] = [
    {
        id: `${Usage.Stage}-stageNumber`,
        name: 'stageNumber',
        usage: Usage.Number,
        title: 'Номер этапа',
        min: 1,
        mandatory: true,
        validators: [],
    },
    {
        id: `${Usage.Stage}-name`,
        name: 'name',
        usage: Usage.ShortText,
        title: 'Наименование этапа',
        mandatory: true,
        validators: [],
    },
    {
        id: `${Usage.Stage}-description`,
        name: 'description',
        usage: Usage.LongText,
        title: 'Описание этапа',
        mandatory: false,
        validators: [],
    },
    {
        id: `${Usage.Stage}-startDate`,
        name: 'startDate',
        usage: Usage.Date,
        title: 'Дата начала этапа',
        mandatory: true,
        validators: [],
    },
    {
        id: `${Usage.Stage}-endDate`,
        name: 'endDate',
        usage: Usage.Date,
        title: 'Дата завершения этапа',
        mandatory: true,
        validators: [],
    },
    {
        id: `${Usage.Stage}-coefficient`,
        name: 'coefficient',
        usage: Usage.Number,
        title: 'Коэффициент этапа в оценке',
        mandatory: true,
        max: 1,
        min: 0,
        validators: [],
    },
    {
        id: `${Usage.Stage}-hasForcedGrade`,
        name: 'hasForcedGrade',
        usage: Usage.Switch,
        title: 'Является ли оценка ментора итоговой для данного этапа?',
        mandatory: false,
        validators: [],
    },
    {
        id: `${Usage.Stage}-tasks`,
        name: 'tasks',
        usage: Usage.Task,
        title: 'Задания этапа',
        mandatory: false,
        validators: [],
    },
];
export const TASK_ATTRIBUTES: Attribute[] = [
    {
        id: `${Usage.Task}-name`,
        name: 'name',
        usage: Usage.ShortText,
        title: 'Наименование задания',
        mandatory: true,
        validators: [],
    },
    {
        id: `${Usage.Task}-description`,
        name: 'description',
        usage: Usage.LongText,
        title: 'Описание задания',
        mandatory: false,
        validators: [],
    },
    {
        id: `${Usage.Task}-isUploadable`,
        name: 'isUploadable',
        usage: Usage.Switch,
        title: 'Нужно ли загружать в него файл?',
        mandatory: true,
        validators: [],
    },
];
export const DEFAULT_STAGE_ID = 'DEFAULT_STAGE_ID';
export const NEW_STAGE: EditableStage = {
    id: DEFAULT_STAGE_ID,
    name: '',
    description: '',
    // @ts-ignore
    startDate: undefined,
    // @ts-ignore
    endDate: undefined,
    // @ts-ignore
    coefficient: undefined,
    tasks: [],
    hasForcedGrade: false,
    isEditing: true,
};
export const DEFAULT_TASK_ID = 'DEFAULT_TASK_ID';
export const NEW_TASK: EditableTask = {
    id: DEFAULT_TASK_ID,
    name: '',
    description: '',
    isUploadable: false,
    isEditing: true,
};
export const DEFAULT_FORM_ID = 'DEFAULT_FORM_ID';
export const NEW_FORM = {
    id: DEFAULT_FORM_ID,
    type: FormType.Initial,
    title: 'Без названия',
    buttonName: '',
    attributes: [],
    mode: FormMode.Edit,
};

export const PROJECT_REQUEST_STATUS_ATTRIBUTE: Attribute = {
    id: `${DEFAULT_ATTRIBUTE_ID}-PROJECT_REQUEST_STATUS_ATTRIBUTE`,
    name: 'status',
    realName: 'status',
    usage: Usage.Radio,
    title: 'Статус',
    mandatory: true,
    validators: [],
    variants: ['Принята', 'Отклонена', 'На рассмотрении'],
    isPlaceholder: true,
    isAdded: true,
};

export const STUDENT_STATUS_ATTRIBUTE: Attribute = {
    id: `${DEFAULT_ATTRIBUTE_ID}-STUDENT_STATUS_ATTRIBUTE`,
    name: 'status',
    realName: 'status',
    usage: Usage.Radio,
    title: 'Статус',
    mandatory: true,
    validators: [],
    variants: ['Обучающийся', 'Выпускник', 'Поступающий', 'Отчисленный'],
    isPlaceholder: true,
    isAdded: true,
};
export const SET_STUDENT_STATUS_ATTRIBUTES = [
    {
        id: `${DEFAULT_ATTRIBUTE_ID}${DEFAULT_ATTRIBUTE_ID}$-student`,
        name: 'student',
        usage: Usage.Student,
        title: 'ФИО студента',
        mandatory: true,
        validators: [],
        isPlaceholder: true,
    },
    STUDENT_STATUS_ATTRIBUTE,
];

export const BLOCKED_ATTRIBUTE: Attribute = {
    id: `${DEFAULT_ATTRIBUTE_ID}-BLOCKED_ATTRIBUTE`,
    name: 'blocked',
    realName: 'blocked',
    usage: Usage.Switch,
    title: 'Заблокирован?',
    mandatory: true,
    validators: [],
    variants: [],
    isPlaceholder: true,
    isAdded: true,
};

export const VALIDATION_PATTERNS_MAP: PhonePattern[] = [
    {
        type: 'Ru',
        pattern: '^((8|\\+7)[\\- ]?)?(\\(?\\d{3,4}\\)?[\\- ]?)?[\\d\\- ]{7,10}$',
        displayPattern: [
            {
                pattern: '(\\d{3})(\\d{2})(\\d{2})',
                newSubStr: '$1-$2-$3',
                len: 7,
            },
            {
                pattern: '(\\d{1})(\\d{3})(\\d{2})(\\d{2})',
                newSubStr: '+$1 $2-$3-$4',
                len: 8,
            },
            {
                pattern: '(\\d{2})(\\d{3})(\\d{2})(\\d{2})',
                newSubStr: '+$1 $2-$3-$4',
                len: 9,
            },
            {
                pattern: '(\\d{3})(\\d{3})(\\d{2})(\\d{2})',
                newSubStr: '+$1 $2-$3-$4',
                len: 10,
            },
            {
                pattern: '(\\d{1})(\\d{3})(\\d{3})(\\d{2})(\\d{2})',
                newSubStr: '$1 ($2) $3-$4-$5',
                len: 11,
            },
            {
                pattern: '(\\d{1})(\\d{3})(\\d{3})(\\d{2})(\\d{2})',
                newSubStr: '$1 ($2) $3-$4-$5',
                len: 12,
            },
        ],
    },
];
