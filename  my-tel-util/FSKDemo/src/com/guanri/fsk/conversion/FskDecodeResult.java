package com.guanri.fsk.conversion;

import com.guanri.android.lib.utils.TypeConversion;

public class FskDecodeResult {
	public final static int ZERO = Short.MIN_VALUE*2;
	public final static int ONE = Short.MAX_VALUE*2;
	
	private boolean recordInfo = false;//是否记录解码过程数据
	private SourceQueue resultDataList = null;//保持结果数据队列
	
	private byte[] data = null;//解码后的数据
	private int dataIndex = 0;
	
	int sourceIndex = 0;
	public int[] sourceValue = null;//元素数据数组
	public int[] singleFilter = null;//第一次滤波后数组
	public int[] boundFilter = null;//第二次滤波后数组
	public int[] maxAverage = null;//最大平均值*0.55
	
	public int[] valueZeroOne = null;//0，1值
	
	public FskDecodeResult(){
		this(false);
	}
	
	public FskDecodeResult(boolean recordInfo){
		this.recordInfo =  recordInfo ;
		this.resultDataList = new SourceQueue();
	}

	public boolean isRecordDecodeInfo(){
		return this.recordInfo;
	}
	
	public void addSourceValue(int source,int singleFilterValue,int boundFilterValue,int maxAverageValue){
		if(!this.recordInfo)
			return ;
		
		if(sourceValue == null){
			sourceValue = new int[1024];
			singleFilter = new int[1024];
			boundFilter = new int[1024];
			maxAverage = new int[1024];
			valueZeroOne  = new int[1024];
			sourceIndex = 0;
		}else if(sourceIndex>=sourceValue.length){
			int[] temp = sourceValue;
			sourceValue = new int[sourceIndex+1024];
			System.arraycopy(temp, 0, sourceValue, 0, sourceIndex);
			
			temp = singleFilter;
			singleFilter = new int[sourceIndex+1024];
			System.arraycopy(temp, 0, singleFilter, 0, sourceIndex);
			
			temp = boundFilter;
			boundFilter = new int[sourceIndex+1024];
			System.arraycopy(temp, 0, boundFilter, 0, sourceIndex);
			
			temp = maxAverage;
			maxAverage = new int[sourceIndex+1024];
			System.arraycopy(temp, 0, maxAverage, 0, sourceIndex);
			
			temp = valueZeroOne;
			valueZeroOne = new int[sourceIndex+1024];
			System.arraycopy(temp, 0, valueZeroOne, 0, sourceIndex);
			temp = null;
		}
		
		sourceValue[sourceIndex] = source;
		singleFilter[sourceIndex] = singleFilterValue;
		boundFilter[sourceIndex] = boundFilterValue;
		maxAverage[sourceIndex] = maxAverageValue;
		if(boundFilterValue>maxAverageValue){
			valueZeroOne[sourceIndex] = ONE;
		}else{
			valueZeroOne[sourceIndex] = ZERO;
		}
		
		sourceIndex++;
	}
	
	
	
	/**
	 * 加入解码结果
	 * @param value
	 */
	int headLength = 4;
	public synchronized void addResult(byte value){
		if(data == null){
			data = new byte[256];
			dataIndex = 0;
		}else if(dataIndex>=data.length){
			byte[] temp = data;
			data = new byte[dataIndex+256];
			System.arraycopy(temp, 0, data, 0, dataIndex);
			temp = null;
		}
		data[dataIndex] = value;
		dataIndex++;
		
//		if(dataIndex>4){
//			System.out.println("收到数据长度:"+dataIndex+"：长度："+TypeConversion.bytesToShortEx(data, 1));
//			try{
//				System.out.println("收到数据:"+TypeConversion.asciiToString(data, 4, dataIndex-4));
//			}catch(Exception e){
//				
//			}
//		}
		
		if(dataIndex>=headLength&&TypeConversion.bytesToShortEx(data, 1)==dataIndex-headLength){//一段数据接收完成
			if(dataIndex>headLength){
				byte[] temp = new byte[dataIndex-headLength];
				System.arraycopy(data, headLength,temp , 0, dataIndex-headLength);
				resultDataList.put(temp);
			}
			dataIndex = 0;
		}
	}
	
	/**
	 * 初始化结果数据
	 * @param value
	 */
	public synchronized int getDataIndex(){
		return dataIndex;
	}
	
	/**
	 * 初始化结果数据
	 * @param value
	 */
	public synchronized byte[] getData(){
		return resultDataList.get();
	}

	
}