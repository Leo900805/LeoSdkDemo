package com.hosengamers.beluga.gpg;

import android.app.Activity;
import android.content.Intent;
import android.content.IntentSender;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.Api;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.games.Games;
import com.google.android.gms.games.Player;


/**
 * Created by user on 2016/9/29.
 */

public class GPGService implements GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener, GPGListener{

    Activity activity;

    // Request code used to invoke sign in user interactions.
    private static final int RC_SIGN_IN = 9001;
    private static final int RC_UNUSED = 5001;
    private static final String TAG = "Leo GPGService";
    public GoogleApiClient mGoogleApiClient;


    // Are we currently resolving a connection failure?
    private boolean mResolvingConnectionFailure = false;

    // Has the user clicked the sign-in button?
    private boolean mSignInClicked = false;

    // Set to true to automatically start the sign in flow when the Activity starts.
    // Set to false to require the user to click the button in order to sign in.
    private boolean mAutoStartSignInFlow = true;

    public GPGService(Activity activity){
        Log.d(TAG, "new Activity..");
        this.activity = activity;
    }

    public void Create(){
        Log.d(TAG, "onCreate...");
        // Create the Google Api Client with access to Plus and Games
        Log.i(TAG, "Create api...");
        GPGService.this.mGoogleApiClient = new GoogleApiClient.Builder(GPGService.this.activity)
                .addConnectionCallbacks(GPGService.this)
                .addOnConnectionFailedListener(GPGService.this)
                .addApi(Games.API).addScope(Games.SCOPE_GAMES)
                .build();
        Log.i(TAG, "Create api...end");
        GPGService.this.mGoogleApiClient.connect();
        Log.d(TAG, "onCreate...end");
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        Log.d(TAG, "onConnectied...");
        // Log.d(TAG, "onConnectied...bundle.toString():"+bundle.toString());
        GPGService.this.mGoogleApiClient.connect();

        // Set the greeting appropriately on main menu
        Player p = Games.Players.getCurrentPlayer(GPGService.this.mGoogleApiClient);
        String displayName;
        if (p == null) {
            Log.w(TAG, "mGamesClient.getCurrentPlayer() is NULL!");
            displayName = "???";
            Toast.makeText(GPGService.this.activity, displayName, Toast.LENGTH_SHORT).show();
        } else {
            displayName = p.getDisplayName();
            Toast.makeText(GPGService.this.activity, displayName, Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onConnectionSuspended(int i) {
        Log.d(TAG, "onConnectionSuspended() called. Trying to reconnect.");
        GPGService.this.mGoogleApiClient.connect();
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        Log.d(TAG, "onConnectionFailed() called, result: " + connectionResult);

        if (GPGService.this.mResolvingConnectionFailure) {
            Log.d(TAG, "onConnectionFailed() ignoring connection failure; already resolving.");
            return;
        }

        if (GPGService.this.mSignInClicked || GPGService.this.mAutoStartSignInFlow) {
            Log.d(TAG, "onConnectionFailed()  (mSignInClicked || mAutoStartSignInFlow): " +  (GPGService.this.mSignInClicked || GPGService.this.mAutoStartSignInFlow));
            GPGService.this.mAutoStartSignInFlow = false;
            GPGService.this.mSignInClicked = false;
            try {
                Log.d(TAG, "onConnectionFailed()  in  try" );
                connectionResult.startResolutionForResult(GPGService.this.activity, RC_SIGN_IN);
                Log.d(TAG, "connectionResult.getResolution():"+connectionResult.getResolution() );
            } catch (IntentSender.SendIntentException e) {
                e.printStackTrace();
                GPGService.this.mGoogleApiClient.connect();
            }
        }
    }

    @Override
    public void onStartGameRequested(boolean hardMode) {

    }

    @Override
    public void onShowAchievementsRequested() {
        Log.d(TAG, "exe onShowAchievementsRequested()...");
        Log.d(TAG, "isSignedIn()...is "+ isSignedIn() );
        if (isSignedIn()) {
            Log.d(TAG, "in if condition...");
            GPGService.this.activity.startActivityForResult(Games.Achievements.getAchievementsIntent(mGoogleApiClient), RC_UNUSED);
        } else {
            Log.d(TAG, "in else condition...");
            Log.d(TAG, "Please Sign in to view achievements...");
            Toast.makeText(GPGService.this.activity, "Please Sign in to view achievements.", Toast.LENGTH_SHORT).show();
        }
        Log.d(TAG, "exe onShowAchievementsRequested()...end.");
    }

    @Override
    public void onShowLeaderboardsRequested() {
        Log.d(TAG, "exe onShowLeaderboardsRequested()...");
        Log.d(TAG, "isSignedIn()...is "+ isSignedIn() );
        if (isSignedIn()) {
            Log.d(TAG, "in if condition...");
            GPGService.this.activity.startActivityForResult(Games.Leaderboards.getAllLeaderboardsIntent(mGoogleApiClient), RC_UNUSED);
        } else {
            Log.d(TAG, "in else condition...");
            Log.d(TAG, "Please Sign in to view leaderboards...");
            Toast.makeText(GPGService.this.activity, "Please Sign in to view leaderboards.", Toast.LENGTH_SHORT).show();
        }
        Log.d(TAG, "exe onShowLeaderboardsRequested()...end.");
    }

    @Override
    public void onSignInButtonClicked() {
        // start the sign-in flow
        Log.d(TAG, "onSignInButtonClicked...");
        GPGService.this.mSignInClicked = true;
        GPGService.this.mGoogleApiClient.connect();
    }

    @Override
    public void onSignOutButtonClicked() {
        Log.d(TAG, "onSignOutButtonClicked...");
        if (GPGService.this.mGoogleApiClient.isConnected()) {
            Log.d(TAG, "in if condition exe GPGService.this.mGoogleApiClient.isConnected()...");
            GPGService.this.mSignInClicked = false;
            Games.signOut(GPGService.this.mGoogleApiClient);
            Log.d(TAG, "Games.signOut...");
            GPGService.this.mGoogleApiClient.disconnect();
        }else {
            Log.d(TAG, "in else condition already signout...");
        }
        Log.d(TAG, "onSignOutButtonClicked... end");
    }

    @Override
    public void onResult(int requestCode, int responseCode, Intent intent) {
        Log.d(TAG, "requestCode is "+requestCode+ " (requestCode == RC_SIGN_IN) is "+ (requestCode == RC_SIGN_IN) );
        if (GPGService.this.mGoogleApiClient != null){
            GPGService.this.mGoogleApiClient.connect();
        }else{
            Log.d(TAG, "GPGService.this.mGoogleApiClient is null...");
        }
        Log.d(TAG, "Success...");
    }

    @Override
    public void disconnect() {
        Log.d(TAG, "exe disconnect() ");
        if (GPGService.this.mGoogleApiClient.isConnected()) {
            Log.d(TAG, "disconnect(): in if disconnecting...");
            GPGService.this.mGoogleApiClient.disconnect();
        }
        Log.d(TAG, "end disconnect() ");
    }

    @Override
    public void unlockAchievements(String achievement_id) {
        Log.d(TAG, "exe  unlockAchievements...");
        if (isSignedIn()) {
            if (achievement_id != null){
                Log.d(TAG, "in if condition...");
                Games.Achievements.unlock(GPGService.this.mGoogleApiClient, achievement_id);
            }else {
                Log.d(TAG, "in if else...");
                Log.d(TAG, "Please input achievement id...");
                Toast.makeText(GPGService.this.activity, "Please input achievement id.", Toast.LENGTH_SHORT).show();
            }
        } else {
            Log.d(TAG, "in else condition...");
            Log.d(TAG, "Please Sign in to view achievements...");
            Toast.makeText(GPGService.this.activity, "Please Sign in to view achievements.", Toast.LENGTH_SHORT).show();
        }
        Log.d(TAG, "exe  unlockAchievements...end");
    }

    @Override
    public void unlockLeaderboardsSubmitScore(String leaderboard_id, long l) {
        Log.d(TAG, "exe  unlockLeaderboardsSubmitScore...");
        if (isSignedIn()) {
            if (leaderboard_id != null){
                Log.d(TAG, "in if condition...");
                Games.Leaderboards.submitScore(GPGService.this.mGoogleApiClient, leaderboard_id, l);
            }else {
                Log.d(TAG, "in if else...");
                Log.d(TAG, "Please input leaderboards id...");
                Toast.makeText(GPGService.this.activity, "Please input leaderboards id.", Toast.LENGTH_SHORT).show();
            }
        } else {
            Log.d(TAG, "in else condition...");
            Log.d(TAG, "Please Sign in to view leaderboards...");
            Toast.makeText(GPGService.this.activity, "Please Sign in to view leaderboards.", Toast.LENGTH_SHORT).show();
        }
        Log.d(TAG, "exe  unlockLeaderboardsSubmitScore...end");
    }

    private boolean isSignedIn() {
        return (GPGService.this.mGoogleApiClient != null && GPGService.this.mGoogleApiClient.isConnected());
    }

}
