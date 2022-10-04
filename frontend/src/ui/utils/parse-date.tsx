export const parseDate = (date: Date) =>
    date.toLocaleString('ru', {year: 'numeric', month: 'long', day: 'numeric'});
