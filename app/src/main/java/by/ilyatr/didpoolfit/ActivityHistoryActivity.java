package by.ilyatr.didpoolfit;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

import by.ilyatr.didpoolfit.adapter.WorkoutHistoryAdapter;
import by.ilyatr.didpoolfit.model.WorkoutHistoryItem;

public class ActivityHistoryActivity extends AppCompatActivity {
    private UserStorage storage;
    private RecyclerView list;

    @Override protected void onCreate(Bundle state) {
        super.onCreate(state);
        setContentView(R.layout.activity_history);
        ProfileUi.insets(this);
        storage = new UserStorage(this);
        ((TextView) findViewById(R.id.profile_title)).setText(R.string.activity_history);
        findViewById(R.id.profile_more).setVisibility(View.INVISIBLE);
        findViewById(R.id.profile_back).setOnClickListener(v -> finish());
        findViewById(R.id.activityNav).setSelected(true);
        findViewById(R.id.homeNav).setOnClickListener(v -> open(HomeActivity.class));
        findViewById(R.id.profileNav).setOnClickListener(v -> open(ProfileActivity.class));
        for (int id : new int[]{R.id.searchNav, R.id.cameraNav}) {
            findViewById(id).setOnClickListener(v -> Toast.makeText(this, R.string.coming_soon, Toast.LENGTH_SHORT).show());
        }
        list = findViewById(R.id.history_list);
        list.setLayoutManager(new LinearLayoutManager(this));
    }

    @Override protected void onResume() {
        super.onResume();
        if (ProfileUi.requireUser(this, storage) == null) return;
        try {
            List<WorkoutHistoryItem> history = storage.getHistory();
            list.setAdapter(new WorkoutHistoryAdapter(history));
            findViewById(R.id.history_empty).setVisibility(history.isEmpty() ? View.VISIBLE : View.GONE);
        } catch (UserStorage.StorageException e) {
            Toast.makeText(this, R.string.error_storage, Toast.LENGTH_LONG).show();
        }
    }

    private void open(Class<?> screen) {
        startActivity(new Intent(this, screen).addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP));
        finish();
    }
}
