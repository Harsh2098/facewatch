package com.hmproductions.facewatch.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.util.Pair;
import androidx.recyclerview.widget.RecyclerView;

import com.hmproductions.facewatch.R;
import com.hmproductions.facewatch.data.Student;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import static com.hmproductions.facewatch.utils.ExtensionsKt.customFormatDate;
import static com.hmproductions.facewatch.utils.ExtensionsKt.removeLeadingZero;

public class HistoryRecyclerAdapter extends RecyclerView.Adapter<HistoryRecyclerAdapter.HistoryViewHolder> {

    private Context context;
    private List<Student> modifiedStudentList;

    public HistoryRecyclerAdapter(Context context) {
        this.context = context;
        this.modifiedStudentList = new ArrayList<>();
    }

    @NonNull
    @Override
    public HistoryViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new HistoryViewHolder(LayoutInflater.from(context).inflate(R.layout.history_list_item, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull HistoryViewHolder holder, int position) {

        Student currentStudent = modifiedStudentList.get(position);

        holder.presentTextView.setText(currentStudent.getRoll_no());
        holder.dateTextView.setText(currentStudent.getDate());
        holder.courseCodeTextView.setText(currentStudent.getCourse_code());
    }

    @Override
    public int getItemCount() {
        if (modifiedStudentList == null) return 0;
        return modifiedStudentList.size();
    }

    public void swapData(List<Student> newList) {
        HashMap<Pair<String, String>, String> lookUp = new HashMap<>();

        for (Student rawStudent : newList) {

            rawStudent.setDate(customFormatDate(rawStudent.getDate()));

            Pair<String, String> currentKey = new Pair<>(rawStudent.getDate(), rawStudent.getCourse_code());
            if (lookUp.containsKey(currentKey)) {
                lookUp.put(currentKey, lookUp.get(currentKey) + ", " +
                        removeLeadingZero(rawStudent.getRoll_no().substring(rawStudent.getRoll_no().length() - 3)));
            } else {
                lookUp.put(currentKey, removeLeadingZero(rawStudent.getRoll_no().substring(rawStudent.getRoll_no().length() - 3)));
            }
        }

        modifiedStudentList.clear();
        for (HashMap.Entry<Pair<String, String>, String> currentRecord : lookUp.entrySet()) {
            if (currentRecord.getKey().first != null && currentRecord.getKey().second != null)
                modifiedStudentList.add(
                        new Student(currentRecord.getValue(), currentRecord.getKey().first, currentRecord.getKey().second)
                );
        }

        notifyDataSetChanged();
    }

    static class HistoryViewHolder extends RecyclerView.ViewHolder {

        TextView dateTextView, courseCodeTextView, presentTextView;

        HistoryViewHolder(@NonNull View itemView) {
            super(itemView);

            dateTextView = itemView.findViewById(R.id.dateTextView);
            courseCodeTextView = itemView.findViewById(R.id.courseCodeTextView);
            presentTextView = itemView.findViewById(R.id.presentRollNoTextView);
        }
    }
}
