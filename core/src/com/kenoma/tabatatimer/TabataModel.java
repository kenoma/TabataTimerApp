package com.kenoma.tabatatimer;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Preferences;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator;
import com.badlogic.gdx.utils.I18NBundle;
import com.kenoma.tabatatimer.misc.NotificationsInstructions;
import com.kenoma.tabatatimer.misc.SoundEvent;
import com.kenoma.tabatatimer.misc.State;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * Created by Kenoma on 11.03.2015.
 */
public class TabataModel {
    public static final float TapTimeout = 0.20f;
    public static final float LongTapTimeout = 0.8f;
    public static final float LongTapTimeoutSetting = 0.33f;

    public static final int MinTabatas = 4;
    public static final int MaxTabatas = 10;

    public float progressBarItemWidth = 0;
    public float animationTimerValue = 240;

    private Main app;

    private boolean isSoundEnabled = true;
    private boolean isVibroEnabled = true;
    //private boolean isNotificationsEnabled=true;

    private int setsCount = 8;
    private int currentSet = -1;
    private State currentState = State.Before;
    private Map<State, Float> duration;
    private Map<State, float[]> backColors;
    private float[] currentBackColor;

    private float currentTimerValue = 240;

    private BitmapFont font_header;
    private BitmapFont font_body;
    private BitmapFont font_footer, font_footer_active;
    public float setTransitionProgress = 0.4f;
    static public float BasePrepareDuration = 10;
    static public float BaseWorkDuration = 20;
    static public float BaseRestDuration = 10;

    public Texture getSettingsButton() {
        return settingsButton;
    }

    private Texture settingsButton;
    I18NBundle bundle;

    private float tapX;
    private float tapY;
    private float tapRad;
    private float[] tapColor;

    Sound soundWork;
    Sound soundRest;
    Sound soundFinish;
    Sound soundTick;
    Sound soundTap;

    public TabataModel() {
        initFonts();
        initSettingsButton();
        FileHandle baseFileHandle = Gdx.files.internal("data/i18n/Bundle");
        Locale locale = Locale.getDefault();
        bundle = I18NBundle.createBundle(baseFileHandle, locale);

        duration = new HashMap<State, Float>();
        duration.put(State.Before, 240.0f);
        duration.put(State.Pause, 0.001f);
        duration.put(State.Prepare, 10.0f);
        duration.put(State.Rest, 10.0f);
        duration.put(State.Work, 20.0f);

        backColors = new HashMap<State, float[]>();
        backColors.put(State.Before, new float[]{52.0f / 256.0f, 73.0f / 256.0f, 94.0f / 256.0f});
        backColors.put(State.Pause, new float[]{52.0f / 256.0f, 153.0f / 256.0f, 219.0f / 256.0f});
        backColors.put(State.Prepare, new float[]{230.0f / 256.0f, 126.0f / 256.0f, 34.0f / 256.0f});
        backColors.put(State.Rest, new float[]{46.0f / 256.0f, 204.0f / 256.0f, 113.0f / 256.0f});
        backColors.put(State.Work, new float[]{231.0f / 256.0f, 76.0f / 256.0f, 60.0f / 256.0f});
        backColors.put(State.Settings, new float[]{1, 1, 1});

        currentBackColor = backColors.get(State.Before).clone();

        tapX = 0;
        tapY = 0;
        tapRad = 0;
        tapColor = getBackColorForState(State.Before);
        soundWork = Gdx.audio.newSound(Gdx.files.internal("sound/work.mp3"));
        soundRest = Gdx.audio.newSound(Gdx.files.internal("sound/rest.mp3"));
        soundFinish = Gdx.audio.newSound(Gdx.files.internal("sound/finish.mp3"));
        soundTick = Gdx.audio.newSound(Gdx.files.internal("sound/tick.mp3"));
        soundTap = Gdx.audio.newSound(Gdx.files.internal("sound/tap.mp3"));

        Preferences prefs = Gdx.app.getPreferences("tabatatimer");
        setsCount = prefs.getInteger("setsCount", 8);
    }

    private void initSettingsButton() {
        int buttonWidth = (int) (Gdx.graphics.getWidth() * 0.0625f);

        Pixmap pixmap = new Pixmap(buttonWidth, buttonWidth, Pixmap.Format.RGBA8888);
        Pixmap.setBlending(Pixmap.Blending.None);
        pixmap.setColor(1, 1, 1, 0.1f);
        pixmap.fillCircle(buttonWidth / 2, buttonWidth / 2, buttonWidth / 2 - 1);
        settingsButton = new Texture(pixmap);
        pixmap.dispose();
    }

