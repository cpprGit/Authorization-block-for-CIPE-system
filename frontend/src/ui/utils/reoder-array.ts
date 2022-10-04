export const reorderArray = <T>(
    array: T[],
    from: number,
    to: number,
    length = 1
): T[] | undefined => {
    if (length === 0 || length === array.length || from === to) {
        // return an unchanged copy
        return array.slice();
    }

    if (length < 0 || length > array.length || from + length > array.length) {
        return undefined;
    }

    const before = array.slice(0, from);
    const within = array.slice(from, from + length);
    const after = array.slice(from + length);

    const result = [];
    let i = 0;
    let b = 0;
    let w = 0;
    let a = 0;

    while (i < to) {
        if (b < before.length) {
            result.push(before[b]);
            b += 1;
        } else {
            result.push(after[a]);
            a += 1;
        }
        i += 1;
    }

    while (w < length) {
        result.push(within[w]);
        w += 1;
        i += 1;
    }

    while (i < array.length) {
        if (b < before.length) {
            result.push(before[b]);
            b += 1;
        } else {
            result.push(after[a]);
            a += 1;
        }
        i += 1;
    }

    return result;
};
