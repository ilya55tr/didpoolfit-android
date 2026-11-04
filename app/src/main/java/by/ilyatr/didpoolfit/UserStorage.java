package by.ilyatr.didpoolfit;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Patterns;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import by.ilyatr.didpoolfit.model.WorkoutHistoryItem;

/** Небольшое локальное хранилище: email -> профиль, пароль и история тренировок. */
public final class UserStorage {
    // Activities submit all storage writes to this single queue.
    static final Executor WRITES = Executors.newSingleThreadExecutor();
    private final SharedPreferences preferences;

    public enum RegistrationResult { SUCCESS, EMAIL_EXISTS, PHONE_EXISTS, INVALID_PROFILE }
    public enum UpdateResult { SUCCESS, INVALID_PROFILE, WRONG_PASSWORD, NO_SESSION }

    public static final class User {
        public final String fullName, phoneNumber, email, gender, photoFileName;
        public final long dateOfBirth;
        public final float weight;
        public final int height;

        private User(JSONObject json) throws JSONException {
            fullName = json.getString("fullName");
            phoneNumber = json.getString("phoneNumber");
            email = json.getString("email");
            gender = json.getString("gender");
            dateOfBirth = json.getLong("dateOfBirth");
            weight = (float) json.getDouble("weight");
            height = json.getInt("height");
            photoFileName = json.optString("photoFileName", "");
        }
    }

    public static final class StorageException extends RuntimeException {
        private StorageException(Throwable cause) { super("Cannot read or save users", cause); }
    }

    public UserStorage(Context context) {
        this(context.getApplicationContext().getSharedPreferences("users", Context.MODE_PRIVATE));
    }

    // Тесты передают отдельный файл preferences, не меняя аккаунты приложения.
    UserStorage(SharedPreferences preferences) { this.preferences = preferences; }

    public static boolean isPasswordValid(String password) {
        return password != null && password.matches(
                "^(?=(?:\\p{Punct}*[0-9]){5})(?=[0-9]*\\p{Punct})[0-9\\p{Punct}]{8}$");
    }

    public boolean hasUsers() {
        return readUsers().length() > 0;
    }

    public boolean emailExists(String email) {
        return readUsers().has(normalizeEmail(email));
    }

    public boolean phoneExists(String phone) {
        JSONObject users = readUsers();
        String digits = phone == null ? "" : phone.replaceAll("\\D", "");
        Iterator<String> emails = users.keys();
        try {
            while (emails.hasNext()) {
                if (digits.equals(users.getJSONObject(emails.next()).getString("phoneNumber"))) return true;
            }
            return false;
        } catch (JSONException e) { throw new StorageException(e); }
    }

    public RegistrationResult register(String name, String phone, String email, String password,
                                       String gender, long birthDate, float weight, int height) {
        email = normalizeEmail(email);
        phone = phone == null ? "" : phone.replaceAll("\\D", "");
        if (!validProfile(name, gender, birthDate, weight, height)
                || !Patterns.EMAIL_ADDRESS.matcher(email).matches()
                || !phone.matches("[0-9]{11}") || !isPasswordValid(password)) return RegistrationResult.INVALID_PROFILE;
        JSONObject users = readUsers();
        if (users.has(email)) return RegistrationResult.EMAIL_EXISTS;
        if (phoneExists(phone)) return RegistrationResult.PHONE_EXISTS;
        try {
            JSONObject user = new JSONObject();
            user.put("email", email);
            user.put("phoneNumber", phone);
            user.put("password", password);
            user.put("fullName", name.trim());
            user.put("gender", gender);
            user.put("dateOfBirth", birthDate);
            user.put("weight", weight);
            user.put("height", height);
            user.put("photoFileName", "");
            user.put("history", new JSONArray());
            users.put(email, user);
            saveUsers(users);
            return RegistrationResult.SUCCESS;
        } catch (JSONException e) { throw new StorageException(e); }
    }

    public boolean login(String email, String password) {
        email = normalizeEmail(email);
        JSONObject user = readUsers().optJSONObject(email);
        if (user == null || password == null || !password.equals(user.optString("password"))) return false;
        if (!preferences.edit().putString("currentEmail", email).commit()) throw new StorageException(new IllegalStateException("Login not saved"));
        return true;
    }

