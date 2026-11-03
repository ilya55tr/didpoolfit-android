package by.ilyatr.didpoolfit;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ValueAnimator;
import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.material.progressindicator.LinearProgressIndicator;



public class SplashActivity extends AppCompatActivity {

    ValueAnimator animator;
    private UserStorage storage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_splash);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        storage = new UserStorage(this);
        LinearProgressIndicator progress = findViewById(R.id.splashProgress);
        animator = ValueAnimator.ofInt(0, 100);
        animator.setDuration(3000);
        TextView percent = findViewById(R.id.splashPercent);
        animator.addUpdateListener(a -> {
            int value = (int) a.getAnimatedValue();
            progress.setProgress(value);
            percent.setText(getString(R.string.splash_percent, value));
        });
        animator.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                navigate();
            }
        });
        animator.start();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // cancel() тоже вызывает onAnimationEnd — сначала снимаем слушатели,
        // иначе при закрытии сплэша (поворот, «назад») откроется следующий экран
        animator.removeAllListeners();
        animator.cancel();
    }

    private void navigate() {
        if (isFinishing() || isDestroyed()) return;
        try {
            Class<?> target = storage.getCurrentUser() != null ? HomeActivity.class
                    : storage.hasUsers() ? LoginActivity.class : WelcomeScreenActivity.class;
            startActivity(new Intent(this, target));
            finish();
        } catch (UserStorage.StorageException e) {
            Toast.makeText(this, R.string.error_storage, Toast.LENGTH_LONG).show();
            ((TextView) findViewById(R.id.splashPercent)).setText(R.string.storage_retry);
            findViewById(R.id.splashProgress).setOnClickListener(v -> recreate());
        }
    }
}
