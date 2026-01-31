package com.kenoma.tabatatimer;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Graphics;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.PixmapIO;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.glutils.FloatTextureData;
import com.badlogic.gdx.graphics.glutils.FrameBuffer;
import com.badlogic.gdx.graphics.glutils.PixmapTextureData;
import com.badlogic.gdx.scenes.scene2d.ui.TextTooltip;
import com.badlogic.gdx.scenes.scene2d.utils.SpriteDrawable;
import com.badlogic.gdx.utils.BufferUtils;
import com.badlogic.gdx.utils.ScreenUtils;

import java.nio.ByteBuffer;



public class ScreenshotFactory {
    private static int counter = 1;

    public static void saveScreenshot() {
        try {
            FileHandle fh;
            int width = Gdx.graphics.getWidth();
            int height = Gdx.graphics.getHeight();
            do {
                fh = new FileHandle("screenshot_" + width + "_" + height + "_" + counter++ + ".png");
            } while (fh.exists());
            Pixmap pixmap = getScreenshot(0, 0, width, height, true);
            PixmapIO.writePNG(fh, pixmap);
            pixmap.dispose();
        } catch (Exception e) {
        }
    }

  /*  public static Texture getScreenShot(Game game) {
        int w = Gdx.graphics.getWidth();
        int h = Gdx.graphics.getHeight();
        Texture result = null;
        FrameBuffer buffer = new FrameBuffer(Pixmap.Format.RGBA8888, w, h, false);
        try {
            Gdx.graphics.getGL20().glViewport(0, 0, w, h);
            buffer.begin();
             //Gdx.graphics.requestRendering(); // Or however you normally draw it
            game.getScreen().render(0);
            buffer.end();
            result = buffer.getColorBufferTexture();
        } catch (final Exception e) {
            e.printStackTrace();
        } finally {
            buffer.dispose();
        }

        //Pixmap pixmap =ScreenUtils.getFrameBufferPixmap;// 0, 0, width, height);// getScreenshot(0, 0, width, height, true);

        return result;
    }*/

//    static byte[] readData(int width, int height) {
//        int numBytes = width * height * 4;
//        ByteBuffer pixels = BufferUtils.newByteBuffer(numBytes);
//        Gdx.gl.glPixelStorei(GL20.GL_PACK_ALIGNMENT, 1);
//        Gdx.gl.glReadPixels(0, 0, width, height, GL20.GL_RGBA, GL20.GL_UNSIGNED_BYTE, pixels);
//
//        byte[] lines = new byte[numBytes];
//        int numBytesPerLine = width * 4;
//        for (int i = 0; i < height; i++) {
//            pixels.position((height - i - 1) * numBytesPerLine);
//            pixels.get(lines, i * numBytesPerLine, numBytesPerLine);
//        }
//        //pixels.clear();
//        //pixels.put(lines);
//        return lines;
//    }

    private static Pixmap getScreenshot(int x, int y, int w, int h, boolean yDown) {
        Pixmap pixmap = ScreenUtils.getFrameBufferPixmap(x, y, w, h);

        if (yDown) {
            // Flip the pixmap upside down
            ByteBuffer pixels = pixmap.getPixels();
            int numBytes = w * h * 4;
            byte[] lines = new byte[numBytes];
            int numBytesPerLine = w * 4;
            for (int i = 0; i < h; i++) {
                pixels.position((h - i - 1) * numBytesPerLine);
                pixels.get(lines, i * numBytesPerLine, numBytesPerLine);
            }
            pixels.clear();
            pixels.put(lines);
        }

        return pixmap;
    }
}