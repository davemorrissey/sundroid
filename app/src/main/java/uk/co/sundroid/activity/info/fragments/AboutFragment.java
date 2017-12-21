package uk.co.sundroid.activity.info.fragments;

import android.os.Build;
import android.os.Build.VERSION;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import uk.co.sundroid.AbstractFragment;
import uk.co.sundroid.BuildConfig;
import uk.co.sundroid.R.id;
import uk.co.sundroid.R.layout;

public class AboutFragment extends AbstractFragment {

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle state) {
		if (container == null) {
			return null;
		}
		View view = inflater.inflate(layout.frag_info_about, container, false);
        String version = String.format("Sundroid %s$1", BuildConfig.VERSION_NAME);
        textInView(view, id.aboutVersion, version);
        textInView(view, id.aboutCodes, getVersionCodes());
		return view;
	}

    public String getVersionCodes() {
        return "Build: " +
                BuildConfig.VERSION_CODE +
                ", API: " +
                VERSION.SDK_INT +
                "\n" +
                Build.MANUFACTURER +
                " " +
                Build.DEVICE;
    }

}
