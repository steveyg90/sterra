package com.mygdx.game;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.InputProcessor;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.FPSLogger;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.glutils.FrameBuffer;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer.ShapeType;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.physics.box2d.World;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.mygdx.badguys.BadGuy;
import com.mygdx.badguys.BadGuyOutsideManager;
import com.mygdx.badguys.OutsideLand;
import com.mygdx.badguys.Pig;
import com.mygdx.badguys.Sheep;
import com.mygdx.parallax.ParallaxBackground;
import com.mygdx.parallax.ParallaxLayer;

import box2dLight.ConeLight;
import box2dLight.RayHandler;

public class Sterria extends ApplicationAdapter implements InputProcessor {

	private ShapeRenderer shapeRenderer;
		
	private int squareX, squareY;
	
	private Skin skin;
	private Stage stage;
	
	private BitmapFont fpsFont = null;

	private boolean torch = false;
	private float torchDir = 0;
	
	private int FPS = 0;

	private final int LIGHTCHECKFRAMES = 1;
	
	private World world;
	RayHandler rayHandler;
	private ConeLight ct;
	
	private Sprite lightSprite;
	private Texture lightTexture;

	FrameBuffer lightBuffer;
	TextureRegion lightBufferRegion;

	private ParallaxBackground cloudBackground;

	private boolean debug = false;

	private SpriteBatch batch;
	private BitmapFont font;

	@SuppressWarnings("unused")
	private FPSLogger fpsLogger;
	private OrthographicCamera camera, hudCamera, topHudCamera;

	private boolean bRight = false;
	private boolean bLeft = false;
	private boolean bDown = false;

	TextureRegion[][] regions = null;
	private Texture spriteSheet, cloudTexture, cloudOneTexture;

	static  int SCREENWIDTH = 0;			//1280 - these are got in the create method;
	static  int SCREENHEIGHT = 0;			//1024 - these are got in the create method;
	static final int TILEWIDTH = 16;
	static final int TILEHEIGHT = 16;
	static final int WORLDWIDTH = 1000;// 2000; // 2000 tiles wide = ~62 screens
	static final int WORLDHEIGHT = 2000;// 1000; // 500 tiles deep = ~41 screens
										// deep or
										// 7 up 8 down
	private WorldMap worldMap;

	private ShaderProgram nightShader;

	float nightCol = 0.5f;

	// Shaders
	final String vertexShader = new FileHandle(
			"../my-gdx-game-core/data/vertexShader.glsl").readString();

	final String nightPixelShader = new FileHandle(
			"../my-gdx-game-core/data/nightCycle.frag").readString();

	Texture cross = null;

	private Vector3 playerPosition;

	private Player player;
	
	private BadGuy pig;
	private BadGuy sheep;
	
	private Sprite hudSprite;
	
	SoundFx sfx = null;
	
	private List<OutsideLand> landList;

	private BadGuyOutsideManager outsideManager;
	
