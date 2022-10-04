import {ApiResponse} from 'src/types';

const isOptionalBoolean = (val: ApiResponse) =>
    typeof val === 'boolean' || typeof val === 'undefined' || val === null;
export const checkOptionalBooleans = (vals: ApiResponse[], names: string[]) => {
    vals.forEach((val, ind) => {
        if (!isOptionalBoolean(val)) {
            throw Error(`Wrong attribute property type ${names[ind]}`);
        }
    });
};
const isBoolean = (val: ApiResponse) => typeof val === 'boolean';
export const checkBooleans = (vals: ApiResponse[], names: string[]) => {
    vals.forEach((val, ind) => {
        if (!isBoolean(val)) {
            throw Error(`Wrong attribute property type ${names[ind]}`);
        }
    });
};
const isOptionalString = (val: ApiResponse) =>
    typeof val === 'string' || typeof val === 'undefined' || val === null;
export const checkOptionalStrings = (vals: ApiResponse[], names: string[]) => {
    vals.forEach((val, ind) => {
        if (!isOptionalString(val)) {
            throw Error(`Wrong attribute property type ${names[ind]}`);
        }
    });
};
const isString = (val: ApiResponse) => typeof val === 'string';
export const checkStrings = (vals: ApiResponse[], names: string[]) => {
    vals.forEach((val, ind) => {
        if (!isString(val)) {
            throw Error(`Wrong attribute property type ${names[ind]}`);
        }
    });
};
const isOptionalNumber = (val: ApiResponse) =>
    typeof val === 'number' || typeof val === 'undefined' || val === null;
export const checkOptionalNumbers = (vals: ApiResponse[], names: string[]) => {
    vals.forEach((val, ind) => {
        if (!isOptionalNumber(val)) {
            throw Error(`Wrong attribute property type ${names[ind]}`);
        }
    });
};
const isNumber = (val: ApiResponse) => typeof val === 'number';
// eslint-disable-next-line  @typescript-eslint/no-unused-vars
const checkNumbers = (vals: ApiResponse[], names: string[]) => {
    vals.forEach((val, ind) => {
        if (!isNumber(val)) {
            throw Error(`Wrong attribute property type ${names[ind]}`);
        }
    });
};
