package com.ratik.almostsnapchat;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;

import com.amazonaws.services.s3.AmazonS3;
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
public class ViewImageActvity extends AppCompatActivity {

    private static final String TAG = ViewImageActvity.class.getSimpleName();
    private String key;

    private ProgressBar progressBar;
    private ImageView imageView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_show);

        Intent intent = getIntent();
        key = intent.getStringExtra("key");

        imageView = (ImageView) findViewById(R.id.imageSnapView);
        progressBar = (ProgressBar) findViewById(R.id.progressBar);

        getImage();
    }

    private void getImage() {
        progressBar.setVisibility(View.VISIBLE);
        GeneratePresignedUrlRequest urlRequest = new GeneratePresignedUrlRequest(Constants.MY_BUCKET, key);
        urlRequest.setExpiration(new Date(System.currentTimeMillis() + 60000));

        AmazonS3 s3Client = Util.getS3Client(this);
        URL url = s3Client.generatePresignedUrl(urlRequest);

        Picasso.with(this).load(url.toString()).into(imageView, new Callback() {
            @Override
            public void onSuccess() {
                progressBar.setVisibility(View.INVISIBLE);
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

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finish();
    }
}
