var exec = require('cordova/exec');

var ShopyPush = {
    startBackgroundService: function(success, error) {
        exec(success, error, 'ShopyPush', 'startBackgroundService', []);
    },
    stopBackgroundService: function(success, error) {
        exec(success, error, 'ShopyPush', 'stopBackgroundService', []);
    },
    // 🔥 AGREGAR ESTE MÉTODO
    isServiceRunning: function(success, error) {
        exec(success, error, 'ShopyPush', 'isServiceRunning', []);
    }
};

module.exports = ShopyPush;