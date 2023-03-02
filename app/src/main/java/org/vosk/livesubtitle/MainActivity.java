package org.vosk.livesubtitle;

import static android.text.TextUtils.substring;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.media.AudioManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.provider.Settings;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.mlkit.common.model.DownloadConditions;
import com.google.mlkit.nl.translate.Translation;
import com.google.mlkit.nl.translate.Translator;
import com.google.mlkit.nl.translate.TranslatorOptions;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class MainActivity extends AppCompatActivity {

    public static AudioManager audio;
    public static int mStreamVolume;

    private final ArrayList<String> arraylist_models = new ArrayList<>();
    private final ArrayList<String> arraylist_models_URL = new ArrayList<>();
    private final ArrayList<String> arraylist_src = new ArrayList<>();
    private final ArrayList<String> arraylist_dst = new ArrayList<>();
    private final ArrayList<String> arraylist_src_languages = new ArrayList<>();
    private final ArrayList<String> arraylist_dst_languages = new ArrayList<>();
    private final Map<String, String> map_model_country = new HashMap<>();
    private final Map<String, String> map_src_country = new HashMap<>();
    private final Map<String, String> map_dst_country = new HashMap<>();
    private final Map<String, String> map_country_models_URL = new HashMap<>();

    private CheckBox checkbox_debug_mode;
    private Spinner spinner_src_languages;
    private Button button_delete_model;
    private Button button_download_model;
    private Button button_cancel;
    private ProgressBar mProgressBar;
    private TextView textview_model_URL;
    private TextView textview_model_zip_file;
    private TextView textview_file_size;
    private TextView textview_bytes_downloaded;
    private TextView textview_server_response;
    private TextView textview_model_used_path;
    private TextView textview_src;
    private Spinner spinner_dst_languages;
    private TextView textview_dst;
    @SuppressLint("StaticFieldLeak")
    public static TextView textview_recognizing;
    @SuppressLint("StaticFieldLeak")
    public static TextView textview_overlaying;
    @SuppressLint("StaticFieldLeak")
    public static TextView textview_output_messages;
    @SuppressLint("StaticFieldLeak")
    public static TextView textview_mlkit_status;
    @SuppressLint("StaticFieldLeak")
    public static EditText voice_text;

    Thread thread_download_vosk_model;

    private Translator translator;
    private TranslatorOptions options;
    private DownloadConditions conditions;

    private String string_en_src_folder;
    private String string_en_dst_folder;
    private String string_src_en_folder;
    private String string_dst_en_folder;
    private File file_en_src_folder;
    private File file_en_dst_folder;
    private File file_src_en_folder;
    private File file_dst_en_folder;
    private String mlkit_status_message = "";

    //DON'T FORGET TO MODIFY AndroidManifest.xml
    //         <activity
    //            android:name=".MainActivity"
    //            android:configChanges="keyboardHidden|screenSize|orientation|screenLayout|navigation"


    @SuppressLint({"ClickableViewAccessibility", "QueryPermissionsNeeded", "SetTextI18n"})
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        arraylist_models.add("en-US");
        arraylist_models.add("zh-CN");
        arraylist_models.add("ru-RU");
        arraylist_models.add("fr-FR");
        arraylist_models.add("de-DE");
        arraylist_models.add("es-ES");
        arraylist_models.add("pt-PT");
        arraylist_models.add("tr-TR");
        arraylist_models.add("vi-VN");
        arraylist_models.add("it-IT");
        arraylist_models.add("nl-NL");
        arraylist_models.add("ca-ES");
        arraylist_models.add("fa-IR");
        arraylist_models.add("uk-UA");
        arraylist_models.add("kk-KZ");
        arraylist_models.add("sv-SE");
        arraylist_models.add("ja-JP");
        arraylist_models.add("eo-EO");
        arraylist_models.add("hi-IN");
        arraylist_models.add("cs-CZ");
        arraylist_models.add("pl-PL");

        arraylist_src.add("en");
        arraylist_src.add("zh");
        arraylist_src.add("ru");
        arraylist_src.add("fr");
        arraylist_src.add("de");
        arraylist_src.add("es");
        arraylist_src.add("pt");
        arraylist_src.add("tr");
        arraylist_src.add("vi");
        arraylist_src.add("it");
        arraylist_src.add("nl");
        arraylist_src.add("ca");
        arraylist_src.add("fa");
        arraylist_src.add("uk");
        arraylist_src.add("kk");
        arraylist_src.add("sv");
        arraylist_src.add("ja");
        arraylist_src.add("eo");
        arraylist_src.add("hi");
        arraylist_src.add("cs");
        arraylist_src.add("pl");

        arraylist_src_languages.add("English");
        arraylist_src_languages.add("Chinese");
        arraylist_src_languages.add("Russian");
        arraylist_src_languages.add("French");
        arraylist_src_languages.add("German");
        arraylist_src_languages.add("Spanish");
        arraylist_src_languages.add("Portuguese");
        arraylist_src_languages.add("Turkish");
        arraylist_src_languages.add("Vietnamese");
        arraylist_src_languages.add("Italian");
        arraylist_src_languages.add("Dutch");
        arraylist_src_languages.add("Catalan");
        arraylist_src_languages.add("Persian");
        arraylist_src_languages.add("Ukrainian");
        arraylist_src_languages.add("Kazakh");
        arraylist_src_languages.add("Swedish");
        arraylist_src_languages.add("Japanese");
        arraylist_src_languages.add("Esperanto");
        arraylist_src_languages.add("Hindi");
        arraylist_src_languages.add("Czech");
        arraylist_src_languages.add("Polish");

        arraylist_models_URL.add("https://alphacephei.com/vosk/models/vosk-model-small-en-us-0.15.zip");
        arraylist_models_URL.add("https://alphacephei.com/vosk/models/vosk-model-small-cn-0.22.zip");
        arraylist_models_URL.add("https://alphacephei.com/vosk/models/vosk-model-small-ru-0.22.zip");
        arraylist_models_URL.add("https://alphacephei.com/vosk/models/vosk-model-small-fr-0.22.zip");
        arraylist_models_URL.add("https://alphacephei.com/vosk/models/vosk-model-small-de-0.15.zip");
        arraylist_models_URL.add("https://alphacephei.com/vosk/models/vosk-model-small-es-0.42.zip");
        arraylist_models_URL.add("https://alphacephei.com/vosk/models/vosk-model-small-pt-0.3.zip");
        arraylist_models_URL.add("https://alphacephei.com/vosk/models/vosk-model-small-tr-0.3.zip");
        arraylist_models_URL.add("https://alphacephei.com/vosk/models/vosk-model-small-vn-0.3.zip");
        arraylist_models_URL.add("https://alphacephei.com/vosk/models/vosk-model-small-it-0.22.zip");
        arraylist_models_URL.add("https://alphacephei.com/vosk/models/vosk-model-small-nl-0.22.zip");
        arraylist_models_URL.add("https://alphacephei.com/vosk/models/vosk-model-small-ca-0.4.zip");
        arraylist_models_URL.add("https://alphacephei.com/vosk/models/vosk-model-small-fa-0.5.zip");
        arraylist_models_URL.add("https://alphacephei.com/vosk/models/vosk-model-small-uk-v3-small.zip");
        arraylist_models_URL.add("https://alphacephei.com/vosk/models/vosk-model-small-kz-0.15.zip");
        arraylist_models_URL.add("https://alphacephei.com/vosk/models/vosk-model-small-sv-rhasspy-0.15.zip");
        arraylist_models_URL.add("https://alphacephei.com/vosk/models/vosk-model-small-ja-0.22.zip");
        arraylist_models_URL.add("https://alphacephei.com/vosk/models/vosk-model-small-eo-0.42.zip");
        arraylist_models_URL.add("https://alphacephei.com/vosk/models/vosk-model-small-hi-0.22.zip");
        arraylist_models_URL.add("https://alphacephei.com/vosk/models/vosk-model-small-cs-0.4-rhasspy.zip");
        arraylist_models_URL.add("https://alphacephei.com/vosk/models/vosk-model-small-pl-0.22.zip");

        for (int i = 0; i < arraylist_src_languages.size(); i++) {
            map_model_country.put(arraylist_src_languages.get(i), arraylist_models.get(i));
        }

        for (int i = 0; i < arraylist_src_languages.size(); i++) {
            map_src_country.put(arraylist_src_languages.get(i), arraylist_src.get(i));
        }

        for (int i = 0; i < arraylist_src_languages.size(); i++) {
            map_country_models_URL.put(arraylist_src_languages.get(i), arraylist_models_URL.get(i));
        }

        arraylist_dst.add("af");
        arraylist_dst.add("ar");
        arraylist_dst.add("be");
        arraylist_dst.add("bg");
        arraylist_dst.add("bn");
        arraylist_dst.add("ca");
        arraylist_dst.add("cs");
        arraylist_dst.add("cy");
        arraylist_dst.add("da");
        arraylist_dst.add("de");
        arraylist_dst.add("el");
        arraylist_dst.add("en");
        arraylist_dst.add("eo");
        arraylist_dst.add("es");
        arraylist_dst.add("et");
        arraylist_dst.add("fa");
        arraylist_dst.add("fi");
        arraylist_dst.add("fr");
        arraylist_dst.add("ga");
        arraylist_dst.add("gl");
        arraylist_dst.add("gu");
        arraylist_dst.add("he");
        arraylist_dst.add("hi");
        arraylist_dst.add("hr");
        arraylist_dst.add("ht");
        arraylist_dst.add("hu");
        arraylist_dst.add("id");
        arraylist_dst.add("is");
        arraylist_dst.add("it");
        arraylist_dst.add("ja");
        arraylist_dst.add("ka");
        arraylist_dst.add("kn");
        arraylist_dst.add("ko");
        arraylist_dst.add("lt");
        arraylist_dst.add("lv");
        arraylist_dst.add("mk");
        arraylist_dst.add("mr");
        arraylist_dst.add("ms");
        arraylist_dst.add("mt");
        arraylist_dst.add("nl");
        arraylist_dst.add("no");
        arraylist_dst.add("pl");
        arraylist_dst.add("pt");
        arraylist_dst.add("ro");
        arraylist_dst.add("ru");
        arraylist_dst.add("sk");
        arraylist_dst.add("sl");
        arraylist_dst.add("sq");
        arraylist_dst.add("sv");
        arraylist_dst.add("sw");
        arraylist_dst.add("ta");
        arraylist_dst.add("te");
        arraylist_dst.add("th");
        arraylist_dst.add("tl");
        arraylist_dst.add("tr");
        arraylist_dst.add("uk");
        arraylist_dst.add("ur");
        arraylist_dst.add("vi");
        arraylist_dst.add("zh");

        arraylist_dst_languages.add("Afrikaans");
        arraylist_dst_languages.add("Arabic");
        arraylist_dst_languages.add("Belarusian");
        arraylist_dst_languages.add("Bulgarian");
        arraylist_dst_languages.add("Bengali");
        arraylist_dst_languages.add("Catalan");
        arraylist_dst_languages.add("Czech");
        arraylist_dst_languages.add("Welsh");
        arraylist_dst_languages.add("Danish");
        arraylist_dst_languages.add("German");
        arraylist_dst_languages.add("Greek");
        arraylist_dst_languages.add("English");
        arraylist_dst_languages.add("Esperanto");
        arraylist_dst_languages.add("Spanish");
        arraylist_dst_languages.add("Estonian");
        arraylist_dst_languages.add("Persian");
        arraylist_dst_languages.add("Finnish");
        arraylist_dst_languages.add("French");
        arraylist_dst_languages.add("Irish");
        arraylist_dst_languages.add("Galician");
        arraylist_dst_languages.add("Gujarati");
        arraylist_dst_languages.add("Hebrew");
        arraylist_dst_languages.add("Hindi");
        arraylist_dst_languages.add("Croatian");
        arraylist_dst_languages.add("Haitian");
        arraylist_dst_languages.add("Hungarian");
        arraylist_dst_languages.add("Indonesian");
        arraylist_dst_languages.add("Icelandic");
        arraylist_dst_languages.add("Italian");
        arraylist_dst_languages.add("Japanese");
        arraylist_dst_languages.add("Georgian");
        arraylist_dst_languages.add("Kannada");
        arraylist_dst_languages.add("Korean");
        arraylist_dst_languages.add("Lithuania");
        arraylist_dst_languages.add("Latvian");
        arraylist_dst_languages.add("Macedonian");
        arraylist_dst_languages.add("Marathi");
        arraylist_dst_languages.add("Malay");
        arraylist_dst_languages.add("Maltese");
        arraylist_dst_languages.add("Dutch");
        arraylist_dst_languages.add("Norwegia");
        arraylist_dst_languages.add("Polish");
        arraylist_dst_languages.add("Portuguese");
        arraylist_dst_languages.add("Romanian");
        arraylist_dst_languages.add("Russian");
        arraylist_dst_languages.add("Slovak");
        arraylist_dst_languages.add("Slovenian");
        arraylist_dst_languages.add("Albanian");
        arraylist_dst_languages.add("Swedish");
        arraylist_dst_languages.add("Swahili");
        arraylist_dst_languages.add("Tamil");
        arraylist_dst_languages.add("Telugu");
        arraylist_dst_languages.add("Thai");
        arraylist_dst_languages.add("Tagalog");
        arraylist_dst_languages.add("Turkish");
        arraylist_dst_languages.add("Ukrainian");
        arraylist_dst_languages.add("Urdu");
        arraylist_dst_languages.add("Vietnamese");
        arraylist_dst_languages.add("Chinese");

        for (int i = 0; i < arraylist_dst_languages.size(); i++) {
            map_dst_country.put(arraylist_dst_languages.get(i), arraylist_dst.get(i));
        }

        checkbox_debug_mode = findViewById(R.id.checkbox_debug_mode);
        spinner_src_languages = findViewById(R.id.spinner_src_languages);
        setup_src_spinner(arraylist_src_languages);
        button_delete_model = findViewById(R.id.button_delete_model);
        button_download_model = findViewById(R.id.button_download_model);
        button_cancel = findViewById(R.id.button_cancel);
        mProgressBar = findViewById(R.id.mProgressBar);
        textview_model_URL = findViewById(R.id.textview_model_URL);
        textview_model_zip_file = findViewById(R.id.textview_model_zip_file);
        textview_file_size = findViewById(R.id.textview_file_size);
        textview_bytes_downloaded = findViewById(R.id.textview_bytes_downloaded);
        textview_server_response = findViewById(R.id.textview_server_response);
        textview_model_used_path = findViewById(R.id.textview_model_used_path);
        textview_src = findViewById(R.id.textview_src);
        spinner_dst_languages = findViewById(R.id.spinner_dst_languages);
        setup_dst_spinner(arraylist_dst_languages);
        textview_dst = findViewById(R.id.textview_dst);
        textview_recognizing = findViewById(R.id.textview_recognizing);
        textview_overlaying = findViewById(R.id.textview_overlaying);
        Button button_toggle_overlay = findViewById(R.id.button_toggle_overlay);
        textview_output_messages = findViewById(R.id.textview_output_messages);
        textview_mlkit_status = findViewById(R.id.textview_mlkit_status);
        voice_text = findViewById(R.id.voice_text);

        audio = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
        mStreamVolume = audio.getStreamVolume(AudioManager.STREAM_NOTIFICATION);

        RECOGNIZING_STATUS.IS_RECOGNIZING = false;
        RECOGNIZING_STATUS.STRING = "RECOGNIZING_STATUS.IS_RECOGNIZING = " + RECOGNIZING_STATUS.IS_RECOGNIZING;
        setText(textview_recognizing, RECOGNIZING_STATUS.STRING);
        OVERLAYING_STATUS.IS_OVERLAYING = false;
        OVERLAYING_STATUS.STRING = "OVERLAYING_STATUS.IS_OVERLAYING = " + OVERLAYING_STATUS.IS_OVERLAYING;
        setText(textview_overlaying, OVERLAYING_STATUS.STRING);

        NotificationManager notificationManager = (NotificationManager) this.getSystemService(Context.NOTIFICATION_SERVICE);
        if (notificationManager.isNotificationPolicyAccessGranted()) {
            audio.setStreamVolume(AudioManager.STREAM_NOTIFICATION, (int) Double.parseDouble(String.valueOf((long) (audio.getStreamMaxVolume(AudioManager.STREAM_NOTIFICATION) / 2))), 0);
        }
        else {
            startActivity(new Intent(android.provider.Settings.ACTION_NOTIFICATION_POLICY_ACCESS_SETTINGS));
        }

        VOICE_TEXT.STRING = "";
        TRANSLATION_TEXT.STRING = "";

        DisplayMetrics display = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(display);
        float d = display.density;
        DISPLAY_METRIC.DISPLAY_WIDTH = display.widthPixels;
        DISPLAY_METRIC.DISPLAY_HEIGHT = display.heightPixels;
        DISPLAY_METRIC.DISPLAY_DENSITY = d;
        //setText(textview_output_messages, DISPLAY_METRIC.DISPLAY_WIDTH+","+DISPLAY_METRIC.DISPLAY_HEIGHT);

        int h;
        if (Objects.equals(LANGUAGE.DST, "ja") || Objects.equals(LANGUAGE.DST, "zh-CN") || Objects.equals(LANGUAGE.DST, "zh-TW")) {
            h = 75;
        }
        else {
            h = 62;
        }
        voice_text.setHeight((int) (h * getResources().getDisplayMetrics().density));

        MLKIT_DICTIONARY.READY = false;
        VOSK_MODEL.IS_DOWNLOADING = false;

        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM);
            getSupportActionBar().setCustomView(R.layout.actionbar_layout);
        }

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.RECORD_AUDIO}, 1);
        }

        checkbox_debug_mode.setOnClickListener(view -> {
            if(((CompoundButton) view).isChecked()){
                textview_src.setVisibility(View.VISIBLE);
                textview_dst.setVisibility(View.VISIBLE);
                textview_recognizing.setVisibility(View.VISIBLE);
                setText(textview_recognizing, RECOGNIZING_STATUS.STRING);
                textview_overlaying.setVisibility(View.VISIBLE);
                setText(textview_overlaying, OVERLAYING_STATUS.STRING);
                textview_output_messages.setVisibility(View.VISIBLE);
                textview_mlkit_status.setVisibility(View.VISIBLE);
                if (LANGUAGE.SRC != null) {
                    String ls  = "LANGUAGE.SRC = " + LANGUAGE.SRC;
                    setText(textview_src, ls);
                }
                else {
                    textview_src.setHint("LANGUAGE.SRC");
                }
                if (LANGUAGE.DST != null) {
                    String ld = "LANGUAGE.DST = " + LANGUAGE.DST;
                    setText(textview_dst, ld);
                }
                else {
                    textview_src.setHint("LANGUAGE.SRC");
                }
                if (!Objects.equals(VOSK_MODEL.ISO_CODE, "en-US")) {
                    if (new File(VOSK_MODEL.EXTRACTED_PATH + VOSK_MODEL.ISO_CODE).exists()) {
                        textview_model_used_path.setVisibility(View.VISIBLE);
                        String string_model_used_path = "VOSK model used path=" + VOSK_MODEL.USED_PATH;
                        setText(textview_model_used_path, string_model_used_path);
                    }
                    else {
                        textview_model_URL.setVisibility(View.VISIBLE);
                        textview_server_response.setVisibility(View.VISIBLE);
                        textview_model_zip_file.setVisibility(View.VISIBLE);
                        textview_file_size.setVisibility(View.VISIBLE);
                        if (VOSK_MODEL.IS_DOWNLOADING) {
                            textview_bytes_downloaded.setVisibility(View.VISIBLE);
                        }
                        else {
                            textview_bytes_downloaded.setVisibility(View.GONE);
                        }
                    }
                }
            }
            else {
                textview_src.setVisibility(View.GONE);
                textview_dst.setVisibility(View.GONE);
                textview_recognizing.setVisibility(View.GONE);
                textview_overlaying.setVisibility(View.GONE);
                textview_model_used_path.setVisibility(View.GONE);
                textview_mlkit_status.setVisibility(View.GONE);
                if (!Objects.equals(VOSK_MODEL.ISO_CODE, "en-US")) {
                    if (new File(VOSK_MODEL.EXTRACTED_PATH + VOSK_MODEL.ISO_CODE).exists()) {
                        textview_model_used_path.setVisibility(View.GONE);
                    } else {
                        textview_model_URL.setVisibility(View.GONE);
                        textview_server_response.setVisibility(View.GONE);
                        textview_model_zip_file.setVisibility(View.GONE);
                        if (VOSK_MODEL.IS_DOWNLOADING) {
                            textview_file_size.setVisibility(View.VISIBLE);
                            textview_bytes_downloaded.setVisibility(View.VISIBLE);
                        }
                        else {
                            textview_file_size.setVisibility(View.GONE);
                            textview_bytes_downloaded.setVisibility(View.GONE);
                        }
                    }
                }
            }
        });

        if(checkbox_debug_mode.isChecked()){
            textview_src.setVisibility(View.VISIBLE);
            textview_dst.setVisibility(View.VISIBLE);
            textview_recognizing.setVisibility(View.VISIBLE);
            setText(textview_recognizing, RECOGNIZING_STATUS.STRING);
            textview_overlaying.setVisibility(View.VISIBLE);
            setText(textview_overlaying, OVERLAYING_STATUS.STRING);
            textview_output_messages.setVisibility(View.VISIBLE);
            textview_mlkit_status.setVisibility(View.VISIBLE);
            if (LANGUAGE.SRC != null) {
                String ls  = "LANGUAGE.SRC = " + LANGUAGE.SRC;
                setText(textview_src, ls);
            }
            else {
                textview_src.setHint("LANGUAGE.SRC");
            }
            if (LANGUAGE.DST != null) {
                String ld = "LANGUAGE.DST = " + LANGUAGE.DST;
                setText(textview_dst, ld);
            }
            else {
                textview_src.setHint("LANGUAGE.SRC");
            }
            if (!Objects.equals(VOSK_MODEL.ISO_CODE, "en-US")) {
                if (new File(VOSK_MODEL.EXTRACTED_PATH + VOSK_MODEL.ISO_CODE).exists()) {
                    textview_model_used_path.setVisibility(View.VISIBLE);
                    String string_model_used_path = "VOSK model used path=" + VOSK_MODEL.USED_PATH;
                    setText(textview_model_used_path, string_model_used_path);
                }
                else {
                    textview_model_URL.setVisibility(View.VISIBLE);
                    textview_server_response.setVisibility(View.VISIBLE);
                    textview_model_zip_file.setVisibility(View.VISIBLE);
                    textview_file_size.setVisibility(View.VISIBLE);
                    VOSK_MODEL.ZIP_FILE_SIZE = get_vosk_model_filesize(VOSK_MODEL.URL_ADDRESS);
                    String string_file_size = "VOSK_MODEL.ZIP_FILE_SIZE = " + VOSK_MODEL.ZIP_FILE_SIZE + " bytes";
                    setText(textview_file_size, string_file_size);
                    if (VOSK_MODEL.IS_DOWNLOADING) {
                        textview_bytes_downloaded.setVisibility(View.VISIBLE);
                    }
                    else {
                        textview_bytes_downloaded.setVisibility(View.GONE);
                    }
                }
            }
        }
        else {
            textview_src.setVisibility(View.GONE);
            textview_dst.setVisibility(View.GONE);
            textview_recognizing.setVisibility(View.GONE);
            textview_overlaying.setVisibility(View.GONE);
            textview_model_used_path.setVisibility(View.GONE);
            textview_mlkit_status.setVisibility(View.GONE);
            if (!Objects.equals(VOSK_MODEL.ISO_CODE, "en-US")) {
                if (new File(VOSK_MODEL.EXTRACTED_PATH + VOSK_MODEL.ISO_CODE).exists()) {
                    textview_model_used_path.setVisibility(View.GONE);
                } else {
                    textview_model_URL.setVisibility(View.GONE);
                    textview_server_response.setVisibility(View.GONE);
                    textview_model_zip_file.setVisibility(View.GONE);
                    if (VOSK_MODEL.IS_DOWNLOADING) {
                        textview_file_size.setVisibility(View.VISIBLE);
                        VOSK_MODEL.ZIP_FILE_SIZE = get_vosk_model_filesize(VOSK_MODEL.URL_ADDRESS);
                        String string_file_size = "File size = " + VOSK_MODEL.ZIP_FILE_SIZE + " bytes";
                        setText(textview_file_size, string_file_size);
                        textview_bytes_downloaded.setVisibility(View.VISIBLE);
                    }
                    else {
                        textview_file_size.setVisibility(View.GONE);
                        textview_bytes_downloaded.setVisibility(View.GONE);
                    }
                }
            }
        }

        spinner_src_languages.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                setText(textview_output_messages, "");

                LANGUAGE.SRC_COUNTRY = spinner_src_languages.getSelectedItem().toString();
                LANGUAGE.SRC = map_src_country.get(LANGUAGE.SRC_COUNTRY);
                String string_src = "LANGUAGE.SRC = " + LANGUAGE.SRC;
                setText(textview_src, string_src);

                LANGUAGE.DST_COUNTRY = spinner_dst_languages.getSelectedItem().toString();
                LANGUAGE.DST = map_dst_country.get(LANGUAGE.DST_COUNTRY);
                String string_dst = "LANGUAGE.DST = " + LANGUAGE.DST;
                setText(textview_dst, string_dst);

                VOSK_MODEL.ISO_CODE = map_model_country.get(LANGUAGE.SRC_COUNTRY);
                VOSK_MODEL.URL_ADDRESS = map_country_models_URL.get(LANGUAGE.SRC_COUNTRY);
                if (VOSK_MODEL.URL_ADDRESS != null) {
                    VOSK_MODEL.ZIP_FILENAME = substring(VOSK_MODEL.URL_ADDRESS, VOSK_MODEL.URL_ADDRESS.lastIndexOf('/') + 1, VOSK_MODEL.URL_ADDRESS.length());
                }
                VOSK_MODEL.SAVE_AS = getExternalFilesDir(null).getAbsolutePath() + File.separator + VOSK_MODEL.ZIP_FILENAME;
                VOSK_MODEL.EXTRACTED_PATH = getExternalFilesDir(null).getAbsolutePath() + File.separator + "downloaded" + File.separator;
                VOSK_MODEL.USED_PATH = VOSK_MODEL.EXTRACTED_PATH + VOSK_MODEL.ISO_CODE;

                String string_url = "VOSK_MODEL.URL_ADDRESS = " + VOSK_MODEL.URL_ADDRESS;
                setText(textview_model_URL, string_url);
                String string_zip_path = "VOSK_MODEL.SAVE_AS = " + VOSK_MODEL.SAVE_AS;
                setText(textview_model_zip_file, string_zip_path);
                check_vosk_downloaded_model(VOSK_MODEL.ISO_CODE);

                string_en_src_folder = Environment.getDataDirectory() + "/data/" + getApplicationContext().getPackageName() + "/no_backup/com.google.mlkit.translate.models/" + "en" + "_" + LANGUAGE.SRC;
                file_en_src_folder = new File(string_en_src_folder);
                string_en_dst_folder = Environment.getDataDirectory() + "/data/" + getApplicationContext().getPackageName() + "/no_backup/com.google.mlkit.translate.models/" + "en" + "_" + LANGUAGE.DST;
                file_en_dst_folder = new File(string_en_dst_folder);
                string_src_en_folder = Environment.getDataDirectory() + "/data/" + getApplicationContext().getPackageName() + "/no_backup/com.google.mlkit.translate.models/" + LANGUAGE.SRC + "_" + "en" ;
                file_src_en_folder = new File(string_src_en_folder);
                string_dst_en_folder = Environment.getDataDirectory() + "/data/" + getApplicationContext().getPackageName() + "/no_backup/com.google.mlkit.translate.models/" + LANGUAGE.DST + "_" + "en" ;
                file_dst_en_folder = new File(string_dst_en_folder);

                options = new TranslatorOptions.Builder()
                        .setSourceLanguage(LANGUAGE.SRC)
                        .setTargetLanguage(LANGUAGE.DST)
                        .build();
                translator = Translation.getClient(options);
                conditions = new DownloadConditions.Builder().build();
                check_mlkit_dictionary();

                int h;
                if (Objects.equals(LANGUAGE.DST, "ja") || Objects.equals(LANGUAGE.DST, "zh")) {
                    h = 75;
                }
                else {
                    h = 62;
                }
                voice_text.setHeight((int) (h * getResources().getDisplayMetrics().density));

                stop_vosk_voice_recognizer();
                stop_create_overlay_translation_text();
                stop_create_overlay_mic_button();

                if (OVERLAYING_STATUS.IS_OVERLAYING) {
                    if (!RECOGNIZING_STATUS.IS_RECOGNIZING) {
                        if (create_overlay_mic_button.mic_button != null) create_overlay_mic_button.mic_button.setImageResource(R.drawable.ic_mic_black_off);
                    } else {
                        start_vosk_voice_recognizer();
                        if (create_overlay_mic_button.mic_button != null) create_overlay_mic_button.mic_button.setImageResource(R.drawable.ic_mic_black_on);
                    }
                    start_create_overlay_mic_button();
                    if (create_overlay_mic_button.mic_button != null) create_overlay_mic_button.mic_button.setBackgroundColor(Color.parseColor("#80000000"));

                    start_create_overlay_translation_text();
                    if (TRANSLATION_TEXT.STRING.length() > 0) {
                        if (create_overlay_translation_text.overlay_translation_text != null) setText(create_overlay_translation_text.overlay_translation_text, TRANSLATION_TEXT.STRING);
                    }

                    if (!Objects.equals(VOSK_MODEL.ISO_CODE, "en-US")) {
                        if (!new File(VOSK_MODEL.EXTRACTED_PATH + VOSK_MODEL.ISO_CODE).exists()) {
                            stop_vosk_voice_recognizer();
                            stop_create_overlay_translation_text();
                            stop_create_overlay_mic_button();

                            RECOGNIZING_STATUS.IS_RECOGNIZING = false;
                            RECOGNIZING_STATUS.STRING = "RECOGNIZING_STATUS.IS_RECOGNIZING = " + RECOGNIZING_STATUS.IS_RECOGNIZING;
                            setText(textview_recognizing, RECOGNIZING_STATUS.STRING);

                            OVERLAYING_STATUS.IS_OVERLAYING = false;
                            OVERLAYING_STATUS.STRING = "OVERLAYING_STATUS.IS_OVERLAYING = " + OVERLAYING_STATUS.IS_OVERLAYING;
                            setText(textview_overlaying, OVERLAYING_STATUS.STRING);

                            String msg = "You have to download the model first";
                            setText(textview_output_messages, msg);
                        }
                    }
                }
                setText(textview_recognizing, RECOGNIZING_STATUS.STRING);
                setText(textview_overlaying, OVERLAYING_STATUS.STRING);
            }

            public void onNothingSelected(AdapterView<?> adapterView) {
                LANGUAGE.SRC_COUNTRY = spinner_src_languages.getSelectedItem().toString();
                LANGUAGE.SRC = map_src_country.get(LANGUAGE.SRC_COUNTRY);
                String string_src = "LANGUAGE.SRC = " + LANGUAGE.SRC;
                setText(textview_src, string_src);

                LANGUAGE.DST_COUNTRY = spinner_dst_languages.getSelectedItem().toString();
                LANGUAGE.DST = map_dst_country.get(LANGUAGE.DST_COUNTRY);
                String string_dst = "LANGUAGE.DST = " + LANGUAGE.DST;
                setText(textview_dst, string_dst);

                VOSK_MODEL.ISO_CODE = map_model_country.get(LANGUAGE.SRC_COUNTRY);
                VOSK_MODEL.URL_ADDRESS = map_country_models_URL.get(LANGUAGE.SRC_COUNTRY);
                if (VOSK_MODEL.URL_ADDRESS != null) {
                    VOSK_MODEL.ZIP_FILENAME = substring(VOSK_MODEL.URL_ADDRESS, VOSK_MODEL.URL_ADDRESS.lastIndexOf('/') + 1, VOSK_MODEL.URL_ADDRESS.length());
                }
                VOSK_MODEL.SAVE_AS = getExternalFilesDir(null).getAbsolutePath() + File.separator + VOSK_MODEL.ZIP_FILENAME;
                VOSK_MODEL.EXTRACTED_PATH = getExternalFilesDir(null).getAbsolutePath() + File.separator + "downloaded" + File.separator;
                VOSK_MODEL.USED_PATH = VOSK_MODEL.EXTRACTED_PATH + VOSK_MODEL.ISO_CODE;

                String string_url = "VOSK_MODEL.URL_ADDRESS = " + VOSK_MODEL.URL_ADDRESS;
                setText(textview_model_URL, string_url);
                String string_zip_path = "VOSK_MODEL.SAVE_AS = " + VOSK_MODEL.SAVE_AS;
                setText(textview_model_zip_file, string_zip_path);
                check_vosk_downloaded_model(VOSK_MODEL.ISO_CODE);
            }
        });

        spinner_dst_languages.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                setText(textview_output_messages, "");

                LANGUAGE.SRC_COUNTRY = spinner_src_languages.getSelectedItem().toString();
                LANGUAGE.SRC = map_src_country.get(LANGUAGE.SRC_COUNTRY);
                String string_src = "LANGUAGE.SRC = " + LANGUAGE.SRC;
                setText(textview_src, string_src);

                LANGUAGE.DST_COUNTRY = spinner_dst_languages.getSelectedItem().toString();
                LANGUAGE.DST = map_dst_country.get(LANGUAGE.DST_COUNTRY);
                String string_dst = "LANGUAGE.DST = " + LANGUAGE.DST;
                setText(textview_dst, string_dst);

                string_en_src_folder = Environment.getDataDirectory() + "/data/" + getApplicationContext().getPackageName() + "/no_backup/com.google.mlkit.translate.models/" + "en" + "_" + LANGUAGE.SRC;
                string_en_dst_folder = Environment.getDataDirectory() + "/data/" + getApplicationContext().getPackageName() + "/no_backup/com.google.mlkit.translate.models/" + "en" + "_" + LANGUAGE.DST;
                string_src_en_folder = Environment.getDataDirectory() + "/data/" + getApplicationContext().getPackageName() + "/no_backup/com.google.mlkit.translate.models/" + LANGUAGE.SRC + "_" + "en" ;
                string_dst_en_folder = Environment.getDataDirectory() + "/data/" + getApplicationContext().getPackageName() + "/no_backup/com.google.mlkit.translate.models/" + LANGUAGE.DST + "_" + "en" ;
                file_en_src_folder = new File(string_en_src_folder);
                file_en_dst_folder = new File(string_en_dst_folder);
                file_src_en_folder = new File(string_src_en_folder);
                file_dst_en_folder = new File(string_dst_en_folder);

                options = new TranslatorOptions.Builder()
                        .setSourceLanguage(LANGUAGE.SRC)
                        .setTargetLanguage(LANGUAGE.DST)
                        .build();
                translator = Translation.getClient(options);
                conditions = new DownloadConditions.Builder().build();
                check_mlkit_dictionary();

                int h;
                if (Objects.equals(LANGUAGE.DST, "ja") || Objects.equals(LANGUAGE.DST, "zh")) {
                    h = 75;
                }
                else {
                    h = 62;
                }
                voice_text.setHeight((int) (h * getResources().getDisplayMetrics().density));

                stop_vosk_voice_recognizer();
                stop_create_overlay_translation_text();
                stop_create_overlay_mic_button();

                if (OVERLAYING_STATUS.IS_OVERLAYING) {
                    if (!RECOGNIZING_STATUS.IS_RECOGNIZING) {
                        if (create_overlay_mic_button.mic_button != null) create_overlay_mic_button.mic_button.setImageResource(R.drawable.ic_mic_black_off);
                    } else {
                        start_vosk_voice_recognizer();
                        if (create_overlay_mic_button.mic_button != null) create_overlay_mic_button.mic_button.setImageResource(R.drawable.ic_mic_black_on);
                    }
                    start_create_overlay_mic_button();
                    if (create_overlay_mic_button.mic_button != null) create_overlay_mic_button.mic_button.setBackgroundColor(Color.parseColor("#80000000"));

                    start_create_overlay_translation_text();
                    if (TRANSLATION_TEXT.STRING.length() > 0) {
                        if (create_overlay_translation_text.overlay_translation_text != null) setText(create_overlay_translation_text.overlay_translation_text, TRANSLATION_TEXT.STRING);
                    }
                }
                setText(textview_recognizing, RECOGNIZING_STATUS.STRING);
                setText(textview_overlaying, OVERLAYING_STATUS.STRING);
            }

            public void onNothingSelected(AdapterView<?> adapterView) {
                LANGUAGE.SRC_COUNTRY = spinner_src_languages.getSelectedItem().toString();
                LANGUAGE.SRC = map_src_country.get(LANGUAGE.SRC_COUNTRY);
                String string_src = "LANGUAGE.SRC = " + LANGUAGE.SRC;
                setText(textview_src, string_src);

                LANGUAGE.DST_COUNTRY = spinner_dst_languages.getSelectedItem().toString();
                LANGUAGE.DST = map_dst_country.get(LANGUAGE.DST_COUNTRY);
                String string_dst = "LANGUAGE.DST = " + LANGUAGE.DST;
                setText(textview_dst, string_dst);
            }
        });

        button_delete_model.setOnClickListener(v -> {
            setText(textview_output_messages, "");
            File ddir = new File(VOSK_MODEL.USED_PATH);
            if (ddir.exists()) {
                deleteRecursively(ddir);
                String msg;
                if (checkbox_debug_mode.isChecked()) {
                    msg = ddir + " deleted";
                }
                else {
                    msg = "Model deleted";
                }
                //toast(msg);
                setText(textview_output_messages, msg);
            }
            check_vosk_downloaded_model(VOSK_MODEL.ISO_CODE);
        });

        button_download_model.setOnClickListener(v -> {
            setText(textview_output_messages, "");
            VOSK_MODEL.IS_DOWNLOADING = true;
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                if (!Environment.isExternalStorageManager()) {
                    try {
                        Intent intent = new Intent(Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION);
                        intent.addCategory("android.intent.category.DEFAULT");
                        intent.setData(Uri.parse(String.format("package:%s", getApplicationContext().getPackageName())));
                        startActivity(intent);
                    } catch (Exception e) {
                        e.printStackTrace();
                        Intent intent = new Intent();
                        intent.setAction(Settings.ACTION_MANAGE_ALL_FILES_ACCESS_PERMISSION);
                        startActivity(intent);
                    }
                }
            } else {
                if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                    ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1);
                }
            }

            thread_download_vosk_model = new Thread(() -> downloadModel(VOSK_MODEL.URL_ADDRESS));
            thread_download_vosk_model.start();

            runOnUiThread(() -> {
                File edir = new File(getApplicationContext().getExternalFilesDir(null), "downloaded");
                if (!edir.exists() && edir.mkdir()) {
                    Log.d(edir.toString(), "created");
                }
                button_download_model.setVisibility(View.GONE);
                button_cancel.setVisibility(View.VISIBLE);
                mProgressBar.setVisibility(View.VISIBLE);
                textview_file_size.setVisibility(View.VISIBLE);
                textview_bytes_downloaded.setVisibility(View.VISIBLE);
            });
        });

        button_cancel.setOnClickListener(v -> {
            setText(textview_output_messages, "");
            VOSK_MODEL.IS_DOWNLOADING = false;
            if (thread_download_vosk_model != null) {
                thread_download_vosk_model.interrupt();
                thread_download_vosk_model = null;
            }
            if (!Objects.equals(VOSK_MODEL.USED_PATH, "")) {
                File ddir = new File(VOSK_MODEL.USED_PATH);
                if (ddir.exists()) {
                    System.out.println(VOSK_MODEL.USED_PATH + " exist");
                    deleteRecursively(ddir);
                    String msg;
                    if (checkbox_debug_mode.isChecked()) {
                        msg = ddir + " deleted";
                    } else {
                        msg = "Model deleted";
                    }
                    //toast(msg);
                    setText(textview_output_messages, msg);
                } else {
                    System.out.println(VOSK_MODEL.USED_PATH + " is not exist");
                }
            }
            check_vosk_downloaded_model(VOSK_MODEL.ISO_CODE);
        });

        button_toggle_overlay.setOnClickListener(v -> {
            setText(textview_output_messages,"");
            if (Objects.equals(VOSK_MODEL.ISO_CODE, "en-US")) {
                OVERLAYING_STATUS.IS_OVERLAYING = !OVERLAYING_STATUS.IS_OVERLAYING;
                OVERLAYING_STATUS.STRING = "OVERLAYING_STATUS.IS_OVERLAYING = " + OVERLAYING_STATUS.IS_OVERLAYING;
                setText(textview_overlaying, OVERLAYING_STATUS.STRING);

                if (OVERLAYING_STATUS.IS_OVERLAYING) {
                    if (Settings.canDrawOverlays(getApplicationContext())) {
                        start_create_overlay_mic_button();
                        start_create_overlay_translation_text();
                    }
                    else {
                        Handler handler = new Handler(Looper.getMainLooper());
                        ExecutorService executorService = Executors.newSingleThreadExecutor();
                        Runnable runnable = () -> startActivity(new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION));
                        executorService.execute(runnable);
                        handler.postDelayed(() -> {
                            if (Settings.canDrawOverlays(getApplicationContext())) {
                                start_create_overlay_mic_button();
                                start_create_overlay_translation_text();
                                OVERLAYING_STATUS.IS_OVERLAYING = true;
                                String os = "Overlay permission granted";
                                setText(textview_output_messages, os);
                            }
                            else {
                                OVERLAYING_STATUS.IS_OVERLAYING = false;
                                String os = "Failed to get overlay permission in 15 seconds, please retry to tap TOGGLE OVERLAY button again";
                                setText(textview_output_messages, os);
                            }
                            OVERLAYING_STATUS.STRING = "OVERLAYING_STATUS.IS_OVERLAYING = " + OVERLAYING_STATUS.IS_OVERLAYING;
                            setText(textview_overlaying, OVERLAYING_STATUS.STRING);
                        }, 15000);
                    }

                } else {
                    stop_vosk_voice_recognizer();
                    stop_create_overlay_translation_text();
                    stop_create_overlay_mic_button();
                    RECOGNIZING_STATUS.IS_RECOGNIZING = false;
                    setText(textview_recognizing, RECOGNIZING_STATUS.STRING);
                    setText(textview_overlaying, OVERLAYING_STATUS.STRING);
                    VOICE_TEXT.STRING = "";
                    TRANSLATION_TEXT.STRING = "";
                    setText(voice_text, "");
                    String hints = "Recognized words";
                    voice_text.setHint(hints);
                    if (create_overlay_translation_text.overlay_translation_text != null) {
                        setText(create_overlay_translation_text.overlay_translation_text, "");
                        create_overlay_translation_text.overlay_translation_text.setVisibility(View.INVISIBLE);
                        create_overlay_translation_text.overlay_translation_text_container.setVisibility(View.INVISIBLE);
                    }
                    if (create_overlay_mic_button.mic_button != null) {
                        create_overlay_mic_button.mic_button.setVisibility(View.INVISIBLE);
                    }
                    VOICE_TEXT.STRING = "";
                    TRANSLATION_TEXT.STRING = "";
                    setText(voice_text, "");
                    hints = "Recognized words";
                    voice_text.setHint(hints);
                }
            }
            else {
                if (new File(VOSK_MODEL.EXTRACTED_PATH + VOSK_MODEL.ISO_CODE).exists()) {
                    OVERLAYING_STATUS.IS_OVERLAYING = !OVERLAYING_STATUS.IS_OVERLAYING;
                    OVERLAYING_STATUS.STRING = "OVERLAYING_STATUS.IS_OVERLAYING = " + OVERLAYING_STATUS.IS_OVERLAYING;
                    setText(textview_overlaying, OVERLAYING_STATUS.STRING);
                    setText(textview_output_messages,"");

                    if (OVERLAYING_STATUS.IS_OVERLAYING) {
                        if (Settings.canDrawOverlays(getApplicationContext())) {
                            start_create_overlay_mic_button();
                            start_create_overlay_translation_text();
                        }
                        else {
                            Handler handler = new Handler(Looper.getMainLooper());
                            ExecutorService executorService = Executors.newSingleThreadExecutor();
                            Runnable runnable = () -> startActivity(new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION));
                            executorService.execute(runnable);
                            handler.postDelayed(() -> {
                                if (Settings.canDrawOverlays(getApplicationContext())) {
                                    start_create_overlay_mic_button();
                                    start_create_overlay_translation_text();
                                    OVERLAYING_STATUS.IS_OVERLAYING = true;
                                    String os = "Overlay permission granted";
                                    setText(textview_output_messages, os);
                                }
                                else {
                                    OVERLAYING_STATUS.IS_OVERLAYING = false;
                                    String os = "Failed to get overlay permission in 15 seconds, please retry to tap TOGGLE OVERLAY button again";
                                    setText(textview_output_messages, os);
                                }
                                OVERLAYING_STATUS.STRING = "OVERLAYING_STATUS.IS_OVERLAYING = " + OVERLAYING_STATUS.IS_OVERLAYING;
                                setText(textview_overlaying, OVERLAYING_STATUS.STRING);
                            }, 15000);
                        }

                    } else {
                        stop_vosk_voice_recognizer();
                        stop_create_overlay_translation_text();
                        stop_create_overlay_mic_button();
                        RECOGNIZING_STATUS.IS_RECOGNIZING = false;
                        setText(textview_recognizing, RECOGNIZING_STATUS.STRING);
                        setText(textview_overlaying, OVERLAYING_STATUS.STRING);
                        setText(textview_output_messages, "");
                        VOICE_TEXT.STRING = "";
                        TRANSLATION_TEXT.STRING = "";
                        setText(voice_text, "");
                        String hints = "Recognized words";
                        voice_text.setHint(hints);
                        if (create_overlay_translation_text.overlay_translation_text != null) {
                            setText(create_overlay_translation_text.overlay_translation_text, "");
                            create_overlay_translation_text.overlay_translation_text.setVisibility(View.INVISIBLE);
                            create_overlay_translation_text.overlay_translation_text_container.setVisibility(View.INVISIBLE);
                        }
                        if (create_overlay_mic_button.mic_button != null) {
                            create_overlay_mic_button.mic_button.setVisibility(View.INVISIBLE);
                        }
                        setText(textview_output_messages, "");
                        VOICE_TEXT.STRING = "";
                        TRANSLATION_TEXT.STRING = "";
                        setText(voice_text, "");
                        hints = "Recognized words";
                        voice_text.setHint(hints);
                    }
                }
                else {
                    String msg = "You have to download the model first";
                    setText(textview_output_messages, msg);

                    RECOGNIZING_STATUS.IS_RECOGNIZING = false;
                    RECOGNIZING_STATUS.STRING = "RECOGNIZING_STATUS.IS_RECOGNIZING = " + RECOGNIZING_STATUS.IS_RECOGNIZING;
                    setText(textview_recognizing, RECOGNIZING_STATUS.STRING);

                    OVERLAYING_STATUS.IS_OVERLAYING = false;
                    OVERLAYING_STATUS.STRING = "OVERLAYING_STATUS.IS_OVERLAYING = " + OVERLAYING_STATUS.IS_OVERLAYING;
                    setText(textview_overlaying, OVERLAYING_STATUS.STRING);
                }
            }
        });

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        stop_create_overlay_translation_text();
        stop_create_overlay_mic_button();
        stop_vosk_voice_recognizer();
        MainActivity.audio.setStreamVolume(AudioManager.STREAM_NOTIFICATION, MainActivity.mStreamVolume, AudioManager.ADJUST_SAME);
        finish();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        stop_create_overlay_translation_text();
        stop_create_overlay_mic_button();
        stop_vosk_voice_recognizer();
        MainActivity.audio.setStreamVolume(AudioManager.STREAM_NOTIFICATION, MainActivity.mStreamVolume, AudioManager.ADJUST_SAME);
    }

    public void setup_src_spinner(ArrayList<String> supported_languages) {
        Collections.sort(supported_languages);
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, R.layout.spinner_textview_align, supported_languages);
        adapter.setDropDownViewResource(R.layout.spinner_textview_align);
        spinner_src_languages.setAdapter(adapter);
        spinner_src_languages.setSelection(supported_languages.indexOf("English"));
    }

    public void setup_dst_spinner(ArrayList<String> supported_languages) {
        Collections.sort(supported_languages);
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, R.layout.spinner_textview_align, supported_languages);
        adapter.setDropDownViewResource(R.layout.spinner_textview_align);
        spinner_dst_languages.setAdapter(adapter);
        spinner_dst_languages.setSelection(supported_languages.indexOf("Indonesian"));
    }

    private void start_create_overlay_mic_button() {
        Intent i = new Intent(this, create_overlay_mic_button.class);
        startService(i);
    }

    private void stop_create_overlay_mic_button() {
        stopService(new Intent(this, create_overlay_mic_button.class));
    }

    private void start_create_overlay_translation_text() {
        Intent i = new Intent(this, create_overlay_translation_text.class);
        startService(i);
    }

    private void stop_create_overlay_translation_text() {
        stopService(new Intent(this, create_overlay_translation_text.class));
    }

    private void start_vosk_voice_recognizer() {
        Intent i = new Intent(this, VoskVoiceRecognizer.class);
        startService(i);
    }

    private void stop_vosk_voice_recognizer() {
        stopService(new Intent(this, VoskVoiceRecognizer.class));
    }

    private void check_mlkit_dictionary() {
        if (Objects.equals(LANGUAGE.SRC, LANGUAGE.DST)) {
            MLKIT_DICTIONARY.READY = true;
            mlkit_status_message = "";
        }
        if (Objects.equals(LANGUAGE.SRC, "en")) {
            if (file_en_dst_folder.exists() || file_dst_en_folder.exists()) {
                MLKIT_DICTIONARY.READY = true;
                mlkit_status_message = "MLKIT dictionary is ready";
            } else {
                MLKIT_DICTIONARY.READY = false;
                mlkit_status_message = "MLKIT dictionary is not ready";
            }
        }
        if (Objects.equals(LANGUAGE.DST, "en")) {
            if (file_en_src_folder.exists() || file_src_en_folder.exists()) {
                MLKIT_DICTIONARY.READY = true;
                mlkit_status_message = "MLKIT dictionary is ready";
            } else {
                MLKIT_DICTIONARY.READY = false;
                mlkit_status_message = "MLKIT dictionary is not ready";
            }
        }
        if (!(Objects.equals(LANGUAGE.SRC, "en")) && !(Objects.equals(LANGUAGE.DST, "en"))) {
            if ((file_en_src_folder.exists() || file_src_en_folder.exists()) && (file_en_dst_folder.exists()) || file_dst_en_folder.exists()) {
                MLKIT_DICTIONARY.READY = true;
                mlkit_status_message = "MLKIT dictionary is ready";
            }
            else if ((file_en_src_folder.exists() || file_src_en_folder.exists()) && !file_dst_en_folder.exists() && !file_en_dst_folder.exists()) {
                MLKIT_DICTIONARY.READY = false;
                mlkit_status_message = "MLKIT dictionary is not ready";
            }
            else if ((file_en_dst_folder.exists() || file_dst_en_folder.exists()) && !file_src_en_folder.exists() && !file_en_src_folder.exists()) {
                MLKIT_DICTIONARY.READY = false;
                mlkit_status_message = "MLKIT dictionary is not ready";
            }
            else if (!file_en_src_folder.exists() && !file_en_dst_folder.exists() && !file_src_en_folder.exists() && !file_dst_en_folder.exists()) {
                MLKIT_DICTIONARY.READY = false;
                mlkit_status_message = "MLKIT dictionary is not ready";
            }
        }

        if (!MLKIT_DICTIONARY.READY) {
            mlkit_status_message = "Downloading MLKIT dictionary, please be patient";
            setText(MainActivity.textview_output_messages, mlkit_status_message);
            mlkit_status_message = "MLKIT dictionary is not ready";
            setText(textview_mlkit_status, mlkit_status_message);

            translator.downloadModelIfNeeded(conditions)
                    .addOnSuccessListener(unused -> {
                        MLKIT_DICTIONARY.READY = true;
                        String msg = "MLKIT dictionary download completed";
                        setText(MainActivity.textview_output_messages, msg);
                        mlkit_status_message = "MLKIT dictionary is ready";
                        setText(textview_mlkit_status, mlkit_status_message);
                    })
                    .addOnFailureListener(e -> {});
            if (translator != null) translator.close();
        }
        else {
            new Handler(Looper.getMainLooper()).post(() -> {
                mlkit_status_message = "MLKIT dictionary is ready";
                setText(textview_mlkit_status, mlkit_status_message);
                if (translator != null) translator.close();
            });
        }

    }

    int fileSize;
    String stringFileSize;
    String response;
    public int get_vosk_model_filesize(String models_URL) {
        Handler handler = new Handler(Looper.getMainLooper());
        ExecutorService executor = Executors.newSingleThreadScheduledExecutor();
        executor.execute(() -> {
            try {
                URL url = new URL(models_URL);
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.connect();
                response = "Server responses :\nconnection.getResponseCode() = " + connection.getResponseCode() + "\nconnection.getResponseMessage() = " + connection.getResponseMessage();
                if (connection.getContentLength() > 0) {
                    fileSize = connection.getContentLength();
                    if (checkbox_debug_mode.isChecked()) {
                        stringFileSize = "VOSK_MODEL.ZIP_FILE_SIZE = " + fileSize + " bytes";
                    }
                    else {
                        stringFileSize = "File size = " + fileSize + " bytes";
                    }
                }

                handler.post(()-> {
                    setText(textview_server_response, response);
                    setText(textview_file_size, stringFileSize);
                });

            } catch (IOException e) {
                e.printStackTrace();
                check_vosk_downloaded_model(VOSK_MODEL.ISO_CODE);
                setText(textview_output_messages, e.getMessage());
                //new Handler().postDelayed(() -> setText(textview_output_messages, ""), 3000);
            }
        });
        return fileSize;
    }


    int count;
    long bytes_downloaded;
    public void downloadModel (String models_URL) {
        mProgressBar.setIndeterminate(false);
        mProgressBar.setMax(100);
        mProgressBar.setProgress(0);

        File dir = new File(String.valueOf(this.getExternalFilesDir(null)));
        if (!(dir.exists())) {
            boolean mkdir = dir.mkdir();
            if (!mkdir) {
                String msg = "Directory creation failed";
                //toast(msg);
                setText(textview_output_messages, msg);
                //new Handler().postDelayed(() -> setText(textview_output_messages, ""), 3000);
            }
        }

        File edir = new File(this.getExternalFilesDir(null), "downloaded");
        if (!(edir.exists())) {
            boolean mkdir = edir.mkdir();
            if (!mkdir) {
                String msg = "Directory creation failed";
                //toast(msg);
                setText(textview_output_messages, msg);
                //new Handler().postDelayed(() -> setText(textview_output_messages, ""), 3000);
            }
        }

        Handler handler = new Handler(Looper.getMainLooper());
        ExecutorService executor = Executors.newSingleThreadScheduledExecutor();

        executor.execute(() -> {
            try {
                URL url = new URL(models_URL);
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.connect();

                byte[] data = new byte[1024];
                bytes_downloaded = 0;
                if (connection.getContentLength() > 0) {
                    VOSK_MODEL.ZIP_FILE_SIZE = connection.getContentLength();
                    String response = "Server responses :\nconnection.getResponseCode() = " + connection.getResponseCode() + "\nconnection.getResponseMessage() = " + connection.getResponseMessage();
                    setText(textview_server_response, response);
                    InputStream input = connection.getInputStream();
                    FileOutputStream output = new FileOutputStream(VOSK_MODEL.SAVE_AS);

                    while ((count = input.read(data)) != -1 && VOSK_MODEL.IS_DOWNLOADING) {
                        String string_file_size;
                        String string_bytes_downloaded;
                        bytes_downloaded += count;

                        if (checkbox_debug_mode.isChecked()) {
                            string_file_size = "VOSK_MODEL.ZIP_FILE_SIZE = " + VOSK_MODEL.ZIP_FILE_SIZE + " bytes";
                            string_bytes_downloaded = "bytes_downloaded = " + bytes_downloaded + " bytes";
                        } else {
                            string_file_size = "File size = " + VOSK_MODEL.ZIP_FILE_SIZE + " bytes";
                            string_bytes_downloaded = "Bytes downloaded = " + bytes_downloaded + " bytes";
                        }

                        publishProgress((int) ((bytes_downloaded * 100) / VOSK_MODEL.ZIP_FILE_SIZE));
                        output.write(data, 0, count);

                        setText(textview_file_size, string_file_size);
                        setText(textview_bytes_downloaded, string_bytes_downloaded);
                    }
                    output.flush();
                    output.close();
                    input.close();
                }

                handler.post(() -> {
                    if (VOSK_MODEL.IS_DOWNLOADING) {
                        button_cancel.setVisibility(View.GONE);
                        mProgressBar.setVisibility(View.GONE);
                        textview_file_size.setVisibility(View.GONE);
                        textview_bytes_downloaded.setVisibility(View.GONE);
                        Decompress df = new Decompress(VOSK_MODEL.SAVE_AS, VOSK_MODEL.EXTRACTED_PATH);
                        df.unzip();
                        File oldfolder = new File(VOSK_MODEL.EXTRACTED_PATH, VOSK_MODEL.ZIP_FILENAME.replace(".zip", ""));
                        File newfolder = new File(VOSK_MODEL.EXTRACTED_PATH, VOSK_MODEL.ISO_CODE);

                        boolean rendir = oldfolder.renameTo(newfolder);
                        if (!rendir) {
                            String msg = "Directory rename failed";
                            //toast(msg);
                            setText(textview_output_messages, msg);
                            //new Handler().postDelayed(() -> setText(textview_output_messages, ""), 3000);
                        }

                        File ddir = new File(VOSK_MODEL.SAVE_AS);
                        deleteRecursively(ddir);
                        VOSK_MODEL.USED_PATH = VOSK_MODEL.EXTRACTED_PATH + VOSK_MODEL.ISO_CODE;
                        setText(textview_output_messages, "");
                        check_vosk_downloaded_model(VOSK_MODEL.ISO_CODE);
                        VOSK_MODEL.IS_DOWNLOADING = false;
                    }
                    else {
                        button_cancel.setVisibility(View.GONE);
                        mProgressBar.setVisibility(View.GONE);
                        textview_file_size.setVisibility(View.GONE);
                        textview_bytes_downloaded.setVisibility(View.GONE);
                        button_download_model.setVisibility(View.VISIBLE);
                        executor.shutdown();
                    }
                });

            } catch (Exception e) {
                check_vosk_downloaded_model(VOSK_MODEL.ISO_CODE);
                setText(textview_output_messages, e.getMessage());
            }
        });
    }

    private void publishProgress(Integer... progress) {
        mProgressBar.setProgress(progress[0]);
    }

    private void check_vosk_downloaded_model(String string_model) {
        File edir = new File(VOSK_MODEL.EXTRACTED_PATH + string_model);
        if (Objects.equals(VOSK_MODEL.ISO_CODE, "en-US")) {
            button_delete_model.setVisibility(View.GONE);
            button_download_model.setVisibility(View.GONE);
            button_cancel.setVisibility(View.GONE);
            mProgressBar.setVisibility(View.GONE);
            textview_model_URL.setVisibility(View.GONE);
            textview_server_response.setVisibility(View.GONE);
            textview_model_zip_file.setVisibility(View.GONE);
            textview_file_size.setVisibility(View.GONE);
            textview_bytes_downloaded.setVisibility(View.GONE);
            textview_model_used_path.setVisibility(View.GONE);
        } else {
            if (edir.exists()) {
                VOSK_MODEL.USED_PATH = VOSK_MODEL.EXTRACTED_PATH + string_model;
                button_delete_model.setVisibility(View.VISIBLE);
                button_cancel.setVisibility(View.GONE);
                button_download_model.setVisibility(View.GONE);
                textview_model_URL.setVisibility(View.GONE);
                textview_server_response.setVisibility(View.GONE);
                textview_model_zip_file.setVisibility(View.GONE);
                textview_file_size.setVisibility(View.GONE);
                textview_bytes_downloaded.setVisibility(View.GONE);
                if (checkbox_debug_mode.isChecked()) {
                    textview_model_used_path.setVisibility(View.VISIBLE);
                    String string_model_used_path = "VOSK_MODEL.USED_PATH = " + VOSK_MODEL.USED_PATH;
                    setText(textview_model_used_path, string_model_used_path);
                }
            } else {
                VOSK_MODEL.USED_PATH = "";
                button_delete_model.setVisibility(View.GONE);
                button_download_model.setVisibility(View.VISIBLE);
                if (checkbox_debug_mode.isChecked()) {
                    textview_model_URL.setVisibility(View.VISIBLE);
                    textview_server_response.setVisibility(View.VISIBLE);
                    textview_model_zip_file.setVisibility(View.VISIBLE);
                    textview_file_size.setVisibility(View.VISIBLE);
                    VOSK_MODEL.ZIP_FILE_SIZE = get_vosk_model_filesize(VOSK_MODEL.URL_ADDRESS);
                    String string_file_size = "VOSK_MODEL.ZIP_FILE_SIZE = " + VOSK_MODEL.ZIP_FILE_SIZE + " bytes";
                    runOnUiThread(() -> textview_file_size.setText(string_file_size));
                }
                textview_model_used_path.setVisibility(View.GONE);
            }
        }
    }

    void deleteRecursively(File fileOrDirectory) {
        if (fileOrDirectory.isDirectory())
            for (File child : Objects.requireNonNull(fileOrDirectory.listFiles()))
                deleteRecursively(child);
        fileOrDirectory.delete();
    }

    public void setText(final TextView tv, final String text){
        //new Handler(Looper.getMainLooper()).post(() -> tv.setText(text));
        runOnUiThread(() -> tv.setText(text));
    }

    /*private void toast(String message) {
        new Handler(Looper.getMainLooper()).post(() -> Toast.makeText(getApplicationContext(), message, Toast.LENGTH_SHORT).show());
    }*/

}
