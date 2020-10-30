package edu.lewisu.game;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.InputAdapter;
//import com.badlogic.gdx.InputProcessor;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Vector3;

abstract class CameraEffect {
	protected OrthographicCamera cam;
	protected int duration, progress;
	protected ShapeRenderer renderer;
	protected SpriteBatch batch;
	public CameraEffect(OrthographicCamera cam, int duration, SpriteBatch batch, ShapeRenderer renderer) {
		this.cam = cam;
		this.duration = duration;
		this.batch = batch;
		this.renderer = renderer;
		progress = duration;
	}
	public boolean isActive() {
		return (progress<duration);
	}
	public abstract void play();
	public void updateCamera() {
		cam.update();
		if (renderer != null) {
			renderer.setProjectionMatrix(cam.combined);
		}
		if (batch != null) {
			batch.setProjectionMatrix(cam.combined);
		}
	}
	public void start() {
		progress = 0;
	}
}

class CameraShake extends CameraEffect {
	private int intensity;
	private int speed;
	public int getIntensity() {
		return intensity;
	}
	public void setIntensity(int intensity) {
		if (intensity < 0) {
			this.intensity = 0;
		} else {
			this.intensity = intensity;
		}
	}
	public int getSpeed() {
		return speed;
	}
	public void setSpeed(int speed) {
		if (speed < 0) {
			speed = 0;
		} else {
			if (speed > duration) {
				speed = duration / 2;
			} else {
				this.speed = speed;
			}
		}
	}
	@Override
	public boolean isActive() {
		return super.isActive() && speed > 0;
	}
	public CameraShake(OrthographicCamera cam, int duration, SpriteBatch batch, ShapeRenderer renderer, int intensity, int speed) {
		super(cam,duration,batch,renderer);
		setIntensity(intensity);
		setSpeed(speed);
	}
	@Override
	public void play() {
		if (isActive()) {
			if (progress % speed == 0) {
				intensity = -intensity;
				cam.translate(2*intensity,0);
			}
			progress++;
			if (isActive()) {
				cam.translate(-intensity,0);
			}
			updateCamera();
		}
	}
	@Override
	public void start() {
		super.start();
		cam.translate(intensity,0);
		updateCamera();
	}
}


class InputHandler extends InputAdapter {
    private OrthographicCamera cam;
	private SpriteBatch batch;
    private boolean shiftHeld;
    public int WIDTH;
    public int HEIGHT;
    private Vector3 startCam, startMouse;
    public InputHandler(OrthographicCamera cam, SpriteBatch batch) {
        this.cam = cam;
        this.batch = batch;
        shiftHeld = false;
        WIDTH = Gdx.graphics.getWidth();
        HEIGHT = Gdx.graphics.getHeight();
    }
    @Override
    public boolean keyDown(int keyCode) {
        boolean updateCam = false;
        if (keyCode == Keys.SHIFT_LEFT || keyCode == Keys.SHIFT_RIGHT) {
            shiftHeld = true;
            updateCam = true;
        }
        if (keyCode == Keys.UP) {
            if (shiftHeld) {
                cam.zoom++;
            } else {
                cam.translate(0,5);
            }
            updateCam = true;
        }
        if (keyCode == Keys.DOWN) {
            if (shiftHeld) {
                cam.zoom--;
            } else {
                cam.translate(0,-5);
            }
            updateCam = true;
        }
        if (keyCode == Keys.LEFT) {
            if (shiftHeld) {
                cam.rotate(2);
            } else {
                cam.translate(-5,0);
            }
            updateCam = true;
        }
        if (keyCode == Keys.RIGHT) {
            if (shiftHeld) {
                cam.rotate(-2);
            } else {
                cam.translate(5,0);
            }
            updateCam = true;
		}            
        if (keyCode == Keys.ESCAPE) {
            Gdx.app.exit();
        }
        if (updateCam) {
            cam.update();
            batch.setProjectionMatrix(cam.combined);
        }
        return true;
    }
    @Override
    public boolean keyUp(int keyCode) {
        if (keyCode == Keys.SHIFT_LEFT || keyCode == Keys.SHIFT_RIGHT) {
            shiftHeld = false;
        }
        return true;
	}
	@Override     
	public boolean keyTyped(char character) {        
		return false;     
	}       
    @Override
    public boolean touchDown(int screenX, int screenY, int point, int button) {
        startCam = new Vector3(cam.position.x,cam.position.y,0);
        startMouse = new Vector3(screenX,screenY,0);
        return true;
	}
	@Override     
	public boolean touchUp(int screenX, int screenY, int pointer, int button) {        
		return false;     
	}
	public void updateCamera() {
		cam.update();
		batch.setProjectionMatrix(cam.combined);
	}

