/*
 * Copyright (c) 2017 Rishi Raj
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package me.itsrishi.exercisecounter.adapters;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import me.itsrishi.exercisecounter.R;
import me.itsrishi.exercisecounter.listeners.ExerciseModificationListener;
import me.itsrishi.exercisecounter.listeners.RecyclerViewClickListener;
import me.itsrishi.exercisecounter.models.Exercise;

/**
 * @author Rishi Raj
 */

public class ExerciseAdapter extends RecyclerView.Adapter<ExerciseAdapter.ExerciseViewHolder>
        implements RecyclerViewClickListener,ListChangeAdapter {

    private ArrayList<Exercise> exercises;
    private List<ExerciseModificationListener> exerciseModificationListeners;
    private RecyclerViewClickListener clickListener;

    public ExerciseAdapter(ArrayList<Exercise> exercises, List<ExerciseModificationListener> exerciseModificationListeners, RecyclerViewClickListener clickListener) {
        this.exercises = exercises;
        this.exerciseModificationListeners = exerciseModificationListeners;
        this.clickListener = clickListener;
    }

    @Override
    public ExerciseViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View exerciseView = LayoutInflater.from(parent.getContext()).
                inflate(R.layout.exercise_layout,parent,false);
        return new ExerciseViewHolder(exerciseView);
    }


    @Override
    public void onBindViewHolder(ExerciseViewHolder holder, int position) {
        holder.name.setText("" + exercises.get(position).getName());
        holder.reps.setText("X "+ exercises.get(position).getTurns());
        holder.position = position;
    }

    @Override
    public int getItemCount() {
        try {
            return exercises.size();
        } catch (NullPointerException ex) {
            return 0;
        }
    }

    @Override
    public void onClick(int position, View view) {
        clickListener.onClick(position,view);
    }

    class ExerciseViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        int position;
        TextView name,reps;
        ExerciseViewHolder(View itemView) {
            super(itemView);
            name = (TextView) itemView.findViewById(R.id.exercise_name);
            reps = (TextView) itemView.findViewById(R.id.exercise_reps);
            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            clickListener.onClick(position,v);
        }
    }
    @Override
    public boolean onItemMove(int fromPosition, int toPosition) {
        if (fromPosition < toPosition) {
            for (int i = fromPosition; i < toPosition; i++) {
                Collections.swap(exercises, i, i + 1);
            }
        } else {
            for (int i = fromPosition; i > toPosition; i--) {
                Collections.swap(exercises, i, i - 1);
            }
        }
        notifyItemMoved(fromPosition, toPosition);
        for (ExerciseModificationListener exerciseModificationListener :
                exerciseModificationListeners) {
            exerciseModificationListener.onChange(false);
        }
        return true;
    }

    @Override
    public boolean onItemDismiss(int position) {
        exercises.remove(position);
        notifyItemRemoved(position);
        for (ExerciseModificationListener exerciseModificationListener :
                exerciseModificationListeners) {
            exerciseModificationListener.onChange(true);
        }
        return true;
    }
}
