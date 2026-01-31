package com.kenoma.tabatatimer;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.FrameBuffer;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.viewport.ScreenViewport;
import com.kenoma.tabatatimer.misc.State;

public class TabataView implements Screen {
    TabataController controller;
    TabataModel model;
    final Stage stage;
    final Label header, body;
    final Label.LabelStyle headerStyle, bodyStyle;
    final ShapeRenderer shapeRenderer;
    final SpriteBatch batch;
    final Table tbl;
    final FrameBuffer buffer;

    public TabataView(Main app, TabataController controller, TabataModel model) {
        batch = new SpriteBatch();
        shapeRenderer = new ShapeRenderer();
        model.resetTimer();
        model.setApp(app);
        this.controller = controller;
        this.model = model;

        stage = new Stage(new ScreenViewport());

        headerStyle = new Label.LabelStyle(model.getFontHeader(), Color.WHITE);
        bodyStyle = new Label.LabelStyle(model.getFontBody(), Color.WHITE);

        header = new Label("BEFO-STATE", headerStyle);
        header.setColor(Color.WHITE);
        header.setText("");

        body = new Label(" 00:00 ", bodyStyle);

        body.setColor(Color.WHITE);
        body.setAlignment(Align.center);


        Skin uiSkin = new Skin();
        tbl = new Table(uiSkin);
        //tbl.debug();
        tbl.setFillParent(true);
        tbl.align(Align.top);

        tbl.add(header).align(Align.center).pad(20, 0, 0, 0);
        tbl.row();

        float pad_top = Gdx.graphics.getHeight() / 4 + body.getStyle().font.getCapHeight() / 2;
        tbl.add(body).width(Gdx.graphics.getWidth()).expand().pad(0, 0, pad_top, 0).align(Align.center);
        tbl.row();

        this.stage.addActor(tbl);
        buffer = new FrameBuffer(Pixmap.Format.RGBA8888, Gdx.graphics.getWidth(), Gdx.graphics.getHeight(), false);
    }

    @Override
    public void show() {
        Gdx.input.setInputProcessor(controller);
        Gdx.input.setCatchBackKey(true);
    }

    Texture currentScreen;
    @Override
    public void render(float delta) {
        controller.update(delta);
        float[] cCol = model.getCurrentBackColor();

        if (model.getCurrentState() == State.Before)
            buffer.begin();
        Gdx.graphics.getGL20().glClearColor(cCol[0], cCol[1], cCol[2], 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
        drawTapShape();
        drawText();
        batch.begin();
        drawSettingsButton();
        drawProgressBar(delta);
        batch.end();
        stage.act(delta);
        stage.draw();

        if (model.getCurrentState() == State.Before) {
            buffer.end();
            currentScreen = buffer.getColorBufferTexture();
            batch.begin();
            batch.draw(currentScreen, 0, 0, Gdx.graphics.getWidth(), Gdx.graphics.getHeight()
                    , 0, 0, Gdx.graphics.getWidth(), Gdx.graphics.getHeight(), false, true);
            batch.end();
        }
    }

    private void drawTapShape() {
        if (model.getTapRad() != 0) {
            Gdx.gl.glEnable(GL20.GL_BLEND);
            Gdx.gl.glBlendFunc(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);
            float[] cCol = model.getTapColor();
            shapeRenderer.setColor(cCol[0], cCol[1], cCol[2], model.getCurrentState()== State.Before?1.0f:
                    model.getTapRad() / (2.2f * Math.max(Gdx.graphics.getWidth(), Gdx.graphics.getHeight())));
            //shapeRenderer.setColor(Color.BLACK);
            shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
            shapeRenderer.circle(model.getTapX(), model.getTapY(), model.getTapRad());
            shapeRenderer.end();
            Gdx.gl.glDisable(GL20.GL_BLEND);
        }
    }

    private long pauseDelayBlinking = 0;

    private void drawText() {
        State state = model.getCurrentState();
        if (state != State.Pause)
            pauseDelayBlinking = System.currentTimeMillis();

        switch (state) {
            case Before:
                body.setVisible(true);
                header.setText(model.getBundle().format("header_before", model.getSetsCount()));
                header.setVisible(false);
                break;
            case Prepare:
                body.setVisible(true);
                header.setText(model.getBundle().get("header_prepare"));
                header.setVisible(true);
                break;
            case Work:
                body.setVisible(true);
                header.setText(model.getBundle().get("header_work"));
                break;
            case Rest:
                body.setVisible(true);
                header.setText(model.getBundle().get("header_rest"));
                break;
            case Pause:
                header.setText(model.getBundle().get("header_pause"));
                long curTime = System.currentTimeMillis();
                if (curTime - pauseDelayBlinking > 1500 && curTime % 1000 < 500)
                    body.setVisible(false);
                else
                    body.setVisible(true);
                break;
        }

        float timerv = state == State.Before ? model.animationTimerValue + 0.5f : model.getCurrentTimerValue();
        int minutes = (int) (timerv / 60);
        int seconds = (int) (timerv - minutes * 60);
        body.setText(String.format(" %02d", minutes) + ":" + String.format("%02d ", seconds + (state == State.Before ? 0 : 1)));
    }

    private void drawSettingsButton() {
        if (model.getCurrentState() == State.Before) {
            int buttonWidth = (int) (Gdx.graphics.getWidth() * 0.0625f);
            if (model.getTapRad() < buttonWidth / 2)
                batch.draw(model.getSettingsButton(), buttonWidth / 2, Gdx.graphics.getHeight() - 3 * buttonWidth / 2);
        }
    }


    private void drawProgressBar(float delta) {
        float width = model.progressBarItemWidth;
        float fheight = model.getFontFooter().getCapHeight();
        float height = fheight + (Gdx.graphics.getHeight() * 0.1f - fheight) / 2.0f;

        BitmapFont barFont;

        for (int set = 0; set < TabataModel.MaxTabatas; set++) {
            if (set == model.getCurrentSet())
                barFont = model.getFontFooterActive();
            else
                barFont = model.getFontFooter();

            if (set < model.getCurrentSet() || model.getCurrentState() == State.Before)
                barFont.setColor(1, 1, 1, 1);
            else if (set == model.getCurrentSet()) {
                model.setTransitionProgress += delta * (1.0f - model.setTransitionProgress);
                barFont.setColor(1, 1, 1, model.setTransitionProgress);
            } else
                barFont.setColor(1, 1, 1, 0.4f);
            String text = " " + (set + 1) + " ";
            float text_width = 0.5f * barFont.getSpaceWidth() * text.length();
            barFont.draw(batch, text, set * width + 0.5f * width - text_width, height);
        }

    }

    @Override
    public void resize(int width, int height) {
        stage.getViewport().update(width, height, true);
        tbl.setSize(Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
    }

    @Override
    public void pause() {
        Gdx.app.debug("state", "pause");
    }

    @Override
    public void resume() {
        Gdx.app.debug("state", "resume");
    }

    @Override
    public void hide() {
        Gdx.app.debug("state", "hide");
    }

    @Override
    public void dispose() {
        batch.dispose();
        stage.dispose();
        shapeRenderer.dispose();
        buffer.dispose();
    }

    public Texture takeScreenshot() {
        return currentScreen;
    }
}
