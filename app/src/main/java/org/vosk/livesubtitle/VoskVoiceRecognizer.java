package org.vosk.livesubtitle;

import android.annotation.SuppressLint;
import android.app.Service;
import android.content.Intent;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.view.View;
import android.widget.Toast;

import com.google.mlkit.common.model.DownloadConditions;
import com.google.mlkit.nl.translate.Translation;
import com.google.mlkit.nl.translate.Translator;
import com.google.mlkit.nl.translate.TranslatorOptions;

import org.vosk.LibVosk;
import org.vosk.LogLevel;
import org.vosk.Model;
import org.vosk.Recognizer;
import org.vosk.android.RecognitionListener;
import org.vosk.android.SpeechService;
import org.vosk.android.SpeechStreamService;
import org.vosk.android.StorageService;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Locale;
import java.util.Objects;
import java.util.Timer;
import java.util.TimerTask;

public class VoskVoiceRecognizer extends Service implements RecognitionListener {
    public VoskVoiceRecognizer() {
    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }

    private Model model;
    private SpeechService speechService;
    private SpeechStreamService speechStreamService;
    private Translator translator;
    private String results;
    private String mlkit_status_message;

    /*android.text.TextWatcher tw = new android.text.TextWatcher() {
        public void afterTextChanged(android.text.Editable s) {}
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
        public void onTextChanged(CharSequence s, int start, int before, int count) {
            translate_api translate = new translate_api();
            if (RECOGNIZING_STATUS.RECOGNIZING) {
                if (MainActivity.voice_text != null) {
                    //get_translation(MainActivity.voice_text.getText(), LANGUAGE.SRC, LANGUAGE.DST);
                    if (MainActivity.voice_text.length() > 0) {
                        MainActivity.textview_debug2.setText(mlkit_status_message);
                    }
                    if (TRANSLATION_TEXT.STRING.length() == 0) {
                        create_overlay_translation_text.overlay_translation_text.setVisibility(View.INVISIBLE);
                        create_overlay_translation_text.overlay_translation_text_container.setVisibility(View.INVISIBLE);
                    } else {
                        create_overlay_translation_text.overlay_translation_text_container.setVisibility(View.VISIBLE);
                        create_overlay_translation_text.overlay_translation_text.setVisibility(View.VISIBLE);
                        create_overlay_translation_text.overlay_translation_text.setText(TRANSLATION_TEXT.STRING);
                        create_overlay_translation_text.overlay_translation_text.setSelection(create_overlay_translation_text.overlay_translation_text.getText().length());
                    }
                }
                translate.setOnTranslationCompleteListener(new translate_api.OnTranslationCompleteListener() {
                    @Override
                    public void onStartTranslation() {}

                    @Override
                    public void onCompleted(String text) {
                        TRANSLATION_TEXT.STRING = text;
                    }

                    @Override
                    public void onError(Exception e) {
                        //Toast.makeText(MainActivity.this, "Unknown error", Toast.LENGTH_SHORT).show();
                    }
                });
                translate.execute(VOICE_TEXT.STRING, LANGUAGE.SRC, LANGUAGE.DST);

            } else {
                if (translator != null) translator.close();
                create_overlay_translation_text.overlay_translation_text.setVisibility(View.INVISIBLE);
                create_overlay_translation_text.overlay_translation_text_container.setVisibility(View.INVISIBLE);
            }
        }
    };*/

    @Override
    public void onCreate() {
        super.onCreate();
        LibVosk.setLogLevel(LogLevel.INFO);
        //MainActivity.voice_text.addTextChangedListener(tw);
        if (translator != null) {
            translator.close();
        }
        if (speechService != null) {
            speechService.stop();
            speechService.shutdown();
            speechService = null;
        }
        if (speechStreamService != null) {
            speechStreamService.stop();
            speechStreamService = null;
        }

        int h;
        if (Objects.equals(LANGUAGE.SRC, "ja") || Objects.equals(LANGUAGE.SRC, "zh-Hans") || Objects.equals(LANGUAGE.SRC, "zh-Hant")) {
            h = 122;
        }
        else {
            h = 109;
        }
        MainActivity.voice_text.setHeight((int) (h * getResources().getDisplayMetrics().density));

        if (Objects.equals(VOSK_MODEL.ISO_CODE, "en-US")) {
            initModel();
        } else {
            initDownloadedModel();
        }

        if (RECOGNIZING_STATUS.RECOGNIZING) {
            Timer timer = new Timer();
            timer.schedule(new TimerTask() {
                @Override
                public void run() {
                    if (VOICE_TEXT.STRING != null) {
                        get_translation(VOICE_TEXT.STRING, LANGUAGE.SRC, LANGUAGE.DST);
                        //translate(VOICE_TEXT.STRING, LANGUAGE.SRC, LANGUAGE.DST);
                    }
                }
            },0,1000);
            if (VOICE_TEXT.STRING.length() > 0) {
                MainActivity.textview_debug2.setText(mlkit_status_message);
            }
        } else {
            if (translator != null) translator.close();
        }
    }

