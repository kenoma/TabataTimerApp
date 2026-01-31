package com.kenoma.tabatatimer;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.InputProcessor;
import com.kenoma.tabatatimer.misc.SoundEvent;
import com.kenoma.tabatatimer.misc.State;

public class TabataController implements InputProcessor {
    TabataModel model;
    private float artificalSlide = 0;

    public TabataController(TabataModel model) {
        this.model = model;
    }


    public void update(float delta) {
        if (delta > 1.0f && model.getCurrentState() != State.Before) {
            Gdx.app.debug("Delay", "Delay is " + delta + " sec.");
            model.restoreState(delta);
            return;
        }

        if (model.getCurrentSet() >= model.getSetsCount() ||
                (model.getCurrentSet() == model.getSetsCount() - 1 && model.getCurrentState() == State.Rest)) {

            model.setCurrentState(State.Before);
            model.resetTimer();
        }

        updateBackColor(delta);
        updateProgressBar(delta);
        updateTouchEvent(delta);
        updateArtificalSlide(delta);

        if (model.getCurrentTimerValue() <= delta) {
            Gdx.app.debug("state", "state change " + model.getCurrentState());
            model.vibrate();
            switch (model.getCurrentState()) {
                case Before:
                case Pause:

                    break;
                case Prepare:
                    model.playSound(SoundEvent.SoundEventWork);
                    model.setCurrentState(State.Work);
                    break;
                case Work:
                    if (model.getCurrentSet() + 1 >= model.getSetsCount())
                        model.playSound(SoundEvent.SoundEventFinish);
                    else
                        model.playSound(SoundEvent.SoundEventRest);
                    model.setCurrentState(State.Rest);
                    break;
                case Rest:
                    model.playSound(SoundEvent.SoundEventWork);
                    model.setCurrentState(State.Work);
                    model.proceedSet();
                    break;
            }
        }

        switch (model.getCurrentState()) {
            case Before:
                break;
            case Pause:
                break;
            case Prepare:
            case Rest:
                float cT = model.getCurrentTimerValue();
                if ((cT - 3) * (cT - delta - 3) < 0)
                    model.playSound(SoundEvent.SoundEventTick);
                if ((cT - 2) * (cT - delta - 2) < 0)
                    model.playSound(SoundEvent.SoundEventTick);
                if ((cT - 1) * (cT - delta - 1) < 0)
                    model.playSound(SoundEvent.SoundEventTick);
            case Work:
                model.addDeltaToCurrentTimerValue(-delta);
                break;
        }
    }

    private void updateArtificalSlide(float delta) {
        float w = Gdx.graphics.getWidth();
        if (Math.abs(artificalSlide) > 0.05f * w) {
            artificalSlide += -4.0f * delta * artificalSlide;
            touchDragged((int) (w - artificalSlide), (int) entryPointY, 0);
        } else if (artificalSlide != 0)
            panning = false;
    }


    private void updateProgressBar(float delta) {
        if (!panning) {
            float speed = Math.min(delta * 5.1415926f, 0.5f);
            float width = Gdx.graphics.getWidth() / (float) (model.getSetsCount() + 0.5f * (model.getSetsCount() / TabataModel.MaxTabatas));
            model.progressBarItemWidth += speed * (width - model.progressBarItemWidth);
        }
        model.animationTimerValue += 10.0f * delta * (model.getCurrentTimerValue() - model.animationTimerValue);
    }

    private void updateBackColor(float delta) {
        float[] cCol = model.getCurrentBackColor();
        float[] cTrg = model.getBackColorForState(model.getCurrentState());
        float speed = Math.min(delta * 3.1415926f, 0.5f);
        for (int i = 0; i < 3; i++)
            cCol[i] += speed * (cTrg[i] - cCol[i]);
    }


    private float entryPointX = 0;
    private float entryPointY = 0;
    private float entryProgressBarWidth = 0;
    private boolean panning = false;

    private boolean isTouch = false;
    private float touchTimer = 0;

    public void resetTouch() {
        model.setTapRad(0);
        isTouch = false;
    }

