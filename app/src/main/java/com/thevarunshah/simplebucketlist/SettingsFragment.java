package com.thevarunshah.simplebucketlist;

import android.app.backup.RestoreObserver;
import android.content.ActivityNotFoundException;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import com.google.android.material.snackbar.Snackbar;
import androidx.appcompat.app.AlertDialog;

import android.preference.SwitchPreference;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

import com.thevarunshah.simplebucketlist.internal.Utility;

public class SettingsFragment extends PreferenceFragment {

    private View overlay = null;
    private View progressBar = null;

    private final static String TAG = "SettingsFragment";

    @Override
    public void onCreate(final Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.settings);

        overlay = getActivity().findViewById(R.id.overlay);
        progressBar = getActivity().findViewById(R.id.progress_bar);

        SwitchPreference addToTop = (SwitchPreference) findPreference("add_to_top");
        addToTop.setChecked(Utility.getAddToTopPreference(getActivity().getApplicationContext())); //set default
        addToTop.setOnPreferenceChangeListener((preference, newValue) -> {
            Utility.updateAddToTopPreference(getActivity().getApplicationContext(), (boolean) newValue);
            return true;
        });

        Preference restore = findPreference("restore");
        restore.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                //inflate layout with customized alert dialog view
                LayoutInflater layoutInflater = LayoutInflater.from(getActivity());
                final View dialog = layoutInflater.inflate(R.layout.info_dialog, null, false);
                final AlertDialog.Builder infoDialogBuilder = new AlertDialog.Builder(getActivity());

                //customize alert dialog and set its view
                infoDialogBuilder.setTitle("Restore Data");
                infoDialogBuilder.setIcon(R.drawable.ic_warning_black_24px);
                infoDialogBuilder.setView(dialog);

                //fetch textview and set its text
                final TextView message = dialog.findViewById(R.id.info_dialog);
                message.setText(R.string.restore_message);

                //set up actions for dialog buttons
                infoDialogBuilder.setPositiveButton("RESTORE", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int whichButton) {
                        overlay.setVisibility(View.VISIBLE);
                        progressBar.setVisibility(View.VISIBLE);
                        Utility.restoreData(getActivity().getApplicationContext(), new RestoreObserver() {
                            @Override
                            public void restoreFinished(int error) {
                                super.restoreFinished(error);
                                if (error == 0) {
                                    Utility.readData(getActivity().getApplicationContext());
                                }
                                Snackbar snackbar = Snackbar.make(getActivity().findViewById(R.id.relativeLayout), error == 0 ? R.string.restore_success_message : R.string.restore_failed_message,
                                        error == 0 ? Snackbar.LENGTH_SHORT : Snackbar.LENGTH_LONG);
                                overlay.setVisibility(View.GONE);
                                progressBar.setVisibility(View.GONE);
                                snackbar.show();
                            }
                        });
                    }
                });
                infoDialogBuilder.setNegativeButton("CANCEL", null);

                //create and show the dialog
                AlertDialog infoDialog = infoDialogBuilder.create();
                infoDialog.show();
                return true;
            }
        });

        Preference about = findPreference("about");
        //display app information and prompt them to rate it.
        about.setOnPreferenceClickListener(preference -> {
            //inflate layout with customized alert dialog view
            LayoutInflater layoutInflater = LayoutInflater.from(getActivity());
            final View dialog = layoutInflater.inflate(R.layout.info_dialog, null, false);
            final AlertDialog.Builder infoDialogBuilder = new AlertDialog.Builder(getActivity());

            //customize alert dialog and set its view
            infoDialogBuilder.setTitle("About");
            infoDialogBuilder.setIcon(R.drawable.ic_info_black_24px);
            infoDialogBuilder.setView(dialog);

            //fetch textview and set its text
            final TextView message = dialog.findViewById(R.id.info_dialog);
            message.setText(R.string.about_message);

            //set up actions for dialog buttons
            infoDialogBuilder.setPositiveButton("RATE APP", (dialogInterface, whichButton) -> {

                String appPackageName = getActivity().getApplicationContext().getPackageName();
                Intent i = new Intent(Intent.ACTION_VIEW);
                try{
                    i.setData(Uri.parse("market://details?id=" + appPackageName));
                    startActivity(i);
                } catch(ActivityNotFoundException e){
                    try{
                        i.setData(Uri.parse("https://play.google.com/store/apps/details?id=" + appPackageName));
                        startActivity(i);
                    } catch (ActivityNotFoundException e2){
                        Snackbar errorBar = Snackbar.make(getActivity().findViewById(R.id.relativeLayout), R.string.play_launch_failed, Snackbar.LENGTH_SHORT);
                        errorBar.show();
                    }
                }
            });
            infoDialogBuilder.setNegativeButton("DISMISS", null);

            //create and show the dialog
            AlertDialog infoDialog = infoDialogBuilder.create();
            infoDialog.show();
            return true;
        });
    }
}
