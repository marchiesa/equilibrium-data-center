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

public class LeastLoadedSchedulerWithDemands extends ECMPScheduler{

	private double lastTime=0;
	private List<Flow> lastFlows;
	private Map<Integer,List<Router>> podId2coreSwitchesAvailable;
	private Map<Integer,Integer> hostId2coreRouterId;
	private Map<Integer,Set<Flow>> hostId2flows;
	private List<Flow> queued;


	public LeastLoadedSchedulerWithDemands(){
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


	@Override
	public void notifyFlowFinished(Flow flow){
		this.getFlowId2path().remove(flow.getId());
		this.getFlowId2coreRouterId().remove(flow.getId());
		this.lastFlows.remove(flow);
	};

	public void notifyOfNewTimeTick(double time, List<Flow> flows) {
		double frequency = this.getConfig().getSimulatedAnnealingFrequency();
		List<Flow> toBeRemoved = new LinkedList<Flow>();
		for(Flow flow: queued)
			if(time-flow.getStartTime()>=0.10){
				this.lastFlows.add(flow);
				toBeRemoved.add(flow);
			}
		for(Flow flow : toBeRemoved){
			this.queued.remove(flow);
		}

		for(Flow flow : flows){
			if(flow.getSize()>1000000){
				if(time-flow.getStartTime()>=0.10)
					lastFlows.add(flow);
				else
					this.queued.add(flow);
			}
		}
		if(((int)(time/frequency))!=lastTime){
			lastTime=(int)(time/frequency);	
			//System.out.println(lastTime);
			this.rerouteFlows();
			//lastFlows = new LinkedList<Flow>();
		}
	}


	private void rerouteFlows() {
		//System.out.println("lastFlwos.size: " + lastFlows.size());
		for(Flow flow: lastFlows){
			if(flow.isFinished())
				continue;
			Router bestCoreRouter=null;
			double bestCongestion=Double.MAX_VALUE;
			Integer coreRouterId =this.getFlowId2coreRouterId().get(flow.getId());
			//System.out.println("flow-id: " + flow.getId() + " core-router: " + coreRouterId + " flow-finished: " + flow.isFinished());
			List<Link> path =this.getRouting().getPathOfFlow(flow, coreRouterId);
			double congestion = 0;
			for(Link link : path){
				double congestionOnLink= this.getNetworkState().getLinkId2bandwidthused().get(link.getId());
				if(this.getNetworkState().getLinkId2flows().get(link.getId()).contains(flow))
					congestionOnLink-=flow.getCurrentRate();
				if(congestionOnLink>congestion)
					congestion=congestionOnLink;
			}
			bestCongestion=congestion;
			bestCoreRouter=this.getNetwork().getId2router().get(coreRouterId);
			for(Router coreRouter: this.getNetwork().getLayer2routers().get(3)){
				if(coreRouter.getId()==this.getFlowId2coreRouterId().get(flow.getId())){
					continue;
				}
				path =this.getRouting().getPathOfFlow(flow, coreRouter.getId());
				congestion = 0;
				for(Link link : path){
					double congestionOnLink= this.getNetworkState().getLinkId2bandwidthused().get(link.getId());
					if(this.getNetworkState().getLinkId2flows().get(link.getId()).contains(flow))
						congestionOnLink-=flow.getCurrentRate();
					if(congestionOnLink>congestion)
						congestion=congestionOnLink;
				}
				if(congestion<bestCongestion){
					bestCongestion=congestion;
					bestCoreRouter=coreRouter;
					this.numberOfFlowSwappingOperations++;
				}
			}
			List<Link> oldPath = this.getRouting().getPathOfFlow(flow, this.getFlowId2coreRouterId().get(flow.getId()));
			this.getNetworkState().removeFlowFromPath(flow, oldPath);
			this.getNetworkState().updateLinksState(oldPath,-flow.getCurrentRate());

			//add
			List<Link> newPath = this.getRouting().getPathOfFlow(flow, bestCoreRouter.getId());
			getNetworkState().addFlowToPath(flow, newPath);
			this.getNetworkState().updateLinksState(newPath,flow.getCurrentRate());
			this.getFlowId2path().put(flow.getId(), newPath);
			this.getFlowId2coreRouterId().put(flow.getId(), bestCoreRouter.getId());			
		}
		//System.out.println("#swap: " + this.numberOfFlowSwappingOperations);
	}





}
