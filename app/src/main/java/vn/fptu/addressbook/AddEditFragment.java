package vn.fptu.addressbook;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;

import androidx.fragment.app.Fragment;

public class AddEditFragment extends Fragment {
    public interface AddEditFragmentListener {
        // called after edit completed so contact can be redisplayed
        void onAddEditCompleted(long rowID);
    }

    private AddEditFragmentListener listener;
    private long rowID; // database row ID of the contact
    private Bundle contactInfoBundle; // arguments for editing a contact

    private EditText nameEditText;
    private EditText phoneEditText;
    private EditText emailEditText;
    private EditText streetEditText;
    private EditText cityEditText;
    private EditText stateEditText;
    private EditText zipEditText;

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        listener = (AddEditFragmentListener) activity;
    }

    // remove AddEditFragmentListener when Fragment detached
    @Override
    public void onDetach() {
        super.onDetach();
        listener = null;
    }

    // called when Fragment's view needs to be created
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        setRetainInstance(true); // save fragment across config changes
        setHasOptionsMenu(true); // fragment has menu items to display

        // inflate GUI and get references to EditTexts
        View view =
                inflater.inflate(R.layout.fragment_add_edit, container, false);
        nameEditText = view.findViewById(R.id.nameEditText);
        phoneEditText = view.findViewById(R.id.phoneEditText);
        emailEditText = view.findViewById(R.id.emailEditText);
        streetEditText = view.findViewById(R.id.streetEditText);
        cityEditText = view.findViewById(R.id.cityEditText);
        stateEditText = view.findViewById(R.id.stateEditText);
        zipEditText = view.findViewById(R.id.zipEditText);

        contactInfoBundle = getArguments(); // null if creating new contact

        if (contactInfoBundle != null) {
            rowID = contactInfoBundle.getLong(MainActivity.ROW_ID);
            nameEditText.setText(contactInfoBundle.getString("name"));
            phoneEditText.setText(contactInfoBundle.getString("phone"));
            emailEditText.setText(contactInfoBundle.getString("email"));
            streetEditText.setText(contactInfoBundle.getString("street"));
            cityEditText.setText(contactInfoBundle.getString("city"));
            stateEditText.setText(contactInfoBundle.getString("state"));
            zipEditText.setText(contactInfoBundle.getString("zip"));
        }

        // set Save Contact Button's event listener
        Button saveContactButton =
                (Button) view.findViewById(R.id.saveContactButton);
        saveContactButton.setOnClickListener(saveContactButtonClicked);
        return view;
    }

    View.OnClickListener saveContactButtonClicked = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            if (nameEditText.getText().toString().trim().length() != 0) {
                // AsyncTask to save contact, then notify listener
                @SuppressLint("StaticFieldLeak")
                class SaveContactTask extends AsyncTask<Long, Object, Object> {
                    @Override
                    protected Object doInBackground(Long... params) {
                        saveContact();
                        return null;
                    }

                    @Override
                    protected void onPostExecute(Object result) {
                        listener.onAddEditCompleted(rowID);
                    }
                }

                new SaveContactTask().execute();

            } else {
                // required contact name is blank, so display error dialog
                AlertDialog.Builder builder =
                        new AlertDialog.Builder(getActivity());
                builder.setMessage(R.string.error_message);

                // provide an OK button that simply dismisses the dialog
                builder.setPositiveButton(R.string.ok, null);
                builder.show();
            }
        }
    };

    private void saveContact() {
        DatabaseConnector databaseConnector =
                new DatabaseConnector(getActivity());

        if (contactInfoBundle == null) {
            // insert the contact information into the database
            rowID = databaseConnector.insertContact(
                    nameEditText.getText().toString(),
                    phoneEditText.getText().toString(),
                    emailEditText.getText().toString(),
                    streetEditText.getText().toString(),
                    cityEditText.getText().toString(),
                    stateEditText.getText().toString(),
                    zipEditText.getText().toString());
        } else {
            databaseConnector.updateContact(rowID,
                    nameEditText.getText().toString(),
                    phoneEditText.getText().toString(),
                    emailEditText.getText().toString(),
                    streetEditText.getText().toString(),
                    cityEditText.getText().toString(),
                    stateEditText.getText().toString(),
                    zipEditText.getText().toString());
        }
    }
}
