var exec = require('cordova/exec');

var ShopyPush = {
    // Obtener token FCM
    getToken: function(successCallback, errorCallback) {
        exec(successCallback, errorCallback, 'ShopyPush', 'getToken', []);
    },
    
    // Escuchar mensajes FCM
    onMessageReceived: function(callback) {
        exec(callback, null, 'ShopyPush', 'onMessageReceived', []);
    },
    
    // Escuchar refresh de token
    onTokenRefresh: function(callback) {
        exec(callback, null, 'ShopyPush', 'onTokenRefresh', []);
    },
    
    // Verificar si el servicio está corriendo
    isServiceRunning: function(successCallback, errorCallback) {
        exec(successCallback, errorCallback, 'ShopyPush', 'isServiceRunning', []);
    }
};

module.exports = ShopyPush;