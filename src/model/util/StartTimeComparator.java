package model.util;

import java.util.Comparator;

import model.Flow;

public class StartTimeComparator implements Comparator<Flow> {

	@Override
	public int compare(Flow f1, Flow f2) {
		if(f1.getStartTime()-f2.getStartTime()>0)
			return 1;
		else if(f1.getStartTime()-f2.getStartTime()<0)
			return -1;
		else
			return 0;
	}


}
