package info.plux.pluxapi.sampleapp;

import android.app.Activity;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.location.Location;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.os.Parcelable;
import android.os.SystemClock;
import android.support.v4.app.NotificationCompat;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.DefaultRetryPolicy;
import com.android.volley.NetworkResponse;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

import info.plux.pluxapi.Communication;
import info.plux.pluxapi.Constants;
import info.plux.pluxapi.bitalino.BITalinoCommunication;
import info.plux.pluxapi.bitalino.BITalinoCommunicationFactory;
import info.plux.pluxapi.bitalino.BITalinoDescription;
import info.plux.pluxapi.bitalino.BITalinoException;
import info.plux.pluxapi.bitalino.BITalinoFrame;
import info.plux.pluxapi.bitalino.BITalinoState;

import static info.plux.pluxapi.Constants.ACTION_COMMAND_REPLY;
import static info.plux.pluxapi.Constants.ACTION_DATA_AVAILABLE;
import static info.plux.pluxapi.Constants.ACTION_DEVICE_READY;
import static info.plux.pluxapi.Constants.ACTION_EVENT_AVAILABLE;
import static info.plux.pluxapi.Constants.ACTION_STATE_CHANGED;
import static info.plux.pluxapi.Constants.EXTRA_COMMAND_REPLY;
import static info.plux.pluxapi.Constants.EXTRA_DATA;
import static info.plux.pluxapi.Constants.EXTRA_STATE_CHANGED;
import static info.plux.pluxapi.Constants.IDENTIFIER;
import static info.plux.pluxapi.Constants.States;

public class DeviceActivity extends Activity implements View.OnClickListener, GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener {

    /*
     * BLE elements
     */
    private final String TAG = this.getClass().getSimpleName();
    public final static String EXTRA_DEVICE = "info.plux.pluxapi.sampleapp.DeviceActivity.EXTRA_DEVICE";
    public final static String FRAME = "info.plux.pluxapi.sampleapp.DeviceActivity.Frame";
    private BluetoothDevice bluetoothDevice;
    private BITalinoCommunication bitalino;
    private boolean isBITalino2 = false;

    Timer timer;
    TimerTask timerTask;
    //public static Handler handler2;
    private States currentState = States.DISCONNECTED;
    private boolean isUpdateReceiverRegistered = false;

    /*
     * UI elements
     */
    private TextView nameTextView;
    private TextView addressTextView;
    private TextView elapsedTextView;
    private TextView stateTextView;
    private TextView resultado;
    private TextView resultsTextView;

    private Button connectButton;
    private Button stateButton;
    private Button disconnectButton;
    private Button startButton;
    private Button stopButton;
    private Button batteryThresholdButton;
    private Button triggerButton;
    private Button pwmButton;

    private LinearLayout bitalinoLinearLayout;
    private RadioButton digital1RadioButton;
    private RadioButton digital2RadioButton;
    private RadioButton digital3RadioButton;
    private RadioButton digital4RadioButton;
    private SeekBar batteryThresholdSeekBar;
    private SeekBar pwmSeekBar;

    private boolean isDigital1RadioButtonChecked = false;
    private boolean isDigital2RadioButtonChecked = false;
    private float alpha = 0.25f;


    private final String LOG_TAG = "LaurenceTestApp";
    private TextView txtOutput;
    private Button b;
    private GoogleApiClient mGoogleApiClient;
    private LocationRequest mLocationRequest;
    private LocationListener listener;
    boolean envie;

    public String message;

    public ProducerThread hiloProducer = new ProducerThread();
    public ConsumerThread hiloConsumer = new ConsumerThread();
    public CalculosThread calculosThread = new CalculosThread();
    public static Handler handlerDatos;

    public static final int THREAD_PRIORITY_BACKGROUND = 10;

    private static final int NOTIFY_ID = 100;
    private static final String YES_ACTION = "com.tinbytes.simplenotificationapp.YES_ACTION";
    public Handler handler2;
    //public Handler handlerPrint;
    public Runnable runnable;
    private NotificationManager notificationManager;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (getIntent().hasExtra(EXTRA_DEVICE)) {
            bluetoothDevice = getIntent().getParcelableExtra(EXTRA_DEVICE);
        }

        setContentView(R.layout.activity_main);
        initView();
        setUIElements();

        envie = true;
        notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        processIntentAction(getIntent());