	@Override
	public void create() {

		sfx = new SoundFx();

		SCREENWIDTH = Gdx.graphics.getWidth();
		SCREENHEIGHT = Gdx.graphics.getHeight();
		
		fpsFont = new BitmapFont();

		player = new Player("../my-gdx-game-core/assets/sprites.png", SCREENWIDTH / 2,
				SCREENHEIGHT / 2); // our hero!
		
		sfx.playMusic();
		
		shapeRenderer = new ShapeRenderer();
			
		world = new World(new Vector2(0, 0), true); // Box2D world, used for
													// Box2D lights
		rayHandler = new RayHandler(this.world);
		RayHandler.setGammaCorrection(true);
		RayHandler.useDiffuseLight(true);
		rayHandler.setAmbientLight(1);
		rayHandler.setShadows(false);

		this.lightTexture = new Texture(
				"../my-gdx-game-core/assets/mylight.png");
		this.lightSprite = new Sprite(this.lightTexture);
		this.lightSprite.scale(3.0f);

		hudSprite = new Sprite(new Texture("../my-gdx-game-core/assets/tophud.png"));
		
		worldMap = new WorldMap(WORLDWIDTH, WORLDHEIGHT); // this will create
															// the world map
		
		
		// Load pixel and vertex shaders
		nightShader = new ShaderProgram(vertexShader, nightPixelShader);

		if (!nightShader.isCompiled()) {
			Gdx.app.error("Shader", nightShader.getLog());
		}

		else {
			nightShader.begin();
			nightShader.setUniformf("ambientColor", this.nightCol,
					this.nightCol, this.nightCol, this.nightCol);
			nightShader.end();
		}

		batch = new SpriteBatch();
		fpsLogger = new FPSLogger();

		spriteSheet = new Texture("../my-gdx-game-core/assets/davidtiles.png");
		cloudTexture = new Texture("../my-gdx-game-core/assets/cloudsnew.png");
		cloudOneTexture = new Texture("../my-gdx-game-core/assets/cloudone.png");
		regions = TextureRegion.split(spriteSheet, TILEWIDTH, TILEHEIGHT);

		camera = new OrthographicCamera(SCREENWIDTH, SCREENHEIGHT - 100);
		hudCamera = new OrthographicCamera(SCREENWIDTH, 100);
		topHudCamera = new OrthographicCamera(SCREENWIDTH, 100);

		font = new BitmapFont();
		font.setScale(1.0f);
		font.setColor(Color.WHITE);


		// w-2000 h-1000
		// Position player middle on X and near top of map on Y
		playerPosition = new Vector3(SCREENWIDTH / 2, SCREENHEIGHT / 2, 0);
		playerPosition.x = (WORLDWIDTH / 2) * TILEWIDTH;

		playerPosition.y = 1300 * 16;

		camera.position.x = playerPosition.x;
		camera.position.y = playerPosition.y;

		
/////////////// ADD SOME ABOVE GROUND BADDIES /////////////////////////
// This needs moving into the BadGuyOutsideManager class really      //		
		
		landList = new ArrayList<OutsideLand>();		
		
		// Get possible starting spawn points for outside baddies (on the grass)
		worldMap.findLand(landList);
		
		outsideManager = new BadGuyOutsideManager();

		Random r = new Random();
		
		for(OutsideLand ol : landList)
		{
			int rr = r.nextInt(3);
			switch(rr)
			{
			case 0:
			{
				sheep = new Sheep(ol.x * TILEWIDTH, (1+ol.y) * TILEHEIGHT, 4, 2, camera);
				sheep.load("../my-gdx-game-core/assets/badguys.png", 40, 5, false);
				sheep.setHealth((byte)r.nextInt(3));
				outsideManager.addBadGuy((int)sheep.getX(), (int)sheep.getY(), sheep);
				break;
			}	
			
			case 1:
			{
				pig = new Pig(ol.x * TILEWIDTH, (1+ol.y) * TILEHEIGHT, 0, 2, camera);
				pig.load("../my-gdx-game-core/assets/badguys.png", 40, 5, false);
				pig.setHealth((byte)r.nextInt(3));
				outsideManager.addBadGuy((int)pig.getX(), (int)pig.getY(), pig);
			}
			break;
			default:
				pig = new Pig(ol.x * TILEWIDTH, (1+ol.y) * TILEHEIGHT, 2, 2, camera);
				pig.load("../my-gdx-game-core/assets/badguys.png", 40, 5, false);
				pig.setHealth((byte)r.nextInt(3));
				outsideManager.addBadGuy((int)pig.getX(), (int)pig.getY(), pig);
				break;
		
			}
		}
		

//////////////////////////////////////////
		
		ct = new ConeLight(rayHandler, 8, Color.WHITE, 400, playerPosition.x,
				playerPosition.y, 0, 30);

		// Cloud background - add backgrounds here
		cloudBackground = new ParallaxBackground(new ParallaxLayer[] {
		// new ParallaxLayer(regions[0][0],new Vector2(),new Vector2(0, 0)),
		new ParallaxLayer(cloudTexture, new Vector2(1.0f, 0.0f), new Vector2(0,
				0)),
		// new ParallaxLayer(cloudOneTexture,new Vector2(2.5f,0),new
		// Vector2(0,-180),new Vector2(0, 0)),
				}, 960, 640, new Vector2(4, 0));

		Gdx.input.setInputProcessor(this);
		
	}

