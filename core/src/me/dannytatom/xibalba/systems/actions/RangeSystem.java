package me.dannytatom.xibalba.systems.actions;

import aurelienribon.tweenengine.Tween;
import aurelienribon.tweenengine.TweenCallback;
import com.badlogic.ashley.core.Entity;
import com.badlogic.ashley.core.Family;
import com.badlogic.gdx.math.Vector2;
import me.dannytatom.xibalba.Main;
import me.dannytatom.xibalba.components.AttributesComponent;
import me.dannytatom.xibalba.components.PositionComponent;
import me.dannytatom.xibalba.components.VisualComponent;
import me.dannytatom.xibalba.components.actions.RangeComponent;
import me.dannytatom.xibalba.systems.UsesEnergySystem;
import me.dannytatom.xibalba.utils.ComponentMappers;
import me.dannytatom.xibalba.utils.SpriteAccessor;
import me.dannytatom.xibalba.world.WorldManager;

import java.util.Objects;

public class RangeSystem extends UsesEnergySystem {
  public RangeSystem() {
    super(Family.all(RangeComponent.class).get());
  }

  @Override
  protected void processEntity(Entity entity, float deltaTime) {
    RangeComponent range = ComponentMappers.range.get(entity);
    AttributesComponent attributes = ComponentMappers.attributes.get(entity);

    if (range.position != null && !entity.isScheduledForRemoval()) {
      Entity target = WorldManager.entityHelpers.getEnemyAt(range.position);

      if (target != null) {
        WorldManager.combatHelpers.range(entity, target, range.bodyPart, range.item, range.skill);
      }

      if (Objects.equals(range.skill, "throwing")) {
        ComponentMappers.item.get(range.item).throwing = false;
        doThrowAnimation(entity, range.item, range.position, false);
      } else {
        if (target == null) {
          doThrowAnimation(entity, range.item, range.position, false);
        } else {
          doThrowAnimation(entity, range.item, range.position, true);
        }
      }
    }

    attributes.energy -= RangeComponent.COST;
    entity.remove(RangeComponent.class);
  }

  private void doThrowAnimation(Entity entity, Entity item, Vector2 position, boolean destroy) {
    WorldManager.state = WorldManager.State.WAITING;

    // We have to set the items position before starting the tween since who knows wtf
    // position it had before it ended up in your inventory.
    PositionComponent throwerPosition = ComponentMappers.position.get(entity);
    WorldManager.entityHelpers.updatePosition(item, throwerPosition.pos);

    VisualComponent itemVisual = ComponentMappers.visual.get(item);

    Tween.to(itemVisual.sprite, SpriteAccessor.XY, .5f).target(
        position.x * Main.SPRITE_WIDTH, position.y * Main.SPRITE_HEIGHT
    ).setCallback(
        (type, source) -> {
          if (type == TweenCallback.COMPLETE) {
            if (destroy) {
              WorldManager.inventoryHelpers.destroyItem(entity, item);
            } else {
              WorldManager.inventoryHelpers.dropItem(entity, item, position);
            }

            WorldManager.state = WorldManager.State.PLAYING;
          }
        }
    ).start(Main.tweenManager);
  }
}
