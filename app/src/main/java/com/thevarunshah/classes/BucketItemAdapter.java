package com.thevarunshah.classes;

import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Paint;
import android.support.v7.app.AlertDialog;
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
import android.widget.TextView;

import com.thevarunshah.simplebucketlist.R;

import java.util.ArrayList;

public class BucketItemAdapter extends ArrayAdapter<Item> {

	private final static String TAG = "BucketItemAdapter"; //for debugging purposes

	private ArrayList<Item> bucketList; //the list the adapter manages
	private Context context; //context attached to adapter

	/**
	 * the bucket list adapter
	 *
	 * @param context the application context
	 * @param textViewResourceId the layout view for each row
	 * @param bucketList the list of items
	 */
	public BucketItemAdapter(Context context, int textViewResourceId, ArrayList<Item> bucketList) {

		super(context, textViewResourceId, bucketList);
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
			}
		});

		//attach a on-tap listener to the item for editing
		holder.item.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {

				TextView tv = (TextView) v;
				final Item item = getItem(position); //get clicked item

				//inflate layout with customized alert dialog view
				LayoutInflater layoutInflater = LayoutInflater.from(getContext());
				final View dialog = layoutInflater.inflate(R.layout.input_dialog, null);
				final AlertDialog.Builder editItemDialogBuilder = new AlertDialog.Builder(getContext(),
						R.style.AppCompatAlertDialogStyle);

				//customize alert dialog and set its view
				editItemDialogBuilder.setTitle("Edit Item");
				editItemDialogBuilder.setIcon(R.drawable.ic_edit_black_24dp);
				editItemDialogBuilder.setView(dialog);

				//fetch and set up edittext
				final EditText input = (EditText) dialog.findViewById(R.id.input_dialog_text);
				input.setText(tv.getText());
				input.setFocusableInTouchMode(true);
				input.requestFocus();

				//set up actions for dialog buttons
				editItemDialogBuilder.setPositiveButton("SAVE", new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialogInterface, int whichButton) {

						//update text of item and the view
						item.setItemText(input.getText().toString());
						notifyDataSetChanged();
					}
				});
				editItemDialogBuilder.setNegativeButton("CANCEL", null);

				//create and show the dialog
				AlertDialog editItemDialog = editItemDialogBuilder.create();
				editItemDialog.show();

				//show keyboard
				editItemDialog.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE);
			}
		});

		//attach a long-tap listener to the item
		holder.item.setOnLongClickListener(new View.OnLongClickListener() {
			@Override
			public boolean onLongClick(View v) {

				final Item item = getItem(position); //get clicked item

				//inflate layout with customized alert dialog view
				LayoutInflater layoutInflater = LayoutInflater.from(getContext());
				final View dialog = layoutInflater.inflate(R.layout.info_dialog, null);
				final AlertDialog.Builder deleteItemDialogBuilder = new AlertDialog.Builder(getContext(),
						R.style.AppCompatAlertDialogStyle);

				//customize alert dialog and set its view
				deleteItemDialogBuilder.setTitle("Confirm Delete");
				deleteItemDialogBuilder.setIcon(R.drawable.ic_delete_black_24dp);
				deleteItemDialogBuilder.setView(dialog);

				//fetch textview and set its text
				final TextView message = (TextView) dialog.findViewById(R.id.info_dialog);
				message.setText("Are you sure you want to delete this item?");

				//set up actions for dialog buttons
				deleteItemDialogBuilder.setPositiveButton("DELETE", new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialogInterface, int whichButton) {

						//remove item from adapter and update view
						bucketList.remove(item);
						notifyDataSetChanged();
					}
				});
				deleteItemDialogBuilder.setNegativeButton("CANCEL", null);

				//create and show the dialog
				AlertDialog deleteItemDialog = deleteItemDialogBuilder.create();
				deleteItemDialog.show();

				return true;
			}
		});

		//get item and link references to holder
		Item item = bucketList.get(position);
		holder.item.setText(item.getItemText());
		holder.done.setChecked(item.isDone());
		holder.done.setTag(item);

		return convertView;
	}
}
