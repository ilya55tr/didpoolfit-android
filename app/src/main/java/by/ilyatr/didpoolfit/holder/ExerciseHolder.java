package by.ilyatr.didpoolfit.holder;

import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import by.ilyatr.didpoolfit.R;

public class ExerciseHolder extends RecyclerView.ViewHolder {
  private TextView name;
  private TextView amount;
  private ImageView image;
  public ExerciseHolder(@NonNull View itemView) {
    super(itemView);
    name = itemView.findViewById(R.id.exercise_name);
    amount = itemView.findViewById(R.id.exercises_amount);
    image = itemView.findViewById(R.id.exercise_img);
  }

  public TextView getName() {
    return name;
  }

  public void setName(TextView name) {
    this.name = name;
  }

  public TextView getAmount() {
    return amount;
  }

  public void setAmount(TextView amount) {
    this.amount = amount;
  }

  public ImageView getImage() {
    return image;
  }

  public void setImage(ImageView image) {
    this.image = image;
  }
}
