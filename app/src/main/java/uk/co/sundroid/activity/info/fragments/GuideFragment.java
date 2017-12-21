package uk.co.sundroid.activity.info.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import uk.co.sundroid.AbstractFragment;
import uk.co.sundroid.R;
import uk.co.sundroid.R.id;
import uk.co.sundroid.util.theme.ThemePalette;

import static uk.co.sundroid.R.id.*;

public class GuideFragment extends AbstractFragment {

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle state) {
		if (container == null) {
			return null;
		}
        View view = inflater.inflate(R.layout.frag_info_guide, container, false);
    	view.findViewById(infGuideDisclaimerDisclosure).setOnClickListener(v -> toggle(v, id.infGuideDisclaimerBody));
    	view.findViewById(infGuideLocationDisclosure).setOnClickListener(v -> toggle(v, id.infGuideLocationBody));
    	view.findViewById(infGuideOfflineDisclosure).setOnClickListener(v -> toggle(v, id.infGuideOfflineBody));
    	view.findViewById(infGuideTimeDisclosure).setOnClickListener(v -> toggle(v, id.infGuideTimeBody));
    	view.findViewById(infGuideZonesDisclosure).setOnClickListener(v -> toggle(v, id.infGuideZonesBody));
    	view.findViewById(infGuideTrackerDisclosure).setOnClickListener(v -> toggle(v, id.infGuideTrackerBody));

        String disclaimer = "THIS DATA IS PROVIDED WITH NO GUARANTEE OF ACCURACY AND NO WARRANTY OF FITNESS FOR ANY PURPOSE.\n\nTimes are usually accurate to within a few minutes, but they do not take into account local geography or your altitude, so the observed times in hilly regions may be different. Errors in the far North and South can be much greater.";
        textInView(view, id.infGuideDisclaimerText, disclaimer);
		return view;
	}
	
	private void toggle(View disclosure, int bodyId) {
		View view = getView();
		if (view == null || isDetached()) {
			return;
		}
		View bodyView = view.findViewById(bodyId);
        if (bodyView.getVisibility() == View.GONE) {
            bodyView.setVisibility(View.VISIBLE);
			((TextView)disclosure).setCompoundDrawablesWithIntrinsicBounds(0, 0, ThemePalette.getDisclosureOpen(), 0);
        } else {
            bodyView.setVisibility(View.GONE);
			((TextView)disclosure).setCompoundDrawablesWithIntrinsicBounds(0, 0, ThemePalette.getDisclosureClosed(), 0);
        }
	}
	
}
