var exec = require('cordova/exec');

module.exports = {
    // No necesitamos métodos, el plugin escucha automáticamente
    dummy: function(success, error) {
        exec(success, error, 'ShopyPush', 'dummy', []);
    }
};