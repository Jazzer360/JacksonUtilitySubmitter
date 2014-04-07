package com.derekjass.jacksonutilitysubmitter;

import android.app.Activity;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

public class PurchaseGraphFragment extends Fragment {

	public interface GraphPurchasingAgent {
		public void purchaseGraph();
	}

	private GraphPurchasingAgent mAgent;

	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);

		try {
			mAgent = (GraphPurchasingAgent) activity;
		} catch (ClassCastException e) {
			throw new ClassCastException(activity.toString() +
					" must implement GraphPurchasingAgent");
		}
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View view = inflater.inflate(
				R.layout.fragment_purchase_graph, container, false);

		TextView desc = (TextView) view.findViewById(R.id.featureDescription);
		desc.setText(Html.fromHtml(getString(R.string.feature_description)));

		Button button = (Button) view.findViewById(R.id.purchaseButton);
		button.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				mAgent.purchaseGraph();
			}
		});

		return view;
	}
}
