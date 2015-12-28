package com.thevarunshah.classes;

import android.content.Context;
import android.content.DialogInterface;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.TextView;

import com.thevarunshah.simplebucketlist.R;

import java.util.ArrayList;

public class ArchivedItemAdapter extends ArrayAdapter<Item> {

	private final static String TAG = "ArchivedItemAdapter"; //for debugging purposes

	private ArrayList<Item> archiveList; //the list the adapter manages
	private Context context; //context attached to adapter

	/**
	 * the archive list adapter
	 *
	 * @param context the application context
	 * @param textViewResourceId the layout view for each row
	 * @param archiveList the list of items
	 */
	public ArchivedItemAdapter(Context context, int textViewResourceId, ArrayList<Item> archiveList) {
		
		super(context, textViewResourceId, archiveList);
		this.context = context;
		this.archiveList = archiveList;
	}

	/**
	 * a view holder for each item in the row
	 */
	private class ViewHolder {
		
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
			holder.delete = (ImageButton) convertView.findViewById(R.id.delete_button);
			convertView.setTag(holder);
		} 
		else{
			holder = (ViewHolder) convertView.getTag();
		}
		
		//attach a on-tap listener to the item for deleting
		holder.delete.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {

				final Item item = getItem(position); //get clicked item

				//inflate layout with customized alert dialog view
				LayoutInflater layoutInflater = LayoutInflater.from(getContext());
				final View dialog = layoutInflater.inflate(R.layout.delete_dialog, null);
				final AlertDialog.Builder deleteItemDialogBuilder = new AlertDialog.Builder(getContext(),
						R.style.AppCompatAlertDialogStyle);

				//customize alert dialog and set its view
				deleteItemDialogBuilder.setTitle("Confirm Delete");
				deleteItemDialogBuilder.setIcon(R.drawable.ic_warning_black_24dp);
				deleteItemDialogBuilder.setView(dialog);

				//fetch textview and set its text
				final TextView message = (TextView) dialog.findViewById(R.id.delete_dialog);
				message.setText("Are you sure you want to delete this item?");

				deleteItemDialogBuilder.setPositiveButton("DELETE", new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialogInterface, int whichButton) {

						//remove item from adapter and update view
						archiveList.remove(item);
						notifyDataSetChanged();
					}
				});
				deleteItemDialogBuilder.setNegativeButton("CANCEL", null);

				//create and show the dialog
				AlertDialog deleteItemDialog = deleteItemDialogBuilder.create();
				deleteItemDialog.show();
			}
		});

		//get item and link references to holder
		Item item = archiveList.get(position);
		holder.item.setText(item.getItemText());
		holder.delete.setTag(item);

		return convertView;
	}
}
