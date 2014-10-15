package com.bifi.mindpathsapp;

import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

/*
 * Class that implements the Dialog Fragments
 */
public class MindPathsDialog extends DialogFragment {
	
	public MindPathsDialog (){

	}
	
	@Override
	public View onCreateView (LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

		View view = inflater.inflate(R.layout.mind_paths_general_info_dialog, container);
		getDialog().setTitle(R.string.general_info_title);
		
		return view;
	}
}