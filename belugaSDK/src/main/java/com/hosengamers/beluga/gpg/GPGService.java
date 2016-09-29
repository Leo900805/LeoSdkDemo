package com.hosengamers.beluga.gpg;

import android.app.Activity;

import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.games.Games;

/**
 * Created by user on 2016/9/29.
 */

public class GPGService {
    Activity activity;
    GoogleApiClient.ConnectionCallbacks connectionCallbacks;
    GoogleApiClient.OnConnectionFailedListener onConnectionFailedListener;

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

    int responeCode;

    public GPGService(Activity activity, GoogleApiClient.ConnectionCallbacks connectionCallbacks,
                      GoogleApiClient.OnConnectionFailedListener onConnectionFailedListener){
        this.activity = activity;
        this.connectionCallbacks = connectionCallbacks;
        this.onConnectionFailedListener = onConnectionFailedListener;
    }
    public void Create(){
        // Create the Google Api Client with access to Plus and Games

        mGoogleApiClient = new GoogleApiClient.Builder(this.activity)
                .addConnectionCallbacks(this.connectionCallbacks)
                .addOnConnectionFailedListener(this.onConnectionFailedListener)
                .addApi(Games.API).addScope(Games.SCOPE_GAMES)
                .build();

    }
}
