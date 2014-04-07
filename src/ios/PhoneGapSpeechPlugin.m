//
//  PhoneGapSpeechPlugin.m
//  PhoneGapSpeechTest
//
//  Created by Adam on 10/3/12.
//
//

#import "PhoneGapSpeechPlugin.h"
#import "ICredentials.h"
#import "Credentials.h"
#import <SpeechKit/SpeechKit.h>


@implementation PhoneGapSpeechPlugin
@synthesize recognizerInstance, vocalizer;

BOOL isInitialized = false;


- (void)dealloc {

    
    if (lastResultArray != nil){
        [lastResultArray dealloc];
        lastResultArray = nil;
    }
        
    [recoCallbackId dealloc];
    [ttsCallbackId dealloc];
    
    [vocalizer release];
    [recognizerInstance release];
    [SpeechKit destroy];
    [super dealloc];
}

/*
 * Creates a dictionary with the return code and text passed in
 *
 */
- (NSMutableDictionary*) createReturnDictionary: (int) returnCode withText:(NSString*) returnText{
    
    NSMutableDictionary* returnDictionary = [[[NSMutableDictionary alloc] init] autorelease];

    [returnDictionary setObject:[NSNumber numberWithInt:returnCode] forKey:KEY_RETURN_CODE];
    [returnDictionary setObject:returnText forKey:KEY_RETURN_TEXT];
    
    return returnDictionary;
    
}

/*
 * Initializes speech kit
 */
- (void) initSpeechKit:(CDVInvokedUrlCommand*)command{
    
    NSLog(@"PhoneGapSpeechPlugin.initSpeechKit: Entered method.");
    
    //get the callback id
    NSString *callbackId = command.callbackId;
    NSLog(@"PhoneGapSpeechPlugin.initSpeechKit: Callback id [%@].",  callbackId);
    CDVPluginResult *result;
    
    // get the parameters
    NSString *credentialClassName = [command.arguments objectAtIndex:0];
    NSLog(@"PhoneGapSpeechPlugin.initSpeechKit: credentialClassName [%@].",  credentialClassName);
    NSString *serverName = [command.arguments objectAtIndex:1];
    NSLog(@"PhoneGapSpeechPlugin.initSpeechKit: serverName [%@].",  serverName);
    NSString *portStr = [command.arguments objectAtIndex:2];
    NSLog(@"PhoneGapSpeechPlugin.initSpeechKit: port [%@].",  portStr);
    NSString *enableSSLStr = [command.arguments objectAtIndex:3];
    NSLog(@"PhoneGapSpeechPlugin.initSpeechKit: enableSSL [%@].",  enableSSLStr);
        
    // construct the credential object
    id<ICredentials> creds = nil;
    Class credClass = NSClassFromString(credentialClassName);
    if (credClass != nil){
        NSLog(@"PhoneGapSpeechPlugin.initSpeechKit: Credentials class loaded.");
        creds = [[[credClass alloc] init] autorelease];
        if (creds != nil){
            NSLog(@"PhoneGapSpeechPlugin.initSpeechKit: Credentials class intialized.");
        }
    }
    
    // get the app id
    NSString *appId = [creds getAppId];
    NSLog(@"PhoneGapSpeechPlugin.initSpeechKit: app id [%@].",  appId);
    
    // initialize speech kit
    [SpeechKit setupWithID: appId
                            host: serverName
                            port: [portStr intValue]
                            useSSL: [enableSSLStr boolValue]
                            delegate:self];
    
    NSMutableDictionary* returnDictionary;
    
    returnDictionary = [self createReturnDictionary: RC_SUCCESS withText: @"Success"];
    [returnDictionary setObject:EVENT_INIT_COMPLETE forKey:KEY_EVENT];
    
    result = [CDVPluginResult resultWithStatus:CDVCommandStatus_OK messageAsDictionary:returnDictionary];
 
    [self.commandDelegate sendPluginResult:result callbackId:callbackId];
    isInitialized = true;
    
    NSLog(@"PhoneGapSpeechPlugin.initSpeechKit: Leaving method.");
}