	@Override
	public void render() {

		Vector3 poss = new Vector3(camera.position);
		poss.y -= 10;

		if (checkCollision(poss) && !player.jumping ) {
			camera.position.y -= 2; // scroll map down
		}

		int fps = Gdx.graphics.getFramesPerSecond();

		Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

		this.world.step(1 / 60f, 8, 3);

		if (this.camera.position.y > 1100 * 16)
			cloudBackground.render(0.05f, this.nightShader); // render clouds

		batch.setProjectionMatrix(camera.combined);

		if(this.checkBlockAbove(camera.position))  // can we jump?  If block above, we cannot
		{
			if(player.jumping)
			{
				player.jump();
				camera.position.y += 2;
			} 
			else
			{
				if(Gdx.input.isKeyJustPressed(Input.Keys.UP) && !checkCollision(poss))
				{
					player.jump();
					camera.position.y += 2;
				}
			}
		}	
		else
		{
			player.jumping = false;   // player not jumping now
		}
		
		if (Gdx.input.isKeyJustPressed(Input.Keys.W)) {
			removeBlock(camera.position, bDown, bLeft, bRight);
		}

		if (Gdx.input.isKeyPressed(Input.Keys.T)) {
			// Torch
			torch = !torch;
		}

		if (Gdx.input.isKeyPressed(Input.Keys.R)) {
			camera.zoom = 1;
			debug = false;
		}

		// Pressing the L key will attempt to place a torch
		if (Gdx.input.isKeyPressed(Input.Keys.L)) {
			Vector3 screenSpace = toScreenSpace(camera.position);
			int x = (int) screenSpace.x;
			int y = (int) screenSpace.y;

			// Add a torch/light
			this.worldMap.addLight(x, y);
			
				//new PointLight(rayHandler, 50, Color.RED, 110,
					//	camera.position.x, camera.position.y);
			
		}

		if (Gdx.input.isKeyPressed(Input.Keys.MINUS))
			camera.zoom += 3.0f;
		if (Gdx.input.isKeyPressed(Input.Keys.I))
			camera.zoom -= 3.0f;

		if (Gdx.input.isKeyPressed(Input.Keys.LEFT)) {
			torchDir = 180;
			player.moveLeft(1);
			Vector3 po = new Vector3(camera.position);
			po.x -= 2;
			if (checkCollision(po)) {
				camera.position.x -= 1; // scroll map left
			}  
		}

		if (Gdx.input.isKeyPressed(Input.Keys.RIGHT)) {
			torchDir = 0;
			player.moveRight(1);
			Vector3 po = new Vector3(camera.position);
			po.x += 2.0f;
			if (checkCollision(po)) {
				camera.position.x += 1; // scroll map right
			}  
		}

		// We need to do a jump - TO DO
		if (Gdx.input.isKeyPressed(Input.Keys.DOWN)) {
			torchDir+=2;
		}
		if (Gdx.input.isKeyPressed(Input.Keys.PAGE_UP)) {
			camera.position.y += 200; // jump map up
		}
		if (Gdx.input.isKeyPressed(Input.Keys.PAGE_DOWN)) {
			camera.position.y -= 200; // jump map down
		}
		if (Gdx.input.isKeyPressed(Input.Keys.D)) {
			debug = true; // turn debug on - basically draw the whole map
		}
		if (Gdx.input.isKeyPressed(Input.Keys.F)) {
			debug = false; // turn debug off
		}

		camera.update();

		// Calculate what we should draw
		int camX = (int) camera.position.x - SCREENWIDTH / 2;
		camX /= TILEWIDTH;
		int endX = SCREENWIDTH / TILEHEIGHT + camX;

		int camY = (int) camera.position.y - SCREENHEIGHT / 2;
		camY /= TILEHEIGHT;
		int endY = SCREENHEIGHT / TILEHEIGHT + camY;

		batch.begin();

		// Draw the map - only draw what camera sees
		worldMap.drawMap(debug, batch, regions, camX - 2, camY, endX + 2, endY,
				WORLDWIDTH, WORLDHEIGHT);


		worldMap.moveWater(camX - 4, camY - 4, endX + 4, endY + 4);

		////////////////////////////////////////////////////////////////
		// Render bad guy sprites and move / update / animate etc them
		////////////////////////////////////////////////////////////////
		
		outsideManager.renderBadGuys(camera,batch);
		
		////////////////////////////////////////////////////////////////
		
		
		if(FPS >= LIGHTCHECKFRAMES )
		{
			worldMap.updateLights(camX - 4, camY - 4, endX + 4, endY + 4, torch);
			FPS = 0;
		}
		else
			FPS++;
	
		batch.end();

		Matrix4 uiMatrix = topHudCamera.combined.cpy();
		uiMatrix.setToOrtho2D(0, 0, SCREENWIDTH, SCREENHEIGHT);
		batch.setProjectionMatrix(uiMatrix);
		topHudCamera.update();
		batch.begin();
		hudSprite.setPosition((SCREENWIDTH-hudSprite.getWidth())/2, SCREENHEIGHT-hudSprite.getHeight()-20);
		hudSprite.draw(batch);
		batch.end();
		
		// HUD - Draw hud (in our case some text!)
		
		uiMatrix = hudCamera.combined.cpy();
		uiMatrix.setToOrtho2D(0, 0, SCREENWIDTH, SCREENHEIGHT);
		batch.setProjectionMatrix(uiMatrix);
		hudCamera.update();

		batch.begin();
		
		drawCameraPosition(batch, camera.position); 
		showFeet(batch, camera.position );
		 
		this.drawFps(batch, fps);

		batch.end();
		
		// When player has a torch
		ct.setActive(torch);
		if(torch) {
			worldMap.updateTorch( toScreenSpace(camera.position));
			ct.setPosition(camera.position.x,camera.position.y);
			ct.setDirection(torchDir);
		}

		rayHandler.setCombinedMatrix(camera.combined);
		rayHandler.updateAndRender();

		// Draw player after we have drawn the world
		player.render(0);
		
		shapeRenderer.setProjectionMatrix(camera.combined);
		shapeRenderer.setColor(Color.RED);
	    shapeRenderer.begin(ShapeType.Line);
	    shapeRenderer.rect(squareX,squareY, 16,16);
	    shapeRenderer.end();

	}