        mThread.run();
//        hiloProducer.start();
//        SystemClock.sleep(100);
//        hiloConsumer.start();


        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addApi(LocationServices.API)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .build();

        listener = new LocationListener() {
            @Override
            public void onLocationChanged(Location location) {
                try {
                    post("1", location.getLatitude(), location.getLongitude());
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        };
    }

    //Resultados del Hilo alterno en el Hilo principal

    public Handler handlerPrint = new Handler() {
        ArrayList<String> mylist = new ArrayList<>();
        @Override
        public void handleMessage(Message msg) {
            Bundle bundle = msg.getData();
            String message = bundle.getString("message");
            resultsTextView.setText(message);
            mylist.add(message);
            //resultado.setText(String.valueOf(mylist.size()));
            //Log.d("Contador", message);
        }
    };

    private Intent getNotificationIntent() {
        Intent intent = new Intent(this, DeviceActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        return intent;
    }

    //HANDLER QUE ENVIA LOS MENSAJES DE TEXTO. LLEGA EL AVISO DESDE EL GET CUANDO
    public Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {

            LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, mLocationRequest, listener);
            Bundle bundle = msg.getData();
            String message = bundle.getString("tiempo");
        }
    };

    @Override
    protected void onStart() {
        super.onStart();
        // Connect the client.
        mGoogleApiClient.connect();
    }

    @Override
    public void onConnected(Bundle bundle) {

        mLocationRequest = LocationRequest.create();
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        mLocationRequest.setInterval(10); // Update location every second
        mLocationRequest.setNumUpdates(1);
        //LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, mLocationRequest, (LocationListener) this);
    }


    // Hilo que recibe las caracteristicas calculadas por el Hilo Consumer
    public class MyThread extends Thread {
        @Override
        public void run() {
            android.os.Process.setThreadPriority(android.os.Process.THREAD_PRIORITY_MORE_FAVORABLE);
            android.os.Process.setThreadPriority(android.os.Process.THREAD_PRIORITY_BACKGROUND);

            handlerDatos = new Handler() {

                @Override
                public void handleMessage(Message msg) {
                    Bundle bundle = msg.getData();
                    message = bundle.getString("data");
                    //Log.d("Mensaje hilo calculos", message);

                    try {
                        get();
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            };
        }
    }

    MyThread mThread = new MyThread();

    //
    @Override
    public void onConnectionSuspended(int i) {
        Log.i(LOG_TAG, "GoogleApiClient connection has been suspend");
    }

    //
    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
        Log.i(LOG_TAG, "GoogleApiClient connection has failed");
    }

    //
//
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    //
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        return super.onOptionsItemSelected(item);
    }

    //
//
    private void post(String uid, double lat, double lon) throws JSONException {

        // {host}/partner/post endpoint
        String url = "https://fathomless-waters-78804.herokuapp.com/attack/";

        // JSON object to post to the DB
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("uid", uid);
        jsonObject.put("lat", lat);
        jsonObject.put("lon", lon);

        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.POST, url, jsonObject,
                new Response.Listener<JSONObject>() {

                    @Override
                    public void onResponse(JSONObject response) {

                        try {
                            String OK = response.getString("ok");
                            //txtOutput.setText("Mensaje enviado: " + OK);
                            Log.d("Consumer", "Mensaje enviado....");

                            if (OK == "true") {

                                Message dataToSend = sendBundle("iniciar_reloj");
                                if (ProducerThread.mHandler != null) {
                                    ProducerThread.mHandler.sendMessage(dataToSend);
                                }

                            }

                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                }, new Response.ErrorListener() {


            @Override
            public void onErrorResponse(VolleyError error) {

                System.out.println("Error for uniqId : ");
                NetworkResponse errorRes = error.networkResponse;
                Log.d("", errorRes.statusCode + "");

            }
        }) {

            //Posting headers
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                HashMap<String, String> headers = new HashMap<String, String>();
                headers.put("Content-Type", "application/json; charset=utf-8");
                return headers;
            }
        };

        jsonObjectRequest.setRetryPolicy(new DefaultRetryPolicy(
                30000,
                DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));

        // Making the request
        RequestQueue queue = Volley.newRequestQueue(this);
        queue.add(jsonObjectRequest);

    }

