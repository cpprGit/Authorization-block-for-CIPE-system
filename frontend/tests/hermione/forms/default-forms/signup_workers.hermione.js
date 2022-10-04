const HOST = 'http://localhost:3000';

/// Users/arkazantseva/Documents/dev/ccpr/frontend/node_modules/@gemini-testing/webdriverio/build/lib/commands
// Запуск npx hermione (--update-refs)
describe('default-forms', function() {
    it('should screen default form for worker sign up', function() {
        return this.browser
            .url(HOST + '/for_workers')
            .waitForVisible('.form-layout')
            .assertView('plain', '.form-layout')
    });
    it('should fill and send default form for worker sign up', function() {
        return this.browser
            .url(HOST + '/for_workers')
            .waitForVisible('.form-layout')
            .setValue('.form-layout .form-input:nth-child(3) input', 'new-email')
            .assertView('plain', '.form-layout')
    });
});