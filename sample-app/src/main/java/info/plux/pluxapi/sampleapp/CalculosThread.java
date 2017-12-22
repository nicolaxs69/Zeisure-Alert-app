package info.plux.pluxapi.sampleapp;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.os.SystemClock;
import android.util.Log;

import java.util.ArrayList;
import java.util.Collections;

public class CalculosThread extends Thread {

    public static Handler mHandler;
    public Handler handler;
    public ArrayList mylist = new ArrayList();
    public int EMG;
    public Runnable runnable;

    public void run() {
        android.os.Process.setThreadPriority(android.os.Process.THREAD_PRIORITY_MORE_FAVORABLE);
        android.os.Process.setThreadPriority(android.os.Process.THREAD_PRIORITY_BACKGROUND);
//        Looper.prepare();
        Log.d("Mensaje", "Hilo Calculador iniciado...");
        hiloDatos();

      //   Looper.loop();
    }

    private Message sendBundle(String mensaje) {

        Message dataToSend = Message.obtain();
        Bundle bundle = new Bundle();
        bundle.putString("data", mensaje);
        dataToSend.setData(bundle);
        return dataToSend;
    }

    public void hiloDatos() {
        handler = new Handler();
        runnable = new Runnable() {
            @Override
            public void run() {
                android.os.Process.setThreadPriority(android.os.Process.THREAD_PRIORITY_MORE_FAVORABLE);
                android.os.Process.setThreadPriority(android.os.Process.THREAD_PRIORITY_BACKGROUND);

                mHandler = new Handler() {
                        @Override
                        public void handleMessage(Message msg) {
                            super.handleMessage(msg);
                            mHandler.obtainMessage();

                            String message = msg.getData().getString("data");
                            EMG=Integer.parseInt(message);
                            mylist.add(EMG);
                            Object obj=Collections.max(mylist);
                            int val=(int)obj;
                            Log.d("Maximo",String.valueOf(val));
                        }
                    };
                handler.postDelayed(runnable, 1000);
            }
        };
        handler.post(runnable);
    }

}

