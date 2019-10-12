package com.nimil.uptodate;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatDialogFragment;

public class CustomMessageBox extends AppCompatDialogFragment {
    TextView mTextDialogMessage;
    String mDialogMessage;
    CustomMessageBox(String dialogMessage){
        mDialogMessage=dialogMessage;
    }
    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        AlertDialog.Builder builder=new AlertDialog.Builder(getActivity(),R.style.CustomDialogTheme);
        LayoutInflater inflater=getActivity().getLayoutInflater();
        View view=inflater.inflate(R.layout.delete_dialog_box,null);
        mTextDialogMessage=view.findViewById(R.id.text_dialog_msg);
        mTextDialogMessage.setText(mDialogMessage);
        builder.setView(view)
                .setTitle("Message")
                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {

                    }
                });


        return builder.create();
    }
}
