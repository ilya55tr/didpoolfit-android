package by.ilyatr.didpoolfit;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ValueAnimator;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.material.progressindicator.LinearProgressIndicator;


public class SplashActivity extends AppCompatActivity {

    ValueAnimator animator;
    SharedPreferences userData;

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
        userData = getSharedPreferences("user", MODE_PRIVATE);
        boolean registered = userData.getBoolean("registered", false);
        boolean loggedIn = userData.getBoolean("loggedIn", false);
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
                if (loggedIn) {
                    startActivity(new Intent(SplashActivity.this, HomeActivity.class));
                } else if (registered) {
                    startActivity(new Intent(SplashActivity.this, LoginActivity.class));
                } else {
                    startActivity(new Intent(SplashActivity.this, WelcomeScreenActivity.class));
                }
                finish();
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
}