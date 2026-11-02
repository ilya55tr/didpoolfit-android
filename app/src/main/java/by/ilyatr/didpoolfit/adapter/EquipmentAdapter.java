package by.ilyatr.didpoolfit.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

import by.ilyatr.didpoolfit.R;
import by.ilyatr.didpoolfit.holder.EquipmentViewHolder;
import by.ilyatr.didpoolfit.model.Equipment;

public class EquipmentAdapter extends RecyclerView.Adapter<EquipmentViewHolder> {

  private final List<Equipment> equipments;

  public EquipmentAdapter(List<Equipment> equipments) {
    this.equipments = equipments;
  }

  @NonNull
  @Override
  public EquipmentViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
    View view = LayoutInflater.from(parent.getContext())
            .inflate(R.layout.equipment_item, parent, false);
    return new EquipmentViewHolder(view);

  }

  @Override
  public void onBindViewHolder(@NonNull EquipmentViewHolder holder, int position) {
    Equipment equipment = equipments.get(position);
    holder.getImage().setImageResource(equipment.getImage());
    holder.getName().setText(equipment.getName());
  }

  @Override
  public int getItemCount() {
    return equipments.size();
  }
}
