
package net.ninjaenterprises.nuance;

import org.apache.cordova.CallbackContext;
import org.apache.cordova.CordovaPlugin;
import org.apache.cordova.PluginResult;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import net.ninjaenterprises.nuance.Credentials;

import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import com.nuance.nmdp.speechkit.*;

/**
 * Sample PhoneGap plugin to call Nuance Speech Kit
 * @author asmyth
 *
 */
public class NuancePlugin extends CordovaPlugin{
    
	/**
	 * Action to initialize speech kit
	 */
	public static final String ACTION_INIT = "initSpeechKit";
	/**
	 * Action to start recognition
	 */
	public static final String ACTION_START_RECO = "startRecognition";
	/**
	 * Action to stop recognition
	 */
	public static final String ACTION_STOP_RECO = "stopRecognition";
	/**
	 * Action to get recognition results
	 */
	public static final String ACTION_GET_RECO_RESULT = "getRecoResult";
	/**
	 * Action to clean up speech kit after initialization
	 */
	public static final String ACTION_CLEANUP = "cleanup";
	/**
	 * Action to start TTS playback
	 */
	public static final String ACTION_PLAY_TTS = "playTTS";
	/**
	 * Action to stop TTS playback
	 */
	public static final String ACTION_STOP_TTS = "stopTTS";
	/**
	 * Action to setup a callback id to get the next event
	 */
	public static final String ACTION_QUERY_NEXT_EVENT = "queryNextEvent";
	
	/**
	 * Return code - success
	 */
	public static final int RC_SUCCESS = 0;
	/**
	 * Return code - failure
	 */
	public static final int RC_FAILURE = -1;
	/**
	 * Return code - speech kit not initialized
	 */
	public static final int RC_NOT_INITIALIZED = -2;
	/**
	 * Return code - speech recognition not started
	 */
	public static final int RC_RECO_NOT_STARTED = -3;
	/**
	 * Return code - no recognition result is available
	 */
	public static final int RC_RECO_NO_RESULT_AVAIL = -4;
	/**
	 * Return code - TTS playback was not started
	 */
	public static final int RC_TTS_NOT_STARTED = -5;
	/**
	 * Return code - recognition failure
	 */
	public static final int RC_RECO_FAILURE = -6;
	/**
	 * Return code TTS text is invalid
	 */
	public static final int RC_TTS_TEXT_INVALID = -7;
	/**
	 * Return code - TTS parameters are invalid
	 */
	public static final int RC_TTS_PARAMS_INVALID = -8;
	
	/**
	 * Call back event - Initialization complete
	 */
	public static final String EVENT_INIT_COMPLETE = "InitComplete";
	/**
	 * Call back event - clean up complete
	 */
	public static final String EVENT_CLEANUP_COMPLETE = "CleanupComplete";
	/**
	 * Call back event - Recognition started
	 */
	public static final String EVENT_RECO_STARTED = "RecoStarted";
	/**
	 * Call back event - Recognition compelte
	 */
	public static final String EVENT_RECO_COMPLETE = "RecoComplete";
	/**
	 * Call back event - Recognition stopped
	 */
	public static final String EVENT_RECO_STOPPED = "RecoStopped";
	/**
	 * Call back event - Processing speech recognition result
	 */
	public static final String EVENT_RECO_PROCESSING = "RecoProcessing";
	/**
	 * Call back event - Recognition error
	 */
	public static final String EVENT_RECO_ERROR = "RecoError";
	/**
	 * Call back event - Volume update while recording speech
	 */
	public static final String EVENT_RECO_VOLUME_UPDATE = "RecoVolumeUpdate";
	/**
	 * Call back event - TTS playback started
	 */
	public static final String EVENT_TTS_STARTED = "TTSStarted";
	/**
	 * Call back event - TTS playing
	 */
	public static final String EVENT_TTS_PLAYING = "TTSPlaying";
	/**
	 * Call back event - TTS playback stopped
	 */
	public static final String EVENT_TTS_STOPPED = "TTSStopped";
	/**
	 * Call back event - TTS playback complete
	 */
	public static final String EVENT_TTS_COMPLETE = "TTSComplete";
    
    
	// variables to support recognition
	/**
	 * Speech kit reference
	 */
	private SpeechKit speechKit = null;
	/**
	 * Recognition listener
	 */
    private Recognizer.Listener recoListener;
	/**
	 * Recognizer reference
	 */
    private Recognizer currentRecognizer = null;
	/**
	 * Handler reference
	 */
    private Handler handler = null;
	/**
	 * Reference to last result
	 */
    private Recognition.Result [] lastResult = null;
	/**
	 * State variable to track if recording is active
	 */
    private boolean recording = false;
    
