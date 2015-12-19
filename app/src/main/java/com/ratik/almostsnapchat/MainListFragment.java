package com.ratik.almostsnapchat;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.ListFragment;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ProgressBar;

import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.model.S3ObjectSummary;
import com.ratik.almostsnapchat.adapter.CustomListAdapter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Created by Ratik on 16/12/15.
 */
public class MainListFragment extends ListFragment {

    private static final String TAG = MainListFragment.class.getSimpleName();
    private AmazonS3Client s3;
    private CustomListAdapter simpleAdapter;
    private ArrayList<HashMap<String, Object>> fileNames;

    private ProgressBar progressBar;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);

        fileNames = new ArrayList<>();
        simpleAdapter = new CustomListAdapter(getActivity(), fileNames, R.layout.snap_list_item,
                new String[]{ "key" }, new int[]{ R.id.key });

        setListAdapter(simpleAdapter);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        View view = inflater.inflate(R.layout.fragment_main, container, false);
        progressBar = (ProgressBar) view.findViewById(R.id.progressBar);
        return view;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        new GetFileListTask().execute();
    }

    @Override
    public void onResume() {
        super.onResume();

        // When an item is selected, finish the activity and pass back the S3
        // key associated with the object selected
        getListView().setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int pos, long id) {
                // Save clicked key
                Util.saveViewedFileName(getActivity(), (String) fileNames.get(pos).get("key"));

                int type = getFileType((String) fileNames.get(pos).get("key"));
                if (type == MainActivity.MEDIA_TYPE_IMAGE) {
                    // Display Image
                    Intent intent = new Intent(getActivity(), ViewImageActvity.class);
                    intent.putExtra("key", (String) fileNames.get(pos).get("key"));
                    startActivity(intent);
                } else {
                    // Display Video
                    Intent intent = new Intent(getActivity(), ViewVideoActivity.class);
                    intent.putExtra("key", (String) fileNames.get(pos).get("key"));
                    startActivity(intent);
                }
            }
        });

        fixKeys();
        simpleAdapter.notifyDataSetChanged();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.refresh:
                getListView().setVisibility(View.INVISIBLE);
                new GetFileListTask().execute();
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    private void fixKeys() {
        String[] blacklistedKeys = Util.getViewedFilenames(getActivity());
        if (blacklistedKeys != null) {
            for (int i = 0; i < fileNames.size(); i++) {
                for (String key : blacklistedKeys) {
                    if (key.equals(fileNames.get(i).get("key"))) {
                        fileNames.remove(i);
                    }
                }
            }
        }
    }

    private int getFileType(String key) {
        if (key.contains(".jpg")) {
            return MainActivity.MEDIA_TYPE_IMAGE;
        } else {
            return MainActivity.MEDIA_TYPE_VIDEO;
        }
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
            s3 = Util.getS3Client(getActivity());
            getListView().getEmptyView().setVisibility(View.INVISIBLE);
            progressBar.setVisibility(View.VISIBLE);
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
            progressBar.setVisibility(View.INVISIBLE);
            getListView().setVisibility(View.VISIBLE);
            // Filter blacklisted keys
            fixKeys();
            simpleAdapter.notifyDataSetChanged();
        }
    }
}
