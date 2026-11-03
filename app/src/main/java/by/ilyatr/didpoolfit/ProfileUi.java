package by.ilyatr.didpoolfit;

import android.content.Intent;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

final class ProfileUi {
    private ProfileUi() {}

    static void insets(AppCompatActivity activity) {
        EdgeToEdge.enable(activity);
        ViewCompat.setOnApplyWindowInsetsListener(activity.findViewById(R.id.main), (view, window) -> {
            Insets bars = window.getInsets(WindowInsetsCompat.Type.systemBars() | WindowInsetsCompat.Type.displayCutout());
            Insets ime = window.getInsets(WindowInsetsCompat.Type.ime());
            view.setPadding(bars.left, bars.top, bars.right, Math.max(bars.bottom, ime.bottom));
            return window;
        });
    }

    static UserStorage.User requireUser(AppCompatActivity activity, UserStorage storage) {
        try {
            UserStorage.User user = storage.getCurrentUser();
            if (user != null) return user;
            activity.startActivity(new Intent(activity, LoginActivity.class)
                    .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK));
        } catch (UserStorage.StorageException e) {
            Toast.makeText(activity, R.string.error_storage, Toast.LENGTH_LONG).show();
        }
        activity.finish();
        return null;
    }
}
