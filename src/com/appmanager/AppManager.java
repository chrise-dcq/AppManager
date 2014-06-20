package com.appmanager;

import java.util.ArrayList;
import java.util.List;

import com.appmanager.AppAdapter.AppInfo;

import android.app.Activity;
import android.app.ActivityManager;
import android.app.Application;
import android.app.ActivityManager.RunningAppProcessInfo;
import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;

public class AppManager extends Application {
	
	public static final int ALL = 1;
	public static final int SHOW = 2;
	public static final String SETTING = "setting";
	
	@Override
	public void onCreate() {
		super.onCreate();
	}

	
	/**
	 * 获取正在运行的应用信息
	 * 
	 * @return
	 */
	public ArrayList<AppInfo> getRunningAppInfos(Activity act, int type) {
		ActivityManager mActivityManager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
		PackageManager pm = getPackageManager();
		List<ApplicationInfo> appList = pm.getInstalledApplications(PackageManager.GET_UNINSTALLED_PACKAGES);
		// 正在运行的进程
		List<RunningAppProcessInfo> runningAppProcessInfos = mActivityManager.getRunningAppProcesses();
		// 正在运行的应用
		ArrayList<AppInfo> runningApps = new ArrayList<AppInfo>(runningAppProcessInfos.size());
		for (RunningAppProcessInfo runningAppInfo : runningAppProcessInfos) {
			
			ArrayList<ApplicationInfo> infos = getAppInfo(runningAppInfo.pkgList, appList);
			
			for(ApplicationInfo applicationInfo: infos) {
				if(applicationInfo != null && !isSystemApp(applicationInfo)) {
					AppInfo info = new AppInfo();
					info.packageName = applicationInfo.packageName;
					BitmapDrawable bitmapDrawable = (BitmapDrawable) applicationInfo.loadIcon(pm);
					info.appIcon = getRightSizeIcon(bitmapDrawable);
					info.appName = applicationInfo.loadLabel(pm).toString();
					if(type == ALL) {
						if(!containInfo(runningApps, info)) {
							runningApps.add(info);
						}
					}else if(type == SHOW) {
						boolean show = !act.getSharedPreferences(SETTING, MODE_PRIVATE).getBoolean(info.packageName, false);//默认都显示
						if(show) {
							if(!containInfo(runningApps, info)) {
								runningApps.add(info);
							}
						}
					}
				}
			}
		}
		return runningApps;
	}
	
	private Drawable getRightSizeIcon(BitmapDrawable drawable) {
		Drawable rightDrawable = getResources().getDrawable(R.drawable.session_manager);
		int rightSize = rightDrawable.getIntrinsicWidth();
		Bitmap bitmap = drawable.getBitmap();
		int width = bitmap.getWidth();
		float widths = width;
		float scale = rightSize / widths;
		Matrix matrix = new Matrix();
		matrix.setScale(scale, scale);
		Bitmap bm = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(),
				bitmap.getHeight(), matrix, true);
		return new BitmapDrawable(getResources(), bm);
	}
	
	private boolean containInfo(ArrayList<AppInfo> infos, AppInfo info) {
		for(AppInfo af: infos) {
			if(af.packageName.equals(info.packageName)) {
				return true;
			}
		}
		return false;
	}
	
	public ArrayList<AppInfo> getAllAppInfos(Activity act, int type) {
		PackageManager pm = getPackageManager();
		List<ApplicationInfo> appList = pm.getInstalledApplications(PackageManager.GET_UNINSTALLED_PACKAGES);
		ArrayList<AppInfo> runningApps = new ArrayList<AppInfo>(50);
		for (ApplicationInfo applicationInfo : appList) {
			if(applicationInfo != null && !isSystemApp(applicationInfo)) {
				AppInfo info = new AppInfo();
				info.packageName = applicationInfo.processName;
				info.appIcon = applicationInfo.loadIcon(pm);
				info.appName = applicationInfo.loadLabel(pm).toString();
				if(type == ALL) {
					runningApps.add(info);
				}else if(type == SHOW) {
					boolean show = act.getSharedPreferences(SETTING, MODE_PRIVATE).getBoolean(info.packageName, true);//默认都显示
					if(show) {
						runningApps.add(info);
					}
				}
			}
		}
		return runningApps;
	}
	
	
	
	private boolean isSystemApp(ApplicationInfo appInfo) {
		if ((appInfo.flags & ApplicationInfo.FLAG_SYSTEM) > 0 ) {//system apps
			return true;
		}else {
			return false;
		}
	}
	
	/**
	 * 获取应用信息
	 * 
	 * @param name
	 * @return
	 */
	private ArrayList<ApplicationInfo> getAppInfo(String[] pkgList, List<ApplicationInfo> appList) {
        if (pkgList == null) {
            return null;
        }
        
        ArrayList<ApplicationInfo> infos = new ArrayList<ApplicationInfo>(pkgList.length);
        
        for(String pkg : pkgList) {
	        for (ApplicationInfo appinfo : appList) {
	            if (pkg.equals(appinfo.packageName)) {
	                 infos.add(appinfo);
	            }
	        }
        }
        return infos;
    }
	
	
}
