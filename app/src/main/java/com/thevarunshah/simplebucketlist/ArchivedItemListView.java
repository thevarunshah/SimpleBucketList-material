package com.thevarunshah.simplebucketlist;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.widget.ListView;

import com.thevarunshah.classes.ArchivedItemAdapter;
import com.thevarunshah.classes.Backend;

public class ArchivedItemListView extends AppCompatActivity {

    private static final String TAG = "ArchivedItemListView"; //for debugging purposes

    private ListView listView = null; //main view of items
    private ArchivedItemAdapter listAdapter = null; //adapter for items display

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.archived_item_listview);

        //fetch toolbar and set it as the action bar
        Toolbar toolbar = (Toolbar) findViewById(R.id.archived_toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true); //back button

        //obtain list view and create new archive list custom adapter
        listView = (ListView) findViewById(R.id.archived_listview);
        listAdapter = new ArchivedItemAdapter(this, R.layout.archived_row, Backend.getArchiveList());
        listView.setAdapter(listAdapter); //attach adapter to list view
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()) {
            case android.R.id.home:
                //back button tapped, finish activity
                this.finish();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onPause(){

        super.onPause();
        Backend.writeData(this.getApplicationContext()); //backup data
    }

    @Override
    protected void onResume(){

        super.onResume();
        if(Backend.getArchiveList().isEmpty()){
            Backend.readData(this.getApplicationContext()); //read data from backup
        }
    }
}
