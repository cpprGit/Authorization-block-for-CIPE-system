module.exports = {
    compositeImage: true,
    sets: {
        desktop: {
            files: 'tests/hermione'
        }
    },

    browsers: {
        firefox: {
            desiredCapabilities: {
                browserName: 'firefox' // this browser should be installed on your OS
            }
        }
    }
};