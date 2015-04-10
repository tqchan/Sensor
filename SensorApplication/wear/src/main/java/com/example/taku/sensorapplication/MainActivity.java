package com.example.taku.sensorapplication;

import android.app.Activity;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.support.wearable.view.WatchViewStub;
import android.util.Log;
import android.widget.TextView;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.wearable.DataApi;
import com.google.android.gms.wearable.NodeApi;
import com.google.android.gms.wearable.PutDataMapRequest;
import com.google.android.gms.wearable.PutDataRequest;
import com.google.android.gms.wearable.Wearable;


public class MainActivity extends Activity implements SensorEventListener, GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener {
    private TextView mTextView;
    private TextView textView;
    private final String TAG = MainActivity.class.getName();
    private final float GAIN = 0.9f;
    //    private final String[] SEND_MESSAGES = {"/Action/NONE", "/Action/PUNCH", "/Action/UPPER", "/Action/HOOK"};
    private final String SEND_MESSAGES = "/messagetest";
    private SensorManager mSensorManager;
    private GoogleApiClient mGoogleApiClient;
    private String mNode;
    private static float x, y, z;
    private float motion;
    private float[] gravity = {0.0f, 0.0f, 0.0f};
    private float[] linearAccelatation = {0.0f, 0.0f, 0.0f};
    byte[] by;
    private static final String KEY = "com.example.key";
    private static final String PATH = "/data";
    PutDataMapRequest dataMapRequest;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        final WatchViewStub stub = (WatchViewStub) findViewById(R.id.watch_view_stub);
        stub.setOnLayoutInflatedListener(new WatchViewStub.OnLayoutInflatedListener() {
            @Override
            public void onLayoutInflated(WatchViewStub stub) {
                mTextView = (TextView) stub.findViewById(R.id.text);
                textView = (TextView) stub.findViewById(R.id.textView);
            }
        });

        //sensor>>
        mSensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        //<<sensor

        //>>GoogleApiClient
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addApi(Wearable.API)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .build();

        dataMapRequest = PutDataMapRequest.create(PATH);
    }

    @Override
    protected void onResume() {
        super.onResume();
        Sensor kasoku_sensor = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        Sensor jairo_sensor = mSensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE);

        mSensorManager.registerListener(this, kasoku_sensor, SensorManager.SENSOR_DELAY_GAME);
        mSensorManager.registerListener(this, jairo_sensor, SensorManager.SENSOR_DELAY_GAME);
        mGoogleApiClient.connect();
    }

    @Override
    protected void onPause() {
        super.onPause();
        mSensorManager.unregisterListener(this);
        mGoogleApiClient.disconnect();
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        switch (event.sensor.getType()){
            case Sensor.TYPE_ACCELEROMETER:
                x = event.values[0];
                y = event.values[1];
                z = event.values[2];

                // 加速度から重力の影響を取り除く。以下参照。
                // http://developer.android.com/intl/ja/reference/android/hardware/SensorEvent.html#values
                final float alpha = 0.8f;
                gravity[0] = alpha * gravity[0] + (1 - alpha) * x;
                gravity[1] = alpha * gravity[1] + (1 - alpha) * y;
                gravity[2] = alpha * gravity[2] + (1 - alpha) * z;
                linearAccelatation[0] = x - gravity[0];
                linearAccelatation[1] = y - gravity[1];
                linearAccelatation[2] = z - gravity[2];
                //加速度センサーの値をセット
                String str = "加速度センサー"
                        + "\nX軸:" + linearAccelatation[0]
                        + "\nY軸:" + linearAccelatation[1]
                        + "\nZ軸:" + linearAccelatation[2];
                if (mTextView != null)
                    mTextView.setText(String.format("加速度\n" + "X : %f\nY : %f\nZ : %f\n", linearAccelatation[0], linearAccelatation[1], linearAccelatation[2]));
                break;
            //ジャイロセンサーの値をセット
            case Sensor.TYPE_GYROSCOPE:
                String str_j = "ジャイロセンサー"
                        + "\nx軸中心：" + event.values[mSensorManager.DATA_X]
                        + "\ny軸中心：" + event.values[mSensorManager.DATA_Y]
                        + "\nz軸中心：" + event.values[mSensorManager.DATA_Z];
                if (textView != null)
                    textView.setText(str_j);
                break;
        }

    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }

    @Override
    public void onConnected(Bundle bundle) {
        Log.d(TAG, "onConnected");
        Wearable.NodeApi.getConnectedNodes(mGoogleApiClient).setResultCallback(new ResultCallback<NodeApi.GetConnectedNodesResult>() {
            @Override
            public void onResult(NodeApi.GetConnectedNodesResult nodes) {
                //Nodeは１個に限定
                if (nodes.getNodes().size() > 0) {
                    mNode = nodes.getNodes().get(0).getId();
                }
            }
        });
    }

    @Override
    public void onConnectionSuspended(int i) {
        Log.d(TAG, "onConnectionSuspended");
    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
        Log.d(TAG, "onConnectionFailed : " + connectionResult.toString());
    }


    }



