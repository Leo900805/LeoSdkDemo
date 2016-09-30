package com.hosengamers.beluga.gpg;

import android.app.Activity;
import android.content.Intent;
import android.content.IntentSender;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.Api;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.games.Games;

/**
 * Created by user on 2016/9/29.
 */

public class GPGService implements GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener, GPGListener{
    Activity activity;
    //GoogleApiClient.ConnectionCallbacks connectionCallbacks;
    //GoogleApiClient.OnConnectionFailedListener onConnectionFailedListener;

    // Request code used to invoke sign in user interactions.
    private static final int RC_SIGN_IN = 9001;
    private static final String TAG = "Leo GPGService";
    public GoogleApiClient mGoogleApiClient;


    // Are we currently resolving a connection failure?
    private boolean mResolvingConnectionFailure = false;

    // Has the user clicked the sign-in button?
    private boolean mSignInClicked = false;

    // Set to true to automatically start the sign in flow when the Activity starts.
    // Set to false to require the user to click the button in order to sign in.
    private boolean mAutoStartSignInFlow = true;

    int responeCode;

    public GPGService(Activity activity/*, GoogleApiClient.ConnectionCallbacks connectionCallbacks,
                      GoogleApiClient.OnConnectionFailedListener onConnectionFailedListener*/){
        this.activity = activity;
        //this.connectionCallbacks = connectionCallbacks;
        //this.onConnectionFailedListener = onConnectionFailedListener;


    }
    public void Create(){
        Log.d(TAG, "onCreate...");
        // Create the Google Api Client with access to Plus and Games

        mGoogleApiClient = new GoogleApiClient.Builder(GPGService.this.activity)
                .addConnectionCallbacks(GPGService.this)
                .addOnConnectionFailedListener(GPGService.this)
                .addApi(Games.API).addScope(Games.SCOPE_GAMES)
                .build();

        mGoogleApiClient.connect();
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        Log.d(TAG, "onConnectied...");
        Log.d(TAG, "onConnectied...bundle.toString():"+bundle.toString());

    }

    @Override
    public void onConnectionSuspended(int i) {
        Log.d(TAG, "onConnectionSuspended() called. Trying to reconnect.");
        mGoogleApiClient.connect();
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
                connectionResult.startResolutionForResult(GPGService.this.activity, RC_SIGN_IN);
                Log.d(TAG, "connectionResult.getResolution():"+connectionResult.getResolution() );

                //this.onResult();
            } catch (IntentSender.SendIntentException e) {
                e.printStackTrace();
                mGoogleApiClient.connect();
            }
        }
    }

    @Override
    public void onStartGameRequested(boolean hardMode) {

    }

    @Override
    public void onShowAchievementsRequested() {

    }

    @Override
    public void onShowLeaderboardsRequested() {

    }

    @Override
    public void onSignInButtonClicked() {

    }

    @Override
    public void onSignOutButtonClicked() {

    }

    @Override
    public void onResult(int requestCode, int responseCode, Intent intent) {
        Log.d(TAG, "requestCode is "+requestCode+ " (requestCode == RC_SIGN_IN) is "+ (requestCode == RC_SIGN_IN) );
        GPGService.this.mGoogleApiClient.connect();
        Log.d(TAG, "Success...");
        /*
        if (requestCode == RC_SIGN_IN) {
            Log.d(TAG, "onActivityResult with requestCode == RC_SIGN_IN, responseCode="
                    + responseCode + ", intent=" + intent);
            mSignInClicked = false;
            mResolvingConnectionFailure = false;
            if (responseCode == this.activity.RESULT_OK) {
                mGoogleApiClient.connect();
                Log.d(TAG, "Success.");

            } else {
                //BaseGameUtils.showActivityResultError(this,requestCode,responseCode, R.string.signin_other_error);
                Log.d(TAG, "There was an issue with sign in. Please try again later.");
            }
        }
        */
    }
}