/*
 * Cleans up speech kit when done.
 */
- (void) cleanupSpeechKit:(CDVInvokedUrlCommand*)command{
    
    NSLog(@"PhoneGapSpeechPlugin.cleanupSpeechKit: Entered method.");
    
    //get the callback id
    NSString *callbackId = command.callbackId;
    
    if (lastResultArray != nil){
        [lastResultArray dealloc];
        lastResultArray = nil;
    }
    
    if (vocalizer != nil){
        [vocalizer cancel];
    }
    if (recognizerInstance != nil){
        [recognizerInstance cancel];
    }
    
    // destroy speech kit
    [SpeechKit destroy];
    
    CDVPluginResult *result;
    
    // create the return object
    NSMutableDictionary* returnDictionary = [self createReturnDictionary: RC_SUCCESS withText: @"Success"];
    [returnDictionary setObject:EVENT_CLEANUP_COMPLETE forKey:KEY_EVENT];
    
    // set the return status and object
    result = [CDVPluginResult resultWithStatus:CDVCommandStatus_OK messageAsDictionary: returnDictionary];

    [self.commandDelegate sendPluginResult:result callbackId:callbackId];
    isInitialized = false;
    
    NSLog(@"PhoneGapSpeechPlugin.cleanupSpeechKit: Leaving method.");
}


/*
 * Start speech recognition with parameters passed in
 */
- (void) startRecognition:(CDVInvokedUrlCommand*)command{
    
    NSLog(@"PhoneGapSpeechPlugin.startRecognition: Entered method. Session ID [%@]", [SpeechKit sessionID]);
    
    NSMutableDictionary* returnDictionary;
    CDVPluginResult *result;
    BOOL keepCallBack = false;
    
    //get the callback id and save it for later
    NSString *callbackId = command.callbackId;
    if (recoCallbackId != nil){
        [recoCallbackId dealloc];
    }
    recoCallbackId = [callbackId mutableCopy];
    NSLog(@"PhoneGapSpeechPlugin.startRecognition: Call back id [%@].", recoCallbackId);
    
    if (isInitialized == true){
    
        int numArgs = [command.arguments count];
        if (numArgs >= 2){
            
            NSString *recoType = [command.arguments objectAtIndex:0];
            NSLog(@"PhoneGapSpeechPlugin.startRecognition: Reco type [%@].", recoType);
            NSString *lang = [command.arguments objectAtIndex:1];
            NSLog(@"PhoneGapSpeechPlugin.startRecognition: Language [%@].", lang);

            if (lastResultArray != nil){
                [lastResultArray dealloc];
                lastResultArray = nil;
            }
            
            if (recognizerInstance != nil)
                [recognizerInstance release];
                
            NSString *recognitionModel = SKDictationRecognizerType;
            if ([recoType caseInsensitiveCompare:@"websearch"]){
                recognitionModel = SKSearchRecognizerType;
            }
            NSLog(@"PhoneGapSpeechPlugin.startRecognition: Recognition model set to [%@].", recognitionModel);
            
            recognizerInstance = [[SKRecognizer alloc] initWithType:SKDictationRecognizerType
                                                    detection:SKLongEndOfSpeechDetection
                                                    language:lang
                                                    delegate:self];
            
            returnDictionary = [self createReturnDictionary: RC_SUCCESS withText: @"Success"];
            [returnDictionary setObject:EVENT_RECO_STARTED forKey:KEY_EVENT];
            keepCallBack = true;
            
        }
        else{
            returnDictionary = [self createReturnDictionary: RC_RECO_NOT_STARTED withText: @"Invalid parameters count passed."];
            [returnDictionary setObject:EVENT_RECO_ERROR forKey:KEY_EVENT];
        }
    }
    else{
        returnDictionary = [self createReturnDictionary: RC_NOT_INITIALIZED withText: @"Reco Start Failure: Speech Kit not initialized."];
        [returnDictionary setObject:EVENT_RECO_ERROR forKey:KEY_EVENT];
    }
    
    result = [CDVPluginResult resultWithStatus:CDVCommandStatus_OK messageAsDictionary:returnDictionary];
    [result setKeepCallbackAsBool:keepCallBack];
    
    [self.commandDelegate sendPluginResult:result callbackId:callbackId];
        
    NSLog(@"PhoneGapSpeechPlugin.startRecognition: Leaving method.");
}