	private boolean displayBlockDiagAbove(boolean dir, Vector3 position) {
		int val = dir == true ? 0 : -1;
		Vector3 screenSpace = toScreenSpace(position);
		int x = Math.round(screenSpace.x) + val;
		int y = Math.round(screenSpace.y) + 1;
		BlankEntity entity = this.worldMap.getBlock(x, y);

		if (entity != null) {
			if (/* entity.toString().equals(".") || */entity instanceof CaveEntity
					|| entity instanceof WaterEntity)

			{
				return !true;
			}
		} else
			// entity is null
			return !true;

		return false;
	}

	@SuppressWarnings("unused")
	private boolean keyPressed;

	@Override
	public boolean keyDown(int keycode) {
		if (keycode == Input.Keys.DOWN) {
			keyPressed = true;
		}
		return false;
	}

	// ///////////////////////////////////////////////////////////
	// Remove a block - blocks take a varied amount of time to
	// dig out. Example - rock is 7 hits before it will be removed
	// and dirt is 2 hits.
	// TODO: Set animation of digging and allow collectable when
	// dug out (or <BlankEntity>.hits == 0 and block is removed)
	// ///////////////////////////////////////////////////////////
	private void removeBlock(Vector3 position, boolean bDown, boolean bLeft,
			boolean bRight) {
		Vector3 screenSpace = toScreenSpace(position);
		int x = (int) screenSpace.x;
		int y = (int) screenSpace.y;

		if (bDown)
			y--;
		if (bRight)
			x += 1;
		if (bLeft)
			x -= 1;
		BlankEntity entity = this.worldMap.getBlock(x, y);
		
		if(entity instanceof LightEntity)  // light/torch?
		{
			worldMap.removeLight(x, y);
			return;
		}
		
		if(entity instanceof CaveTopEntity || entity instanceof CaveLeftEntity || entity instanceof CaveRightEntity ||
				entity instanceof CaveBottomEntity)
		{
			if (entity.hits == 0) // hit block enough times? If so, we can go
				// ahead and remove it
				this.worldMap.removeEntity(x, y);
			else
				entity.hits--; // still need to hit the block more
			
		}
		else
		{
			if (entity != null && !(entity instanceof CaveEntity)
					&& !(entity instanceof WaterEntity)) {
				if (entity.hits == 0) // hit block enough times? If so, we can go
									// ahead and remove it
					this.worldMap.removeEntity(x, y);
				else
					entity.hits--; // still need to hit the block more

			}
		}
	}

	private boolean checkCollision(Vector3 position) {
		Vector3 screenSpace = toScreenSpace(position);
		int x = Math.round(screenSpace.x);
		int y = Math.round(screenSpace.y);
		BlankEntity entity = this.worldMap.getBlock(x, y);
		if (entity instanceof CaveTopEntity || entity instanceof CaveLeftEntity || entity instanceof CaveRightEntity )
			return true;
		return entity == null /* || entity.toString().equals(".") */
				|| entity instanceof CaveEntity
				|| entity instanceof WaterEntity
				|| entity instanceof LavaEntity;

	}
	
