const PRINT_SIZE_KEY = 'bizflow_print_size';
const DEFAULT_SIZE = 'K80';

function getSavedPrintSize() {
    const stored = localStorage.getItem(PRINT_SIZE_KEY);
    return stored || DEFAULT_SIZE;
}

function setSavedPrintSize(value) {
    localStorage.setItem(PRINT_SIZE_KEY, value);
}

function syncPrintSizeInputs() {
    const current = getSavedPrintSize();
    document.querySelectorAll('input[name="printSize"]').forEach(input => {
        input.checked = input.value === current;
        input.addEventListener('change', () => {
            if (input.checked) {
                setSavedPrintSize(input.value);
            }
        });
    });
}

window.addEventListener('DOMContentLoaded', () => {
    syncPrintSizeInputs();
});
