package com.mygdx.game.desktop;

import com.badlogic.gdx.backends.lwjgl.LwjglApplication;
import com.badlogic.gdx.backends.lwjgl.LwjglApplicationConfiguration;
import com.mygdx.game.Sterria;

public class DesktopLauncher {
	public static void main (String[] arg) {
		
		// Resolutions
		// 1920x1080, 1280x800,1440x900, 1600x900,1680x1050,1360x780, 1024x768
		LwjglApplicationConfiguration config = new LwjglApplicationConfiguration();
		config.title = "SterriaLand by SDG Games";
		config.width =  1920;
		config.height = 1080;
		config.vSyncEnabled = true;
		config.backgroundFPS =0;
		config.foregroundFPS = 0;
		config.fullscreen = !true;
		config.resizable = false;
	
		new LwjglApplication(new Sterria(), config);
		

	}
}
