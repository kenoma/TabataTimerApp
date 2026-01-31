package com.kenoma.tabatatimer;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.Preferences;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.glutils.FrameBuffer;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.CheckBox;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.scenes.scene2d.utils.SpriteDrawable;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.I18NBundle;
import com.badlogic.gdx.utils.viewport.ScreenViewport;
import com.kenoma.tabatatimer.misc.MaskedTapShader;
import com.kenoma.tabatatimer.misc.SoundEvent;

import java.util.Locale;

import javax.xml.soap.Text;

public class SettingsView implements Screen {
    final Table tbl;
    final Stage stage;
    final BitmapFont titleFont, textFont;
    final Main app;

    final ShapeRenderer shapeRenderer;
    Texture screenshotExercise, screenshotSettings;
    SpriteBatch batch, backBatch;
    private float touchTimer = 0;
    final FrameBuffer buffer;
    float tapRad = 0;

    public void reset(Texture screenShot) {
        screenshotExercise = screenShot;
        tapRad = 0;
        isTouch = false;
        tbl.setColor(1, 1, 1, 0);
    }

    MaskedTapShader maskedTapShader = null;
    private boolean isTouch = false, isBirth = false;
    TextureAtlas atlas;
    Texture blackLine, settingsButton;
    TabataModel model;

    public SettingsView(final Main app, final TabataModel model) {
        this.model = model;
        int screenWidth = Gdx.graphics.getWidth();
        int screenHeigh = Gdx.graphics.getHeight();
        buffer = new FrameBuffer(Pixmap.Format.RGBA8888, screenWidth, screenHeigh, false);
        Pixmap pixmap = new Pixmap(Gdx.graphics.getWidth(), 1, Pixmap.Format.RGBA8888);
        pixmap.setColor(0.8f, 0.8f, 0.8f, 1);
        pixmap.fill();
        blackLine = new Texture(pixmap);
        pixmap.dispose();

        int buttonWidth = (int) (Gdx.graphics.getWidth() * 0.0625f);
        pixmap = new Pixmap(buttonWidth, buttonWidth, Pixmap.Format.RGBA8888);
        Pixmap.setBlending(Pixmap.Blending.None);
        pixmap.setColor(1, 1, 1, 1);
        pixmap.fill();
        pixmap.setColor(52.0f / 256f, 73.0f / 256f, 94.0f / 256f, 0.1f);
        pixmap.fillCircle(buttonWidth / 2, buttonWidth / 2, buttonWidth / 2 - 1);
        settingsButton = new Texture(pixmap);
        pixmap.dispose();

        atlas = new TextureAtlas(Gdx.files.internal("ui/gui.atlas"));

        batch = new SpriteBatch();
        backBatch = new SpriteBatch();
        shapeRenderer = new ShapeRenderer();

        this.app = app;

        FileHandle baseFileHandle = Gdx.files.internal("data/i18n/Bundle");
        Locale locale = Locale.getDefault();
        I18NBundle bundle = I18NBundle.createBundle(baseFileHandle, locale);

        textFont = new BitmapFont(Gdx.files.internal("data/fonts/sett_text.fnt"));//  generator.generateFont(parameter);
        textFont.getRegion().getTexture().setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear);
        titleFont = new BitmapFont(Gdx.files.internal("data/fonts/sett_title.fnt"));//  generator.generateFont(parameter);

