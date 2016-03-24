package com.mygdx.badguys;

import com.badlogic.gdx.graphics.Camera;

public class Pig extends BadGuy {

	public Pig(float x, float y, int animStart, int frameAmount, Camera camera) {
		super(x, y, animStart, frameAmount, camera);
		health = 10;
	}
	
	@Override
	public void move(float x, float y) {
		System.out.println(this.getClass().getName() + " move");
	}

	@Override
	public void attack() {
		System.out.println(this.getClass().getName() + " attack");
		
	}
}
