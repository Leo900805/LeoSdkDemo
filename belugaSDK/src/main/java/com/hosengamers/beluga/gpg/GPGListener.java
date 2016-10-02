package com.hosengamers.beluga.gpg;

import android.content.Intent;

/**
 * Created by user on 2016/9/30.
 */

public interface GPGListener {
    public void onStartGameRequested(boolean hardMode);
    public void onShowAchievementsRequested();
    public void onShowLeaderboardsRequested();
    public void onSignInButtonClicked();
    public void onSignOutButtonClicked();
    public void onResult(int requestCode, int responseCode, Intent intent);
    public void disconnect();
    public void unlockAchievements(String achievement_id);
    public void unlockLeaderboardsSubmitScore(String leaderboard_id, long l);
}
