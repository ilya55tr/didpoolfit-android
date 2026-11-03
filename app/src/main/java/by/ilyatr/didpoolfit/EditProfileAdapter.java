package by.ilyatr.didpoolfit;

import android.os.Bundle;
import android.text.Editable;
import android.text.InputType;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.textfield.MaterialAutoCompleteTextView;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import java.util.ArrayList;
import java.util.List;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

/** Значения принадлежат Draft, а не переиспользуемым ViewHolder. */
final class EditProfileAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    static final int NAME = 0, GENDER = 1, BIRTH = 2, HEIGHT = 3, WEIGHT = 4, CURRENT = 5, NEW = 6, REPEAT = 7;
    static final class Draft {
        final String[] values = new String[8];
        final boolean[] touched = new boolean[8];
        long birthDate;
        boolean submitted;
        int passwordError;

        Draft(UserStorage.User user, Bundle state) {
            values[NAME] = user.fullName;
            values[GENDER] = user.gender;
            values[HEIGHT] = String.valueOf(user.height);
            values[WEIGHT] = String.valueOf(user.weight);
            values[CURRENT] = values[NEW] = values[REPEAT] = "";
            birthDate = state == null ? user.dateOfBirth : state.getLong("birthDate", user.dateOfBirth);
            if (state != null) {
                for (int i = NAME; i <= WEIGHT; i++) values[i] = state.getString("field_" + i, values[i]);
            }
            setBirthDate(birthDate);
        }

        void save(Bundle state) {
            // Пароли намеренно не записываем в Bundle; при пересоздании их вводят заново.
            for (int i = NAME; i <= WEIGHT; i++) state.putString("field_" + i, values[i]);
            state.putLong("birthDate", birthDate);
        }

        void setBirthDate(long value) {
            birthDate = value;
            SimpleDateFormat format = new SimpleDateFormat("dd.MM.yyyy", Locale.getDefault());
            format.setTimeZone(TimeZone.getTimeZone("UTC"));
            values[BIRTH] = format.format(new Date(value));
        }

        float weight() {
            try { return Float.parseFloat(values[WEIGHT].trim().replace(',', '.')); }
            catch (NumberFormatException e) { return Float.NaN; }
        }

        int height() {
            try { return Integer.parseInt(values[HEIGHT].trim()); }
            catch (NumberFormatException e) { return 0; }
        }

        int error(int field) {
            String value = values[field];
            if (field == NAME) return value.trim().isEmpty() ? R.string.error_empty : 0;
            if (field == GENDER) return "Male".equals(value) || "Female".equals(value) ? 0 : R.string.error_empty;
            if (field == BIRTH) return birthDate > System.currentTimeMillis() ? R.string.error_birth_date : 0;
            if (field == HEIGHT) return height() < 50 || height() > 250 ? R.string.error_height : 0;
            if (field == WEIGHT) return Float.isNaN(weight()) || weight() < 20 || weight() > 300 ? R.string.error_weight : 0;
            if (field == CURRENT) return passwordError != 0 ? passwordError
                    : !values[NEW].isEmpty() && value.isEmpty() ? R.string.error_empty : 0;
            if (field == NEW) return !value.isEmpty() && !UserStorage.isPasswordValid(value) ? R.string.error_password : 0;
            if (field == REPEAT) return !value.equals(values[NEW]) ? R.string.error_repeat_password : 0;
            return 0;
        }

        int firstError() {
            for (int i = 0; i < values.length; i++) if (error(i) != 0) return i;
            return -1;
        }
    }

    private final EditProfileActivity activity;
    final Draft draft;
    private final List<FieldHolder> holders = new ArrayList<>();

    EditProfileAdapter(EditProfileActivity activity, Draft draft) {
        this.activity = activity;
        this.draft = draft;
        setHasStableIds(true);
    }

    @Override public int getItemCount() { return 9; }
    @Override public long getItemId(int position) { return position; }
    @Override public int getItemViewType(int position) { return position == 0 ? 0 : position == GENDER + 1 ? 2 : position == BIRTH + 1 ? 3 : 1; }

    @Override public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int type) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        if (type == 0) return new RecyclerView.ViewHolder(inflater.inflate(R.layout.item_edit_photo, parent, false)) {};
        if (type == 2) return new RecyclerView.ViewHolder(inflater.inflate(R.layout.item_edit_gender, parent, false)) {};
        FieldHolder holder = new FieldHolder(inflater.inflate(R.layout.item_edit_field, parent, false));
        holders.add(holder);
        return holder;
    }

    @Override public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        if (position == 0) {
            ProfilePhotos.show(activity, holder.itemView.findViewById(R.id.edit_avatar), activity.photoName, draft.values[GENDER]);
            holder.itemView.findViewById(R.id.change_photo).setOnClickListener(v -> activity.choosePhoto());
        } else if (position == GENDER + 1) {
            MaterialAutoCompleteTextView input = holder.itemView.findViewById(R.id.edit_gender);
            input.setText(draft.values[GENDER], false);
            input.setOnItemClickListener((parent, view, index, id) -> {
                draft.values[GENDER] = input.getText().toString();
                draft.touched[GENDER] = true;
                notifyItemChanged(0);
            });
        } else ((FieldHolder) holder).bind(position - 1);
    }

    void refreshErrors() {
        for (FieldHolder holder : holders) holder.error();
    }

    final class FieldHolder extends RecyclerView.ViewHolder {
        final TextInputLayout layout;
        final TextInputEditText input;
        TextWatcher watcher;
        int field = -1;

        FieldHolder(View view) {
            super(view);
            layout = (TextInputLayout) view;
            input = view.findViewById(R.id.edit_field_input);
        }

        void bind(int field) {
            if (watcher != null) input.removeTextChangedListener(watcher);
            this.field = field;
            int[] labels = {R.string.field_name, R.string.field_gender, R.string.field_birth_date, R.string.field_height,
                    R.string.field_weight, R.string.current_password, R.string.new_password, R.string.repeat_password};
            input.setHint(labels[field]);
            boolean password = field >= CURRENT;
            input.setInputType(password ? InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD
                    : field == HEIGHT ? InputType.TYPE_CLASS_NUMBER
                    : field == WEIGHT ? InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_DECIMAL
                    : InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_CAP_WORDS);
            input.setTypeface(androidx.core.content.res.ResourcesCompat.getFont(activity, R.font.poppins_regular));
            input.setFocusable(field != BIRTH);
            input.setFocusableInTouchMode(field != BIRTH);
            input.setOnClickListener(field == BIRTH ? v -> activity.chooseBirthDate() : null);
            layout.setEndIconMode(password ? TextInputLayout.END_ICON_PASSWORD_TOGGLE : TextInputLayout.END_ICON_NONE);
            layout.setHelperText(activity.getString(field == NEW ? R.string.password_change_hint : labels[field]));
            input.setText(draft.values[field]);
            error();
            watcher = new TextWatcher() {
                @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
                @Override public void onTextChanged(CharSequence s, int start, int before, int count) {}
                @Override public void afterTextChanged(Editable value) {
                    draft.values[field] = value.toString();
                    draft.touched[field] = true;
                    if (field == CURRENT) draft.passwordError = 0;
                    refreshErrors();
                }
            };
            input.addTextChangedListener(watcher);
        }

        void error() {
            if (field < 0) return;
            int error = draft.error(field);
            layout.setError(error == 0 || (!draft.submitted && !draft.touched[field]) ? null : activity.getString(error));
        }
    }
}