	/*
	 * Checks to see if we have blocks we can pass through above us, useful when player going to perform
	 * a jump
	 */
	private boolean checkBlockAbove(Vector3 position) {
		Vector3 screenSpace = toScreenSpace(position);
		int x = Math.round(screenSpace.x);
		int y = Math.round(screenSpace.y);
		if( (worldMap.getBlock(x,y+1)==null) || (worldMap.getBlock(x, y+1) instanceof CaveEntity || worldMap.getBlock(x, y+1) instanceof WaterEntity
				|| worldMap.getBlock(x, y+1) instanceof LavaEntity))
		{
			return true;
		}
		return false; 
	}

	// Some helper methods
	private Vector3 toScreenSpace(Vector3 position) {
		Vector3 v = new Vector3(0, 0, 0);
		v.x = position.x / TILEWIDTH;
		v.y = position.y / TILEHEIGHT;
		return v;
	}

	private void showFeet(SpriteBatch batch, Vector3 position)
	{
		Vector3 screenSpace = toScreenSpace(position);
		String pos = String.format("Depth: %1.0f feet",
				(this.WORLDHEIGHT - screenSpace.y) * 4 );
		font.setColor(Color.CYAN);
		font.draw(batch, pos, 10, SCREENHEIGHT);
	}
	
	private void drawCameraPosition(SpriteBatch batch, Vector3 position) {
		Vector3 screenSpace = toScreenSpace(position);
		String pos = String.format("Player map pos:[X:%1.0f,Y:%1.0f]",
				screenSpace.x, screenSpace.y);
		font.setColor(Color.YELLOW);
		font.draw(batch, pos, 10, 70);
		String playerPos = String.format("Camera pos:[X:%.2f, Y:%.2f]",
				position.x, position.y);
		font.draw(batch, playerPos, 10, 50);
	
	}

	private void drawFps(SpriteBatch batch, int fps) {


		if (fps >= 45) {
			// 45 or more FPS show up in green
			fpsFont.setColor(0, 1, 0, 1);
		} else if (fps >= 30) {
			// 30 or more FPS show up in yellow
			fpsFont.setColor(1, 1, 0, 1);
		} else {
			// less than 30 FPS show up in red
			fpsFont.setColor(1, 0, 0, 1);
		}
		fpsFont.draw(batch, "FPS: " + fps, SCREENWIDTH - 70, SCREENHEIGHT);
		fpsFont.setColor(1, 1, 1, 1); // white

	}

	@Override
	public void dispose() {
		batch.dispose();
		player.getSpriteBatch().dispose();
		nightShader.dispose();
		font.dispose();
		spriteSheet.dispose();
		cloudTexture.dispose();
		cloudOneTexture.dispose();
		rayHandler.dispose();
		world.dispose();
		fpsFont.dispose();
		sfx.dispose();
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
	public boolean keyUp(int keycode) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean keyTyped(char character) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean touchDown(int screenX, int screenY, int pointer, int button) {
		Vector3 mousePos = new Vector3();
		mousePos.set(Gdx.input.getX(), Gdx.input.getY(), 0);
		camera.unproject(mousePos);
		squareX = (int)mousePos.x;
		squareY = (int)mousePos.y;
		squareX -= squareX % TILEWIDTH;
		squareY -= squareY % TILEHEIGHT;

		if (button == Input.Buttons.LEFT) {
			sfx.playDirt();
			removeBlock(mousePos, bDown, bLeft, bRight);
			return true;
		}
		return false;
	}

	@Override
	public boolean touchUp(int screenX, int screenY, int pointer, int button) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean touchDragged(int screenX, int screenY, int pointer) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean mouseMoved(int screenX, int screenY) {
	
		Vector3 mousePos = new Vector3();
		mousePos.set(Gdx.input.getX(), Gdx.input.getY(), 0);
		camera.unproject(mousePos);
		squareX = (int)mousePos.x;
		squareY = (int)mousePos.y;
		squareX -= squareX % 16;
		squareY -= squareY % 16;
		return false;
	}

	@Override
	public boolean scrolled(int amount) {
		// TODO Auto-generated method stub
		return false;
	}
}
