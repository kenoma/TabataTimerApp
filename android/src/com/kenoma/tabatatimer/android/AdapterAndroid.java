package com.kenoma.tabatatimer.android;

import android.app.Activity;
import android.app.Application;
import android.app.Notification;
import android.app.NotificationManager;
import android.content.Context;
import android.net.Uri;
import android.os.SystemClock;

import com.kenoma.tabatatimer.misc.NotificationsHandler;
import com.kenoma.tabatatimer.misc.NotificationsInstructions;

import java.util.List;

public class AdapterAndroid implements NotificationsHandler
{
    private Activity gameActivity;

    public AdapterAndroid(Activity gameActivity)
    {
        this.gameActivity = gameActivity;
    }

    @Override
    public void prepareNotifications(List<NotificationsInstructions> instructions)
    {

    }

    @Override
    public void cancelAllNotifications()
    {

    }
}