package com.thevarunshah.simplebucketlist.internal;

import android.content.Context;
import android.graphics.Color;
import android.support.design.widget.Snackbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.TextView;

import com.thevarunshah.simplebucketlist.R;
import com.thevarunshah.classes.Item;

import java.util.ArrayList;

public class ArchivedItemAdapter extends ArrayAdapter<Item> {

	private final static String TAG = "ArchivedItemAdapter"; //for debugging purposes

	private final ArrayList<Item> archiveList; //the list the adapter manages
	private final Context context; //context attached to adapter

	/**
	 * the archive list adapter
	 *  @param context the application context
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

	@Override
	public View getView(final int position, View convertView, ViewGroup parent) {

		ViewHolder holder = new ViewHolder();

		if(convertView == null){
			//inflate view and link each component to the holder
			LayoutInflater vi = (LayoutInflater) this.context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			convertView = vi.inflate(R.layout.archived_row, null);
			holder.item = (TextView) convertView.findViewById(R.id.row_text);
			holder.unarchive = (ImageButton) convertView.findViewById(R.id.unarchive_button);
			holder.delete = (ImageButton) convertView.findViewById(R.id.delete_button);
			convertView.setTag(holder);
		} 
		else{
			holder = (ViewHolder) convertView.getTag();
		}

		//attach a on-tap listener to the item for unarchiving
		holder.unarchive.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {

				final Item item = getItem(position); //get clicked item

				//remove item from adapter, add to bucket list and update view
				Utility.getBucketList().add(item);
				archiveList.remove(item);
				notifyDataSetChanged();
				Utility.writeData(getContext()); //backup data

				//display success message and give option to undo
				Snackbar infoBar = Snackbar.make(v, "Item unarchived.", Snackbar.LENGTH_LONG);
				infoBar.setAction("UNDO", new OnClickListener() {
					@Override
					public void onClick(View v) {

						//undo unarchiving
						Utility.getBucketList().remove(item);
						archiveList.add(position, item);
						notifyDataSetChanged();
						Utility.writeData(getContext()); //backup data
					}
				});
				infoBar.setActionTextColor(Color.WHITE);
				infoBar.show();
			}
		});
		
		//attach a on-tap listener to the item for deleting
		holder.delete.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {

				final Item item = getItem(position); //get clicked item

				//remove item from adapter and update view
				archiveList.remove(item);
				notifyDataSetChanged();
				Utility.writeData(getContext()); //backup data

				//display success message and give option to undo
				Snackbar infoBar = Snackbar.make(v, "Item deleted.", Snackbar.LENGTH_LONG);
				infoBar.setAction("UNDO", new OnClickListener() {
					@Override
					public void onClick(View v) {

						//undo deleting
						archiveList.add(position, item);
						notifyDataSetChanged();
						Utility.writeData(getContext()); //backup data
					}
				});
				infoBar.setActionTextColor(Color.WHITE);
				infoBar.show();
			}
		});

		//get item and link references to holder
		Item item = archiveList.get(position);
		holder.item.setText(item.getItemText());
		holder.delete.setTag(item);

		return convertView;
	}
}
