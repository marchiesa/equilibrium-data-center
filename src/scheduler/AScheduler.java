package scheduler;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import model.Flow;
import model.Link;
import model.Network;
import model.NetworkState;
import reader.ConfigReader;

public abstract class AScheduler {

	private NetworkState networkState;
	private Network network;
	private Routing routing;
	private Map<Integer,Integer> flowId2coreRouterId;
	private Map<Integer,List<Link>> flowId2path;
	private Map<Integer,Integer> flowId2oldCoreRouterId;
	private ConfigReader config;
	protected long numberOfFlowSwappingOperations=0;

	public AScheduler(){
		this.flowId2coreRouterId = new HashMap<Integer,Integer>();
		this.flowId2oldCoreRouterId = new HashMap<Integer,Integer>();
		this.flowId2path = new HashMap<Integer,List<Link>>();
	}
	
	public ConfigReader getConfig() {
		return config;
	}
	public void setConfig(ConfigReader config) {
		this.config = config;
	}

	public Routing getRouting() {
		return routing;
	}

	public void setRouting(Routing routing) {
		this.routing = routing;
	}

	public NetworkState getNetworkState() {
		return networkState;
	}

	public void setNetworkState(NetworkState networkState) {
		this.networkState = networkState;
	}
	
	public Network getNetwork() {
		return network;
	}

	public void setNetwork(Network network) {
		this.network = network;
	}

	public void moveFlowToCoreRouter(Flow flow, int coreRouterId){
		this.flowId2coreRouterId.put(flow.getId(), coreRouterId);
	}
	
	public int getCoreRouterForFlow(Flow flow) {
		return this.flowId2coreRouterId.get(flow.getId());
	}
	
	
	public Map<Integer, Integer> getFlowId2coreRouterId() {
		return flowId2coreRouterId;
	}

	public void setFlowId2coreRouterId(Map<Integer, Integer> flowId2coreRouterId) {
		this.flowId2coreRouterId = flowId2coreRouterId;
	}

	public Map<Integer, List<Link>> getFlowId2path() {
		return flowId2path;
	}

	public void setFlowId2path(Map<Integer, List<Link>> flowId2path) {
		this.flowId2path = flowId2path;
	}
	
	public Map<Integer, Integer> getFlowId2oldCoreRouterId() {
		return flowId2oldCoreRouterId;
	}

	public void setFlowId2oldCoreRouterId(Map<Integer, Integer> flowId2oldCoreRouterId) {
		this.flowId2oldCoreRouterId = flowId2oldCoreRouterId;
	}
	
	public void notifyFlowFinished(Flow flow){};

	public abstract void computeCoreRouterPerNewFlow(Flow flow);

	public void notifyOfNewTimeTick(double time, List<Flow> flows) {};

	public void initialize(){}

	public long getNumberOfFlowSwappingOperations() {
		return numberOfFlowSwappingOperations;
	}
	public void setNumberOfFlowSwappingOperations(long numberOfFlowSwappingOperations) {
		this.numberOfFlowSwappingOperations = numberOfFlowSwappingOperations;
	}

	public void setCurrentFlows(List<Flow> currentFlows) {
		// TODO Auto-generated method stub
		
	};
	
	
	
	
	
}
