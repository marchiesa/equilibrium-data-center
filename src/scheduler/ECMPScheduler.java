package scheduler;

import java.util.List;

import model.Flow;
import model.Link;

public class ECMPScheduler extends AScheduler{

	@Override
	public void computeCoreRouterPerNewFlow(Flow flow) {
		int coreRouterIndex = (int)(Math.random()*this.getNetworkState().getNetwork().getLayer2routers().get(3).size());
		//coreRouterIndex = 0; // TODO: remove
		if(this.getFlowId2coreRouterId().get(flow.getId()) != null)
			this.getFlowId2oldCoreRouterId().put(flow.getId(), this.getFlowId2coreRouterId().get(flow.getId()));
		this.getFlowId2coreRouterId().put(flow.getId(), coreRouterIndex);
		//System.out.println("NEW! flow-id: " + flow.getId() + " core-router-id: " + coreRouterIndex);
		List<Link> path = this.getRouting().getPathOfFlow(flow, coreRouterIndex);
		this.getFlowId2path().put(flow.getId(), path);
	}

}
