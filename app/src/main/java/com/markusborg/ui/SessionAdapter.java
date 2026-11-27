package com.markusborg.ui;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.core.content.ContextCompat;

import java.util.ArrayList;

import com.markusborg.logic.Setting;

/**
 * Created by Markus Borg on 2015-11-22.
 */
public class SessionAdapter extends ArrayAdapter<Setting> {

    private ArrayList<Setting> objects;

    public SessionAdapter(Context context, int textViewResourceId, ArrayList<Setting> objects) {
        super(context, textViewResourceId, objects);
        this.objects = objects;
    }

    /**
     * Describes how an individual Session is presented in the list view.
     * @param position
     * @param convertView
     * @param parent
     * @return
     */
    public View getView(int position, View convertView, ViewGroup parent){
        View v = convertView;

        if (v == null) {
            LayoutInflater inflater = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            v = inflater.inflate(R.layout.list_item, null);
        }

		// the variable position is sent in as an argument to this method.
		// The variable simply refers to the position of the current object in the list,
		// the ArrayAdapter iterates through the list we sent it.
        Setting i = objects.get(position);

        if (i != null) {
            ImageView iv = (ImageView) v.findViewById(R.id.sessionImageView);
            TextView tv = (TextView) v.findViewById(R.id.sessionTextView);

            // Display the corresponding ball icon
            Drawable ballIcon = ContextCompat.getDrawable(getContext(),
                    i.isSquash() ? R.drawable.squashball : R.drawable.shuttlecock);
            iv.setImageDrawable(ballIcon);
            tv.setText(i.getRestrictedString());
        }

        return v;
    }
}
