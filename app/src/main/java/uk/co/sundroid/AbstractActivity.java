package uk.co.sundroid;

import android.app.ActionBar;
import android.app.Activity;
import android.os.Bundle;
import android.view.*;
import android.view.View.OnClickListener;
import android.widget.TextView;

import uk.co.sundroid.util.theme.ThemePalette;

import java.util.List;

import static uk.co.sundroid.NavItem.NavItemLocation.*;

/**
 * Provides some helper functions for activities.
 */
public abstract class AbstractActivity extends Activity implements OnClickListener {

    private List<NavItem> navItems;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ThemePalette.onActivityCreateSetTheme(this);
        getWindow().requestFeature(Window.FEATURE_ACTION_BAR);
        getWindow().setBackgroundDrawableResource(ThemePalette.getAppBg());
        ActionBar actionBar = getActionBar();
        if (actionBar != null) {
            actionBar.setBackgroundDrawable(getResources().getDrawable(ThemePalette.getActionBarBg()));
        }
    }

    @Override
    public final boolean onCreateOptionsMenu(Menu menu) {
        menu.clear();
        if (navItems != null) {
            for (NavItem navItem : navItems) {
                MenuItem menuItem = menu.add(Menu.NONE, navItem.getAction(), Menu.NONE, navItem.getTitle());
                if (navItem.getIcon() > 0) {
                    menuItem.setIcon(navItem.getIcon());
                }
                if (navItem.getLocation() == HEADER) {
                    menuItem.setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);
                } else if (navItem.getLocation() == HEADER_IF_ROOM) {
                    menuItem.setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM);
                } else {
                    menuItem.setShowAsAction(MenuItem.SHOW_AS_ACTION_NEVER);
                }
            }
        }
        return true;
    }

    @Override
    public final boolean onOptionsItemSelected(MenuItem item) {
        if (navItems != null) {
            for (NavItem navItem : navItems) {
                if (navItem.getAction() == item.getItemId()) {
                    onNavItemSelected(navItem.getAction());
                    return true;
                }
            }
        }
        onNavBackSelected();
        return true;
    }

    @Override
    public void onClick(View view) {

    }

    protected void onNavItemSelected(int itemPosition) {

    }

    protected void onNavBackSelected() {

    }

    protected final void setNavItems(List<NavItem> navItems) {
        this.navItems = navItems;
        invalidateOptionsMenu();
    }

    protected final void setActionBarTitle(String title) {
        setActionBarTitle(title, null);
    }

    protected final void setActionBarTitle(String title, String subtitle) {
        ActionBar actionBar = getActionBar();
        if (actionBar != null) {
            actionBar.setTitle(title);
            actionBar.setSubtitle(subtitle);
        }
    }

    protected final void setDisplayHomeAsUpEnabled() {
        ActionBar actionBar = getActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }
    }

    protected void text(int id, String text) {
        TextView view = findViewById(id);
        view.setText(text);
    }

    protected void show(int id, CharSequence text) {
        TextView view = findViewById(id);
        view.setVisibility(View.VISIBLE);
        view.setText(text);
    }

    protected void show(int... ids) {
        for (int id : ids) {
            findViewById(id).setVisibility(View.VISIBLE);
        }
    }

    protected void remove(int... ids) {
        for (int id : ids) {
            findViewById(id).setVisibility(View.GONE);
        }
    }

    protected void hide(int... ids) {
        for (int id : ids) {
            findViewById(id).setVisibility(View.INVISIBLE);
        }
    }

    protected void textInView(View view, int id, String text) {
        TextView child = view.findViewById(id);
        child.setText(text);
    }

    protected void showInView(View view, int id, CharSequence text) {
        TextView child = view.findViewById(id);
        child.setVisibility(View.VISIBLE);
        child.setText(text);
    }

    protected void showInView(View view, int... ids) {
        for (int id : ids) {
            view.findViewById(id).setVisibility(View.VISIBLE);
        }
    }

    protected void removeInView(View view, int... ids) {
        for (int id : ids) {
            view.findViewById(id).setVisibility(View.GONE);
        }
    }

}
