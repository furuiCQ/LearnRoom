package com.markfrain.sample.learnroom.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.markfrain.sample.learnroom.entitys.City;

import java.util.ArrayList;
import java.util.List;

public class ListAdapter extends RecyclerView.Adapter<ListAdapter.ListViewHolder> {
    private List<City> dataList = new ArrayList<>();

    public List<City> getDataList() {
        return dataList;
    }

    public void setDataList(List<City> dataList) {
        this.dataList = dataList;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ListViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new ListViewHolder(LayoutInflater.from(parent.getContext())
                .inflate(android.R.layout.simple_list_item_1, null));
    }

    @Override
    public void onBindViewHolder(@NonNull ListViewHolder holder, int position) {
        holder.textView.setText(dataList.get(position).getCity());
    }

    @Override
    public int getItemCount() {
        return dataList.size();
    }

    public static class ListViewHolder extends RecyclerView.ViewHolder {
        public TextView textView;

        public ListViewHolder(@NonNull View itemView) {
            super(itemView);
            textView = itemView.findViewById(android.R.id.text1);
        }

    }
}
