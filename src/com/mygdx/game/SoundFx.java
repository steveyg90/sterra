package com.mygdx.game;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.utils.Disposable;

public class SoundFx implements Disposable {

	Sound digDirt;
	Sound MainTrack; 
	
	public SoundFx()
	{
		digDirt = Gdx.audio.newSound(Gdx.files	
	         .internal("../my-gdx-game-core/data/coal.mp3"));
		MainTrack = Gdx.audio.newSound(Gdx.files
		         .internal("../my-gdx-game-core/data/TheHaunting.mp3"));
	}
	
	public void playDirt()
	{
		digDirt.play();
	}
	
	public void playMusic()
	{
		MainTrack.loop();
	}
	
	@Override
	public void dispose() {
		// TODO Auto-generated method stub
		digDirt.dispose();
		MainTrack.dispose();
		
		System.out.println("All music disposed");
	}

}
