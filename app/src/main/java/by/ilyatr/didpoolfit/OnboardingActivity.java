package by.ilyatr.didpoolfit;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ImageButton;
import android.widget.ImageView;

import androidx.activity.EdgeToEdge;
import androidx.activity.OnBackPressedCallback;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class OnboardingActivity extends AppCompatActivity {

    int step = 0;
    ImageView onbFrame;
    ImageView onbTextSection;
    ImageButton onbButton;

    int[] frames = new int[]{
            R.drawable.onboarding_frame_1,
            R.drawable.onboarding_frame_2,
            R.drawable.onboarding_frame_3,
            R.drawable.onboarding_frame_4,
    };
    int[] texts = new int[]{
            R.drawable.onboarding_text_section_1,
            R.drawable.onboarding_text_section_2,
            R.drawable.onboarding_text_section_3,
            R.drawable.onboarding_text_section_4,
    };
    int[] buttons = new int[]{
            R.drawable.onboarding_button_1,
            R.drawable.onboarding_button_2,
            R.drawable.onboarding_button_3,
            R.drawable.onboarding_button_4,
    };


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_onboarding);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        onbFrame = findViewById(R.id.onboarding_frame);
        onbButton = findViewById(R.id.onboarding_button);
        onbTextSection = findViewById(R.id.onboarding_text_section);
        showStep(step);

        onbButton.setOnClickListener(v -> {
            if (step == frames.length-1){
                startActivity(new Intent(this, RegisterActivity.class));
                return;
            }
            step++;
            showStep(step);
        });

        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                if (step == 0){
                    finish();
                    return;
                }
                step--;
                showStep(step);
            }
        });
    }

    private void showStep(int step) {
        onbFrame.setImageResource(frames[step]);
        onbTextSection.setImageResource(texts[step]);
        onbButton.setImageResource(buttons[step]);
    }

}