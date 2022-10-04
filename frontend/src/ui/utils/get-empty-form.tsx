import {FormType} from 'src/types';

export const getEmptyForm = (type: FormType) => ({
    type: type,
    title: '',
    description: '',
    attributes: [],
    buttonName: '',
});
