package model;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import scheduler.Routing;

public class NetworkState {

	private Map<Integer,Double> linkId2bandwidthused;
	private Network network;
	private Routing routing;
	private Map<Integer,List<Flow>> linkId2flows;

	public NetworkState(){
		this.linkId2bandwidthused = new HashMap<Integer,Double>();
		this.linkId2flows = new HashMap<Integer,List<Flow>>();
	}

	public Network getNetwork() {
		return network;
	}

	public void setNetwork(Network network) {
		this.network = network;
		for(Link link: network.getId2link().values()){
			this.linkId2bandwidthused.put(link.getId(), 0d);
			this.linkId2flows.put(link.getId(), new LinkedList<Flow>());
		}
	}

	public Map<Integer, Double> getLinkId2bandwidthused() {
		return linkId2bandwidthused;
	}

	public void setLinkId2bandwidthused(Map<Integer, Double> linkId2bandwidthused) {
		this.linkId2bandwidthused = linkId2bandwidthused;
	}

	public Routing getRouting() {
		return routing;
	}

	public void setRouting(Routing routing) {
		this.routing = routing;
	}
	
	public Map<Integer, List<Flow>> getLinkId2flows() {
		return linkId2flows;
	}

	public void setLinkId2flows(Map<Integer, List<Flow>> linkId2flows) {
		this.linkId2flows = linkId2flows;
	}
	
	public void addFlowToLink(Flow flow, Link link){
		List<Flow> flowIds = this.linkId2flows.get(link.getId());
		if(flowIds==null){
			flowIds = new LinkedList<Flow>();
			this.linkId2flows.put(link.getId(), flowIds);
		}
		flowIds.add(flow);
	}
	
	public void addFlowToPath(Flow flow, List<Link> path){
		for(Link link : path)
			this.addFlowToLink(flow, link);
	}
	
	public void removeFlowFromLink(Flow flow, Link link){
		this.linkId2flows.get(link.getId()).remove(flow);
	}
	
	public void removeFlowFromPath(Flow flow, List<Link> path){
		for(Link link : path)
			this.removeFlowFromLink(flow, link);
	}

	public void updateLinksState(List<Link> path, double amount){
		for(Link link : path){
			double bandwidthUsed = this.linkId2bandwidthused.get(link.getId());
			/*if(link.getId()==37 && Math.random()<0.00002 && amount>0){
				System.out.println("LINK 37. bandwidth: " +  bandwidthUsed + " -> " + (bandwidthUsed+amount));
			}*/
			bandwidthUsed+=amount;
			this.linkId2bandwidthused.put(link.getId(), bandwidthUsed);
		}

	}

}
