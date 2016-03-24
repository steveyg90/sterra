package com.mygdx.badguys;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.mygdx.game.Player;

public abstract class BadGuy  {

	protected enum State { 
		IDLE,
		CHASE
	}
	
	protected State currentState = State.IDLE;
	
	protected float x, y;
	protected byte health;

	private static Texture walkSheet = null;
	protected static Animation walkAnimation;
	protected static TextureRegion[] walkFrames;
	protected static TextureRegion currentFrame;
	protected SpriteBatch batch;
	protected int animStart;
	protected int frameCount;
	protected Camera camera;
	protected float stateTime;

	public float getX() { return x; }
	public float getY() { return y; }
	
	public void setHealth(byte health)
	{
		this.health = health;
	}
	
	public BadGuy(float x, float y, int animStart, int frameCount, Camera camera)
	{
		super();
		this.x = x;
		this.y = y;	
		this.animStart = animStart;
		this.frameCount = frameCount;
		this.camera = camera;
	}


	public void load(String textureFile, int cols, int rows, boolean bNewFile) {
		if(walkSheet == null || bNewFile)
		{
			walkSheet = new Texture(Gdx.files.internal(textureFile));
	
			TextureRegion[][] tmp = TextureRegion.split(walkSheet, walkSheet.getWidth() / cols, walkSheet.getHeight() / rows); 
			walkFrames = new TextureRegion[cols * rows];
	
			int index = 0;
			
			for (int i = 0; i < rows; i++) {
				for (int j = 0; j < cols; j++) {
					walkFrames[index++] = tmp[i][j];
				}
			}
			walkAnimation = new Animation(1.0f, walkFrames); // #11
		}
		this.batch = new SpriteBatch(); 		
		stateTime = this.animStart;

	}

	public abstract void move(float x, float y);

	public abstract void attack();

	public void decreaseHealth(int amount) {
	}

	public void spawn(int x, int y) {
	}

	public void render(SpriteBatch batch) {
		stateTime += Gdx.graphics.getDeltaTime();
		if(stateTime >= this.animStart + this.frameCount) 
			stateTime=this.animStart;
		currentFrame = walkAnimation.getKeyFrame(stateTime, true);
		Color oldCol = batch.getColor();
		batch.setColor(1,1,1,1); // VIP!
		batch.draw(currentFrame, x, y, 16, 16);
		batch.setColor(oldCol);
	}
	
}
