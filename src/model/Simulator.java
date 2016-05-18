package model;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import flowgenerator.FlowsHandler;
import reader.ConfigReader;
import reader.NetworkReader;
import scheduler.AScheduler;
import scheduler.ECMPScheduler;
import scheduler.LeastLoadedScheduler;
import scheduler.LeastLoadedSchedulerWithDemands;
import scheduler.NonBlockingScheduler;
import scheduler.Routing;
import scheduler.RoutingNonBlocking;
import scheduler.SimulatedAnnealingScheduler;
import statistics.StatisticsManager;

public class Simulator {

	private Network network;
	private NetworkState networkState;
	private ConfigReader config;
	private NetworkReader networkReader;
	private FlowsHandler flowHandler;
	private Routing routing;
	private String configFile;

	public Simulator(String configFile) throws IOException{
		this.config = new ConfigReader();
		this.config.readConfig(configFile);
		if(config.getSchedulerType().equals("non-blocking")){
			this.routing = new RoutingNonBlocking();
		}else{
			this.routing = new Routing();
		}
		this.networkReader = new NetworkReader(config,routing);
		this.network= this.networkReader.readNetwork();
		//System.out.println(network);
		this.routing.setNetwork(network);
		this.routing.constructClosDownwardsRouting();
		this.flowHandler = new FlowsHandler(config);
		this.networkState = new NetworkState();
		this.networkState.setNetwork(network);
		this.networkState.setRouting(routing);
	}

	public String getConfigFile() {
		return configFile;
	}

	public void setConfigFile(String configFile) {
		this.configFile = configFile;
	}

	public Network getNetwork() {
		return network;
	}
	public void setNetwork(Network network) {
		this.network = network;
	}

	public NetworkState getNetworkState() {
		return networkState;
	}
	public void setNetworkState(NetworkState networkState) {
		this.networkState = networkState;
	}	

