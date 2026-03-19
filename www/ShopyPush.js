var exec = require('cordova/exec');

module.exports = {
    startBackgroundService: function(success, error) {
        exec(success, error, 'ShopyPush', 'startBackgroundService', []);
    },
    stopBackgroundService: function(success, error) {
        exec(success, error, 'ShopyPush', 'stopBackgroundService', []);
    }
};