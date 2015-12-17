package com.ratik.almostsnapchat;

import android.os.Bundle;
import android.support.v4.app.ListFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

/**
 * Created by Ratik on 16/12/15.
 */
public class MainListFragment extends ListFragment {

    public MainListFragment() {
        // Nothing here
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        View view = inflater.inflate(R.layout.fragment_main, container, false);
        return view;
    }


    @Override
    public void onResume() {
        super.onResume();
    }
}
