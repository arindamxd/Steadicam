package me.echodev.steadicam;

import android.content.Intent;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;

import com.amazonaws.mobile.client.AWSMobileClient;
//import com.amazonaws.mobile.config.AWSConfiguration;
import com.amazonaws.mobileconnectors.s3.transferutility.*;
//import com.amazonaws.mobileconnectors.s3.transferutility.TransferUtility;
//import com.amazonaws.mobileconnectors.s3.transferutility.TransferState;
//import com.amazonaws.mobileconnectors.s3.transferutility.TransferObserver;
//import com.amazonaws.mobileconnectors.s3.transferutility.TransferListener;
import com.amazonaws.services.s3.AmazonS3Client;
import com.bumptech.glide.Glide;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;

public class MainActivity extends AppCompatActivity {

    static final int REQUEST_IMAGE_CAPTURE = 101;

    private Button btnUpload, btnPhoto;
    private ImageView imgCover;
    private String photoPath;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        btnPhoto = findViewById(R.id.btn_photo);
        btnUpload = findViewById(R.id.btn_upload);
        imgCover = findViewById(R.id.img_cover);

        createDir();
        AWSMobileClient.getInstance().initialize(this).execute();

        btnPhoto.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dispatchTakePictureIntent();
            }
        });
        btnUpload.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                uploadData();
            }
        });

//        Glide.with(this)
//                .load(R.drawable.imgur_1080p)
//                .into(imgCover);
    }

    private void dispatchTakePictureIntent() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
            File photoFile = createImageFile();
            photoPath = photoFile.getAbsolutePath();
            Uri photoUri = FileProvider.getUriForFile(getApplicationContext(), getPackageName() + ".fileprovider", photoFile);
            takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoUri);
            startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK) {
            Glide.with(this)
                    .load(photoPath)
                    .into(imgCover);
        }
    }

    public void uploadData() {

        String s3FilePath = "s3Folder/s3Key.txt";
//        String localFilePath = "/path/to/file/localFile.txt";
        String localFilePath = photoPath;

        // Initialize AWSMobileClient if not initialized upon the app startup.
        // AWSMobileClient.getInstance().initialize(this).execute();

        TransferUtility transferUtility = TransferUtility.builder()
                .context(getApplicationContext())
                .awsConfiguration(AWSMobileClient.getInstance().getConfiguration())
                .s3Client(new AmazonS3Client(AWSMobileClient.getInstance().getCredentialsProvider()))
                .build();

        TransferObserver uploadObserver = transferUtility.upload(s3FilePath, new File(localFilePath));

        uploadObserver.setTransferListener(new TransferListener() {

            @Override
            public void onStateChanged(int id, TransferState state) {
                if (state == TransferState.COMPLETED) {
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
        if (uploadObserver.getState() == TransferState.COMPLETED) {
            // Handle a completed upload.
        }
    }

    private File createImageFile() {
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageName = timeStamp + ".jpg";
        String imagePath = Environment.getExternalStorageDirectory().getAbsolutePath() + File.separator + "Steadicam";
        imagePath += File.separator + Environment.DIRECTORY_PICTURES + File.separator + imageName;

        return new File(imagePath);
    }

    private void createDir() {
        File appDirPicture = new File(Environment.getExternalStorageDirectory()
                + File.separator
                + "Steadicam"
                + File.separator
                + Environment.DIRECTORY_PICTURES
        );

        if (!appDirPicture.exists()) {
            appDirPicture.mkdirs();
        }
    }
}
