package com.example.mohamed.listen_to_me;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Set;

/**
 * Created by mohamed on 8/3/2016.
 */
public class BluetoothList extends Activity{
  BluetoothAdapter bt = BluetoothAdapter.getDefaultAdapter();
   Set<BluetoothDevice> btd = bt.getBondedDevices() ;
    BluetoothDevice BluetoothDevice2 ;
    private boolean flag = false ;
    ArrayList<String> Adressess ;
    String address = null;
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.list);
        final ListView listView = (ListView) findViewById(R.id.listView);
//getIntent().getStringArrayListExtra("Array list")
        ArrayList<String> devicesNames = getIntent().getStringArrayListExtra("Array list");
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_single_choice,devicesNames );
        listView.setAdapter(adapter);
        listView.setChoiceMode(ListView.CHOICE_MODE_SINGLE);
        Adressess = getIntent().getStringArrayListExtra("Array addresses");
            listView.setOnItemClickListener(new AdapterView.OnItemClickListener(){

                @Override
                public void onItemClick(AdapterView<?> adapterView, View view, int position, long l) {
                    flag = false ;
                    for( BluetoothDevice bluetoothDevice : btd){
                        if(bluetoothDevice.getName().equals((String)listView.getItemAtPosition(position))){
                            BluetoothDevice2 =   bluetoothDevice ;
                            flag = true ;
                            break ;
                        }

                    }
                    if(flag){
                        message("This device is currently paired");
                        address = BluetoothDevice2.getAddress();
                      //  message(address);
                    }
                    else{

                        message("This device is not currently paired");
                        address = Adressess.get(position);
                        //message(address);
                    }
                    Intent mBackIntent = new Intent () ;
                    if(address!=null) {
                        mBackIntent.putExtra("Device address", address);
                    }
                    setResult(Activity.RESULT_OK, mBackIntent);
                    finish();

                }
            });
    }
    private void message(String mess){
        Toast.makeText(getBaseContext(),mess,Toast.LENGTH_SHORT).show();
    }
}
