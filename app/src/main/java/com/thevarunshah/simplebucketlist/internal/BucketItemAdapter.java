package com.thevarunshah.simplebucketlist.internal;

import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Color;
import android.graphics.Paint;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;

import com.thevarunshah.classes.Item;
import com.thevarunshah.simplebucketlist.BucketItemListView;
import com.thevarunshah.simplebucketlist.R;

import java.util.ArrayList;

public class BucketItemAdapter extends ArrayAdapter<Item> {

	private final static String TAG = "BucketItemAdapter"; //for debugging purposes

	private final ArrayList<Item> bucketList; //the list the adapter manages
	private final Context context; //context attached to adapter
	private final boolean tablet;

	/**
	 * the bucket list adapter
	 *  @param context the application context
	 * @param bucketList the list of items
	 */
	public BucketItemAdapter(Context context, ArrayList<Item> bucketList, boolean tablet) {

		super(context, R.layout.row, bucketList);
		this.context = context;
		this.bucketList = bucketList;
		this.tablet = tablet;
	}

	/**
	 * a view holder for each item in the row
	 */
	private class ViewHolder {

		CheckBox done;
		TextView item;
		ImageButton edit;
		ImageButton archive;
		ImageButton delete;
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
			if(tablet){
				holder.edit = (ImageButton) convertView.findViewById(R.id.context_edit);
				holder.archive = (ImageButton) convertView.findViewById(R.id.context_archive);
				holder.delete = (ImageButton) convertView.findViewById(R.id.context_delete);
			}
			convertView.setTag(holder);
		}
		else{
			holder = (ViewHolder) convertView.getTag();
		}

		final Item item = getItem(position);

		final ViewHolder holderFinal = holder;
		//attach a check listener to the checkbox
		holder.done.setOnCheckedChangeListener(new OnCheckedChangeListener() {
			@Override
			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {

				//get item and set as done/undone
				item.setDone(isChecked);

				//apply or get rid of strikethrough effect
				if (isChecked) {
					holderFinal.item.setPaintFlags(holderFinal.item.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
				} else {
					holderFinal.item.setPaintFlags(holderFinal.item.getPaintFlags() & ~Paint.STRIKE_THRU_TEXT_FLAG);
				}

				Utility.writeData(getContext()); //backup data
			}
		});

		//tablet view listeners
		if(tablet) {

			//on click for the item text
			holder.item.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View view) {

					holderFinal.done.setChecked(!item.isDone());
				}
			});

			//on click for edit
			holder.edit.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View view) {

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
					input.setText(item.getItemText());
					input.setFocusableInTouchMode(true);
					input.requestFocus();

					//set up actions for dialog buttons
					editItemDialogBuilder.setPositiveButton("SAVE", new DialogInterface.OnClickListener() {
						@Override
						public void onClick(DialogInterface dialogInterface, int whichButton) {

							//update text of item and the view
							item.setItemText(input.getText().toString());

							Utility.writeData(getContext()); //backup data
						}
					});
					editItemDialogBuilder.setNegativeButton("CANCEL", new DialogInterface.OnClickListener() {
						@Override
						public void onClick(DialogInterface dialog, int which) {

						}
					});

					//create and show the dialog
					AlertDialog editItemDialog = editItemDialogBuilder.create();
					editItemDialog.show();

					//show keyboard
					editItemDialog.getWindow()
							.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE);
				}
			});

			//on click for item archive
			holder.archive.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View view) {

					//move item to the archive list and update the view
					Utility.moveToArchive(position);
					notifyDataSetChanged();
					Utility.writeData(getContext()); //backup data

					//display success message
					Snackbar infoBar = Snackbar.make(view, "Item archived.", Snackbar.LENGTH_SHORT);
					infoBar.show();
				}
			});

			//on click for item delete
			holder.delete.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View view) {

					//remove item from adapter and update view
					bucketList.remove(position);
					notifyDataSetChanged();
					Utility.writeData(getContext()); //backup data

					//display success message and give option to undo
					Snackbar infoBar = Snackbar.make(view, "Item deleted.", Snackbar.LENGTH_LONG);
					infoBar.setAction("UNDO", new View.OnClickListener() {
						@Override
						public void onClick(View v) {

							//undo deleting
							bucketList.add(position, item);
							notifyDataSetChanged();
							Utility.writeData(getContext()); //backup data
						}
					});
					infoBar.setActionTextColor(Color.WHITE);
					infoBar.show();
				}
			});
		}

		//get item and link references to holder
		Item itemHolder = bucketList.get(position);
		holder.item.setText(itemHolder.getItemText());
		holder.done.setChecked(itemHolder.isDone());
		holder.done.setTag(itemHolder);

		return convertView;
	}

	public ArrayList<Item> getBucketList() {
		return bucketList;
	}
}
