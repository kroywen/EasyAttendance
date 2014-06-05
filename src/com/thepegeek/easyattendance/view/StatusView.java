package com.thepegeek.easyattendance.view;

import java.util.List;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.animation.AnimationUtils;
import android.widget.TextView;
import android.widget.ViewFlipper;

import com.thepegeek.easyattendance.R;
import com.thepegeek.easyattendance.model.Status;
import com.thepegeek.easyattendance.util.Utils;

public class StatusView extends ViewFlipper implements OnClickListener {
	
	public interface OnStatusChangeListener {
		void onStatusChanged(Status status);
	}
	
	protected OnStatusChangeListener listener;
	protected float fromPosition;
	protected List<Status> statuses; 
	protected int current;
	
	public StatusView(Context context) {
		this(context, null);
	}

	public StatusView(Context context, AttributeSet attrs) {
		super(context, attrs);
		setClickable(true);
		setOnClickListener(this);
	}
	
	public void setOnStatusChangeListener(OnStatusChangeListener listener) {
		this.listener = listener;
	}
	
	/*
	@Override
	public boolean onTouchEvent(MotionEvent event) {
		switch (event.getAction()) {
		case MotionEvent.ACTION_DOWN:
			fromPosition = event.getX();
			break;
		case MotionEvent.ACTION_UP:
			float toPosition = event.getX();
			if (fromPosition > toPosition) {
				setInAnimation(AnimationUtils.loadAnimation(getContext(), R.anim.go_next_in));
				setOutAnimation(AnimationUtils.loadAnimation(getContext(), R.anim.go_next_out));
				showNext();
				if (listener != null) {
					Status nextStatus = getNextStatus();
					listener.onStatusChanged(nextStatus);
				}
			} else if (fromPosition < toPosition) {
				setInAnimation(AnimationUtils.loadAnimation(getContext(), R.anim.go_prev_in));
				setOutAnimation(AnimationUtils.loadAnimation(getContext(), R.anim.go_prev_out));
				showPrevious();
				if (listener != null) {
					Status prevStatus = getPreviousStatus();
					listener.onStatusChanged(prevStatus);
				}
			}
			break;
		}
		return true;
	}
	*/
	
	public void setStatuses(List<Status> statuses) {
		this.statuses = statuses;
		removeAllViews();
		if (!Utils.isEmpty(statuses)) {
			LayoutInflater inflater = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			for (Status status : statuses) {
				View view = inflater.inflate(R.layout.status_layout, this, false);
				((TextView) view.findViewById(R.id.name)).setText(status.getName());
				
				TextView absent = (TextView) view.findViewById(R.id.absent);
				absent.setBackgroundColor(status.getColor());
				absent.setText(status.isAbsent() ? R.string.absent : R.string.present);
				
				addView(view);
			}
		}
	}
	
	protected Status getNextStatus() {
		current = (current == statuses.size()-1) ? 0 : current+1;
		return statuses.get(current);
	}
	
	protected Status getPreviousStatus() {
		current = (current == 0) ? statuses.size()-1 : current-1;
		return statuses.get(current);
	}
	
	public void setCurrent(Status status) {
		if (status == null) return;
		for (int i=0; i<statuses.size(); i++) {
			if (statuses.get(i).getId() == status.getId()) {
				current = i;
				setDisplayedChild(i);
				break;
			}
		}
	}

	@Override
	public void onClick(View v) {
		setInAnimation(AnimationUtils.loadAnimation(getContext(), R.anim.go_next_in));
		setOutAnimation(AnimationUtils.loadAnimation(getContext(), R.anim.go_next_out));
		showNext();
		if (listener != null) {
			Status nextStatus = getNextStatus();
			listener.onStatusChanged(nextStatus);
		}
	}

}
