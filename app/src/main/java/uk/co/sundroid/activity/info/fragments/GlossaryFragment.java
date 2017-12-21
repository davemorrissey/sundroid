package uk.co.sundroid.activity.info.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import uk.co.sundroid.AbstractFragment;
import uk.co.sundroid.R;
import uk.co.sundroid.util.theme.ThemePalette;

import static uk.co.sundroid.R.id.*;

public class GlossaryFragment extends AbstractFragment {

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle state) {
		if (container == null) {
			return null;
		}
		View view = inflater.inflate(R.layout.frag_info_glossary, container, false);
    	view.findViewById(infGlossaryApsisDisclosure).setOnClickListener(v -> toggle(v, infGlossaryApsisBody));
    	view.findViewById(infGlossaryAzimuthDisclosure).setOnClickListener(v -> toggle(v, infGlossaryAzimuthBody));
    	view.findViewById(infGlossaryCivTwiDisclosure).setOnClickListener(v -> toggle(v, infGlossaryCivTwiBody));
    	view.findViewById(infGlossaryNtcTwiDisclosure).setOnClickListener(v -> toggle(v, infGlossaryNtcTwiBody));
    	view.findViewById(infGlossaryAstTwiDisclosure).setOnClickListener(v -> toggle(v, infGlossaryAstTwiBody));
    	view.findViewById(infGlossarySolNoonDisclosure).setOnClickListener(v -> toggle(v, infGlossarySolNoonBody));
    	view.findViewById(infGlossaryGoldenDisclosure).setOnClickListener(v -> toggle(v, infGlossaryGoldenBody));
    	view.findViewById(infGlossarySolsticeDisclosure).setOnClickListener(v -> toggle(v, infGlossarySolsticeBody));
    	view.findViewById(infGlossaryEquinoxDisclosure).setOnClickListener(v -> toggle(v, infGlossaryEquinoxBody));
    	view.findViewById(infGlossarySolEclipseDisclosure).setOnClickListener(v -> toggle(v, infGlossarySolEclipseBody));
    	view.findViewById(infGlossaryLunarEclipseDisclosure).setOnClickListener(v -> toggle(v, infGlossaryLunarEclipseBody));
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