/*
 * Stops recognition that has previously been started
 */
- (void) stopRecognition:(CDVInvokedUrlCommand*)command{
    
    
    NSLog(@"PhoneGapSpeechPlugin.stopRecognition: Entered method.");
    
    //get the callback id
    NSString *callbackId = command.callbackId;
    
    CDVPluginResult *result;
    [recognizerInstance stopRecording];
    
    //[recognizerInstance cancel];
    
    //if (recognizerInstance != nil)
    //    [recognizerInstance release];
    
    NSMutableDictionary* returnDictionary = [self createReturnDictionary: RC_SUCCESS withText: @"Success"];
    [returnDictionary setObject:EVENT_RECO_STOPPED forKey:KEY_EVENT];
    
    result = [CDVPluginResult resultWithStatus:CDVCommandStatus_OK messageAsDictionary: returnDictionary];
    [result setKeepCallbackAsBool:YES];

    [self.commandDelegate sendPluginResult:result callbackId:callbackId];
    
    NSLog(@"PhoneGapSpeechPlugin.stopRecognition: Leaving method.");
    
}

/*
 * Gets the result from the previous successful recognition
 */
- (void) getRecoResult:(CDVInvokedUrlCommand*)command{
    
    NSLog(@"PhoneGapSpeechPlugin.getRecoResult: Entered method.");
    
    //get the callback id
    NSString *callbackId = command.callbackId;
    
    CDVPluginResult *result;
    NSMutableDictionary* returnDictionary;
    
    if (lastResultArray != nil){
        

        int numOfResults = [lastResultArray count];
        NSLog(@"PhoneGapSpeechPlugin.getRecoResult: Result count [%d]", numOfResults);
        if (numOfResults > 0){
            
            returnDictionary = [self createReturnDictionary: RC_SUCCESS withText: @"Success"];
            
            // set the first result text
            NSMutableDictionary *result1 = lastResultArray[0];
            NSString *resultText = [result1 objectForKey:@"value"];
            [returnDictionary setObject:resultText forKey:KEY_RESULT];
            // set the array
            [returnDictionary setObject:lastResultArray forKey:KEY_RESULTS];
            
        }
        else{
          returnDictionary = [self createReturnDictionary: RC_RECO_NO_RESULT_AVAIL withText: @"No result available."];
        }
    }
    else{
        returnDictionary = [self createReturnDictionary: RC_RECO_NO_RESULT_AVAIL withText: @"No result available."];
    }
    
    result = [CDVPluginResult resultWithStatus:CDVCommandStatus_OK messageAsDictionary:returnDictionary];

    [self.commandDelegate sendPluginResult:result callbackId:callbackId];
    
    NSLog(@"PhoneGapSpeechPlugin.stopRecognition: Leaving method.");
    
}

/*
 * Start text to speech with the parameters passed
 */
