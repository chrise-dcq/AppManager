package com.appmanager;

import java.util.HashMap;

import com.appmanager.AppAdapter.ViewCache;

import android.app.Activity;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.AdapterView.OnItemClickListener;

public class SettingActivity extends Activity implements
	OnItemClickListener{

	private AppAdapter appAdapter;

	@SuppressWarnings("unchecked")
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.setting);
		AppManager application = (AppManager) getApplication();
		appAdapter = new AppAdapter(SettingActivity.this, application.getRunningAppInfos(SettingActivity.this, AppManager.ALL), R.layout.listviewitem);
		ListView appListView = (ListView) findViewById(R.id.setting_list);
		appAdapter.isSelected = (HashMap<String, Boolean>) getSharedPreferences(AppManager.SETTING, MODE_PRIVATE).getAll();	
		appListView.setAdapter(appAdapter);
		appListView.setOnItemClickListener(this);
		getActionBar().setIcon(R.drawable.setting);
		getActionBar().setHomeButtonEnabled(true);
		getActionBar().setDisplayHomeAsUpEnabled(true);
	}
	
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) 
		   {        
		      case android.R.id.home:            
		         finish();       
		         return true;        
		      default:            
		         return super.onOptionsItemSelected(item);    
		   }
	}
	
	/**
	 * AdapterView 即 ListView
	 */
	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position,
			long id) {
		ViewCache viewCache = (ViewCache) view.getTag();
		viewCache.app_CheckBox.toggle();// 反选
		Boolean isChecked = viewCache.app_CheckBox.isChecked();
		getSharedPreferences(AppManager.SETTING, MODE_PRIVATE).edit().putBoolean(viewCache.info.packageName, isChecked).commit();
	}

}
