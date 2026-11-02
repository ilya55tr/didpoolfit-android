package by.ilyatr.didpoolfit.holder;

import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import by.ilyatr.didpoolfit.R;

public class SetHeaderHolder extends RecyclerView.ViewHolder{
  TextView title;
  public SetHeaderHolder(@NonNull View itemView) {
    super(itemView);
    title = itemView.findViewById(R.id.set_title);
  }

  public TextView getTitle() {
    return title;
  }

  public void setTitle(TextView title) {
    this.title = title;
  }
}
