package com.thevarunshah.simplebucketlist;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;

public class AddItem extends Activity implements OnClickListener{
	
	private static final String TAG = "AddItem"; //for debugging purposes
	
	@Override
    protected void onCreate(Bundle savedInstanceState) {
		
        super.onCreate(savedInstanceState);
        setContentView(R.layout.add_item);
        
        //obtain button and attach press listener to it
        Button addItem = (Button) findViewById(R.id.add_item);
        addItem.setOnClickListener(this);
    }
    
    @Override
	public void onClick(View v) {

    	Log.i(TAG, "finished entering goal");
    	
    	//edittext object of the goal entered
    	EditText text = (EditText) findViewById(R.id.item_text);
    	
    	//create new intent to put the goal in
    	Intent data = new Intent();
    	data.putExtra("text", text.getText().toString());
    	
    	//attach data to return call and go back to main view
    	setResult(RESULT_OK, data);
    	finish();
	}

}