	@Override
	public boolean touchDragged(int screenX, int screenY, int pointer) {
		if (shiftHeld) {
			/*tan45=Y/X; 
			theta = arctan(y/x);
			theta = Math.atan2(screenY,screenX); */
			cam.rotate(-5);
			//System.out.println(r);
		}       
		if (screenX < Gdx.graphics.getWidth() && screenX > 0 && screenY < Gdx.graphics.getHeight() && screenY > 0) {
		float diffX = screenX - startMouse.x;
		float diffY = screenY - startMouse.y;
		cam.position.x = startCam.x + diffX;
		cam.position.y = startCam.y - diffY;
		updateCamera();
		}
		return true;
	}
	@Override     
    public boolean mouseMoved(int screenX, int screenY) {              
		return true;     
	}  

	@Override     
	public boolean scrolled(int amount) {                 
		return true;     
	}  
}

public class FunWithInputHandlers extends ApplicationAdapter {
	SpriteBatch batch;
	TextureRegion img;
	Texture tex;
	Sprite sprite;
	OrthographicCamera cam;
	public int imgX, imgY;
	public int imgAngle;
	int imgWidth, imgHeight;
	int imgOrgX, imgOrgY;
	public int WIDTH;
	int HEIGHT;
	CameraShake shaker;

	public void handleInput() {
		boolean shiftHeld = false;
		if (Gdx.input.isKeyPressed(Keys.SHIFT_LEFT) || Gdx.input.isKeyPressed(Keys.SHIFT_RIGHT)) {
			shiftHeld = true;
		}
		if (Gdx.input.isKeyJustPressed(Keys.W)) {
			imgY += 10;
		}
		if (Gdx.input.isKeyJustPressed(Keys.S)) {
			imgY -= 10;
		}
		if (Gdx.input.isKeyJustPressed(Keys.A)) {
			if (shiftHeld) {
				imgAngle += 5;
			} else {
				imgX -=10;
			}
		}
		if (Gdx.input.isKeyJustPressed(Keys.D)) {
			if (shiftHeld) {
				imgAngle -= 5;
			} else {
				imgX +=10;
			}
		}
		
	}
	
	@Override
	public void create () {
		batch = new SpriteBatch();
		tex = new Texture("badlogic.jpg");
		img = new TextureRegion(tex);
		sprite = new Sprite(img);
		imgWidth = tex.getWidth();
		imgHeight = tex.getHeight();
		imgOrgX = imgWidth/2;
		imgOrgY = imgHeight/2;
		sprite.setPosition(Gdx.graphics.getWidth()/2-sprite.getWidth()/2,Gdx.graphics.getHeight()/2 - sprite.getHeight()/2);
		WIDTH = Gdx.graphics.getWidth();
		HEIGHT = Gdx.graphics.getHeight();
		imgX = 0;
		imgY = 0;
		imgAngle = 0;
		cam = new OrthographicCamera(WIDTH,HEIGHT);
		InputHandler handler1 = new InputHandler(cam,batch);
		Gdx.input.setInputProcessor(handler1);
		cam.translate(WIDTH/2,HEIGHT/2);
		cam.update();
		batch.setProjectionMatrix(cam.combined);
		shaker = new CameraShake(cam, 100, batch, null, 10, 2);
	}

	@Override
	public void render () {
		Gdx.gl.glClearColor(1, 0, 0, 1);
		Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
		if (Gdx.input.isKeyJustPressed(Keys.SPACE)) {
			shaker.start();
		}
		shaker.play();
		handleInput();
		batch.begin();
		batch.draw(img, imgX, imgY, imgOrgX, imgOrgY, imgWidth, imgHeight, 1, 1, imgAngle);
		batch.end();
	}
	
	@Override
	public void dispose () {
		batch.dispose();
		//img.dispose();
	}
}
