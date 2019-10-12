package com.nimil.uptodate;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatDialogFragment;

public class CustomDeleteDialog extends AppCompatDialogFragment{

    TextView mTextDialogMessage;
    DeleteDialogListener listener;
    String mDialogMessage;
    CustomDeleteDialog(String dialogMessage){
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
                .setTitle("Delete Confirmation")
                .setNegativeButton("CANCEL", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        listener.applyDeleteReply(false);
                    }
                })
                .setPositiveButton("DELETE", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        listener.applyDeleteReply(true);
                    }
                });

        /*AlertDialog alert=builder.create();
        alert.show();
        Button nbutton = alert.getButton(DialogInterface.BUTTON_NEGATIVE);
        nbutton.setBackgroundColor(getResources().getColor(R.color.colorPrimaryDark));
        Button pbutton = alert.getButton(DialogInterface.BUTTON_POSITIVE);
        pbutton.setBackgroundColor(getResources().getColor(R.color.colorPrimaryDark));*/

        return builder.create();
    }

    @Override
    public void onAttach(@NonNull Context context) {
        try {
            listener=(DeleteDialogListener) context;
        } catch (ClassCastException e) {
            throw new ClassCastException(context.toString()+ " must implement DeleteDialogListener interface.");
        }
        super.onAttach(context);
    }

    interface DeleteDialogListener{
        void applyDeleteReply(boolean isConfirmed);
    }
}
