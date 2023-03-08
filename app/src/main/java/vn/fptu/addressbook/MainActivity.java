package vn.fptu.addressbook;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentTransaction;

import android.os.Bundle;

public class MainActivity extends AppCompatActivity
        implements ContactListFragment.ContactListFragmentListener,
        DetailsFragment.DetailsFragmentListener,
        AddEditFragment.AddEditFragmentListener {

    // keys for storing row ID in Bundle passed to a fragment
    public static final String ROW_ID = "row_id";

    ContactListFragment contactListFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        if (savedInstanceState != null)
            return;

        if (findViewById(R.id.fragmentContainer) != null) {
            contactListFragment = new ContactListFragment();

            // add the fragment to the FrameLayout
            FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
            transaction.add(R.id.fragmentContainer, contactListFragment);
            transaction.commit();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        // if contactListFragment is null, activity running on tablet,
        // so get reference from FragmentManager
        if (contactListFragment == null) {
            contactListFragment = (ContactListFragment) getSupportFragmentManager().findFragmentById(R.id.contactListFragment);
        }
    }

    @Override
    public void onContactSelected(long rowID) {
        if (findViewById(R.id.fragmentContainer) != null) {
            displayContact(rowID, R.id.fragmentContainer);
        } else {
            getSupportFragmentManager().popBackStack();
            displayContact(rowID, R.id.rightPaneContainer);
        }

    }

    private void displayContact(long rowID, int viewID) {
        DetailsFragment detailsFragment = new DetailsFragment();

        // specify contact's row ID as an argument to the DetailsFragment
        Bundle arguments = new Bundle();
        arguments.putLong(ROW_ID, rowID);
        detailsFragment.setArguments(arguments);

        // use a FragmentTransaction to display the DetailsFragment
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.replace(viewID, detailsFragment);
        transaction.addToBackStack(null);
        transaction.commit();
    }

    @Override
    public void onAddContact() {
        if (findViewById(R.id.fragmentContainer) != null) {
            displayAddEditFragment(R.id.fragmentContainer, null);
        } else {
            displayAddEditFragment(R.id.rightPaneContainer, null);
        }
    }

    private void displayAddEditFragment(int viewID, Bundle arguments) {
        AddEditFragment addEditFragment = new AddEditFragment();
        // if editing existing contact, provide contact row ID as an argument
        if (arguments != null) {
            addEditFragment.setArguments(arguments);
        }

        // use a FragmentTransaction to display the AddEditFragment
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.replace(viewID, addEditFragment);
        transaction.addToBackStack(null);
        transaction.commit();
    }

    @Override
    public void onContactDeleted() {
        getSupportFragmentManager().popBackStack();
        if (findViewById(R.id.fragmentContainer) == null) {
            contactListFragment.updateContactList(); // refresh contacts
        }
    }

    @Override
    public void onEditContact(Bundle arguments) {
        if (findViewById(R.id.fragmentContainer) != null) {
            displayAddEditFragment(R.id.fragmentContainer, arguments);
        } else {
            displayAddEditFragment(R.id.rightPaneContainer, arguments);
        }
    }

    @Override
    public void onAddEditCompleted(long rowID) {
        getSupportFragmentManager().popBackStack();
        if (findViewById(R.id.fragmentContainer) == null) {
            contactListFragment.updateContactList(); // refresh contacts
        }
        displayContact(rowID, R.id.rightPaneContainer);
    }

}