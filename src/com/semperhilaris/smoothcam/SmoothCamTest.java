
package com.semperhilaris.smoothcam;

import com.badlogic.gdx.ApplicationListener;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.Input.Peripheral;
import com.badlogic.gdx.graphics.GL10;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.BodyDef;
import com.badlogic.gdx.physics.box2d.BodyDef.BodyType;
import com.badlogic.gdx.physics.box2d.Box2DDebugRenderer;
import com.badlogic.gdx.physics.box2d.CircleShape;
import com.badlogic.gdx.physics.box2d.Fixture;
import com.badlogic.gdx.physics.box2d.FixtureDef;
import com.badlogic.gdx.physics.box2d.PolygonShape;
import com.badlogic.gdx.physics.box2d.World;

public class SmoothCamTest implements ApplicationListener {
	private OrthographicCamera camera;
	private SpriteBatch batch;
	private World world;
	private Box2DDebugRenderer debugRenderer;
	private Body body;
	private SmoothCamSubject player;
	private SmoothCamWorld scw;
	private SmoothCamDebugRenderer scDebug;

	@Override
	public void create () {
		float w = Gdx.graphics.getWidth();
		float h = Gdx.graphics.getHeight();

		debugRenderer = new Box2DDebugRenderer();

		camera = new OrthographicCamera(w / 2, h / 2);
		world = new World(new Vector2(0, 0), true);

		/* Initializing SmoothCam Debug Renderer */
		scDebug = new SmoothCamDebugRenderer();

		/* Creating the subject for the SmoothCamWorld */
		player = new SmoothCamSubject();

		/*
		 * Set the velocity radius for the subject.
		 * At max velocity, the camera will shift that much in the direction of the movement.
		 */
		player.setVelocityRadius(30f);

		/* Creating the SmoothCamWorld with the subject */
		scw = new SmoothCamWorld(player);

		BodyDef bodyDef = new BodyDef();
		bodyDef.type = BodyType.DynamicBody;
		bodyDef.fixedRotation = true;
		bodyDef.linearDamping = 1.0f;
		bodyDef.position.set(0, 0);
		body = world.createBody(bodyDef);
		CircleShape circle = new CircleShape();
		circle.setRadius(6f);
		FixtureDef fixtureDef = new FixtureDef();
		fixtureDef.shape = circle;
		fixtureDef.density = 0.5f;
		fixtureDef.friction = 0.4f;
		fixtureDef.restitution = 0.6f;
		Fixture fixture = body.createFixture(fixtureDef);
		circle.dispose();

		BodyDef groundBodyDef = new BodyDef();
		groundBodyDef.position.set(new Vector2(0, -50));
		Body groundBody = world.createBody(groundBodyDef);
		PolygonShape groundBox = new PolygonShape();
		groundBox.setAsBox(70f, 5.0f);
		groundBody.createFixture(groundBox, 0.0f);
		groundBox.dispose();

		/* Point of interest #1 */
		SmoothCamPoint testpoi = new SmoothCamPoint();
		testpoi.setPosition(0f, -50f);
		testpoi.setInnerRadius(70f);
		testpoi.setOuterRadius(200f);
		scw.addPoint(testpoi);

		/* Point of interest #2 */
		SmoothCamPoint testpoi2 = new SmoothCamPoint();
		testpoi2.setPosition(500f, 100f);
		testpoi2.setInnerRadius(50f);
		testpoi2.setOuterRadius(250f);
		scw.addPoint(testpoi2);

		/* Point of interest #3 */
		SmoothCamPoint testpoi3 = new SmoothCamPoint();
		testpoi3.setPosition(-30f, 400f);
		testpoi3.setInnerRadius(100f);
		testpoi3.setOuterRadius(140f);
		scw.addPoint(testpoi3);

		/* Point of interest #4 */
		SmoothCamPoint testpoi4 = new SmoothCamPoint();
		testpoi4.setPosition(280f, 400f);
		testpoi4.setInnerRadius(60f);
		testpoi4.setOuterRadius(140f);
		scw.addPoint(testpoi4);

		batch = new SpriteBatch();
	}

	@Override
	public void dispose () {
		batch.dispose();
	}

	@Override
	public void render () {
		if (Gdx.input.isPeripheralAvailable(Peripheral.Accelerometer)) {
			float accelX = Gdx.input.getAccelerometerX();
			float accelY = Gdx.input.getAccelerometerY();
			body.applyLinearImpulse(new Vector2(accelY * 4f, accelX * -4f), body.getLocalCenter());
		} else {
			if (Gdx.input.isKeyPressed(Keys.DPAD_LEFT)) body.applyLinearImpulse(new Vector2(-150f, 0f), body.getLocalCenter());
			if (Gdx.input.isKeyPressed(Keys.DPAD_RIGHT)) body.applyLinearImpulse(new Vector2(150f, 0f), body.getLocalCenter());
			if (Gdx.input.isKeyPressed(Keys.DPAD_UP)) body.applyLinearImpulse(new Vector2(0f, 150f), body.getLocalCenter());
			if (Gdx.input.isKeyPressed(Keys.DPAD_DOWN)) body.applyLinearImpulse(new Vector2(0f, -150f), body.getLocalCenter());
		}
		
		/*
		 * Updating the position and velocity of the SmoothCamSubject using Box2D.
		 * In this example, maximum velocity of the body is around 122, 
		 * so we have to divide by that value to get the relative value between -1 and 1 that we need for SmoothCamWorld.
		 * After that, update the SmoothCamWorld and use the new X/Y values to center the libGDX camera.
		 */
		player.setPosition(body.getPosition().x, body.getPosition().y);
		player.setVelocity(body.getLinearVelocity().x / 122f, body.getLinearVelocity().y / 122f);
		scw.update();
		camera.position.set(scw.getX(), scw.getY(), 0);
		camera.update();

		Gdx.gl.glClearColor(1, 1, 1, 1);
		Gdx.gl.glClear(GL10.GL_COLOR_BUFFER_BIT);

		world.step(1 / 60f, 6, 2);
		debugRenderer.render(world, camera.combined);
		
		/* Rendering the debug shapes for the SmoothCamWorld */
		scDebug.render(scw, camera.combined);
	}

	@Override
	public void resize (int width, int height) {
	}

	@Override
	public void pause () {
	}

	@Override
	public void resume () {
	}
}
