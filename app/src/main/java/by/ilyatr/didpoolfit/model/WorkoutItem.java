package by.ilyatr.didpoolfit.model;

public class WorkoutItem {
    private String title;
    private Integer exerciseAmount;
    private Integer time;
    private Integer image;
    private Integer calories;
    private final long seed;

    public WorkoutItem(String title, Integer exerciseAmount, Integer time, Integer image, Integer calories, long seed) {
        this.seed = seed;
        this.title = title;
        this.exerciseAmount = exerciseAmount;
        this.time = time;
        this.image = image;
        this.calories = calories;
    }

    public long getSeed() {
        return seed;
    }

    public Integer getCalories() {
        return calories;
    }

    public void setCalories(Integer calories) {
        this.calories = calories;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public Integer getExerciseAmount() {
        return exerciseAmount;
    }

    public void setExerciseAmount(Integer exerciseAmount) {
        this.exerciseAmount = exerciseAmount;
    }

    public Integer getTime() {
        return time;
    }

    public void setTime(Integer time) {
        this.time = time;
    }

    public Integer getImage() {
        return image;
    }

    public void setImage(Integer image) {
        this.image = image;
    }
}
