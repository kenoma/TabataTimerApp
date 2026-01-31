package com.kenoma.tabatatimer;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Preferences;
import com.badlogic.gdx.backends.iosrobovm.IOSApplication;
import com.badlogic.gdx.backends.iosrobovm.IOSApplicationConfiguration;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.utils.I18NBundle;

import org.robovm.apple.foundation.NSAutoreleasePool;
import org.robovm.apple.glkit.GLKViewDrawableMultisample;
import org.robovm.apple.uikit.UIAlertView;
import org.robovm.apple.uikit.UIAlertViewDelegate;
import org.robovm.apple.uikit.UIAlertViewStyle;
import org.robovm.apple.uikit.UIApplication;
import org.robovm.apple.uikit.UIUserNotificationSettings;
import org.robovm.apple.uikit.UIUserNotificationType;

import java.util.Locale;

public class IOSLauncher extends IOSApplication.Delegate implements UIAlertViewDelegate
{

    @Override
    protected IOSApplication createApplication()
    {
        IOSApplicationConfiguration config = new IOSApplicationConfiguration();
        config.orientationLandscape = false;
        config.orientationPortrait = true;
        config.preventScreenDimming = true;
        config.allowIpod = true;
        config.multisample = GLKViewDrawableMultisample._4X;

        Main game = new Main();
        AdapterIOS adapter = new AdapterIOS();
        game.setNotificationHandler(adapter);
        IOSApplication app = new IOSApplication(game, config);

        System.out.println("Enter to lp");
        Preferences prefs = app.getPreferences("tabatatimer");
        if (prefs.getBoolean("showIOSNotifInfo", true))
        {
            System.out.println("1");

            ///TODO: MAKE LOCALIZATIONABLE STRINGS
            UIAlertView alert = new UIAlertView();
            alert.setAlertViewStyle(UIAlertViewStyle.Default);
            alert.setTitle("Push Notifications required");
            alert.setMessage("For Tabata Timer to function when set to run in the background, please allow Push Notifications in the following screen");
            alert.addButton("Ok");
            System.out.println("3");

            alert.setDelegate(this);
            alert.show();
            System.out.println("5");

            System.out.println("Show info notification");
        } else
            System.out.println("User already agreed with notifications");

        System.out.println("6");

        String os = java.lang.System.getProperty("os.version");
        if (!prefs.getBoolean("showIOSNotifInfo", true) && os != null && !os.contains("6.") && !os.contains("7."))
        {
            //UIUserNotificationSettings uiUserNotificationSettings = new UIUserNotificationSettings();

             //UIUserNotificationSettings.
            //        create(UIUserNotificationType.Alert, null);
           UIApplication.getSharedApplication().
                    registerUserNotificationSettings(UIApplication.getSharedApplication().getCurrentUserNotificationSettings());
            //UIApplication.getSharedApplication().registerUserNotificationSettings(UIUserNotificationSettings.create(UIUserNotificationType.Sound, null));
            //UIApplication.getSharedApplication().registerUserNotificationSettings(UIUserNotificationSettings.create(UIUserNotificationType.Badge, null));
        }

        return app;
    }

    @Override
    public void didFinishLaunching(UIApplication application)
    {
        System.out.println("didFinishLaunching");
        super.didFinishLaunching(application);
    }

    public static void main(String[] argv)
    {
        NSAutoreleasePool pool = new NSAutoreleasePool();
        UIApplication.main(argv, null, IOSLauncher.class);
        pool.close();
    }

    @Override
    public void clicked(UIAlertView alertView, long buttonIndex) {
        if (buttonIndex == 0)
        {
            System.out.println("Button Yes clicked");
            Preferences prefs =  Gdx.app.getPreferences("tabatatimer");
            prefs.putBoolean("showIOSNotifInfo", false);
            prefs.flush();
            String os = java.lang.System.getProperty("os.version");
            if (os != null && !os.contains("6.") && !os.contains("7."))
            {
                UIApplication.getSharedApplication().
                        registerUserNotificationSettings(UIApplication.getSharedApplication().getCurrentUserNotificationSettings());
                //UIApplication.getSharedApplication().registerUserNotificationSettings(UIUserNotificationSettings.create(UIUserNotificationType.Alert, null));
                //UIApplication.getSharedApplication().registerUserNotificationSettings(UIUserNotificationSettings.create(UIUserNotificationType.Sound, null));
                //UIApplication.getSharedApplication().registerUserNotificationSettings(UIUserNotificationSettings.create(UIUserNotificationType.Badge, null));
            }
        }
    }

    @Override
    public void cancel(UIAlertView alertView) {

    }

    @Override
    public void willPresent(UIAlertView alertView) {

    }

    @Override
    public void didPresent(UIAlertView alertView) {

    }

    @Override
    public void willDismiss(UIAlertView alertView, long buttonIndex) {

    }

    @Override
    public void didDismiss(UIAlertView alertView, long buttonIndex) {

    }

    @Override
    public boolean shouldEnableFirstOtherButton(UIAlertView alertView) {
        return false;
    }
}