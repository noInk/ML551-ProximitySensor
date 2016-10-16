package com.ml551.issue;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;


public class ProximityMonitor extends AppCompatActivity implements SensorEventListener, Runnable {

    private static final String TAG  = ProximityMonitor.class.getSimpleName();
    private static final long RETRY_INTERVAL_MILLIS = 2000L;
    private SensorManager mSensorManager;
    private Sensor mSensor;
    private SensorEventListener mSensorEventListener;
    private ThreadFlushMethod mFlushMethod;
    private Context mContext;
    boolean running = false;
    private float mValue;
    private String mCurrentValue;
    private TextView mProximityStatus;
    private Button mButton;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mContext = getApplicationContext();
        mSensorEventListener = this;

        mSensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);

        mSensor = mSensorManager.getDefaultSensor(Sensor.TYPE_PROXIMITY);


        mFlushMethod = new ThreadFlushMethod() {
            @Override
            public void flush(final long n) throws InterruptedException {

                if (running) {
                    mSensorManager.flush(mSensorEventListener);
                    Log.i(TAG, " mFlushMethod flush " +mSensorEventListener);


                }

            }
        };


        mButton = (Button) findViewById(R.id.serviceButton);

        mButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (running) {
                    mSensorManager.unregisterListener(mSensorEventListener);
                    setProximityStatus("Not running");
                    mButton.setText("Start");

                } else {
                    mSensorManager.registerListener(mSensorEventListener, mSensor, SensorManager.SENSOR_DELAY_FASTEST);
                    setProximityStatus("Waiting for data");
                    mButton.setText("Stop");

                }
                running = !running;
            }
        });


    }

    @Override
    public void onSaveInstanceState(Bundle savedInstanceState) {
        super.onSaveInstanceState(savedInstanceState);
    }

    @Override
    public void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);

    }

    @Override
    public void onPause() {
        if (running) {
            mSensorManager.unregisterListener(mSensorEventListener);
            setProximityStatus("Not running");
            ((Button) findViewById(R.id.serviceButton)).setText("Start");

            running = false;
        }
        super.onPause();
    }


    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
    }

    void setProximityStatus(String value) {
        mProximityStatus = (TextView) findViewById(R.id.proximity_status);
        mProximityStatus.setText(value);
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        mValue = event.values[0];
        Log.i(TAG, " onSensorChanged " + mValue);

        mCurrentValue = mValue == 0.0 ? "NEAR" : mValue > 0 ? "FAR" : "UNKNOWN";
        setProximityStatus(mCurrentValue);
        Log.i(TAG, " mCurrentValue " + mCurrentValue);


        if (mCurrentValue == "NEAR") {
            mSensorManager.unregisterListener(mSensorEventListener);
            mSensorManager.registerListener(mSensorEventListener, mSensor, SensorManager.SENSOR_DELAY_FASTEST);
            Log.i(TAG, " UNSTUCK SENSOR ");
        }


        this.run();


    }


    @Override
    public void run() {

        Log.i(TAG, " RUN " + mFlushMethod);
        if (mFlushMethod == null) {
            Log.i(TAG, " mFlushMethod IN");


            if (running) {
                mSensorManager.flush(mSensorEventListener);
                Log.i(TAG, "mSensorManager flush on run");


            }


            Log.w(TAG, " RETRY " + RETRY_INTERVAL_MILLIS);
            this.flush(RETRY_INTERVAL_MILLIS);


        } else {
            if (running) {
                mSensorManager.flush(mSensorEventListener);

                if (mCurrentValue == "NEAR") {
                    mSensorManager.flush(mSensorEventListener);
                    Log.i(TAG, " mCurrentValue " + mCurrentValue);
                }


                Log.i(TAG, "ENDING");


            }
        }
    }


    private void flush(final long n) {
        try {
            mFlushMethod.flush(n);
        } catch (InterruptedException ex) {
            ex.printStackTrace();
        }
    }


    interface ThreadFlushMethod {
        void flush(long paramLong)
                throws InterruptedException;
    }

}
