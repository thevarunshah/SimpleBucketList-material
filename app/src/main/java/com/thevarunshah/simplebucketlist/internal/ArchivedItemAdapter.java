package com.thevarunshah.simplebucketlist.internal;

import android.content.Context;
import android.graphics.Color;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.TextView;

import com.thevarunshah.classes.Item;
import com.thevarunshah.simplebucketlist.R;

import java.util.ArrayList;

public class ArchivedItemAdapter extends ArrayAdapter<Item> {

	private final static String TAG = "ArchivedItemAdapter"; //for debugging purposes

	private final ArrayList<Item> archiveList; //the list the adapter manages
	private final Context context; //context attached to adapter

	/**
	 * the archive list adapter
	 * @param context the application context
	 * @param archiveList the list of items
	 */
	public ArchivedItemAdapter(Context context, ArrayList<Item> archiveList) {
		super(context, R.layout.archived_row, archiveList);
		this.context = context;
		this.archiveList = archiveList;
	}

	/**
	 * a view holder for each item in the row
	 */
	private class ViewHolder {
		ImageButton unarchive;
		ImageButton delete;
		TextView item;
	}

	@NonNull
	@Override
	public View getView(final int position, View convertView, @NonNull ViewGroup parent) {

		ViewHolder holder = new ViewHolder();
		if(convertView == null){
			//inflate view and link each component to the holder
			LayoutInflater vi = (LayoutInflater) this.context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			if (vi != null) {
				convertView = vi.inflate(R.layout.archived_row, parent, false);
				holder.item = convertView.findViewById(R.id.row_text);
				holder.unarchive = convertView.findViewById(R.id.unarchive_button);
				holder.delete = convertView.findViewById(R.id.delete_button);
				convertView.setTag(holder);
			} else {
				return null;
			}
		} else {
			holder = (ViewHolder) convertView.getTag();
		}

		//attach a on-tap listener to the item for unarchiving
		holder.unarchive.setOnClickListener(v -> {

            final Item item = getItem(position); //get clicked item

            //remove item from adapter, add to bucket list and update view
            Utility.getBucketList().add(item);
            archiveList.remove(item);
            notifyDataSetChanged();
            Utility.writeData(getContext()); //backup data

            //display success message and give option to undo
            Snackbar infoBar = Snackbar.make(v, R.string.item_unarchived, Snackbar.LENGTH_LONG);
            infoBar.setAction("UNDO", v12 -> {
				//undo unarchiving
				Utility.getBucketList().remove(item);
				archiveList.add(position, item);
				notifyDataSetChanged();
				Utility.writeData(getContext()); //backup data
			});
            infoBar.setActionTextColor(Color.WHITE);
            infoBar.show();
        });
		
		//attach a on-tap listener to the item for deleting
		holder.delete.setOnClickListener(v -> {

            final Item item = getItem(position); //get clicked item

            //remove item from adapter and update view
            archiveList.remove(item);
            notifyDataSetChanged();
            Utility.writeData(getContext()); //backup data

            //display success message and give option to undo
            Snackbar infoBar = Snackbar.make(v, R.string.item_deleted, Snackbar.LENGTH_LONG);
            infoBar.setAction("UNDO", v1 -> {
				//undo deleting
				archiveList.add(position, item);
				notifyDataSetChanged();
				Utility.writeData(getContext()); //backup data
			});
            infoBar.setActionTextColor(Color.WHITE);
            infoBar.show();
        });

		//get item and link references to holder
		Item item = archiveList.get(position);
		holder.item.setText(item.getItemText());
		holder.delete.setTag(item);

		return convertView;
	}
}
