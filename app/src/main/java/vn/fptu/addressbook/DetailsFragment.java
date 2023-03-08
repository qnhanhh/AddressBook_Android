package vn.fptu.addressbook;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.database.Cursor;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.fragment.app.Fragment;


public class DetailsFragment extends Fragment {
    public interface DetailsFragmentListener {
        void onContactDeleted();

        void onEditContact(Bundle arguments);
    }

    private DetailsFragmentListener listener;

    private long rowID = -1; // selected contact's row ID
    private TextView nameTextView;
    private TextView phoneTextView;
    private TextView emailTextView;
    private TextView streetTextView;
    private TextView cityTextView;
    private TextView stateTextView;
    private TextView zipTextView;

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        listener = (DetailsFragmentListener) activity;
    }

    // remove AddEditFragmentListener when Fragment detached
    @Override
    public void onDetach() {
        super.onDetach();
        listener = null;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true); // save fragment across config changes
        setHasOptionsMenu(true); // fragment has menu items to display

        if (savedInstanceState != null) {
            rowID = savedInstanceState.getLong(MainActivity.ROW_ID);
        } else {
            // get Bundle of arguments then extract the contact's row ID
            Bundle arguments = getArguments();
            if (arguments != null)
                rowID = arguments.getLong(MainActivity.ROW_ID);
        }

        // inflate GUI and get references to the TextViews
        View view =
                inflater.inflate(R.layout.fragment_details, container, false);
        nameTextView = view.findViewById(R.id.nameTextView);
        phoneTextView = view.findViewById(R.id.phoneTextView);
        emailTextView = view.findViewById(R.id.emailTextView);
        streetTextView = view.findViewById(R.id.streetTextView);
        cityTextView = view.findViewById(R.id.cityTextView);
        stateTextView = view.findViewById(R.id.stateTextView);
        zipTextView = view.findViewById(R.id.zipTextView);
        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        new LoadContactTask().execute(rowID);
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putLong(MainActivity.ROW_ID, rowID);
    }

    // display this fragment's menu items
    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.fragment_details_menu, menu);
    }

    // handle menu item selections
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_edit:
                // create Bundle containing contact data to edit
                Bundle arguments = new Bundle();
                arguments.putLong(MainActivity.ROW_ID, rowID);
                arguments.putCharSequence("name", nameTextView.getText());
                arguments.putCharSequence("phone", phoneTextView.getText());
                arguments.putCharSequence("email", emailTextView.getText());
                arguments.putCharSequence("street", streetTextView.getText());
                arguments.putCharSequence("city", cityTextView.getText());
                arguments.putCharSequence("state", stateTextView.getText());
                arguments.putCharSequence("zip", zipTextView.getText());
                listener.onEditContact(arguments); // pass Bundle to listener
                return true;
            case R.id.action_delete:
                deleteContact();
                return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private class LoadContactTask extends AsyncTask<Long, Object, Cursor> {
        DatabaseConnector databaseConnector =
                new DatabaseConnector(getActivity());

        // open database & get Cursor representing specified contact's data
        @Override
        protected Cursor doInBackground(Long... params) {
            databaseConnector.open();
            return databaseConnector.getOneContact(params[0]);
        }

        // use the Cursor returned from the doInBackground method
        @Override
        protected void onPostExecute(Cursor result) {
            super.onPostExecute(result);
            result.moveToFirst(); // move to the first item

            // get the column index for each data item
            int nameIndex = result.getColumnIndex("name");
            int phoneIndex = result.getColumnIndex("phone");
            int emailIndex = result.getColumnIndex("email");
            int streetIndex = result.getColumnIndex("street");
            int cityIndex = result.getColumnIndex("city");
            int stateIndex = result.getColumnIndex("state");
            int zipIndex = result.getColumnIndex("zip");

            // fill TextViews with the retrieved data
            nameTextView.setText(result.getString(nameIndex));
            phoneTextView.setText(result.getString(phoneIndex));
            emailTextView.setText(result.getString(emailIndex));
            streetTextView.setText(result.getString(streetIndex));
            cityTextView.setText(result.getString(cityIndex));
            stateTextView.setText(result.getString(stateIndex));
            zipTextView.setText(result.getString(zipIndex));

            result.close();
            databaseConnector.close();
        }
    }

    private void deleteContact() {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle(R.string.confirm_title);
        builder.setMessage(R.string.confirm_message);

        builder.setPositiveButton(R.string.button_delete, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int button) {
                final DatabaseConnector databaseConnector = new DatabaseConnector(getActivity());

                // AsyncTask deletes contact and notifies listener
                class DeleteContactTask extends AsyncTask<Long, Object, Object> {
                    @Override
                    protected Object doInBackground(Long... params) {
                        databaseConnector.deleteContact(params[0]);
                        return null;
                    }

                    @Override
                    protected void onPostExecute(Object result) {
                        listener.onContactDeleted();
                    }
                }

                // execute the AsyncTask to delete contact at rowID
                new DeleteContactTask().execute(rowID);
            }
        });
        builder.setNegativeButton(R.string.button_cancel, null);
        builder.show();
    }

}

