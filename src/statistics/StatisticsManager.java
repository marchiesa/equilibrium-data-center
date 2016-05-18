package statistics;

import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.Map;

import model.Flow;
import reader.ConfigReader;

public class StatisticsManager {
	
	private ConfigReader config;
	private Map<Integer,Map<String,String>> flowId2metric2value;
	private Map<Integer,Flow> flowId2flow;
	private double transmittedFlow;
	private double granularity;
	private Map<Double,Double> time2transmitted;
	
	public StatisticsManager(ConfigReader config){
		this.flowId2metric2value =new HashMap<Integer,Map<String,String>>();
		this.config = config;
		this.transmittedFlow=0d;
		this.granularity =0.1d;
		this.time2transmitted = new HashMap<Double,Double>();
	}
	public Map<Integer, Flow> getFlowId2flow() {
		return flowId2flow;
	}
	public void setFlowId2flow(Map<Integer, Flow> flowId2flow) {
		this.flowId2flow = flowId2flow;
	}


	public void registerFlow(Flow flow, double startTime, double endTime){
		Map<String, String> metric2value = new HashMap<String, String>();
		metric2value.put("total-time", ""+(endTime-startTime));
		this.flowId2metric2value.put(flow.getId(), metric2value);
	}
	
	public Map<Integer,Map<String,String>> getStatistics(){
		return this.flowId2metric2value;
	}
	
	public void storeStatistics() throws FileNotFoundException, UnsupportedEncodingException{
		String file = config.getOutputFile();
		String fileFlows = file+"-flows.txt";
		String fileTotal = file+"-total.txt";
		PrintWriter writer = null;
		/*writer = new PrintWriter(fileFlows, "UTF-8");
		for(Integer flowId : this.flowId2metric2value.keySet()){
			Flow flow = this.flowId2flow.get(flowId);
			if(flow != null)
				writer.println(flowId + " " + flow.getSize() + " " + flow.getStartTime() + " " + this.flowId2metric2value.get(flowId).get("total-time"));
		}
		writer.close();*/
		
		writer = new PrintWriter(fileTotal, "UTF-8");		
		for(Double time : this.time2transmitted.keySet())
			writer.println(time + " " + this.time2transmitted.get(time) );
		writer.close();
	}
	public void recordTransmittedFlow(double transmittedDuringInterval) {
		this.transmittedFlow+=transmittedDuringInterval;
	}
	
	public void recordTransmittedFlow(double time, double transmittedDuringInterval) {
		double coarseTime = (int)(time/this.granularity);
		Double transmitted = this.time2transmitted.get(coarseTime);
		if(transmitted==null)
			transmitted=0d;
		transmitted+=transmittedDuringInterval;
		this.time2transmitted.put(coarseTime, transmitted);
	}
	
	/*public static void main(String args[]){
		double n1 = 0.2001;
		double n2 = 0.1;
		System.out.println();
	}*/

}
