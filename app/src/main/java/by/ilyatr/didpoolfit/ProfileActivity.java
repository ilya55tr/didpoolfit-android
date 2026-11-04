package by.ilyatr.didpoolfit;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import java.util.Calendar;
import java.util.Locale;
import java.util.TimeZone;

public class ProfileActivity extends AppCompatActivity {
    private UserStorage storage;
    private RecyclerView list;
    private boolean leaving;

    @Override protected void onCreate(Bundle state) {
        super.onCreate(state);
        setContentView(R.layout.activity_profile);
        ProfileUi.insets(this);
        storage = new UserStorage(this);
        list = findViewById(R.id.profile_list);
        list.setLayoutManager(new LinearLayoutManager(this));
        findViewById(R.id.profile_back).setOnClickListener(v -> openHome());
        findViewById(R.id.homeNav).setOnClickListener(v -> openHome());
        findViewById(R.id.profileNav).setSelected(true);
        findViewById(R.id.activityNav).setOnClickListener(v -> startActivity(new Intent(this, ActivityHistoryActivity.class)));
        for (int id : new int[]{R.id.searchNav, R.id.cameraNav}) {
            findViewById(id).setOnClickListener(v -> Toast.makeText(this, R.string.coming_soon, Toast.LENGTH_SHORT).show());
        }
        findViewById(R.id.profile_more).setOnClickListener(this::showAccountMenu);
    }

    @Override protected void onResume() {
        super.onResume();
        UserStorage.User user = ProfileUi.requireUser(this, storage);
        if (user != null) list.setAdapter(new ProfileAdapter(user));
    }

    private void openHome() {
        startActivity(new Intent(this, HomeActivity.class).addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP));
        finish();
    }

    private void showAccountMenu(View anchor) {
        if (leaving) return;
        PopupMenu menu = new PopupMenu(this, anchor);
        menu.getMenu().add(R.string.log_out);
        menu.setOnMenuItemClickListener(item -> {
            new MaterialAlertDialogBuilder(this)
                    .setTitle(R.string.confirm_logout_title)
                    .setMessage(R.string.confirm_logout_message)
                    .setNegativeButton(R.string.cancel, null)
                    .setPositiveButton(R.string.log_out, (dialog, which) -> logout())
                    .show();
            return true;
        });
        menu.show();
    }

    private void logout() {
            if (leaving) return;
            leaving = true;
            UserStorage.WRITES.execute(() -> {
                try {
                    String email = storage.logout();
                    runOnUiThread(() -> {
                        if (isFinishing() || isDestroyed()) return;
                        startActivity(new Intent(this, LoginActivity.class)
                                .putExtra(LoginActivity.EXTRA_EMAIL, email)
                                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK));
                        finish();
                    });
                } catch (UserStorage.StorageException e) {
                    runOnUiThread(() -> {
                        leaving = false;
                        Toast.makeText(this, R.string.error_storage, Toast.LENGTH_LONG).show();
                    });
                }
            });
    }


    private final class ProfileAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
        private final UserStorage.User user;
        ProfileAdapter(UserStorage.User user) { this.user = user; }
        @Override public int getItemCount() { return 5; }
        @Override public int getItemViewType(int position) { return Math.min(position, 2); }

        @Override public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int type) {
            int layout = type == 0 ? R.layout.item_profile_header : type == 1 ? R.layout.item_profile_stats : R.layout.item_profile_section;
            return new RecyclerView.ViewHolder(LayoutInflater.from(parent.getContext()).inflate(layout, parent, false)) {};
        }

        @Override public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
            View view = holder.itemView;
            if (position == 0) {
                ((TextView) view.findViewById(R.id.profile_name)).setText(user.fullName);
                ((TextView) view.findViewById(R.id.profile_email)).setText(user.email);
                ProfilePhotos.show(ProfileActivity.this, view.findViewById(R.id.profile_avatar), user.photoFileName, user.gender);
                view.findViewById(R.id.profile_edit).setOnClickListener(v -> startActivity(new Intent(ProfileActivity.this, EditProfileActivity.class)));
            } else if (position == 1) {
                ((TextView) view.findViewById(R.id.profile_height)).setText(getString(R.string.profile_height_stat, user.height));
                ((TextView) view.findViewById(R.id.profile_weight)).setText(getString(R.string.profile_weight_stat, user.weight));
                Calendar birth = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
                birth.setTimeInMillis(user.dateOfBirth);
                Calendar now = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
                int age = now.get(Calendar.YEAR) - birth.get(Calendar.YEAR);
                if (now.get(Calendar.MONTH) < birth.get(Calendar.MONTH) || (now.get(Calendar.MONTH) == birth.get(Calendar.MONTH)
                        && now.get(Calendar.DAY_OF_MONTH) < birth.get(Calendar.DAY_OF_MONTH))) age--;
                ((TextView) view.findViewById(R.id.profile_age)).setText(getString(R.string.profile_age_stat, Math.max(0, age)));
            } else {
                int title = position == 2 ? R.string.profile_account : position == 3 ? R.string.profile_notifications : R.string.profile_other;
                ((TextView) view.findViewById(R.id.section_title)).setText(title);
                LinearLayout rows = view.findViewById(R.id.section_rows);
                rows.removeAllViews();
                if (position == 2) {
                    addRow(rows, R.string.personal_data, R.drawable.ic_personal_data, false);
                    addRow(rows, R.string.achievement, R.drawable.ic_achievement, false);
                    addRow(rows, R.string.activity_history, R.drawable.ic_activity_history, false);
                    addRow(rows, R.string.workout_progress, R.drawable.ic_workout_progress, false);
                } else if (position == 3) addRow(rows, R.string.pop_up_notification, R.drawable.ic_notification, true);
                else {
                    addRow(rows, R.string.contact_us, R.drawable.ic_contact_us, false);
                    addRow(rows, R.string.privacy_policy, R.drawable.ic_privacy_policy, false);
                    addRow(rows, R.string.settings, R.drawable.ic_settings, false);
                }
            }
        }

        private void addRow(LinearLayout rows, int title, int icon, boolean notification) {
            View row = getLayoutInflater().inflate(R.layout.item_profile_setting, rows, false);
            ((TextView) row.findViewById(R.id.setting_label)).setText(title);
            ((ImageView) row.findViewById(R.id.setting_icon)).setImageResource(icon);
            row.findViewById(R.id.setting_switch).setVisibility(notification ? View.VISIBLE : View.GONE);
            row.findViewById(R.id.setting_chevron).setVisibility(notification ? View.GONE : View.VISIBLE);
            row.setOnClickListener(v -> {
                if (title == R.string.activity_history) startActivity(new Intent(ProfileActivity.this, ActivityHistoryActivity.class));
                else Toast.makeText(ProfileActivity.this,
                        getString(R.string.feature_coming_soon, getString(title)), Toast.LENGTH_SHORT).show();
            });
            rows.addView(row);
        }
    }
}