        titleFont.getRegion().getTexture().setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear);

        stage = new Stage(new ScreenViewport()) {
            @Override
            public boolean touchDragged(int screenX, int screenY, int pointer) {
                //isTouch = false;
                return super.touchDragged(screenX, screenY, pointer);
            }

            @Override
            public boolean touchUp(int screenX, int screenY, int pointer, int button) {
                //if()
                //isTouch = false;

                //if (touchTimer < TabataModel.TapTimeout)
                //    app.switchToExerciseMode();

                return super.touchUp(screenX, screenY, pointer, button);

            }

            @Override
            public boolean touchDown(int screenX, int screenY, int pointer, int button) {
                int sbuttonWidth = (int) (2 * Gdx.graphics.getWidth() * 0.0625f);
                if (screenX < sbuttonWidth && screenY < sbuttonWidth) {
                    isTouch = true;
                    touchTimer = 0;
                }
                return super.touchDown(screenX, screenY, pointer, button);
            }

            @Override
            public boolean keyDown(int keycode) {
                if (keycode == Input.Keys.BACK || keycode == Input.Keys.ESCAPE)
                    app.switchToExerciseMode();
                return false;
            }
        };

        CheckBox.CheckBoxStyle checkBoxStyle = new CheckBox.CheckBoxStyle();
        checkBoxStyle.font = textFont;

        checkBoxStyle.checkboxOff = new SpriteDrawable(atlas.createSprite("switch_off"));
        checkBoxStyle.checkboxOn = new SpriteDrawable(atlas.createSprite("switch_on"));

        Label.LabelStyle labelStyle = new Label.LabelStyle();
        labelStyle.font = textFont;

        Label.LabelStyle titleStyle = new Label.LabelStyle();
        titleStyle.font = titleFont;

        final Preferences prefs = Gdx.app.getPreferences("tabatatimer");
        final CheckBox cbSound = new CheckBox("", checkBoxStyle);
        cbSound.getCells().get(0).size(306.0f / 3.0f, 204.0f / 3.0f);
        cbSound.setChecked(prefs.getBoolean("sound", true));
        cbSound.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                prefs.putBoolean("sound", cbSound.isChecked());
                prefs.flush();
                model.setIsSoundEnabled(cbSound.isChecked());
            }
        });
        final CheckBox cbVibro = new CheckBox("", checkBoxStyle);
        cbVibro.getCells().get(0).size(306.0f / 3.0f, 204.0f / 3.0f);
        cbVibro.setChecked(prefs.getBoolean("vibro", true));
        cbVibro.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                prefs.putBoolean("vibro", cbVibro.isChecked());
                prefs.flush();
                model.setIsVibroEnabled(cbVibro.isChecked());
            }
        });

        Label title = new Label(bundle.get("settings_title"), titleStyle);
        Label lsound = new Label(bundle.get("settings_sound"), labelStyle);
        Label lvibro = new Label(bundle.get("settings_vibro"), labelStyle);

        Image line_1 = new Image(blackLine);
        Image line_2 = new Image(blackLine);
        Image line_4 = new Image(blackLine);
        Skin uiSkin = new Skin();
        tbl = new Table(uiSkin);
        tbl.setFillParent(true);
        tbl.align(Align.top);

        tbl.add(title).pad(Math.round(Math.max(screenHeigh, screenWidth) * 40.0f / 1136.0f), 0, Math.round(Math.max(screenHeigh, screenWidth) / 30), 0);
        tbl.row();
        tbl.add(line_1).pad(Math.round(screenHeigh / 70), 0, Math.round(screenHeigh / 70), 0);
        tbl.row();
        Table rw_1 = new Table();
        rw_1.add(lsound).width(Math.round(screenWidth - 102.0f - screenWidth / 10.0f)).align(Align.left);
        rw_1.add(cbSound).align(Align.right);

        tbl.add(rw_1);
        tbl.row();
        tbl.add(line_2).pad(Math.round(screenHeigh / 70), Math.round(screenWidth * 32.0f / 640.0f), Math.round(screenHeigh / 70), Math.round(screenWidth * 32.0f / 640.0f));
        tbl.row();
        Table rw_2 = new Table();
        rw_2.add(lvibro).width(Math.round(screenWidth - 102.0f - screenWidth / 10.0f)).align(Align.left);
        rw_2.add(cbVibro).align(Align.right);//.height(62.0f).maxHeight(62.0f).minHeight(62.0f);

        tbl.add(rw_2);
        tbl.row();
        tbl.add(line_4).pad(Math.round(screenHeigh / 70), 0, Math.round(screenHeigh / 70), 0);
        tbl.row();
        tbl.setColor(1, 1, 1, 0);
        this.stage.addActor(tbl);
    }

    @Override
    public void show() {
        isBirth = true;
        touchTimer = 0;
        Gdx.input.setInputProcessor(stage);
        Gdx.input.setCatchBackKey(true);
    }

    @Override
    public void render(float delta) {

        int w = Gdx.graphics.getWidth();
        int h = Gdx.graphics.getHeight();
        int buttonWidth = (int) (w * 0.0625f);

        if (isTouch || isBirth)
            buffer.begin();
        Gdx.graphics.getGL20().glClearColor(1, 1, 1, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT | GL20.GL_DEPTH_BUFFER_BIT | (Gdx.graphics.getBufferFormat().coverageSampling ? GL20.GL_COVERAGE_BUFFER_BIT_NV : 0));
        tbl.setColor(1, 1, 1, 1);
        stage.act(delta);
        stage.draw();
        if (isTouch || isBirth) {
            buffer.end();
            screenshotSettings = buffer.getColorBufferTexture();
            if (maskedTapShader == null)
                maskedTapShader = new MaskedTapShader(screenshotExercise, screenshotSettings, buttonWidth, h - buttonWidth);
        }

        touchTimer += delta;
        if (isTouch || isBirth) {
            tapRad += 4.0f * delta * (4.2f * Gdx.graphics.getHeight() - tapRad);
        }

        batch.begin();

        if (isBirth)
            maskedTapShader.render(tapRad, false);
        else if (isTouch)
            maskedTapShader.render(tapRad, true);
        else {
            batch.setBlendFunction(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);
            batch.setColor(1, 1, 1, 1);
            batch.draw(settingsButton, buttonWidth / 2, h - 3 * buttonWidth / 2);
        }
        batch.end();

        if (isBirth && (touchTimer - TabataModel.LongTapTimeoutSetting) * (touchTimer - delta - TabataModel.LongTapTimeoutSetting) <= 0) {
            isBirth = false;
            tapRad = 0;
        }

        if (isTouch && (touchTimer - TabataModel.LongTapTimeoutSetting) * (touchTimer - delta - TabataModel.LongTapTimeoutSetting) <= 0) {
            app.switchToExerciseMode();
        }
    }



    @Override
    public void resize(int width, int height) {

    }

    @Override
    public void pause() {
    }

    @Override
    public void resume() {
    }

    @Override
    public void hide() {
    }

    @Override
    public void dispose() {
        textFont.dispose();
        titleFont.dispose();
        stage.dispose();
        atlas.dispose();
        blackLine.dispose();
        settingsButton.dispose();
        batch.dispose();
        shapeRenderer.dispose();
        backBatch.dispose();
        buffer.dispose();
        maskedTapShader.dispose();
    }


}
