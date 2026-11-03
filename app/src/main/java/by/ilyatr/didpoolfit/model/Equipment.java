package by.ilyatr.didpoolfit.model;

public class Equipment {
  public enum EquipmentType {
    NONE, BARBELL, ROPE, WATER
  }

  private String name;
  private int image;

  private final EquipmentType equipmentType;

  public Equipment(String name, int image, EquipmentType equipmentType) {
    this.equipmentType = equipmentType;
    this.name = name;
    this.image = image;
  }

  public EquipmentType getEquipmentType() {
    return equipmentType;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public int getImage() {
    return image;
  }

  public void setImage(int image) {
    this.image = image;
  }
}