    /**
     * ID provided to invoke callback function.
     */
    private CallbackContext recognitionCallbackContext = null;
    
    // variables to support TTS
	/**
	 * Vocalizer reference for text to speech
	 */
    private Vocalizer vocalizerInstance = null;
	/**
	 * Context object for text to speech tracking
	 */
    private Object _lastTtsContext = null;
    /**
     * ID provided to invoke callback function.
     */
    private CallbackContext ttsCallbackContext = null;
    
    public CallbackContext callbackContext;
    /**
     * Method to initiate calls from PhoneGap/javascript API
     *
     * @param action
     * The action method
     *
     * @param data
     * Incoming parameters
     *
     * @param callbackId
     * The call back id
     */
	public boolean execute(String action, JSONArray data, CallbackContext callbackContext) throws JSONException {
        
		Log.d("NuancePlugin", "NuancePlugin.execute: Entered method. Action = ["+action+"] Call Back Context = ["+callbackContext+"]");
		
		PluginResult result = null;
        
		try{
			
			if (ACTION_INIT.equals(action)) { // INITALIZE
				// initialize sppech kit
				result = initSpeechKit(data, callbackContext);
			}
			else if (ACTION_CLEANUP.equals(action)) {  // CLEANUP
				result = cleanupSpeechKit(data, callbackContext);
			}
			else if (ACTION_START_RECO.equals(action)) {  // START RECOGNITION
				result = startRecognition(data, callbackContext);
			}
			else if (ACTION_STOP_RECO.equals(action)) {  // STOP RECOGNITION
				result = stopRecognition(data, callbackContext);
			}
			else if (ACTION_GET_RECO_RESULT.equals(action)) {  // GET THE LAST RESULT
				Log.d("NuancePlugin", "NuancePlugin.execute: Call to get results.");
				result = getRecoResult(data, callbackContext);
			}
			else if (ACTION_PLAY_TTS.equals(action)) {  // START TTS PLAYBACK
				Log.d("NuancePlugin", "NuancePlugin.execute: Call to start TTS.");
				result = startTTS(data, callbackContext);
			}
			else if (ACTION_STOP_TTS.equals(action)) {  // START TTS PLAYBACK
				Log.d("NuancePlugin", "NuancePlugin.execute: Call to stop TTS.");
				result = stopTTS(data, callbackContext);
			}
			else if (ACTION_QUERY_NEXT_EVENT.equals(action)) {  // add callback for next event
                
				Log.d("NuancePlugin", "NuancePlugin.execute: Call to query next event.");
				JSONObject returnObject = new JSONObject();
				
				ttsCallbackContext = callbackContext;
                
				setReturnCode(returnObject, RC_SUCCESS, "Query Success");
				result = new PluginResult(PluginResult.Status.OK, returnObject);
			}
			else {
				result = new PluginResult(PluginResult.Status.INVALID_ACTION);
				Log.e("NuancePlugin", "NuancePlugin.execute: Invalid action ["+action+"] passed");
			}
            
		}
		catch (JSONException jsonEx) {
			Log.e("NuancePlugin", "NuancePlugin.execute: ["+action+"] Got JSON Exception "+ jsonEx.getMessage(), jsonEx);
			result = new PluginResult(PluginResult.Status.JSON_EXCEPTION);
		}
		catch (Exception e){
			Log.e("NuancePlugin", "NuancePlugin.execute: ["+action+"] Got Exception "+ e.getMessage(), e);
			result = new PluginResult(PluginResult.Status.ERROR);
		}
        
		callbackContext.sendPluginResult(result);
		Log.d("NuancePlugin", "NuancePlugin.execute: Leaving method.");
		return true;
	}
	
