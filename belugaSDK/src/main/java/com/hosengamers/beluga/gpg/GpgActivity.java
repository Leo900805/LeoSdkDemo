package com.hosengamers.beluga.gpg;

import android.app.Activity;
import android.content.Intent;
import android.content.IntentSender;
import android.os.Bundle;
import android.os.PersistableBundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.games.Games;

/**
 * Created by Leo on 2016/9/28.
 */

public class GpgActivity extends Activity
                        implements GoogleApiClient.ConnectionCallbacks,
                                   GoogleApiClient.OnConnectionFailedListener{

    // Request code used to invoke sign in user interactions.
    private static final int RC_SIGN_IN = 9001;
    private static final String TAG = "Leo GPG";
    private GoogleApiClient mGoogleApiClient;


    // Are we currently resolving a connection failure?
    private boolean mResolvingConnectionFailure = false;

    // Has the user clicked the sign-in button?
    private boolean mSignInClicked = false;

    // Set to true to automatically start the sign in flow when the Activity starts.
    // Set to false to require the user to click the button in order to sign in.
    private boolean mAutoStartSignInFlow = true;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(TAG, "OnCreate...");

        //logout first
        Log.d(TAG, "Sign-out button clicked");
        Log.d(TAG, "(mGoogleApiClient != null) :" + (mGoogleApiClient != null));
        if (mGoogleApiClient != null){
            Log.d(TAG, "onCreate in if.... ");
            mSignInClicked = false;
            Games.signOut(mGoogleApiClient);
            mGoogleApiClient.disconnect();
        }


        // Create the Google Api Client with access to Plus and Games
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(Games.API).addScope(Games.SCOPE_GAMES)
                .build();
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (mGoogleApiClient.isConnected()) {
            mGoogleApiClient.disconnect();
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        Log.d(TAG, "onStart...");
        mGoogleApiClient.connect();
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        Log.d(TAG, "onConnect...");
    }

    @Override
    public void onConnectionSuspended(int i) {
        Log.d(TAG, "onConnectionSuspended() called. Trying to reconnect.");
        mGoogleApiClient.connect();
    }

    @Override
    protected void onActivityResult(int requestCode, int responseCode, Intent intent) {
        super.onActivityResult(requestCode, responseCode, intent);
        Log.d(TAG, "requestCode is "+requestCode+ " (requestCode == RC_SIGN_IN) is "+ (requestCode == RC_SIGN_IN) );
        if (requestCode == RC_SIGN_IN) {
            Log.d(TAG, "onActivityResult with requestCode == RC_SIGN_IN, responseCode="
                    + responseCode + ", intent=" + intent);
            mSignInClicked = false;
            mResolvingConnectionFailure = false;
            if (responseCode == RESULT_OK) {
                mGoogleApiClient.connect();
                Log.d(TAG, "Success.");
                Intent resultdata = new Intent();
                Bundle bundle = new Bundle();
                bundle.putString("type", "GPG");
                resultdata.putExtras(bundle);
                setResult(Activity.RESULT_OK, resultdata); //回傳RESULT_OK
                finish();
            } else {
                //BaseGameUtils.showActivityResultError(this,requestCode,responseCode, R.string.signin_other_error);
                Log.d(TAG, "There was an issue with sign in. Please try again later.");
            }
        }

    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        Log.d(TAG, "onConnectionFailed() called, result: " + connectionResult);

        if (mResolvingConnectionFailure) {
            Log.d(TAG, "onConnectionFailed() ignoring connection failure; already resolving.");
            return;
        }

        if (mSignInClicked || mAutoStartSignInFlow) {
            Log.d(TAG, "onConnectionFailed()  (mSignInClicked || mAutoStartSignInFlow): " +  (mSignInClicked || mAutoStartSignInFlow));
            mAutoStartSignInFlow = false;
            mSignInClicked = false;
            try {
                Log.d(TAG, "onConnectionFailed()  in  try" );
                connectionResult.startResolutionForResult(this, RC_SIGN_IN);
            } catch (IntentSender.SendIntentException e) {
                e.printStackTrace();
                mGoogleApiClient.connect();
            }
        }
    }
}
