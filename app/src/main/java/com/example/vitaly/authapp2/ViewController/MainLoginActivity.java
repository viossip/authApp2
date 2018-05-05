package com.example.vitaly.authapp2.ViewController;

import android.Manifest;
import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.drawable.AnimationDrawable;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;

import com.example.vitaly.authapp2.R;
import com.example.vitaly.authapp2.Model.*;


public class MainLoginActivity extends AppCompatActivity {

//    private static final String[] CREDENTIALS = new String[]{
//            "admin:1234:d0ecd2c7db75e999e256d0b396f995e1",
//            "user:4321:e7096f6143aff9ceb073b14872b1659d",
//            "sudo:1111:a7d134af6240efabcd1bf548c757a4a1"
//    };

    private static final String SECRET_CREDS = "sudo:1111";

    private UserLoginTask mAuthTask = null;

    private EditText mEmailView;
    private EditText mPasswordView;
    private View mProgressView;
    private View mLoginFormView;

    private ImageView outcomeAnimationPlaceHolder;
    private AnimationDrawable animation;
    private TelephonyManager tm;
    //private GetHandler serverRequest;
    private SecretRequest request;
    private boolean getSensorResult = false;
    private int prev_val;
    private int secret_number;
    private String device_id;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_login);

        mEmailView = findViewById(R.id.username);
        mPasswordView = findViewById(R.id.password);
        mLoginFormView = findViewById(R.id.login_form);
        mProgressView = findViewById(R.id.login_progress);
        outcomeAnimationPlaceHolder = findViewById(R.id.result_animation);

        Button mEmailSignInButton = findViewById(R.id.user_sign_in_button);
        mEmailSignInButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                attemptLogin();
            }
        });


        if (ActivityCompat.checkSelfPermission(this,
                Manifest.permission.READ_PHONE_STATE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.READ_PHONE_STATE},0);
        } else {
            tm = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
            device_id = tm.getDeviceId();

            if (device_id != null)
                request = new SecretRequest(SECRET_CREDS.split(":")[0],
                                            SECRET_CREDS.split(":")[1],
                                            device_id);
        }

        SensorManager mySensorManager = (SensorManager)getSystemService(SENSOR_SERVICE);

        Sensor LightSensor = mySensorManager.getDefaultSensor(Sensor.TYPE_LIGHT);
        if(LightSensor != null){
            mySensorManager.registerListener( LightSensorListener, LightSensor,
                    SensorManager.SENSOR_DELAY_NORMAL);
        }else{
            Log.d("ERROR", "ERROR in sensor initialization");
        }
    }

    private final SensorEventListener LightSensorListener = new SensorEventListener(){

        @Override
        public void onAccuracyChanged(Sensor sensor, int accuracy) { }

        @Override
        public void onSensorChanged(SensorEvent event) {
            if(event.sensor.getType() == Sensor.TYPE_LIGHT && getSensorResult){
                if (event.values[0] <= 1 && ( Math.abs( prev_val - event.values[0] ) >= 2))
                    Log.d("EVENT", "+++HIDE+++ " + ++secret_number );
                prev_val = (int) event.values[0];
            }

        }
    };


    private void attemptLogin() {
        if (mAuthTask != null) {
            return;
        }

        mEmailView.setError(null);
        mPasswordView.setError(null);

        String email = mEmailView.getText().toString();
        String password = mPasswordView.getText().toString();

        boolean cancel = false;
        View focusView = null;

        if (!TextUtils.isEmpty(password) && !isPasswordValid(password)) {
            mPasswordView.setError(getString(R.string.error_invalid_password));
            focusView = mPasswordView;
            cancel = true;
        }

        if (TextUtils.isEmpty(email)) {
            mEmailView.setError(getString(R.string.error_field_required));
            focusView = mEmailView;
            cancel = true;
        } else if (!isEmailValid(email)) {
            mEmailView.setError(getString(R.string.error_invalid_email));
            focusView = mEmailView;
            cancel = true;
        }

        if (cancel) {
            focusView.requestFocus();
        } else {
            showProgress(true);
            mAuthTask = new UserLoginTask(email, password, outcomeAnimationPlaceHolder);
            mAuthTask.execute((Void) null);
        }
    }

    private boolean isEmailValid(String email) {
        return !TextUtils.isEmpty(email);
    }

    private boolean isPasswordValid(String password) {
        return password.length() > 3;
    }

    /**
     * Shows the progress UI and hides the login form.
     */
    private void showProgress(final boolean show) {

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR2) {
            int shortAnimTime = getResources().getInteger(android.R.integer.config_shortAnimTime);

            mLoginFormView.setVisibility(show ? View.GONE : View.VISIBLE);
            mLoginFormView.animate().setDuration(shortAnimTime).alpha(
                    show ? 0 : 1).setListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    mLoginFormView.setVisibility(show ? View.GONE : View.VISIBLE);
                }
            });

            mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
            mProgressView.animate().setDuration(shortAnimTime).alpha(
                    show ? 1 : 0).setListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
                }
            });
        } else {
            mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
            mLoginFormView.setVisibility(show ? View.GONE : View.VISIBLE);
        }
    }


    /**
     * Asynchronous login/registration task used to authenticate the user.
     */
    public class UserLoginTask extends AsyncTask<Void, Void, Boolean> {

        private final String mEmail;
        private final String mPassword;
        private ImageView animationPlaceHlder;

        UserLoginTask(String email, String password, ImageView animationPlaceHlder) {
            mEmail = email;
            mPassword = password;
            this.animationPlaceHlder = animationPlaceHlder;
        }

        @Override
        protected Boolean doInBackground(Void... params) {
            String[] result = null;

            try {
                if(request != null){
                    result = request.doWork().split(":");
                    if ( result != null) {
                        getSensorResult = true;
                        Thread.sleep(5000);
                        getSensorResult = false;
                    } else {
                        throw new InterruptedException();
                    }
                } else {
                    throw new InterruptedException() ;
                }
            } catch (InterruptedException e) {
                return false;
            }

            if( result[0].equals(device_id) &&
                result[1].equals(mPassword) &&
                result[2].equals(secret_number + ""))
                    return true;
            return false;
        }

        @Override
        protected void onPostExecute(final Boolean success) {
            mAuthTask = null;
            secret_number = 0;
            showProgress(false);

            if (success) {
                animationPlaceHlder.setBackgroundResource(R.drawable.animation_win);
            } else {
                animationPlaceHlder.setBackgroundResource(R.drawable.animation_loss);
                mPasswordView.setError(getString(R.string.error_incorrect_password));
                mPasswordView.requestFocus();
            }

            animation = (AnimationDrawable) this.animationPlaceHlder.getBackground();
            animation.start();

            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    animation.stop();
                }
            }, 5000);
        }

        @Override
        protected void onCancelled() {
            mAuthTask = null;
            showProgress(false);
        }
    }


}



