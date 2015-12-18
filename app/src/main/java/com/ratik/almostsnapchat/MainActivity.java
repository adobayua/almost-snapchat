package com.ratik.almostsnapchat;

import android.Manifest;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import com.amazonaws.auth.CognitoCachingCredentialsProvider;
import com.amazonaws.mobileconnectors.s3.transferutility.TransferListener;
import com.amazonaws.mobileconnectors.s3.transferutility.TransferObserver;
import com.amazonaws.mobileconnectors.s3.transferutility.TransferState;
import com.amazonaws.mobileconnectors.s3.transferutility.TransferUtility;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3Client;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = MainActivity.class.getSimpleName();

    public static final int TAKE_PHOTO_REQUEST = 0;
    public static final int TAKE_VIDEO_REQUEST = 1;

    public static final int MEDIA_TYPE_IMAGE = 3;
    public static final int MEDIA_TYPE_VIDEO = 4;

    public static final int VIDEO_SECOND_LIMIT = 10;
    public static final int VIDEO_QUALITY = 1;

    protected Uri mMediaUri;

    private DialogInterface.OnClickListener mDialogListener =
            new DialogInterface.OnClickListener() {
        @Override
        public void onClick(DialogInterface dialog, int which) {
            switch(which) {
                case TAKE_PHOTO_REQUEST:
                    // Take picture
                    Intent takePhotoIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                    mMediaUri = getOutputMediaFileUri(MEDIA_TYPE_IMAGE);
                    if (mMediaUri == null) {
                        // Display an error
                        Toast.makeText(MainActivity.this, R.string.error_external_storage,
                                Toast.LENGTH_LONG).show();
                    }
                    else {
                        takePhotoIntent.putExtra(MediaStore.EXTRA_OUTPUT, mMediaUri);
                        startActivityForResult(takePhotoIntent, TAKE_PHOTO_REQUEST);
                    }
                    break;
                case TAKE_VIDEO_REQUEST:
                    // Take video
                    Intent takeVideoIntent = new Intent(MediaStore.ACTION_VIDEO_CAPTURE);
                    mMediaUri = getOutputMediaFileUri(MEDIA_TYPE_VIDEO);
                    if (mMediaUri == null) {
                        // Display an error
                        Toast.makeText(MainActivity.this, R.string.error_external_storage,
                                Toast.LENGTH_LONG).show();
                    }
                    else {
                        takeVideoIntent.putExtra(MediaStore.EXTRA_OUTPUT, mMediaUri);
                        takeVideoIntent.putExtra(MediaStore.EXTRA_DURATION_LIMIT, VIDEO_SECOND_LIMIT);
                        takeVideoIntent.putExtra(MediaStore.EXTRA_VIDEO_QUALITY, VIDEO_QUALITY);
                        startActivityForResult(takeVideoIntent, TAKE_VIDEO_REQUEST);
                    }
                    break;
            }
        }

        private Uri getOutputMediaFileUri(int mediaType) {
            // To be safe, check that the SDCard is mounted or not
            if (isExternalStorageAvailable()) {
                // Get the external storage directory
                String appName = MainActivity.this.getString(R.string.app_name);
                File mediaStorageDir = new File(
                        Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES),
                        appName);

                // Create our sub-directory
                if (!mediaStorageDir.exists()) {
                    if (!mediaStorageDir.mkdirs()) {
                        Log.e(TAG, "Failed to create directory.");
                        return null;
                    }
                }

                // Create the file
                File mediaFile;
                Date now = new Date();
                String timestamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(now);

                String path = mediaStorageDir.getPath() + File.separator;
                if (mediaType == MEDIA_TYPE_IMAGE) {
                    mediaFile = new File(path + "IMG_" + timestamp + ".jpg");
                }
                else if (mediaType == MEDIA_TYPE_VIDEO) {
                    mediaFile = new File(path + "VID_" + timestamp + ".mp4");
                }
                else {
                    return null;
                }

                Log.d(TAG, "File: " + Uri.fromFile(mediaFile));

                // Return the file's URI
                return Uri.fromFile(mediaFile);
            }
            else {
                return null;
            }
        }

        private boolean isExternalStorageAvailable() {
            int permissionCheck = ContextCompat.checkSelfPermission(MainActivity.this,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE);
            if (permissionCheck == PackageManager.PERMISSION_GRANTED) {
                String state = Environment.getExternalStorageState();
                return state.equals(Environment.MEDIA_MOUNTED);
            }
            else {
                ActivityCompat.requestPermissions(MainActivity.this,
                        new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                        Constants.CONST_WRITE_EXTERNAL_STORAGE);
            }
            return false;
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
            AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
            builder.setItems(R.array.camera_choices, mDialogListener);
            AlertDialog dialog = builder.create();
            dialog.show();
            }
        });

        // Adding a ListFragment to the view
        getSupportFragmentManager().beginTransaction()
                .add(R.id.mainContent, new MainListFragment()).commit();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == RESULT_OK) {
            Intent mediaScanIntent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
            mediaScanIntent.setData(mMediaUri);
            sendBroadcast(mediaScanIntent);

            uploadFile();
        }
        else if (resultCode != RESULT_CANCELED) {
            Toast.makeText(this, R.string.general_error, Toast.LENGTH_LONG).show();
        }
    }

    private void uploadFile() {
        // Initialize the Amazon Cognito credentials provider
        CognitoCachingCredentialsProvider credentialsProvider =
                new CognitoCachingCredentialsProvider(
                getApplicationContext(),
                Constants.POOL_ID, // Identity Pool ID
                Regions.US_EAST_1 // Region
        );

        AmazonS3 s3Client = new AmazonS3Client(credentialsProvider);
        TransferUtility transferUtility = new TransferUtility(s3Client, getApplicationContext());

        File upload = new File(mMediaUri.getPath());
        TransferObserver observer = transferUtility.upload(
                Constants.MY_BUCKET,
                upload.getName(),
                upload
        );

        observer.setTransferListener(new TransferListener() {
            @Override
            public void onStateChanged(int id, TransferState state) {

            }

            @Override
            public void onProgressChanged(int id, long bytesCurrent, long bytesTotal) {
                int percentage = (int) (bytesCurrent / bytesTotal * 100);
                Log.d(TAG, "Uploading: " + percentage + "%");
                if (percentage == 100) {
                    Toast.makeText(MainActivity.this, "Posted!", Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onError(int id, Exception ex) {

            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_main, menu);
        return true;
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.refresh:
                // Not implemented here
                return false;
            default:
                break;
        }

        return false;
    }
}
