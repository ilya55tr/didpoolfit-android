package by.ilyatr.didpoolfit.adapter;


import android.content.Intent;
import android.graphics.LinearGradient;
import android.graphics.Shader;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

import by.ilyatr.didpoolfit.R;
import by.ilyatr.didpoolfit.WorkoutDetailsActivity;
import by.ilyatr.didpoolfit.holder.WorkoutItemViewHolder;
import by.ilyatr.didpoolfit.model.WorkoutItem;

public class WorkoutItemAdapter extends RecyclerView.Adapter<WorkoutItemViewHolder> {

    private final List<WorkoutItem> items;
    public WorkoutItemAdapter(List<WorkoutItem> items) {
        this.items = items;
    }

    @NonNull
    @Override
    public WorkoutItemViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater
                .from(parent.getContext())
                .inflate(R.layout.workout_item, parent, false);
        return new WorkoutItemViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull WorkoutItemViewHolder holder, int position) {
        WorkoutItem workoutItem = items.get(position);
        holder.getTitle().setText(workoutItem.getTitle());
        holder.getTitle().getPaint().setShader(null);
        Button workoutButton = holder.getWorkoutButton();

        workoutButton.post(() -> {
            if (workoutButton.getLayout() == null || workoutButton.getLayout().getLineCount() == 0) {
                return;
            }
            float start = workoutButton.getLayout().getLineLeft(0);
            float end = workoutButton.getLayout().getLineRight(0);
            if (end > start) {
                LinearGradient gradient = new LinearGradient(
                        start, 0, end, 0,
                        workoutButton.getContext().getColor(R.color.gradient_start_welcome_btn),
                        workoutButton.getContext().getColor(R.color.gradient_end_welcome_btn),
                        Shader.TileMode.CLAMP
                );
                workoutButton.getPaint().setShader(gradient);
                workoutButton.invalidate();
            }
        });

        Intent workoutDetailsIntent = new Intent(
                workoutButton.getContext(),
                WorkoutDetailsActivity.class
        );
        workoutDetailsIntent.putExtra("title", workoutItem.getTitle());
        workoutDetailsIntent.putExtra("exercises", workoutItem.getExerciseAmount().intValue());
        workoutDetailsIntent.putExtra("mins", workoutItem.getTime().intValue());
        workoutDetailsIntent.putExtra("calories", workoutItem.getCalories().intValue());
        workoutDetailsIntent.putExtra("seed", workoutItem.getSeed());

        workoutButton.setOnClickListener(v ->
                workoutButton.getContext().startActivity(workoutDetailsIntent));

        holder.getInfo().setText(holder.itemView.getContext().getString(R.string.workout_info,
                workoutItem.getExerciseAmount(),
                workoutItem.getTime()));
        holder.getWorkoutImage().setImageResource(workoutItem.getImage());
    }

    @Override
    public int getItemCount() {
        return items.size();
    }
}
