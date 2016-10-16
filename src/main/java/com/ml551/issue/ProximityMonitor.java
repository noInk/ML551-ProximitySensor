package com.ml551.issue;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;


public class ProximityMonitor extends AppCompatActivity implements SensorEventListener, Runnable {

    private static final String TAG = ProximityMonitor.class.getSimpleName();
    private static final long RETRY_INTERVAL_MILLIS = 500L;
    private static final String NEAR = "NEAR";
    private static final String FAR = "FAR";
    private static final String UNKNOWN = "UNKNOWN";
    boolean running = false;
    AsyncTask asyncTask;
    boolean hasRun = false;
    private SensorManager mSensorManager;
    private Sensor mSensor;
    private SensorEventListener mSensorEventListener;
    private ThreadFlushMethod mFlushMethod;
    private Context mContext;
    private float mValue;
    private String mCurrentValue;
    private TextView mProximityStatus;
    private Button mButton;
    private Handler uiHandler = new Handler();

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


                asyncTask = new threadTask().execute();


            }
        };


        mButton = (Button) findViewById(R.id.serviceButton);

        mButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (running) {
                    mSensorManager.unregisterListener(mSensorEventListener);
                    setProximityStatus((mContext.getResources().getString(R.string.proximity_not_running)));
                    mButton.setText((mContext.getResources().getString(R.string.service_start)));

                } else {
                    mSensorManager.registerListener(mSensorEventListener, mSensor, SensorManager.SENSOR_DELAY_FASTEST);
                    setProximityStatus((mContext.getResources().getString(R.string.proximity_waiting_data)));
                    mButton.setText((mContext.getResources().getString(R.string.service_stop)));

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
            setProximityStatus(mContext.getResources().getString(R.string.proximity_not_running));
            ((Button) findViewById(R.id.serviceButton)).setText((mContext.getResources().getString(R.string.service_start)));

            running = false;
        }
        super.onPause();
    }

    @Override
    protected void onStop() {
        Log.i(TAG, Void.TYPE.getName());
        if (running) {
            mSensorManager.unregisterListener(mSensorEventListener);
            running = false;
        }
        super.onStop();
        uiHandler.removeCallbacksAndMessages(null);
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
        class Reference {};


        mValue = event.values[0];
        Log.i(TAG, Reference.class.getEnclosingMethod().getName().toString() + " " + mValue);

        mCurrentValue = mValue == 0.0 ? NEAR : mValue > 0 ? FAR : UNKNOWN;
        setProximityStatus(mCurrentValue);
        Log.i(TAG, Reference.class.getEnclosingMethod().getName().toString() + " " + mCurrentValue);


        if (mCurrentValue.equals(NEAR)) {
            run();
        }

    }


    @Override
    public void run() {
        if (mFlushMethod != null) {
            if (running) {

                flush(RETRY_INTERVAL_MILLIS);

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


    public class threadTask extends AsyncTask<AppCompatActivity, Integer, Boolean> {


        @Override
        protected void onPreExecute() {
            mSensorManager.flush(mSensorEventListener);

            hasRun = true;
        }

        @Override
        protected void onPostExecute(Boolean bolean) {


            hasRun = false;


        }

        protected Boolean doInBackground(AppCompatActivity... activities) {


            try {

                new Thread(new Runnable() {
                    public void run() {


                        uiHandler.post(new Runnable() {
                            public void run() {

                                ///////////////////////////////////////////////////////
                                mSensorManager.unregisterListener(mSensorEventListener);
                                Log.i(TAG, " UNSTUCK PROXIMITY SENSOR");
                                mSensorManager.registerListener(mSensorEventListener, mSensor, SensorManager.SENSOR_DELAY_FASTEST);
                                ///////////////////////////////////////////////////////


                            }
                        });
                    }
                }).start();
            } catch (Exception e) {
            }


            return hasRun;
        }


    }

}