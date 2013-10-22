package org.sistemavotacion.android.ui;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import org.sistemavotacion.android.AppData;
import org.sistemavotacion.android.R;
import org.sistemavotacion.android.UserCertRequestActivity;
import org.sistemavotacion.android.UserCertResponseActivity;

public class CertNotFoundDialog  extends DialogFragment {

	public static final String TAG = "CertNotFoundDialog";
	
	public static final int MAX_MSG_LENGTH = 400;



    public CertNotFoundDialog() {
        // Empty constructor required for DialogFragment
    }

    
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.cert_not_found_dialog, container);
        TextView msgTextView = (TextView) view.findViewById(R.id.msg);
        /*Button cancelVoteButton = (Button) view.findViewById(R.id.cancel_button);
        cancelVoteButton.setOnClickListener(new Button.OnClickListener() {
            public void onClick(View v) {
            	onDestroyView();
            }  
        });*/
        Button openReceiptButton = (Button) view.findViewById(R.id.get_cert_button);
        openReceiptButton.setOnClickListener(new Button.OnClickListener() {
            public void onClick(View v) {
            	Intent intent = null;
          	  	switch(AppData.getInstance(getActivity().getBaseContext()).getEstado()) {
          	  		case CON_CSR:
          	  			intent = new Intent(getActivity(), UserCertResponseActivity.class);
          	  			break;
          	  		case SIN_CSR:
          	  			intent = new Intent(getActivity(), UserCertRequestActivity.class);
          	  			break;
          	  	}
          	  	if(intent != null) startActivity(intent);
            }  
        });
        getDialog().setTitle(getString(R.string.cert_not_found_caption));
        msgTextView.setText(Html.fromHtml(getString(
				R.string.certificado_no_encontrado_msg)));
        return view;
    }
    
}