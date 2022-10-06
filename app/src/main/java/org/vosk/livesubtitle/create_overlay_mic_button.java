package org.vosk.livesubtitle;

import android.annotation.SuppressLint;
import android.app.Service;
import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.Color;
import android.os.IBinder;
import android.view.Gravity;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;

public class create_overlay_mic_button extends Service{

    public create_overlay_mic_button() {}

    private GlobalOverlay mGlobalOverlay_mic_button;
    @SuppressLint("StaticFieldLeak")
    public static ImageView mic_button;

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    public void onCreate() {
        super.onCreate();
        create_mic_button();
    }

    @Override
    public void onConfigurationChanged(@NonNull Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        if (newConfig.orientation == Configuration.ORIENTATION_LANDSCAPE) {
            if (RECOGNIZING_STATUS.RECOGNIZING) {
                mic_button.setImageResource(R.drawable.ic_mic_black_on);
            }
        }
        else if (newConfig.orientation == Configuration.ORIENTATION_PORTRAIT){
            mic_button.setForegroundGravity(Gravity.CENTER_HORIZONTAL);
            if (RECOGNIZING_STATUS.RECOGNIZING) {
                mic_button.setImageResource(R.drawable.ic_mic_black_on);
            }
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (mGlobalOverlay_mic_button != null) {
            //OVERLAYING_STATUS.OVERLAYING = false;
            mGlobalOverlay_mic_button.removeOverlayView(mic_button);
        }
        if (IS_OVER_REMOVEVIEW_STATUS.IS_OVER) {
            RECOGNIZING_STATUS.RECOGNIZING = false;
            OVERLAYING_STATUS.OVERLAYING = false;
            VOICE_TEXT.STRING = "";
            TRANSLATION_TEXT.STRING = "";
            MainActivity.voice_text.setText("");
            if (create_overlay_translation_text.overlay_translation_text != null) {
                create_overlay_translation_text.overlay_translation_text.setText("");
                create_overlay_translation_text.overlay_translation_text.setVisibility(View.INVISIBLE);
                create_overlay_translation_text.overlay_translation_text_container.setVisibility(View.INVISIBLE);
            }
            stop_vosk_voice_recognizer();
        }
    }

    private void create_mic_button() {
        mGlobalOverlay_mic_button = new GlobalOverlay(this);
        mic_button = new ImageView(this);
        if (!RECOGNIZING_STATUS.RECOGNIZING) {
            mic_button.setImageResource(R.drawable.ic_mic_black_off);
        } else {
            mic_button.setImageResource(R.drawable.ic_mic_black_on);
        }
        mic_button.setBackgroundColor(Color.parseColor("#80000000"));
        mGlobalOverlay_mic_button.addOverlayView(mic_button,
                96,
                96,
                (int) ((0.5 * DISPLAY_METRIC.DISPLAY_WIDTH) - (0.5 * 96)),
                0,
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        RECOGNIZING_STATUS.RECOGNIZING = !RECOGNIZING_STATUS.RECOGNIZING;
                        String string_recognizing = "Recognizing=" + RECOGNIZING_STATUS.RECOGNIZING;
                        MainActivity.textview_recognizing.setText(string_recognizing);
                        if (!RECOGNIZING_STATUS.RECOGNIZING) {
                            mic_button.setImageResource(R.drawable.ic_mic_black_off);
                            VOICE_TEXT.STRING = "";
                            TRANSLATION_TEXT.STRING = "";
                            if (create_overlay_translation_text.overlay_translation_text != null) {
                                create_overlay_translation_text.overlay_translation_text.setText("");
                                create_overlay_translation_text.overlay_translation_text.setVisibility(View.INVISIBLE);
                                create_overlay_translation_text.overlay_translation_text_container.setVisibility(View.INVISIBLE);
                            }
                            MainActivity.voice_text.setText("");
                            //MainActivity.audio.setStreamVolume(AudioManager.STREAM_NOTIFICATION, MainActivity.mStreamVolume, AudioManager.FLAG_REMOVE_SOUND_AND_VIBRATE);
                            stop_vosk_voice_recognizer();
                        } else {
                            //MainActivity.audio.setStreamVolume(AudioManager.STREAM_NOTIFICATION, 0, AudioManager.FLAG_REMOVE_SOUND_AND_VIBRATE);
                            mic_button.setImageResource(R.drawable.ic_mic_black_on);
                            if (TRANSLATION_TEXT.STRING.length() == 0) {
                                if (create_overlay_translation_text.overlay_translation_text != null) {
                                    create_overlay_translation_text.overlay_translation_text.setVisibility(View.INVISIBLE);
                                    create_overlay_translation_text.overlay_translation_text_container.setVisibility(View.INVISIBLE);
                                }
                            } else {
                                if (create_overlay_translation_text.overlay_translation_text != null) {
                                    create_overlay_translation_text.overlay_translation_text_container.setVisibility(View.VISIBLE);
                                    create_overlay_translation_text.overlay_translation_text.setVisibility(View.VISIBLE);
                                }
                            }
                            start_vosk_voice_recognizer();
                        }
                    }
                },
                new View.OnLongClickListener() {
                    @Override
                    public boolean onLongClick(View v) {
                        return false;
                    }
                },
                new GlobalOverlay.OnRemoveOverlayListener() {
                    @Override
                    public void onRemoveOverlay(View mic_button, boolean isRemovedByUser) {
                        //toast("onRemoveOverlay");
                        stopSelf();
                    }
                });

    }

    private void start_vosk_voice_recognizer() {
        Intent i = new Intent(this, VoskVoiceRecognizer.class);
        startService(i);
    }

    private void stop_vosk_voice_recognizer() {
        stopService(new Intent(this, VoskVoiceRecognizer.class));
    }

    private void toast(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }

}