	public void run() throws FileNotFoundException, UnsupportedEncodingException{
		double time=0;
		Double timeInterval = this.config.getUpdateInterval();
		Double bandwidthIncreaseRate = this.config.getBandwidthIncrease();
		List<Flow> currentFlows = new LinkedList<Flow>();
		List<Flow> finishedFlows = new LinkedList<Flow>();
		Double finishTime = config.getFinishTime();

		AScheduler scheduler=null;
		if(config.getSchedulerType().equals("ecmp")){
			scheduler = new ECMPScheduler();
		}else if(config.getSchedulerType().equals("least-loaded")){
			scheduler = new LeastLoadedScheduler();
		}else if(config.getSchedulerType().equals("simulated-annealing")){
			scheduler = new SimulatedAnnealingScheduler();
		}else if(config.getSchedulerType().equals("non-blocking")){
			scheduler = new NonBlockingScheduler();
		}else if(config.getSchedulerType().equals("least-loaded-demands")){
			scheduler = new LeastLoadedSchedulerWithDemands();
		}
		scheduler.setRouting(this.routing);
		scheduler.setNetworkState(networkState);
		scheduler.setNetwork(network);
		scheduler.setConfig(config);
		scheduler.initialize();

		StatisticsManager stat = new StatisticsManager(config);
		stat.setFlowId2flow(flowHandler.getFlowId2flow());

		System.out.println("finish-time: "+config.getFinishTime());
		System.out.println("initial-flow-rate: "+config.getInitialFlowRate());
		System.out.println("link-bandwidth: "+config.getLinkBandwidth());
		System.out.println("network-file: "+config.getNetworkFile());
		System.out.println("network-parity: "+config.getNetworkParity());
		System.out.println("output-file: "+config.getOutputFile());
		System.out.println("flow-file: "+config.getFlowsFile());
		System.out.println("scheduler-type: "+config.getSchedulerType());
		System.out.println("bandwidth-increase: "+config.getBandwidthIncrease());
		System.out.println("simulated-annealing: "+config.getSimulatedAnnealingFrequency());
		System.out.println("update-interval: "+config.getUpdateInterval());
		
		int numberOfActiveFlows = 0;
		int total =0;
		List<Flow> flows = new LinkedList<Flow>();
		while(time <= finishTime){

			if(time %0.01 <0.0001)System.out.println("time: " + time + " flows: " + numberOfActiveFlows + " finished: " + finishedFlows.size() + " total: " + total);

			scheduler.notifyOfNewTimeTick(time,flows);

			//get next events
			flows = this.flowHandler.getNewFlows(time);
			numberOfActiveFlows+=flows.size();
			total+=flows.size();

			//System.out.println("-- time: " + time );
			//update transmitted amount for older flows
			List<Flow> tempFlows = new LinkedList<Flow>();
			double sum = 0;
			for(Flow flow: currentFlows){
				//System.out.println("flow: " + flow.getId() + " rate: " + flow.getCurrentRate());
				sum += flow.getCurrentRate();
				double transmittedDuringInterval = flow.getCurrentRate()*timeInterval;
				double transmittedSoFar = flow.getTransmitted();
				flow.setTransmitted(transmittedSoFar+transmittedDuringInterval);
				if(flow.isFinished()){
					numberOfActiveFlows--;
					tempFlows.add(flow);
					stat.registerFlow(flow, flow.getStartTime(), time);
					stat.recordTransmittedFlow(time,flow.getSize()-transmittedSoFar);
				}else{
					stat.recordTransmittedFlow(time,transmittedDuringInterval);
				}
			}
			//if(time %0.01 <0.0001)System.out.println("average-rate: " + (sum/currentFlows.size()));

			//remove the finished flows
			for(Flow flow: tempFlows){
				finishedFlows.add(flow);
				currentFlows.remove(flow);
				List<Link> pathFlow = this.routing.getPathOfFlow(flow, scheduler.getCoreRouterForFlow(flow));
				this.networkState.removeFlowFromPath(flow, pathFlow);
				this.networkState.updateLinksState(pathFlow,-flow.getCurrentRate());
				scheduler.notifyFlowFinished(flow);
			}

			//compute the new routing paths for this events 
			for(Flow flow : flows){
				//System.out.println("new flow id: " + flow.getId() + " from " + flow.getSourceId() + " to " + flow.getDestinationId());
				scheduler.computeCoreRouterPerNewFlow(flow);
				List<Link> path = scheduler.getFlowId2path().get(flow.getId());
				networkState.addFlowToPath(flow, path);
				this.networkState.updateLinksState(path,flow.getCurrentRate());
			}

			//update the rate of the flows
			for(Flow flow: currentFlows){
				double flowIncrease = this.updateFlowRate(flow,timeInterval,bandwidthIncreaseRate);
				flow.setCurrentRate(flow.getCurrentRate()+flowIncrease);
				List<Link> path = scheduler.getFlowId2path().get(flow.getId());
				this.networkState.updateLinksState(path,flowIncrease);
			}
			for(Flow flow: flows){
				currentFlows.add(flow);
			}

			//check if any link is congested
			Set<Flow> congestedFlows = new TreeSet<Flow>();
			sum=0;
			for(Link link: this.network.getId2link().values()){
				double bandwidthUsed = this.networkState.getLinkId2bandwidthused().get(link.getId());
				sum+= bandwidthUsed;
				if(bandwidthUsed > link.getBandwidth()){
					//System.out.println("link: " + link.getId() + " used: " + bandwidthUsed + " available: " + link.getBandwidth() );
					for(Flow flow : this.networkState.getLinkId2flows().get(link.getId())){
						congestedFlows.add(flow);
					}
				}
			}
			//if(time %0.01 <0.0001)System.out.println("congestion: " + (sum/network.getId2link().size()));


			//reduce rate of flows through congested links
			for(Flow flow : congestedFlows){
				double removedAmountOfFlow = 0;
				if(flow.getCurrentRate()==1460)
					removedAmountOfFlow=flow.getCurrentRate()/2;
				else
					removedAmountOfFlow = flow.getCurrentRate()/2;
				//System.out.println("flow: " + flow.getId() + " flow-rate: " + flow.getCurrentRate() + " -> " + (flow.getCurrentRate()-removedAmountOfFlow));
				flow.setCurrentRate(flow.getCurrentRate()-removedAmountOfFlow);
				
				flow.setSlowStart(false);
				List<Link> path = scheduler.getFlowId2path().get(flow.getId());
				if(flow.isFinished())
						scheduler.notifyFlowFinished(flow);
				else
						this.networkState.updateLinksState(path,-removedAmountOfFlow);
			}

			//update time
			time+=timeInterval;
		}

		stat.storeStatistics();
	}

	private double updateFlowRate(Flow flow, double timeInterval, double bandwidthIncreaseRate) {
		double currentRate = flow.getCurrentRate();
		if(flow.isSlowStart()){
			currentRate*=2;
			return currentRate/2;
		}else{
			currentRate+=bandwidthIncreaseRate*timeInterval;
			return bandwidthIncreaseRate*timeInterval;
		}
	}

	public static void main(String args[]) throws IOException{
		Simulator simulator = new Simulator(args[0]);
		simulator.run();
	}

}