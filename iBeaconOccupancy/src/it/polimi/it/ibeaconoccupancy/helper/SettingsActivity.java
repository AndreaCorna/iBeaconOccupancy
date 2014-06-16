package it.polimi.it.ibeaconoccupancy.helper;


import it.polimi.it.ibeaconoccupancy.R;
import android.os.Bundle;
import android.preference.PreferenceActivity;
import android.preference.PreferenceFragment;

/**
 * This class controls the settings activity in order to choose between logic on client
 * side and logic on server side.
 * @author Andrea Corna - Lorenzo Fontana
 *
 */
public class SettingsActivity extends PreferenceActivity
{
    @Override
    protected void onCreate(final Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        getFragmentManager().beginTransaction().replace(android.R.id.content, new SettingsActivityFragment()).commit();
    }

    public static class SettingsActivityFragment extends PreferenceFragment
    {
        @Override
        public void onCreate(final Bundle savedInstanceState)
        {
            super.onCreate(savedInstanceState);
            addPreferencesFromResource(R.xml.preferences);
        }
    }
}