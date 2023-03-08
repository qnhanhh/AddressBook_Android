package vn.fptu.addressbook;

import android.app.Activity;
import android.database.Cursor;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.CursorAdapter;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;
import android.widget.AdapterView.OnItemClickListener;

import androidx.fragment.app.ListFragment;

public class ContactListFragment extends ListFragment {

    public interface ContactListFragmentListener {
        void onContactSelected(long rowID);

        void onAddContact();
    }

    private ContactListFragmentListener listener;
    private CursorAdapter contactAdapter;

    // set ContactListFragmentListener when fragment attached
    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        listener = (ContactListFragmentListener) activity;
    }

    // remove ContactListFragmentListener when Fragment detached
    @Override
    public void onDetach() {
        super.onDetach();
        listener = null;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        setRetainInstance(true);
        setHasOptionsMenu(true);
        // set text to display when there are no contacts
        setEmptyText(getResources().getString(R.string.no_contacts));

        // get ListView reference and configure ListView
        ListView contactListView = getListView();
        contactListView.setOnItemClickListener(viewContactListener);
        contactListView.setChoiceMode(ListView.CHOICE_MODE_SINGLE);

        // map each contact's name to a TextView in the ListView layout
        String[] from = new String[]{"name"};
        int[] to = new int[]{android.R.id.text1};
        contactAdapter = new SimpleCursorAdapter(getActivity(), android.R.layout.simple_list_item_activated_1, null, from, to, 0);
        setListAdapter(contactAdapter);
    }

    OnItemClickListener viewContactListener = new AdapterView.OnItemClickListener() {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            listener.onContactSelected(id);
        }
    };

    // when fragment resumes, use a GetContactsTask to load contacts
    @Override
    public void onResume() {
        super.onResume();
        new GetContactsTask().execute((Object[]) null);
    }

    // performs database query outside GUI thread
    private class GetContactsTask extends AsyncTask<Object, Object, Cursor> {
        DatabaseConnector databaseConnector = new DatabaseConnector(getActivity());

        // open database & get Cursor representing all contacts
        @Override
        protected Cursor doInBackground(Object... params) {
            databaseConnector.open();
            return databaseConnector.getAllContacts();
        }

        // use the Cursor returned from the doInBackground method
        @Override
        protected void onPostExecute(Cursor result) {
            contactAdapter.changeCursor(result); // set the adapter's Cursor
            databaseConnector.close();
        }
    }

    // when fragment stops, close Cursor and remove from contactAdapter
    @Override
    public void onStop() {
        Cursor cursor = contactAdapter.getCursor();
        contactAdapter.changeCursor(null);

        if (cursor != null)
            cursor.close();

        super.onStop();
    }

    // display this fragment's menu items
    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.fragment_contact_list_menu, menu);
    }

    // handle choice from options menu
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.action_add) {
            listener.onAddContact();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    public void updateContactList() {
        new GetContactsTask().execute((Object[]) null);
    }
}
