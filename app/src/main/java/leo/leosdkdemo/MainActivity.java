package leo.leosdkdemo;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.pm.Signature;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
import android.support.annotation.Nullable;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.facebook.AccessToken;
import com.facebook.FacebookSdk;
import com.facebook.share.widget.LikeView;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdSize;
//import com.google.android.gms.appindexing.Action;
//import com.google.android.gms.appindexing.AppIndex;
import com.google.android.gms.common.api.GoogleApiClient;
import com.hosengamers.beluga.ads.Banner;
import com.hosengamers.beluga.ads.BannerAdListener;
import com.hosengamers.beluga.belugakeys.Keys;
import com.hosengamers.beluga.gpg.GPGService;
import com.hosengamers.beluga.gpg.GpgActivity;
import com.hosengamers.beluga.invite.FacebookFriendsInviteActivity;
import com.hosengamers.beluga.loginpage.AuthClientActivity;
import com.hosengamers.beluga.loginpage.FacebookInfoManager;
import com.hosengamers.beluga.payment.iab.InAppBillingActivity;
import com.hosengamers.beluga.payment.mol.MOLActivity;
import com.hosengamers.beluga.payment.mycard.MyCardActivity;
import com.hosengamers.beluga.share.FacebookShare;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.json.JSONObject;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;

import bolts.AppLinks;



@SuppressWarnings("deprecation")
public class MainActivity extends Activity {

    String appid = "MEMBER";
    String apikey = "a0c5560931b60786a9190a29c03a38bc";
    Intent serviceIntent;
    public final static int REQUEST_CODE = -1010101; /*(see edit II)*/
    Bundle b;

    GPGService gpgService;
    /**
     * ATTENTION: This was auto-generated to implement the App Indexing API.
     * See https://g.co/AppIndexing/AndroidStudio for more information.
     */
    private GoogleApiClient client;

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Log.i("Main Demo", "onActivityResult start...");
        Log.i("Main Demo", "requestCode:" + requestCode);
        Log.i("Main Demo", "resultCode:" + Activity.RESULT_OK);
        if (requestCode == 100 && resultCode == Activity.RESULT_OK) {
            try {
                TextView v = (TextView) this.findViewById(R.id.tv1);
                Bundle bundle = data.getExtras();
                String type = bundle.getString("type");
                Log.i("Main Demo", "type is " + type);
                if (type.equals("LOGIN")) {
                    Log.i("Main Demo", "Is " + type + "do if condition...");

                    String jsonData = bundle.getString(Keys.JsonData.toString());
                    v.setText("Login info :\nLogin Json Data:" + jsonData);

                    Log.i("fb login ", "status :"+ (AccessToken.getCurrentAccessToken()!=null) );
                    if (AccessToken.getCurrentAccessToken() != null){
                        FacebookInfoManager facebookInfoManager = new FacebookInfoManager();
                        facebookInfoManager.loadFriendsList();
                        Log.i("List", facebookInfoManager.getFriendsList());
                    }



                    /*
                                         * 测试强验证：
                                         * 将 String 转换成Json
                                         * 在Json中取得token，送去需求参数appid，apikey，Token，MD5
                                         *
                                         * MD5 加密三次 MD5(MD5(MD5(appid + apikey + token)))
                                         */
                    JSONObject jObj = new JSONObject(jsonData);
                    String token = jObj.getString("token");
                    final List<NameValuePair> params = new ArrayList<NameValuePair>();
                    //MD5 加密三次
                    String sign = MD5(MD5(MD5(appid + apikey + token)));
                    params.add(new BasicNameValuePair("appid", appid));
                    params.add(new BasicNameValuePair("apikey", apikey));
                    params.add(new BasicNameValuePair("Token", token));
                    params.add(new BasicNameValuePair("MD5", sign));

                    Runnable runnable = new Runnable() {
                        @Override
                        public void run() {

                            makePostRequest(params);
                        }
                    };
                    new Thread(runnable).start();



                } else if (type.equals("PAYMENT")) {
                    Log.i("Main Demo", "Is " + type + "do else if condition...");
                    String order = bundle.getString("order");
                    v.setText("json order: \n" + order + "\n" + "response code: \n" + bundle.getInt("response") + "\n" +
                            "status:\n" + bundle.getString("status") + "\n");
                }else if(type.equals("SHARE")){
                    String value = bundle.getString(Keys.JsonData.toString());
                    v.setText("value: \n" + value );
                }else if(type.equals("GAME_INVITE")){
                    String value = bundle.getString(Keys.JsonData.toString());
                    v.setText("json: \n" + value );
                }else if (type.equals("APP_INVITE")){
                    String value = bundle.getString(Keys.JsonData.toString());
                    v.setText("json: \n" + value );
                }else if(type.equals("GPG")){
                    Log.i("Main Demo", "success...");
                }
            } catch (Exception ex) {
                ex.printStackTrace();
            }

        }else if(requestCode == 9001){

            gpgService.onResult(requestCode, resultCode, data);
            Log.d("Activity", "Success.");
        }
        Log.i("Main Demo", "onActivityResult end...");
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        FacebookSdk.sdkInitialize(getApplicationContext());

