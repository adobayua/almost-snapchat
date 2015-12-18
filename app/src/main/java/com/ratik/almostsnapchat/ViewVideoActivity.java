package com.ratik.almostsnapchat;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.Toast;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.GeneratePresignedUrlRequest;

import java.net.URISyntaxException;
import java.net.URL;
import java.util.Date;

/**
 * Created by Ratik on 18/12/15.
 */
public class ViewVideoActivity extends AppCompatActivity {

    private static final String TAG = ViewImageActvity.class.getSimpleName();
    private String key;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_show);

        Intent intent = getIntent();
        key = intent.getStringExtra("key");

        getVideo();
    }

    private void getVideo() {
        GeneratePresignedUrlRequest urlRequest = new GeneratePresignedUrlRequest(Constants.MY_BUCKET, key);
        urlRequest.setExpiration(new Date(System.currentTimeMillis() + 360000));

        AmazonS3 s3Client = Util.getS3Client(this);
        URL url = s3Client.generatePresignedUrl(urlRequest);

        Uri uri = null;
        try {
            uri = Uri.parse(url.toURI().toString());
        } catch (URISyntaxException e) {
            Toast.makeText(this, "ERROR!", Toast.LENGTH_LONG).show();
        }
        Intent intent = new Intent(Intent.ACTION_VIEW, uri);
        intent.setDataAndType(uri, "video/*");
        startActivity(intent);
    }

    @Override
    protected void onPause() {
        super.onPause();
        finish();
    }
}
