package com.kenoma.tabatatimer;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Preferences;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.utils.I18NBundle;
import com.kenoma.tabatatimer.misc.NotificationsHandler;
import com.kenoma.tabatatimer.misc.NotificationsInstructions;

import org.robovm.apple.foundation.NSDate;
import org.robovm.apple.foundation.NSOperationQueue;
import org.robovm.apple.foundation.NSTimeZone;
import org.robovm.apple.uikit.UIAlertView;
import org.robovm.apple.uikit.UIAlertViewDelegate;
import org.robovm.apple.uikit.UIAlertViewStyle;
import org.robovm.apple.uikit.UIApplication;
import org.robovm.apple.uikit.UILocalNotification;
import org.robovm.apple.uikit.UIUserNotificationSettings;
import org.robovm.apple.uikit.UIUserNotificationType;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class AdapterIOS implements NotificationsHandler {
    //List<UILocalNotification> notificationList = new ArrayList<>();

    public AdapterIOS() {
        UIApplication.getSharedApplication().setApplicationIconBadgeNumber(0);
        //UIApplication.getSharedApplication().cancelAllLocalNotifications();
    }


    @Override
    public void prepareNotifications(final List<NotificationsInstructions> instructions) {
        //notificationList.clear();
        Gdx.app.debug("AdapterIOS", "Placing instructions to notification queue");
        NSOperationQueue.getMainQueue().addOperation(new Runnable() {
            @Override
            public void run() {
                for (final NotificationsInstructions notf : instructions) {
                    NSDate date = new NSDate();

                    NSDate secondsMore = date.newDateByAddingTimeInterval(notf.seconsToFire);

                    UILocalNotification localNotification = new UILocalNotification();

                    localNotification.setFireDate(secondsMore);
                    localNotification.setApplicationIconBadgeNumber(0);
                    localNotification.setAlertBody(notf.Phase);
                    localNotification.setSoundName(notf.sound + ".aif");
                    localNotification.setTimeZone(NSTimeZone.getDefaultTimeZone());
                    //localNotification.setApplicationIconBadgeNumber(UIApplication.getSharedApplication().getApplicationIconBadgeNumber() + 1);
                    //notificationList.add(localNotification);
                    UIApplication.getSharedApplication().scheduleLocalNotification(localNotification);

                }
            }
        });
    }

    @Override
    public void cancelAllNotifications() {
        UIApplication.getSharedApplication().cancelAllLocalNotifications();
        /*for (UILocalNotification localNotification : notificationList) {
            UIApplication.getSharedApplication().cancelLocalNotification(localNotification);
            localNotification.release();
            localNotification.dispose();
        }
        notificationList.clear();*/
    }
}