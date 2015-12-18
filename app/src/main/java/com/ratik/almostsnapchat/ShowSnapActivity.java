package com.ratik.almostsnapchat;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.ImageView;

import com.amazonaws.auth.CognitoCachingCredentialsProvider;
import com.amazonaws.mobileconnectors.s3.transferutility.TransferListener;
import com.amazonaws.mobileconnectors.s3.transferutility.TransferObserver;
import com.amazonaws.mobileconnectors.s3.transferutility.TransferState;
import com.amazonaws.mobileconnectors.s3.transferutility.TransferUtility;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3Client;

import java.io.File;

/**
 * Created by Ratik on 18/12/15.
 */
public class ShowSnapActivity extends AppCompatActivity {

    private static final String TAG = ShowSnapActivity.class.getSimpleName();
    private String key;
    private ImageView imageView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_show);

        Intent intent = getIntent();
        key = intent.getStringExtra("key");

        imageView = (ImageView) findViewById(R.id.imageSnapView);

        downloadMedia();
    }

    private void downloadMedia() {
        // Get the external storage directory
        String appName = ShowSnapActivity.this.getString(R.string.app_name);
        File mediaStorageDir = new File(
                Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES),
                appName + "/" + key);

        // Initialize the Amazon Cognito credentials provider
        CognitoCachingCredentialsProvider credentialsProvider =
                new CognitoCachingCredentialsProvider(
                        getApplicationContext(),
                        Constants.POOL_ID, // Identity Pool ID
                        Regions.US_EAST_1 // Region
                );

        AmazonS3 s3Client = new AmazonS3Client(credentialsProvider);
        TransferUtility transferUtility = new TransferUtility(s3Client, getApplicationContext());

        // Initiate the download
        final TransferObserver observer = transferUtility.download(Constants.MY_BUCKET, key, mediaStorageDir);
        observer.setTransferListener(new TransferListener() {
            @Override
            public void onStateChanged(int id, TransferState state) {

            }

            @Override
            public void onProgressChanged(int id, long bytesCurrent, long bytesTotal) {
                int percentage = (int) (bytesCurrent / bytesTotal * 100);
                Log.d(TAG, "Downloading: " + percentage + "%");
                if(percentage == 100) {
                    Bitmap bitmap = BitmapFactory.decodeFile(observer.getAbsoluteFilePath());
                    imageView.setImageBitmap(bitmap);
                }
            }

            @Override
            public void onError(int id, Exception ex) {

            }
        });


    }
}
