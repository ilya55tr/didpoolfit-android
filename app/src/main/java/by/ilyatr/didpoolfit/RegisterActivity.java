package by.ilyatr.didpoolfit;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.telephony.PhoneNumberFormattingTextWatcher;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Patterns;
import android.widget.Button;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.material.checkbox.MaterialCheckBox;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

public class RegisterActivity extends AppCompatActivity {

    // Только цифры и спецсимволы (ASCII-пунктуация), ровно 8 символов
    static final String PASSWORD_REGEX = "^[0-9\\p{Punct}]{8}$";
    // +7 и 10 цифр номера
    static final int PHONE_DIGITS = 11;

    TextInputEditText phoneInput;
    TextInputLayout nameLayout;
    TextInputLayout phoneLayout;
    TextInputLayout emailLayout;
    TextInputLayout passwordLayout;
    TextInputLayout repeatPasswordLayout;
    MaterialCheckBox termsCheck;
    Button registerButton;
    TextView loginTextView;
    SharedPreferences userData;
    SharedPreferences.Editor userEdit;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_register);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        phoneInput = findViewById(R.id.phoneInput);
        new Thread(() -> {
            PhoneNumberFormattingTextWatcher watcher = new PhoneNumberFormattingTextWatcher("RU");
            runOnUiThread(() -> phoneInput.addTextChangedListener(watcher));
        }).start();
        phoneInput.setOnFocusChangeListener((v, hasFocus) -> {
            String text = phoneInput.getText().toString();
            if (hasFocus && text.isEmpty()) {
                phoneInput.setText("+7");
                phoneInput.setSelection(phoneInput.getText().length());
            } else if (!hasFocus && text.replaceAll("\\D", "").equals("7")) {
                phoneInput.setText("");
            }
        });

        nameLayout = findViewById(R.id.nameLayout);
        phoneLayout = findViewById(R.id.phoneLayout);
        emailLayout = findViewById(R.id.emailLayout);
        passwordLayout = findViewById(R.id.passwordLayout);
        repeatPasswordLayout = findViewById(R.id.repeatPasswordLayout);
        termsCheck = findViewById(R.id.termsCheck);
        registerButton = findViewById(R.id.registerButton);
        loginTextView = findViewById(R.id.loginTextView);

        clearErrorOnInput(nameLayout);
        clearErrorOnInput(phoneLayout);
        clearErrorOnInput(emailLayout);
        clearErrorOnInput(passwordLayout);
        clearErrorOnInput(repeatPasswordLayout);
        termsCheck.setOnCheckedChangeListener((v, isChecked) -> {
            if (isChecked) termsCheck.setErrorShown(false);
        });

        userData = getSharedPreferences("user", MODE_PRIVATE);
        userEdit = userData.edit();
        registerButton.setOnClickListener(v -> {
            if (validate()) {
                userEdit.putString("fullName",
                        String.valueOf(nameLayout.getEditText().getText()));
                userEdit.putString("phoneNumber",
                        String.valueOf(phoneLayout.getEditText().getText()));
                userEdit.putString("email",
                        String.valueOf(emailLayout.getEditText().getText()));
                userEdit.putString("password",
                        String.valueOf(passwordLayout.getEditText().getText()));
                userEdit.putBoolean("registered", true);
                startActivity(new Intent(this, RegisterSecondActivity.class));
            }
        });

        loginTextView.setOnClickListener(v -> {
            startActivity(new Intent(this, LoginActivity.class));
        });
        loginTextView.setHighlightColor(Color.TRANSPARENT);
    }

    private boolean validate() {
        boolean valid = true;

        // 1. Пустые поля
        valid &= checkNotEmpty(nameLayout);
        valid &= checkNotEmpty(emailLayout);
        valid &= checkNotEmpty(passwordLayout);
        valid &= checkNotEmpty(repeatPasswordLayout);

        // Телефон: "+7" после фокуса — тоже пусто, поэтому считаем цифры
        String phoneDigits = getText(phoneLayout).replaceAll("\\D", "");
        if (phoneDigits.length() <= 1) {
            phoneLayout.setError(getString(R.string.error_empty));
            valid = false;
        } else if (phoneDigits.length() != PHONE_DIGITS) {
            phoneLayout.setError(getString(R.string.error_phone));
            valid = false;
        }

        // 2. Email
        String email = getText(emailLayout);
        if (!email.isEmpty() && !Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            emailLayout.setError(getString(R.string.error_email));
            valid = false;
        }

        // 3. Пароль: только цифры и спецсимволы, 8 символов
        String password = getText(passwordLayout);
        if (!password.isEmpty() && !password.matches(PASSWORD_REGEX)) {
            passwordLayout.setError(getString(R.string.error_password));
            valid = false;
        }

        // 4. Повтор пароля
        String repeatPassword = getText(repeatPasswordLayout);
        if (!repeatPassword.isEmpty() && !repeatPassword.equals(password)) {
            repeatPasswordLayout.setError(getString(R.string.error_repeat_password));
            valid = false;
        }

        // Соглашение
        if (!termsCheck.isChecked()) {
            termsCheck.setErrorShown(true);
            termsCheck.setErrorAccessibilityLabel(getString(R.string.error_terms));
            valid = false;
        }

        return valid;
    }

    private boolean checkNotEmpty(TextInputLayout layout) {
        if (getText(layout).isEmpty()) {
            layout.setError(getString(R.string.error_empty));
            return false;
        }
        return true;
    }

    // Пароли не обрезаем: пробел — тоже символ пароля
    private String getText(TextInputLayout layout) {
        String text = String.valueOf(layout.getEditText().getText());
        boolean isPassword = layout == passwordLayout || layout == repeatPasswordLayout;
        return isPassword ? text : text.trim();
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
}