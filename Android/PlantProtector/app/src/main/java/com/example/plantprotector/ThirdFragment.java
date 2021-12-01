package com.example.plantprotector;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.navigation.fragment.NavHostFragment;

import com.example.plantprotector.databinding.FragmentThirdBinding;

public class ThirdFragment extends Fragment {
    private FragmentThirdBinding binding;

    @Override
    public View onCreateView(
            LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState
    ) {

        binding = FragmentThirdBinding.inflate(inflater, container, false);
        return binding.getRoot();

    }

    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        binding.buttonPrevious.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                NavHostFragment.findNavController(ThirdFragment.this)
                        .navigate(R.id.action_thirdFragment_to_SecondFragment);
            }
        });

        binding.buttonHome.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                NavHostFragment.findNavController(ThirdFragment.this)
                        .navigate(R.id.action_thirdFragment_to_FirstFragment);
            }
        });
    }

    public void updateInfo(MainActivity.PlantInfo plantInfo) {
        if (((MainActivity)getActivity()).isRunning()) {
            if (plantInfo.message != null){
                ((TextView)getView().findViewById(R.id.plant_2_status)).setText(plantInfo.message);
                ((TextView)getView().findViewById(R.id.plant_2_latency)).setText((int) plantInfo.latency);
                ((TextView)getView().findViewById(R.id.plant_2_packet_loss)).setText((int) plantInfo.packetLoss);
            } else {
                ((TextView)getView().findViewById(R.id.plant_2_status)).setText(R.string.updating);
                ((TextView)getView().findViewById(R.id.plant_2_latency)).setText(R.string.updating);
                ((TextView)getView().findViewById(R.id.plant_2_packet_loss)).setText(R.string.updating);
            }

        } else {
            ((TextView)getView().findViewById(R.id.plant_2_status)).setText(R.string.connect_for_info);
            ((TextView)getView().findViewById(R.id.plant_2_latency)).setText(R.string.connect_for_info);
            ((TextView)getView().findViewById(R.id.plant_2_packet_loss)).setText(R.string.connect_for_info);
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        updateInfo(((MainActivity)getActivity()).getPlantInfo(2));
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
