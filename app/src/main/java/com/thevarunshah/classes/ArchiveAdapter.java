package com.thevarunshah.classes;

import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Paint;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.thevarunshah.simplebucketlist.R;

import java.util.ArrayList;

public class ArchiveAdapter extends ArrayAdapter<BucketItem> {

	private final static String TAG = "BucketAdapter"; //for debugging purposes

	private ArrayList<BucketItem> archiveList; //the list the adapter manages
	private Context context; //context attached to adapter

	public ArchiveAdapter(Context context, int textViewResourceId, ArrayList<BucketItem> archiveList) {
		
		super(context, textViewResourceId, archiveList);
		
		this.context = context;
		this.archiveList = archiveList;
	}

	private class ViewHolder {
		
		ImageButton delete;
		TextView goal;
	}

	@Override
	public View getView(final int position, View convertView, ViewGroup parent) {

		ViewHolder holder = null;

		if(convertView == null){
			
			LayoutInflater vi = (LayoutInflater) this.context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			convertView = vi.inflate(R.layout.archive_row, null);

			holder = new ViewHolder();
			holder.goal = (TextView) convertView.findViewById(R.id.row_text);
			holder.delete = (ImageButton) convertView.findViewById(R.id.delete_button);
			convertView.setTag(holder);
		} 
		else{
			
			holder = (ViewHolder) convertView.getTag();
		}
		
		final ViewHolder holderFinal = holder;
		
		//attach a press listener for toast message
		holder.delete.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {

				Log.i(TAG, "clicked on delete");

				final BucketItem item = (BucketItem) getItem(position); //get clicked item

				LayoutInflater layoutInflater = LayoutInflater.from(getContext());
				final View dialog = layoutInflater.inflate(R.layout.delete_dialog, null);
				final android.support.v7.app.AlertDialog.Builder alertDialog = new android.support.v7.app.AlertDialog.Builder(getContext(), R.style.AppCompatAlertDialogStyle);
				alertDialog.setTitle("Confirm Delete");
				alertDialog.setIcon(R.drawable.ic_warning_black_24dp);

				alertDialog.setView(dialog);

				final TextView message = (TextView) dialog.findViewById(R.id.delete_dialog);
				message.setText("Are you sure you want to delete this item?");

				alertDialog.setPositiveButton("DELETE", new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialogInterface, int whichButton) {

						Log.i(TAG, "deleting goal");

						//remove goal from adapter and update view
						archiveList.remove(item);
						notifyDataSetChanged();
					}
				});
				alertDialog.setNegativeButton("CANCEL", null);

				android.support.v7.app.AlertDialog alert = alertDialog.create();
				alert.show();
			}
		});

		BucketItem item = archiveList.get(position);
		holder.goal.setText(item.getGoal());
		holder.delete.setTag(item);

		return convertView;
	}
}