	/**
	 * Method to initialize speech kit.
	 *
	 * @param data
	 * The data object passed into exec
	 *
	 * @param callbackId
	 * The callback id passed into exec
	 *
	 * @return PluginResult
	 * The populated PluginResult
	 *
	 * @throws JSONException
	 */
	private PluginResult initSpeechKit(JSONArray data, CallbackContext callbackContext) throws JSONException{
		
		PluginResult result = null;
		Log.d("NuancePlugin", "NuancePlugin.initSpeechKit: Entered method.");
		
		// Get parameters to do initialization
		String credentialClassName = "net.ninjaenterprises.nuance.Credentials";
		
		Log.d("NuancePlugin", "NuancePlugin.initSpeechKit: init: Credential Class = ["+credentialClassName+"]");
		String serverName = data.getString(1);
		Log.d("NuancePlugin", "NuancePlugin.initSpeechKit: init: Server = ["+serverName+"]");
		int port = data.getInt(2);
		Log.d("NuancePlugin", "NuancePlugin.initSpeechKit: init: Port = ["+port+"]");
		boolean sslEnabled = data.getBoolean(3);
		Log.d("NuancePlugin", "NuancePlugin.initSpeechKit: init: SSL = ["+sslEnabled+"]");
        
		JSONObject returnObject = new JSONObject();
		
		try{
	        if (speechKit == null){
	        	
	        	Class credentialClass = Class.forName(credentialClassName);
	        	ICredentials credentials = (ICredentials)credentialClass.newInstance();
	        	
	        	String appId = credentials.getAppId();
	        	byte[] appKey = credentials.getAppKey();
	        	
	            speechKit = SpeechKit.initialize(cordova.getActivity().getApplicationContext(), appId, serverName, port, sslEnabled, appKey);
	            speechKit.connect();
	            // TODO: Keep an eye out for audio prompts not working on the Droid 2 or other 2.2 devices.
	            //Prompt beep = _speechKit.defineAudioPrompt(R.beep);
	            //_speechKit.setDefaultRecognizerPrompts(beep, Prompt.vibration(100), null, null);
	        }
	        
	        setReturnCode(returnObject, RC_SUCCESS, "Init Success");
	        returnObject.put("event", EVENT_INIT_COMPLETE);
			result = new PluginResult(PluginResult.Status.OK, returnObject);
			result.setKeepCallback(false);
		}
		catch(Exception e){
			Log.e("NuancePlugin", "NuancePlugin.initSpeechKit: Error initalizing:" +e.getMessage(), e);
			setReturnCode(returnObject, RC_FAILURE, e.toString());
			result = new PluginResult(PluginResult.Status.OK, returnObject);
		}
		
		Log.d("NuancePlugin", "NuancePlugin.initSpeechKit: Leaving method.");
		return result;
		
	} // end initSpeechKit
	
	/**
	 * Cleans up speech kit variables if they have been initialized
	 *
	 * @param data
	 * @param callbackId
	 * @return
	 * @throws JSONException
	 */
	private PluginResult cleanupSpeechKit(JSONArray data, CallbackContext callbackContext) throws JSONException{
        
		Log.d("NuancePlugin", "NuancePlugin.cleanupSpeechKit: Entered method.");
		PluginResult result = null;
        
		if (vocalizerInstance != null){
			try{
				vocalizerInstance.cancel();
				Log.d("NuancePlugin", "NuancePlugin.cleanupSpeechKit: Vocalizer cancelled.");
			}
			catch(IllegalStateException ise){
				Log.w("NuancePlugin", "NuancePlugin.cleanupSpeechKit: Error cancelling vocalizer: ", ise);
			}
			vocalizerInstance = null;
			
		}
		
		if (currentRecognizer != null){
			try{
				currentRecognizer.cancel();
				Log.d("NuancePlugin", "NuancePlugin.cleanupSpeechKit: Recognizer cancelled.");
			}
			catch(IllegalStateException ise){
				Log.w("NuancePlugin", "NuancePlugin.cleanupSpeechKit: Error cancelling recognizer: ", ise);
			}
			currentRecognizer = null;
		}
		
		if (speechKit != null){
			speechKit.release();
			speechKit = null;
			Log.d("NuancePlugin", "NuancePlugin.cleanupSpeechKit: Speech kit released.");
		}
        
		JSONObject returnObject = new JSONObject();
		setReturnCode(returnObject, RC_SUCCESS, "Cleanup Success");
		returnObject.put("event", EVENT_CLEANUP_COMPLETE);
		result = new PluginResult(PluginResult.Status.OK, returnObject);
		
		Log.d("NuancePlugin", "NuancePlugin.cleanupSpeechKit: Leaving method.");
		return result;
	}
	
