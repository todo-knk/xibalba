package me.dannytatom.xibalba.components;

import com.badlogic.ashley.core.Component;

public class AttributesComponent extends Component {
  public final String name;
  public final int speed;
  public final int vision;
  public final int maxHealth;
  public int toughness;
  public int damage;
  public int health;
  public int energy;

  /**
   * Holds entity attributes.
   *
   * @param speed The entity's speed
   */
  public AttributesComponent(String name, int speed, int vision, int maxHealth, int toughness, int damage) {
    this.energy = speed;
    this.name = name;
    this.speed = speed;
    this.vision = vision;
    this.maxHealth = maxHealth;
    this.health = maxHealth;
    this.toughness = toughness;
    this.damage = damage;
  }
}