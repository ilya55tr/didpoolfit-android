package by.ilyatr.didpoolfit.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

import by.ilyatr.didpoolfit.R;
import by.ilyatr.didpoolfit.holder.ExerciseHolder;
import by.ilyatr.didpoolfit.holder.SetHeaderHolder;
import by.ilyatr.didpoolfit.model.ExerciseRow;

public class ExerciseRowAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

  List<ExerciseRow> exercises;

  public ExerciseRowAdapter(List<ExerciseRow> exercises) {
    this.exercises = exercises;
  }

  @NonNull
  @Override
  public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
    if (viewType == ExerciseRow.HEADER){
      View view = LayoutInflater.from(parent.getContext())
              .inflate(R.layout.exercise_set, parent, false);
      return new SetHeaderHolder(view);
    }

    View view = LayoutInflater.from(parent.getContext())
            .inflate(R.layout.exercise_item, parent, false);
    return new ExerciseHolder(view);
  }

  @Override
  public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
    ExerciseRow row = exercises.get(position);
    if (holder instanceof ExerciseHolder){
      SetHeaderHolder setHeaderHolder = (SetHeaderHolder) holder;
      setHeaderHolder.getTitle().setText(row.getTitle());
    }else {
      ExerciseHolder exerciseHolder = (ExerciseHolder) holder;
      exerciseHolder.getName().setText(row.getExercise().getName());
      exerciseHolder.getAmount().setText(row.getExercise().getAmount());
      exerciseHolder.getImage().setImageResource(row.getExercise().getImage());
    }
  }

  @Override
  public int getItemCount() {
    return exercises.size();
  }

  @Override
  public int getItemViewType(int position) {
    return exercises.get(position).getType();
  }
}