	/**
	 * Starts recognition.
	 *
	 * @param data
	 * @param callbackId
	 * @return
	 * @throws JSONException
	 */
	private PluginResult startRecognition(JSONArray data, CallbackContext callbackContext) throws JSONException{
		
		Log.d("NuancePlugin", "NuancePlugin.startRecognition: Entered method.");
		PluginResult result = null;
		
		JSONObject returnObject = new JSONObject();
		if (recoListener != null){
			Log.d("NuancePlugin", "NuancePlugin.execute: LISTENER IS NOT NULL");
		}
		if (speechKit != null){
            
			// get the recognition type
			String recognitionType = data.getString(0);
			Log.d("NuancePlugin", "NuancePlugin.execute: startReco: Reco Type = ["+recognitionType+"]");
			String recognizerType = Recognizer.RecognizerType.Dictation;
			if ("websearch".equalsIgnoreCase(recognitionType)){
				recognizerType = Recognizer.RecognizerType.Search;
			}
			// get the language
			String language = data.getString(1);
			Log.d("NuancePlugin", "NuancePlugin.execute: startReco: Language = ["+language+"]");
			
			recognitionCallbackContext = callbackContext;
			lastResult = null;
			handler = new Handler();
			recoListener = createListener();
            
			// create and start the recognizer reference
			currentRecognizer = speechKit.createRecognizer(recognizerType, Recognizer.EndOfSpeechDetection.Long, language, recoListener, handler);
			currentRecognizer.start();
            
            
			Log.d("NuancePlugin", "NuancePlugin.execute: Recognition started.");
			setReturnCode(returnObject, RC_SUCCESS, "Reco Start Success");
			returnObject.put("event", EVENT_RECO_STARTED);
			
		}
		else{
			Log.e("NuancePlugin", "NuancePlugin.execute: Speech kit was null, initialize not called.");
			setReturnCode(returnObject, RC_NOT_INITIALIZED, "Reco Start Failure: Speech Kit not initialized.");
		}
        
		result = new PluginResult(PluginResult.Status.OK, returnObject);
		result.setKeepCallback(true);
		Log.d("NuancePlugin", "NuancePlugin.startRecognition: Leaving method.");
		return result;
		
	} // end startRecogition
	
	/**
	 * Stops recognition.
	 *
	 * @param data
	 * @param callbackId
	 * @return
	 * @throws JSONException
	 */
	private PluginResult stopRecognition(JSONArray data, CallbackContext callbackContext) throws JSONException{
		
		Log.d("NuancePlugin", "NuancePlugin.stopRecognition: Entered method.");
		PluginResult result = null;
		JSONObject returnObject = new JSONObject();
        
		if (currentRecognizer != null){
			// stop the recognizer
			currentRecognizer.stopRecording();
			Log.d("NuancePlugin", "NuancePlugin.execute: Recognition started.");
			setReturnCode(returnObject, RC_SUCCESS, "Reco Stop Success");
			returnObject.put("event", EVENT_RECO_STOPPED);
            
		}
		else{
			Log.e("NuancePlugin", "NuancePlugin.execute: Recognizer was null, start not called.");
			setReturnCode(returnObject, RC_RECO_NOT_STARTED, "Reco Stop Failure: Recognizer not started.");
		}
        
		result = new PluginResult(PluginResult.Status.OK, returnObject);
		Log.d("NuancePlugin", "NuancePlugin.stopRecognition: Leaving method.");
		return result;
		
	} // end stopRecogition
	
	
	/**
	 * Retrieves recognition results from the previous recognition
	 * @param data
	 * @param callbackId
	 * @return
	 * @throws JSONException
	 */
	private PluginResult getRecoResult(JSONArray data, CallbackContext callbackContext) throws JSONException{
		
		Log.d("NuancePlugin", "NuancePlugin.getRecoResult: Entered method.");
		PluginResult result = null;
		JSONObject returnObject = new JSONObject();
        
		if (lastResult != null){
			setReturnCode(returnObject, RC_SUCCESS, "Success");
            String resultString = getResultString(lastResult);
            returnObject.put("result", resultString);
            Log.d("NuancePlugin", "Result string = ["+resultString+"].");
            JSONArray resultArray = getResultArray(lastResult);
            returnObject.put("results", resultArray);
			
		}
		else{
			Log.d("NuancePlugin", "NuancePlugin.execute: Last result was null.");
			setReturnCode(returnObject, RC_RECO_NO_RESULT_AVAIL, "No result available.");
		}
		
		result = new PluginResult(PluginResult.Status.OK, returnObject);
		Log.d("NuancePlugin", "NuancePlugin.getRecoResult: Leaving method.");
		return result;
		
	} // end getRecoResult
	
	
	
