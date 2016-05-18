package model;

public class Flow implements Comparable<Flow>{
	
	private int id;
	private int sourceId;
	private int destinationId;
	private Double size;
	private Double startTime;
	private double transmitted;
	private double currentRate;
	private boolean finished;
	private boolean slowStart;
	
	public Flow(){
		this.finished=false;
		this.slowStart=true;
	}
	
	public int getId() {
		return id;
	}
	public void setId(int id) {
		this.id = id;
	}
	public int getSourceId() {
		return sourceId;
	}
	public void setSourceId(int sourceId) {
		this.sourceId = sourceId;
	}
	public int getDestinationId() {
		return destinationId;
	}
	public void setDestinationId(int destinationId) {
		this.destinationId = destinationId;
	}
	public Double getStartTime() {
		return startTime;
	}
	public void setStartTime(Double startTime) {
		this.startTime = startTime;
	}
	public double getCurrentRate() {
		return currentRate;
	}
	public void setCurrentRate(double currentRate) {
		this.currentRate = currentRate;
	}
	public Double getSize() {
		return size;
	}
	public void setSize(Double size) {
		this.size = size;
	}
	public Double getTransmitted() {
		return transmitted;
	}
	public void setTransmitted(Double transmitted) {
		if(transmitted>=this.size){
			this.transmitted=this.size;
			this.finished=true;
		}
		else
			this.transmitted = transmitted;
	}

	public boolean isFinished() {
		return finished;
	}

	public void setFinished(boolean finished) {
		this.finished = finished;
	}

	public boolean isSlowStart() {
		return slowStart;
	}

	public void setSlowStart(boolean slowStart) {
		this.slowStart = slowStart;
	}
	
	public boolean equals(Object o ){
		Flow f = (Flow)o;
		return this.getId() == f.getId();
	}
	
	public int hashCode(){
		return this.id;
	}

	@Override
	public int compareTo(Flow o) {
		return this.getId() - o.getId();
	}
	
	
	

}
