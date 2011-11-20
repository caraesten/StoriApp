package com.StoriTell.StoriTell;

import android.app.Activity;
import android.os.Bundle;
import android.widget.TextView;

public class storyDetail extends Activity {
	@Override
	public void onCreate(Bundle savedInstanceState){
		super.onCreate(savedInstanceState);
		setContentView(R.layout.story_detail);
		Bundle extras = getIntent().getExtras();
		String storyString = new String();
		String storyTitle = new String();
		int storyID;
		if (extras != null){
			storyString = extras.getString("text");
			storyTitle = extras.getString("title");
			storyID = extras.getInt("id");
		}
		TextView title = (TextView)findViewById(R.id.storyTitle);
		TextView story = (TextView)findViewById(R.id.storyText);
		title.setText(storyTitle);
		story.setText(storyString.replace("\\n", "\n"));
	}
}
