package by.ilyatr.didpoolfit.holder;

import android.graphics.LinearGradient;
import android.graphics.Shader;
import android.widget.TextView;
import android.view.View;
import android.widget.ImageView;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import by.ilyatr.didpoolfit.R;

public class WorkoutItemViewHolder extends RecyclerView.ViewHolder {
    private TextView title;
    private TextView info;
    private ImageView workoutImage;
    private Button workoutButton;
    public WorkoutItemViewHolder(@NonNull View itemView) {
        super(itemView);
        title = itemView.findViewById(R.id.workout_title);
        info = itemView.findViewById(R.id.workout_info);
        workoutImage = itemView.findViewById(R.id.workout_image);
        workoutButton = itemView.findViewById(R.id.workout_button);
    }

    public TextView getTitle() {
        return title;
    }

    public Button getWorkoutButton() {
        return workoutButton;
    }

    public void setTitle(TextView title) {
        this.title = title;
    }

    public TextView getInfo() {
        return info;
    }

    public void setInfo(TextView info) {
        this.info = info;
    }

    public ImageView getWorkoutImage() {
        return workoutImage;
    }

    public void setWorkoutImage(ImageView workoutImage) {
        this.workoutImage = workoutImage;
    }
}