	/**
	 * Starts TTS playback.
	 *
	 * @param data
	 * @param callbackId
	 * @return
	 * @throws JSONException
	 */
	private PluginResult startTTS(JSONArray data, CallbackContext callbackContext) throws JSONException{
		
		Log.d("NuancePlugin", "NuancePlugin.startTTS: Entered method.");
		
		PluginResult result = null;
		JSONObject returnObject = new JSONObject();
		
		String ttsText = data.getString(0);
		Log.d("NuancePlugin", "NuancePlugin.execute: startTTS: Text = ["+ttsText+"]");
		String language = data.getString(1);
		Log.d("NuancePlugin", "NuancePlugin.execute: startTTS: Language = ["+language+"]");
		String voice = data.getString(2);
		Log.d("NuancePlugin", "NuancePlugin.execute: startTTS: Voice = ["+voice+"]");
		ttsCallbackContext = callbackContext;
		
		if ((ttsText == null) || ("".equals(ttsText))){
			setReturnCode(returnObject, RC_TTS_TEXT_INVALID, "TTS Text Invalid");
		}
		else
            if ((language == null) && (voice == null)){
                setReturnCode(returnObject, RC_TTS_PARAMS_INVALID, "Invalid language or voice.");
            }
            else
                if (speechKit != null){
                    if (vocalizerInstance == null){
                        Vocalizer.Listener vocalizerListener = createVocalizerListener();
                        Log.d("NuancePlugin", "NuancePlugin.execute: startTTS: Created vocalizer listener.");
                        
                        if (language != null){
                            vocalizerInstance = speechKit.createVocalizerWithLanguage(language, vocalizerListener, new Handler());
                        }
                        else{
                            vocalizerInstance = speechKit.createVocalizerWithVoice(voice, vocalizerListener, new Handler());
                        }
                        _lastTtsContext = new Object();
                        Log.d("NuancePlugin", "NuancePlugin.execute: startTTS: Calling speakString.");
                        vocalizerInstance.speakString(ttsText, _lastTtsContext);
                        Log.d("NuancePlugin", "NuancePlugin.execute: startTTS: Called speakString.");
                        
                        Log.d("NuancePlugin", "NuancePlugin.execute: startTTS: Created vocalizer.");
                    }
                    else{
                        if (language != null){
                            vocalizerInstance.setLanguage(language);
                        }
                        else{
                            vocalizerInstance.setVoice(voice);
                        }
                        
                        _lastTtsContext = new Object();
                        Log.d("NuancePlugin", "NuancePlugin.execute: startTTS: Calling speakString.");
                        
                        vocalizerInstance.speakString(ttsText, _lastTtsContext);
                        
                        Log.d("NuancePlugin", "NuancePlugin.execute: startTTS: Called speakString.");
                    }
                    
                    setReturnCode(returnObject, RC_SUCCESS, "Success");
                }
                else{
                    Log.e("NuancePlugin", "NuancePlugin.execute: Speech kit was null, initialize not called.");
                    setReturnCode(returnObject, RC_NOT_INITIALIZED, "TTS Start Failure: Speech Kit not initialized.");
                }
		
		result = new PluginResult(PluginResult.Status.OK, returnObject);
		result.setKeepCallback(true);
		
		Log.d("NuancePlugin", "NuancePlugin.startTTS: Leaving method.");
		return result;
		
	} // end startTTS
	
