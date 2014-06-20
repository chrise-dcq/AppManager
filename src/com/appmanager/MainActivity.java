package com.appmanager;

import java.io.IOException;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.Map.Entry;

import com.appmanager.AppAdapter.AppInfo;
import com.appmanager.AppAdapter.ViewCache;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;
import android.widget.AdapterView.OnItemClickListener;

public class MainActivity extends Activity implements
	OnItemClickListener, OnClickListener{

	private AlertDialog mDialog = null;
	private AppAdapter appAdapter;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		getOverflowMenu();
		setContentView(R.layout.main);
	}
	
	@Override
	protected void onResume() {
		super.onResume();
		AppManager application = (AppManager) getApplication();
		appAdapter = new AppAdapter(MainActivity.this, application.getRunningAppInfos(MainActivity.this, AppManager.SHOW), R.layout.listviewitem);
		ListView appListView = (ListView) findViewById(R.id.app_list);
		appListView.setAdapter(appAdapter);
		appListView.setOnItemClickListener(this);
		selectAll(true);
		
	}


	@Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }
	
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
    	if(mDialog == null) {
    		mDialog = new AlertDialog.Builder(MainActivity.this).create();
    	} 
        switch(item.getItemId()) {
        case R.id.action_help:
        	mDialog.setTitle(getResources().getString(R.string.action_help));
        	mDialog.setMessage(getResources().getString(R.string.action_help_msg));
        	mDialog.show();
        	break;
        case R.id.action_about:
        	mDialog.setTitle(getResources().getString(R.string.action_about));
        	mDialog.setMessage(getResources().getString(R.string.action_about_msg));
        	mDialog.show();
        	break;
        case R.id.action_feedback:
        	break;
        case R.id.action_settings:
        	startActivity(new Intent(MainActivity.this, SettingActivity.class));
        	break;
        }
        return super.onOptionsItemSelected(item);
    }
	
    private void getOverflowMenu() {
        try {
           ViewConfiguration config = ViewConfiguration.get(this);
           Field menuKeyField = ViewConfiguration.class.getDeclaredField("sHasPermanentMenuKey");
           if(menuKeyField != null) {
               menuKeyField.setAccessible(true);
               menuKeyField.setBoolean(config, false);
           }
       } catch (Exception e) {
           e.printStackTrace();
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
		appAdapter.isSelected.put(viewCache.info.packageName, isChecked);
		setTitle("已选中" + getSelectCount() + "项");
	}

	/**
	 * 获取 选中的个数
	 * 
	 * @return
	 */
	private int getSelectCount() {
		int count = 0;
		Set<Entry<String, Boolean>> entrySet = appAdapter.isSelected.entrySet();
		Iterator<Entry<String, Boolean>> it = entrySet.iterator();
		while (it.hasNext()) {
			Entry<String, Boolean> entry = (Entry<String, Boolean>) it.next();
			Boolean isSlected = entry.getValue();
			if (isSlected) {
				count++;
			}
		}
		return count;
	}

	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.kill:
			forceStopByShell();
			break;

		default:
			break;
		}
		appAdapter.notifyDataSetChanged();
	}

	/**
	 * 全选,全不选
	 * 
	 * @param isSlected
	 */
	private void selectAll(boolean isSlected) {
		for (AppInfo appInfo : appAdapter.getInfos()) {
			appAdapter.isSelected.put(appInfo.packageName, isSlected);
		}
	}
	
	private void forceStopByShell() {
		Set<Entry<String, Boolean>> entrySet = appAdapter.isSelected.entrySet();
		Iterator<Entry<String, Boolean>> iterator = entrySet.iterator();
		boolean kill_self = false;
		String mPackageName = getPackageName();
		while (iterator.hasNext()) {
			Entry<String, Boolean> entry = (Entry<String, Boolean>) iterator.next();
			if (entry.getValue()) {
				String packageName = entry.getKey();
				if(packageName.equals(mPackageName)) {
					kill_self = true;
				}else {
					commandLine(packageName);
				}
			}
		}
		
		if(kill_self) {
			commandLine(mPackageName);
		}else {
			AppManager application = (AppManager) getApplication();
			appAdapter.updateData(application.getRunningAppInfos(MainActivity.this, AppManager.SHOW));//更新数据
		}
	}
	
	private void commandLine(String packageName) {
		List<String> commands = new ArrayList<String>();
		commands.add("su");
        commands.add("|");
        commands.add("am");
        commands.add("force-stop");
        commands.add(packageName);
        ProcessBuilder pb = new ProcessBuilder(commands);
        try {
        	pb.start();
		} catch (IOException e) {
			e.printStackTrace();
		}
        
        Toast.makeText(MainActivity.this,
				"Kill " + packageName,
				Toast.LENGTH_SHORT).show();
	}

}