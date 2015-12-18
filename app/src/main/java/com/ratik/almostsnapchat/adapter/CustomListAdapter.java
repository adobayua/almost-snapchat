package com.ratik.almostsnapchat.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Filterable;
import android.widget.ImageView;
import android.widget.SimpleAdapter;
import android.widget.TextView;

import com.ratik.almostsnapchat.R;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by Ratik on 19/12/15.
 */
public class CustomListAdapter extends SimpleAdapter implements Filterable {

    private static final String TAG = CustomListAdapter.class.getSimpleName();
    private LayoutInflater inflater;
    private Context context;

    private ArrayList<HashMap<String, Object>> fileNames;

    public CustomListAdapter(Context context, ArrayList<HashMap<String, Object>> data,
                             int resource, String[] from, int[] to) {
        super(context, data, resource, from, to);
        this.context = context;
        this.fileNames = data;

        inflater = LayoutInflater.from(this.context);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder;

        if (convertView == null) {
            convertView = inflater.inflate(R.layout.snap_list_item, parent, false);
            holder = new ViewHolder();
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        // Storing key
        String text = (String) fileNames.get(position).get("key");

        holder.textView = (TextView) convertView.findViewById(R.id.key);
        holder.imageView = (ImageView) convertView.findViewById(R.id.snapTypeView);

        if(text.contains(".jpg")) {
            // Image
            holder.textView.setText("Image Message");
            holder.imageView.setImageResource(R.drawable.ic_image);
        } else {
            // Video
            holder.textView.setText("Video Message");
            holder.imageView.setImageResource(R.drawable.ic_video);
        }

        return convertView;
    }


    private class ViewHolder {
        TextView textView;
        ImageView imageView;
    }
}
