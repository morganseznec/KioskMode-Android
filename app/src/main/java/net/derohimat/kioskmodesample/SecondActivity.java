package net.derohimat.kioskmodesample;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
import android.view.View;
import android.widget.Button;

import androidx.core.content.FileProvider;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * Created by derohimat on 19/08/2016.
 */
public class SecondActivity extends BaseActivity implements View.OnClickListener {

    private Button btnState;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_second);

        btnState = findViewById(R.id.btnState);
        Button btnBack = findViewById(R.id.btnBack);
        Button btnUpdateApp = findViewById(R.id.btnUpdateApp); // Update app button
        Button btnQuitApp = findViewById(R.id.btnQuitApp); // Quit app button
        Button btnOpenWifiSettings = findViewById(R.id.btnOpenWifiSettings);

        btnState.setOnClickListener(this);
        btnBack.setOnClickListener(this);
        btnUpdateApp.setOnClickListener(this); // Set click listener
        btnQuitApp.setOnClickListener(this); // Set click listener for quit
        btnOpenWifiSettings.setOnClickListener(this); // Set click listener

        setUpAdmin();
        updateButtonState();
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.btnState:
                enableKioskMode(!KioskModeApp.isInLockMode());
                updateButtonState();
                break;
            case R.id.btnBack:
                finish();
                break;
            case R.id.btnUpdateApp:
                updateApp(); // Call update app method
                break;
            case R.id.btnQuitApp:
                quitApp(); // Call quit app method
                break;
            case R.id.btnOpenWifiSettings:
                openWifiSettings();
                break;
        }
    }

    public static void startThisActivity(Context context) {
        Intent intent = new Intent(context, SecondActivity.class);
        context.startActivity(intent);
    }

    private void quitApp() {
        finishAffinity(); // Closes all activities in the task and stops the app
    }

    private void updateApp() {
        // Implement the download and installation process in a background thread
        new Thread(() -> {
            try {
                URL url = new URL("https://ubiq-android-apk.s3.eu-west-1.amazonaws.com/lnblive.apk"); // URL of your APK
                HttpURLConnection c = (HttpURLConnection) url.openConnection();
                c.setRequestMethod("GET");
                c.connect();

                // Use the app's private storage directory
                File apkFile = new File(getExternalFilesDir(null), "update.apk");
                if (!apkFile.exists()) {
                    boolean created = apkFile.createNewFile();
                    if (!created) {
                        // Handle the error of file creation
                        return;
                    }
                }
                FileOutputStream fos = new FileOutputStream(apkFile);
                InputStream is = c.getInputStream();

                byte[] buffer = new byte[1024];
                int len1;
                while ((len1 = is.read(buffer)) != -1) {
                    fos.write(buffer, 0, len1);
                }
                fos.close();
                is.close();

                // Trigger the installation
                if (apkFile.exists()) {
                    Uri apkUri = FileProvider.getUriForFile(SecondActivity.this, BuildConfig.APPLICATION_ID + ".provider", apkFile);
                    Intent intent = new Intent(Intent.ACTION_VIEW);
                    intent.setDataAndType(apkUri, "application/vnd.android.package-archive");
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                    startActivity(intent);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }).start();
    }

    private void updateButtonState() {
        if (KioskModeApp.isInLockMode()) {
            btnState.setText("Disable Kiosk Mode");
        } else {
            btnState.setText("Enable Kiosk Mode");
        }
    }

    private void openWifiSettings() {
        startActivity(new Intent(Settings.ACTION_WIFI_SETTINGS));
    }
}
