package uk.co.sundroid;

import android.app.Activity;
import android.app.Fragment;
import android.content.Context;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import uk.co.sundroid.R.id;

/**
 * Provides some helper functions for fragments.
 */
public abstract class AbstractFragment extends Fragment {

	protected Context getApplicationContext() {
        Activity activity = getActivity();
        if (activity == null) {
            return null;
        } else {
    		return activity.getApplicationContext();
        }
	}
    
    protected void textInView(View view, int id, CharSequence text) {
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
    
    protected void hideInView(View view, int... ids) {
    	for (int id : ids) {
    		view.findViewById(id).setVisibility(View.INVISIBLE);
    	}
    }
    
    protected void imageInView(View view, int id, int drawable) {
    	((ImageView)view.findViewById(id)).setImageResource(drawable);
    }

    protected int view(String name) throws IllegalArgumentException {
    	try {
    		return id.class.getField(name).getInt(null);
    	} catch (Exception e) {
    		throw new IllegalArgumentException("No view with id " + name + " exists");
    	}
    }

}
