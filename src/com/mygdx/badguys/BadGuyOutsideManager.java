package com.mygdx.badguys;

import java.util.ArrayList;
import java.util.List;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;


public class BadGuyOutsideManager {

	private List<BadGuy> badGuys;

	private int width, height;
	
	public BadGuyOutsideManager() {
		badGuys = new ArrayList<BadGuy>();
		width = Gdx.graphics.getWidth() / 2;
		height = Gdx.graphics.getHeight() / 2;
	}
		
	public void addBadGuy(int x, int y, BadGuy badGuy)
	{
		badGuys.add(badGuy);
	}
	
	public void renderBadGuys(Camera camera, SpriteBatch batch) {
		for(int i=0; i<badGuys.size();i++)
		{
			BadGuy badguy = badGuys.get(i);
			if(badguy.health <=0)     // if badguy dead, remove them from the list
				removeBadGuy(badguy);
			else
			{
				// Only draw outside baddies that are in the camera's view
				if( ( badguy.x >= camera.position.x - (width+30) && badguy.x <= camera.position.x + width + 10)  
			      && ( badguy.y >= camera.position.y - height && badguy.y <= camera.position.y + height))
				{
					badguy.render(batch);
				}
			}
		}
	}
	
	public void removeBadGuy(BadGuy badGuy)
	{
		badGuys.remove(badGuy);
	}
}

