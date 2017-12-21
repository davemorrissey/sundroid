package uk.co.sundroid.activity.info;

import android.app.Fragment;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;

import uk.co.sundroid.AbstractActivity;
import uk.co.sundroid.R;
import uk.co.sundroid.R.id;
import uk.co.sundroid.activity.info.fragments.AboutFragment;
import uk.co.sundroid.activity.info.fragments.GlossaryFragment;
import uk.co.sundroid.activity.info.fragments.GuideFragment;

import static uk.co.sundroid.R.id.*;

public class InfoActivity extends AbstractActivity implements OnClickListener {
	
	private static final String INFO_TAG = "infoFragment";

	private InfoGroup infoGroup;

    @Override
    public void onCreate(Bundle savedInstanceState) {
    	super.onCreate(savedInstanceState);
        setContentView(R.layout.info);
        setActionBarTitle("Help");
        setDisplayHomeAsUpEnabled();

    	this.infoGroup = InfoGroup.GUIDE;

    	initTabs();
    	changeFragment();
    }
    
    @Override
    protected void onNavBackSelected() {
        finish();
    }

    private void changeFragment() {
    	remove(id.infoFragment);
    	Fragment fragment;
    	if (this.infoGroup == InfoGroup.GUIDE) {
    		fragment = new GuideFragment();
    	} else if (this.infoGroup == InfoGroup.GLOSSARY) {
    		fragment = new GlossaryFragment();
    	} else {
            fragment = new AboutFragment();
        }
        getFragmentManager()
				.beginTransaction()
				.replace(id.infoFragment, fragment, INFO_TAG)
				.commit();
    	show(infoFragment);
    }
	
    private void setTab(InfoGroup infoGroup) {
    	if (!infoGroup.equals(this.infoGroup)) {
	    	this.infoGroup = infoGroup;
	    	updateTabs();
	    	changeFragment();
    	}
    }
	
    private void initTabs() {
    	findViewById(guideTabActive).setOnClickListener(v -> setTab(InfoGroup.GUIDE));
    	findViewById(guideTabInactive).setOnClickListener(v -> setTab(InfoGroup.GUIDE));
    	findViewById(glossaryTabActive).setOnClickListener(v -> setTab(InfoGroup.GLOSSARY));
    	findViewById(glossaryTabInactive).setOnClickListener(v -> setTab(InfoGroup.GLOSSARY));
        findViewById(aboutTabActive).setOnClickListener(v -> setTab(InfoGroup.ABOUT));
        findViewById(aboutTabInactive).setOnClickListener(v -> setTab(InfoGroup.ABOUT));
    	updateTabs();
    }
    
	private void updateTabs() {
		findViewById(guideTabActive).setVisibility(infoGroup == InfoGroup.GUIDE ? View.VISIBLE : View.GONE);
		findViewById(guideTabInactive).setVisibility(infoGroup == InfoGroup.GUIDE ? View.GONE : View.VISIBLE);
		findViewById(glossaryTabActive).setVisibility(infoGroup == InfoGroup.GLOSSARY ? View.VISIBLE : View.GONE);
		findViewById(glossaryTabInactive).setVisibility(infoGroup == InfoGroup.GLOSSARY ? View.GONE : View.VISIBLE);
        findViewById(aboutTabActive).setVisibility(infoGroup == InfoGroup.ABOUT ? View.VISIBLE : View.GONE);
        findViewById(aboutTabInactive).setVisibility(infoGroup == InfoGroup.ABOUT ? View.GONE : View.VISIBLE);
	}
    
}
