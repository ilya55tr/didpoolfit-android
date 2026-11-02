package by.ilyatr.didpoolfit;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

import by.ilyatr.didpoolfit.adapter.WorkoutItemAdapter;
import by.ilyatr.didpoolfit.model.WorkoutItem;

public class HomeActivity extends AppCompatActivity {

    ImageButton homeNav;
    BmiChartView bmiChart;
    SharedPreferences userData;
    TextView name;
    RecyclerView recyclerView;
    List<WorkoutItem> items = new ArrayList<>();
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_home);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        homeNav = findViewById(R.id.homeNav);
        homeNav.setSelected(true);

        bmiChart = findViewById(R.id.bmiChart);
        userData = getSharedPreferences("user", MODE_PRIVATE);
        showBmi();

        name = findViewById(R.id.name);
        name.setText(userData.getString("fullName", "null"));

        WorkoutItemAdapter adapter = new WorkoutItemAdapter(items);
        recyclerView = findViewById(R.id.workout_recycler_View);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);
        loadData();
    }

    private void showBmi() {
        float weight = userData.getFloat("weight", 0);
        int height = userData.getInt("height", 0);
        if (Float.isNaN(weight) || weight < 20 || weight > 300 || height < 50 || height > 250) {
            bmiChart.setBmi(Float.NaN);
            return;
        }

        float heightMeters = height / 100f;
        float bmi = weight / (heightMeters * heightMeters);
        bmiChart.setBmi(bmi);
    }

    private void loadData(){
        items.add(new WorkoutItem(
                "Fullbody Workout",
                14, 20,
                R.drawable.workout_fullbody));
        items.add(new WorkoutItem(
                "Lowerbody Workout",
                12, 40,
                R.drawable.workout_lowerbody));
        items.add(new WorkoutItem(
                "AB Workout",
                14, 20,
                R.drawable.workout_ab));
        items.add(new WorkoutItem(
                "Fullbody Workout",
                14, 20,
                R.drawable.workout_fullbody));
        items.add(new WorkoutItem(
                "Lowerbody Workout",
                12, 40,
                R.drawable.workout_lowerbody));
        items.add(new WorkoutItem(
                "AB Workout",
                14, 20,
                R.drawable.workout_ab));
    }
}
