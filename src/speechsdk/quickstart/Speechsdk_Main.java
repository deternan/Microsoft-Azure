package speechsdk.quickstart;

import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.Semaphore;

import com.microsoft.cognitiveservices.speech.*;
import com.microsoft.cognitiveservices.speech.audio.AudioConfig;
import com.microsoft.cognitiveservices.speech.translation.SpeechTranslationConfig;
import com.microsoft.cognitiveservices.speech.translation.TranslationRecognizer;

/*
 * Reference
 * https://docs.microsoft.com/zh-tw/azure/cognitive-services/speech-service/quickstart-java-jre
 * 
 * SubscriptionKey
 * https://docs.microsoft.com/zh-tw/azure/cognitive-services/speech-service/get-started
 * 
 * ServiceRegion
 * https://docs.microsoft.com/zh-tw/azure/cognitive-services/speech-service/regions
 * 
 */

public class Speechsdk_Main {

	private static Semaphore stopTranslationWithFileSemaphore;
	
	// Replace below with your own subscription key
    private static String speechSubscriptionKey = "b6802314338643e6ae6f4fae681020aa";
    // Replace below with your own service region (e.g., "westus").
    private static String serviceRegion = "westus";
	// voice file
    private static String voicefile = "Track01.wav";
    
    static TranslationRecognizer recognizer;
    
    /**
     * @param args Arguments are ignored in this sample.
     * @throws ExecutionException 
     * @throws InterruptedException 
     */
	
	public Speechsdk_Main() throws InterruptedException, ExecutionException
	{
		// input_microphone();
		translationWithFileAsync();
	}
	
//	private void input_microphone()
//	{
//		try {
//
//            int exitCode = 1;
//            SpeechConfig config = SpeechConfig.fromSubscription(speechSubscriptionKey, serviceRegion);
//            assert(config != null);
//
//            SpeechRecognizer reco = new SpeechRecognizer(config);
//            assert(reco != null);
//
//            System.out.println("Say something...");
//
//            Future<SpeechRecognitionResult> task = reco.recognizeOnceAsync();
//            assert(task != null);
//
//            SpeechRecognitionResult result = task.get();
//            assert(result != null);
//
//            if (result.getReason() == ResultReason.RecognizedSpeech) {
//                System.out.println("We recognized: " + result.getText());
//                exitCode = 0;
//            }
//            else if (result.getReason() == ResultReason.NoMatch) {
//                System.out.println("NOMATCH: Speech could not be recognized.");
//            }
//            else if (result.getReason() == ResultReason.Canceled) {
//                CancellationDetails cancellation = CancellationDetails.fromResult(result);
//                System.out.println("CANCELED: Reason=" + cancellation.getReason());
//
//                if (cancellation.getReason() == CancellationReason.Error) {
//                    System.out.println("CANCELED: ErrorCode=" + cancellation.getErrorCode());
//                    System.out.println("CANCELED: ErrorDetails=" + cancellation.getErrorDetails());
//                    System.out.println("CANCELED: Did you update the subscription info?");
//                }
//            }
//
//            reco.close();
//            
//            System.exit(exitCode);
//        } catch (Exception ex) {
//            System.out.println("Unexpected exception: " + ex.getMessage());
//
//            assert(false);
//            System.exit(1);
//        }
//	}
	
	public static void translationWithFileAsync() throws InterruptedException, ExecutionException
	{
	    stopTranslationWithFileSemaphore = new Semaphore(0);

	    // Creates an instance of a speech translation config with specified
	    // subscription key and service region. Replace with your own subscription key
	    // and service region (e.g., "westus").
	    SpeechTranslationConfig config = SpeechTranslationConfig.fromSubscription(speechSubscriptionKey, serviceRegion);

	    // Sets source and target languages
	    String fromLanguage = "en-US";	    
	    config.setSpeechRecognitionLanguage(fromLanguage);
	    config.addTargetLanguage("en-US");
//	    config.addTargetLanguage("fr");

	    // Creates a translation recognizer using file as audio input.
	    // Replace with your own audio file name.
	    AudioConfig audioInput = AudioConfig.fromWavFileInput(voicefile);
	    recognizer = new TranslationRecognizer(config, audioInput);	    
	    {	    	
	    	// Subscribes to events.	    	
	        recognizer.recognizing.addEventListener((s, e) -> 
	        {
	            System.out.println("RECOGNIZING in '" + fromLanguage + "': Text=" + e.getResult().getText());

	            Map<String, String> map = e.getResult().getTranslations();
	            for(String element : map.keySet()) {
	                System.out.println("    TRANSLATING into '" + element + "'': " + map.get(element));
	            }
	        });

	        recognizer.recognized.addEventListener((s, e) -> 
	        {
	            if (e.getResult().getReason() == ResultReason.TranslatedSpeech) {
	                System.out.println("RECOGNIZED in '" + fromLanguage + "': Text=" + e.getResult().getText());

	                Map<String, String> map = e.getResult().getTranslations();
	                for(String element : map.keySet()) {
	                    System.out.println("    TRANSLATED into '" + element + "'': " + map.get(element));
	                }
	            }
	            if (e.getResult().getReason() == ResultReason.RecognizedSpeech) {
	                System.out.println("RECOGNIZED: Text=" + e.getResult().getText());
	                System.out.println("    Speech not translated.");
	            }
	            else if (e.getResult().getReason() == ResultReason.NoMatch) {
	                System.out.println("NOMATCH: Speech could not be recognized.");
	            }
	        });

	        recognizer.canceled.addEventListener((s, e) -> 
	        {
	            System.out.println("CANCELED: Reason=" + e.getReason());

	            if (e.getReason() == CancellationReason.Error) {
	                System.out.println("CANCELED: ErrorCode=" + e.getErrorCode());
	                System.out.println("CANCELED: ErrorDetails=" + e.getErrorDetails());
	                System.out.println("CANCELED: Did you update the subscription info?");
	            }

	            stopTranslationWithFileSemaphore.release();;
	        });

	        recognizer.sessionStarted.addEventListener((s, e) -> {
	            System.out.println("\nSession started event.");
	        });

	        recognizer.sessionStopped.addEventListener((s, e) -> {
	            System.out.println("\nSession stopped event.");

	            // Stops translation when session stop is detected.
	            System.out.println("\nStop translation.");
	            stopTranslationWithFileSemaphore.release();;
	        });

	        // Starts continuous recognition. Uses StopContinuousRecognitionAsync() to stop recognition.
//	        System.out.println("Start translation...");
//	        recognizer.startContinuousRecognitionAsync().get();

	        // Waits for completion.
//	        stopTranslationWithFileSemaphore.acquire();;

	        // Stops translation.
//	        recognizer.stopContinuousRecognitionAsync().get();
	    }
	}
	
    public static void main(String[] args) 
    {
    	try {
			Speechsdk_Main speech = new Speechsdk_Main();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ExecutionException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}    	
    }
}
