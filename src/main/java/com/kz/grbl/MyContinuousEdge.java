package com.kz.grbl;

public class MyContinuousEdge extends MyEdge {
	public enum EdgeType {
    START, ONPIPE, END
	}
	
	EdgeType edgeType;
	
	public MyContinuousEdge(Integer edgeNo, Integer surfaceNo) {
		super(edgeNo, surfaceNo);
		this.edgeType = EdgeType.ONPIPE;
	}	
	
	public MyContinuousEdge() {
		super();
		this.edgeType = EdgeType.ONPIPE;
	}

}
