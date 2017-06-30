package com.example.mohamed.listen_to_me;

import android.annotation.TargetApi;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.graphics.drawable.AnimationDrawable;
import android.media.AudioManager;
import android.os.Build;
import android.speech.RecognizerIntent;
import android.speech.tts.TextToSpeech;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

import org.ispeech.SpeechSynthesis;
import org.ispeech.SpeechSynthesisEvent;
import org.ispeech.error.InvalidApiKeyException;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

public class MainActivity extends Activity {

    int result;
    Button listenBtn, speechBtn, letterButton, numberButton, wordButton;
    ImageView signViews;
    TextToSpeech ttsSpeech;
    Spinner list;
    TextView textView;
    ProgressDialog pDialog;
    ToggleButton toggleButton;
    List<String> language = new ArrayList<>();
    ArrayList<BluetoothDevice> bluetoothDevices = new ArrayList<>();
    BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
    BluetoothDevice bluetoothDevice;
    BluetoothSocket bluetoothSocket;
    BluetoothThread bluetoothThread;
    ManageBluetoothData manageBluetoothData;
    boolean listSelection = false;
    String lang = "en-US";
    private static final String TAG = "iSpeech SDK Sample";
    SpeechSynthesis synthesis;
    Context _context;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        InitiateUI();

        language.add("En-US");
        language.add("Ar-Eg");

