var exec = require('cordova/exec');

var ShopyPush = {
    // Registrar token FCM
    getToken: function(successCallback, errorCallback) {
        exec(successCallback, errorCallback, 'ShopyPush', 'getToken', []);
    },
    
    // Verificar permisos
    checkPermissions: function(successCallback, errorCallback) {
        exec(successCallback, errorCallback, 'ShopyPush', 'checkPermissions', []);
    },
    
    // Solicitar permisos (Android 13+)
    requestPermissions: function(successCallback, errorCallback) {
        exec(successCallback, errorCallback, 'ShopyPush', 'requestPermissions', []);
    },
    
    // Registrar listener para notificaciones
    onNotification: function(successCallback, errorCallback) {
        exec(successCallback, errorCallback, 'ShopyPush', 'onNotification', []);
    },
    
    // Registrar listener para token refresh
    onTokenRefresh: function(successCallback, errorCallback) {
        exec(successCallback, errorCallback, 'ShopyPush', 'onTokenRefresh', []);
    },
    
    // Verificar si el servicio está activo
    isServiceActive: function(successCallback, errorCallback) {
        exec(successCallback, errorCallback, 'ShopyPush', 'isServiceActive', []);
    },
    
    // Verificar versión de Android
    getAndroidVersion: function(successCallback, errorCallback) {
        exec(successCallback, errorCallback, 'ShopyPush', 'getAndroidVersion', []);
    },
    
    // Abrir configuración de la app
    openAppSettings: function(successCallback, errorCallback) {
        exec(successCallback, errorCallback, 'ShopyPush', 'openAppSettings', []);
    },
    
    // Limpiar todas las notificaciones
    clearAllNotifications: function(successCallback, errorCallback) {
        exec(successCallback, errorCallback, 'ShopyPush', 'clearAllNotifications', []);
    }
};

module.exports = ShopyPush;