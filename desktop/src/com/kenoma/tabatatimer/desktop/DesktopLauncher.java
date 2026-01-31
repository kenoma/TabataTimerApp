package com.kenoma.tabatatimer.desktop;

import com.badlogic.gdx.backends.lwjgl.LwjglApplication;
import com.badlogic.gdx.backends.lwjgl.LwjglApplicationConfiguration;
import com.kenoma.tabatatimer.Main;

public class DesktopLauncher {
	public static void main (String[] arg) {
		LwjglApplicationConfiguration config = new LwjglApplicationConfiguration();
        //320*480
        //640×960
        ///1080×1920

        //iphone3
        //config.height = 480;
        //config.width = 320;

        //iphone4
        config.height =960;
        config.width =  640;

        //iphone5
        //config.height = 1136;
        //config.width = 640;

        //iphone6
        //config.height = 1334;
        //config.width = 750;

        //iphone6 plus
        //config.height = 2208;
        //config.width = 1242;

        Main game = new Main();
		config.resizable=false;

		new LwjglApplication(game, config);
	}
}