    private void initFonts() {
        int screenWidth = Gdx.graphics.getWidth();

        FreeTypeFontGenerator generator = new FreeTypeFontGenerator(Gdx.files.internal("data/fonts/light.ttf"));
        FreeTypeFontGenerator.FreeTypeFontParameter parameter = new FreeTypeFontGenerator.FreeTypeFontParameter();
        parameter.size = 36 * screenWidth / 640;
        font_header = generator.generateFont(parameter);
        generator.dispose();

        generator = new FreeTypeFontGenerator(Gdx.files.internal("data/fonts/ulight.ttf"));
        parameter = new FreeTypeFontGenerator.FreeTypeFontParameter();
        parameter.size = 216 * screenWidth / 640;
        parameter.characters = "0123456789:";

        font_body = generator.generateFont(parameter);
        font_body.setFixedWidthGlyphs("0123456789");
        generator.dispose();

        generator = new FreeTypeFontGenerator(Gdx.files.internal("data/fonts/thin.ttf"));
        parameter = new FreeTypeFontGenerator.FreeTypeFontParameter();
        parameter.size = 48 * screenWidth / 640;
        parameter.characters = "0123456789";
        font_footer = generator.generateFont(parameter);
        generator.dispose();

        generator = new FreeTypeFontGenerator(Gdx.files.internal("data/fonts/light.ttf"));
        parameter = new FreeTypeFontGenerator.FreeTypeFontParameter();
        parameter.size = 48 * screenWidth / 640;
        parameter.characters = "0123456789";
        //parameter.borderColor = Color.WHITE;
        //parameter.borderWidth=1;
        //parameter.kerning=true;
        font_footer_active = generator.generateFont(parameter);
        generator.dispose();
    }

    public void resetTimer() {
        currentSet = -1;
        Preferences prefs = Gdx.app.getPreferences("tabatatimer");
        setsCount = prefs.getInteger("setsCount", 8);
        currentTimerValue = setsCount * (BaseRestDuration + BaseWorkDuration);
        currentState = State.Before;
    }

    public BitmapFont getFontHeader() {
        return font_header;
    }

    public BitmapFont getFontBody() {
        return font_body;
    }

    public BitmapFont getFontFooter() {
        return font_footer;
    }

    public BitmapFont getFontFooterActive() {
        return font_footer_active;
    }

    public State getCurrentState() {
        return currentState;
    }

    State savedState;

    public void setCurrentState(State newState) {
        if (newState == State.Pause) {
            savedState = this.currentState;
            this.currentState = newState;
        } else if (newState == State.Resume) {
            this.currentState = savedState;
        } else {
            currentTimerValue = (float) duration.get(newState);
            this.currentState = newState;
        }
    }

    public void dispose() {
        font_body.dispose();
        font_footer.dispose();
        font_footer_active.dispose();
        font_header.dispose();
        soundRest.dispose();
        soundWork.dispose();
        soundFinish.dispose();
        soundTick.dispose();
        soundTap.dispose();
        settingsButton.dispose();
    }

    public float getCurrentTimerValue() {
        return currentTimerValue;
    }

    public void addDeltaToCurrentTimerValue(float delta) {
        this.currentTimerValue += delta;
    }

    public void recomputeTimeIntervals(float overallDuration) {
        float tabatacCycleDur = BaseWorkDuration + BaseRestDuration;
        if (overallDuration < MinTabatas * tabatacCycleDur)
            overallDuration = MinTabatas * tabatacCycleDur + BaseRestDuration;
        if (overallDuration > MaxTabatas * tabatacCycleDur)
            overallDuration = MaxTabatas * tabatacCycleDur + BaseRestDuration;

        currentTimerValue = overallDuration;
        setsCount = (int) (overallDuration / tabatacCycleDur);
        Preferences prefs = Gdx.app.getPreferences("tabatatimer");
        prefs.putInteger("setsCount", setsCount);
        prefs.flush();
        float extra = (overallDuration - setsCount * tabatacCycleDur) / (setsCount * 3.0f);

        duration.put(State.Prepare, BasePrepareDuration + extra);
        duration.put(State.Work, BaseWorkDuration + extra);
        duration.put(State.Rest, BaseRestDuration + extra);
    }

    public int getSetsCount() {
        return setsCount;
    }

    public void addSets(int value) {
        setsCount += value;
    }

    public int getCurrentSet() {
        return currentSet;
    }

    public void proceedSet() {
        currentSet += 1;
        setTransitionProgress = 0.4f;
    }

    public I18NBundle getBundle() {
        return bundle;
    }

    public float[] getBackColorForState(State state) {
        if (state == State.Resume)
            return backColors.get(savedState);
        else
            return backColors.get(state);
    }

    public float[] getCurrentBackColor() {
        return currentBackColor;
    }

    public float getTapRad() {
        return tapRad;
    }

