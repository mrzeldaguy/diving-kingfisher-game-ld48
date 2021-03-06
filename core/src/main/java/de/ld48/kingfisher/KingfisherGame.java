package de.ld48.kingfisher;

import com.badlogic.ashley.core.Engine;
import com.badlogic.ashley.core.Entity;
import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.graphics.*;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.badlogic.gdx.utils.viewport.Viewport;
import de.ld48.kingfisher.component.BirdStateComponent;
import de.ld48.kingfisher.component.PositionComponent;
import de.ld48.kingfisher.component.StaticPositionComponent;
import de.ld48.kingfisher.component.VelocityComponent;
import de.ld48.kingfisher.system.MovementSystem;
import de.ld48.kingfisher.system.WindMovementSystem;

/**
 * {@link com.badlogic.gdx.ApplicationListener} implementation shared by all platforms.
 */
public class KingfisherGame extends ApplicationAdapter {
    public static final int Y_START = 340;

    private ShapeRenderer shapeRenderer;
    private SpriteBatch spriteBatch;
    private Engine engine;
    private Entity playerEntity;
    private MovementSystem movementSystem;
    private WindMovementSystem windMovementSystem;

    private Camera camera;
    private final float LERP = .6f;
    private Viewport viewport;
    private Stage stage;

    private Music music;

    private Texture background;
    private Texture branch;
    private Entity branchEntity;

    @Override
    public void create() {
        super.create();
        shapeRenderer = new ShapeRenderer();
        spriteBatch = new SpriteBatch();

        playerEntity = new Entity();
        playerEntity.add(new PositionComponent(220, Y_START));
        playerEntity.add(new VelocityComponent());
        playerEntity.add(new BirdStateComponent(BirdStateComponent.BirdState.INITIAL));

        branchEntity = new Entity();
        branchEntity.add(new PositionComponent(0, Y_START));
        branchEntity.add(new StaticPositionComponent(0, Y_START));

        engine = new Engine();
        engine.addEntity(playerEntity);
        engine.addEntity(branchEntity);
        movementSystem = new MovementSystem();
        windMovementSystem = new WindMovementSystem();
        engine.addSystem(movementSystem);
        engine.addSystem(windMovementSystem);

        camera = new OrthographicCamera(640, 480);
        viewport = new FitViewport(640, 480, camera);
        stage = new Stage(viewport);

        music = Gdx.audio.newMusic(Gdx.files.internal("dumdidum.wav"));
        music.setVolume(.3f);
        music.setLooping(true);
        music.play();

        background = new Texture("bg2.png");
        branch = new Texture("branchalt.png");
    }

    @Override
    public void resize(int width, int height) {
        viewport.update(width, height);
    }

    @Override
    public void render() {
        super.render();
        Gdx.gl.glClearColor(0.3f, 0.3f, 1, 0);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        float deltaTime = Gdx.graphics.getDeltaTime();
        engine.update(deltaTime);

        PositionComponent branchPositionComponent = branchEntity.getComponent(PositionComponent.class);
        PositionComponent playerPositionComponent = playerEntity.getComponent(PositionComponent.class);
        VelocityComponent playerVelocityComponent = playerEntity.getComponent(VelocityComponent.class);
        BirdStateComponent playerStateComponent = playerEntity.getComponent(BirdStateComponent.class);

        if (Gdx.input.isKeyJustPressed(Input.Keys.LEFT)) {
            playerVelocityComponent.x = -100;
        } else if (Gdx.input.isKeyJustPressed(Input.Keys.RIGHT)) {
            playerVelocityComponent.x = 100;
        }

        if (Gdx.input.isKeyJustPressed(Input.Keys.SPACE)) {
            if (playerStateComponent.state.equals(BirdStateComponent.BirdState.INITIAL)) {
                playerVelocityComponent.y = 100;
                playerStateComponent.nextState();
            }
            if (playerStateComponent.state.equals(BirdStateComponent.BirdState.ASCENDING)) {
                playerVelocityComponent.y = 100;
            }
        }

        Vector3 position = camera.position;
        position.x += (playerPositionComponent.x - position.x + 50) * LERP * deltaTime;
        position.y += (playerPositionComponent.y - position.y) * LERP * deltaTime;
        camera.update();

        viewport.apply();
        shapeRenderer.setProjectionMatrix(camera.combined);
        spriteBatch.setProjectionMatrix(camera.combined);
        spriteBatch.begin();
        spriteBatch.draw(background, -background.getWidth() / 2, 0);
        spriteBatch.draw(branch, branchPositionComponent.x, branchPositionComponent.y);
        spriteBatch.end();

        shapeRenderer.setColor(Color.RED);
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
        shapeRenderer.rect(playerPositionComponent.x, playerPositionComponent.y, 100, 100);
        shapeRenderer.end();

        stage.getViewport().apply();
        stage.draw();
    }
}