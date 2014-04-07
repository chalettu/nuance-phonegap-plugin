
var NuanceSpeechKitPlugin = function() {
};

// **Initialize speech kit**
//
// `credentialClassName`  The class name to be loaded to retrieve the app id and key  
// `serverName`  The hostname of the server to connect to  
// `port`  The port number for connection  
// `sslEnabled`  True if SSL is enabled for the connection  
// `successCallback`  The callback function for success  
// `failureCallback`  The callback function for error  
NuanceSpeechKitPlugin.prototype.initialize = function(credentialClassName,
                                                      serverName, port, sslEnabled,
                                                      successCallback, failureCallback) {
    return Cordova.exec( successCallback,
                         failureCallback,
                         "PhoneGapSpeechPlugin",
                         "initSpeechKit",
                         [credentialClassName, serverName, port, sslEnabled]);
};

// **Clean up speech kit**
//
// `successCallback` The callback function for success  
// `failureCallback` The callback function for error  
NuanceSpeechKitPlugin.prototype.cleanup = function(successCallback, failureCallback) {
    return Cordova.exec(successCallback,
                         failureCallback,
                         "PhoneGapSpeechPlugin",
                         "cleanupSpeechKit",
                         []);
};

// **Starts speech recognition**
//
// `recoType`  Type of recognition (dictation or websearch)  
// `language`  Language code for recognition  
// `successCallback`  The callback function for success  
// `failureCallback`  The callback function for error  
NuanceSpeechKitPlugin.prototype.startRecognition = function(recoType, language,
                                                            successCallback, failureCallback) {
    return Cordova.exec(successCallback,
                         failureCallback,
                         "PhoneGapSpeechPlugin",
                         "startRecognition",
                         [recoType, language]);
};

// **Stops speech recognition**
//
// `successCallback`  The callback function for success  
// `failureCallback`  The callback function for error  
NuanceSpeechKitPlugin.prototype.stopRecognition = function(successCallback, failureCallback) {
    return Cordova.exec(successCallback,
                         failureCallback,
                         "PhoneGapSpeechPlugin",
                         "stopRecognition",
                         []);
};

// **Gets the last set of results from speech recognition**
//
// `successCallback` The callback function for success  
// `failureCallback` The callback function for error  
NuanceSpeechKitPlugin.prototype.getResults = function(successCallback, failureCallback) {
    return Cordova.exec(successCallback,
                         failureCallback,
                         "PhoneGapSpeechPlugin",
                         "getRecoResult",
                         []);
};

// **Plays text using text to speech**
//
// `text` The text to play  
// `language` Language code for TTS playback  
// `voice` The voice to be used for TTS playback  
// `successCallback`  The callback function for success  
// `failureCallback`  The callback function for error  
NuanceSpeechKitPlugin.prototype.playTTS = function(text, language, voice,
                                                   successCallback, failureCallback) {
    return Cordova.exec(successCallback,
                         failureCallback,
                         "PhoneGapSpeechPlugin",
                         "startTTS",
                         [text, language, voice]);
};

// **Stops text to speech playback**
//
// `text` The text to play  
// `language` Language code for TTS playback  
// `voice` The voice to be used for TTS playback  
// `successCallback`  The callback function for success  
// `failureCallback`  The callback function for error  
NuanceSpeechKitPlugin.prototype.stopTTS = function(successCallback, failureCallback) {
    return Cordova.exec(successCallback,
                         failureCallback,
                         "PhoneGapSpeechPlugin",
                         "stopTTS",
                         []);
};


	