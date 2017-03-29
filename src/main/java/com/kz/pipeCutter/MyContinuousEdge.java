package com.kz.pipeCutter;

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

 public String toString()
 {
	 String type = "";
	 if(edgeType == EdgeType.START)
		 type = "START";
	 else if (edgeType == EdgeType.START)
		 type = "ONPIPE";
	 else if (edgeType == EdgeType.END)
		 type = "END";
	 return (this.edgeNo + " " + type);
 }
}