        setContentView(R.layout.activity_main);

        Uri targetUrl =
                AppLinks.getTargetUrlFromInboundIntent(this, getIntent());
        Log.i("Activity", "App Link Target status: " + (targetUrl != null));
        if (targetUrl != null) {
            Log.i("Activity", "App Link Target URL: " + targetUrl.toString());
        }
        LikeView likeView = (LikeView) findViewById(R.id.likeView);
        likeView.setObjectIdAndType(
                "https://www.facebook.com/DarkChessworld/",
                LikeView.ObjectType.PAGE);

        try {
            Log.d("KeyHash:", "start...");
            PackageInfo info = getPackageManager().getPackageInfo(
                    "beluga.com.belugademov3", PackageManager.GET_SIGNATURES);
            for (Signature signature : info.signatures) {
                MessageDigest md = MessageDigest.getInstance("SHA");
                md.update(signature.toByteArray());
                Log.d("KeyHash:", Base64.encodeToString(md.digest(), Base64.DEFAULT));
            }
            Log.d("KeyHash:", "end...");
        } catch (NameNotFoundException e) {
            Log.d("KeyHash e1:", e.toString());
        } catch (NoSuchAlgorithmException e) {
            Log.d("KeyHash e2:", e.toString());
        }

        Button btnMorph1 = (Button) this.findViewById(R.id.b1);
        btnMorph1.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                StartAuthClient();
            }
        });

        BannerAdListener adListener = new BannerAdListener() {
            @Override
            public void onAdLoaded() {

            }

            @Override
            public void onAdFailedToLoad(String paramString) {

            }

            @Override
            public void onAdOpened() {

            }

            @Override
            public void onAdClosed() {

            }

            @Override
            public void onAdLeftApplication() {

            }
        };
        Banner banner = new Banner(this, adListener);
        AdSize adsize = new AdSize(300, 50);

        int positon_bottom = 1;
        banner.create("ca-app-pub-8757418816557505/7494005598", adsize, positon_bottom);
        //banner for test...
        AdRequest adRequest = new AdRequest.Builder()
                .addTestDevice(AdRequest.DEVICE_ID_EMULATOR)
                .addTestDevice("2E8B2CCE503B3B15EE7E53F3C0E62CE4")
                .build();

        banner.loadAd(adRequest);
        banner.show();
        // ATTENTION: This was auto-generated to implement the App Indexing API.
        // See https://g.co/AppIndexing/AndroidStudio for more information.
        //client = new GoogleApiClient.Builder(this).addApi(AppIndex.API).build();
    }

    private void StartAuthClient() {

        String appid = "MEMBER";
        String apikey = "a0c5560931b60786a9190a29c03a38bc";
        String packageID = this.getClass().getPackage().getName();
        Intent intent;
        boolean inMaintain = false;
        String dialogTitle = "Warnings";// if inMaintain is false setDialog title null
        String dialogMessage = "server in maintain...";// if inMaintain is false setDialog message null

        intent = new Intent(this, AuthClientActivity.class);
        intent.putExtra(Keys.AppID.toString(), appid);
        intent.putExtra(Keys.ApiKey.toString(), apikey);
        intent.putExtra(Keys.PackageID.toString(), packageID);
        intent.putExtra(Keys.GameLogo.toString(), R.drawable.bg_pic);
        intent.putExtra(Keys.GameBackground.toString(), R.drawable.bg_pic);
        intent.putExtra(Keys.ActiveMaintainDialog.toString(), inMaintain);
        intent.putExtra(Keys.DialogMessage.toString(), dialogMessage);
        intent.putExtra(Keys.DialogTitle.toString(), dialogTitle);
        Log.i("pID", packageID);
        startActivityForResult(intent, 100);
    }

    public void startGooglePaymentButtonPress(View v) {
        String SKU_GAS = "gold";
        String base64 = "MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAg7qTllPgQQycszoBnRlOqxigZQ0nXjcN8qJtRskfYHdLrgWzU2qp7XNuUyrlSIqsFRY/t5VIUP0Q+VTs21zAFcJUyFicDb2s8gQcBnruO5rq20ivaZqv+YdOhHT0+8ZgTKzJ/jnD8xh/QkjBFMKlX3N8trW65Cqum6IoffTR9RlAtQpqkc0NQ4cRmV3wua0Ornr4KGd8ENqyI+KmQ0fnnpMWSCzv+Vscg6erRtRN+912W+8RU9Meo3MK/+NVPl6xxH6+8uUYSAqq9oXE8v/GHaYcjs61rCu3w3uTTdj65a4oAfBZgp73LD4EBs/vkRMfUlZGIxXz7lrh/fYQUKLAEQIDAQAB";
        String UserId = "1030176"; //user id

        Intent i = new Intent(MainActivity.this, InAppBillingActivity.class);
        Bundle b = new Bundle();
        b.putString(InAppBillingActivity.base64EncodedPublicKey, base64);
        b.putString(InAppBillingActivity.ItemID, SKU_GAS);
        b.putString(InAppBillingActivity.User_ID, UserId);


        b.putString(InAppBillingActivity.Server, "TestServer1");
        b.putString(InAppBillingActivity.Role, "leo");
        b.putString(InAppBillingActivity.Order, "TestBelugaItem");
        i.putExtras(b);
        startActivityForResult(i, InAppBillingActivity.GBilling_REQUEST);
    }


    public void startMOLPaymentButtonPress(View v) {
        String user_id = "1000005";
        String game_id = "04101786";
        String app_id = "herinv";
        String PackageID = this.getClass().getPackage().getName();
        String server_id = "999";
        String role = "leo";
        Intent i = new Intent(this, MOLActivity.class);
        Bundle b = new Bundle();
        b.putString("UserId", user_id);
        b.putString("gamerID", game_id);
        b.putString("AppID", app_id);
        b.putString("PackageID", PackageID);
        b.putString("ServerID", server_id);
        b.putString("RoleName", role);
        i.putExtras(b);
        startActivity(i);
    }


    public void startMyCardSmallPaymentButtonPress(View v) {
        Intent i = new Intent(this, MyCardActivity.class);
        Bundle b = new Bundle();

        String serviceType = MyCardActivity.TYPE_SMALL_PAYMENT;
        String apikey = "412c1bd510967dce3b050842a35fae18";
        String appid = "kilmasa";
        String uid = "1040714";


        b.putString("type", serviceType);
        b.putString(Keys.ApiKey.toString(), apikey);
        b.putString(Keys.AppID.toString(), appid);
        b.putString("uid", uid);


        b.putString("tserver", "TestServer1");
        b.putString("trol", "test");
        b.putString("titem", "TestItem1");


        b.putString("order", "order12345");
        i.putExtras(b);
        startActivity(i);
    }

    public void startMyCardSerialNumberButtonPress(View v) {
        Intent i = new Intent(this, MyCardActivity.class);
        Bundle b = new Bundle();

        b.putString("type", MyCardActivity.TYPE_SERIAL_NUMBER);
        b.putString(Keys.ApiKey.toString(), "4fbc7b551f8ab63d4dadd8694ff261bf");
        b.putString(Keys.AppID.toString(), "fanadv3");
        b.putString("uid", "1000448");


        b.putString("tserver", "TestServer1");
        b.putString("trol", "test");
        b.putString("titem", "TestItem1");

        b.putString("order", "order12345");
        i.putExtras(b);
        startActivity(i);
    }


    public void stopService(View v) {
        if (serviceIntent != null) {
            stopService(serviceIntent);
        }
    }

    public void callFacebookInvite(View v) {
        Log.i("main act", "invite start...");

        String appLinkUrl = "https://fb.me/1766547930283000";
        String previewImageUrl = "https://lh3.googleusercontent.com/8yh5cZfJpWbUADdb3zF_XF-zxVFhc7-w7nl1sMMZFdI3UgidVLsDZ7LXeChz5C_X8GQ=w300-rw";
        Intent i = new Intent(this, FacebookFriendsInviteActivity.class);
        i.putExtra(Keys.AppLinkUrl.toString(), appLinkUrl);
        i.putExtra(Keys.AppLinkPreviewImageUrl.toString(), previewImageUrl);
        startActivityForResult(i, 100);
    }

    public void callFacebookGameInvite(View v) {
        Log.i("main act", "game invite start...");
        String shareContentTitle = "test";
        String shareContentDescription = "test";
        Intent intent = new Intent(this, com.hosengamers.beluga.invite.FacebookGameInviteActivity.class);
        intent.putExtra(Keys.ShareContentDescription.toString(), shareContentDescription);
        intent.putExtra(Keys.ShareContentTitle.toString(), shareContentTitle);
        startActivityForResult(intent, 100);
    }

    public void callFacebookShare(View v) {
        Log.i("main act", "invite start...");

        String shareContentTitle = "hello";
        String shareContentDescription = "hello";
        String shareContentUrl = "https://play.google.com/store/apps/details?id=com.HSGame.HSGameAcne";
        String shareImageUrl = "https://lh3.googleusercontent.com/8yh5cZfJpWbUADdb3zF_XF-zxVFhc7-w7nl1sMMZFdI3UgidVLsDZ7LXeChz5C_X8GQ=w300-rw";
        Intent intent = new Intent(this, FacebookShare.class);
        intent.putExtra(Keys.ShareContentUrl.toString(), shareContentUrl);
        intent.putExtra(Keys.ShareImageUrl.toString(), shareImageUrl);
        intent.putExtra(Keys.ShareContentDescription.toString(), shareContentDescription);
        intent.putExtra(Keys.ShareContentTitle.toString(), shareContentTitle);
        startActivityForResult(intent, 100);
    }

    public void gpg(View view){
        Log.i("main act", "GPG start...");
        /*
        Intent intent = new Intent(this, GpgActivity.class);
        startActivityForResult(intent, 100);
             */
        gpgService = new GPGService(this);
        gpgService.Create();
    }

    public void gpgLogout(View view){
        Log.i("main act", "gpgLogout...");
        gpgService.onSignOutButtonClicked();
    }


    @Override
    protected void onStop() {
        super.onStop();
        Log.i("main act", "GPG stop...");
        if (gpgService != null){
            gpgService.disconnect();
        }

    }

    public void showLeaderboards(View view){
        if(gpgService != null){
            gpgService.onShowLeaderboardsRequested();
        }

    }

    public void showAchievements(View view){

        if(gpgService != null){
            gpgService.onShowAchievementsRequested();
        }


    }

    public void unlockLeaderboards(View view){
        if(gpgService != null){
            gpgService.unlockLeaderboardsSubmitScore("CgkI6vSku6EeEAIQBw", 1339);
        }

    }

    public void unlockAchievements(View view){
        if(gpgService != null){
            gpgService.unlockAchievements("CgkI6vSku6EeEAIQAg");
        }
    }

    private void makePostRequest(List<NameValuePair> params) {

        HttpClient httpClient = new DefaultHttpClient();
        // replace with your url
        HttpPost httpPost = new HttpPost("http://games.belugame.com/api/Auth/?");


        //Encoding POST data
        try {
            Log.i("test", "line 266...");
            httpPost.setEntity(new UrlEncodedFormEntity(params));
        } catch (UnsupportedEncodingException e) {
            // log exception
            Log.i("test", "line 270...");
            e.printStackTrace();
        }

        //making POST request.
        try {
            Log.i("test", "line 276...");

            HttpResponse response = httpClient.execute(httpPost);
            // write response to log
            Log.d("Http Post Response:", response.toString());
            Log.i("post status>>>", "httpResponse.getStatusLine().getStatusCode():" + response.getStatusLine().getStatusCode());
            if (response.getStatusLine().getStatusCode() == 200) {

                String strResult = EntityUtils.toString(response.getEntity());
                Log.i("post Result>>>", "strResult:" + strResult);
            }
        } catch (ClientProtocolException e) {
            Log.i("test", "line 281...");
            // Log exception
            e.printStackTrace();
        } catch (IOException e) {
            Log.i("test", "line 285...");
            // Log exception
            e.printStackTrace();
        }

    }

    //MD5 encrypt
    private String MD5(String str) {
        MessageDigest md5 = null;
        try {
            md5 = MessageDigest.getInstance("MD5");
        } catch (Exception e) {
            e.printStackTrace();
            return "";
        }

        char[] charArray = str.toCharArray();
        byte[] byteArray = new byte[charArray.length];

        for (int i = 0; i < charArray.length; i++) {
            byteArray[i] = (byte) charArray[i];
        }
        byte[] md5Bytes = md5.digest(byteArray);

        StringBuffer hexValue = new StringBuffer();
        for (int i = 0; i < md5Bytes.length; i++) {
            int val = ((int) md5Bytes[i]) & 0xff;
            if (val < 16) {
                hexValue.append("0");
            }
            hexValue.append(Integer.toHexString(val));
        }
        return hexValue.toString();
    }

}
