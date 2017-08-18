package com.coolyota.logreport.tools;

import android.app.ActivityManager;
import android.content.Context;

import java.util.List;

import static android.content.Context.ACTIVITY_SERVICE;

/**
 * des: 
 * 
 * @author  liuwenrong
 * @version 1.0,2017/8/18 
 */
public class ActivityUtils   {

    public static String getTopPackageName(Context ctx){

        //获取到进程管理器

        ActivityManager activityManager = (ActivityManager) ctx.getSystemService(ACTIVITY_SERVICE);
//获取到当前正在运行的任务栈
        List<ActivityManager.RunningTaskInfo> tasks = activityManager.getRunningTasks(1);//参数是想获得的个数，可以随意写
        //获取到最上面的进程
        ActivityManager.RunningTaskInfo taskInfo = tasks.get(0);
        //获取到最顶端应用程序的包名
        String packageName = taskInfo.topActivity.getPackageName();

        return packageName;
    }

}
