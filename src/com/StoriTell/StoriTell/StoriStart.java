package com.StoriTell.StoriTell;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.reflect.Array;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.HashMap;

import org.json.JSONException;
import org.json.JSONObject;

import android.app.ListActivity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.CompoundButton;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.Toast;
import android.widget.ToggleButton;



public class StoriStart extends ListActivity {
    private ArrayList<HashMap<String,String>> hashMapStories;
    MenuItem ref;
    int current_page;
    Button previous;
    Button next;
    String sorting;
    boolean has_next;
    boolean has_prev;
    ProgressDialog pd;
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
    	setContentView(R.layout.main);
        super.onCreate(savedInstanceState);
    	pd = ProgressDialog.show(this, "Loading..", "Refreshing", true, false);
        this.current_page = 1;
        this.sorting = "-pub_date";
    	previous = (Button) findViewById(R.id.prev_page);
    	next = (Button) findViewById(R.id.next_page);
		new Thread(new Runnable(){
			public void run(){
				ArrayList<HashMap<String,String>> stories = fetchStories(current_page, sorting);
				Message msg = Message.obtain();
				msg.obj = stories;
				refreshHandler.sendMessage(msg);
			}
		}).start();
	}
    public void refreshList(ArrayList<HashMap<String,String>> storiesmap){
        this.hashMapStories = storiesmap;
        SimpleAdapter adapterForList = new SimpleAdapter(this,this.hashMapStories,R.layout.list_item, 
        		new String[]{"upvotes","title"},
        		new int[]{R.id.storyVotes, R.id.storyListHead});
        System.out.println("Simp adapter declared");
        setListAdapter(adapterForList);
        ListView lv = getListView();
        final ArrayList<HashMap<String,String>> hashMap = this.hashMapStories;
        lv.setOnItemClickListener(new OnItemClickListener() {
            public void onItemClick(AdapterView<?> parent, View view, int position, long id){
            	Intent intent = new Intent(StoriStart.this,storyDetail.class);
            	intent.putExtra("title", hashMap.get(position).get("title"));
            	intent.putExtra("text", hashMap.get(position).get("text"));
            	intent.putExtra("upvotes", hashMap.get(position).get("upvotes"));
            	intent.putExtra("id", position);
            	startActivity(intent);
            }
		});
		if (has_next == false){
			next.setVisibility(View.INVISIBLE);
		}
		else {
			next.setVisibility(View.VISIBLE);
		}
		if (has_prev == false){
			previous.setVisibility(View.INVISIBLE);
		}
		else {
			previous.setVisibility(View.VISIBLE);
		}
		next.setOnTouchListener(new OnTouchListener(){
			@Override
			public boolean onTouch(View v, MotionEvent event) {
				if (event.getAction() == MotionEvent.ACTION_DOWN){
					current_page += 1;
			    	pd = ProgressDialog.show(StoriStart.this, "Loading..", "Changing page", true, false);
	        		new Thread(new Runnable(){
	        			public void run(){
	        				ArrayList<HashMap<String,String>> stories = fetchStories(current_page, sorting);
	        				Message msg = Message.obtain();
	        				msg.obj = stories;
	        				refreshHandler.sendMessage(msg);
	        			}
	        		}).start();						}
				return true;
				
			}
		});
		previous.setOnTouchListener(new OnTouchListener(){
			@Override
			public boolean onTouch(View v, MotionEvent event) {
				if (event.getAction() == MotionEvent.ACTION_DOWN){
					current_page -= 1;
			    	pd = ProgressDialog.show(StoriStart.this, "Loading..", "Changing page", true, false);
	        		new Thread(new Runnable(){
	        			public void run(){
	        				ArrayList<HashMap<String,String>> stories = fetchStories(current_page, sorting);
	        				Message msg = Message.obtain();
	        				msg.obj = stories;
	        				refreshHandler.sendMessage(msg);
	        			}
	        		}).start();						}
				return false;
			}
		});

    }
    public ArrayList<HashMap<String,String>> fetchStories(int page, String sort_items){
		ArrayList<HashMap<String,String>> storyData = new ArrayList<HashMap<String,String>>();
    	try{
    		int offset = (page * 20) - 20;
    		URL storitell = new URL("http://storitell.com/api/story/?format=json&offset="+offset+"&sort_by="+sort_items+"");
    		URLConnection sc = storitell.openConnection();
    		BufferedReader in = new BufferedReader(new InputStreamReader(sc.getInputStream()));
    		String line;
    		while ((line=in.readLine())!=null){
    			JSONObject ja = new JSONObject(line);
    			for (int i=0;i < ja.getJSONArray("objects").length(); i++){
    	        	HashMap<String,String> entity = new HashMap<String,String>();
    				JSONObject jo = (JSONObject) ja.getJSONArray("objects").get(i);
    				entity.put("text",jo.getString("maintext"));
    				entity.put("upvotes","+" +jo.getString("upvotes"));
    				entity.put("pub_date", jo.getString("pub_date"));
    				String[] tokenized = jo.getString("maintext").split("\\s");
    				int j = 0;
    				String title = new String();
    				while (j<tokenized.length && j < 6){
    					title = title + tokenized[j] + " ";
    					j++;
    				}
    				entity.put("title", title);
    				storyData.add(entity);
    			}
    			JSONObject meta = ja.getJSONObject("meta");
    			if (meta.get("next") == JSONObject.NULL){
    				has_next = false;
    			}
    			else {
    				has_next = true;
    			}
    			if (meta.get("previous") == JSONObject.NULL){
    				has_prev = false;
    			}
    			else {
    				has_prev = true;
    			}
    		}
    	}
    	catch (MalformedURLException e){
    		Context context = getApplicationContext();
    		CharSequence text = "StoriTell is down!";
    		int duration = Toast.LENGTH_SHORT;

    		Toast toast = Toast.makeText(context, text, duration);
    		toast.show();
    	}
    	catch (IOException e){
    		Context context = getApplicationContext();
    		CharSequence text = "StoriTell is down!";
    		int duration = Toast.LENGTH_SHORT;

    		Toast toast = Toast.makeText(context, text, duration);
    		toast.show();
    	}
    	catch (JSONException e){
    		Context context = getApplicationContext();
    		CharSequence text = "StoriTell is down!";
    		int duration = Toast.LENGTH_SHORT;

    		Toast toast = Toast.makeText(context, text, duration);
    		toast.show();
    	}
		return storyData;
    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.list_menu, menu);
        return true;
    }
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle item selection
        switch (item.getItemId()) {
	        case R.id.refresh_list:
	        	pd = ProgressDialog.show(this, "Loading..", "Refreshing", true, false);
        		new Thread(new Runnable(){
        			public void run(){
        				ArrayList<HashMap<String,String>> stories = fetchStories(current_page, sorting);
        				Message msg = Message.obtain();
        				msg.obj = stories;
        				refreshHandler.sendMessage(msg);
        			}
        		}).start();
	            return true;
	        case R.id.sort_upvotes:
	        	if (sorting != "-upvotes"){
		        	pd = ProgressDialog.show(this, "Loading..", "Sorting", true, false);
	        		sorting = "-upvotes";
	        		current_page = 1;
	        		new Thread(new Runnable(){
	        			public void run(){
	        				ArrayList<HashMap<String,String>> stories = fetchStories(current_page, sorting);
	        				Message msg = Message.obtain();
	        				msg.obj = stories;
	        				refreshHandler.sendMessage(msg);
	        			}
	        		}).start();
	        	}
	        	return true;
	        case R.id.sort_recent:
	        	if (sorting != "-pub_date"){
		        	pd = ProgressDialog.show(this, "Loading..", "Sorting", true, false);
	        		sorting = "-pub_date";
	        		new Thread(new Runnable(){
	        			public void run(){
	        				ArrayList<HashMap<String,String>> stories = fetchStories(current_page, sorting);
	        				Message msg = Message.obtain();
	        				msg.obj = stories;
	        				refreshHandler.sendMessage(msg);
	        			}
	        		}).start();
	        	}
	        	return true;
	        default:
	            return super.onOptionsItemSelected(item);
        }
    }
    private Handler refreshHandler = new Handler() {
    	@Override
    	public void handleMessage (Message msg){
    		pd.dismiss();
    		refreshList((ArrayList<HashMap<String, String>>) msg.obj);
    	}
    };
}