    private void updateTapCircle(float delta) {
        if (model.getTapRad() != 0 || (isTouch && touchTimer > 0)) {
            model.setTapX(entryPointX);
            model.setTapY(Gdx.graphics.getHeight() - entryPointY);
            float speedRate = (entryPointX < sbuttonWidth && entryPointY < sbuttonWidth)?2.0f:1.0f;
            float rad = model.getTapRad();
            if (isTouch)
                rad += speedRate * delta * (4.2f * Gdx.graphics.getHeight() - rad);
            else {
                rad -= delta * 3.14f * rad;
                if (rad < Gdx.graphics.getHeight() / 40.0f)
                    rad = 0;
            }

            model.setTapRad(rad);
        }
    }

    static int sbuttonWidth = (int) (2 * Gdx.graphics.getWidth() * 0.0625f);

    private void updateTouchEvent(float delta) {

        touchTimer += delta;
        State state = model.getCurrentState();

        if(isTouch && state==State.Before && entryPointX < sbuttonWidth && entryPointY < sbuttonWidth) {
            model.getApp().switchToSettingsMode(model.getScreenshot());
            return;
        }

        if (state != State.Before)
            updateTapCircle(delta);
        else
            model.setTapRad(0);

        if (isTouch && (touchTimer > TabataModel.TapTimeout)) {

            float[] cTrg, cCol;
            if (touchTimer - delta <= TabataModel.TapTimeout) {
                switch (state) {
                    case Prepare:
                    case Rest:
                    case Work:
                        Gdx.app.debug("touch", "Tap Color " + State.Pause);
                        cTrg = model.getBackColorForState(State.Pause);
                        model.setTapColor(cTrg);
                        break;
                    case Pause:
                        Gdx.app.debug("touch", "Tap Color " + State.Before);
                        cTrg = model.getBackColorForState(State.Before);
                        model.setTapColor(cTrg);
                        break;
                }
            }

            boolean isLongTap = (entryPointX > sbuttonWidth && entryPointY > sbuttonWidth)?
                    ((touchTimer - TabataModel.LongTapTimeout) * (touchTimer - delta - TabataModel.LongTapTimeout) <= 0):
                    ((touchTimer - TabataModel.LongTapTimeoutSetting) * (touchTimer - delta - TabataModel.LongTapTimeoutSetting) <= 0);

            if (isLongTap) {
                switch (state) {
                    case Before:
                        //if (entryPointX < sbuttonWidth && entryPointY < sbuttonWidth)
                        //    model.getApp().switchToSettingsMode();
                        break;
                    case Prepare:
                    case Rest:
                    case Work:
                        model.vibrate();
                        cTrg = model.getBackColorForState(State.Pause);
                        cCol = model.getCurrentBackColor();
                        System.arraycopy(cTrg, 0, cCol, 0, 3);
                        model.setTapRad(0.0f);
                        Gdx.app.debug("touch", "Switch state " + State.Pause);
                        model.setCurrentState(State.Pause);
                        model.setTapColor(model.getBackColorForState(State.Before));
                        break;
                    case Pause:
                        model.vibrate();
                        model.playSound(SoundEvent.SoundEventTap);
                        cTrg = model.getBackColorForState(State.Before);
                        cCol = model.getCurrentBackColor();
                        System.arraycopy(cTrg, 0, cCol, 0, 3);
                        Gdx.app.debug("touch", "Switch state " + State.Before);
                        model.setCurrentState(State.Before);
                        model.resetTimer();

                        break;
                }
            }
            if ((touchTimer - 2 * TabataModel.LongTapTimeout) *
                    (touchTimer - delta - 2 * TabataModel.LongTapTimeout) <= 0 && state == State.Pause) {
                isTouch = false;
                model.vibrate();
                model.playSound(SoundEvent.SoundEventTap);
                model.setTapRad(0.0f);
                Gdx.app.debug("touch", "Switch state " + State.Before);
                cTrg = model.getBackColorForState(State.Before);
                cCol = model.getCurrentBackColor();
                System.arraycopy(cTrg, 0, cCol, 0, 3);

                model.setCurrentState(State.Before);
                model.resetTimer();
                model.setTapColor(model.getBackColorForState(State.Before));
            }
        }

    }



