package model;

public class Link {
	
	private int id;
	private int layer;
	private Router tailRouter;
	private Router headRouter;
	private double bandwidth;
	private boolean isUpward;
	
	public int getId() {
		return id;
	}
	public void setId(int id) {
		this.id = id;
	}
	public Router getTailRouter() {
		return tailRouter;
	}
	public void setTailRouter(Router tailRouter) {
		this.tailRouter = tailRouter;
	}
	public Router getHeadRouter() {
		return headRouter;
	}
	public void setHeadRouter(Router headRouter) {
		this.headRouter = headRouter;
	}
	public double getBandwidth() {
		return bandwidth;
	}
	public void setBandwidth(double bandwidth) {
		this.bandwidth = bandwidth;
	}
	public int getLayer() {
		return layer;
	}
	public void setLayer(int layer) {
		this.layer = layer;
	}
	public boolean isUpward() {
		return isUpward;
	}
	public void setUpward(boolean isUpward) {
		this.isUpward = isUpward;
	}
	public String toString(){
		return "L("+this.id+",t="+this.tailRouter+",h="+this.headRouter+",b="+this.bandwidth+")";
	}
	
}
