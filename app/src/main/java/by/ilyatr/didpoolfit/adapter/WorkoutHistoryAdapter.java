package by.ilyatr.didpoolfit.adapter;

import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import by.ilyatr.didpoolfit.R;
import by.ilyatr.didpoolfit.WorkoutDetailsActivity;
import by.ilyatr.didpoolfit.model.WorkoutHistoryItem;

public class WorkoutHistoryAdapter extends RecyclerView.Adapter<WorkoutHistoryAdapter.Holder> {
    private final List<WorkoutHistoryItem> items;
    private final SimpleDateFormat dateFormat = new SimpleDateFormat("dd.MM.yyyy • HH:mm:ss", Locale.getDefault());

    public WorkoutHistoryAdapter(List<WorkoutHistoryItem> items) { this.items = items; }
    @Override public int getItemCount() { return items.size(); }

    @Override public Holder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new Holder(LayoutInflater.from(parent.getContext()).inflate(R.layout.history_item, parent, false));
    }

    @Override public void onBindViewHolder(Holder holder, int position) {
        WorkoutHistoryItem item = items.get(position);
        holder.title.setText(item.title);
        holder.info.setText(holder.itemView.getContext().getString(R.string.workout_info, item.exercises, item.minutes));
        holder.time.setText(dateFormat.format(new Date(item.startedAt)));
        int image = item.title.startsWith("Lowerbody") ? R.drawable.workout_lowerbody
                : item.title.startsWith("AB") ? R.drawable.workout_ab : R.drawable.workout_fullbody;
        holder.image.setImageResource(image);
        holder.itemView.setOnClickListener(v -> v.getContext().startActivity(new Intent(v.getContext(), WorkoutDetailsActivity.class)
                .putExtra("title", item.title).putExtra("exercises", item.exercises)
                .putExtra("mins", item.minutes).putExtra("seed", item.seed)));
    }

    static class Holder extends RecyclerView.ViewHolder {
        final TextView title, info, time;
        final ImageView image;
        Holder(View view) {
            super(view);
            title = view.findViewById(R.id.history_title);
            info = view.findViewById(R.id.history_info);
            time = view.findViewById(R.id.history_time);
            image = view.findViewById(R.id.history_image);
        }
    }
}
