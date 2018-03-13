package me.echodev.steadicam;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;

import com.amazonaws.mobile.client.AWSMobileClient;
//import com.amazonaws.mobile.config.AWSConfiguration;
//import com.amazonaws.mobileconnectors.s3.transferutility.*;
import com.amazonaws.mobileconnectors.s3.transferutility.TransferUtility;
import com.amazonaws.mobileconnectors.s3.transferutility.TransferState;
import com.amazonaws.mobileconnectors.s3.transferutility.TransferObserver;
import com.amazonaws.mobileconnectors.s3.transferutility.TransferListener;
import com.amazonaws.services.s3.AmazonS3Client;

import java.io.File;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        AWSMobileClient.getInstance().initialize(this).execute();
    }

    public void uploadData() {

        // Initialize AWSMobileClient if not initialized upon the app startup.
        // AWSMobileClient.getInstance().initialize(this).execute();

        TransferUtility transferUtility =
                TransferUtility.builder()
                        .context(getApplicationContext())
                        .awsConfiguration(AWSMobileClient.getInstance().getConfiguration())
                        .s3Client(new AmazonS3Client(AWSMobileClient.getInstance().getCredentialsProvider()))
                        .build();

        TransferObserver uploadObserver =
                transferUtility.upload(
                        "s3Folder/s3Key.txt",
                        new File("/path/to/file/localFile.txt"));

        uploadObserver.setTransferListener(new TransferListener() {

            @Override
            public void onStateChanged(int id, TransferState state) {
                if (TransferState.COMPLETED == state) {
                    // Handle a completed upload.
                }
            }

            @Override
            public void onProgressChanged(int id, long bytesCurrent, long bytesTotal) {
                float percentDonef = ((float)bytesCurrent/(float)bytesTotal) * 100;
                int percentDone = (int)percentDonef;

                Log.d("MainActivity", "   ID:" + id + "   bytesCurrent: " + bytesCurrent + "   bytesTotal: " + bytesTotal + " " + percentDone + "%");
            }

            @Override
            public void onError(int id, Exception ex) {
                // Handle errors
            }

        });

        // If your upload does not trigger the onStateChanged method inside your
        // TransferListener, you can directly check the transfer state as shown here.
        if (TransferState.COMPLETED == uploadObserver.getState()) {
            // Handle a completed upload.
        }
    }
}
