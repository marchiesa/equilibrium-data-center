package flowgenerator;

import java.io.IOException;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import model.Flow;
import reader.ConfigReader;
import reader.FlowReader;

public class FlowsHandler {
	
	private ConfigReader config;
	private FlowReader flowReader;
	private List<Flow> allRemainingFlowsSortedByStartTime;
	private List<Flow> removedFlowsSortedByStartTime;
	private Map<Integer,Flow> flowId2flow;
	
	public FlowsHandler(ConfigReader config) throws NumberFormatException, IOException{
		this.config = config;
		this.flowReader = new FlowReader(config);
		this.flowReader.readFlows();
		this.allRemainingFlowsSortedByStartTime=this.flowReader.getAllFlowsSortedByStartTime();
		this.flowId2flow = new HashMap<Integer,Flow>();
		for(Flow flow: this.allRemainingFlowsSortedByStartTime)
			this.flowId2flow.put(flow.getId(), flow);
		this.removedFlowsSortedByStartTime = new LinkedList<Flow>();
		
	}
	
	
	
	public Map<Integer, Flow> getFlowId2flow() {
		return flowId2flow;
	}



	public void setFlowId2flow(Map<Integer, Flow> flowId2flow) {
		this.flowId2flow = flowId2flow;
	}



	public List<Flow> getNewFlows(double time){
		LinkedList<Flow> flows = new LinkedList<Flow>();
		
		//add new flows
		for(Flow flow : allRemainingFlowsSortedByStartTime){
			if(flow.getStartTime()<=time)
				flows.add(flow);
			else
				break;
		}
		
		//remove these flows
		for(int i=0;i<flows.size();i++){
			this.removedFlowsSortedByStartTime.add(this.allRemainingFlowsSortedByStartTime.remove(0));
		}
		
		return flows;
	}

}
