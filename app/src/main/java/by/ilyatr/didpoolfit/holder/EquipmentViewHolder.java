package by.ilyatr.didpoolfit.holder;

import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import by.ilyatr.didpoolfit.R;

public class EquipmentViewHolder extends RecyclerView.ViewHolder {

  private ImageView image;
  private TextView name;
  public EquipmentViewHolder(@NonNull View itemView) {
    super(itemView);
    image = itemView.findViewById(R.id.equip_img);
    name = itemView.findViewById(R.id.equip_name);
  }

  public ImageView getImage() {
    return image;
  }

  public void setImage(ImageView image) {
    this.image = image;
  }

  public TextView getName() {
    return name;
  }

  public void setName(TextView name) {
    this.name = name;
  }
}
