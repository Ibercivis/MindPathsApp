package com.bifi.mindpathsapp;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import android.support.v4.app.FragmentManager;
import android.support.v7.app.ActionBarActivity;
import android.content.Context;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.PopupMenu;
import android.widget.Toast;
import android.widget.PopupMenu.OnMenuItemClickListener;

/*
 * MindPathsMainActivity
 * ----------------------
 * 
 * Author: Eduardo Lostal
 * 
 */
public class MindPathsMainActivity extends ActionBarActivity {

	DataBaseHelper myDbHelper;
	
	ArrayList<Node> wordPath;
	byte numPairs = 28;
	Pair currentPair = null;
	Node currentStartWord;
	byte currentStep;
	
	Button[] wordButtonList = new Button[12];
	Button startWordButton;
	int[] wordIdList = new int[12];
	
	Context context;
	
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mind_paths_main);
        
        // Create an instance for the database
        myDbHelper = new DataBaseHelper(this);
        
        context = this;
         
        try {
        	myDbHelper.createDataBase();
        } catch (IOException ioe) {
        	throw new Error("Unable to create database");
        }
        
        // Create a new task
        newTask();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu with the info button
        getMenuInflater().inflate(R.menu.mind_paths_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        if (id == R.id.info_action) {
        	
        	View view = findViewById(R.id.info_action);
        	PopupMenu popupMenu = new PopupMenu (this, view);
        	popupMenu.setOnMenuItemClickListener(new OnMenuItemClickListener(){
        		
        		@Override
        		public boolean onMenuItemClick (MenuItem item){
        			
        			FragmentManager fm = getSupportFragmentManager();
        			MindPathsDialog info_dialog = null;
        			
    				if (item.getItemId() == R.id.general_info){
    					// Click to open info dialog
        				info_dialog = new MindPathsDialog ();
        			}
    				
    				// Show the dialog once it has been created according to the proper parameter
    				info_dialog.show(fm, "Dialog fragment");
    				
        			return true;
        		}
        	});
        	popupMenu.inflate(R.menu.info_menu);
        	popupMenu.show();
        	
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
    
    /*
     * Custom on click listener in order to pass the selected word as a parameter.
     * Therefore, a function for the whole buttons must be implemented being reusable
     */
    public class WordButtonOnClickListener implements OnClickListener{

    	int wordSelected;

    	/*
    	 * In the constructor the index of the button in the buttons' array is set.
    	 * So, on button's click, listener function knows which item has been selected
    	 */
    	public WordButtonOnClickListener(int wordSelected)
    	{
    	    this.wordSelected = wordSelected;
    	}

    	@Override
    	public void onClick(View arg0) {
    		
    		// Get the info of the selected word
    		currentStartWord = myDbHelper.getNodeInfo(wordIdList[wordSelected]);
    		
    		// Update number of steps
    		currentStep++;
    		
    		// Check if the current word is the target word
    		if (currentStartWord.id == currentPair.node2.id){
    			// Target word found
    			newTask();
    			
    			// Show a toast
        		int duration = Toast.LENGTH_SHORT;
        		Toast toast = Toast.makeText(context, R.string.completed_toast, duration);
        		toast.show();
    		}
    		else {
        		// Check length of the path
        		if (currentStep == 25){
        			// Length of the path exceeded
        			newTask();
        			
        			// Show a toast
            		int duration = Toast.LENGTH_LONG;
            		Toast toast = Toast.makeText(context, R.string.exceeded_steps_toast, duration);
            		toast.show();
        		}
        		else {
        			// Keep playing
        		
        			// Add the word to the path
        			wordPath.add(currentStartWord);
    		
        			// Update the neighbours and objects displayed in the screen
        			startWordButton.setText(currentStartWord.name);
        			displayNeighbours(currentStartWord, wordPath);
        		}
    		}
    	}

    }
    
    /*
     * newTask
     * -------
     * Create, initialise everything for a new task
     * 
     * Pre: numPairs = 28
     * 		and wordButtonList not null
     * 		and wordIdList not null
     * Post: currentPair not null
     * 		 and currentStartWord not null
     * 		 and currentStep = 1
     * 		 and wordPath = [currentPair.node1]
     * 		 and wordButtonList and wordIdList updated with neighbours of currentStartWord
     *		 and startWordButton not null
     *		 and target button updated
     * 
     */
    public void newTask () {
        
        // Create the array for saving the path
        wordPath = new ArrayList<Node>();

        // Get randomly the index of the pair
		int pairId = (int)(Math.random()*numPairs);
	    
		List<Pair> pairs = myDbHelper.getAllPairs();
		
		// Check that the same pair is not selected subsequently
		if (currentPair == pairs.get(pairId)){
			// Save the next pair in the array as current pair
			currentPair = pairs.get((pairId+1)%numPairs);
		}
		else {
			// Save the current pair
			currentPair = pairs.get(pairId);
		}
		
		// Initialise the starting word
		currentStartWord = currentPair.node1;
		
		// Initialise the step
		currentStep = 1;
	
		// Add the word to the path
		wordPath.add(currentStartWord);
		
		// Initialise buttons and set listeners
		startWordButton = (Button) findViewById(R.id.start_word_button);
		startWordButton.setText(currentPair.node1.name);
		
        Button target_word_button = (Button) findViewById(R.id.target_word_button);
		target_word_button.setText(currentPair.node2.name);
		
		wordButtonList[0] = (Button) findViewById(R.id.word1_button);
		wordButtonList[0].setOnClickListener(new WordButtonOnClickListener(0));
		
		wordButtonList[1] = (Button) findViewById(R.id.word2_button);
		wordButtonList[1].setOnClickListener(new WordButtonOnClickListener(1));
		
		wordButtonList[2] = (Button) findViewById(R.id.word3_button);
		wordButtonList[2].setOnClickListener(new WordButtonOnClickListener(2));
		
		wordButtonList[3] = (Button) findViewById(R.id.word4_button);
		wordButtonList[3].setOnClickListener(new WordButtonOnClickListener(3));
		
		wordButtonList[4] = (Button) findViewById(R.id.word5_button);
		wordButtonList[4].setOnClickListener(new WordButtonOnClickListener(4));
		
		wordButtonList[5] = (Button) findViewById(R.id.word6_button);
		wordButtonList[5].setOnClickListener(new WordButtonOnClickListener(5));
		
		wordButtonList[6] = (Button) findViewById(R.id.word7_button);
		wordButtonList[6].setOnClickListener(new WordButtonOnClickListener(6));
		
		wordButtonList[7] = (Button) findViewById(R.id.word8_button);
		wordButtonList[7].setOnClickListener(new WordButtonOnClickListener(7));
		
		wordButtonList[8] = (Button) findViewById(R.id.word9_button);
		wordButtonList[8].setOnClickListener(new WordButtonOnClickListener(8));
		
		wordButtonList[9] = (Button) findViewById(R.id.word10_button);
		wordButtonList[9].setOnClickListener(new WordButtonOnClickListener(9));
		
		wordButtonList[10] = (Button) findViewById(R.id.word11_button);
		wordButtonList[10].setOnClickListener(new WordButtonOnClickListener(10));
		
		wordButtonList[11] = (Button) findViewById(R.id.word12_button);
		wordButtonList[11].setOnClickListener(new WordButtonOnClickListener(11));
		
		// Get and display neighbours from the current starting word
		displayNeighbours(currentStartWord, wordPath);
    }
    
    /*
     * displayNeighbours
     * ------------------
     * Get and display neighbours from the current starting word
     * 
     * Pre: currentStartWord != null 
     * 		and wordPath.size >= 1
     * Post: wordButtonList and wordIdList updated
     * 		 and neighbours displayed on the screen
     * 
     */
    public void displayNeighbours (Node currentStartWord, ArrayList<Node> wordPath) {
    	
    	// Get the neighbours
    	List<Node> neighbours = myDbHelper.getNeighbours(currentStartWord.id, wordPath);
    	
    	// Shuffle neighbours in order not to display them always in the same order
    	Collections.shuffle(neighbours);
    	
    	// Go through the whole neighbour buttons
    	for (int i=0; i<12; i++){
    		
    		// Check if the button must be visible
    		if (i < neighbours.size()){
    			// Visible
    			wordButtonList[i].setVisibility(View.VISIBLE);
    			wordButtonList[i].setText(neighbours.get(i).name);
    			wordIdList[i] = neighbours.get(i).id;
    		}
    		else {
    			// Not visible
    			wordButtonList[i].setVisibility(View.INVISIBLE);
    		}
    	}
    }
    
    /*
     * showPath
     * ---------
     * Show the current path in a Toast
     * 
     * Pre: wordPath.size >= 1
     * Post: Display on the screen the content of wordPath through a toast
     * 
     */
    public void showPath (View v) {
    	
    	// Show a toast
		int duration = Toast.LENGTH_LONG;
		String currentPath = currentPair.node1.name;
		for (int i=1; i<wordPath.size(); i++){
			currentPath += " -> "+wordPath.get(i).name;
		}
		Toast toast = Toast.makeText(context, currentPath, duration);
		toast.show();
    }
    
    /*
     * reset
     * ------
     * Reset the pair
     * 
     * Pre: currentStartWord != null 
     * 		and currentStep > 0
     *		and wordPath.size >= 1
     * Post: if currentStep+1==25
     *			then newTask
     *		 else currentStep+1
     *			  and wordPath, currentStartWord updated and initial pair in the screen
     * 
     */
    public void resetPair (View v) {
    		
		// Update number of steps
		currentStep++;
		
		// Check length of the path
    	if (currentStep == 25){
    		// Length of the path exceeded
    		newTask();
    			
    		// Show a toast
        	int duration = Toast.LENGTH_LONG;
        	Toast toast = Toast.makeText(context, R.string.exceeded_steps_toast, duration);
        	toast.show();
    	}
    	else {
    		// Keep playing
    		
    		currentStartWord = currentPair.node1;
    		wordPath.add(myDbHelper.resetNode);
    		wordPath.add(currentStartWord);
		
    		// Update the neighbours and objects displayed in the screen
    		startWordButton.setText(currentStartWord.name);
    		displayNeighbours(currentStartWord, wordPath);
    	}
    }
    
    /*
     * newPair
     * --------
     * Prepare a new task when new pair button is clicked
     * 
     * Pre: numPairs = 28
     * 		and wordButtonList not null
     * 		and wordIdList not null
     * Post: currentPair not null
     * 		 and currentStartWord not null
     * 		 and currentStep = 1
     * 		 and wordPath = [currentPair.node1]
     * 		 and wordButtonList and wordIdList updated with neighbours of currentStartWord
     *		 and startWordButton not null
     *		 and target button updated
     * 
     */
    public void newPair (View v) {
    		
		newTask();
    }


}
