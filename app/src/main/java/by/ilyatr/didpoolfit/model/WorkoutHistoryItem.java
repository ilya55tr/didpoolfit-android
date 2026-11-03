package by.ilyatr.didpoolfit.model;

/** Запись о нажатии Start Workout, а не о завершении тренировки. */
public class WorkoutHistoryItem {
    public final String title;
    public final int exercises, minutes;
    public final long seed, startedAt;

    public WorkoutHistoryItem(String title, int exercises, int minutes, long seed, long startedAt) {
        this.title = title;
        this.exercises = exercises;
        this.minutes = minutes;
        this.seed = seed;
        this.startedAt = startedAt;
    }
}
