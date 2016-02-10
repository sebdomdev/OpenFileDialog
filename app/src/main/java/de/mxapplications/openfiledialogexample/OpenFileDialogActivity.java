package de.mxapplications.openfiledialogexample;

import android.annotation.TargetApi;
import android.app.AlarmManager;
import android.app.AlertDialog;
import android.app.PendingIntent;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import de.mxapplications.openfiledialog.OpenFileDialog;

public class OpenFileDialogActivity extends AppCompatActivity {

    private static final int PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_open_file_dialog);

        final EditText selectedPathEditText = (EditText)findViewById(R.id.path_value_edit_text);
        final Button selectFileButton = (Button)findViewById(R.id.open_file_dialog_button);
        final Button selectFileOrFolderButton = (Button)findViewById(R.id.open_file_dialog_with_folder_button);

        final OpenFileDialog openFileDialog = new OpenFileDialog(this);

        openFileDialog.setOnCloseListener(new OpenFileDialog.OnCloseListener() {
            @Override
            public void onCancel() {
            }
            @Override
            public void onOk(String selectedFile) {
                selectedPathEditText.setText(selectedFile);
            }
        });

        selectFileButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openFileDialog.setTitle(R.string.openfiledialog_dialog_title);
                openFileDialog.show();

            }
        });
        selectFileOrFolderButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openFileDialog.setTitle(R.string.openfiledialog_dialog_with_folder_title);
                openFileDialog.setFolderSelectable(true);
                openFileDialog.show();
            }
        });

        if(Build.VERSION.SDK_INT>=Build.VERSION_CODES.M) {
            checkForPermission();
        }
    }

    @TargetApi(23)
    private void checkForPermission(){
        if(PackageManager.PERMISSION_GRANTED!= checkSelfPermission(android.Manifest.permission.READ_EXTERNAL_STORAGE)) {
            if (shouldShowRequestPermissionRationale(android.Manifest.permission.READ_EXTERNAL_STORAGE)) {
                requestPermissions(new String[]{android.Manifest.permission.READ_EXTERNAL_STORAGE}, PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE);
                Toast.makeText(getApplicationContext(), R.string.permission_request_read_external_storage, Toast.LENGTH_LONG).show();
            }else {
                requestPermissions(new String[]{android.Manifest.permission.READ_EXTERNAL_STORAGE}, PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE);
            }
        }
    }
    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        switch (requestCode) {
            case PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE: {
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                    Intent intent = getIntent();
                    PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_CANCEL_CURRENT);
                    AlarmManager am = (AlarmManager)getSystemService(Context.ALARM_SERVICE);
                    am.set(AlarmManager.RTC, System.currentTimeMillis() + 100, pendingIntent);
                    System.exit(0);
                } else {
                    new AlertDialog.Builder(this).setMessage(R.string.permission_request_read_external_storage_denied)
                            .setNeutralButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    OpenFileDialogActivity.this.finish();
                                }
                            })
                            .create()
                            .show();
                }
                return;
            }
        }
    }
}
