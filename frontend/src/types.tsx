import {Toaster} from '@blueprintjs/core';

export type AttributeRef = (HTMLTextAreaElement | null) | (HTMLInputElement | null);

export type Validator = {
    message: string;
    regexp: string;
};

export enum FormType {
    Initial = '',

    StudentRegistration = 'student_registration',
    StudentProfile = 'student_profile',
    StudentProfileTemplate = 'student_profile_template',

    WorkerRegistration = 'worker_registration',
    UserAuthorization = "authorization",

    UserRegistration = 'user_registration',
    UserProfile = 'user_profile',
    UserProfileTemplate = 'user_profile_template',

    OrgProfile = 'org_profile',
    // Не используем
    OrgProfileTemplate = 'org_profile_template',

    ProjectRequest = 'project_request',
    Project = 'project',
    Activity = 'activity',

    Questionnaire = 'questionnaire',
    Help = 'help',

    AttributeCreator = 'attribute-creator',
}

export enum Usage {
    Password = 'password',
    Email = 'email',
    ShortText = 'short_text',
    Number = 'number',
    LongText = 'long_text',
    File = 'file',
    Checkbox = 'checkbox',
    Radio = 'radio',
    Tel = 'tel',
    // Url = 'url',
    Date = 'date',
    // Time = 'time',
    SwitchTable = 'switch_table',
    Switch = 'switch',
    StudentGroup = 'student_group',
    Mentor = 'mentor',
    Activity = 'activity',
    Organisation = 'organisation',
    Student = 'student',
    Validator = 'validator',

    Stage = 'stage',
    Task = 'task',

    Variants = 'variants',
}

export type AttributeValue = any; // string | string[] | undefined | null | Task[] | Stage[] | Date | boolean | {name: string, id: string};

export type Attribute = {
    id: string;
    name: string;
    usage: Usage;
    title: string;
    mandatory: boolean;
    validators: Validator[];
    hasOtherVariant?: boolean;
    realName?: string;
    defaultValue?: AttributeValue;
    description?: string;
    placeholder?: string;
    hint?: string;
    isPlaceholder?: boolean;
    min?: number;
    max?: number;
    variants?: string[];

    isAdded?: boolean;
    modifyAllowed?: boolean;
};
export type FormStats = {
    status: AsyncStatus;
    records: Content[];
};
export type Content = {[k: string]: any};
export type Form = {
    id: string;
    type: FormType;
    title: string;
    attributes: Attribute[];
    mode: FormMode;
    description?: string;
    buttonName?: string;
    content?: Content;
    stats?: FormStats;
};

export enum UserRole {
    Supervisor = 'supervisor', // Руководитель ЦППР
    Manager = 'manager', // Менеджеры ЦППР
    AcademicManager = 'academic_manager', // Академические руководители направлений
    OfficeManager = 'office_manager', // Менеджеры учебных офисов
    Student = 'student',
    Representative = 'representative',
    Mentor = 'mentor',

    Initial = '',
}

export enum ProfileStatus {
    Active = 'active',
    Inactive = 'inactive',

    Blocked = 'Заблокированный',
}

export type UserData = {
    email: string;
    email_verified: boolean;
    exp: number;
    role: UserRole;
    userId: string;
    name: string;
    token: string;
};

/**
 * Статус запроса за данными
 */
export enum AsyncStatus {
    Initial = 'initial',
    Pending = 'pending',
    Error = 'error',
    Success = 'success',
}

export type ToasterFromContext = {current: Toaster | null};

export enum SearchType {
    Initial = '',
    Students = 'students',
    Mentors = 'mentors',
    Managers = 'managers',
    Representatives = 'representatives',
    Activities = 'activities',
    Projects = 'projects',
    Organisations = 'organisations',
    ProjectRequests = 'project_requests',

    Questionnaire = 'questionnaire',
}

export enum FormMode {
    View = 'view',
    Fill = 'fill',
    Edit = 'edit',
    Stats = 'stats',
}

export enum FormsList {
    Archive = 'archive',
    My = 'my',
}

export enum ProfileType {
    User = 'user',
    ProjectRequest = 'project_request',
    Activity = 'activity',
    Project = 'project',
    Organisation = 'organisation',
}

export type Task = {
    id: string;
    name: string;
    description: string;
    file?: null | {
        id: string;
        name: string;
        type: 'file';
    };
    isUploadable: boolean;
};
export type Stage = {
    stageNumber: number;
    id: string;
    name: string;
    description: string;
    startDate: Date;
    endDate: Date;
    hasForcedGrade: boolean;
    coefficient: number;
    tasks: Task[];
    grade?: null | {
        mentorGrade: string;
        managerGrade: string;
    };
};

export type ApiResponse = any;

export type ProfileOrSearchItem = any;

export type StateForm = Form & {errors?: string[]};

export type AttributesByUserRole = {[k in UserRole]: Attribute[]};

export enum ProjectApplicationStatus {
    Accepted = 'accepted',
    Rejected = 'rejected',
    Waiting = 'waiting',
}

export enum ProjectRequestStatus {
    Accepted = 'Принята',
    Rejected = 'Отклонена',
    Waiting = 'На рассмотрении',
}

export enum UserAction {
    Comment = 'написал(а):',
    Approve = 'утвердил(а) заявку:',
    Reject = 'отклонил(а) заявку:',
}

type ProjectRequest = {
    id: string;
    buttonName: string;
    attributes: Attribute[];
    projectNameRus: string;
    projectNameEng: string;
    projectIndividuality: 'Индивидуальный' | 'Групповой';
    projectType: 'Технический' | 'Исследовательский';
    piCourses: number[];
    pmiCourses: number[];
    padCourses: number[];
    status: 'approved' | 'rejected' | 'validating';
};

type Student = {
    id: string;
    content: Content;
    name: string;
    role: 'student';
    course: number;
    faculty: Content;
    group: string;
    email: string;
    status: 'active' | 'inactive' | 'blocked';
};

type User = {
    id: string;
    content: Content;
    name: string;
    role: 'mentor' | 'supervisor' | 'manager' | 'representative';
    organisation: Content;
    email: string;
    status: 'active' | 'inactive' | 'blocked';
};

export type CppwNotification = {
    id: string;
    from: {
        id: string;
        name: string;
        type: string;
    };
    read?: boolean;
    message: string;
    about?: {
        id: string;
        name: string;
        type: string;
    };
};

type Post = {
    id: string;
    file: {
        id: string;
        name: string;
        type: 'file';
    };
    date: string;
    text: string;
};

export type PhonePattern = {
    type: string;
    pattern: string;
    displayPattern: displayPattern[];
};

export type displayPattern = {
    pattern: string;
    newSubStr: string;
    len: number;
};
