package com.bifi.mindpathsapp;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

/*
 * Initial code from: http://www.reigndesign.com/blog/using-your-own-sqlite-database-in-android-applications/
 */

public class DataBaseHelper extends SQLiteOpenHelper{
    
	//The Android's default system path of your application database.
//private static String DB_PATH = "/data/data/com.bifi.mindpathsapp/databases/";
//private static String DB_PATH = "/data/data/com.bifi.mindpathsapp/databases/";
	
	private static String DB_NAME = "networkDatabase.db";
 
	private SQLiteDatabase myDataBase;
 
	private final Context myContext;
	
	public Node resetNode = new Node(0, "(RESET)");
 
	/**
	 * Constructor
	 * Takes and keeps a reference of the passed context in order to access to the application assets and resources.
	 * @param context
	 */
	public DataBaseHelper(Context context) {
 
		super(context, DB_NAME, null, 1);
		this.myContext = context;
	}	
 
	/**
	 * Creates a empty database on the system and rewrites it with your own database.
	 * */
	public void createDataBase() throws IOException{
 
		boolean dbExist = checkDataBase();
 
		if(dbExist){
			//do nothing - database already exist
		}else{
 
			//By calling this method and empty database will be created into the default system path
			//of your application so we are gonna be able to overwrite that database with our database.
			this.getReadableDatabase();
 
			try {
 
				copyDataBase();
 
			} catch (IOException e) {
 
				throw new Error("Error copying database");
 
			}
		}
 
	}
 
	/**
	 * Check if the database already exist to avoid re-copying the file each time you open the application.
	 * @return true if it exists, false if it doesn't
	 */
	private boolean checkDataBase(){
 
		SQLiteDatabase checkDB = null;
 
		try{
//String myPath = DB_PATH + DB_NAME;
			File outFile = myContext.getDatabasePath(DB_NAME);
			String myPath = outFile.getPath() ;
			
			checkDB = SQLiteDatabase.openDatabase(myPath, null, SQLiteDatabase.OPEN_READONLY);
 
		}catch(SQLiteException e){
 
			//database does't exist yet.
 
		}
 
		if(checkDB != null){
 
			checkDB.close();
 
		}
 
		return checkDB != null ? true : false;
	}
 
	/**
	 * Copies your database from your local assets-folder to the just created empty database in the
	 * system folder, from where it can be accessed and handled.
	 * This is done by transfering bytestream.
	 * */
	private void copyDataBase() throws IOException{
 
		//Open your local db as the input stream
		InputStream myInput = myContext.getAssets().open(DB_NAME);
 
		// Path to the just created empty db
//String outFileName = DB_PATH + DB_NAME;
		File outFile = myContext.getDatabasePath(DB_NAME);
		String outFileName = outFile.getPath() ;
		
		//Open the empty db as the output stream
		OutputStream myOutput = new FileOutputStream(outFileName);
 
		//transfer bytes from the inputfile to the outputfile
		byte[] buffer = new byte[1024];
		int length;
		while ((length = myInput.read(buffer))>0){
			myOutput.write(buffer, 0, length);
		}
 
		//Close the streams
		myOutput.flush();
		myOutput.close();
		myInput.close();
 
	}
 
	public void openDataBase() throws SQLException{
 
		//Open the database
//String myPath = DB_PATH + DB_NAME;
		File outFile = myContext.getDatabasePath(DB_NAME);
		String myPath = outFile.getPath() ;
		
		myDataBase = SQLiteDatabase.openDatabase(myPath, null, SQLiteDatabase.OPEN_READONLY);
 
	}
 
	@Override
	public synchronized void close() {
 
		if(myDataBase != null)
			myDataBase.close();
 
		super.close();
 
	}
 
