package com.maulana.custommodul;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;

import java.util.HashMap;

public class SessionManager {
	// Shared Preferences
	SharedPreferences pref;
	
	// Editor for Shared preferences
	Editor editor;
	
	// Context
	Context context;
	
	// Shared pref mode
	int PRIVATE_MODE = 0;
	
	// Sharedpref file name
	private static final String PREF_NAME = "GmediaUserPSP";
	
	// All Shared Preferences Keys
	private static final String IS_LOGIN = "IsLoggedIn";
	public static final String TAG_UID = "uid"; //GA
	public static final String TAG_NIK = "nik"; // MKIOS
	public static final String TAG_NAMA = "nama";
	public static final String TAG_USERNAME = "username";
	public static final String TAG_PASSWORD = "password";
	public static final String TAG_TOKEN = "token";
	public static final String TAG_EXP = "expired_at";
	public static final String TAG_LEVEL = "level";
	public static final String TAG_SAVED = "saved";
	public static final String TAG_AREA = "area";
	public static final String TAG_FLAG = "flag";

	// Constructor
	public SessionManager(Context context){
		this.context = context;
		pref = this.context.getSharedPreferences(PREF_NAME, PRIVATE_MODE);
		editor = pref.edit();
	}
	
	/**
	 * Create login session
	 * */
	public void createLoginSession(String uid, String nik, String nama, String username, String password, String saved, String token, String exp, String level, String area, String flag){

		editor.putBoolean(IS_LOGIN, true);
		
		editor.putString(TAG_UID, uid); // nik GA
		
		editor.putString(TAG_NIK, nik);

		editor.putString(TAG_NAMA, nama);

		editor.putString(TAG_USERNAME, username);

		editor.putString(TAG_PASSWORD, password);

		editor.putString(TAG_SAVED, saved); // value is 0 or 1

		editor.putString(TAG_TOKEN, token);

		editor.putString(TAG_EXP, exp);

		editor.putString(TAG_LEVEL, level);

		editor.putString(TAG_AREA, area);

		editor.putString(TAG_FLAG, flag);
		// commit changes
		editor.commit();
	}

	public void savePassword(String password){

		editor.putString(TAG_PASSWORD, password);

		// commit changes
		editor.commit();
	}
	
	/**
	 * Get stored session data
	 * */
	public HashMap<String, String> getUserDetails(){
		HashMap<String, String> user = new HashMap<String, String>();
		// user uid
		user.put(TAG_UID, pref.getString(TAG_UID, ""));
		
		// user nik
		user.put(TAG_NIK, pref.getString(TAG_NIK, ""));

		user.put(TAG_NAMA, pref.getString(TAG_NAMA, ""));

		user.put(TAG_USERNAME, pref.getString(TAG_USERNAME, ""));

		user.put(TAG_PASSWORD, pref.getString(TAG_PASSWORD, ""));

		user.put(TAG_SAVED, pref.getString(TAG_SAVED, ""));

		user.put(TAG_TOKEN, pref.getString(TAG_TOKEN, ""));

		user.put(TAG_EXP, pref.getString(TAG_EXP, ""));

		user.put(TAG_LEVEL, pref.getString(TAG_LEVEL, ""));

		user.put(TAG_AREA, pref.getString(TAG_AREA, ""));

		user.put(TAG_FLAG, pref.getString(TAG_FLAG, ""));
		// return user
		return user;
	}

	public String getUserInfo(String key){
		return pref.getString(key, "");
	}

	public String getUser(){
		return pref.getString(TAG_NAMA, "");
	}

	public String getUsername(){
		return pref.getString(TAG_USERNAME, "");
	}

	public String getPassword(){
		return pref.getString(TAG_PASSWORD, "");
	}

	public String getLevel(){
		return pref.getString(TAG_LEVEL, "");
	}

	/**
	 * Clear session details
	 * */
	public void logoutUser(Intent logoutIntent){

		// Clearing all data from Shared Preferences
		try {
			editor.clear();
			editor.commit();
		}catch (Exception e){
			e.printStackTrace();
		}

		logoutIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
		context.startActivity(logoutIntent);
		((Activity)context).finish();
		((Activity)context).overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
	}
	
	/**
	 * Quick check for login
	 * **/
	// Get Login State
	public boolean isLoggedIn(){
		if(!getUserDetails().get(TAG_UID).equals("")){
			return true;
		}else{
			return false;
		}
		/*return pref.getBoolean(IS_LOGIN, false);*/
	}

	public boolean isSaved(){
		if(!getUserDetails().get(TAG_SAVED).equals("") && getUserDetails().get(TAG_SAVED).equals("1")){

			return true;
		}else{
			return false;
		}
	}

}
