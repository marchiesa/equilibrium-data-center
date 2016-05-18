package scheduler;

import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import model.Flow;
import model.Link;
import model.Network;
import model.Router;

public class Routing {

	private Map<Integer,	List<Integer>> routerId2downwardLinkIndices;
	private Map<Integer,Map<Integer,List<Link>>> host2coreRouter2links;
	private Map<Integer,Map<Integer,List<Link>>> coreRouter2host2links;
	private Network network;

	public Routing(){
		this.routerId2downwardLinkIndices = new HashMap<Integer, List<Integer>>();
		//this.flowId2coreRouterId = new HashMap<Integer,Integer>();
		//this.flowId2path = new HashMap<Integer,List<Link>>();
		this.host2coreRouter2links = new HashMap<Integer,Map<Integer,List<Link>>>();
		this.coreRouter2host2links = new HashMap<Integer,Map<Integer,List<Link>>>();
	}

	public void constructClosDownwardsRouting(){
		Router router= this.network.getLayer2routers().get(3).get(0);
		this.constructClosDownwardsRoutingRic(router, new LinkedList<Integer>());
		//this.computePathsBetweenHostsAndCoreRouters();
	}

	private void constructClosDownwardsRoutingRic(Router router, List<Integer> linkIndicesTemp) {
		if(router.getLayer()==0){
			LinkedList<Integer> linkIndices = new LinkedList<Integer>(linkIndicesTemp);
			this.routerId2downwardLinkIndices.put(router.getId(), linkIndices);
		}else{
			int i=0;
			for(Link bottomLink: router.getBottomLinks()){
				linkIndicesTemp.add(i);
				this.constructClosDownwardsRoutingRic(bottomLink.getHeadRouter(),linkIndicesTemp);
				linkIndicesTemp.remove(linkIndicesTemp.size()-1);
				i++;
			}
		}
	}

	public List<Integer> downwardLinkIdsByRouterId(int routerId) {
		return this.routerId2downwardLinkIndices.get(routerId);
	}

	public void addDownwardLinkId(Integer routerId, Integer downwardLinkId){
		List<Integer> downwardLinkIds = this.routerId2downwardLinkIndices.get(routerId);
		if(downwardLinkIds==null){
			downwardLinkIds = new LinkedList<Integer>();
			this.routerId2downwardLinkIndices.put(routerId, downwardLinkIds);
		}
		downwardLinkIds.add(downwardLinkId);
	}

	public Network getNetwork() {
		return network;
	}

	public void setNetwork(Network network) {
		this.network = network;
	}

	public Map<Integer, List<Integer>> getRouterId2downwardLinkIndices() {
		return routerId2downwardLinkIndices;
	}

	public void setRouterId2downwardLinkIndices(Map<Integer, List<Integer>> routerId2downwardLinkIndices) {
		this.routerId2downwardLinkIndices = routerId2downwardLinkIndices;
	}


	public List<Link> getPathOfFlow(Flow flow, int coreRouterId){
		//int coreRouterId = this.flowId2coreRouterId.get(flow.getId());
		Router coreRouter = this.network.getId2router().get(coreRouterId);
		int sourceId = flow.getSourceId();
		int destinationId = flow.getDestinationId();
		Router source = this.getNetwork().getId2router().get(sourceId);
		Router destination= this.getNetwork().getId2router().get(destinationId);
		List<Link> linkSourceUpward= new LinkedList<Link>();
		List<Link> linkDestinationDownward= new LinkedList<Link>();
		if(this.getNetwork().getId2router().get(sourceId).getPod()==this.getNetwork().getId2router().get(destinationId).getPod()){
			Router torSource = source.getUpperLinks().get(0).getHeadRouter();
			Router torDestination = destination.getUpperLinks().get(0).getHeadRouter();
			linkSourceUpward.add(source.getUpperLinks().get(0));
			linkDestinationDownward.add(network.getReversedLink(destination.getUpperLinks().get(0)));
			if(!torSource.equals(torDestination)){
				int aggregationIndex = (int)(Math.random()*torSource.getUpperLinks().size());
				Link aggregationLinkSourceRouter = torSource.getUpperLinks().get(aggregationIndex);
				Link aggregationLinkDestinationRouter = network.getReversedLink(torDestination.getUpperLinks().get((int)(aggregationIndex)));
				linkSourceUpward.add(aggregationLinkSourceRouter);
				linkDestinationDownward.add(aggregationLinkDestinationRouter);
			}
		}else{

			List<Integer> linkIndicesSource = this.routerId2downwardLinkIndices.get(sourceId);
			List<Integer> linkIndicesDestination = this.routerId2downwardLinkIndices.get(destinationId);
			List<Link> linkSourceDownward = this.network.getLinksFromIndicesForCoreRouter(linkIndicesSource,coreRouter);
			linkSourceUpward = this.network.getReversedLinks(linkSourceDownward);
			linkDestinationDownward = this.network.getLinksFromIndicesForCoreRouter(linkIndicesDestination,coreRouter);
		}
		List<Link> path = new LinkedList<Link>();
		path.addAll(linkSourceUpward);
		path.addAll(linkDestinationDownward);
		return path;
	}

	public void computePathsBetweenHostsAndCoreRouters (){
		//int coreRouterId = this.flowId2coreRouterId.get(flow.getId());
		for(Router host: this.network.getLayer2routers().get(0)){
			Map<Integer,List<Link>> coreRouter2links = new HashMap<Integer,List<Link>> ();
			for(Router coreRouter :this.network.getLayer2routers().get(3)){
				List<Integer> linkIndicesSource = this.routerId2downwardLinkIndices.get(host);
				List<Link> linkSourceDownward = this.network.getLinksFromIndicesForCoreRouter(linkIndicesSource,coreRouter);
				List<Link> linkSourceUpward = this.network.getReversedLinks(linkSourceDownward);
				coreRouter2links.put(coreRouter.getId(), linkSourceUpward);
			}
			this.host2coreRouter2links.put(host.getId(), coreRouter2links);
		}
		for(Router coreRouter: this.network.getLayer2routers().get(3)){
			Map<Integer,List<Link>> host2links = new HashMap<Integer,List<Link>> ();
			for(Router host :this.network.getLayer2routers().get(0)){
				List<Integer> linkIndicesSource = this.routerId2downwardLinkIndices.get(host);
				List<Link> linkSourceDownward = this.network.getLinksFromIndicesForCoreRouter(linkIndicesSource,coreRouter);
				host2links.put(host.getId(), linkSourceDownward);
			}
			this.host2coreRouter2links.put(coreRouter.getId(), host2links);
		}
	}

}
