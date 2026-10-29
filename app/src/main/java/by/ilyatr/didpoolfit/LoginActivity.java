package by.ilyatr.didpoolfit;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

public class LoginActivity extends AppCompatActivity {


  TextView register;
  TextView forgetLogin;
  Button login;
  TextInputLayout emailLayout;
  TextInputEditText emailInput;

  TextInputLayout passwordLayout;
  TextInputEditText passwordInput;



  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    EdgeToEdge.enable(this);
    setContentView(R.layout.activity_login);
    ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
      Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
      v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
      return insets;
    });

    emailLayout = findViewById(R.id.emailLayout);
    passwordLayout = findViewById(R.id.passwordLayout);
    emailInput = findViewById(R.id.emailInput);
    passwordInput = findViewById(R.id.passwordInput);

    forgetLogin = findViewById(R.id.forgetPassword);

    register.setOnClickListener(v ->{
      startActivity(new Intent(this, RegisterActivity.class));
    });
    forgetLogin.setOnClickListener(v -> {
      Toast.makeText(this, "Coming soon", Toast.LENGTH_SHORT).show();
    });

  }


  private void clearErrorOnInput(TextInputLayout layout) {
    layout.getEditText().addTextChangedListener(new TextWatcher() {
      @Override
      public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

      @Override
      public void onTextChanged(CharSequence s, int start, int before, int count) {}

      @Override
      public void afterTextChanged(Editable s) {
        layout.setError(null);
      }
    });
  }

  private String getText(TextInputLayout layout) {
    String text = String.valueOf(layout.getEditText().getText());
    return text.trim();
  }

  private boolean checkNotEmpty(TextInputLayout layout) {
    if (getText(layout).isEmpty()) {
      layout.setError(getString(R.string.error_empty));
      return false;
    }
    return true;
  }
}