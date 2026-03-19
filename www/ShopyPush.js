var exec = require('cordova/exec');

module.exports = {
    isServiceRunning: function(success, error) {
        exec(success, error, 'ShopyPush', 'isServiceRunning', []);
    }
};