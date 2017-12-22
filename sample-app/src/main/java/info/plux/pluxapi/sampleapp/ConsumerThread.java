package info.plux.pluxapi.sampleapp;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.os.SystemClock;
import android.util.Log;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.List;

/**
 * Created by Nicolas on 14/11/2017.
 */

public class ConsumerThread extends Thread {
    public static Handler mHandler;
    public static Handler mHandler2;
    public static Handler handlerProcess;
    public Handler handler;
    public Runnable runnable;
    public int EMG;
    public int ACC;
    public List mylistEMG = new ArrayList();
    public List mylistACC = new ArrayList();
    String data;
    private int counter;
    Boolean recogerDatos = false;
    public Statistics sta;
    public int maxACC;
    public int minACC;
    public double meanACC;
    public double varianceACC;
    public int maxEMG;
    public int minEMG;
    public double meanEMG;
    public double varianceEMG;

    public void run() {
        android.os.Process.setThreadPriority(android.os.Process.THREAD_PRIORITY_MORE_FAVORABLE);
        android.os.Process.setThreadPriority(android.os.Process.THREAD_PRIORITY_BACKGROUND);
        Looper.prepare();
        Log.d("Mensaje", "Hilo Consumidor iniciado...");

        hiloDatos();

        Message dataToSend = sendBundle("iniciar_reloj");
        if (ProducerThread.mHandler != null) {
            ProducerThread.mHandler.sendMessage(dataToSend);
        }

        mHandler = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                super.handleMessage(msg);
                mHandler.obtainMessage();

                if (msg.getData().getString("data") == "iniciar_recoleccion") {
                    //AQUI RECOLECTO LAS 100 MUESTRAS


                    Log.d("Hilo Consumer dice", "Estoy bebiendo datos del arreglo papu.......");
                    recogerDatos = true;
                }

                if (msg.getData().getString("data") == "parar_recoleccion") {
                    //calculosThread.start();
                    Log.d("Hilo Consumer dice", "Ya acabe de recolectar " + mylistACC.size() + " datos!!!!!");

                    recogerDatos = false;
//
                    if (mylistACC.size() > 0) {
                        sta = new Statistics(mylistACC);
                        maxACC = (int) sta.getMax(mylistACC);
                        minACC = (int) sta.getMin(mylistACC);
                        meanACC = (double) sta.average(mylistACC) / 100;
                        varianceACC = (double) sta.variance(mylistACC) / 100;

//                        sta.variance(mylistACC);
                        Log.d("Valor maximo:  ", String.valueOf(maxACC));
                        Log.d("Valor minimo:  ", String.valueOf(minACC));
                        Log.d("Valor mean:  ", String.valueOf(meanACC));
                        Log.d("Valor varianza:  ", String.valueOf(varianceACC));
                        mylistACC.clear();
                    }


                    if (mylistEMG.size() > 0) {
                        sta = new Statistics(mylistEMG);
                        maxEMG = (int) sta.getMax(mylistEMG);
                        minEMG = (int) sta.getMin(mylistEMG);
                        meanEMG= (double) sta.average(mylistEMG) / 1000;
                        varianceEMG = (double) sta.variance(mylistEMG) / 1000;

//                        sta.variance(mylistEMG);
                        Log.d("Valor maximo:  ", String.valueOf(maxEMG));
                        Log.d("Valor minimo:  ", String.valueOf(minEMG));
                        Log.d("Valor mean:  ", String.valueOf(meanEMG));
                        Log.d("Valor varianza:  ", String.valueOf(varianceEMG));
                        mylistEMG.clear();
                    }



                        //String datoscalculados = "https://svm-miniconda.herokuapp.com/?target=617,383,083804624,16458839";
                        String datoscalculados = "https://svm-server.herokuapp.com/?target=" + String.valueOf(maxACC) + "," + String.valueOf(minACC) + "," + String.valueOf(meanACC) + "," + String.valueOf(varianceACC)+ "," +String.valueOf(maxEMG) + "," + String.valueOf(minEMG) + "," + String.valueOf(meanEMG) + "," + String.valueOf(varianceEMG);
                        Log.d("Datos calculados",datoscalculados);

                        Message dataToSend4 = sendBundle(datoscalculados);
                        if (DeviceActivity.handlerDatos != null) {
                            DeviceActivity.handlerDatos.sendMessage(dataToSend4);
                        }

                        Message dataToSend3 = sendBundle("Ya acabe mis procesos");
                        if (ProducerThread.mHandler != null) {
                            ProducerThread.mHandler.sendMessage(dataToSend3);
                        }


//                    Message dataToSend = sendBundle("iniciar_reloj");
//                    if (ProducerThread.mHandler != null) {
//                        ProducerThread.mHandler.sendMessage(dataToSend);
//                    }

                    }
                }
            }

            ;
        Looper.loop();
        }


    public void hiloDatos() {
        handler = new Handler();
        runnable = new Runnable() {
            @Override
            public void run() {
                android.os.Process.setThreadPriority(android.os.Process.THREAD_PRIORITY_MORE_FAVORABLE);
                android.os.Process.setThreadPriority(android.os.Process.THREAD_PRIORITY_BACKGROUND);

                if (recogerDatos == true) {

                    handlerProcess = new Handler() {
                        @Override
                        public void handleMessage(Message msg) {
                            super.handleMessage(msg);
                            handlerProcess.obtainMessage();

                            String trama = msg.getData().getString("trama");
                            String[] variable= trama.split(",");
                            ACC= Integer.parseInt(variable[0]);
                            EMG= Integer.parseInt(variable[1]);
                            if(ACC != 0){
                                mylistACC.add(ACC);
                                //Log.d("Contador", String.valueOf(ACC));
                            }
                            if(EMG != 0){
                                mylistEMG.add(EMG);
                                //Log.d("Contador", String.valueOf(EMG));
                            }
                        }
                    };
                }
                handler.postDelayed(runnable, 10000);
            }
        };
        handler.post(runnable);
    }

    private Message sendBundle(String mensaje) {
        Message dataToSend = Message.obtain();
        Bundle bundle = new Bundle();
        bundle.putString("data", mensaje);
        dataToSend.setData(bundle);
        return dataToSend;
    }


}

