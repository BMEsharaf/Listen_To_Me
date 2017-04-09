package com.example.mohamed.listen_to_me;

import android.annotation.TargetApi;
import android.app.Activity;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.graphics.drawable.AnimationDrawable;
import android.net.Uri;
import android.os.Build;
import android.os.Handler;
import android.os.Message;
import android.speech.RecognizerIntent;
import android.speech.tts.TextToSpeech;
import android.speech.tts.TextToSpeechService;
import android.speech.tts.UtteranceProgressListener;
import android.speech.tts.Voice;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.appindexing.Action;
import com.google.android.gms.appindexing.AppIndex;
import com.google.android.gms.common.api.GoogleApiClient;

import org.w3c.dom.Text;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

public class MainActivity extends Activity  {

    Button listen, speech;
    ImageView signViews;
    TextToSpeech speakNow;
    Spinner list;
    List<String> language ;
    ArrayList<String> devicesNames , devicesAdress;
    BluetoothAdapter bt = BluetoothAdapter.getDefaultAdapter();

    BluetoothDevice bluetoothDevice ;

    BluetoothSocket bluetoothSocket ;

    ConnectedThread connectedThread;

    Handler bluetoothIn ;
    int handlerState ;

       int  VOICE_RECOGNITION = 1 , result;
    TextView textView ;
    private final int progress_bar_type = 0 ;
    ProgressDialog pDialog ;
    final private int REQUEST_COONECT = 2 ;
    private  InputStream mmInStream;
    String data = "";
    String lang = "en-US";
    boolean flag = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initiateUI();
        language = new ArrayList<String>();
        language.add("English-US");
        language.add("Arabic");

