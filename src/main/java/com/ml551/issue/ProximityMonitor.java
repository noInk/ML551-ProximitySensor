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
    private static final long RETRY_INTERVAL_MILLIS = 2500L;
    private static final String NEAR = "NEAR";
    private static final String FAR = "FAR";
    private static final String UNKNOWN = "UNKNOWN";
    private static final String STRING_SPACE = " ";
    private static final String UNSTUCK_SENSOR = " UNSTUCK " + Sensor.STRING_TYPE_PROXIMITY;


    boolean running = false;

    AsyncTask asyncTask;
    private SensorManager mSensorManager;
    private Sensor mSensor;
    private SensorEventListener mSensorEventListener;
    private SensorUnstuckMethod mSensorUnstuckMethod;
    private Context mContext;
    private float mValue;
    private String mCurrentValue;
    private TextView mProximityStatus;
    private Button mButton;
    private Thread mListenThread;
    private Handler uiHandler = new Handler();

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mContext = getApplicationContext();
        mSensorEventListener = this;

        mSensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);

        mSensor = mSensorManager.getDefaultSensor(Sensor.TYPE_PROXIMITY);


        mSensorUnstuckMethod = new SensorUnstuckMethod() {
            @Override
            public void unstuck() throws InterruptedException {

                asyncTask = new threadTask().execute();

            }
        };


        mListenThread = new Thread(this, TAG);

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

        mListenThread.start();
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
    public void onDestroy() {

        super.onDestroy();
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

        if (running) {
            mSensorManager.unregisterListener(mSensorEventListener);
            running = false;
        }
        super.onStop();
        uiHandler.removeCallbacksAndMessages(null);
    }


    @Override
    protected void onStart() {

        super.onStart();
    }


    @Override
    protected void onResume() {

        super.onResume();

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
        Log.i(TAG, Reference.class.getEnclosingMethod().getName().toString() + STRING_SPACE + mValue);

        mCurrentValue = mValue == 0.0 ? NEAR : mValue > 0 ? FAR : UNKNOWN;
        setProximityStatus(mCurrentValue);
        Log.i(TAG, Reference.class.getEnclosingMethod().getName().toString() + STRING_SPACE + mCurrentValue);


        if (mCurrentValue.equals(NEAR)) {

            run();

        }

    }


    @Override
    public void run() {
        if (mSensorUnstuckMethod != null) {
            if (running) {

                unstuck();

            }
        }
    }


    private void unstuck() {
        try {
            mSensorUnstuckMethod.unstuck();
        } catch (InterruptedException ex) {
            ex.printStackTrace();
        }
    }


    interface SensorUnstuckMethod {
        void unstuck()
                throws InterruptedException;
    }


    public class threadTask extends AsyncTask<AppCompatActivity, Integer, Boolean> {


        @Override
        protected void onPreExecute() {
            mSensorManager.flush(mSensorEventListener);


        }

        @Override
        protected void onPostExecute(Boolean bool) {

            if (Thread.currentThread() != mListenThread) {
                try {
                    mListenThread.join();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }


        }

        protected Boolean doInBackground(AppCompatActivity... activities) {
            class Point {};
            try {

                new Thread(new Runnable() {
                    public void run() {

                        try {
                            Thread.sleep(RETRY_INTERVAL_MILLIS);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }


                        ///////////////////////////////////////////////////////
                        mSensorManager.unregisterListener(mSensorEventListener, mSensor);
                        Log.i(TAG, Point.class.getEnclosingMethod().getName().toString() + UNSTUCK_SENSOR);
                        mSensorManager.registerListener(mSensorEventListener, mSensor, SensorManager.SENSOR_DELAY_FASTEST);
                        ///////////////////////////////////////////////////////


                        uiHandler.post(new Runnable() {
                            public void run() {


                            }
                        });
                    }
                }).start();
            } catch (Exception e) {
            }


            return true;
        }


    }

}