    @Override
    public boolean keyDown(int keycode) {
        if (keycode == Input.Keys.BACK || keycode == Input.Keys.ESCAPE) {
            if (model.getCurrentState() == State.Before)
                Gdx.app.exit();
            else {
                model.setCurrentState(State.Before);
                model.resetTimer();
            }
        }
        return false;
    }

    @Override
    public boolean keyUp(int keycode) {
        return false;
    }

    @Override
    public boolean keyTyped(char character) {
        return false;
    }

    @Override
    public boolean touchDown(int x, int y, int pointer, int button) {
        //ScreenshotFactory.saveScreenshot();
        Gdx.app.debug("touch", "touch down");

        entryPointX = x;
        entryPointY = y;
        int sbuttonWidth = (int) (Gdx.graphics.getWidth() * 0.0625f);
        switch (model.getCurrentState()) {
            case Prepare:
            case Rest:
            case Work:
                model.setTapColor(model.getBackColorForState(State.Pause));
                break;
            case Pause:
                model.setTapColor(model.getBackColorForState(State.Before));
                break;
            case Before:
                artificalSlide = 0;
                model.setTapColor(model.getBackColorForState(State.Settings));
                if (x < 2 * sbuttonWidth && y < 2 * sbuttonWidth) {
                    entryPointX = sbuttonWidth;
                    entryPointY = sbuttonWidth;
                }
                break;
        }


        entryProgressBarWidth = model.progressBarItemWidth;

        isTouch = true;
        touchTimer = 0;
        return false;
    }

    @Override
    public boolean touchUp(int screenX, int screenY, int pointer, int button) {
        Gdx.app.debug("touch", "touch " + touchTimer + " sec");

        if ((model.getCurrentState() == State.Before && entryPointX < sbuttonWidth && entryPointY < sbuttonWidth))
            return false;
        isTouch = false;

        if (!panning && touchTimer < TabataModel.TapTimeout) {
            Gdx.app.debug("touch", "short tap detected");
            //model.setTapColor(model.getBackColorForState(States.Before));
            switch (model.getCurrentState()) {
                case Before:
                    if (entryPointX > sbuttonWidth && entryPointY > sbuttonWidth) {
                        model.playSound(SoundEvent.SoundEventTap);
                        if (screenY < 0.9f * Gdx.graphics.getHeight()) {
                            model.setCurrentState(State.Prepare);
                            model.proceedSet();
                            model.setTapRad(0);
                        } else
                            artificalSlide = Gdx.graphics.getWidth() - entryPointX;
                    }
                    break;
                case Pause:

                    model.setCurrentState(State.Resume);
                    break;
            }
        }
        touchTimer = 0;
        panning = false;
        return false;
    }

    @Override
    public boolean touchDragged(int x, int y, int pointer) {
        if (entryPointX < sbuttonWidth && entryPointY < sbuttonWidth)
            return false;

        float d = Math.abs(x - entryPointX) + Math.abs(y - entryPointY);
        if (d > 44) {
            panning = true;
            isTouch = false;
        }
        if (model.getCurrentState() == State.Before) {
            float maxWidth = Gdx.graphics.getWidth() / 4.0f;
            float minWidth = Gdx.graphics.getWidth() / (TabataModel.MaxTabatas+0.5f);
            float newWidth = Math.max(Math.min(entryProgressBarWidth * x / entryPointX, maxWidth), minWidth);
            model.progressBarItemWidth = newWidth;
            int sets = Math.round(Gdx.graphics.getWidth() / newWidth);

            if (sets < 4)
                sets = 4;
            if (sets > TabataModel.MaxTabatas)
                sets = TabataModel.MaxTabatas;
            model.recomputeTimeIntervals((TabataModel.BaseWorkDuration + TabataModel.BaseRestDuration) * sets);

            System.out.println("pan detected: " + sets);
        }
        return false;
    }

    @Override
    public boolean mouseMoved(int screenX, int screenY) {
        return false;
    }

    @Override
    public boolean scrolled(int amount) {
        return false;
    }
}
