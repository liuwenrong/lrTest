package com.coolyota.logreport;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

public class SecretCodeReceiver extends BroadcastReceiver {

    private static final String TAG = SecretCodeReceiver.class.getSimpleName();

    @Override
    public void onReceive(Context context, Intent intent) {

        if (intent.getData() == null || intent.getData().getHost().isEmpty()) {
            Log.e(TAG, "onReceive: undefined secret code!");
            return;
        }

//        Log.d(TAG, "获取的暗码 host = " + intent.getData().getHost());

        if ("2017".equals(intent.getData().getHost())){
            context.startActivity(LogSettingActivity.getLaunchIntent(context));
        } else if("2018".equals(intent.getData().getHost())){
//            context.startActivity(ServerSettingActivity.getLaunchIntent(context));
            Intent intentServer = new Intent(context, ServerSettingActivity.class);
            context.startActivity(intentServer);
        }

    }
}