	@Override
	public void onCreate(SQLiteDatabase db) {
 
	}
 
	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
 
	}
 
	// CRUD methods
 
	/*
     * getNodeInfo
     * ------------
     * Get node info
     * 
     * Pre: Pre: nodeId>0 and nodeId<2393
     * Post: node.name != null
     * 
     */
	public Node getNodeInfo(int nodeId) {
		
		SQLiteDatabase db = this.getReadableDatabase();
 
		Cursor cursor = db.query("node", new String[] { "_id",
            "name"}, "_id=?",
            new String[] { String.valueOf(nodeId) }, null, null, null, null);
		
		if (cursor != null)
			cursor.moveToFirst();
 
		Node node = new Node(Integer.parseInt(cursor.getString(0)), cursor.getString(1));

		return node;
	}
	
	/*
     * getNeighbours
     * --------------
     * Get neighbours
     * 
     * Pre: nodeId>0 and nodeId<2393
     * Post: neighbours.length < 10
     * 		 elements in neighbours are not immediately after any other appearance 
     * 		 of nodeId in path (arc removed)
     * 
     */
	public List<Node> getNeighbours(int nodeId, ArrayList<Node> path) {
		
		List<Node> neighboursList = new ArrayList<Node>();
		
	    // Select All Query
	    String selectQuery = "SELECT node._id, node.name, arc._id FROM node, arc where arc.node1="+nodeId+" and node._id=arc.node2";
	 
	    SQLiteDatabase db = this.getWritableDatabase();
	    Cursor cursor = db.rawQuery(selectQuery, null);
	 
	    // looping through all rows and adding to list
	    if (cursor.moveToFirst()) {
	        do {
	            Node node = new Node(Integer.parseInt(cursor.getString(0)), cursor.getString(1));
	            
	            // Check if the arc from the node of nodeId to node has been already passed through
	            // Avoid the check if the size is 1
	            if (path.size() == 1){
	            	// Adding node to list
            		neighboursList.add(node);
	            }
	            else {
	            	if (checkArc(getNodeInfo(nodeId), node, path)){
	            		// Adding node to list
	            		neighboursList.add(node);
	            	}
	            }
	        } while (cursor.moveToNext());
	    }
	 
	    // return neighbours list
	    return neighboursList;
	}

	/*
     * getAllPairs
     * ------------
     * Get pairs
     * 
     * Pre: 
     * Post: pairs.length = 28
     * 
     */
	public List<Pair> getAllPairs() {
		
		List<Pair> pairsList = new ArrayList<Pair>();
		
	    // Select All Query
	    String selectQuery = "SELECT startingWord, targetWord FROM pair";
	 
	    SQLiteDatabase db = this.getWritableDatabase();
	    Cursor cursor = db.rawQuery(selectQuery, null);
	 
	    // looping through all rows and adding to list
	    if (cursor.moveToFirst()) {
	        do {
	        	
	            Pair pair = new Pair(getNodeInfo(Integer.parseInt(cursor.getString(0))), getNodeInfo(Integer.parseInt(cursor.getString(1))));
	            // Adding node to list
	            pairsList.add(pair);
	        } while (cursor.moveToNext());
	    }
	 
	    // return pairs list
	    return pairsList;
	}
	
	// More methods
	
	/*
     * getNeighbours
     * --------------
     * Get neighbours
     * 
     * Pre: nodeId>0 and nodeId<2393
     * Post: True if node "end" is not immediately after any other appearance 
     * 		 of node "origin" in "path" after the last appearance of (RESET) (if any)
     * 
     */
	public Boolean checkArc(Node origin, Node end, ArrayList<Node> path) {
		
		// When reset is pressed, new node reset is added with id=0 and name="(RESET)"
		//  Check the last appearance of "(RESET)" (if any)
		int lastAppearance = path.lastIndexOf(resetNode);
		int startWord = 0;

		if (lastAppearance != -1){
			// There was at least one reset
			startWord = lastAppearance+1;
		}
		
		// Go through the path from the beginning or the word after the last appearance
		// of "(RESET)" (if any)
		for (int i=startWord; i<path.size()-1; i++){
			if ((path.get(i).id == origin.id) && (path.get(i+1).id == end.id)){
				return false;
			}
		}
		
		return true;
	}
}