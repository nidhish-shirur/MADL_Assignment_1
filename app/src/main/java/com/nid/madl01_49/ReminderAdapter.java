package com.nid.madl01_49;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;

public class ReminderAdapter extends RecyclerView.Adapter<ReminderAdapter.ViewHolder> {

    private List<Reminder> reminderList;
    private Context context;
    private DatabaseHelper databaseHelper;

    public ReminderAdapter(Context context, List<Reminder> reminderList) {
        this.context = context;
        this.reminderList = reminderList;
        this.databaseHelper = new DatabaseHelper(context); // Setup database access
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_reminder, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Reminder reminder = reminderList.get(position);

        holder.tvTitle.setText(reminder.title);
        holder.tvDesc.setText(reminder.description);
        holder.tvTime.setText(reminder.time);
        holder.tvLocation.setText("Location: " + reminder.location);
        holder.tvCategory.setText("Category: " + reminder.category);

        // Handle Delete Button Click
        holder.btnDelete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // 1. Delete from database
                databaseHelper.deleteReminder(reminder.id);

                // 2. Remove from the visual list
                reminderList.remove(position);

                // 3. Tell the adapter the data changed
                notifyItemRemoved(position);
                notifyItemRangeChanged(position, reminderList.size());

                Toast.makeText(context, "Reminder Deleted", Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public int getItemCount() {
        return reminderList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvTitle, tvDesc, tvTime, tvLocation, tvCategory;
        Button btnDelete; // New button

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvTitle = itemView.findViewById(R.id.tvItemTitle);
            tvDesc = itemView.findViewById(R.id.tvItemDesc);
            tvTime = itemView.findViewById(R.id.tvItemTime);
            tvLocation = itemView.findViewById(R.id.tvItemLocation);
            tvCategory = itemView.findViewById(R.id.tvItemCategory);
            btnDelete = itemView.findViewById(R.id.btnDelete); // Link to XML
        }
    }
}