        ArrayAdapter<String> LanguageAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_dropdown_item, language);
        list.setAdapter(LanguageAdapter);

        InitiatTextToSpeech();
        // Intialize handler object
        bluetoothIn = new Handler(){
            @TargetApi(Build.VERSION_CODES.LOLLIPOP)
            public void handleMessage(android.os.Message msg) {

            if(msg.what==handlerState){
               int writeBuf =  msg.arg1;
               char c = (char)writeBuf;
                Log.i("Here",c+"");
                if(c=='*'){
                    flag = true ;
                    Log.i("Here2",c+"");
                }else if (c=='+'){
                    flag=false ;
                    Log.i("Here3",c+"");
                }else if(c==' '){
                    if(flag==true) {
                        speakNow.setLanguage(new Locale("ar-EG"));
                        speakNow.speak(data, TextToSpeech.QUEUE_FLUSH, null);
                        Log.i("Here4",c+"");
                    }else{
                        speakNow.setLanguage(Locale.UK);
                        speakNow.speak(data, TextToSpeech.QUEUE_FLUSH, null);
                        Log.i("Here5",c+"");
                    }
                    textView.setText(data);
                    data = "";
                }else{
                    if(flag == true ){
                        data+=mapping(c);

                    }else {
                        data += c;
                    }
                }

            }
            }
        };

        /*When Spinner is selected this methode will be invoced "هتشغل"

        * */
        list.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int position, long id) {


              if(speakNow!=null&&!speakNow.isSpeaking()){
                  switch(position) {

                      case 0:

                          lang ="en-US" ;
                          result = speakNow.setLanguage(Locale.US);
                          message("English US");
                          if(result==TextToSpeech.LANG_MISSING_DATA||result==TextToSpeech.ERROR_INVALID_REQUEST) {
                              Toast.makeText(getBaseContext(),"US is not Supported on your android"+speakNow.isLanguageAvailable(Locale.US) ,Toast.LENGTH_SHORT).show();
                          }else{
                              speakNow.setLanguage(Locale.US);
                          }
                          break;
                      case 1:
                          result=speakNow.setLanguage(new Locale("ar"));
                          lang = "ar-EG";
                          if(result==TextToSpeech.LANG_MISSING_DATA||result==TextToSpeech.ERROR_INVALID_REQUEST) {
                             message("GERMAN is not Supported on your android");
                          }else{
                              speakNow.setLanguage(Locale.GERMAN);
                          }
                          break;
                      case 3:

                          result = speakNow.setLanguage(Locale.ITALY);
                          message("Italy");
                          if(result==TextToSpeech.LANG_MISSING_DATA||result==TextToSpeech.ERROR_INVALID_REQUEST) {
                              message("Italy is not Supported on your android");
                          }else{
                              speakNow.setLanguage(Locale.ITALY);
                          }
                  }

              }
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });


        speech.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (speakNow != null) {
                    Speek();
                }
            }
        });

        listen.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                voiceRecognize(lang);
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
    public boolean onOptionsItemSelected(MenuItem item) /* select one item from the list  when occur event */{

        switch(item.getItemId())
        {
            case R.id.Bluetooth_enable :
                bt.enable();//
                return true ;
            case R.id.Bluetooth_disable :
                bt.disable();
                return true ;
            case R.id.bluetooth_discover:
                // Intent filter is specified mmessage will be send to the broadcast receiver
                IntentFilter filter = new IntentFilter();
                filter.addAction(BluetoothDevice.ACTION_FOUND);
                filter.addAction(BluetoothAdapter.ACTION_DISCOVERY_STARTED);
                filter.addAction(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);
                // Register broadcast receiver to listen for this actions
                registerReceiver(mReceiver, filter);
                bt.startDiscovery();
                return true ;
            case R.id.bluetooth_connect:
                if(bluetoothSocket!=null&&bluetoothSocket.isConnected()==false){
                    try {
                        bluetoothSocket.connect();
                        connectedThread = new ConnectedThread(bluetoothSocket);
                        connectedThread.start();
                    } catch (IOException e) {
                        message("Check connection");
                    }
                }
            case R.id.bluetooth_disconnect:
                if(bluetoothSocket!=null&&bluetoothSocket.isConnected()&&mmInStream!=null){
                    try {
                        bluetoothSocket.close();
                        mmInStream.close();
                    } catch (IOException e) {
                        message("Check connection");
                    }
                }
            default :
                return true ;
        }

    }
    private void initiateUI (){
        listen = (Button) findViewById(R.id.button);// init user interface object  from layout
        speech = (Button) findViewById(R.id.button2);
        signViews = (ImageView) findViewById(R.id.imageView4);
        list = (Spinner) findViewById(R.id.spinner);/*consists from data,rander,events
        and  spinner this is class whice have setadaptor function which collect data,rander,events*/
        textView = (TextView) findViewById(R.id.textView);
    }

    private void Speek() {

        if(bluetoothSocket!=null){
         connectedThread = new ConnectedThread(bluetoothSocket);
            connectedThread.start();
        }
    }

    private void voiceRecognize(String language) {

        Intent voicerecogizeIntent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        voicerecogizeIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL,RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        voicerecogizeIntent.putExtra(RecognizerIntent.EXTRA_PROMPT,"Speak Now");
        voicerecogizeIntent.putExtra(RecognizerIntent.EXTRA_MAX_RESULTS,1);
        voicerecogizeIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE,language);
        startActivityForResult(voicerecogizeIntent,VOICE_RECOGNITION);
    }

    private void InitiatTextToSpeech() {
        speakNow = new TextToSpeech(getBaseContext(), new TextToSpeech.OnInitListener() {
            @Override
            public void onInit(int status) {

                if (status == TextToSpeech.SUCCESS) {
                    if (speakNow.isLanguageAvailable(Locale.US) >= 0) {
                        speakNow.setLanguage(Locale.US);
                        speakNow.setPitch(.8f);
                        speakNow.setSpeechRate(1.4f);

                    } else {
                        Toast.makeText(getBaseContext(), "Please Change Language", Toast.LENGTH_SHORT).show();
                    }
                }
                else{
                    message("Error in TTS");
                }
            }
        });
    }

    @TargetApi(Build.VERSION_CODES.ICE_CREAM_SANDWICH_MR1)
    protected void onActivityResult(int requestCode,
                                    int resultCode, Intent data) {

        if (requestCode == VOICE_RECOGNITION && resultCode == RESULT_OK) {
            ArrayList<String> sentences = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
            Toast.makeText(getBaseContext(),sentences.get(0),Toast.LENGTH_SHORT).show();
            convertToSign(sentences.get(0).toLowerCase());
        } else if (requestCode == REQUEST_COONECT && resultCode == RESULT_OK) {
            message(data.getStringExtra("Device address"));
            connect(data.getStringExtra("Device address"));
          super.onActivityResult(requestCode, resultCode, data);
        }
    }

    private void message(String mess){

        Toast.makeText(getBaseContext(),mess,Toast.LENGTH_SHORT).show();
    }
    private void convertToSign(String sentence) {
        int index ;
        textView.setText(sentence);
        AnimationDrawable animation=new AnimationDrawable();
 for(int i = 0 ; i< sentence.length() ; i++){
    index = search(sentence.charAt(i));
    if(index==-1){
        message("Error");
    }else
    // getResources is function of Context class and activity class extends Context
    animation.addFrame(this.getResources().getDrawable(Data.viewsId[index]),700);
}
        animation.setOneShot(true);
        signViews.setBackgroundDrawable(animation);
        animation.start();

    }
