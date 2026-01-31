package com.kenoma.tabatatimer.misc;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Mesh;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.VertexAttribute;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;


public class MaskedTapShader {
    final float triangleCoords[] = new float[]
            {-1f, -1f, 0, 1, 1, 1, 1, 0, 1,
                    1f, -1f, 0, 1, 1, 1, 1, 1, 1,
                    1f, 1f, 0, 1, 1, 1, 1, 1, 0,
                    -1f, 1f, 0, 1, 1, 1, 1, 0, 0};
    final short[] indices = new short[]{0, 1, 2, 2, 3, 0};
    final private ShaderProgram mProgram;
    final Mesh mesh;
    final float[] iResolution = new float[2];
    final float[] buttonCenter = new float[2];
    final Texture _foreground, _background;
    final float scalingFactor;
    final String vertexShader = "attribute vec4 a_position;    \n" +
            "attribute vec4 a_color;\n" +
            "attribute vec2 a_texCoord0;\n" +
            "varying vec2 v_texCoords;" +
            "void main()                  \n" +
            "{                            \n" +
            "   v_texCoords = vec2(a_texCoord0.s, 1.0 - a_texCoord0.t); \n" +
            "   gl_Position =  a_position;  \n" +
            "}                            \n";

    final String fragmentShader = "#ifdef GL_ES\n" +
            "precision mediump float;\n" +
            "#endif\n" +
            "varying vec4 v_color;" +
            "varying vec2 v_texCoords;" +
            "uniform float tapRad,resRatio,invertion;" +
            "uniform vec2 center;" +
            "uniform sampler2D u_fore;" +
            "uniform sampler2D u_back;" +
            "void main()" +
            "{" +
            " vec2 uv=v_texCoords.xy-center.xy; " +
            " float rad = uv.x*uv.x+ resRatio*uv.y*uv.y;" +
            "" +
            " if((invertion==0 && rad  > tapRad)||(invertion!=0 && rad  < tapRad)) " +
            "   gl_FragColor = texture2D(u_fore, v_texCoords); " +
            " else " +
            "   gl_FragColor = texture2D(u_back, v_texCoords);" +
            "}";

    public MaskedTapShader(Texture foreground, Texture background, float x, float y) {
        _foreground = foreground;
        _background = background;
        buttonCenter[0] = x / Gdx.graphics.getWidth();
        buttonCenter[1] = y / Gdx.graphics.getHeight();
        iResolution[0] = Gdx.graphics.getWidth();
        iResolution[1] = Gdx.graphics.getHeight();
        scalingFactor = (float) Math.sqrt(iResolution[0] * iResolution[0] + iResolution[1] * iResolution[1]);
        mProgram = new ShaderProgram(vertexShader, fragmentShader);

        mesh = new Mesh(true, 4, 6, VertexAttribute.Position(), VertexAttribute.ColorUnpacked(), VertexAttribute.TexCoords(0));
        mesh.setVertices(triangleCoords);
        mesh.setIndices(indices);
    }

    public void render(float tapRad, boolean isInverted) {
        _foreground.bind(0);
        mProgram.begin();
        //mProgram.setUniformMatrix("u_worldView", matrix);
        _foreground.bind(1);
        mProgram.setUniformi("u_fore", 1);

        _background.bind(0);
        mProgram.setUniformi("u_back", 0);

        mProgram.setUniformf("tapRad", (tapRad / scalingFactor) * (tapRad / scalingFactor));
        mProgram.setUniformf("invertion", isInverted ? 1 : 0);
        mProgram.setUniformf("resRatio", (iResolution[1] / iResolution[0]) * (iResolution[1] / iResolution[0]));
        mProgram.setUniform2fv("center", buttonCenter, 0, 2);

        mesh.render(mProgram, GL20.GL_TRIANGLES);
        mProgram.end();
    }

    public void dispose()
    {
        mProgram.dispose();
        mesh.dispose();

    }
}