- (void) startTTS:(CDVInvokedUrlCommand*)command{

    NSLog(@"PhoneGapSpeechPlugin.startTTS: Entered method.");
        
    CDVPluginResult *result;
    NSMutableDictionary* returnDictionary;
    BOOL keepCallback = false;
    
    //get the callback id and hold on to it
    NSString *callbackId = command.callbackId;
    if (ttsCallbackId != nil){
        [ttsCallbackId dealloc];
    }
    ttsCallbackId = [callbackId mutableCopy];
    NSLog(@"PhoneGapSpeechPlugin.startTTS: Call back id [%@].", ttsCallbackId);
    
    if (isInitialized == true){
            
        // get the parameters
        NSString *text = [command.arguments objectAtIndex:0];
        NSString *lang = [command.arguments objectAtIndex:1];
        NSString *voice = [command.arguments objectAtIndex:2];
        NSLog(@"PhoneGapSpeechPlugin.startTTS: Text = [%@] Lang = [%@] Voice = [%@].", text, lang, voice);
        
        
        if (vocalizer != nil){
            [vocalizer release];
            vocalizer = nil;
        }
        
        if (text != nil){
        
            if (![voice isEqual:[NSNull null]]){
                NSLog(@"PhoneGapSpeechPlugin.startTTS: Initializing with voice.");
                vocalizer = [[SKVocalizer alloc] initWithVoice:voice delegate:self];
            }
            else
            if (![lang isEqual:[NSNull null]]){
                NSLog(@"PhoneGapSpeechPlugin.startTTS: Initializing with language.");
                vocalizer = [[SKVocalizer alloc] initWithLanguage:lang delegate:self];
            }
            else{
                returnDictionary = [self createReturnDictionary: RC_TTS_PARAMS_INVALID withText: @"Parameters invalid."];
            }
            
            if (vocalizer != nil){
                
                NSLog(@"PhoneGapSpeechPlugin.startTTS: About to speak text.");
                [vocalizer speakString:text];
               
                returnDictionary = [self createReturnDictionary: RC_SUCCESS withText: @"Success"];
                [returnDictionary setObject:EVENT_TTS_STARTED forKey:KEY_EVENT];
                keepCallback = true;
            }
        
        }
        else{
            returnDictionary = [self createReturnDictionary: RC_TTS_PARAMS_INVALID withText: @"Text passed is invalid."];
        }
    }
    else{
        returnDictionary = [self createReturnDictionary: RC_NOT_INITIALIZED withText: @"TTS Start Failure: Speech Kit not initialized.."];
        [returnDictionary setObject:EVENT_TTS_ERROR forKey:KEY_EVENT];
    }
    
    result = [CDVPluginResult resultWithStatus:CDVCommandStatus_OK messageAsDictionary:returnDictionary];
    [result setKeepCallbackAsBool:keepCallback];

    
    [self.commandDelegate sendPluginResult:result callbackId:callbackId];
    
    NSLog(@"PhoneGapSpeechPlugin.startTTS: Leaving method.");
    
    
}

/*
 * Stop TTS playback.
 */
- (void) stopTTS:(CDVInvokedUrlCommand*)command{
    
    NSLog(@"PhoneGapSpeechPlugin.stopTTS: Entered method.");
    
    //get the callback id
    NSString *callbackId = command.callbackId;
    
    CDVPluginResult *result;
    NSMutableDictionary* returnDictionary;
    
    
    if (vocalizer != nil){
        [vocalizer cancel];
    }
    
    returnDictionary = [self createReturnDictionary: RC_SUCCESS withText: @"Success"];
    
    result = [CDVPluginResult resultWithStatus:CDVCommandStatus_OK messageAsDictionary:returnDictionary];
    [result setKeepCallbackAsBool:YES];
    
    [self.commandDelegate sendPluginResult:result callbackId:callbackId];
    
    NSLog(@"PhoneGapSpeechPlugin.stopTTS: Leaving method.");
    
}


