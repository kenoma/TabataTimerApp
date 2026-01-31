package com.kenoma.tabatatimer;

import com.badlogic.gdx.Application;
import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Preferences;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.scenes.scene2d.utils.SpriteDrawable;
import com.kenoma.tabatatimer.misc.NotificationsHandler;

import java.util.Timer;
import java.util.TimerTask;


public class Main extends Game {
    TabataModel model;
    TabataController controller;

    NotificationsHandler notificationHandler;
    float backgroundDelta = 0.500f;
    Timer backgroundTimer = null;
    TimerTask backgroundTask = null;

    SettingsView settingsView;
    TabataView tabataView;

    @Override
    public void create() {
        Preferences prefs = Gdx.app.getPreferences("tabatatimer");

        model = new TabataModel();
        model.setIsSoundEnabled(prefs.getBoolean("sound", true));
        model.setIsVibroEnabled(prefs.getBoolean("vibro", true));

        controller = new TabataController(model);
        tabataView = new TabataView(this, controller, model);
        settingsView = new SettingsView(this, model);
        setScreen(tabataView);

        Gdx.app.setLogLevel(Application.LOG_DEBUG);
        Gdx.app.debug("OS", "OS:" + java.lang.System.getProperty("os.version"));
    }

    public void switchToExerciseMode() {
        controller.resetTouch();
        setScreen(tabataView);
    }

    public void switchToSettingsMode(Texture screenShot) {
        settingsView.reset(screenShot);
        setScreen(settingsView);
    }

    @Override
    public void render() {
        super.render();
    }

    @Override
    public void resize(int width, int height) {
        super.resize(width, height);
        //Log.d("main", "render");
    }

    @Override
    public void pause() {
        super.pause();
        Gdx.app.debug("state", "pause");

        if (Gdx.app.getType() == Application.ApplicationType.iOS && notificationHandler != null)
            notificationHandler.prepareNotifications(model.getNotificationsScheme());
        if (Gdx.app.getType() == Application.ApplicationType.Android) {
            if (backgroundTimer != null) {
                backgroundTask.cancel();
                backgroundTimer.cancel();
                backgroundTimer = null;
            }
            backgroundTimer = new Timer();
            backgroundTask = new TimerTask() {
                @Override
                public void run() {
                    controller.update(backgroundDelta);
                }
            };
            backgroundTimer.schedule(backgroundTask, 0, (long) (1000 * backgroundDelta));
        }
    }

    @Override
    public void resume() {
        super.resume();
        Gdx.app.debug("state", "resume");
        if (Gdx.app.getType() == Application.ApplicationType.iOS && notificationHandler != null)
            notificationHandler.cancelAllNotifications();
        if (Gdx.app.getType() == Application.ApplicationType.Android) {
            if (backgroundTimer != null) {
                backgroundTask.cancel();
                backgroundTimer.cancel();
                backgroundTimer = null;
            }
        }
    }

    public void dispose() {
        model.dispose();


    }

    public void setNotificationHandler(NotificationsHandler handler) {
        this.notificationHandler = handler;
    }

}
