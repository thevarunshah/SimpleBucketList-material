package com.thevarunshah.simplebucketlist;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.TextView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;
import com.thevarunshah.classes.Item;
import com.thevarunshah.simplebucketlist.internal.BucketItemListAdapter;
import com.thevarunshah.simplebucketlist.internal.OnStartDragListener;
import com.thevarunshah.simplebucketlist.internal.SimpleItemTouchHelperCallback;
import com.thevarunshah.simplebucketlist.internal.Utility;

import java.util.ArrayList;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

public class BucketItemListView extends AppCompatActivity implements OnStartDragListener {

	private static final String TAG = "BucketItemListView"; //for debugging purposes

	private RecyclerView recyclerView = null; //main view of items
	private BucketItemListAdapter recyclerAdapter = null; //adapter for items display
	private TextView emptyStateTextView;
	private FloatingActionButton addButton;
	private ItemTouchHelper itemTouchHelper;

	private boolean itemsMoved = false;
	private boolean startedFromShortcut = false;

	@Override
	protected void onCreate(Bundle savedInstanceState) {

		super.onCreate(savedInstanceState);
		setContentView(R.layout.bucket_item_listview);

		final String intentAction = getIntent().getAction();
		startedFromShortcut = intentAction != null
								&& intentAction.equals("android.intent.action.VIEW");

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

		emptyStateTextView = findViewById(R.id.list_empty_textview);

		//obtain add button and attach a on-tap listener to it
		addButton = findViewById(R.id.add_item);
		addButton.setOnClickListener(v -> {

            //inflate layout with customized alert dialog view
            LayoutInflater layoutInflater = LayoutInflater.from(BucketItemListView.this);
            final View dialog = layoutInflater.inflate(R.layout.input_dialog, null, false);
            final AlertDialog.Builder newItemDialogBuilder = new AlertDialog.Builder(BucketItemListView.this);

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
            newItemDialogBuilder.setPositiveButton("ADD", (dialogInterface, whichButton) -> {

                //create new item
                String itemText = input.getText().toString();
                Item item = new Item(itemText);

                //add item to main list and update view
				if (Utility.getAddToTopPreference(getApplicationContext())) {
					Utility.getBucketList().add(0, item);
				} else {
					Utility.getBucketList().add(item);
				}
                recyclerAdapter.notifyDataSetChanged();
                Utility.writeData(getApplicationContext()); //backup data

                if (recyclerView.getVisibility() == View.GONE) {
                    recyclerView.setVisibility(View.VISIBLE);
                    emptyStateTextView.setVisibility(View.GONE);
                }
            });
            newItemDialogBuilder.setNegativeButton("CANCEL", null);

            //create and show the dialog
            AlertDialog newItemDialog = newItemDialogBuilder.create();
            newItemDialog.show();

            if (newItemDialog.getWindow() != null) {
                //show keyboard
                newItemDialog.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE);
            }
        });

		//add specific listeners only if tablet is not being used
		if(!tablet){
			//moving fab out of the way when scrolling listview
			recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {

				@Override
				public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
					if (dy > 0) {
						addButton.animate().translationY(addButton.getHeight()*2); //scrolling down
					} else {
						addButton.animate().translationY(0); //scrolling up
					}
				}
			});
		}
	}

	@Override
	public void onStartDrag(RecyclerView.ViewHolder viewHolder) {
		itemTouchHelper.startDrag(viewHolder);
		itemsMoved = true;
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
				if (removedIndices.size() == 0) {
					Snackbar infoBar = Snackbar.make(findViewById(R.id.coordLayout), R.string.archive_nothing_text, Snackbar.LENGTH_SHORT);
					infoBar.show();
					return true;
				}
				recyclerAdapter.notifyDataSetChanged();
				Utility.writeData(this.getApplicationContext()); //backup data

				//friendly success message and give option to undo
				Snackbar infoBar = Snackbar.make(findViewById(R.id.coordLayout), R.string.archive_done_text, Snackbar.LENGTH_LONG);
				infoBar.setAction("UNDO", v -> {
                    //undo deleting
                    Utility.undoTransferToArchive(removedIndices);
                    recyclerAdapter.notifyDataSetChanged();
                    Utility.writeData(getApplicationContext()); //backup data
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
		if (Utility.getBucketList().isEmpty()) {
			Utility.readData(this.getApplicationContext()); //read data from backup
		} else{
			this.recyclerAdapter.notifyDataSetChanged(); //refresh
		}

		recyclerView.setVisibility(Utility.getBucketList().isEmpty() ? View.GONE : View.VISIBLE);
		emptyStateTextView.setVisibility(Utility.getBucketList().isEmpty() ? View.VISIBLE : View.GONE);

		if (startedFromShortcut) {
			addButton.callOnClick();
		}
	}

	@Override
	protected void onPause() {
		super.onPause();
		startedFromShortcut = false;
		if (itemsMoved) {
			Utility.writeData(this.getApplicationContext()); //backup data
			itemsMoved = false;
		}
	}
}
