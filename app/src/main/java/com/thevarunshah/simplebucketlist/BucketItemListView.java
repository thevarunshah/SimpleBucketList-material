package com.thevarunshah.simplebucketlist;

import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.WindowManager;
import android.widget.EditText;

import com.thevarunshah.classes.Item;
import com.thevarunshah.simplebucketlist.internal.BucketItemListAdapter;
import com.thevarunshah.simplebucketlist.internal.OnStartDragListener;
import com.thevarunshah.simplebucketlist.internal.SimpleItemTouchHelperCallback;
import com.thevarunshah.simplebucketlist.internal.Utility;

import java.util.ArrayList;


public class BucketItemListView extends AppCompatActivity implements OnStartDragListener {

	private static final String TAG = "BucketItemListView"; //for debugging purposes

	private RecyclerView recyclerView = null; //main view of items
	private BucketItemListAdapter recyclerAdapter = null; //adapter for items display

	private ItemTouchHelper itemTouchHelper;

	public static boolean itemsMoved = false;

	@Override
	protected void onCreate(Bundle savedInstanceState) {

		super.onCreate(savedInstanceState);
		setContentView(R.layout.bucket_item_listview);

		//fetch toolbar and set it as the action bar
		Toolbar toolbar = findViewById(R.id.toolbar);
		toolbar.setContentInsetsRelative(72, 72);
		setSupportActionBar(toolbar);

		//check if tablet view is being used
		boolean tablet = (findViewById(R.id.coordLayout_tablet) != null);

		//obtain list view and create new bucket list custom adapter
		recyclerView = findViewById(R.id.recycler_view);
		recyclerAdapter = new BucketItemListAdapter(this, Utility.getBucketList(), tablet, this);
		recyclerView.setAdapter(recyclerAdapter); //attach adapter to list view
		recyclerView.setLayoutManager(new LinearLayoutManager(this));

		ItemTouchHelper.Callback callback = new SimpleItemTouchHelperCallback(recyclerAdapter);
		itemTouchHelper = new ItemTouchHelper(callback);
		itemTouchHelper.attachToRecyclerView(recyclerView);

		//obtain add button and attach a on-tap listener to it
		final FloatingActionButton addButton = findViewById(R.id.add_item);
		addButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {

				//inflate layout with customized alert dialog view
				LayoutInflater layoutInflater = LayoutInflater.from(BucketItemListView.this);
				final View dialog = layoutInflater.inflate(R.layout.input_dialog, null);
				final AlertDialog.Builder newItemDialogBuilder = new AlertDialog.Builder(BucketItemListView.this,
						R.style.AppCompatAlertDialogStyle);

				//customize alert dialog and set its view
				newItemDialogBuilder.setTitle("New Item");
				newItemDialogBuilder.setIcon(R.drawable.ic_launcher);
				newItemDialogBuilder.setView(dialog);

				//fetch and set up edittext
				final EditText input = dialog.findViewById(R.id.input_dialog_text);
				input.setHint("Enter Details");
				input.setFocusableInTouchMode(true);
				input.requestFocus();

				//set up actions for dialog buttons
				newItemDialogBuilder.setPositiveButton("ADD", new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialogInterface, int whichButton) {

						//create new item
						String itemText = input.getText().toString();
						Item item = new Item(itemText);

						//add item to main list and update view
						Utility.getBucketList().add(item);
						recyclerAdapter.notifyDataSetChanged();

						Utility.writeData(getApplicationContext()); //backup data
					}
				});
				newItemDialogBuilder.setNegativeButton("CANCEL", null);

				//create and show the dialog
				AlertDialog newItemDialog = newItemDialogBuilder.create();
				newItemDialog.show();

				//show keyboard
				newItemDialog.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE);
			}
		});

		//add specific listeners only if tablet is not being used
		if(!tablet){

			//moving fab out of the way when scrolling listview
			recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {

				@Override
				public void onScrolled(RecyclerView recyclerView, int dx, int dy){
					if (dy > 0) {
						addButton.animate().translationY(addButton.getHeight()*2); //scrolling down
					}
					else {
						addButton.animate().translationY(0); //scrolling up
					}
				}
			});
		}
	}

	@Override
	public void onStartDrag(RecyclerView.ViewHolder viewHolder) {
		itemTouchHelper.startDrag(viewHolder);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {

		//fetch and set actionbar menu
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.main_menu, menu);
		return super.onCreateOptionsMenu(menu);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {

		switch (item.getItemId()) {
			case R.id.archive_completed:
				//transfer completed items to archive
				final ArrayList<Integer> removedIndices = Utility.transferCompletedToArchive();
				if(removedIndices.size() == 0){
					Snackbar infoBar = Snackbar.make(findViewById(R.id.coordLayout), "No items to archive.",
							Snackbar.LENGTH_SHORT);
					infoBar.show();
					return true;
				}
				recyclerAdapter.notifyDataSetChanged();
				Utility.writeData(this.getApplicationContext()); //backup data

				//friendly success message and give option to undo
				Snackbar infoBar = Snackbar.make(findViewById(R.id.coordLayout), "All completed items archived.",
						Snackbar.LENGTH_LONG);
				infoBar.setAction("UNDO", new OnClickListener() {
					@Override
					public void onClick(View v) {

						//undo deleting
						Utility.undoTransferToArchive(removedIndices);
						recyclerAdapter.notifyDataSetChanged();
						Utility.writeData(getApplicationContext()); //backup data
					}
				});
				infoBar.setActionTextColor(Color.WHITE);
				infoBar.show();
				return true;
			case R.id.display_archived:
				//switch to archive view
				Intent i = new Intent(BucketItemListView.this, ArchivedItemListView.class);
				startActivity(i);
				return true;
			case R.id.settings:
				Intent settingsActivity = new Intent(BucketItemListView.this, SettingsActivity.class);
				startActivity(settingsActivity);
				return true;
		}
		return super.onOptionsItemSelected(item);
	}

	@Override
	protected void onResume(){

		super.onResume();
		if(Utility.getBucketList().isEmpty()){
			Utility.readData(this.getApplicationContext()); //read data from backup
		}
		else{
			this.recyclerAdapter.notifyDataSetChanged(); //refresh
		}
	}

	@Override
	protected void onPause() {

		super.onPause();
		if(itemsMoved) {
			Utility.writeData(this.getApplicationContext()); //backup data
			itemsMoved = false;
		}
	}
}
