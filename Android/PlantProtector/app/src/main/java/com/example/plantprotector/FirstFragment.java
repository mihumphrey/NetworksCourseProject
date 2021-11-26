package com.example.plantprotector;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.navigation.fragment.NavHostFragment;

import com.example.plantprotector.databinding.FragmentFirstBinding;

public class FirstFragment extends Fragment {

    private FragmentFirstBinding binding;

    // Creates the view when loading this fragment
    @Override
    public View onCreateView(
            LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState
    ) {

        binding = FragmentFirstBinding.inflate(inflater, container, false);
        return binding.getRoot();

    }

    // Once the view is created do some settup
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        binding.buttonFirst.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                NavHostFragment.findNavController(FirstFragment.this)
                        .navigate(R.id.action_FirstFragment_to_SecondFragment);
            }
        });

        // This is the connection button. When it is clicked it will either connect the app to the
        // server or disconnect it. It also changes the text on the button when it is clicked for
        // clarity and changes the status connection text as well
        binding.connectionButton.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                if (((TextView)getView().findViewById(R.id.connection_status)).getText().equals(getString(R.string.not_connected))){
                    ((TextView)getView().findViewById(R.id.connection_status)).setText(R.string.connecting);
                    ((Button)getView().findViewById(R.id.connection_button)).setText(R.string.disconnect);
                    ((MainActivity)getActivity()).startClient();
                } else {
                    ((TextView)getView().findViewById(R.id.connection_status)).setText(R.string.not_connected);
                    ((Button)getView().findViewById(R.id.connection_button)).setText(R.string.connect);
                    ((MainActivity)getActivity()).stopClient();
                }
            }
        });
    }

    // Called when the view is left
    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}