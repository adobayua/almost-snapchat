package com.ratik.almostsnapchat;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.ListFragment;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.Toast;

import com.amazonaws.auth.CognitoCachingCredentialsProvider;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.model.S3ObjectSummary;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Created by Ratik on 16/12/15.
 */
public class MainListFragment extends ListFragment {

    // The S3 client used for getting the list of objects in the bucket
    private AmazonS3Client s3;

    private SimpleAdapter simpleAdapter;

    // Hashmap
    private ArrayList<HashMap<String, Object>> fileNames;

    private int mPos;

    public MainListFragment() {
        // Nothing here
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);

//        if (savedInstanceState != null) {
//            mPos = savedInstanceState.getInt("pos");
//            fileNames.remove(mPos);
//            simpleAdapter.notifyDataSetChanged();
//        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        View view = inflater.inflate(R.layout.fragment_main, container, false);
        return view;
    }


    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        // Implemented in the parent activity
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.refresh:
                // Refresh the file list.
                new GetFileListTask().execute();
                Toast.makeText(getActivity(), "Refreshing...", Toast.LENGTH_LONG).show();
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onResume() {
        super.onResume();

        fileNames = new ArrayList<>();

        simpleAdapter = new SimpleAdapter(getActivity(), fileNames,
                R.layout.bucket_item, new String[]{
                "key"
        },
                new int[]{
                        R.id.key
                });
        simpleAdapter.setViewBinder(new SimpleAdapter.ViewBinder() {
            @Override
            public boolean setViewValue(View view, Object data,
                                        String textRepresentation) {
                switch (view.getId()) {
                    case R.id.key:
                        TextView fileName = (TextView) view;
                        fileName.setText((String) data);
                        return true;
                }
                return false;
            }
        });
        setListAdapter(simpleAdapter);

        // When an item is selected, finish the activity and pass back the S3
        // key associated with the object selected
        getListView().setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int pos, long id) {
                mPos = pos;
                Intent intent = new Intent(getActivity(), ShowSnapActivity.class);
                intent.putExtra("key", (String) fileNames.get(pos).get("key"));



                startActivity(intent);
            }
        });

        new GetFileListTask().execute();
    }

    @Override
    public void onPause() {
        super.onPause();
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt("pos", mPos);
    }

    /**
     * This async task queries S3 for all files in the given bucket so that they
     * can be displayed on the screen
     */
    private class GetFileListTask extends AsyncTask<Void, Void, Void> {
        // The list of objects we find in the S3 bucket
        private List<S3ObjectSummary> s3ObjList;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();

            // Initialize the Amazon Cognito credentials provider
            CognitoCachingCredentialsProvider credentialsProvider =
                    new CognitoCachingCredentialsProvider(
                            getActivity().getApplicationContext(),
                            Constants.POOL_ID, // Identity Pool ID
                            Regions.US_EAST_1 // Region
                    );
            s3 = new AmazonS3Client(credentialsProvider);
        }

        @Override
        protected Void doInBackground(Void... inputs) {
            // Queries files in the bucket from S3.
            s3ObjList = s3.listObjects(Constants.MY_BUCKET).getObjectSummaries();
            fileNames.clear();
            for (S3ObjectSummary summary : s3ObjList) {
                HashMap<String, Object> map = new HashMap<String, Object>();
                map.put("key", summary.getKey());
                fileNames.add(map);
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
            simpleAdapter.notifyDataSetChanged();
        }
    }
}
