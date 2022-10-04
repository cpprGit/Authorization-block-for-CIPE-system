export const Clipboard = {
    applySelectableStyles(elem: HTMLElement) {
        elem.style.overflow = 'hidden';
        elem.style.height = '0px';
        elem.style.setProperty('-webkit-user-select', 'all');
        elem.style.setProperty('-moz-user-select', 'all');
        elem.style.setProperty('-ms-user-select', 'all');
        elem.style.setProperty('user-select', 'all');
        return elem;
    },

    copyCells(cells: string[][]) {
        const table = document.createElement('table');
        Clipboard.applySelectableStyles(table);
        for (const row of cells) {
            const tr = table.appendChild(document.createElement('tr'));
            for (const cell of row) {
                const td = tr.appendChild(document.createElement('td'));
                td.textContent = cell;
            }
        }

        const tsv = cells.map((row) => row.join('\t')).join('\n');
        return Clipboard.copyElement(table, tsv);
    },

    copyString(value: string) {
        const text = document.createElement('textarea');
        Clipboard.applySelectableStyles(text);
        text.value = value;

        return Clipboard.copyElement(text, value);
    },

    copyElement(elem: HTMLElement, plaintext?: string) {
        if (!Clipboard.isCopySupported()) {
            return false;
        }

        // must be document.body instead of document.documentElement for firefox
        document.body.appendChild(elem);
        try {
            // @ts-ignore
            window.getSelection().selectAllChildren(elem);

            if (plaintext != null) {
                // add plaintext fallback
                // http://stackoverflow.com/questions/23211018/copy-to-clipboard-with-jquery-js-in-chrome
                elem.addEventListener('copy', (e: any) => {
                    e.preventDefault();
                    const clipboardData = (e as any).clipboardData || (window as any).clipboardData;
                    if (clipboardData != null) {
                        clipboardData.setData('text', plaintext);
                    }
                });
            }

            return document.execCommand('copy');
        } catch (err) {
            return false;
        } finally {
            document.body.removeChild(elem);
        }
    },

    isCopySupported() {
        return document.queryCommandSupported != null && document.queryCommandSupported('copy');
    },
};
