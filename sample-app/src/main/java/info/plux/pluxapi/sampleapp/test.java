package info.plux.pluxapi.sampleapp;

import info.plux.pluxapi.Communication;
import info.plux.pluxapi.bitalino.BITalino;
import info.plux.pluxapi.bitalino.BITalinoCommunication;
import info.plux.pluxapi.bitalino.BITalinoCommunicationFactory;

import android.app.Activity;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.*;
import android.util.Log;
import android.view.View;
import android.widget.*;
import info.plux.pluxapi.Communication;
import info.plux.pluxapi.Constants;
import info.plux.pluxapi.bitalino.*;
import info.plux.pluxapi.bitalino.bth.OnBITalinoDataAvailable;

/**
 * Created by Nicolas on 18/09/2017.
 */

public class test  {

    private final BroadcastReceiver updateReceiver = new BroadcastReceiver() {
        public static final String TAG ="" ;

        @Override
        public void onReceive(Context context, Intent intent) {
            final String action = intent.getAction();
            if (Constants.ACTION_STATE_CHANGED.equals(action)) {

                String identifier = intent.getStringExtra(Constants.IDENTIFIER);
                Constants.States state = Constants.States.getStates(intent.getIntExtra(Constants.EXTRA_STATE_CHANGED,0));
                Log.i(TAG, "Device " + identifier + ": " + state.name());

            } else if (Constants.ACTION_DATA_AVAILABLE.equals(action)) {

                BITalinoFrame frame = intent.getParcelableExtra(Constants.EXTRA_DATA);
                Log.d(TAG, "BITalinoFrame: " + frame.toString());

            } else if (Constants.ACTION_COMMAND_REPLY.equals(action)) {

                String identifier = intent.getStringExtra(Constants.IDENTIFIER);
                Parcelable parcelable = intent.getParcelableExtra(Constants.EXTRA_COMMAND_REPLY);          if(parcelable.getClass().equals(BITalinoState.class)){
                    Log.d(TAG, "BITalinoState: " + parcelable.toString());

                } else if(parcelable.getClass().equals(BITalinoDescription.class)){

                    Log.d(TAG, "BITalinoDescription: isBITalino2: " + ((BITalinoDescription)parcelable).isBITalino2() + "; FwVersion: String.valueOf(((BITalinoDescription)parcelable).getFwVersion())");

                }
            } else if (Constants.ACTION_MESSAGE_SCAN.equals(action)){

                BluetoothDevice device = intent.getParcelableExtra(Constants.EXTRA_DEVICE_SCAN);

            }
        }
    };
    protected static IntentFilter updateIntentFilter() {
        final IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(Constants.ACTION_STATE_CHANGED);
        intentFilter.addAction(Constants.ACTION_DATA_AVAILABLE);
        intentFilter.addAction(Constants.ACTION_COMMAND_REPLY);
        intentFilter.addAction(Constants.ACTION_MESSAGE_SCAN);
        return intentFilter;
    }


}
