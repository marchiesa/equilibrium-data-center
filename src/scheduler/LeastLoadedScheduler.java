package scheduler;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import model.Flow;
import model.Link;
import model.Router;

public class LeastLoadedScheduler extends ECMPScheduler{

	@Override
	public void computeCoreRouterPerNewFlow(Flow flow) {
		if(flow.getSize()<=-1)
			super.computeCoreRouterPerNewFlow(flow);
		else{
			Router bestCoreRouter=null;
			double bestCongestion=Double.MAX_VALUE;
			for(Router coreRouter: this.getNetwork().getLayer2routers().get(3)){
				List<Link> path =this.getRouting().getPathOfFlow(flow, coreRouter.getId());
				double congestion = Double.MIN_VALUE;
				for(Link link : path){
					if(this.getNetworkState().getLinkId2bandwidthused().get(link.getId())>congestion)
						congestion=this.getNetworkState().getLinkId2bandwidthused().get(link.getId());
				}
				if(congestion<bestCongestion){
					bestCongestion=congestion;
					bestCoreRouter=coreRouter;
				}
			}
			if(this.getFlowId2coreRouterId().get(flow.getId()) != null)
				this.getFlowId2oldCoreRouterId().put(flow.getId(), this.getFlowId2coreRouterId().get(flow.getId()));
			this.getFlowId2coreRouterId().put(flow.getId(), bestCoreRouter.getId());
			List<Link> path = this.getRouting().getPathOfFlow(flow, bestCoreRouter.getId());
			this.getFlowId2path().put(flow.getId(), path);
		}
	}

}
