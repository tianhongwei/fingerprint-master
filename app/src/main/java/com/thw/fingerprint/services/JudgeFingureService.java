package com.thw.fingerprint.services;

import java.util.List;
import java.util.concurrent.RunnableScheduledFuture;

import com.thw.fingerprint.FingureAriseActivity;

import android.app.ActivityManager;
import android.app.ActivityManager.RunningAppProcessInfo;
import android.app.ActivityManager.RunningTaskInfo;
import android.app.Service;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ComponentInfo;
import android.os.IBinder;
import android.util.Log;

public class JudgeFingureService extends Service {

	private int countnumber = 0;
	@Override
	public IBinder onBind(Intent arg0) {
		// TODO Auto-generated method stub
		return null;
	}



	@Override
	public void onStart(Intent intent, int startId) {
		super.onStart(intent, startId);
		new Thread(){
			public void run() {
				try {
					while(true){
						Thread.sleep(1000);
						if(isAppOnForeground()){
							Log.i("前台运行", "time"+countnumber);
							if(countnumber>30){
								if(!"com.thw.fingerprint.FingureAriseActivity".equals(listActivity())){
									Intent myintent = new Intent();
									myintent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
									myintent.setClass(getApplicationContext(),FingureAriseActivity.class);
									startActivity(myintent);
								}
								countnumber = 0;
							}else{
								countnumber = 0;
							}
						}else{
							Log.i("后台运行", "time"+countnumber);
							countnumber ++;
						}
					}

				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			};

		}.start();
	}
	/**
	 * 判断程序在前台运行的方法
	 * @return
	 */
	public boolean isAppOnForeground() {
		ActivityManager systemService = (ActivityManager) this.getSystemService(Context.ACTIVITY_SERVICE);
		List<RunningAppProcessInfo> runningAppProcesses = systemService.getRunningAppProcesses();
		if(runningAppProcesses==null) return false;
		for(RunningAppProcessInfo processes: runningAppProcesses){
			if(processes.processName.equals("com.thw.fingerprint")&&processes.importance==RunningAppProcessInfo.IMPORTANCE_FOREGROUND){
				return true;
			}
		}
		return false;
	}
	/**
	 * 判断当前运行在前台的Activity
	 */
	@SuppressWarnings("deprecation")
	public  String listActivity(){
		ActivityManager systemService = (ActivityManager) this.getSystemService(Context.ACTIVITY_SERVICE);
		List<RunningTaskInfo> runningTasks = systemService.getRunningTasks(1);
		RunningTaskInfo runningTaskInfo = runningTasks.get(0);
		ComponentName component = runningTaskInfo.topActivity;
		String className = component.getClassName();
		return className;
	}

}
