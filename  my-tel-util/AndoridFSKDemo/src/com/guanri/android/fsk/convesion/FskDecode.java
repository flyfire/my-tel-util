package com.guanri.android.fsk.convesion;

import com.guanri.android.lib.log.Logger;
import com.guanri.android.lib.utils.TypeConversion;



public class FskDecode {
	Logger logger = Logger.getLogger(FskDecode.class);
	private FskCodeParams fskCodeParams = null;//采样参数
	private SourceQueue sourceQueue = null;//数据员队列
	FskDecodeResult fskDecodeResult = null;//结果数据对象
	
	private int[] singleTArray = null;//频率较高的波形在一个周期内的采样点数组
	private int[] boundTArray = null;//一波特周期内的采样点数组
	private int[] singleMaxArray = null;//固定数量的最大值数组
	private int[] singleMinArray = null;//固定数量的最小值数组
	private int singleMaxArrayLength = 40;//固定数量的最大值数组长度
	public float splitParmats = 0.55f;//0，1分割参数
	
	private int singleTlength0 = 0;//0信号在一个周期内的采样点数 
	private int singleTlength1 = 0;//1信号在一个周期内的采样点数 
	private int singleTArraylength = 0;//频率较高的波形在一个周期内的采样点数
	
	private int boundTlength = 0;//1波特周期内的采样点数 
	
	public FskDecode(FskCodeParams fskCodeParams,SourceQueue sourceQueue,FskDecodeResult fskDecodeResult){
		this.fskCodeParams = fskCodeParams;// new FskCodeParams(2200,1200,11025,2,1200);
		this.sourceQueue = sourceQueue;
		this.fskDecodeResult = fskDecodeResult;
		this.init();
	}
	
	/**
	 * 根据参数FskCodeParams初始化相关数组信息
	 */
	private void init(){
		singleTlength0 = fskCodeParams.getSampleF()/fskCodeParams.getF0();//0信号在一个周期内的采样点数 
		singleTlength1 = fskCodeParams.getSampleF() /fskCodeParams.getF1();//1信号在一个周期内的采样点数 
		singleTArraylength = singleTlength0<singleTlength1?singleTlength0:singleTlength1;
		singleTArray = new int[singleTArraylength];//频率较高的波形在一个周期内的采样点数组
		
		boundTlength = fskCodeParams.getSampleF()/ fskCodeParams.getBoundRate();
		boundTArray = new int[boundTlength];//一波特周期内的采样点数组
			
		singleMaxArray = new int[singleMaxArrayLength];//固定数量的最大值数组
		singleMinArray = new int[singleMaxArrayLength];//固定数量的最小值数组
		
		reviseNextSigPos();
	}
	
	/**
	 * 初始化采样点信息
	 */
	private int singleCount = 0;
	private int nextSinglePosition = 0;
	private int position = 0;
	private boolean started = false;
	private void reviseNextSigPos(){
    	singleCount = 0;
    	nextSinglePosition = 0;
    	position = 0;
    	started = false;
    	calcNextSigPos();
    }	
	
	/**
	 * 计算下一个采样点的信息
	 */
	private void calcNextSigPos(){
		nextSinglePosition = nextSinglePosition +  boundTlength;
		singleCount++;
	    if (singleCount > fskCodeParams.getBoundRate()){
	    	singleCount -= fskCodeParams.getBoundRate();
	    	nextSinglePosition -= position;
	    	position = 0;
	    }
	}
	
	/**
	 * 计算最大平均值
	 */
	private int lastValue = 0;
	private int lastValue1 = 0;
	private int lastValue2 = 0;
	private int maxValue = 0;
	private int maxValueIndex = 0;
	private int totalMaxValue = 0;
	private int averageMaxValue = 0;
	private boolean haveMaxValueFull = false;
	private void calcMaxAverage(int sampleValue) {
		lastValue2 = lastValue1;
		lastValue1 = lastValue;
		lastValue = sampleValue;
		if (lastValue <= lastValue1 && lastValue2 <= lastValue1) { // 判断是否有最大值
			if (lastValue1 < 1000)
				return;
//			if (maxValue > 0) {
//				if (lastValue1 < (maxValue * 0.4))
//					return;
//				if (lastValue1 > (maxValue * 1.8))
//					return;
//			}
			//求最大平均值
			maxValue = lastValue1;
			maxValueIndex = maxValueIndex % singleMaxArrayLength;
			
			totalMaxValue = totalMaxValue - singleMaxArray[maxValueIndex] + maxValue;
			singleMaxArray[maxValueIndex] = maxValue;
			maxValueIndex++;

			if (haveMaxValueFull) {
				averageMaxValue = totalMaxValue / singleMaxArrayLength;
			} else {
				haveMaxValueFull = maxValueIndex >= singleMaxArrayLength;
				averageMaxValue = totalMaxValue / maxValueIndex;
			}
		}
	}