- (void)updateVUMeter {
    
    if ((recognizerInstance != nil) && (isRecording == true)){
        
        float width = (90+recognizerInstance.audioLevel)*5/2;
        NSString *volumeStr = [NSString stringWithFormat:@"%f", width];
        
        CDVPluginResult *result;
        NSMutableDictionary* returnDictionary;
        
        returnDictionary = [self createReturnDictionary: RC_SUCCESS withText: @"Success"];
        [returnDictionary setObject:EVENT_RECO_VOLUME_UPDATE forKey:KEY_EVENT];
        [returnDictionary setObject:volumeStr forKey:@"volumeLevel"];
        
        result = [CDVPluginResult resultWithStatus:CDVCommandStatus_OK messageAsDictionary:returnDictionary];
        [result setKeepCallbackAsBool:YES];
        
        [self.commandDelegate sendPluginResult:result callbackId:recoCallbackId];
       
    
        [self performSelector:@selector(updateVUMeter) withObject:nil afterDelay:0.5];

    }
}

#pragma mark -
#pragma mark SKRecognizerDelegate methods

- (void)recognizerDidBeginRecording:(SKRecognizer *)recognizer
{
    NSLog(@"PhoneGapSpeechPlugin.recognizerDidBeginRecording: Entered method. Recording started. [%@]", recoCallbackId);
    
    CDVPluginResult *result;
    NSMutableDictionary* returnDictionary;
    
    returnDictionary = [self createReturnDictionary: RC_SUCCESS withText: @"Success"];
    [returnDictionary setObject:EVENT_RECO_STARTED forKey:KEY_EVENT];
    
    result = [CDVPluginResult resultWithStatus:CDVCommandStatus_OK messageAsDictionary:returnDictionary];
    [result setKeepCallbackAsBool:YES];
    
    //[self writeJavascript:[result toSuccessCallbackString: recoCallbackId]];
    [self.commandDelegate sendPluginResult:result callbackId:recoCallbackId];

    isRecording = true;
    [self performSelector:@selector(updateVUMeter) withObject:nil afterDelay:0.5];
    
    NSLog(@"PhoneGapSpeechPlugin.recognizerDidBeginRecording: Leaving method.");
}

- (void)recognizerDidFinishRecording:(SKRecognizer *)recognizer
{
    NSLog(@"PhoneGapSpeechPlugin.recognizerDidFinishRecording: Entered method. Recording finished. [%@]", recoCallbackId);
    
    isRecording = false;
    
    CDVPluginResult *result;
    NSMutableDictionary* returnDictionary;
    
    returnDictionary = [self createReturnDictionary: RC_SUCCESS withText: @"Success"];
    [returnDictionary setObject:EVENT_RECO_PROCESSING forKey:KEY_EVENT];
   
    result = [CDVPluginResult resultWithStatus:CDVCommandStatus_OK messageAsDictionary:returnDictionary];
    [result setKeepCallbackAsBool:YES];
    
    //[self writeJavascript:[result toSuccessCallbackString: recoCallbackId]];
    [self.commandDelegate sendPluginResult:result callbackId:recoCallbackId];

    [NSObject cancelPreviousPerformRequestsWithTarget:self selector:@selector(updateVUMeter) object:nil];
    
    NSLog(@"PhoneGapSpeechPlugin.recognizerDidFinishRecording: Leaving method.");
}