    private void initModel() {
        StorageService.unpack(this, VOSK_MODEL.ISO_CODE, "model", (model) -> {
                    this.model = model;
                    recognizeMicrophone();
                },
                (exception) -> setErrorState("Failed to unpack the model" + exception.getMessage()));
    }

    private void initDownloadedModel() {
        if (VOSK_MODEL.DOWNLOADED) {
            model = new Model(VOSK_MODEL.USED_PATH);
            recognizeMicrophone();
        } else {
            if (create_overlay_mic_button.mic_button != null) create_overlay_mic_button.mic_button.setImageResource(R.drawable.ic_mic_black_off);
            RECOGNIZING_STATUS.RECOGNIZING = false;
            stopSelf();
            String msg = "You need to download the model first";
            toast(msg);
        }
    }

    /*@Override
    public void onConfigurationChanged(@NonNull Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        if (newConfig.orientation == Configuration.ORIENTATION_LANDSCAPE) {
            if (RECOGNIZING_STATUS.RECOGNIZING) {
                if (speechService != null) speechService.startListening(this);
            } else {
                if (speechService != null) {
                    speechService.stop();
                    speechService.shutdown();
                }
                if (speechStreamService != null) {
                    speechStreamService.stop();
                }
                if (speechService != null) {
                    speechService.stop();
                }
                if (translator != null) {
                    translator.close();
                }
                stopSelf();
            }
        }

        else if (newConfig.orientation == Configuration.ORIENTATION_PORTRAIT){
            if (RECOGNIZING_STATUS.RECOGNIZING) {
                if (speechService != null) speechService.startListening(this);
            } else {
                if (speechService != null) {
                    speechService.stop();
                    speechService.shutdown();
                }
                if (speechStreamService != null) {
                    speechStreamService.stop();
                }
                if (speechService != null) {
                    speechService.stop();
                }
                if (translator != null) {
                    translator.close();
                }
                stopSelf();
            }
        }
    }*/

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (translator != null) {
            translator.close();
        }
        if (speechService != null) {
            speechService.stop();
            speechService.shutdown();
            speechService = null;
        }
        if (speechStreamService != null) {
            speechStreamService.stop();
            speechStreamService = null;
        }
        /*RECOGNIZING_STATUS.RECOGNIZING = false;
        OVERLAYING_STATUS.OVERLAYING = false;
        VOICE_TEXT.STRING = "";
        TRANSLATION_TEXT.STRING = "";
        MainActivity.voice_text.setText("");
        String hints = "Recognized words";
        MainActivity.voice_text.setHint(hints);
        MainActivity.textview_debug.setText("");
        MainActivity.audio.setStreamVolume(AudioManager.STREAM_NOTIFICATION, (int)Double.parseDouble(String.valueOf((long)(MainActivity.audio.getStreamMaxVolume(AudioManager.STREAM_NOTIFICATION) / 2))), 0);
        if (create_overlay_translation_text.overlay_translation_text != null) {
            create_overlay_translation_text.overlay_translation_text.setText("");
            create_overlay_translation_text.overlay_translation_text.setVisibility(View.INVISIBLE);
            create_overlay_translation_text.overlay_translation_text_container.setVisibility(View.INVISIBLE);
        }
        if (create_overlay_mic_button.mic_button != null) {
            create_overlay_mic_button.mic_button.setVisibility(View.INVISIBLE);
        }*/
    }

    @Override
    public void onPartialResult(String hypothesis) {
        if (hypothesis != null) {
            results = (((((hypothesis.replace("text", ""))
                    .replace("{", ""))
                    .replace("}", ""))
                    .replace(":", ""))
                    .replace("partial", ""))
                    .replace("\"", "");
        }
        if (RECOGNIZING_STATUS.RECOGNIZING) {
            VOICE_TEXT.STRING = results.toLowerCase(Locale.forLanguageTag(LANGUAGE.SRC));
            MainActivity.voice_text.setText(VOICE_TEXT.STRING);
            MainActivity.voice_text.setSelection(MainActivity.voice_text.getText().length());
        }
        else {
            VOICE_TEXT.STRING = "";
            MainActivity.voice_text.setText("");
        }
    }

    @Override
    public void onResult(String hypothesis) {
        /*if (hypothesis != null) {
            results = (((((hypothesis.replace("text", ""))
                    .replace("{", ""))
                    .replace("}", ""))
                    .replace(":", ""))
                    .replace("partial", ""))
                    .replace("\"", "");
        }
        if (RECOGNIZING_STATUS.RECOGNIZING) {
            VOICE_TEXT.STRING = results.toLowerCase(Locale.forLanguageTag(LANGUAGE.SRC));
            MainActivity.voice_text.setText(VOICE_TEXT.STRING);
            MainActivity.voice_text.setSelection(MainActivity.voice_text.getText().length());
        }
        else {
            VOICE_TEXT.STRING = "";
            MainActivity.voice_text.setText("");
        }*/
    }

    @Override
    public void onFinalResult(String hypothesis) {
        /*if (hypothesis != null) {
            results = (((((hypothesis.replace("text", ""))
                    .replace("{", ""))
                    .replace("}", ""))
                    .replace(":", ""))
                    .replace("partial", ""))
                    .replace("\"", "");
        }

        if (RECOGNIZING_STATUS.RECOGNIZING) {
            VOICE_TEXT.STRING = results.toLowerCase(Locale.forLanguageTag(LANGUAGE.SRC));
            MainActivity.voice_text.setText(VOICE_TEXT.STRING);
            MainActivity.voice_text.setSelection(MainActivity.voice_text.getText().length());
        }
        else {
            VOICE_TEXT.STRING = "";
            MainActivity.voice_text.setText("");
        }*/

        /*if (speechStreamService != null) {
            speechStreamService = null;
        }*/
    }

    @Override
    public void onError(Exception e) {
        setErrorState(e.getMessage());
        speechService.startListening(this);
    }

    @Override
    public void onTimeout() {
        speechService.startListening(this);
    }

    /*private void setUiState(int state) {
        switch (state) {
            case STATE_START:
                MainActivity.textview_debug.setText(R.string.preparing);
                MainActivity.textview_debug.setMovementMethod(new ScrollingMovementMethod());
                break;
            case STATE_READY:
                MainActivity.textview_debug.setText(R.string.ready);
                break;
            case STATE_DONE:
                break;
            case STATE_FILE:
                MainActivity.textview_debug.setText(getString(R.string.starting));
                break;
            case STATE_MIC:
                MainActivity.textview_debug.setText(R.string.say_something);
                break;
            default:
                throw new IllegalStateException("Unexpected value: " + state);
        }
    }*/

    private void setErrorState(String message) {
        MainActivity.textview_debug.setText(message);
        if (speechService != null) speechService.startListening(this);
    }

    private void recognizeMicrophone() {
        if (speechService != null) {
            speechService.stop();
            speechService = null;
            String string_recognizing = "Recognizing=" + RECOGNIZING_STATUS.RECOGNIZING;
            MainActivity.textview_recognizing.setText(string_recognizing);
            String string_overlaying = "Overlaying=" + OVERLAYING_STATUS.OVERLAYING;
            MainActivity.textview_overlaying.setText(string_overlaying);
        } else {
            try {
                Recognizer rec = new Recognizer(model, 16000.0f);
                speechService = new SpeechService(rec, 16000.0f);
                speechService.startListening(this);
            } catch (IOException e) {
                setErrorState(e.getMessage());
            }
        }
    }

    @SuppressLint("SetTextI18n")
    private void get_translation(String text, String textFrom, String textTo) {
        TranslatorOptions options = new TranslatorOptions.Builder()
                .setSourceLanguage(textFrom)
                .setTargetLanguage(textTo)
                .build();

        translator = Translation.getClient(options);

        if (!MLKIT_DICTIONARY.READY) {
            String msg = "Downloading dictionary, please be patient";
            toast(msg);
            String downloaded_status_message = "Dictionary is ready";
            MainActivity.textview_debug2.setText(downloaded_status_message);
            DownloadConditions conditions = new DownloadConditions.Builder().build();
            translator.downloadModelIfNeeded(conditions)
                    .addOnSuccessListener(unused -> MLKIT_DICTIONARY.READY = true)
                    .addOnFailureListener(e -> {});
        }

        if (MLKIT_DICTIONARY.READY) {
            mlkit_status_message = "Dictionary is ready";
            MainActivity.textview_debug2.setText(mlkit_status_message);
            translator.translate(String.valueOf(text)).addOnSuccessListener(s -> {
                TRANSLATION_TEXT.STRING = s;
                if (RECOGNIZING_STATUS.RECOGNIZING) {
                    if (TRANSLATION_TEXT.STRING.length() == 0) {
                        create_overlay_translation_text.overlay_translation_text.setVisibility(View.INVISIBLE);
                        create_overlay_translation_text.overlay_translation_text_container.setVisibility(View.INVISIBLE);
                    } else {
                        create_overlay_translation_text.overlay_translation_text_container.setVisibility(View.VISIBLE);
                        create_overlay_translation_text.overlay_translation_text.setVisibility(View.VISIBLE);
                        create_overlay_translation_text.overlay_translation_text.setText(TRANSLATION_TEXT.STRING);
                        create_overlay_translation_text.overlay_translation_text.setSelection(create_overlay_translation_text.overlay_translation_text.getText().length());
                    }
                } else {
                    create_overlay_translation_text.overlay_translation_text.setVisibility(View.INVISIBLE);
                    create_overlay_translation_text.overlay_translation_text_container.setVisibility(View.INVISIBLE);
                }
            }).addOnFailureListener(e -> {});
        }
    }

    private void toast(String message) {
        //Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
        new Handler(Looper.getMainLooper()).post(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(getApplicationContext(), message, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void copyFile(String inputPath, String inputFile, String outputPath) {
        InputStream in;
        OutputStream out;

        try {
            File dir = new File (outputPath);
            if (!dir.exists()) {
                dir.mkdirs();
            }

            in = new FileInputStream(inputPath + inputFile);
            out = new FileOutputStream(outputPath + inputFile);

            byte[] buffer = new byte[1024];
            int read;
            while ((read = in.read(buffer)) != -1) {
                out.write(buffer, 0, read);
            }
            in.close();
            in = null;

            // write the output file (You have now copied the file)
            out.flush();
            out.close();
            out = null;

        }  catch (FileNotFoundException fnfe1) {
            //Log.e("tag", fnfe1.getMessage());
        }
        catch (Exception e) {
            //Log.e("tag", e.getMessage());
        }

    }

    public static void copyFolder(File source, File destination) {
        if (source.isDirectory()) {
            if (!destination.exists()) {
                destination.mkdirs();
            }
            String[] files = source.list();
            if (files != null) {
                for (String file : files) {
                    File srcFile = new File(source, file);
                    File destFile = new File(destination, file);
                    copyFolder(srcFile, destFile);
                }
            }
        } else {
            InputStream in = null;
            OutputStream out = null;
            try {
                in = new FileInputStream(source);
                out = new FileOutputStream(destination);
                byte[] buffer = new byte[1024];
                int length;
                while ((length = in.read(buffer)) > 0) {
                    out.write(buffer, 0, length);
                }
            } catch (Exception e) {
                try {
                    if (in != null) {
                        in.close();
                    }
                } catch (IOException e1) {
                    e1.printStackTrace();
                }

                try {
                    if (out != null) {
                        out.close();
                    }
                } catch (IOException e1) {
                    e1.printStackTrace();
                }
            }
        }
    }

    public void translate(String t, String src, String dst) {
        translate_api translate = new translate_api();
        translate.setOnTranslationCompleteListener(new translate_api.OnTranslationCompleteListener() {
            @Override
            public void onStartTranslation() {}

            @Override
            public void onCompleted(String text) {
                TRANSLATION_TEXT.STRING = text;
                if (RECOGNIZING_STATUS.RECOGNIZING) {
                    if (TRANSLATION_TEXT.STRING.length() == 0) {
                        create_overlay_translation_text.overlay_translation_text.setVisibility(View.INVISIBLE);
                        create_overlay_translation_text.overlay_translation_text_container.setVisibility(View.INVISIBLE);
                    } else {
                        create_overlay_translation_text.overlay_translation_text_container.setVisibility(View.VISIBLE);
                        create_overlay_translation_text.overlay_translation_text.setVisibility(View.VISIBLE);
                        create_overlay_translation_text.overlay_translation_text.setText(TRANSLATION_TEXT.STRING);
                        create_overlay_translation_text.overlay_translation_text.setSelection(create_overlay_translation_text.overlay_translation_text.getText().length());
                    }
                } else {
                    create_overlay_translation_text.overlay_translation_text.setVisibility(View.INVISIBLE);
                    create_overlay_translation_text.overlay_translation_text_container.setVisibility(View.INVISIBLE);
                }
            }

            @Override
            public void onError(Exception e) {
                //Toast.makeText(MainActivity.this, "Unknown error", Toast.LENGTH_SHORT).show();
            }
        });
        translate.execute(t, src, dst);
    }
}
