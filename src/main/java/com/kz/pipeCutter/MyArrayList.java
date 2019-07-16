package com.kz.pipeCutter;

import java.util.ArrayList;

public class MyArrayList<E> extends ArrayList<E>{
	@Override
	public String toString() {
		// TODO Auto-generated method stub
		String ret="";
		int size = this.size();
		
		for (int i=0; i<size;i++) {
			ret = ret + "\n" + this.get(i);
		}
		return ret;
	}
}