	/**
	 * Stops TTS playback
	 *
	 * @param data
	 * @param callbackId
	 * @return
	 * @throws JSONException
	 */
	private PluginResult stopTTS(JSONArray data, CallbackContext callbackContext) throws JSONException{
        
		Log.d("NuancePlugin", "NuancePlugin.stopTTS: Entered method.");
		PluginResult result = null;
		JSONObject returnObject = new JSONObject();
		
		//ttsCallbackId = callbackId;
		
		if (vocalizerInstance != null){
			vocalizerInstance.cancel();
			Log.d("NuancePlugin", "NuancePlugin.execute: stopTTS: Vocalizer cancelled.");
			setReturnCode(returnObject, RC_SUCCESS, "Success");
			returnObject.put("event", EVENT_TTS_COMPLETE);
            
		}
		else{
			setReturnCode(returnObject, RC_TTS_NOT_STARTED, "TTS Stop Failure: TTS not started.");
		}
		result = new PluginResult(PluginResult.Status.OK, returnObject);
		Log.d("NuancePlugin", "NuancePlugin.stopTTS: Leaving method.");
		return result;
		
	} // end stopTTS
	
	/**
	 * Sets the return code and text into the return object passed.
	 * @param returnObject
	 * @param returnCode
	 * @param returnText
	 * @throws JSONException
	 */
	private void setReturnCode(JSONObject returnObject, int returnCode, String returnText) throws JSONException{
		returnObject.put("returnCode", returnCode);
		returnObject.put("returnText", returnText);
	} // end setReturnCode
	
	
    private Recognizer.Listener createListener()
    {
        return new Recognizer.Listener()
        {
            
            public void onRecordingBegin(Recognizer recognizer)
            {
            	Log.d("NuancePlugin", "Recording...");
            	recording = true;
            	
            	try{
	            	JSONObject returnObject = new JSONObject();
					setReturnCode(returnObject, RC_SUCCESS, "Recording Started");
					returnObject.put("event", EVENT_RECO_STARTED);
					
		            PluginResult result = new PluginResult(PluginResult.Status.OK, returnObject);
		            result.setKeepCallback(true);
		            android.util.Log.d("NuancePlugin", "NuancePlugin: NuancePlugin: Recognizer.Listener.onRecordingDone: Reco Started... success: "+recognitionCallbackContext);
		            recognitionCallbackContext.sendPluginResult(result);
                    
	            }
	            catch(JSONException je){
	                android.util.Log.e("NuancePlugin", "NuancePlugin: Recognizer.Listener.onRecordingBegin: Error setting return: "+je.getMessage(), je);
	            }
                
                Runnable r = new Runnable()
                {
                    public void run()
                    {
                        if ((currentRecognizer != null) && (recording == true))
                        {
                        	try{
            	            	JSONObject returnObject = new JSONObject();
            					//setReturnCode(returnObject, RC_SUCCESS, "OK");
            					returnObject.put("event", EVENT_RECO_VOLUME_UPDATE);
            					returnObject.put("volumeLevel", Float.toString(currentRecognizer.getAudioLevel()));
                                
            					PluginResult result = new PluginResult(PluginResult.Status.OK, returnObject);
            		            result.setKeepCallback(true);
                                
            		            recognitionCallbackContext.sendPluginResult(result);
                                
            	            }
            	            catch(JSONException je){
            	                android.util.Log.e("NuancePlugin", "NuancePlugin: Recognizer.Listener.onRecordingDone: Error setting return: "+je.getMessage(), je);
            	            }
                            //_listeningDialog.setLevel(Float.toString(_currentRecognizer.getAudioLevel()));
                            handler.postDelayed(this, 500);
                        }
                        
                    }
                };
                r.run();
	            
            }
            
            public void onRecordingDone(Recognizer recognizer)
            {
            	Log.d("NuancePlugin", "Processing...");
            	recording = false;
            	try{
	            	JSONObject returnObject = new JSONObject();
					setReturnCode(returnObject, RC_SUCCESS, "Processing");
					returnObject.put("event", EVENT_RECO_PROCESSING);
                    
					PluginResult result = new PluginResult(PluginResult.Status.OK, returnObject);
		            result.setKeepCallback(true);
		            android.util.Log.d("NuancePlugin", "NuancePlugin: NuancePlugin: Recognizer.Listener.onRecordingDone: Reco Done... success: "+recognitionCallbackContext);
		            recognitionCallbackContext.sendPluginResult(result);
	            }
	            catch(JSONException je){
	                android.util.Log.e("NuancePlugin", "NuancePlugin: Recognizer.Listener.onRecordingDone: Error setting return: "+je.getMessage(), je);
	            }
            }
            
            /**
             * Handle error event and call error callback function
             */
            public void onError(Recognizer recognizer, SpeechError error) {
            	
            	android.util.Log.d("NuancePlugin", "Recognizer.Listener.onError: Entered method.");
            	if (recognizer != currentRecognizer) return;
            	currentRecognizer = null;
            	recording = false;
            	
                // Display the error + suggestion in the edit box
                String detail = error.getErrorDetail();
                String suggestion = error.getSuggestion();
                
                JSONObject returnObject = new JSONObject();
                try{
					setReturnCode(returnObject, RC_RECO_FAILURE, "Reco Failure");
					returnObject.put("event", EVENT_RECO_ERROR);
					returnObject.put("result", detail);
                }
                catch(JSONException je){
                    android.util.Log.e("NuancePlugin", "Recognizer.Listener.onError: Error storing results: "+je.getMessage(), je);
                }
                recognitionCallbackContext.error(returnObject);
                
                // for debugging purpose: printing out the speechkit session id
                android.util.Log.d("NuancePlugin", "Recognizer.Listener.onError: "+error);
            }
            
            /**
             * Get results and call the success callback function
             */
            public void onResults(Recognizer recognizer, Recognition results) {
                
            	android.util.Log.d("NuancePlugin", "Recognizer.Listener.onResults: Entered method.");
                currentRecognizer = null;
                recording = false;
                
                int count = results.getResultCount();
                Recognition.Result [] rs = new Recognition.Result[count];
                for (int i = 0; i < count; i++)
                {
                    rs[i] = results.getResult(i);
                }
                lastResult = rs;
                String resultString = getResultString(rs);
                
                
                JSONObject returnObject = new JSONObject();
                try{
					setReturnCode(returnObject, RC_SUCCESS, "Reco Success");
					returnObject.put("event", EVENT_RECO_COMPLETE);
					returnObject.put("result", resultString);
                	JSONArray resultArray = getResultArray(rs);
                	returnObject.put("results", resultArray);
                }
                catch(JSONException je){
                    android.util.Log.e("NuancePlugin", "Recognizer.Listener.onResults: Error storing results: "+je.getMessage(), je);
                }
                recognitionCallbackContext.success(returnObject);
                
                android.util.Log.d("NuancePlugin", "Recognizer.Listener.onResults: Leaving method.  Results = "+resultString);
            }
        };
    }
    
    
    /**
     * Creates a JSONArray representation of the results passed in
     * @param results
     * @return
     * @throws JSONException
     */
    private JSONArray getResultArray(Recognition.Result[] results) throws JSONException {
    	JSONArray resultArray = new JSONArray();
        
    	int resultCount = results.length;
        if (resultCount > 0)
        {
        	Log.d("NuancePlugin", "Recognizer.Listener.onResults: Result count: "+resultCount);
            JSONObject tempResult = null;
            for (int i = 0; i < results.length; i++){
            	tempResult = new JSONObject();
            	tempResult.put("value", results[i].getText());
            	tempResult.put("confidence", results[i].getScore());
            	resultArray.put(tempResult);
            }
        }
        return resultArray;
    }
    
