package com.coolyota.logreport;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class SecretCodeReceiver extends BroadcastReceiver {

    private static final String TAG = SecretCodeReceiver.class.getSimpleName();

    @Override
    public void onReceive(Context context, Intent intent) {

        if (intent.getData() == null || intent.getData().getHost().isEmpty()) {
            return;
        }

        if ("900".equals(intent.getData().getHost())){
//            context.startActivity(LogSettingActivity.getLaunchIntent(context));

            Intent intentServer = new Intent(context, MainActivity.class);
//            Intent intentServer = new Intent(context, LogSettingActivity.class);
            context.startActivity(intentServer);
        } else if("2018".equals(intent.getData().getHost())){
//            context.startActivity(ServerSettingActivity.getLaunchIntent(context));
            Intent intentServer = new Intent(context, ServerSettingActivity.class);
            context.startActivity(intentServer);
        }

    }
}
