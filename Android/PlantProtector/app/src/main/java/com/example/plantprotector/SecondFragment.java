package com.example.plantprotector;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.navigation.fragment.NavHostFragment;

import com.example.plantprotector.databinding.FragmentSecondBinding;

public class SecondFragment extends Fragment {

    private FragmentSecondBinding binding;

    @Override
    public View onCreateView(
            LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState
    ) {
        binding = FragmentSecondBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        binding.buttonNext.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                NavHostFragment.findNavController(SecondFragment.this)
                        .navigate(R.id.action_SecondFragment_to_thirdFragment);
            }
        });

        binding.buttonHome.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                NavHostFragment.findNavController(SecondFragment.this)
                        .navigate(R.id.action_SecondFragment_to_FirstFragment);
            }
        });
    }

    public void updateInfo(MainActivity.PlantInfo plantInfo) {
        if (((MainActivity)getActivity()).isRunning()) {
            if (plantInfo.message != null){
                ((TextView)getView().findViewById(R.id.plant_1_status)).setText(plantInfo.message);
                ((TextView)getView().findViewById(R.id.plant_1_latency)).setText((int) plantInfo.latency);
                ((TextView)getView().findViewById(R.id.plant_1_packet_loss)).setText((int) plantInfo.packetLoss);
            } else {
                ((TextView)getView().findViewById(R.id.plant_1_status)).setText(R.string.updating);
                ((TextView)getView().findViewById(R.id.plant_1_latency)).setText(R.string.updating);
                ((TextView)getView().findViewById(R.id.plant_1_packet_loss)).setText(R.string.updating);
            }

        } else {
            ((TextView)getView().findViewById(R.id.plant_1_status)).setText(R.string.connect_for_info);
            ((TextView)getView().findViewById(R.id.plant_1_latency)).setText(R.string.connect_for_info);
            ((TextView)getView().findViewById(R.id.plant_1_packet_loss)).setText(R.string.connect_for_info);
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        updateInfo(((MainActivity)getActivity()).getPlantInfo(1));
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}