package com.thevarunshah.classes;

import java.util.ArrayList;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Paint;
import android.text.InputType;
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

import com.thevarunshah.simplebucketlist.BucketListView;
import com.thevarunshah.simplebucketlist.R;

public class BucketAdapter extends ArrayAdapter<BucketItem> {
	
	private final static String TAG = "BucketAdapter"; //for debugging purposes

	private ArrayList<BucketItem> bucketList; //the list the adapter manages
	private Context context; //context attached to adapter

	public BucketAdapter(Context context, int textViewResourceId, ArrayList<BucketItem> bucketList) {
		
		super(context, textViewResourceId, bucketList);
		
		this.context = context;
		this.bucketList = bucketList;
	}

	private class ViewHolder {
		
		CheckBox done;
		TextView goal;
	}

	@Override
	public View getView(final int position, View convertView, ViewGroup parent) {

		ViewHolder holder = null;

		if(convertView == null){
			
			LayoutInflater vi = (LayoutInflater) this.context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			convertView = vi.inflate(R.layout.row, null);

			holder = new ViewHolder();
			holder.goal = (TextView) convertView.findViewById(R.id.row_text);
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

				BucketItem item = (BucketItem) getItem(position); //get checked item
				item.setDone(isChecked); //set as done/undone

				if (isChecked) {
					holderFinal.goal.setPaintFlags(holderFinal.goal.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG); //apply strikethrough effect
				} else {
					holderFinal.goal.setPaintFlags(holderFinal.goal.getPaintFlags() & ~Paint.STRIKE_THRU_TEXT_FLAG); //get rid of strikethrough effect
				}
			}
		});
		
		//attach a press listener for toast message
		holder.done.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				
				CheckBox cb = (CheckBox) v;
				if(cb.isChecked()){
					Toast.makeText(v.getContext(), "Good Job!", Toast.LENGTH_SHORT).show(); //tell them good job
				}
			}
		});
		
		//attach a press listener to goal for editing
		holder.goal.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {

				TextView tv = (TextView) v;
				Log.i(TAG, "clicked on " + tv.getText());

				final BucketItem item = (BucketItem) getItem(position); //get clicked item

				LayoutInflater layoutInflater = LayoutInflater.from(getContext());
				final View dialog = layoutInflater.inflate(R.layout.input_dialog, null);
				final android.support.v7.app.AlertDialog.Builder alertDialog = new android.support.v7.app.AlertDialog.Builder(getContext(), R.style.AppCompatAlertDialogStyle);
				alertDialog.setTitle("Edit Item");
				alertDialog.setIcon(R.drawable.ic_edit_black_24dp);

				alertDialog.setView(dialog);

				//set up edit text object
				final EditText input = (EditText) dialog.findViewById(R.id.input_dialog_text);
				input.setText(tv.getText());
				input.setFocusableInTouchMode(true);
				input.requestFocus();

				alertDialog.setPositiveButton("SAVE", new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialogInterface, int whichButton) {

						Log.i(TAG, "saving edited goal");

						BucketItem updatedItem = new BucketItem(input.getText().toString());
						updatedItem.setDone(item.isDone());
						bucketList.remove(item);
						bucketList.add(position, updatedItem);
						notifyDataSetChanged();
					}
				});
				alertDialog.setNegativeButton("CANCEL", null);

				android.support.v7.app.AlertDialog alert = alertDialog.create();
				alert.show();

				alert.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE);
			}
		});

		//attach a long press listener to the item
		holder.goal.setOnLongClickListener(new View.OnLongClickListener() {
			@Override
			public boolean onLongClick(View v) {

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
						bucketList.remove(item);
						notifyDataSetChanged();
					}
				});
				alertDialog.setNegativeButton("CANCEL", null);

				android.support.v7.app.AlertDialog alert = alertDialog.create();
				alert.show();

				return true;
			}
		});

		BucketItem item = bucketList.get(position);
		holder.goal.setText(item.getGoal());
		holder.done.setChecked(item.isDone());
		holder.done.setTag(item);

		return convertView;
	}
}
