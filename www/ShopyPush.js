var exec = require('cordova/exec');

module.exports = {
    // Método para probar la comunicación (opcional)
    testNotification: function(title, body, success, error) {
        exec(success, error, 'ShopyPush', 'testNotification', [title, body]);
    }
};