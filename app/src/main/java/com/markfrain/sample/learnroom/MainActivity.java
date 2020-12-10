package com.markfrain.sample.learnroom;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.markfrain.sample.learnroom.adapter.ListAdapter;
import com.markfrain.sample.learnroom.databinding.ActivityMainBinding;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {
    MainViewModel viewModel;
    ActivityMainBinding binding;

    ListAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_main);

        viewModel = new ViewModelProvider(this,
                new ViewModelProvider.AndroidViewModelFactory(getApplication()))
                .get(MainViewModel.class);

        viewModel.getListLiveData().observe(this, data -> {
            if (data != null && data.data != null) {
                getSystemUserSuccess(data.data);
            } else {
                getSystemUserSuccess(new ArrayList<>());

            }
        });
        adapter = new ListAdapter();
        binding.recyclerView.setLayoutManager(new LinearLayoutManager(this));
        binding.recyclerView.setAdapter(adapter);
    }

    public void getSystemUserSuccess(List<SystemUser> systemUsers) {
        adapter.setDataList(systemUsers);
    }
}