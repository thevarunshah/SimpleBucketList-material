package com.thevarunshah.classes;

import java.io.Serializable;

public class Item implements Serializable {

	private static final long serialVersionUID = 1L; //for serializing data

	private String itemText = ""; //the itemText
	private boolean done; //boolean indicating if item is done

	/**
	 * creates a new bucket list item
	 *
	 * @param text the contents of the item
	 */
	public Item(String text){
		this.itemText = text;
		this.done = false;
	}

	public String getItemText() {
		return itemText;
	}

	public void setItemText(String itemText) {
		this.itemText = itemText;
	}

	public boolean isDone() {
		return done;
	}

	public void setDone(boolean done) {
		this.done = done;
	}

	@Override
	public String toString(){
		return this.itemText;
	}

}