    //
//
    public void get() throws JSONException {


        //final String url = "https://svm-miniconda.herokuapp.com/?target=617,383,083804624,16458839";

        RequestQueue queue = Volley.newRequestQueue(this); // this = context

        JsonObjectRequest getRequest = new JsonObjectRequest(Request.Method.GET, message, null,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {

                        try {
                            int prediction = response.getInt("prediction");
                            String SVM = "SVM Miniconda Response: ";

                            switch (prediction) {
                                case 0:
                                    new Thread(new Runnable() {
                                        public void run() {
                                            Looper.prepare();
                                            showActionButtonsNotification();
                                            Looper.loop();
                                        }
                                    }).start();

                                    SystemClock.sleep(5000);
                                    Log.d("Hilo Consumer dice", String.valueOf(envie));

                                    new Thread(new Runnable() {
                                        public void run() {
                                            android.os.Process.setThreadPriority(android.os.Process.THREAD_PRIORITY_MORE_FAVORABLE);
                                            android.os.Process.setThreadPriority(android.os.Process.THREAD_PRIORITY_BACKGROUND);
                                            Looper.prepare();

                                            if (envie) {
                                                Message dataToSend = sendBundle("tiempo");
                                                if (handler != null) {
                                                    handler.sendMessage(dataToSend);
                                                }
                                            }
                                            Looper.loop();
                                        }
                                    }).start();


                                    //txtOutput.setText(SVM + "Convulsionando");
                                    //LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, mLocationRequest, listener);
                                    break;
                                case 1:
                                    //txtOutput.setText(SVM + "Corriendo");

                                    Message dataToSend = sendBundle("iniciar_reloj");
                                    if (ProducerThread.mHandler != null) {
                                        ProducerThread.mHandler.sendMessage(dataToSend);
                                    }
                                    break;
                                case 2:
                                    //txtOutput.setText(SVM + "Caminando");

                                    Message dataToSend1 = sendBundle("iniciar_reloj");
                                    if (ProducerThread.mHandler != null) {
                                        ProducerThread.mHandler.sendMessage(dataToSend1);
                                    }
                                    break;
                            }

                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {

                        Log.d("Error.Response", String.valueOf(error));
                        Message dataToSend1 = sendBundle("iniciar_reloj");
                        if (ProducerThread.mHandler != null) {
                            ProducerThread.mHandler.sendMessage(dataToSend1);
                        }
                    }
                }
        );
// add it to the RequestQueue
        queue.add(getRequest);

    }

    //
    private void showActionButtonsNotification() {
        Intent yesIntent = getNotificationIntent();
        yesIntent.setAction(YES_ACTION);

        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(this);

        Notification notification = new NotificationCompat.Builder(this)
                .setContentIntent(PendingIntent.getActivity(this, 0, getNotificationIntent(), PendingIntent.FLAG_UPDATE_CURRENT))
                .setSmallIcon(R.mipmap.ic_launcher)
                .setTicker("Action Buttons Notification Received")
                .setContentTitle("ALERTA!!!!")
                .setDefaults(Notification.DEFAULT_SOUND)
                .setDefaults(Notification.DEFAULT_VIBRATE)
                .setContentText("Nuestro sistema ha detectado un posible ataque , ¿Se encuentra bien?")
                .setWhen(System.currentTimeMillis())
                .setAutoCancel(true)
                .addAction(new NotificationCompat.Action(
                        R.mipmap.ic_thumb_up_black_36dp,
                        getString(R.string.yes),
                        PendingIntent.getActivity(this, 0, yesIntent, PendingIntent.FLAG_UPDATE_CURRENT)))

                .build();
        Uri alarmSound = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        notificationBuilder.setSound(alarmSound);
        notificationManager.notify(NOTIFY_ID, notification);

    }

    //
    @Override
    protected void onNewIntent(Intent intent) {
        processIntentAction(intent);
        super.onNewIntent(intent);
    }

    //
//
    private void processIntentAction(Intent intent) {
        if (intent.getAction() != null) {
            switch (intent.getAction()) {
                case YES_ACTION:
                    Toast.makeText(this, "Yes :)", Toast.LENGTH_SHORT).show();
                    Message dataToSend4 = sendBundle("iniciar_reloj");
                    if (ProducerThread.mHandler != null) {
                        ProducerThread.mHandler.sendMessage(dataToSend4);
                    }
            }
        }
    }

    //
    public Message sendBundle(String mensaje) {
        Message dataToSend = Message.obtain();
        Bundle bundle = new Bundle();
        bundle.putString("data", mensaje);
        dataToSend.setData(bundle);
        return dataToSend;
    }


    @Override
    protected void onResume() {
        super.onResume();

        //startTimer("MIERDA!!!!!!");
        registerReceiver(updateReceiver, makeUpdateIntentFilter());
        isUpdateReceiverRegistered = true;

        int duration = Toast.LENGTH_SHORT;
        Toast toast = Toast.makeText(getApplicationContext(), "In the onResume() event", duration);
        toast.show();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        if (isUpdateReceiverRegistered) {
            unregisterReceiver(updateReceiver);
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        //startTimer();
        int duration = Toast.LENGTH_SHORT;
        Toast toast = Toast.makeText(getApplicationContext(), "In the onPause() event", duration);
        toast.show();
        registerReceiver(updateReceiver, makeUpdateIntentFilter());
        isUpdateReceiverRegistered = true;
    }

    @Override
    public void onStop() {

        int duration = Toast.LENGTH_SHORT;
        Toast toast = Toast.makeText(getApplicationContext(), "In the onStop() event", duration);
        toast.show();
        mGoogleApiClient.connect();
        processIntentAction(getIntent());
        registerReceiver(updateReceiver, makeUpdateIntentFilter());
        isUpdateReceiverRegistered = true;
        super.onStop();
    }

    /*
 * UI elements
 */
    private void initView() {
        nameTextView = (TextView) findViewById(R.id.device_name_text_view);
        addressTextView = (TextView) findViewById(R.id.mac_address_text_view);
        elapsedTextView = (TextView) findViewById(R.id.elapsed_time_Text_view);
        stateTextView = (TextView) findViewById(R.id.state_text_view);
        resultado = (TextView) findViewById(R.id.resultado_text_view);

        connectButton = (Button) findViewById(R.id.connect_button);
        disconnectButton = (Button) findViewById(R.id.disconnect_button);
        startButton = (Button) findViewById(R.id.start_button);
        stopButton = (Button) findViewById(R.id.stop_button);

        //bitalino ui elements
        bitalinoLinearLayout = (LinearLayout) findViewById(R.id.bitalino_linear_layout);
        stateButton = (Button) findViewById(R.id.state_button);
        digital1RadioButton = (RadioButton) findViewById(R.id.digital_1_radio_button);
        digital2RadioButton = (RadioButton) findViewById(R.id.digital_2_radio_button);
        digital3RadioButton = (RadioButton) findViewById(R.id.digital_3_radio_button);
        digital4RadioButton = (RadioButton) findViewById(R.id.digital_4_radio_button);
        triggerButton = (Button) findViewById(R.id.trigger_button);
        batteryThresholdSeekBar = (SeekBar) findViewById(R.id.battery_threshold_seek_bar);
        batteryThresholdButton = (Button) findViewById(R.id.battery_threshold_button);
        pwmSeekBar = (SeekBar) findViewById(R.id.pwm_seek_bar);
        pwmButton = (Button) findViewById(R.id.pwm_button);
        resultsTextView = (TextView) findViewById(R.id.results_text_view);
        resultado = (TextView) findViewById(R.id.resultado_text_view);
    }

    private void setUIElements() {

        if (bluetoothDevice.getName() == null) {
            nameTextView.setText("BITalino");
        } else {
            nameTextView.setText(bluetoothDevice.getName());
        }

        addressTextView.setText(bluetoothDevice.getAddress());
        stateTextView.setText(currentState.name());

        Communication communication = Communication.getById(bluetoothDevice.getType());
        Log.d(TAG, "Communication: " + communication.name());
        if (communication.equals(Communication.BLE)) {
            communication = Communication.BLE;
        }

        bitalino = new BITalinoCommunicationFactory().getCommunication(communication, this);

        connectButton.setOnClickListener(this);
        disconnectButton.setOnClickListener(this);
        startButton.setOnClickListener(this);
        stopButton.setOnClickListener(this);
        stateButton.setOnClickListener(this);
        digital1RadioButton.setOnClickListener(this);
        digital2RadioButton.setOnClickListener(this);
        digital3RadioButton.setOnClickListener(this);
        digital4RadioButton.setOnClickListener(this);
        triggerButton.setOnClickListener(this);
        batteryThresholdButton.setOnClickListener(this);
        pwmButton.setOnClickListener(this);
    }

    /*
     * Local Broadcast
     */


    private final BroadcastReceiver updateReceiver = new BroadcastReceiver() {
        @Override

        public void onReceive(Context context, Intent intent) {

            final String action = intent.getAction();

            if (ACTION_STATE_CHANGED.equals(action)) {

                String identifier = intent.getStringExtra(IDENTIFIER);
                States state = States.getStates(intent.getIntExtra(EXTRA_STATE_CHANGED, 0));

                Log.i(TAG, identifier + " -> " + state.name());

                stateTextView.setText(state.name());
                //stateTextView.setText(identifier);

                switch (state) {
                    case NO_CONNECTION:
                        break;
                    case LISTEN:
                        break;
                    case CONNECTING:
                        break;
                    case CONNECTED:
                        break;
                    case ACQUISITION_TRYING:
                        break;
                    case ACQUISITION_OK:
                        hiloProducer.start();
                        SystemClock.sleep(10);
                        hiloConsumer.start();
                        //calculosThread.run();
                        break;
                    case ACQUISITION_STOPPING:
                        break;
                    case DISCONNECTED:
                        break;
                    case ENDED:
                        break;

                }
            } else if (ACTION_DATA_AVAILABLE.equals(action)) {

                if (intent.hasExtra(EXTRA_DATA)) {
                    final Parcelable parcelable = intent.getParcelableExtra(EXTRA_DATA);

                    if (parcelable.getClass().equals(BITalinoState.class)) { //BITalino

                        BITalinoState state = (BITalinoState) parcelable;
                        int batery = state.getBattery();
                        Log.d("Contador", String.valueOf(batery));
                    }

                    if (parcelable.getClass().equals(BITalinoFrame.class)) { //BITalino

                        //resultsTextView.setText(parcelable.toString());

                        BITalinoFrame frame = intent.getParcelableExtra(Constants.EXTRA_DATA);
                        String raw = frame.toString();
                        //resultsTextView.setText(frame.toString());
                        //Log.d("Value", String.valueOf(frame.toString()));

                        // Se crea un nuevo Hilo que se ejecute aparte del Principal
                        Runnable aRunnable = new Runnable() {

                            public void run() {
                                android.os.Process.setThreadPriority(android.os.Process
                                        .THREAD_PRIORITY_BACKGROUND);
                                //goToSleep()

                                BITalinoFrame frame = (BITalinoFrame) parcelable;
                                int EMG = 0;
                                int ACC = 4;
                                int valueEMG = frame.getAnalog(EMG);
                                int valueACC = frame.getAnalog(ACC);
//
//                                Message printUI = sendBundle(String.valueOf(valueEMG));
//                                if (handlerPrint != null) {
//                                    handlerPrint.sendMessage(printUI);
//                                }

//                                Message message = sendBundle("message", String.valueOf("EMG: "+valueEMG + "\n ACC: "+valueACC));
//                                if (ConsumerThread.handlerProcess != null) {
//                                    ConsumerThread.handlerProcess.sendMessage(message);
//                                }
                                Message message = sendBundleS("trama", String.valueOf(valueACC)+","+String.valueOf(valueEMG));
                                if (ConsumerThread.handlerProcess != null) {
                                    ConsumerThread.handlerProcess.sendMessage(message);
                                }

//                                Message message2 = sendBundleS("EMG", String.valueOf(valueEMG));
//                                if (ConsumerThread.handlerProcess != null) {
//                                    ConsumerThread.handlerProcess.sendMessage(message2);
//                                }

                            }
                        };
                        Thread aThread = new Thread(aRunnable);
                        aThread.start();
                    }

                }

            } else if (ACTION_COMMAND_REPLY.equals(action)) {
                String identifier = intent.getStringExtra(IDENTIFIER);

                BITalinoFrame frame = intent.getParcelableExtra(Constants.EXTRA_DATA);

                if (intent.hasExtra(EXTRA_COMMAND_REPLY) && (intent.getParcelableExtra(EXTRA_COMMAND_REPLY) != null)) {
                    Parcelable parcelable = intent.getParcelableExtra(EXTRA_COMMAND_REPLY);

                    if (parcelable.getClass().equals(BITalinoState.class)) { //BITalino

                        BITalinoState state = (BITalinoState) parcelable;
                        int batery = state.getBattery();
                        Log.d("Contador", String.valueOf(batery));

                        Log.d(TAG, ((BITalinoState) parcelable).toString());
                        //resultsTextView.setText(parcelable.toString());
                        //resultsTextView.setText(String.valueOf(batery));

                    } else if (parcelable.getClass().equals(BITalinoDescription.class)) { //BITalino

                        isBITalino2 = ((BITalinoDescription) parcelable).isBITalino2();
                        resultsTextView.setText("isBITalino2: " + isBITalino2 + "; FwVersion: " + String.valueOf(((BITalinoDescription) parcelable).getFwVersion()));


                    }
                }
            }
        }

    };


    private IntentFilter makeUpdateIntentFilter() {
        final IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(ACTION_STATE_CHANGED);
        intentFilter.addAction(ACTION_DATA_AVAILABLE);
        intentFilter.addAction(ACTION_EVENT_AVAILABLE);
        intentFilter.addAction(ACTION_DEVICE_READY);
        intentFilter.addAction(ACTION_COMMAND_REPLY);
        return intentFilter;
    }

    @Override
    public void onClick(View view) {

        switch (view.getId()) {

            case R.id.connect_button:

                try {
                    bitalino.connect(bluetoothDevice.getAddress());

                } catch (BITalinoException e) {
                    e.printStackTrace();
                }
                break;
            case R.id.disconnect_button:

                try {
                    bitalino.disconnect();
                } catch (BITalinoException e) {
                    e.printStackTrace();
                }
                break;
            case R.id.start_button:

                try {
                    bitalino.start(new int[]{0, 1, 2, 3, 4, 5}, 100);
                } catch (BITalinoException e) {
                    e.printStackTrace();
                }
                break;
            case R.id.stop_button:
                try {
                    bitalino.stop();
                } catch (BITalinoException e) {
                    e.printStackTrace();
                }
                break;
            case R.id.state_button:
                try {
                    bitalino.state();
                } catch (BITalinoException e) {
                    e.printStackTrace();
                }
                break;
            case R.id.trigger_button:
                int[] digitalChannels;
                if (isBITalino2) {
                    digitalChannels = new int[2];
                } else {
                    digitalChannels = new int[4];
                }

                digitalChannels[0] = (digital1RadioButton.isChecked()) ? 1 : 0;
                digitalChannels[1] = (digital2RadioButton.isChecked()) ? 1 : 0;

                if (!isBITalino2) {
                    digitalChannels[2] = (digital3RadioButton.isChecked()) ? 1 : 0;
                    digitalChannels[4] = (digital4RadioButton.isChecked()) ? 1 : 0;
                }

                try {
                    bitalino.trigger(digitalChannels);
                } catch (BITalinoException e) {
                    e.printStackTrace();
                }
                break;
            case R.id.digital_1_radio_button:
                if (isDigital1RadioButtonChecked) {
                    digital1RadioButton.setChecked(false);
                } else {
                    digital1RadioButton.setChecked(true);
                }
                isDigital1RadioButtonChecked = digital1RadioButton.isChecked();
                break;
            case R.id.digital_2_radio_button:
                if (isDigital2RadioButtonChecked) {
                    digital2RadioButton.setChecked(false);
                } else {
                    digital2RadioButton.setChecked(true);
                }
                isDigital2RadioButtonChecked = digital2RadioButton.isChecked();
                break;
            case R.id.digital_3_radio_button:
                if (digital3RadioButton.isChecked()) {
                    digital3RadioButton.setChecked(false);
                } else {
                    digital3RadioButton.setChecked(true);
                }
                break;
            case R.id.digital_4_radio_button:
                if (digital4RadioButton.isChecked()) {
                    digital4RadioButton.setChecked(false);
                } else {
                    digital4RadioButton.setChecked(true);
                }
                break;
            case R.id.battery_threshold_button:

                try {
                    bitalino.battery(batteryThresholdSeekBar.getProgress());
                } catch (BITalinoException e) {
                    e.printStackTrace();
                }
                break;
            case R.id.pwm_button:
                try {
                    bitalino.pwm(pwmSeekBar.getProgress());

                } catch (BITalinoException e) {
                    e.printStackTrace();
                }
                break;
        }
    }

    private Message sendBundleS(String mensaje, String info) {
        Message dataToSend = Message.obtain();
        Bundle bundle = new Bundle();
        bundle.putString(mensaje, info);
        dataToSend.setData(bundle);
        return dataToSend;
    }

    private Message sendBundleInt(String mensaje, int info) {
        Message dataToSend = Message.obtain();
        Bundle bundle = new Bundle();
        bundle.putInt(mensaje, info);
        dataToSend.setData(bundle);
        return dataToSend;
    }

}