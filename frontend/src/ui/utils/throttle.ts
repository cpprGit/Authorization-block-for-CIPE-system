export function throttle<F>(func: F, ms: number): F {
    let isThrottled = false,
        savedArgs: any = null,
        savedThis: any = null;

    function wrapper() {
        if (isThrottled) {
            // (2)
            savedArgs = arguments;
            // @ts-ignore
            savedThis = this;
            return;
        }

        // @ts-ignore
        func.apply(this, arguments); // (1)

        isThrottled = true;

        setTimeout(function () {
            isThrottled = false; // (3)
            if (savedArgs) {
                wrapper.apply(savedThis, savedArgs);
                savedArgs = savedThis = null;
            }
        }, ms);
    }

    // @ts-ignore
    return wrapper;
}
