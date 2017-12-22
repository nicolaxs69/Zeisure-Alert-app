package info.plux.pluxapi.sampleapp;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.os.SystemClock;
import android.util.Log;

public class ProducerThread extends Thread {

    public static Handler mHandler;
    public static Handler mHandler2;

    public void run() {
        android.os.Process.setThreadPriority(android.os.Process.THREAD_PRIORITY_MORE_FAVORABLE);
        android.os.Process.setThreadPriority(android.os.Process.THREAD_PRIORITY_BACKGROUND);
        Looper.prepare();
        Log.d("Mensaje", "Hilo productor iniciado...");

        mHandler = new Handler() {
            @Override
            public void handleMessage(Message msg) {

                super.handleMessage(msg);
                mHandler.obtainMessage();

                if (msg.getData().getString("data") == "iniciar_reloj") {
                    SystemClock.sleep(10);

                    Log.d("Mensaje", "___________Prodcutor iniciado: Van 0 segundos______________");

                    Message dataToSend = sendBundle("iniciar_recoleccion");

                    if (ConsumerThread.mHandler != null) {
                        ConsumerThread.mHandler.sendMessage(dataToSend);
                    }
                    goToSleep();

                    Message dataToSend2 = sendBundle("parar_recoleccion");

                    if (ConsumerThread.mHandler != null) {
                        ConsumerThread.mHandler.sendMessage(dataToSend2);
                    }

                    Log.d("Hilo Productor dice", "Van 10 segundos");
                }


                if (msg.getData().getString("data") == "Ya acabe mis procesos") {
                    SystemClock.sleep(100);

                    Log.d("Hilo Productor dice", "El consumidor ya acabo sus procesos________________");
                }

            }
        };
        Looper.loop();

    }

    private Message sendBundle(String mensaje) {

        Message dataToSend = Message.obtain();
        Bundle bundle = new Bundle();
        bundle.putString("data", mensaje);
        dataToSend.setData(bundle);
        return dataToSend;
    }

    private void goToSleep() {
        SystemClock.sleep(10000);
    }
}