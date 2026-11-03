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
import java.util.EnumSet;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import com.google.android.material.snackbar.Snackbar;

import by.ilyatr.didpoolfit.adapter.EquipmentAdapter;
import by.ilyatr.didpoolfit.adapter.ExerciseRowAdapter;
import by.ilyatr.didpoolfit.model.Equipment;
import by.ilyatr.didpoolfit.model.Equipment.EquipmentType;
import by.ilyatr.didpoolfit.model.Exercise;
import by.ilyatr.didpoolfit.model.ExerciseRow;
import by.ilyatr.didpoolfit.model.WorkoutPlan;

public class WorkoutDetailsActivity extends AppCompatActivity {
    private final EnumSet<EquipmentType> selected = EnumSet.noneOf(EquipmentType.class);

    private static final EquipmentType[] EXERCISE_EQUIPMENT = {
            EquipmentType.NONE, EquipmentType.NONE, EquipmentType.ROPE,
            EquipmentType.BARBELL, EquipmentType.BARBELL, EquipmentType.WATER,
            EquipmentType.NONE, EquipmentType.NONE, EquipmentType.NONE
    };

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
        equipment.add(new Equipment("Barbell", R.drawable.barbell, EquipmentType.BARBELL));
        equipment.add(new Equipment("Skipping Rope", R.drawable.skipping_rope, EquipmentType.ROPE));
        equipment.add(new Equipment("Water Bottle", R.drawable.bottle, EquipmentType.WATER));
        TextView equipmentAmount = findViewById(R.id.equipments_amount);
        equipmentAmount.setText(getString(R.string.amount_items, equipment.size()));

        if (savedInstanceState != null) {
            ArrayList<String> filters = savedInstanceState.getStringArrayList("equipment_filters");
            if (filters != null) {
                for (String filter : filters) selected.add(EquipmentType.valueOf(filter));
            }
        }

        RecyclerView equipmentList = findViewById(R.id.equipments_recycler_view);
        equipmentList.setLayoutManager(
                new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));

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
                        time, EXERCISE_IMAGES[step.exercise], EXERCISE_EQUIPMENT[step.exercise])));
            }
        }
        TextView setAmount = findViewById(R.id.exercises_amount);
        setAmount.setText(getString(R.string.set_amount, plan.setCount));

        RecyclerView exerciseList = findViewById(R.id.exercises_recycler_view);
        exerciseList.setLayoutManager(new LinearLayoutManager(this));
        exerciseList.setNestedScrollingEnabled(false);
        List<ExerciseRow> visibleRows = new ArrayList<>();
        ExerciseRowAdapter adapter = new ExerciseRowAdapter(visibleRows);
        exerciseList.setAdapter(adapter);
        Runnable refresh = () -> {
            int visibleSets = filterExercises(rows, visibleRows);
            setAmount.setText(getString(R.string.set_amount, visibleSets));
            findViewById(R.id.exercise_filter_empty).setVisibility(
                    visibleRows.isEmpty() ? android.view.View.VISIBLE : android.view.View.GONE);
            adapter.notifyDataSetChanged();
        };
        equipmentList.setAdapter(new EquipmentAdapter(equipment, selected, type -> {
            if (!selected.add(type)) selected.remove(type);
            refresh.run();
        }));
        refresh.run();
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

    private int filterExercises(List<ExerciseRow> rows, List<ExerciseRow> visibleRows) {
        visibleRows.clear();
        ExerciseRow pendingHeader = null;
        int visibleSets = 0;
        for (ExerciseRow row : rows) {
            if (row.getType() == ExerciseRow.HEADER) {
                pendingHeader = row;
            } else if (selected.isEmpty() || selected.contains(row.getExercise().getEquipmentType())) {
                if (pendingHeader != null) {
                    visibleRows.add(pendingHeader);
                    pendingHeader = null;
                    visibleSets++;
                }
                visibleRows.add(row);
            }
        }
        return visibleSets;
    }

    @Override protected void onSaveInstanceState(Bundle state) {
        super.onSaveInstanceState(state);
        ArrayList<String> filters = new ArrayList<>();
        for (EquipmentType type : selected) filters.add(type.name());
        state.putStringArrayList("equipment_filters", filters);
    }

    @Override protected void onDestroy() { executor.shutdown(); super.onDestroy(); }
}
