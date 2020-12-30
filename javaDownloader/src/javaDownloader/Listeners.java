package javaDownloader;

/*
The Listener Class containing the Listener Interfaces!
 */
public class Listeners {

    /*
    ProgressListener to keep track of downloading process!
     */
    public interface ProgressListener {

        /*
        onStart gets triggered when download starts!
         */
        void onStart();

        /*
        onProgress keeps track of how much data is downloaded in percent!
        progressPercent = 0 if file length is not provided!
         */
        void onProgress(double progressPercent);

        /*
        onSpeedInKB gives the speed at which the data is being downloaded in Kilobytes per Second!
         */
        void onSpeedInKB(double speed);

        /*
        onComplete gets triggered when downloading process is finished successfully!
         */
        void onComplete();
    }

    /*
    EventListener to handle events like errors while downloading,
    pause/resume events etc!
     */
    public interface EventListener {

        /*
        onError gets triggered when an error occurs,
        like connection reset, file generation failure etc!
         */
        void onError(String error);

        /*
        onPause gets triggered when downloading is paused successfully!
         */
        void onPause();

        /*
        onResume gets triggered when downloading is resumed successfully!
         */
        void onResume();

        /*
        isRetrying gets triggered when retrying is in progress!
         */
        void isRetrying();
    }
}