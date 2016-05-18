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

public class SimulatedAnnealingScheduler extends ECMPScheduler{

	private Map<Integer,Integer> hostId2coreRouterId;
	private Map<Integer,Set<Flow>> hostId2flows;
	private static final int INITIAL_TEMPERATURE = 100;
	private double lastTime = 0;
	private Map<Integer,List<Router>> podId2coreSwitchesAvailable;

	public  SimulatedAnnealingScheduler(){
		this.hostId2coreRouterId = new HashMap<Integer,Integer>();
		this.hostId2flows = new HashMap<Integer,Set<Flow>>();
		this.podId2coreSwitchesAvailable = new HashMap<Integer,List<Router>>();
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

	public void rerouteFlows(){

		//update energy
		double currentEnergy = this.computeEnergy();

		//run
		int i=0;
		double sum =0;
		for(int temperature =INITIAL_TEMPERATURE; temperature>0 ;temperature--){
			
			//if(temperature % 100 ==0)System.out.println("temp: " + temperature);
			
			//try to swap to hosts' core routers
			
			Map<String,Router> neighbor = this.computeNeighbor();
			
			if(neighbor==null)
				continue;
			Router host1 = neighbor.get("host1");
			Router host2 = neighbor.get("host2");
			
			//double energyGain = -1;
			//double energyGain = this.computeGainAndSwap(currentEnergy,host1,host2); //this step also swap flows
			double energyGain = this.computeOriginalGainAndSwap(currentEnergy,host1,host2); //this step also swap flows
			
			
			//if(this.computeProbability(energyGain,temperature)>Math.random()){
			if(energyGain<0){
				currentEnergy+=energyGain;
			}else{
				this.swapFlows(host1,host2);
			}		
		}
		//System.out.println("average time: " + (sum/i));
		 
	}

	private void swapFlows(Router host1, Router host2) {
		//System.out.println("flows: " + this.hostId2flows.get(host1.getId()).size());
		for(Flow flow : this.hostId2flows.get(host1.getId())){

			//remove
			List<Link> oldPath = this.getRouting().getPathOfFlow(flow, this.hostId2coreRouterId.get(host1.getId()));
			this.getNetworkState().removeFlowFromPath(flow, oldPath);
			this.getNetworkState().updateLinksState(oldPath,-flow.getCurrentRate());

			//add
			List<Link> newPath = this.getRouting().getPathOfFlow(flow, this.hostId2coreRouterId.get(host2.getId()));
			getNetworkState().addFlowToPath(flow, newPath);
			this.getNetworkState().updateLinksState(newPath,flow.getCurrentRate());
			this.getFlowId2path().put(flow.getId(), newPath);
			this.getFlowId2coreRouterId().put(flow.getId(), this.hostId2coreRouterId.get(host2.getId()));
		}

		//host2
		for(Flow flow : this.hostId2flows.get(host2.getId())){
			//remove
			List<Link> oldPath = this.getRouting().getPathOfFlow(flow, this.hostId2coreRouterId.get(host2.getId()));
			this.getNetworkState().removeFlowFromPath(flow, oldPath);
			this.getNetworkState().updateLinksState(oldPath,-flow.getCurrentRate());

			//add
			List<Link> newPath = this.getRouting().getPathOfFlow(flow, this.hostId2coreRouterId.get(host1.getId()));
			getNetworkState().addFlowToPath(flow, newPath);
			this.getNetworkState().updateLinksState(newPath,flow.getCurrentRate());
			this.getFlowId2path().put(flow.getId(), newPath);
			this.getFlowId2coreRouterId().put(flow.getId(), this.hostId2coreRouterId.get(host1.getId()));
		}
		int temp = this.hostId2coreRouterId.get(host1.getId());
		this.hostId2coreRouterId.put(host1.getId(), this.hostId2coreRouterId.get(host2.getId()));
		this.hostId2coreRouterId.put(host2.getId(), temp);
	}

	private double computeProbability(double energyGain, int temperature) {
		if(energyGain<0)
			return 1;
		else{
			return Math.pow(Math.E, 1000*temperature/INITIAL_TEMPERATURE*(-energyGain));
		}
	}

	private Map<String,Router> computeNeighbor(){
		Map<String,Router> chosenHosts = new HashMap<String,Router>();

		double random = Math.random()*3;
		//random = 2.5; //TODO: remove
		if(random<1){
			// (i) first neighbor function: swap the assigned core switches for any two 
			// randomly chosen hosts in a randomly chosen pod
			int randomPod = (int)(Math.random()*Math.sqrt(this.getNetwork().getLayer2routers().get(3).size())*2);
			//randomPod=0; //TODO: remove
			Set<Router> routersInPod = this.getNetwork().getPodId2hosts().get(randomPod);
			List<Router> hosts = new LinkedList<Router>();
			for(Router router : routersInPod)
				if(router.getLayer() == 0 && this.hostId2coreRouterId.get(router.getId())!=null)
					hosts.add(router);
			if(hosts.size()<3){
				return null;
			}
			Router host1 = hosts.get((int)(Math.random()*hosts.size()));
			Router host2 = hosts.get((int)(Math.random()*hosts.size()));
			while(host1.getId()==host2.getId()){
				host2 = hosts.get((int)(Math.random()*hosts.size()));
			}
			chosenHosts.put("host1", host1);
			chosenHosts.put("host2", host2);

		}else if(random<2){
			// (ii) second chosen hosts in a randomly chosen edge switch
			List<Router> edges = this.getNetwork().getLayer2routers().get(1);
			Router edge = edges.get((int)(Math.random()*edges.size()));	
			//edge = this.getNetwork().getId2router().get(12);
			List<Router> hosts = new LinkedList<Router>();
			for(Link link: edge.getBottomLinks())
				if(this.hostId2coreRouterId.get(link.getHeadRouter().getId())!=null)
					hosts.add(link.getHeadRouter());
			if(hosts.size()<3){
				return null;
			}
			Router host1 = hosts.get((int)(Math.random()*hosts.size()));
			Router host2 = hosts.get((int)(Math.random()*hosts.size()));
			while(host1.getId()==host2.getId()){
				host2 = hosts.get((int)(Math.random()*hosts.size()));
			}
			chosenHosts.put("host1", host1);
			chosenHosts.put("host2", host2);

		}else{
			// (iii) third neighbor function: randomly choose an edge or aggregation switch 
			// with equal probability and swap the assigned core switches for a random 
			// pair of hosts that use the chosen edge or aggregation switch to reach their 
			// currently assigned core switches.
			if(Math.random()<0.5){
				//if(false){ // TODO:remove
				//edge switch
				List<Router> edges = this.getNetwork().getLayer2routers().get(1);
				Router edge = edges.get((int)(Math.random()*edges.size()));
				//edge = this.getNetwork().getId2router().get(12); //TODO: remove
				List<Router> hosts = new LinkedList<Router>();
				for(Link link: edge.getBottomLinks())
					if(this.hostId2coreRouterId.get(link.getHeadRouter().getId())!=null)
						hosts.add(link.getHeadRouter());
				if(hosts.size()<3){
					return null;
				}
				Router host1 = hosts.get((int)(Math.random()*hosts.size()));
				Router host2 = hosts.get((int)(Math.random()*hosts.size()));
				while(host1.getId()==host2.getId()){
					host2 = hosts.get((int)(Math.random()*hosts.size()));
				}
				chosenHosts.put("host1", host1);
				chosenHosts.put("host2", host2);
			}else{
				//aggregation switch
				List<Router> aggregations = this.getNetwork().getLayer2routers().get(2);
				Router aggregation = aggregations.get((int)(Math.random()*aggregations.size()));
				//aggregation = this.getNetwork().getId2router().get(9); // TODO:remove
				List<Router> hosts = new LinkedList<Router>();
				Set<Integer> coreSwitches= new TreeSet<Integer>();
				for(Link link: aggregation.getUpperLinks())
					coreSwitches.add(link.getHeadRouter().getId());
				for(Integer hostId: this.hostId2coreRouterId.keySet()){
					if(coreSwitches.contains(this.hostId2coreRouterId.get(hostId)) && aggregation.getPod()==this.getNetwork().getId2router().get(hostId).getPod())
						hosts.add(this.getNetwork().getId2router().get(hostId));
				}
				if(hosts.size()<3){
					return null;
				}
				Router host1 = hosts.get((int)(Math.random()*hosts.size()));
				Router host2 = hosts.get((int)(Math.random()*hosts.size()));
				while(host1.getId()==host2.getId()){
					host2 = hosts.get((int)(Math.random()*hosts.size()));
				}
				chosenHosts.put("host1", host1);
				chosenHosts.put("host2", host2);
			}
		}
		return chosenHosts;
	}


	private double computeGainAndSwap(double currentEnergy,Router host1, Router host2) {
		this.swapFlows(host1, host2);
		double newEnergy = this.computeEnergy();
		return newEnergy-currentEnergy;
	}
	
	private double computeOriginalGainAndSwap(double currentEnergy,Router host1, Router host2) {
		this.swapFlows(host1, host2);
		double newEnergy = this.computeOriginalEnergy();
		return newEnergy-currentEnergy;
	}

	private double computeOriginalEnergy() {
		double energy =0;
		for(Link link : this.getNetwork().getId2link().values()){
			if(link.getBandwidth()<this.getNetworkState().getLinkId2bandwidthused().get(link.getId()))
				energy+=this.getNetworkState().getLinkId2bandwidthused().get(link.getId())-link.getBandwidth();
		}
		return energy;
	}

	private double computeEnergy() {
		double energy =0;
		for(Link link : this.getNetwork().getId2link().values()){
			energy+=Math.pow(this.getNetworkState().getLinkId2bandwidthused().get(link.getId())/link.getBandwidth(),6);
		}
		return energy;
	}





	@Override
	public void notifyFlowFinished(Flow flow){
		this.hostId2flows.get(flow.getDestinationId()).remove(flow);
		this.getFlowId2path().remove(flow.getId());
		this.getFlowId2coreRouterId().remove(flow.getId());
	};

	@Override
	public void notifyOfNewTimeTick(double time, List<Flow> flows) {
		double frequency = this.getConfig().getSimulatedAnnealingFrequency();
		if(((int)(time/frequency))!=lastTime){
			lastTime=(int)(time/frequency);	
			//System.out.println(lastTime);
			this.rerouteFlows();
		}
	};

}
