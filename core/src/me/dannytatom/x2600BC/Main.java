package me.dannytatom.x2600BC;

import com.badlogic.ashley.core.Engine;
import com.badlogic.ashley.core.Entity;
import com.badlogic.ashley.core.Family;
import com.badlogic.ashley.utils.ImmutableArray;
import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator;
import me.dannytatom.x2600BC.components.PositionComponent;
import me.dannytatom.x2600BC.components.VisualComponent;
import me.dannytatom.x2600BC.generators.CaveGenerator;
import me.dannytatom.x2600BC.systems.MovementSystem;

import java.util.Map;

public class Main extends ApplicationAdapter {
    private static int SPRITE_WIDTH = 24;
    private static int SPRITE_HEIGHT = 24;

    Engine engine;
    SpriteBatch batch;
    BitmapFont font;
    OrthographicCamera camera;
    Entity player;
    Texture ground;
    Texture wallTop;
    Texture wallFront;
    int[][] map;

    @Override
    public void create() {
        batch = new SpriteBatch();

        // Set font
        FreeTypeFontGenerator generator = new FreeTypeFontGenerator(Gdx.files.internal("fonts/Monaco.ttf"));
        font = generator.generateFont(new FreeTypeFontGenerator.FreeTypeFontParameter());
        font.setColor(Color.WHITE);
        generator.dispose();

        // Generate cave & find player starting position
        CaveGenerator cave = new CaveGenerator(40, 30);
        map = cave.generate();
        Map<String, Integer> startingPosition = cave.findPlayerStart();

        // Initialize textures
        ground = new Texture("sprites/ground.png");
        wallTop = new Texture("sprites/wall_top.png");
        wallFront = new Texture("sprites/wall_front.png");

        // Setup camera
        camera = new OrthographicCamera(Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
        camera.update();

        // Add player entity
        player = new Entity();
        player.add(new PositionComponent(startingPosition.get("x"), startingPosition.get("y")));
        player.add(new VisualComponent("sprites/player.png"));

        // Setup engine
        engine = new Engine();
        engine.addSystem(new MovementSystem(map));
        engine.addEntity(player);
    }

    @Override
    public void render() {
        // Get all entities with a position & visual component
        ImmutableArray<Entity> entities = engine.getEntitiesFor(Family.all(PositionComponent.class, VisualComponent.class).get());

        // Clear screen
        Gdx.gl.glClearColor(0, 0, 0, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        // Update Ashley engine
        engine.update(Gdx.graphics.getDeltaTime());

        // Update camera
        camera.position.set(player.getComponent(PositionComponent.class).x * SPRITE_WIDTH,
                player.getComponent(PositionComponent.class).y * SPRITE_HEIGHT,
                0);
        camera.update();

        // Draw shit
        batch.begin();
        batch.setProjectionMatrix(camera.combined);

        // Draw map
        for (int x = 0; x < map.length; x++) {
            for (int y = 0; y < map[x].length; y++) {
                int tile = map[x][y];
                int realX = x * SPRITE_WIDTH;
                int realY = y * SPRITE_HEIGHT;

                switch (tile) {
                    case Constants.EMPTINESS:
                        break;
                    case Constants.GROUND:
                        batch.draw(ground, realX, realY);
                        break;
                    case Constants.WALL_TOP:
                        batch.draw(wallTop, realX, realY);
                        break;
                    case Constants.WALL_FRONT:
                        batch.draw(wallFront, realX, realY);
                        break;
                }
            }
        }

        // Iterate entities with a Position & Visual component
        // and draw them
        for (Entity entity : entities) {
            PositionComponent position = Mappers.position.get(entity);
            VisualComponent visual = Mappers.visual.get(entity);

            int realX = position.x * SPRITE_WIDTH;
            int realY = position.y * SPRITE_HEIGHT;

            batch.draw(visual.sprite, realX, realY);
        }

        batch.end();
    }
}
