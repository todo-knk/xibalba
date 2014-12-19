package me.dannytatom.x2600BC.screens;

import com.badlogic.ashley.core.Engine;
import com.badlogic.ashley.core.Entity;
import com.badlogic.ashley.core.Family;
import com.badlogic.ashley.utils.ImmutableArray;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.InputProcessor;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Vector2;
import me.dannytatom.x2600BC.Main;
import me.dannytatom.x2600BC.components.*;
import me.dannytatom.x2600BC.factories.MobFactory;
import me.dannytatom.x2600BC.map.CaveGenerator;
import me.dannytatom.x2600BC.map.Map;
import me.dannytatom.x2600BC.systems.BrainSystem;
import me.dannytatom.x2600BC.systems.MovementSystem;
import me.dannytatom.x2600BC.systems.ai.TargetSystem;
import me.dannytatom.x2600BC.systems.ai.WanderSystem;
import me.dannytatom.x2600BC.utils.ComponentMappers;

class PlayScreen implements Screen, InputProcessor {
  private static final int SPRITE_WIDTH = 24;
  private static final int SPRITE_HEIGHT = 24;

  private final Main game;
  private OrthographicCamera camera;
  private SpriteBatch batch;
  private Engine engine;
  private Entity player;
  private CaveGenerator cave;
  private Map map;
  private MobFactory mobFactory;

  /**
   * Play Screen.
   *
   * @param main Instance of Main class
   */
  public PlayScreen(Main main) {
    game = main;
    engine = new Engine();
    batch = new SpriteBatch();

    // Setup input
    Gdx.input.setInputProcessor(this);

    // Setup factories
    mobFactory = new MobFactory(game.assets);

    // Generate cave
    cave = new CaveGenerator(game.assets.get("sprites/cave.atlas"), 40, 30);
    map = new Map(engine, cave.geometry, cave.map);

    // Setup engine
    engine.addSystem(new MovementSystem(map));
    engine.addSystem(new BrainSystem(map));
    engine.addSystem(new WanderSystem(map));
    engine.addSystem(new TargetSystem(map));

    // Setup camera
    camera = new OrthographicCamera(Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
    camera.update();

    // Add player entity
    Vector2 startingPosition = map.findPlayerStart();
    player = new Entity();
    player.add(new PlayerComponent());
    player.add(new PositionComponent((int) startingPosition.x, (int) startingPosition.y));
    player.add(new MovementComponent());
    player.add(new VisualComponent(game.assets.get("sprites/player.png")));
    player.add(new AttributesComponent(5, 100));
    engine.addEntity(player);

    spawnMobs();
  }

  @Override
  public void render(float delta) {
    // Clear screen
    Gdx.gl.glClearColor(0, 0, 0, 1);

    // Don't update any entities until the player has
    // an action to take
    if (game.executeTurn) {
      // Let the systems run!
      engine.update(delta);

      // Get all entities with energy to spend
      ImmutableArray<Entity> entities =
          engine.getEntitiesFor(Family.all(AttributesComponent.class).get());

      // Give energy back
      for (Entity entity : entities) {
        AttributesComponent attributes = ComponentMappers.attributes.get(entity);
        attributes.energy += attributes.speed;
      }

      // Turn over
      game.executeTurn = false;
    }

    // Get player position for camera
    PositionComponent playerPosition = player.getComponent(PositionComponent.class);

    // Update camera
    camera.position.set(playerPosition.x * SPRITE_WIDTH, playerPosition.y * SPRITE_HEIGHT, 0);
    camera.update();

    renderMap();
  }

  void renderMap() {
    batch.setProjectionMatrix(camera.combined);

    Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

    batch.begin();

    // Draw map
    for (int x = 0; x < cave.map.length; x++) {
      for (int y = 0; y < cave.map[x].length; y++) {
        batch.draw(cave.map[x][y].sprite, x * SPRITE_WIDTH, y * SPRITE_HEIGHT);
      }
    }

    // Iterate entities with a Position & Visual component
    // and draw them
    ImmutableArray<Entity> entities =
        engine.getEntitiesFor(Family.all(PositionComponent.class, VisualComponent.class).get());

    for (Entity entity : entities) {
      PositionComponent position = ComponentMappers.position.get(entity);
      VisualComponent visual = ComponentMappers.visual.get(entity);

      batch.draw(visual.sprite, position.x * SPRITE_WIDTH,
          (position.y * SPRITE_HEIGHT) + (SPRITE_HEIGHT / 2));
    }

    batch.end();
  }

  void spawnMobs() {
    for (int i = 0; i < 20; i++) {
      Vector2 pos = map.findMobStart();
      Entity mob = mobFactory.spawn("spiderMonkey", (int) pos.x, (int) pos.y);

      engine.addEntity(mob);
    }
  }

  @Override
  public void resize(int width, int height) {

  }

  @Override
  public void show() {

  }

  @Override
  public void hide() {

  }

  @Override
  public void pause() {

  }

  @Override
  public void resume() {

  }

  @Override
  public void dispose() {
    batch.dispose();
  }

  @Override
  public boolean keyDown(int keyCode) {
    MovementComponent movement = player.getComponent(MovementComponent.class);

    switch (keyCode) {
      case Input.Keys.BACKSLASH:
        game.debug ^= true;
        break;
      case Input.Keys.SPACE:
        game.executeTurn = true;
        break;
      case Input.Keys.K:
        movement.direction = "N";
        game.executeTurn = true;
        break;
      case Input.Keys.U:
        movement.direction = "NE";
        game.executeTurn = true;
        break;
      case Input.Keys.L:
        movement.direction = "E";
        game.executeTurn = true;
        break;
      case Input.Keys.N:
        movement.direction = "SE";
        game.executeTurn = true;
        break;
      case Input.Keys.J:
        movement.direction = "S";
        game.executeTurn = true;
        break;
      case Input.Keys.B:
        movement.direction = "SW";
        game.executeTurn = true;
        break;
      case Input.Keys.H:
        movement.direction = "W";
        game.executeTurn = true;
        break;
      case Input.Keys.Y:
        movement.direction = "NW";
        game.executeTurn = true;
        break;
      default:
    }

    return true;
  }

  @Override
  public boolean keyUp(int keyCode) {
    return false;
  }

  @Override
  public boolean keyTyped(char character) {
    return false;
  }

  @Override
  public boolean touchDown(int screenX, int screenY, int pointer, int button) {
    return false;
  }

  @Override
  public boolean touchUp(int screenX, int screenY, int pointer, int button) {
    return false;
  }

  @Override
  public boolean touchDragged(int screenX, int screenY, int pointer) {
    return false;
  }

  @Override
  public boolean mouseMoved(int screenX, int screenY) {
    return false;
  }

  @Override
  public boolean scrolled(int amount) {
    return false;
  }
}
