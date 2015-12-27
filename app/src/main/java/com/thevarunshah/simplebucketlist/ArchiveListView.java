package com.thevarunshah.simplebucketlist;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.widget.ListView;

import com.thevarunshah.classes.Backend;
import com.thevarunshah.classes.BucketAdapter;

public class ArchiveListView extends AppCompatActivity {

    private static final String TAG = "ArchiveListView"; //for debugging purposes

    private ListView listView = null; //main view of goals
    private BucketAdapter listAdapter = null; //adapter for goals display

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.archived_list_view);

        Toolbar myToolbar = (Toolbar) findViewById(R.id.archive_toolbar);
        setSupportActionBar(myToolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        //obtain list view and create new bucket list custom adapter
        listView = (ListView) findViewById(R.id.archive_listview);
        listAdapter = new BucketAdapter(this, R.layout.row, Backend.getArchiveList());

        //attach adapter to list view
        listView.setAdapter(listAdapter);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                this.finish();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onPause(){

        super.onPause();

        //backup data
        Log.i(TAG, "writing to file");
        Backend.writeData(this.getApplicationContext());
    }

    @Override
    protected void onResume(){

        super.onResume();

        //read data from backup
        if(Backend.getArchiveList().isEmpty()){
            Log.i(TAG, "reading from file");
            Backend.readData(this.getApplicationContext());
        }
    }
}
