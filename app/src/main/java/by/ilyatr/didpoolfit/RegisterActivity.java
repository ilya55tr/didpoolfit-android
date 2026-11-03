package by.ilyatr.didpoolfit;

import static android.content.Intent.FLAG_ACTIVITY_CLEAR_TOP;

import android.content.Intent;
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

import java.util.HashSet;
import java.util.Set;

public class RegisterActivity extends AppCompatActivity {

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
    UserStorage storage;
    private final Set<TextInputLayout> editedFields = new HashSet<>();

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

        storage = new UserStorage(this);

        validateOnInput(nameLayout);
        validateOnInput(phoneLayout);
        validateOnInput(emailLayout);
        validateOnInput(passwordLayout);
        validateOnInput(repeatPasswordLayout);
        termsCheck.setOnCheckedChangeListener((v, isChecked) -> {
            if (isChecked) termsCheck.setErrorShown(false);
            updateRegistrationSteps();
        });

        updateRegistrationSteps();
        registerButton.setOnClickListener(v -> {
            if (validate()) {
                Intent next = new Intent(this, RegisterSecondActivity.class);
                next.putExtra("fullName", getText(nameLayout));
                next.putExtra("phoneNumber", getText(phoneLayout));
                next.putExtra("email", getText(emailLayout));
                next.putExtra("password", getText(passwordLayout));
                startActivity(next);
            }
        });

        loginTextView.setOnClickListener(v -> {
            Intent loginIntent = new Intent(this, LoginActivity.class);
            loginIntent.addFlags(FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(loginIntent);
        });
        loginTextView.setHighlightColor(Color.TRANSPARENT);
    }

    private boolean validate() {
        boolean valid = true;
        for (TextInputLayout layout : new TextInputLayout[]{
                nameLayout, phoneLayout, emailLayout, passwordLayout, repeatPasswordLayout}) {
            valid &= validateInput(layout, true);
        }

        // Соглашение
        if (!termsCheck.isChecked()) {
            termsCheck.setErrorShown(true);
            termsCheck.setErrorAccessibilityLabel(getString(R.string.error_terms));
            valid = false;
        }

        return valid;
    }

    // Одни правила используются и для открытия следующего шага, и перед сохранением.
    private boolean validateInput(TextInputLayout layout, boolean showError) {
        String text = getText(layout);
        int error = 0;
        if (text.isEmpty()) {
            error = R.string.error_empty;
        } else if (layout == phoneLayout) {
            String digits = text.replaceAll("\\D", "");
            if (digits.length() <= 1) {
                error = R.string.error_empty;
            } else if (digits.length() != PHONE_DIGITS) {
                error = R.string.error_phone;
            } else if (storage.phoneExists(text)) {
                error = R.string.error_phone_exists;
            }
        } else if (layout == emailLayout) {
            if (!Patterns.EMAIL_ADDRESS.matcher(text).matches()) {
                error = R.string.error_email;
            } else if (storage.emailExists(text)) {
                error = R.string.error_email_exists;
            }
        } else if (layout == passwordLayout && !UserStorage.isPasswordValid(text)) {
            error = R.string.error_password;
        } else if (layout == repeatPasswordLayout && !text.equals(getText(passwordLayout))) {
            error = R.string.error_repeat_password;
        }
        if (showError) {
            layout.setError(error == 0 ? null : getString(error));
        }
        return error == 0;
    }

    private void updateRegistrationSteps() {
        boolean previousValid = true;
        for (TextInputLayout layout : new TextInputLayout[]{
                nameLayout, phoneLayout, emailLayout, passwordLayout, repeatPasswordLayout}) {
            // Не вызываем setEnabled повторно: это может мешать фокусу при вводе.
            if (layout.isEnabled() != previousValid) {
                layout.setEnabled(previousValid);
            }
            if (!previousValid) layout.setError(null);
            // Ошибки показываем после начала ввода, только у доступных полей.
            // Повтор пароля также перепроверяется при изменении самого пароля.
            previousValid = previousValid && validateInput(layout, editedFields.contains(layout));
        }
        termsCheck.setEnabled(previousValid);
        registerButton.setEnabled(previousValid && termsCheck.isChecked());
        registerButton.setAlpha(registerButton.isEnabled() ? 1f : 0.5f);
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        updateRegistrationSteps();
    }

    // Пароли не обрезаем: пробел — тоже символ пароля
    private String getText(TextInputLayout layout) {
        String text = String.valueOf(layout.getEditText().getText());
        boolean isPassword = layout == passwordLayout || layout == repeatPasswordLayout;
        return isPassword ? text : text.trim();
    }

    private void validateOnInput(TextInputLayout layout) {
        layout.getEditText().addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}

            @Override
            public void afterTextChanged(Editable s) {
                editedFields.add(layout);
                updateRegistrationSteps();
            }
        });
    }
}
