package scheduler;

import model.Flow;

public class NonBlockingScheduler extends AScheduler{

	@Override
	public void computeCoreRouterPerNewFlow(Flow flow) {
		this.getFlowId2coreRouterId().put(flow.getId(), 0);
		this.getFlowId2path().put(flow.getId(), this.getRouting().getPathOfFlow(flow, 0));
	}
	

}