- (void)recognizer:(SKRecognizer *)recognizer didFinishWithResults:(SKRecognition *)results
{
    NSLog(@"PhoneGapSpeechPlugin.recognizerDidFinishWithResults: Entered method. Got results. [%@]", recoCallbackId);
    
    NSLog(@"PhoneGapSpeechPlugin.recognizerDidFinishWithResults: Session id [%@].", [SpeechKit sessionID]); // for debugging purpose: printing out the speechkit session id
    
    isRecording = false;
    
    long numOfResults = [results.results count];
    NSLog(@"PhoneGapSpeechPlugin.recognizerDidFinishWithResults: Result count [%ld]", numOfResults);


    CDVPluginResult *result;
    NSMutableDictionary *returnDictionary;
    
    
	if (numOfResults > 0){
        
        returnDictionary = [self createReturnDictionary: RC_SUCCESS withText: @"Success"];
        
        NSString *resultText = [results firstResult];
        NSLog(@"PhoneGapSpeechPlugin.recognizerDidFinishWithResults: Result = [%@]", resultText);
        
        [returnDictionary setObject:resultText forKey:KEY_RESULT];
        
		//alternativesDisplay.text = [[results.results subarrayWithRange:NSMakeRange(1, numOfResults-1)] componentsJoinedByString:@"\n"];

        NSMutableArray *resultArray = [[NSMutableArray alloc] init];
        
        for (int i = 0; i < numOfResults; i++){
            NSMutableDictionary *resultDictionary = [[[NSMutableDictionary alloc] init] autorelease];
            [resultDictionary setObject:results.results[i] forKey:@"value"];
            [resultDictionary setObject:results.scores[i] forKey:@"confidence"];
            [resultArray addObject:resultDictionary];
        }
        
        lastResultArray = resultArray;
        
        [returnDictionary setObject:resultArray forKey:KEY_RESULTS];
    }
    else{
        returnDictionary = [self createReturnDictionary: RC_RECO_NO_RESULT_AVAIL withText: @"No result available."];
    }
    
    /*
    if (results.suggestion) {
        
        UIAlertView *alert = [[UIAlertView alloc] initWithTitle:@"Suggestion"
                                                        message:results.suggestion
                                                       delegate:nil
                                              cancelButtonTitle:@"OK"
                                              otherButtonTitles:nil];
        [alert show];
        [alert release];
    }
    */
    
	[recognizerInstance release];
	recognizerInstance = nil;
    
    //returnDictionary = [self createReturnDictionary: RC_SUCCESS withText: @"Success"];
    [returnDictionary setObject:EVENT_RECO_COMPLETE forKey:KEY_EVENT];
    
    result = [CDVPluginResult resultWithStatus:CDVCommandStatus_OK messageAsDictionary:returnDictionary];
    [result setKeepCallbackAsBool:NO];
    
    //[self writeJavascript:[result toSuccessCallbackString: recoCallbackId]];
    [self.commandDelegate sendPluginResult:result callbackId:recoCallbackId];
    
    if (recoCallbackId != nil){
        [recoCallbackId dealloc];
        recoCallbackId = nil;
    }
    
    NSLog(@"PhoneGapSpeechPlugin.recognizerDidFinishWithResults: Leaving method");
    
}

- (void)recognizer:(SKRecognizer *)recognizer didFinishWithError:(NSError *)error suggestion:(NSString *)suggestion
{
    NSLog(@"PhoneGapSpeechPlugin.recognizerDidFinishWithError: Entered method. Got error. [%@]", recoCallbackId);
    NSLog(@"PhoneGapSpeechPlugin.recognizerDidFinishWithError: Session id [%@].", [SpeechKit sessionID]); // for debugging purpose: printing out the speechkit session id
    
    isRecording = false;

    /*
    UIAlertView *alert = [[UIAlertView alloc] initWithTitle:@"Error"
                                                    message:[error localizedDescription]
                                                   delegate:nil
                                          cancelButtonTitle:@"OK"
                                          otherButtonTitles:nil];
    [alert show];
    [alert release];
    
    if (suggestion) {
        UIAlertView *alert = [[UIAlertView alloc] initWithTitle:@"Suggestion"
                                                        message:suggestion
                                                       delegate:nil
                                              cancelButtonTitle:@"OK"
                                              otherButtonTitles:nil];
        [alert show];
        [alert release];
        
    }
    */
	[recognizerInstance release];
	recognizerInstance = nil;

    CDVPluginResult *result;
    NSMutableDictionary* returnDictionary;
    
    returnDictionary = [self createReturnDictionary: RC_RECO_FAILURE withText: [error localizedDescription]];
    [returnDictionary setObject:EVENT_RECO_ERROR forKey:KEY_EVENT];
     
    
    result = [CDVPluginResult resultWithStatus:CDVCommandStatus_OK messageAsDictionary:returnDictionary];
    [result setKeepCallbackAsBool:NO];
    
    //[self writeJavascript:[result toSuccessCallbackString: recoCallbackId]];
    [self.commandDelegate sendPluginResult:result callbackId:recoCallbackId];
    
    if (recoCallbackId != nil){
        [recoCallbackId dealloc];
        recoCallbackId = nil;
    }
    NSLog(@"PhoneGapSpeechPlugin.recognizerDidFinishWithError: Leaving method");
}

