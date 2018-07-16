# android-youtube-dl
The android library that wraps Python 2.7 and youtube-dl python scripts.

The library uses Python 2.7 distribution from: https://github.com/kivy/python-for-android project

The forked version of python-for-android could be found there: 
https://github.com/Redwid/python-for-android
It has modified native wrapper to work with ``org.redwid.android.youtube.dl.YoutubeDlService`` 

The forked version of python youtube-dl application could be found there: 
https://github.com/Redwid/youtube-dl
It has a few changes to be able to work on android with Python 2.7, in additional it writes the output into files instead of printing to console.

## Usage
You could embed that library into your own android application (please see /app as an example).

Or install the app and invoke it by using intents:

Firstly add this constants to your code:
```java    
    public static final String ACTION_DUMP_JSON =    "org.redwid.android.youtube.dl.action.DUMP_JSON";
    public static final String JSON_RESULT_SUCCESS = "org.redwid.android.youtube.dl.result.JSON_RESULT_SUCCESS";
    public static final String JSON_RESULT_ERROR =   "org.redwid.android.youtube.dl.result.JSON_RESULT_ERROR";
    public static final String VALUE_JSON = "JSON";
    public static final String VALUE_URL = "URL";
    public static final String VALUE_TIME_OUT = "TIME_OUT";
```  
Register broadcast receiver:
```java        
           final IntentFilter intentFilter = new IntentFilter();
           intentFilter.addAction(JSON_RESULT_SUCCESS);
           intentFilter.addAction(JSON_RESULT_ERROR);
           this.context.registerReceiver(new BroadcastReceiver() {
               @Override
               public void onReceive(final Context context, final Intent intent) {
                   if(JSON_RESULT_SUCCESS.equals(intent.getAction()) || JSON_RESULT_ERROR.equals(intent.getAction())) {                                             
                        if (JSON_RESULT_ERROR.equals(intent.getAction())) {
                            Log.d(TAG, "onReceive(): JSON_RESULT_ERROR");
                        } else if (JSON_RESULT_SUCCESS.equals(intent.getAction())) {                            
                            Log.d(TAG, "onReceive(): JSON_RESULT_SUCCESS");                              
                            final String jsonAsString = intent.getStringExtra(VALUE_JSON);
                            //Paase youtube-dl json response     
                        }                   
                   }
               }
           }, intentFilter);
```
And finally send intent to android-youtube-dl, where %url% is a link to your youtube video:
```java        
    final Intent intent = new Intent();
    intent.setClassName("org.redwid.android.youtube.dl.harness", "org.redwid.android.youtube.dl.YoutubeDlService");
    intent.setAction(ACTION_DUMP_JSON);
    intent.putExtra(VALUE_URL, %url%);
    context.startService(serviceIntent);    
``` 
When ``YoutubeDlService`` finished, your application will receive ``JSON_RESULT_SUCCESS`` or ``JSON_RESULT_ERROR`` intents.  

## Update 
If you would like to customize youtube-dl python scripts or android native wrapper - re build private.mp3 and jniLibs: 
1. Clone youtube-dl:
git clone https://github.com/Redwid/youtube-dl.git
2. Clone python-for-android:
git clone https://github.com/Redwid/python-for-android.git
3. Execute sh /python-for-android/clean-build-copy.sh
4. Find you new python and youtube-dl package in /libs/src/main/assets/private.mp3 
and native libraries in /libs/src/main/jniLibs/

## App
You could use the sample application to find out how to use *android-youtube-dl* 
Application has one screen. 
If you share any video from youtube android application to *Android Youtube-Dl*, after processing it will display video meta info and all discovered links:
![alt text](/app/distr/ss0.png) ![alt text](/app/distr/ss1.png) 

## License
[Apache 2.0](http://www.apache.org/licenses/LICENSE-2.0.html)