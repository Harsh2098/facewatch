package com.hmproductions.facewatch.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.hmproductions.facewatch.R;
import com.hmproductions.facewatch.data.Person;

import java.util.List;

public class PersonRecyclerAdapter extends RecyclerView.Adapter<PersonRecyclerAdapter.PersonViewHolder> {

    private Context context;
    private List<Person> personList;
    private PersonClickListener listener;

    public interface PersonClickListener {
        void onPersonClicked(Person person, int position);
    }

    public PersonRecyclerAdapter(Context context, List<Person> personList, PersonClickListener newsItemClickListener) {
        this.personList = personList;
        this.context = context;
        this.listener = newsItemClickListener;
    }

    @NonNull
    @Override
    public PersonViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(context).inflate(R.layout.person_list_item, parent, false);
        return new PersonViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull PersonViewHolder holder, int position) {

        Person currentPerson = personList.get(position);
        holder.nameTextView.setText(currentPerson.getName());
        holder.rollNumberTextView.setText(currentPerson.getRollNumber());
    }

    @Override
    public int getItemCount() {
        if (personList == null || personList.size() == 0) return 0;
        return personList.size();
    }

    public void swapData(List<Person> list) {
        personList = list;
        notifyDataSetChanged();
    }

    public void updateStudentDetails(String name, String rollNo, int position) {
        personList.get(position).setName(name);
        personList.get(position).setRollNumber(rollNo);
        notifyItemChanged(position);
    }

    public List<Person> getPersonList() {
        return personList;
    }

    class PersonViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        TextView nameTextView, rollNumberTextView;

        PersonViewHolder(View view) {
            super(view);
            nameTextView = view.findViewById(R.id.personNameTextView);
            rollNumberTextView = view.findViewById(R.id.rollNumberTextView);

            view.setOnClickListener(this);
        }

        @Override
        public void onClick(View view) {
            listener.onPersonClicked(personList.get(getAdapterPosition()), getAdapterPosition());
        }
    }
}