//
//  PhoneGapSpeechPlugin.h
//  PhoneGapSpeechTest
//
//  Created by Adam on 10/3/12.
//
//

#import <Cordova/CDV.h>
#import <SpeechKit/SpeechKit.h>


// Return code - success
const int RC_SUCCESS = 0;
// Return code - failure
const int RC_FAILURE = -1;
// Return code - speech kit not initialized
const int RC_NOT_INITIALIZED = -2;
// Return code - speech recognition not started
const int RC_RECO_NOT_STARTED = -3;
// Return code - no recognition result is available
const int RC_RECO_NO_RESULT_AVAIL = -4;
// Return code - TTS playback was not started
const int RC_TTS_NOT_STARTED = -5;
// Return code - recognition failure
const int RC_RECO_FAILURE = -6;
// Return code - TTS text is invalid
const int RC_TTS_TEXT_INVALID = -7;
// Return code - TTS parameters are invalid
const int RC_TTS_PARAMS_INVALID = -8;
// Return code - TTS failure
const int RC_TTS_FAILURE = -9;

// Call back event - Initialization complete
const NSString* EVENT_INIT_COMPLETE = @"InitComplete";
// Call back event - clean up complete
const NSString* EVENT_CLEANUP_COMPLETE = @"CleanupComplete";
// Call back event - Recognition started
const NSString* EVENT_RECO_STARTED = @"RecoStarted";
// Call back event - Recognition compelte
const NSString* EVENT_RECO_COMPLETE = @"RecoComplete";
// Call back event - Recognition stopped
const NSString* EVENT_RECO_STOPPED = @"RecoStopped";
// Call back event - Processing speech recognition result
const NSString* EVENT_RECO_PROCESSING = @"RecoProcessing";
// Call back event - Recognition error
const NSString* EVENT_RECO_ERROR = @"RecoError";
// Call back event - Recognition already started
const NSString* EVENT_RECO_ALREADY_STARTED = @"RecoAlreadyStarted";
// Call back event - Volume update while recording speech
const NSString* EVENT_RECO_VOLUME_UPDATE = @"RecoVolumeUpdate";
// Call back event - TTS playback started
const NSString* EVENT_TTS_STARTED = @"TTSStarted";
// Call back event - TTS playing
const NSString* EVENT_TTS_PLAYING = @"TTSPlaying";
// Call back event - TTS playback stopped
const NSString* EVENT_TTS_STOPPED = @"TTSStopped";
// Call back event - TTS playback complete
const NSString* EVENT_TTS_COMPLETE = @"TTSComplete";
// Call back event - TTS error
const NSString* EVENT_TTS_ERROR = @"TTSError";

// Keys for return values from the plugin
const NSString* KEY_RETURN_CODE = @"returnCode";
const NSString* KEY_RETURN_TEXT = @"returnText";
const NSString* KEY_EVENT = @"event";
const NSString* KEY_RESULT = @"result";
const NSString* KEY_RESULTS = @"results";

@interface PhoneGapSpeechPlugin : CDVPlugin <SpeechKitDelegate, SKVocalizerDelegate, SKRecognizerDelegate>{
    
  BOOL isSpeaking;
  BOOL isRecording;
  BOOL isInitialized;
  NSString* recoCallbackId;
  NSString* ttsCallbackId;
  NSMutableArray *lastResultArray;
  SKVocalizer* vocalizer;
  SKRecognizer* recognizerInstance;

}


@property(readonly)         SKVocalizer* vocalizer;
@property(readonly)         SKRecognizer* recognizerInstance;

// Initialize Speech Kit
- (void) initSpeechKit:(CDVInvokedUrlCommand*)command;
// Clean up Speech Kit
- (void) cleanupSpeechKit:(CDVInvokedUrlCommand*)command;

// Start speech recognition
- (void) startRecognition:(CDVInvokedUrlCommand*)command;
// Stop speech recognition
- (void) stopRecognition:(CDVInvokedUrlCommand*)command;
// Get the last recognition results
- (void) getRecoResult:(CDVInvokedUrlCommand*)command;

// Start text to speech
- (void) startTTS:(CDVInvokedUrlCommand*)command;
// Stop text to speech
- (void) stopTTS:(CDVInvokedUrlCommand*)command;


@end

