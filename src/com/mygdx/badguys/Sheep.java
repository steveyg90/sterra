package com.mygdx.badguys;

import com.badlogic.gdx.graphics.Camera;


public class Sheep extends BadGuy {

	public Sheep(float x, float y, int animStart, int frameCount, Camera camera) {
		super(x, y, animStart, frameCount, camera);
		health = 5;
	}

	
	@Override
	public void move(float x, float y) {
		this.x += x;
		switch(currentState)
		{
		case IDLE:
			if(Math.abs(this.x - camera.position.x ) < 100)
				currentState = State.CHASE;
			break;
		case CHASE: 
			if(Math.abs(this.x - camera.position.x ) > 100)
				currentState = State.IDLE;
			break;
		}
	}

	@Override
	public void attack() {
	}

}
