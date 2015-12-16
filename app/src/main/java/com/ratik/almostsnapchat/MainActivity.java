package com.ratik.almostsnapchat;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {

    // Constants
    public static final int TAKE_PHOTO_REQUEST = 0;
    public static final int PICK_PHOTO_REQUEST = 1;
    public static final int TAKE_VIDEO_REQUEST = 2;
    public static final int PICK_VIDEO_REQUEST = 3;

    public static final int MEDIA_TYPE_IMAGE = 4;
    public static final int MEDIA_TYPE_VIDEO = 5;

    public static final int FILE_SIZE_LIMIT = 1024 * 1024 * 10; // 10 MB

    private static final String TAG = MainActivity.class.getSimpleName();

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

                case PICK_PHOTO_REQUEST:
                    // Choose picture
                    Intent choosePhotoIntent = new Intent(Intent.ACTION_GET_CONTENT);
                    choosePhotoIntent.setType("image/*");
                    startActivityForResult(choosePhotoIntent, PICK_PHOTO_REQUEST);
                    break;
                case TAKE_VIDEO_REQUEST:
                    // Take video
                    break;
                case PICK_VIDEO_REQUEST:
                    // Choose video
                    Intent chooseVideoIntent = new Intent(Intent.ACTION_GET_CONTENT);
                    chooseVideoIntent.setType("video/*");
                    startActivityForResult(chooseVideoIntent, PICK_VIDEO_REQUEST);
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
            String state = Environment.getExternalStorageState();
            return state.equals(Environment.MEDIA_MOUNTED);
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
            if (requestCode == PICK_PHOTO_REQUEST || requestCode == PICK_VIDEO_REQUEST) {
                if (data == null) {
                    Toast.makeText(this, getString(R.string.general_error), Toast.LENGTH_LONG).show();
                }
                else {
                    mMediaUri = data.getData();
                }

                Log.i(TAG, "Media URI: " + mMediaUri);
                if (requestCode == PICK_VIDEO_REQUEST) {
                    // Make sure the file is less than 10 MB
                    int fileSize = 0;
                    InputStream inputStream = null;

                    try {
                        inputStream = getContentResolver().openInputStream(mMediaUri);
                        fileSize = inputStream.available();
                    }
                    catch (FileNotFoundException e) {
                        Toast.makeText(this, R.string.error_opening_file, Toast.LENGTH_LONG).show();
                        return;
                    }
                    catch (IOException e) {
                        Toast.makeText(this, R.string.error_opening_file, Toast.LENGTH_LONG).show();
                        return;
                    }
                    finally {
                        try {
                            inputStream.close();
                        } catch (IOException e) {
                            // Nothing here
                        }
                    }

                    if (fileSize >= FILE_SIZE_LIMIT) {
                        Toast.makeText(this, R.string.file_too_large_error, Toast.LENGTH_LONG).show();
                        return;
                    }
                }
            }
            else {
                // User wants to use the camera
                Intent mediaScanIntent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
                mediaScanIntent.setData(mMediaUri);
                sendBroadcast(mediaScanIntent);
            }

//            Intent recipientsIntent = new Intent(this, RecipientsActivity.class);
//            recipientsIntent.setData(mMediaUri);
//
//            String fileType;
//            if (requestCode == PICK_PHOTO_REQUEST || requestCode == TAKE_PHOTO_REQUEST) {
//                fileType = ParseConstants.TYPE_IMAGE;
//            }
//            else {
//                fileType = ParseConstants.TYPE_VIDEO;
//            }
//
//            recipientsIntent.putExtra(ParseConstants.KEY_FILE_TYPE, fileType);
//            startActivity(recipientsIntent);
        }
        else if (resultCode != RESULT_CANCELED) {
            Toast.makeText(this, R.string.general_error, Toast.LENGTH_LONG).show();
        }
    }
}