    public void setTapRad(float tapRad) {
        this.tapRad = tapRad;
    }

    public float getTapX() {
        return tapX;
    }

    public void setTapX(float tapX) {
        this.tapX = tapX;
    }

    public float getTapY() {
        return tapY;
    }

    public void setTapY(float tapY) {
        this.tapY = tapY;
    }

    public void playSound(SoundEvent soundEvent) {
        Gdx.app.debug("sound", "play " + soundEvent + " " + isSoundEnabled);
        if (isSoundEnabled) {
            switch (soundEvent) {
                case SoundEventWork:
                    soundWork.play();
                    break;
                case SoundEventRest:
                    soundRest.play();
                    break;
                case SoundEventFinish:
                    soundFinish.play();
                    break;
                case SoundEventTick:
                    soundTick.play();
                    break;
                case SoundEventTap:
                    soundTap.play();
                    break;
            }
        }
    }

    public void vibrate() {
        Gdx.app.debug("vibro", "Vibration " + isVibroEnabled);
        if (isVibroEnabled)
            Gdx.input.vibrate(2000);
    }

    List<NotificationsInstructions> notInstructions;

    public void restoreState(float bigDelta) {
        Gdx.app.debug("restoring", "BigDelta is " + bigDelta);
        if (notInstructions != null) {
            boolean isRestored = false;
            for (NotificationsInstructions ni : notInstructions)
                if (ni.currentState != State.Tick) {
                    Gdx.app.debug("restoring", "Instruction " + ni.currentSet + " State " + ni.currentState + " bigDelta " + bigDelta + "STF " + ni.seconsToFire);
                    int stf = ni.seconsToFire + (ni.currentState == State.Rest ? 3 : 0);
                    if (bigDelta - stf < 0) {
                        currentTimerValue = stf - bigDelta;
                        currentState = ni.currentState;
                        currentSet = ni.currentSet;
                        isRestored = true;
                        break;
                    }
                    //bigDelta -= ni.seconsToFire;
                }

            if (!isRestored) {
                currentState = State.Before;
                currentTimerValue = 0;
                currentSet = setsCount + 1;
            }
        }
    }

    public List<NotificationsInstructions> getNotificationsScheme() {
        // Gdx.app.debug("notifications", "Start generating list - "+isNotificationsEnabled);

        if (currentState == State.Before) {
            Gdx.app.debug("notifications", "No list");
            return new ArrayList<NotificationsInstructions>();

        } else {
            notInstructions = new ArrayList<NotificationsInstructions>();
            State tmp_state = currentState;
            int secondsToLaunch = 0;
            boolean isRemaining = true;
            for (int set = currentSet; set < setsCount; set++) {
                Gdx.app.debug("notifications", "set " + set + " state " + tmp_state);
                if (tmp_state == State.Rest || tmp_state == State.Prepare) {
                    secondsToLaunch += isRemaining ? currentTimerValue : duration.get(State.Rest);
                    isRemaining = false;
                    tmp_state = State.Work;

                    NotificationsInstructions ins = new NotificationsInstructions();
                    ins.seconsToFire = secondsToLaunch - 3;
                    ins.Phase = bundle.get("header_work") + " " + (set + 1);
                    ins.sound = "ticktack";
                    ins.currentState = State.Rest;
                    ins.currentSet = set;
                    ins.vibrate = true;
                    notInstructions.add(ins);
                }
                if (tmp_state == State.Work) {
                    secondsToLaunch += isRemaining ? currentTimerValue : duration.get(State.Work);
                    isRemaining = false;
                    tmp_state = State.Rest;
                    NotificationsInstructions ins = new NotificationsInstructions();
                    ins.seconsToFire = secondsToLaunch;
                    if (set == setsCount - 1) {
                        ins.Phase = bundle.get("message_done");
                        ins.sound = "finish";
                    } else {
                        ins.Phase = bundle.get("header_rest") + " " + (set + 1);
                        ins.sound = "rest";
                    }
                    ins.currentState = State.Work;
                    ins.currentSet = set;
                    ins.vibrate = true;
                    notInstructions.add(ins);
                }
            }
            return notInstructions;
        }
    }

    public float[] getTapColor() {
        return tapColor;
    }

    public void setTapColor(float[] tapColor) {
        this.tapColor = tapColor;
    }

    public Main getApp() {
        return app;
    }

    public void setApp(Main app) {
        this.app = app;
    }

    public void setIsSoundEnabled(boolean isSoundEnabled) {
        this.isSoundEnabled = isSoundEnabled;
    }

    public void setIsVibroEnabled(boolean isVibroEnabled) {
        this.isVibroEnabled = isVibroEnabled;
    }


    public Texture getScreenshot()
    {
        return app.tabataView.takeScreenshot();
    }

}
