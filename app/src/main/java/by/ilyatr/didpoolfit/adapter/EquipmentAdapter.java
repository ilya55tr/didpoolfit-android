package by.ilyatr.didpoolfit.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.function.Consumer;

import by.ilyatr.didpoolfit.R;
import by.ilyatr.didpoolfit.holder.EquipmentViewHolder;
import by.ilyatr.didpoolfit.model.Equipment;
import by.ilyatr.didpoolfit.model.Equipment.EquipmentType;

public class EquipmentAdapter extends RecyclerView.Adapter<EquipmentViewHolder> {

  private final List<Equipment> equipments;

  private final Set<EquipmentType> selected;
  private final Consumer<EquipmentType> onToggle;

  public EquipmentAdapter(List<Equipment> equipments, Set<EquipmentType> selected,
                          Consumer<EquipmentType> onToggle) {
    this.equipments = equipments;
    this.selected = selected;
    this.onToggle = onToggle;
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
    holder.itemView.setSelected(selected.contains(equipment.getEquipmentType()));
    holder.itemView.setOnClickListener(v -> {
      onToggle.accept(equipment.getEquipmentType());
      holder.itemView.setSelected(selected.contains(equipment.getEquipmentType()));
    });
  }

  @Override
  public int getItemCount() {
    return equipments.size();
  }
}
