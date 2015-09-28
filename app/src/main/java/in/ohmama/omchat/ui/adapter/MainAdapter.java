package in.ohmama.omchat.ui.adapter;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

import java.lang.ref.WeakReference;
import java.util.Hashtable;

import in.ohmama.omchat.ui.fragment.AboutSelfFragment;
import in.ohmama.omchat.ui.fragment.ChatListFragment;
import in.ohmama.omchat.ui.fragment.RosterFragment;
import in.ohmama.omchat.ui.fragment.ExploreFragment;

/**
 * Created by Leon on 9/8/15.
 */
public class MainAdapter extends FragmentPagerAdapter {

    public Hashtable<Integer, WeakReference<Fragment>> fragmentReferences = new Hashtable<>();
    private FragmentManager fragmentManager;

    public MainAdapter(FragmentManager fm) {
        super(fm);
    }

    @Override
    public Fragment getItem(int position) {
        Fragment fragment = null;
        if (fragmentReferences.get(position) != null)
            fragment = fragmentReferences.get(position).get();
        if (fragment != null)
            return fragmentReferences.get(position).get();
        switch (position) {
            case 0:
                fragment = new ChatListFragment();
                break;
            case 1:
                fragment = new RosterFragment();
                break;
            case 2:
                fragment = new ExploreFragment();
                break;
            default:
                fragment = new AboutSelfFragment();
                break;
        }
        fragmentReferences.put(position, new WeakReference<Fragment>(fragment));
        return fragment;
    }

    @Override
    public int getCount() {
        return 4;
    }


}
