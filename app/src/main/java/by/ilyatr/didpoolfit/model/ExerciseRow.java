package by.ilyatr.didpoolfit.model;

public class ExerciseRow {
  public static final int HEADER = 0;
  public static final int EXERCISE = 1;

  private final int type;
  private final String title;
  private final Exercise exercise;

  public ExerciseRow(String title) {
    this.type = HEADER;
    this.title = title;
    this.exercise = null;
  }

  public ExerciseRow(Exercise exercise) {
    this.type = EXERCISE;
    this.title = null;
    this.exercise = exercise;
  }

  public int getType() {
    return type;
  }

  public String getTitle() {
    return title;
  }

  public Exercise getExercise() {
    return exercise;
  }
}
