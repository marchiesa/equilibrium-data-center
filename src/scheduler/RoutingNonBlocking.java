package scheduler;

import java.util.LinkedList;
import java.util.List;

import model.Flow;
import model.Link;

public class RoutingNonBlocking extends Routing{

	
	public List<Link> getPathOfFlow(Flow flow, int coreRouterId){
		List<Link> path = new LinkedList<Link>();
		Link link1 = this.getNetwork().getId2router().get(flow.getSourceId()).getUpperLinks().get(0);
		Link link2 = this.getNetwork().getId2router().get(flow.getDestinationId()).getUpperLinks().get(0);
		Link link2reversed = this.getNetwork().getReversedLink(link2);
		path.add(link1);
		path.add(link2reversed);
		return path;
	}
}
