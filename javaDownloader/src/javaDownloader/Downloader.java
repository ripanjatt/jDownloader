package javaDownloader;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.net.URL;
import java.text.DecimalFormat;

/*
Downloader class handles all the background downloading process,
Using the URL class available in Java builtin library!
 */

public class Downloader {

    private Listeners.ProgressListener progressListener;
    private Listeners.EventListener eventListener;
    private InputStream inputStream;
    private long size = 0;
    public boolean isPaused = false;
    private boolean isComplete = false;
    private String link, fileName, downloadPath;
    private long length;
    private DecimalFormat df;

    public Downloader(String link, String fileName, String downloadPath, long length){
        /*
        The only Constructor to initialize a Downloader Object!
        params:
        link            ->      the url to the file!
        fileName        ->      the name of the file to be downloaded!
        downloadPath    ->      the path to the download folder(eg: in android Environment.getExternalStorageDirectory() + "/Your_app_folder")!
        length          ->      the length of the file to be downloaded(can be equals to zero)!
         */
        this.progressListener = null;
        this.link = link;
        this.fileName = fileName;
        if(!downloadPath.endsWith("/")){
            downloadPath += "/";
        }
        this.downloadPath = downloadPath;
        this.length = length;
        df = new DecimalFormat("0.00");
    }

    public void setOnProgressListener(Listeners.ProgressListener progressListener){
        /*
        Method to set ProgressListener!
         */
        this.progressListener = progressListener;
    }

    public void setOnEventListener(Listeners.EventListener eventListener){
        /*
        Method to set EventListener!
         */
        this.eventListener = eventListener;
    }

    public void download(){
        /*
        Main downloading Thread!
        Retries every 3000 milliseconds in case of error!
         */
        new Thread(new Runnable() {
            @Override
            public void run() {
                FileOutputStream fos = null;
                if(progressListener != null){
                    progressListener.onStart();
                }
                try {
                    File dir = new File(downloadPath);
                    if(!dir.exists()){
                        boolean created = dir.mkdirs();
                        System.out.println(created);
                    }
                    if(size != 0){
                        /*
                        If the size is not 0, then it means that the downloader is either retrying or resuming!
                        This block reads the downloaded data and put it to the FileOutputStream!
                        This is to avoid re-downloading in case of error!
                        Since FileOutputStream recreates the file clearing the previous data,
                        So, this block handles any data loss!
                         */
                        size = new File(dir + "/" + fileName).length();
                        InputStream stream = new FileInputStream(dir + "/" + fileName);
                        byte[] data = new byte[(int) size];
                        //noinspection ResultOfMethodCallIgnored
                        stream.read(data);
                        stream.close();
                        fos = new FileOutputStream(new File(dir + "/" + fileName));
                        fos.write(data);
                    } else {
                        fos = new FileOutputStream(new File(dir + "/" + fileName));
                    }
                } catch (Exception e){
                    if(eventListener != null){
                        eventListener.onError("" + e);
                    }
                }
                while(!isPaused && !isComplete){
                    /*
                    Main downloading loop!
                     */
                    try{
                        URL url = new URL(link);
                        inputStream = url.openStream();
                        //noinspection ResultOfMethodCallIgnored
                        inputStream.skip(size);
                        byte[] data = new byte[1024];
                        int read;
                        long time = System.currentTimeMillis();
                        long temp = 0;
                        double speed;
                        while (!isPaused && (read = inputStream.read(data)) != -1) {
                            /*
                            This loop reads all the data from InputStream and write a chuck of bytes to the end of the file!
                             */
                            assert fos != null;
                            fos.write(data, 0, read);
                            size += read;
                            temp += read;
                            if(System.currentTimeMillis() - time > 1000){
                                /*
                                This block calculates the value of network speed!
                                It calculates the data downloaded in bytes after every second!
                                 */
                                speed = (temp * 1000.0) / ((System.currentTimeMillis() - time) * 1024.0);
                                time = System.currentTimeMillis();
                                temp = 0;
                                double tempSpeed = Double.parseDouble(df.format(speed));
                                if(progressListener != null){
                                    progressListener.onSpeedInKB(tempSpeed);
                                }
                            }
                            if(length != 0){
                                /*
                                This block calculates the percentage of data downloaded!
                                 */
                                double percent = Double.parseDouble(df.format((((size) / (float)length) * 100.0)));
                                if(progressListener != null){
                                    progressListener.onProgress(percent);
                                }
                            }
                        }
                        if(isPaused) {
                            if(eventListener != null){
                                eventListener.onPause();
                            }
                            break;
                        }
                        isComplete = true;
                        if(progressListener != null){
                            progressListener.onComplete();
                        }
                    } catch (Exception e){
                        try{
                            if(isPaused){
                                if(eventListener != null){
                                    eventListener.onPause();
                                }
                                break;
                            }
                            /*
                            This block handles any error and retries every 3000 milliseconds if downloading is not paused!
                             */
                            if(eventListener != null){
                                eventListener.isRetrying();
                            }
                            Thread.sleep(3000);
                        } catch (Exception e1){
                            if (eventListener != null){
                                eventListener.onError("" + e);
                            }
                        }
                    }
                }
            }
        }).start();
    }

    public void pause(){
        /*
        Method to pause the downloading!
         */
        isPaused = true;
        try {
            inputStream.close();
        } catch (Exception e){
            if(eventListener != null){
                eventListener.onError("" + e);
            }
        }
    }

    public void resume(){
        /*
        Method to resume the downloading!
         */
    	isPaused = false;
        download();
        if(eventListener != null){
            eventListener.onResume();
        }
    }
}

