package org.codebase.reminderalarm;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

public class EventsAdapter extends RecyclerView.Adapter<EventsAdapter.EventsViewHolder> {

    private ArrayList<EventsModel> arrayList;
    private Context context;
    private EventsModel eventsModel;

    public EventsAdapter(ArrayList<EventsModel> arrayList, Context context) {
        this.arrayList = arrayList;
        this.context = context;
    }

    @NonNull
    @Override
    public EventsViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        View listItem = inflater.inflate(R.layout.events_detail_list, parent, false);
        EventsViewHolder viewHolder = new EventsViewHolder(listItem);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull EventsViewHolder holder, int position) {

        eventsModel = arrayList.get(position);

        holder.title.setText(eventsModel.getTitle());
        holder.text.setText(eventsModel.getText());
        holder.eventDate.setText(eventsModel.getDate());
    }

    @Override
    public int getItemCount() {
        return arrayList.size();
    }

    public static class EventsViewHolder extends RecyclerView.ViewHolder {

        public TextView title, text, eventDate;
        public EventsViewHolder(@NonNull View itemView) {
            super(itemView);

            title = itemView.findViewById(R.id.title);
            text = itemView.findViewById(R.id.text);
            eventDate = itemView.findViewById(R.id.date);
        }
    }
}
