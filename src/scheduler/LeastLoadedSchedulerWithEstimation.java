package scheduler;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import model.Flow;
import model.Link;
import model.Router;

public class LeastLoadedSchedulerWithEstimation extends ECMPScheduler{

	private double lastTime=0;
	private List<Flow> lastFlows;
	private Map<Integer,List<Router>> podId2coreSwitchesAvailable;
	private Map<Integer,Integer> hostId2coreRouterId;
	private Map<Integer,Set<Flow>> hostId2flows;
	private List<Flow> queued;


	public LeastLoadedSchedulerWithEstimation(){
		this.lastFlows = new LinkedList<Flow>();
		this.hostId2coreRouterId = new HashMap<Integer,Integer>();
		this.hostId2flows = new HashMap<Integer,Set<Flow>>();
		this.podId2coreSwitchesAvailable = new HashMap<Integer,List<Router>>();
		this.queued = new LinkedList<Flow>();
	}


	public void initialize(){
		for(int i=this.getConfig().getNetworkParity()-1;i>=0;i--){
			List<Router> coreSwitches = new LinkedList<Router>(this.getNetwork().getLayer2routers().get(3));
			this.podId2coreSwitchesAvailable.put(i, coreSwitches);
		}
		for(Router host : this.getNetwork().getLayer2routers().get(0)){
			List<Router> coreSwitches = this.podId2coreSwitchesAvailable.get(host.getPod());
			Router core = coreSwitches.get((int)(Math.random()*coreSwitches.size()));
			this.hostId2coreRouterId.put(host.getId(), core.getId());
			coreSwitches.remove(core);
			Set<Flow> flows = new TreeSet<Flow>();
			this.hostId2flows.put(host.getId(), flows);
		}
	}


	@Override
	public void computeCoreRouterPerNewFlow(Flow flow) {		
		if(this.hostId2coreRouterId.get(flow.getDestinationId())==null){
			super.computeCoreRouterPerNewFlow(flow);
			Integer a =null; a.byteValue();
			this.hostId2coreRouterId.put(flow.getDestinationId(), this.getFlowId2coreRouterId().get(flow.getId()));
		}else{
			if(this.getFlowId2coreRouterId().get(flow.getId()) != null)
				this.getFlowId2oldCoreRouterId().put(flow.getId(), this.getFlowId2coreRouterId().get(flow.getId()));
			this.getFlowId2coreRouterId().put(flow.getId(), this.hostId2coreRouterId.get(flow.getDestinationId()));
			List<Link> path = this.getRouting().getPathOfFlow(flow, this.hostId2coreRouterId.get(flow.getDestinationId()));
			this.getFlowId2path().put(flow.getId(), path);
		}
		Set<Flow> flows = this.hostId2flows.get(flow.getDestinationId());
		flows.add(flow);
	}


}