	/**
	 * 进行虑波
	 */
	private int singleTIndex = 0;
	private int singleTTotalValue = 0;
	private int boundTIndex = 0;
	private int boundTTotalValue = 0;
	private boolean isSingle0 = false;
	double splitValue = 0;
	
	private int singleFilterValue = 0;
	private int boundFilterValue = 0;

	public boolean decode(int sampleValue) {
		boolean result = false;
	    //根据信号波中0或者1的较大频率的波的周期进行滤波
		singleTIndex = singleTIndex % singleTArraylength;
		singleTTotalValue = singleTTotalValue - singleTArray[singleTIndex] + sampleValue;
		singleTArray[singleTIndex] = sampleValue;
		singleTIndex ++;
		singleFilterValue = singleTTotalValue / singleTArraylength;
		singleFilterValue = Math.abs(singleFilterValue);
	   
	   // 在波的周期滤波基础上进行波特周期滤波
	    boundTIndex = boundTIndex % boundTlength;
	    boundTTotalValue = boundTTotalValue - boundTArray[boundTIndex] + singleFilterValue;
	    boundTArray[boundTIndex] = singleFilterValue;
	    boundTIndex ++;
	    boundFilterValue = boundTTotalValue / boundTlength;
	    
	    calcMaxAverage(boundFilterValue);//求最大平均值
	    
	    //记录解码过程数据，是否记录在addSourceValue方法中进行判断
	    if(fskDecodeResult.isRecordDecodeInfo()){
	    	fskDecodeResult.addSourceValue(sampleValue, singleFilterValue, boundFilterValue, Math.round(averageMaxValue*splitParmats));
	    }
	    
	    double splitValue = averageMaxValue*splitParmats;
	    if (! started) {
	    	if (boundFilterValue < splitValue)
	    		position ++;
	    	else
	    		position = 0;

	      if (position >= (boundTlength - singleTArraylength + 1)) {
	    	  reviseNextSigPos();
	    	  started = true;
	    	  isSingle0 = true;
	    	  result = true;
	      }
	    } else {
	    	position ++;
	    	if (position >= nextSinglePosition) {
	    		calcNextSigPos();
	    		isSingle0 = boundFilterValue < splitValue;
	    		result = true;
	    	}
	    }
		
		return result;
	}
	
	/**
	 * 解码
	 */
	private byte decodeValue = 0;
	private byte state = s_Start;
	private static final byte s_Start = 0;
	private static final byte s_b0 = 1;
	private static final byte s_b1 = 2;
	private static final byte s_b2 = 3;
	private static final byte s_b3 = 4;
	private static final byte s_b4 = 5;
	private static final byte s_b5 = 6;
	private static final byte s_b6 = 7;
	private static final byte s_b7 = 8;
	private static final byte s_Stop = 9;
	public boolean decodeValue(int sampleValue){
		boolean result = false;
		if (decode(sampleValue)) {
			switch (state) {
			case s_Start:
				if (isSingle0) {
					state ++;
					decodeValue = 0;
				} else
					reviseNextSigPos();
				break;
			case s_b0:
			case s_b1:
			case s_b2:
			case s_b3:
			case s_b4:
			case s_b5:
			case s_b6:
			case s_b7:
				state ++;
				decodeValue >>= 1;
	    		if (! isSingle0) decodeValue |= 0x80; else decodeValue &= (0x7F);
				break;
			case s_Stop:
		         result = !isSingle0;
		         state = s_Start;
		         reviseNextSigPos();
				break;
			default:
				state = s_Start;						
			}
		}
		return result;
	}
	
	/**
	 * 开始解码
	 */
	public boolean isContinue = true;//是否停止
	public void beginDecode(){
		while(isContinue){
			if(sourceQueue.size()<=0){
				try{
					Thread.sleep(100);
				}catch(InterruptedException e){
					e.printStackTrace();
				}
				//isContinue = false;
			}else{
				byte[] source = sourceQueue.get();
				if(fskCodeParams.getSampleByteLength()==2){//采样精度为2byte
					for(int i=0;i<source.length/2;i++){
						if(decodeValue(TypeConversion.bytesToShort(source, i*2))){
							fskDecodeResult.addResult(decodeValue);//记录解码结果
							decodeValue = 0;
						}
					}
				}
			
			}
		}
	}
	


}