int search (char c){
    Log.i("Tag"," "+c);
    for(int k = 0 ; k <Data.signChar.length ; k++){

        if(c==Data.signChar[k]){

            return  k ;
        }
    }
    return -2 ;
}

    private final BroadcastReceiver mReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (BluetoothAdapter.ACTION_DISCOVERY_STARTED.equals(action)) {
                message("Started");
                devicesAdress = new ArrayList<String>();
                devicesNames = new ArrayList<String>() ;
                // Start Showing dialog to wait
                showDialog(progress_bar_type);
            } else if (BluetoothAdapter.ACTION_DISCOVERY_FINISHED.equals(action)) {
               // End the dialog
                pDialog.dismiss();
                if(devicesNames!=null) {
                    Intent intent2 = new Intent(getBaseContext(), BluetoothList.class);
                    intent2.putStringArrayListExtra("Array list", devicesNames);
                    intent2.putStringArrayListExtra("Array addresses", devicesAdress);
                    startActivityForResult(intent2,REQUEST_COONECT);

                }else{
                    message("No devices found");
                }

            } else if (BluetoothDevice.ACTION_FOUND.equals(action)) {
                //bluetooth device found
                BluetoothDevice device = (BluetoothDevice) intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                devicesNames.add(device.getName());
                devicesAdress.add(device.getAddress());

            }
        }
    };

    /**
     * Showing Dialog
     * */
    @Override
    protected Dialog onCreateDialog(int id) {
        switch (id) {
            case progress_bar_type:
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
private void connect(String Adress) {
    bluetoothDevice = bt.getRemoteDevice(Adress);
    try {
        bluetoothSocket = bluetoothDevice.createInsecureRfcommSocketToServiceRecord(bluetoothDevice.getUuids()[0].getUuid());
        message("OK");
    } catch (IOException ConnectionError) {
        message("Error in connection");
    }
    try {
        bluetoothSocket.connect();
    } catch (IOException e) {
        message(e.getMessage());
    }
}
    private class ConnectedThread extends Thread {

        public StringBuilder vocab = null;
        //creation of the connect thread
        public ConnectedThread(BluetoothSocket socket) {
            InputStream tmpIn = null;
            try {
                //Create I/O streams for connection
                tmpIn = socket.getInputStream();
            } catch (IOException e) {
            }

            mmInStream = tmpIn;

        }

        public void run() {

            try {
            while (true) {

                        int data =mmInStream.read();

                        Log.i("Test","data"+data);
                        //read bytes from input buffer
                       // String readMessage = new String(buffer, 0, bytes);
                        bluetoothIn.obtainMessage(handlerState,data,0).sendToTarget();


            }
            } catch (IOException e) {

            }
        }
    }
    @Override
    protected void onDestroy() {
        super.onDestroy();
        try {
        if (speakNow != null&&connectedThread!=null) {
            speakNow.stop();
            speakNow.shutdown();
            mmInStream.close();
            bluetoothSocket.close();
        }


        } catch (IOException e) {
            message("Error");
        }
    }
    public char mapping (char input){

        for(int i = 0 ; i<Data.ENGLISH.length;i++){
            if(input==Data.ENGLISH[i]){
                return Data.ARABIC[i];
            }
        }
        return ' ';
    }
}