    public User getCurrentUser() {
        JSONObject user = readUsers().optJSONObject(preferences.getString("currentEmail", ""));
        if (user == null) return null;
        try { return new User(user); }
        catch (JSONException e) { throw new StorageException(e); }
    }

    public String logout() {
        String email = preferences.getString("currentEmail", "");
        if (!preferences.edit().remove("currentEmail").commit()) throw new StorageException(new IllegalStateException("Logout not saved"));
        return email;
    }

    public UpdateResult updateProfile(String expectedEmail, String name, String gender, long birthDate,
                                      float weight, int height, String photo, String currentPassword, String newPassword) {
        JSONObject users = readUsers();
        String email = preferences.getString("currentEmail", "");
        JSONObject user = users.optJSONObject(email);
        if (user == null || !email.equals(expectedEmail)) return UpdateResult.NO_SESSION;
        if (!validProfile(name, gender, birthDate, weight, height) || photo == null
                || (!photo.isEmpty() && !photo.matches("avatar_[0-9]+\\.jpg"))
                || newPassword == null || (!newPassword.isEmpty() && !isPasswordValid(newPassword))) return UpdateResult.INVALID_PROFILE;
        if (!newPassword.isEmpty() && !user.optString("password").equals(currentPassword)) return UpdateResult.WRONG_PASSWORD;
        try {
            user.put("fullName", name.trim());
            user.put("gender", gender);
            user.put("dateOfBirth", birthDate);
            user.put("weight", weight);
            user.put("height", height);
            user.put("photoFileName", photo);
            if (!newPassword.isEmpty()) user.put("password", newPassword);
            saveUsers(users);
            return UpdateResult.SUCCESS;
        } catch (JSONException e) { throw new StorageException(e); }
    }

    public boolean addWorkout(String expectedEmail, String title, int exercises, int minutes, long seed, long startedAt) {
        JSONObject users = readUsers();
        String email = preferences.getString("currentEmail", "");
        JSONObject user = users.optJSONObject(email);
        if (user == null || !email.equals(expectedEmail)) return false;
        try {
            JSONObject entry = new JSONObject();
            entry.put("title", title);
            entry.put("exercises", exercises);
            entry.put("minutes", minutes);
            entry.put("seed", seed);
            entry.put("startedAt", startedAt);
            user.getJSONArray("history").put(entry);
            saveUsers(users);
            return true;
        } catch (JSONException e) { throw new StorageException(e); }
    }

    public List<WorkoutHistoryItem> getHistory() {
        List<WorkoutHistoryItem> result = new ArrayList<>();
        JSONObject user = readUsers().optJSONObject(preferences.getString("currentEmail", ""));
        if (user == null) return result;
        try {
            JSONArray history = user.getJSONArray("history");
            for (int i = 0; i < history.length(); i++) {
                JSONObject entry = history.getJSONObject(i);
                result.add(new WorkoutHistoryItem(entry.getString("title"), entry.getInt("exercises"),
                        entry.getInt("minutes"), entry.getLong("seed"), entry.getLong("startedAt")));
            }
            result.sort((a, b) -> Long.compare(b.startedAt, a.startedAt));
            return result;
        } catch (JSONException e) { throw new StorageException(e); }
    }

    private JSONObject readUsers() {
        try { return new JSONObject(preferences.getString("list", "{}")); }
        catch (JSONException | ClassCastException e) { throw new StorageException(e); }
    }

    private void saveUsers(JSONObject users) {
        if (!preferences.edit().putString("list", users.toString()).commit()) throw new StorageException(new IllegalStateException("Users not saved"));
    }

    private String normalizeEmail(String email) { return email == null ? "" : email.trim().toLowerCase(Locale.ROOT); }

    private boolean validProfile(String name, String gender, long birth, float weight, int height) {
        return name != null && !name.trim().isEmpty() && ("Male".equals(gender) || "Female".equals(gender))
                && birth <= System.currentTimeMillis() && !Float.isNaN(weight) && weight >= 20 && weight <= 300
                && height >= 50 && height <= 250;
    }
}