#pragma mark -
#pragma mark SKVocalizerDelegate methods


- (void)vocalizer:(SKVocalizer *)vocalizer willBeginSpeakingString:(NSString *)text {
    
    NSLog(@"PhoneGapSpeechPlugin.vocalizerWillBeginSpeakingString: Entered method.");
    isSpeaking = YES;
    
	//if (text){
		//textReadSoFar.text = [[textReadSoFar.text stringByAppendingString:text] stringByAppendingString:@"\n"];
    //}
    
    
    CDVPluginResult *result;
    NSMutableDictionary* returnDictionary;
        
    returnDictionary = [self createReturnDictionary: RC_SUCCESS withText: @"Success"];
    [returnDictionary setObject:EVENT_TTS_STARTED forKey:KEY_EVENT];
        
    result = [CDVPluginResult resultWithStatus:CDVCommandStatus_OK messageAsDictionary:returnDictionary];
    [result setKeepCallbackAsBool:YES];
    //[self writeJavascript:[result toSuccessCallbackString: ttsCallbackId]];
    [self.commandDelegate sendPluginResult:result callbackId:ttsCallbackId];
    
    NSLog(@"PhoneGapSpeechPlugin.vocalizerWillBeginSpeakingString: Leaving method");
    
}

- (void)vocalizer:(SKVocalizer *)vocalizer willSpeakTextAtCharacter:(NSUInteger)index ofString:(NSString *)text {
    NSLog(@"PhoneGapSpeechPlugin.vocalizerWillSpeakTextAtChar: Entered method."); 
    //textReadSoFar.text = [text substringToIndex:index];
}

- (void)vocalizer:(SKVocalizer *)vocalizer didFinishSpeakingString:(NSString *)text withError:(NSError *)error {
    
    NSLog(@"PhoneGapSpeechPlugin.vocalizerDidFinishSpeakingString: Finished Speaking: Session id [%@].", [SpeechKit sessionID]); // for debugging purpose: printing out the speechkit session id
    
    CDVPluginResult *result;
    NSMutableDictionary* returnDictionary;
    isSpeaking = NO;
    
	if (error != nil){
        NSLog(@"PhoneGapSpeechPlugin.vocalizerDidFinishSpeakingString: Error: [%@].", [error localizedDescription]);
        returnDictionary = [self createReturnDictionary: RC_TTS_FAILURE withText: [error localizedDescription]];
        [returnDictionary setObject:EVENT_TTS_ERROR forKey:KEY_EVENT];
	}
    else{
    
        returnDictionary = [self createReturnDictionary: RC_SUCCESS withText: @"Success"];
        [returnDictionary setObject:EVENT_TTS_COMPLETE forKey:KEY_EVENT];
    }
    
    result = [CDVPluginResult resultWithStatus:CDVCommandStatus_OK messageAsDictionary:returnDictionary];
    [result setKeepCallbackAsBool:NO];
    //[self writeJavascript:[result toSuccessCallbackString: ttsCallbackId]];
    [self.commandDelegate sendPluginResult:result callbackId:ttsCallbackId];
    
    if (ttsCallbackId != nil){
        [ttsCallbackId dealloc];
        ttsCallbackId = nil;
    }
    
    NSLog(@"PhoneGapSpeechPlugin.vocalizerDidFinishSpeakingString: Entered method."); 
    
}

@end
