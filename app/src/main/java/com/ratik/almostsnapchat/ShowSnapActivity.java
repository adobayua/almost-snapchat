package com.ratik.almostsnapchat;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.ImageView;

import com.amazonaws.auth.CognitoCachingCredentialsProvider;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.model.GeneratePresignedUrlRequest;
import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;

import java.net.URL;
import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;

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

        GeneratePresignedUrlRequest urlRequest = new GeneratePresignedUrlRequest(Constants.MY_BUCKET, key);
        urlRequest.setExpiration(new Date(System.currentTimeMillis() + 60000));

        // Initialize the Amazon Cognito credentials provider
        CognitoCachingCredentialsProvider credentialsProvider =
                new CognitoCachingCredentialsProvider(
                        getApplicationContext(),
                        Constants.POOL_ID, // Identity Pool ID
                        Regions.US_EAST_1 // Region
                );

        AmazonS3 s3Client = new AmazonS3Client(credentialsProvider);

        URL url = s3Client.generatePresignedUrl(urlRequest);
        Picasso.with(this).load(url.toString()).into(imageView, new Callback() {
            @Override
            public void onSuccess() {
                Timer timer = new Timer();
                timer.schedule(new TimerTask() {
                    @Override
                    public void run() {
                        finish();
                    }
                }, 10 * 1000);
            }

            @Override
            public void onError() {

            }
        });
    }
}
