package com.thevarunshah.classes;

import android.content.Context;
import android.graphics.Paint;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.TextView;

import com.thevarunshah.simplebucketlist.R;

import java.util.ArrayList;

public class BucketItemAdapter extends ArrayAdapter<Item> {

	private final static String TAG = "BucketItemAdapter"; //for debugging purposes

	private final ArrayList<Item> bucketList; //the list the adapter manages
	private final Context context; //context attached to adapter

	/**
	 * the bucket list adapter
	 *  @param context the application context
	 * @param bucketList the list of items
	 */
	public BucketItemAdapter(Context context, ArrayList<Item> bucketList) {

		super(context, R.layout.row, bucketList);
		this.context = context;
		this.bucketList = bucketList;
	}

	/**
	 * a view holder for each item in the row
	 */
	private class ViewHolder {

		CheckBox done;
		TextView item;
	}

	@Override
	public View getView(final int position, View convertView, ViewGroup parent) {

		ViewHolder holder = new ViewHolder();

		if(convertView == null){
			//inflate view and link each component to the holder
			LayoutInflater vi = (LayoutInflater) this.context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			convertView = vi.inflate(R.layout.row, null);
			holder.item = (TextView) convertView.findViewById(R.id.row_text);
			holder.done = (CheckBox) convertView.findViewById(R.id.row_check);
			convertView.setTag(holder);
		}
		else{
			holder = (ViewHolder) convertView.getTag();
		}

		final ViewHolder holderFinal = holder;
		//attach a check listener to the checkbox
		holder.done.setOnCheckedChangeListener(new OnCheckedChangeListener() {
			@Override
			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {

				//get item and set as done/undone
				Item item = getItem(position);
				item.setDone(isChecked);

				//apply or get rid of strikethrough effect
				if (isChecked) {
					holderFinal.item.setPaintFlags(holderFinal.item.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
				} else {
					holderFinal.item.setPaintFlags(holderFinal.item.getPaintFlags() & ~Paint.STRIKE_THRU_TEXT_FLAG);
				}

				Backend.writeData(getContext()); //backup data
			}
		});

		//get item and link references to holder
		Item item = bucketList.get(position);
		holder.item.setText(item.getItemText());
		holder.done.setChecked(item.isDone());
		holder.done.setTag(item);

		return convertView;
	}

	public ArrayList<Item> getBucketList() {
		return bucketList;
	}
}
