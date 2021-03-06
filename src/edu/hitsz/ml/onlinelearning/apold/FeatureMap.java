package edu.hitsz.ml.onlinelearning.apold;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.HashMap;

/**
 * 将字符特征转化为数字特征
 * @author tm
 *
 */
public class FeatureMap implements Serializable{

	/**
	 *
	 */
	private static final long serialVersionUID = 1L;
	private HashMap<String, Integer> features;
	private int featureNumber;

	public FeatureMap(){
		this(10000);
	}

	public FeatureMap(int num){
		features = new HashMap<String, Integer>(num);
		featureNumber = 0;
	}


	public int getFeatureNumber(){
		return features.size();
	}

	public HashMap<String, Integer> getMap(){
		return features;
	}

	public int size(){
		return features.size();
	}



	/**
	 * 添加新特征，返回特征对应的数字
	 * @param newFeature
	 * @param bTrain true:停止增长，不把特征放进Map; false:不停止增长，把特征放进Map
	 * @return
	 */
	public int add(String newFeature, boolean bTrain){
		if(features.containsKey(newFeature)){
			return features.get(newFeature);
		}
		else {
			if(bTrain) {
				features.put(newFeature, featureNumber);
				return featureNumber++;
			}
			else
				return -1;
		}
	}

	public void add(String newFeature, int i){
		features.put(newFeature, i);
		featureNumber++;
	}

	public int get(String newFeature){
		if(features.containsKey(newFeature)){
			return features.get(newFeature);
		}
		else{
			return -1;
		}
	}

	 public void writeObject (ObjectOutputStream out) throws IOException {
		out.writeObject(features);
	 }

	 public void readObject (ObjectInputStream in) throws IOException, ClassNotFoundException {
		features = (HashMap<String, Integer>) in.readObject();
	 }


}