    /**
     * Returns the text of the first recognition result
     * @param results
     * @return
     */
    private String getResultString(Recognition.Result[] results)
    {
    	String output = "";
        
        if (results.length > 0)
        {
            output = results[0].getText();
            
            //for (int i = 0; i < results.length; i++)
            //    _.add("[" + results[i].getScore() + "]: " + results[i].getText());
        }
        return output;
        
    }
    
    
    
    Vocalizer.Listener createVocalizerListener(){
    	
        
	    return new Vocalizer.Listener()
	    {
	        @Override
	        public void onSpeakingBegin(Vocalizer vocalizer, String text, Object context) {
	            //updateCurrentText("Playing text: \"" + text + "\"", Color.GREEN, false);
	        	
                //updateCurrentText("", Color.YELLOW, false);
                JSONObject returnObject = new JSONObject();
                try{
					returnObject.put("returnCode", "TTS Playback Started");
					returnObject.put("event", EVENT_TTS_STARTED);
					android.util.Log.d("NuancePlugin", "NuancePlugin: Vocalizer.Listener.onSpeakingDone: TTS Started...");
                }
                catch(JSONException je){
                    android.util.Log.e("NuancePlugin", "NuancePlugin: Vocalizer.Listener.onSpeakingBegin: Error setting return: "+je.getMessage(), je);
                }
                android.util.Log.d("NuancePlugin", "NuancePlugin: Vocalizer.Listener.onSpeakingBegin: TTS Started... success: "+ttsCallbackContext);
                // SETTING THIS EVENT DOES NOT ALLOW THE CALL TO SUCCESS LATER WITH COMPLETE
                PluginResult result = new PluginResult(PluginResult.Status.OK, returnObject);
                result.setKeepCallback(true);
                ttsCallbackContext.sendPluginResult(result);
	        	
	            // for debugging purpose: printing out the speechkit session id
	            android.util.Log.d("NuancePlugin", "NuancePlugin: Vocalizer.Listener.onSpeakingBegin: Leaving method.");
	        }
            
	        @Override
	        public void onSpeakingDone(Vocalizer vocalizer, String text, SpeechError error, Object context)
	        {
	        	android.util.Log.d("NuancePlugin", "NuancePlugin: Vocalizer.Listener.onSpeakingDone: Context = ["+context+"], last context = "+_lastTtsContext);
	            // Use the context to detemine if this was the final TTS phrase
	            if (context != _lastTtsContext)
	            {
	                //updateCurrentText("More phrases remaining", Color.YELLOW, false);
	                //updateCurrentText("", Color.YELLOW, false);
	                JSONObject returnObject = new JSONObject();
	                try{
						setReturnCode(returnObject, RC_SUCCESS, "TTS Playing...");
						returnObject.put("event", EVENT_TTS_PLAYING);
						
						android.util.Log.d("NuancePlugin", "NuancePlugin: Vocalizer.Listener.onSpeakingDone: TTS Playing...");
	                }
	                catch(JSONException je){
	                    android.util.Log.e("NuancePlugin", "NuancePlugin: Vocalizer.Listener.onSpeakingDone: Error setting return: "+je.getMessage(), je);
	                }
	                android.util.Log.d("NuancePlugin", "NuancePlugin: Vocalizer.Listener.onSpeakingDone: TTS Playing... success: "+ttsCallbackContext);
	                PluginResult result = new PluginResult(PluginResult.Status.OK, returnObject);
	                result.setKeepCallback(true);
	                ttsCallbackContext.sendPluginResult(result);
                    
	            }
	            else
	            {
	                //updateCurrentText("", Color.YELLOW, false);
	                JSONObject returnObject = new JSONObject();
	                try{
						setReturnCode(returnObject, RC_SUCCESS, "TTS Playback Complete");
						returnObject.put("event", EVENT_TTS_COMPLETE);
						android.util.Log.d("NuancePlugin", "NuancePlugin: Vocalizer.Listener.onSpeakingDone: TTS Complete...");
	                }
	                catch(JSONException je){
	                    android.util.Log.e("NuancePlugin", "NuancePlugin: Vocalizer.Listener.onSpeakingDone: Error setting return: "+je.getMessage(), je);
	                }
	                android.util.Log.d("NuancePlugin", "NuancePlugin: Vocalizer.Listener.onSpeakingDone: TTS Complete... success: "+ttsCallbackContext);
	                ttsCallbackContext.success(returnObject);
                    
	            }
	            // for debugging purpose: printing out the speechkit session id
	            android.util.Log.d("NuancePlugin", "NuancePlugin: Vocalizer.Listener.onSpeakingDone: Leaving method.");
                
	        }
	    };
	    
    }
    
    
    
} // end class
