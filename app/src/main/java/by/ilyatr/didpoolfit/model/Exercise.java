package by.ilyatr.didpoolfit.model;

public class Exercise {

  private String name;
  private String amount;
  private Integer image;

  public Exercise(String name, String amount, Integer image) {
    this.name = name;
    this.amount = amount;
    this.image = image;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public String getAmount() {
    return amount;
  }

  public void setAmount(String amount) {
    this.amount = amount;
  }

  public Integer getImage() {
    return image;
  }

  public void setImage(Integer image) {
    this.image = image;
  }
}