        ArrayAdapter<String> LanguageAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, language);
        list.setAdapter(LanguageAdapter);

        InitiatTextToSpeech();
        prepareArabicTTSEngine();
        manageBluetoothData = new ManageBluetoothData(synthesis, textView, ttsSpeech);

        list.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int position, long id) {


                if (ttsSpeech != null && !ttsSpeech.isSpeaking()) {
                    switch (position) {

                        case 0:

                            listSelection = true;
                            lang = "en-US";
                            result = ttsSpeech.setLanguage(Locale.US);
                            message("En-US");
                            if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.ERROR_INVALID_REQUEST) {
                                Toast.makeText(getBaseContext(), "US is not Supported on your android" + ttsSpeech.isLanguageAvailable(Locale.US), Toast.LENGTH_SHORT).show();
                            } else {
                                ttsSpeech.setLanguage(Locale.US);
                            }
                            break;
                        case 1:
                            listSelection = false;
                            lang = "ar-EG";
                            prepareArabicTTSEngine();
                            break;
                    }

                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });


        speechBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {


                if (ttsSpeech != null && synthesis != null) {
                    speak(manageBluetoothData);

                } else {
                    InitiatTextToSpeech();
                    prepareArabicTTSEngine();
                    speak(manageBluetoothData);
                }
            }
        });

        letterButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (bluetoothThread != null) {
                    try {
                        if (listSelection) {
                            bluetoothThread.sendChar("e");
                        } else {

                            bluetoothThread.sendChar("f");

                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        });
        numberButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                numberButton.setBackgroundColor(Color.RED);
                if (bluetoothThread != null) {
                    try {
                        if (listSelection) {
                            bluetoothThread.sendChar("g");
                        } else {

                            bluetoothThread.sendChar("h");

                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
                numberButton.setBackgroundColor(Color.BLUE);
            }

        });
        wordButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (bluetoothThread != null) {
                    try {
                        if (listSelection) {
                            bluetoothThread.sendChar("i");
                        } else {

                            bluetoothThread.sendChar("j");

                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        });
        listenBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                voiceRecognize(lang);
            }
        });
        toggleButton.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {

                if (bluetoothThread != null) {
                    try {
                        if (isChecked && listSelection) {

                            bluetoothThread.sendChar("a");
                        } else if (isChecked && !listSelection) {
                            bluetoothThread.sendChar("b");
                        } else if (!isChecked && listSelection) {
                            bluetoothThread.sendChar("c");
                        } else if (!isChecked && !listSelection) {
                            bluetoothThread.sendChar("d");
                        }
                    } catch (IOException exception) {
                        message("Check connection");
                    }
                }
            }
        });

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    //
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()) {
            case R.id.Bluetooth_enable:
                bluetoothAdapter.enable();
                return true;
            case R.id.Bluetooth_disable:
                bluetoothAdapter.disable();
                return true;
            case R.id.bluetooth_discover:
                IntentFilter filter = new IntentFilter();
                filter.addAction(BluetoothDevice.ACTION_FOUND);
                filter.addAction(BluetoothAdapter.ACTION_DISCOVERY_STARTED);
                filter.addAction(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);
                registerReceiver(mReceiver, filter);
                bluetoothAdapter.startDiscovery();
                return true;
            case R.id.bluetooth_connect:

                if (bluetoothSocket == null || !bluetoothSocket.isConnected()) {

                    if (bluetoothDevice != null) {
                        connect(bluetoothDevice);
                    } else {
                        message("Please Start discovery for bluetooth device");
                    }
                }
                return true;
            case R.id.bluetooth_disconnect:
                if (bluetoothSocket != null && bluetoothThread != null) {
                    try {
                        bluetoothSocket.close();
                        bluetoothThread.terminate();
                        bluetoothThread.inputStream.close();
                        bluetoothThread.outputStream.close();
                        bluetoothSocket = null;
                    } catch (IOException e) {
                        message("Check connection");
                    }
                }
            default:
                return true;
        }

    }

    private void InitiateUI() {
        listenBtn = (Button) findViewById(R.id.button);// init user interface object  from layout
        speechBtn = (Button) findViewById(R.id.button2);
        letterButton = (Button) findViewById(R.id.letterButton);
        numberButton = (Button) findViewById(R.id.NumberButton);
        wordButton = (Button) findViewById(R.id.wordButton);
        signViews = (ImageView) findViewById(R.id.imageView4);
        list = (Spinner) findViewById(R.id.spinner);
        textView = (TextView) findViewById(R.id.textView);
        toggleButton = (ToggleButton) findViewById(R.id.toggleButton2);
    }


    private void voiceRecognize(String language) {

        Intent voicerecogizeIntent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        voicerecogizeIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        voicerecogizeIntent.putExtra(RecognizerIntent.EXTRA_PROMPT, "speak Now");
        voicerecogizeIntent.putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 1);
        voicerecogizeIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, language);
        startActivityForResult(voicerecogizeIntent, Data.VOICE_RECOGNITION);
    }

    private void InitiatTextToSpeech() {
        ttsSpeech = new TextToSpeech(this, new TextToSpeech.OnInitListener() {
            @Override
            public void onInit(int status) {

                if (status == TextToSpeech.SUCCESS) {
                    if (ttsSpeech.isLanguageAvailable(Locale.US) >= 0) {
                        ttsSpeech.setLanguage(Locale.US);
                        ttsSpeech.setPitch(.8f);
                        ttsSpeech.setSpeechRate(1.4f);

                    } else {
                        Toast.makeText(getBaseContext(), "This language is not supported", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    message("Error in TTS");
                }
            }
        });
    }

    @TargetApi(Build.VERSION_CODES.ICE_CREAM_SANDWICH_MR1)
    protected void onActivityResult(int requestCode,
                                    int resultCode, Intent data) {

        if (requestCode == Data.VOICE_RECOGNITION && resultCode == RESULT_OK) {
            ArrayList<String> sentences = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
            Toast.makeText(getBaseContext(), sentences.get(0), Toast.LENGTH_SHORT).show();
            convertToSign(sentences.get(0).toLowerCase());
        } else if (requestCode == Data.REQUEST_CONNECT && resultCode == RESULT_OK) {
            bluetoothDevice = data.getExtras().getParcelable("Device object");
            if (bluetoothDevice != null) {
                message(bluetoothDevice.getAddress());
                connect(bluetoothDevice);
            }
            super.onActivityResult(requestCode, resultCode, data);
        }
    }

    private void message(String mess) {
        Toast.makeText(getBaseContext(), mess, Toast.LENGTH_SHORT).show();

    }

    private void convertToSign(String sentence) {
        int index;
        textView.setText(sentence);
        AnimationDrawable animation = new AnimationDrawable();
        for (int i = 0; i < sentence.length(); i++) {
            index = search(sentence.charAt(i));
            if (index == -1) {
                message("Error");
            } else
                animation.addFrame(this.getResources().getDrawable(Data.viewsId[index]), 700);
        }
        animation.setOneShot(true);
        signViews.setBackgroundDrawable(animation);
        animation.start();
    }

    int search(char c) {

        for (int k = 0; k < Data.signChar.length; k++) {

            if (c == Data.signChar[k]) {

                return k;
            }
        }
        return -2;
    }

    private final BroadcastReceiver mReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (BluetoothAdapter.ACTION_DISCOVERY_STARTED.equals(action)) {
                message("Started");
                bluetoothDevices = new ArrayList<>();
                showDialog(Data.progress_bar_type);
            } else if (BluetoothAdapter.ACTION_DISCOVERY_FINISHED.equals(action)) {
                pDialog.dismiss();
                if (bluetoothDevices != null) {
                    Intent intent2 = new Intent(MainActivity.this, BluetoothList.class);
                    intent2.putParcelableArrayListExtra("Bluetooth", bluetoothDevices);
                    startActivityForResult(intent2, Data.REQUEST_CONNECT);

                } else {
                    message("No devices found");
                }

            } else if (BluetoothDevice.ACTION_FOUND.equals(action)) {

                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                bluetoothDevices.add(device);
            }
        }
    };


    @Override
    protected Dialog onCreateDialog(int id) {
        switch (id) {
            case Data.progress_bar_type:
                pDialog = new ProgressDialog(this);
                pDialog.setMessage("Scanning... Please wait...");
                pDialog.setIndeterminate(false);
                pDialog.setMax(100);
                pDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
                pDialog.setCancelable(true);
                pDialog.show();
                return pDialog;
            default:
                return null;
        }
    }

    @TargetApi(Build.VERSION_CODES.ICE_CREAM_SANDWICH_MR1)
    private void connect(BluetoothDevice deviceAddress) {

        try {

            bluetoothSocket = deviceAddress.createInsecureRfcommSocketToServiceRecord(deviceAddress.getUuids()[0].getUuid());
            bluetoothSocket.connect();
        } catch (IOException ConnectionError) {
            message("Error in connection");
        } catch (NullPointerException ex) {
            message("Please ensure bond to HC-O5");
        }
    }

    private void speak(ManageBluetoothData manageBluetoothData1) {

        if (bluetoothSocket != null && manageBluetoothData1 != null) {
            try {
                bluetoothThread = new BluetoothThread(bluetoothSocket, manageBluetoothData1);
                bluetoothThread.start();
            } catch (IOException ex) {
                message("Error for creating new Thread");
            }


        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        try {


                if (ttsSpeech != null) {
                    ttsSpeech.stop();
                    ttsSpeech.shutdown();

                }

                if(synthesis!=null){
                    synthesis.stop();
                }

                if(bluetoothSocket!=null) {
                    bluetoothSocket.close();
                }
                if(bluetoothThread != null) {
                    bluetoothThread.interrupt();
                    bluetoothThread.inputStream.close();
                    bluetoothThread.outputStream.close();
                }



        } catch (IOException e) {
            message("Error");
        }
    }

    private void prepareArabicTTSEngine() {
        try {
            synthesis = SpeechSynthesis.getInstance(this);
            synthesis.setVoiceType("arabicmale");
            synthesis.setStreamType(AudioManager.STREAM_MUSIC);
            synthesis.setSpeechSynthesisEvent(new SpeechSynthesisEvent() {

                public void onPlaySuccessful() {

                }

                public void onPlayStopped() {

                }

                public void onPlayFailed(Exception e) {

                    AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
                    builder.setMessage("Error[TTSActivity]: " + e.toString())
                            .setCancelable(false)
                            .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int id) {
                                }
                            });
                    AlertDialog alert = builder.create();
                    alert.show();
                }

                public void onPlayStart() {

                }

                @Override
                public void onPlayCanceled() {

                }
            });

        } catch (InvalidApiKeyException e) {
            Log.e(TAG, "Invalid API key\n" + Arrays.toString(e.getStackTrace()));
            Toast.makeText(_context, "ERROR: Invalid API key", Toast.LENGTH_LONG).show();
        }
    }
}