package me.dannytatom.xibalba.systems.ai;

import com.badlogic.ashley.core.Entity;
import com.badlogic.ashley.core.Family;
import com.badlogic.ashley.systems.IteratingSystem;
import com.badlogic.gdx.math.Vector2;
import me.dannytatom.xibalba.components.PositionComponent;
import me.dannytatom.xibalba.components.actions.MovementComponent;
import me.dannytatom.xibalba.components.ai.BrainComponent;
import me.dannytatom.xibalba.components.ai.TargetComponent;
import me.dannytatom.xibalba.map.Map;
import me.dannytatom.xibalba.utils.ComponentMappers;
import org.xguzm.pathfinding.grid.GridCell;
import org.xguzm.pathfinding.grid.NavigationGrid;
import org.xguzm.pathfinding.grid.finders.AStarGridFinder;

import java.util.ArrayList;
import java.util.List;

public class TargetSystem extends IteratingSystem {
  private final Map map;

  /**
   * TargetSystem constructor.
   *
   * @param map The map we're on
   */
  public TargetSystem(Map map) {
    super(Family.all(TargetComponent.class, PositionComponent.class).get());

    this.map = map;
  }

  @Override
  protected void processEntity(Entity entity, float deltaTime) {
    BrainComponent brain = ComponentMappers.brain.get(entity);
    TargetComponent target = ComponentMappers.target.get(entity);

    // Create path to a random open cell on the map if one
    // doesn't already exist.
    if (brain.path == null || brain.path.isEmpty()) {
      PositionComponent position = ComponentMappers.position.get(entity);

      NavigationGrid<GridCell> grid = new NavigationGrid<>(map.createGridCells());
      AStarGridFinder<GridCell> finder = new AStarGridFinder<>(GridCell.class);

      brain.path = finder.findPath((int) position.pos.x, (int) position.pos.y,
          (int) target.pos.x, (int) target.pos.y, grid);
    }

    // If a path to the target could be found, start walking.
    // If it becomes blocked, reset the path.
    //
    // TODO: Instead of checking next cell, check any cell in the path that's in their vision
    if (brain.path != null) {
      GridCell cell = brain.path.get(0);

      if (cell.isWalkable()) {
        entity.add(new MovementComponent(new Vector2(cell.getX(), cell.getY())));

        List<GridCell> newPath = new ArrayList<>(brain.path);
        newPath.remove(cell);

        brain.path = newPath;
      } else {
        brain.path = null;
      }
    }
  }
}