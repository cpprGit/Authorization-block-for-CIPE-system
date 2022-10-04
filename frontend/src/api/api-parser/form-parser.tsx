import {ApiResponse, Attribute, Form, FormMode, FormType, Usage, Validator} from 'src/types';
import {
    checkBooleans,
    checkOptionalBooleans,
    checkOptionalNumbers,
    checkOptionalStrings,
    checkStrings,
} from './type-checkes';

const getUsage: (usage: string) => Usage = (usage) => {
    if (Object.values(Usage).includes(usage as Usage)) {
        return usage as Usage;
    }
    throw new Error(`Unknown usage ${usage}`);
};

export const getFormType: (formType: string) => FormType = (formType) => {
    if (Object.values(FormType).includes(formType as FormType)) {
        return formType as FormType;
    }
    throw new Error(`Unknown form type ${formType}`);
};

const parseValidator = ({regexp, message}: ApiResponse): Validator | undefined => {
    checkStrings([message, regexp], ['message', 'regexp']);
    try {
        return {
            message,
            regexp: regexp,
        };
    } catch (e) {
        throw Error(`Wrong attribute validator: ${e.message}`);
    }
};

export const parseAttribute = ({
    id,
    name,
    description,
    usage,
    title,
    mandatory,
    hasOtherVariant,
    hint,
    validators,
    min,
    max,
    placeholder,
    variants,
}: ApiResponse) => {
    try {
        const parsedUsage = getUsage(usage);
        checkStrings([id, name, title], ['id', 'name', 'title']);
        checkOptionalStrings([hint, placeholder], ['hint', 'placeholder', 'description']);
        checkOptionalNumbers([min, max], ['min', 'max']);
        checkBooleans([mandatory], ['mandatory']);
        checkOptionalBooleans([hasOtherVariant], ['hasOtherVariant']);

        return {
            id,
            name,
            description: description === null ? '' : description,
            usage: parsedUsage,
            title,
            hasOtherVariant,
            mandatory,
            hint: hint === null ? '' : hint,
            min: min === null ? undefined : min,
            max: max === null ? undefined : max,
            validators: validators.map(parseValidator),
            placeholder: placeholder === null ? '' : placeholder,
            variants,
            isAdded: true,
        };
    } catch (e) {
        console.error(`Wrong attribute (${name}) format: ${e.message}`);
    }
};

const parseField = ({name, title, modifyAllowed, attribute}: ApiResponse) => {
    try {
        checkStrings([name, title], ['name', 'title']);

        return {
            name,
            title,
            modifyAllowed: Boolean(modifyAllowed),
            attribute: parseAttribute(attribute),
        };
    } catch (e) {
        console.error(`Wrong attribute (${name}) format: ${e.message}`);
    }
};

export const parseAttributes = (attributes: ApiResponse) => {
    try {
        return attributes.map(parseAttribute).filter((item: Attribute | undefined) => !!item);
    } catch (e) {
        console.error(`Wrong api answer! (parseAttributes)`);
        return [];
    }
};
export const parseFields = (fields: ApiResponse) => {
    try {
        return fields
            .map(parseField)
            .filter((item: Attribute | undefined) => !!item)
            .map(
                ({
                    attribute,
                    modifyAllowed,
                    name,
                }: {
                    attribute: Attribute;
                    modifyAllowed: boolean;
                    name: string;
                }) => ({
                    ...attribute,
                    modifyAllowed,
                    realName: name,
                })
            );
    } catch (e) {
        console.error(`Wrong api answer! (parseFields)`);
        return [];
    }
};

export const parseForm = (
    {
        id,
        schemaType,
        title,
        description,
        attributes,
        fields,
        buttonName,
        schemaContent,
    }: ApiResponse,
    mode: FormMode
) => {
    try {
        checkStrings([id, title], ['id', 'title']);
        checkOptionalStrings([buttonName, description], ['buttonName', 'description']);

        return {
            id,
            type: getFormType(schemaType),
            title,
            attributes: [
                ...(fields ? parseFields(fields) : []),
                ...(attributes ? parseAttributes(attributes) : []),
            ],
            buttonName,
            description,
            errors: [],
            mode: mode,
            content: schemaContent ? JSON.parse(schemaContent) : undefined,
        };
    } catch (e) {
        console.error(`Wrong form ${id} format: ${e.message}`);
    }
};

export const parseForms = (formsFromApi: ApiResponse, mode: FormMode) => {
    if (!formsFromApi.map || typeof formsFromApi.map !== 'function') {
        console.error(`Wrong api answer! (parseForms)`);
        return [];
    }

    return formsFromApi
        .map((item: ApiResponse) => parseForm(item, mode))
        .filter((item: Form | undefined) => !!item);
};
