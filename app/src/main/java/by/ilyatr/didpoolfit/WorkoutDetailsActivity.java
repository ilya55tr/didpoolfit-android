package by.ilyatr.didpoolfit;

import android.os.Bundle;
import android.widget.TextView;
import android.widget.Button;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Random;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import com.google.android.material.snackbar.Snackbar;

import by.ilyatr.didpoolfit.adapter.EquipmentAdapter;
import by.ilyatr.didpoolfit.adapter.ExerciseRowAdapter;
import by.ilyatr.didpoolfit.model.Equipment;
import by.ilyatr.didpoolfit.model.Exercise;
import by.ilyatr.didpoolfit.model.ExerciseRow;
import by.ilyatr.didpoolfit.model.WorkoutPlan;

public class WorkoutDetailsActivity extends AppCompatActivity {
    private final ExecutorService executor = Executors.newSingleThreadExecutor();
    // Indices match the exercise constants in WorkoutPlan.
    private static final String[] EXERCISE_NAMES = {
            "Warm Up", "Jumping Jack", "Skipping", "Squats", "Arm Raises",
            "Rest and Drink", "Incline Push-Ups", "Push-Ups", "Cobra Stretch"
    };
    private static final int[] EXERCISE_IMAGES = {
            R.drawable.warm_up, R.drawable.jumping_jack, R.drawable.skipping,
            R.drawable.squats, R.drawable.arm_raises, R.drawable.rest_and_drink,
            R.drawable.incline_push_ups, R.drawable.push_ups, R.drawable.cobra_stretch
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_workout_details);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets safeArea = insets.getInsets(
                    WindowInsetsCompat.Type.systemBars() | WindowInsetsCompat.Type.displayCutout());
            v.setPadding(safeArea.left, safeArea.top, safeArea.right, safeArea.bottom);
            return insets;
        });
        UserStorage storage = new UserStorage(this);
        UserStorage.User user = ProfileUi.requireUser(this, storage);
        if (user == null) return;

        WorkoutPlan plan = WorkoutPlan.generate(
                getIntent().getIntExtra("exercises", 10),
                getIntent().getIntExtra("mins", 20),
                getIntent().getLongExtra("seed", 0L));
        String title = getIntent().getStringExtra("title");
        TextView workoutName = findViewById(R.id.workout_name);
        workoutName.setText(title == null ? getString(R.string.default_workout_title) : title);
        TextView description = findViewById(R.id.workout_description);
        description.setText(getString(R.string.workout_description,
                plan.steps.size(), plan.minutes, plan.minutes * 5));

        List<Equipment> equipment = new ArrayList<>();
        String[] equipmentNames = {"Barbell", "Skipping Rope", "Water Bottle"};
        int[] equipmentImages = {
                R.drawable.barbell, R.drawable.skipping_rope, R.drawable.bottle
        };
        for (int i = 0; i < equipmentNames.length; i++) {
            equipment.add(new Equipment(equipmentNames[i], equipmentImages[i]));
        }
        Random equipmentRandom = new Random();
        int extraEquipmentCount = equipmentRandom.nextInt(3);
        for (int i = 0; i < extraEquipmentCount; i++) {
            int equipmentIndex = equipmentRandom.nextInt(equipmentNames.length);
            equipment.add(new Equipment(
                    equipmentNames[equipmentIndex], equipmentImages[equipmentIndex]));
        }
        TextView equipmentAmount = findViewById(R.id.equipments_amount);
        equipmentAmount.setText(getString(R.string.amount_items, equipment.size()));

        RecyclerView equipmentList = findViewById(R.id.equipments_recycler_view);
        equipmentList.setLayoutManager(
                new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        equipmentList.setAdapter(new EquipmentAdapter(equipment));

        List<ExerciseRow> rows = new ArrayList<>();
        int index = 0;
        for (int set = 0; set < plan.setCount; set++) {
            rows.add(new ExerciseRow(getString(R.string.set_title, set + 1)));
            int setSize = plan.getSetSize(set);
            for (int i = 0; i < setSize; i++) {
                WorkoutPlan.Step step = plan.steps.get(index++);
                String time = String.format(Locale.ROOT, "%02d:%02d",
                        step.seconds / 60, step.seconds % 60);
                rows.add(new ExerciseRow(new Exercise(EXERCISE_NAMES[step.exercise],
                        time, EXERCISE_IMAGES[step.exercise])));
            }
        }
        TextView setAmount = findViewById(R.id.exercises_amount);
        setAmount.setText(getString(R.string.set_amount, plan.setCount));

        RecyclerView exerciseList = findViewById(R.id.exercises_recycler_view);
        exerciseList.setLayoutManager(new LinearLayoutManager(this));
        exerciseList.setNestedScrollingEnabled(false);
        exerciseList.setAdapter(new ExerciseRowAdapter(rows));
        Button start = findViewById(R.id.startWorkoutButton);
        String workoutTitle = workoutName.getText().toString();
        long seed = getIntent().getLongExtra("seed", 0L);
        start.setOnClickListener(v -> {
            long pressedAt = System.currentTimeMillis();
            start.setEnabled(false);
            executor.execute(() -> {
                try {
                    boolean saved = storage.addWorkout(user.email, workoutTitle, plan.steps.size(), plan.minutes, seed, pressedAt);
                    runOnUiThread(() -> {
                        if (isFinishing() || isDestroyed()) return;
                        start.setEnabled(true);
                        if (saved) Snackbar.make(findViewById(R.id.main), R.string.workout_added, Snackbar.LENGTH_LONG)
                                .setAnchorView(start)
                                .setAction(R.string.history_action, action -> startActivity(new android.content.Intent(this, ActivityHistoryActivity.class)))
                                .show();
                        else Toast.makeText(this, R.string.error_session, Toast.LENGTH_LONG).show();
                    });
                } catch (UserStorage.StorageException e) {
                    runOnUiThread(() -> {
                        if (isFinishing() || isDestroyed()) return;
                        start.setEnabled(true);
                        Toast.makeText(this, R.string.error_storage, Toast.LENGTH_LONG).show();
                    });
                }
            });
        });
    }

    @Override protected void onDestroy() { executor.shutdown(); super.onDestroy(); }
}
