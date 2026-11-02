package by.ilyatr.didpoolfit.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

/** A reproducible demo plan. All durations are seconds, including warm-up and rest. */
public final class WorkoutPlan {
    public static final int WARM_UP = 0;
    public static final int JUMPING_JACK = 1;
    public static final int SKIPPING = 2;
    public static final int SQUATS = 3;
    public static final int ARM_RAISES = 4;
    public static final int REST = 5;
    public static final int INCLINE_PUSH_UPS = 6;
    public static final int PUSH_UPS = 7;
    public static final int STRETCH = 8;

    public static final class Step {
        public final int exercise;
        public final int seconds;

        private Step(int exercise, int seconds) {
            this.exercise = exercise;
            this.seconds = seconds;
        }
    }

    public final List<Step> steps;
    public final int minutes;
    public final int setCount;

    private WorkoutPlan(List<Step> steps, int minutes) {
        this.steps = Collections.unmodifiableList(steps);
        this.minutes = minutes;
        this.setCount = (steps.size() + 4) / 5;
    }

    public int getSetSize(int set) {
        if (set < 0 || set >= setCount) {
            throw new IllegalArgumentException("Invalid set index");
        }
        return steps.size() / setCount + (set < steps.size() % setCount ? 1 : 0);
    }

    public static WorkoutPlan generate(int count, int minutes, long seed) {
        // Keep even malformed or missing Intent extras within a small, feasible plan.
        count = Math.max(3, Math.min(20, count));
        minutes = Math.max((count + 1) / 2, Math.min(count * 5, minutes));
        Random random = new Random(seed);
        List<Integer> pool = new ArrayList<>();
        Collections.addAll(pool, JUMPING_JACK, SKIPPING, SQUATS,
                ARM_RAISES, INCLINE_PUSH_UPS, PUSH_UPS);
        Collections.shuffle(pool, random);

        List<Integer> order = new ArrayList<>();
        order.add(WARM_UP);
        int cursor = 0;
        for (int i = 1; i < count - 1; i++) {
            if (count >= 5 && i == count / 2) {
                order.add(REST);
            } else {
                order.add(pool.get(cursor++ % pool.size()));
            }
        }
        order.add(STRETCH);

        // Reserve 30..300 seconds for every remaining row before choosing its duration.
        int remaining = minutes * 60;
        List<Integer> durations = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            int rowsLeft = count - i - 1;
            int min = Math.max(30, remaining - rowsLeft * 300);
            int max = Math.min(300, remaining - rowsLeft * 30);
            int seconds = min + random.nextInt(max - min + 1);
            durations.add(seconds);
            remaining -= seconds;
        }
        // Avoid assigning the leftover duration to the last exercise every time.
        Collections.shuffle(durations, random);
        List<Step> steps = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            steps.add(new Step(order.get(i), durations.get(i)));
        }
        return new WorkoutPlan(steps, minutes);
    }
}
