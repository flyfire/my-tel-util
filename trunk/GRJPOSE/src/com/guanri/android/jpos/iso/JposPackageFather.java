package com.guanri.android.jpos.iso;

import java.io.UnsupportedEncodingException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.TreeMap;

import com.guanri.android.lib.log.Logger;
import com.guanri.android.lib.utils.TypeConversion;

/**
 * 封包父类
 * @author Administrator
 *
 */
public abstract class JposPackageFather {
	Logger logger = Logger.getLogger(JposPackageFather.class);
	
	protected TreeMap<Integer,Object> mSendMap = null;//表示需要分包的数据
	protected JposBitMap mBitMap= null;
	protected JposMessageType mMessageType = null;
	
	
	/**
	 * 构造方法
	 * @param sendMap
	 * @param messageType
	 */
	public JposPackageFather(TreeMap<Integer,Object> sendMap,JposMessageType messageType){
		this.mSendMap = sendMap;
		this.mMessageType = messageType;
		this.mBitMap = new JposBitMap();
	}
	
	/**
	 * POS请求构造MAC数据block
	 */
	public byte[] packagMacBlock(){
		return getMacSource();
	}
	
	/**
	 * 设置MAC值
	 * @param mac
	 * @return
	 */
	public boolean setMac(byte[] mac){
		mSendMap.put(64, mac);
		return true;
	}
	/**
	 * 获取位数据对象
	 * @param key
	 * @return
	 */
	public Object getSendMapValue(Integer key){
		return mSendMap.get(key);
	}
	/**
	 * 计算消息摘要需要的源数据
	 */
	protected abstract byte[] getMacSource();
	/**
	 * 对数据打包
	 * @return
	 */
	public byte[] packaged(){
		int dataLength = 0;//消息数据长度
		Iterator<Integer> keyIt = mSendMap.keySet().iterator();//遍历数据位
		ArrayList<byte[]> datas = new ArrayList<byte[]>(mSendMap.size());
		byte[] bitDataTemp = null;
		//遍历数据位
		while(keyIt.hasNext()){
			Integer key = keyIt.next();
			parseBitMap(key-1);//构造位图对象
			bitDataTemp = parseBitValue(key);
			if(bitDataTemp!=null){// 计算总长度
				dataLength +=bitDataTemp.length;
			}
			datas.add(bitDataTemp);
		}
		
		//解析位图
		byte[] baseBitmap = mBitMap.parseBitmapBase();
		logger.debug("位图数据："+TypeConversion.byteTo0XString(baseBitmap, 0, baseBitmap.length));
		

		byte[] resultBuffer = new byte[mMessageType.getMessageTypeLength() + baseBitmap.length + dataLength];
		mMessageType.setPageLength((short)(resultBuffer.length-2));
		
		int resultBufferIndex = 0;
		// 组装消息类型
		byte[] msgType = mMessageType.parseValue();
		logger.debug("msgType："+msgType.length);
		System.arraycopy(msgType, 0, resultBuffer, resultBufferIndex, msgType.length);
		resultBufferIndex +=msgType.length;
		// 组装位图字段
		logger.debug("baseBitmap："+baseBitmap.length);
		System.arraycopy(baseBitmap, 0, resultBuffer, resultBufferIndex, baseBitmap.length);
		resultBufferIndex +=baseBitmap.length;

		// 遍历获取每个域的数据
		for (int i = 0; i < datas.size(); i++) {
			byte[] temp = datas.get(i); 
			if(temp!=null){
				// 组装数据字段
				//logger.debug(resultBufferIndex+":"+resultBuffer.length+":"+temp.length);
				System.arraycopy(temp, 0, resultBuffer, resultBufferIndex, temp.length);
				resultBufferIndex += temp.length;
			}
		}
		
		return resultBuffer;
	}
	
	protected abstract byte[] parseBitValue(int position);
	
	/**
	 * 解析位信息
	 * 
	 * 调用相应类的方法
	 * 
	 * 方法名为parseFeild1，parseFeild2，parseFeild3，1、2、3分别表示位代码
	 * 
	 * @param position
	 * @return
	 */
	protected byte[] parseBitValue(int position,String methodName){
		//String methodName = PARSE_METHOD+position;//解析相应位数据的方法名称
		try{
			Method method = this.getClass().getMethod(methodName, Object.class);
			byte[] value = (byte[])method.invoke(this, mSendMap.get(position));
			return value;
		}catch(NoSuchMethodException e){
			e.printStackTrace();
			//NoSuchMethodException, SecurityException

		}catch(InvocationTargetException ive){
			ive.printStackTrace();
		}catch(IllegalAccessException ile){
			ile.printStackTrace();
		}
		return null;
		
	}
	
	/**
	 * 更新位图对象
	 * @param position
	 */
	protected void parseBitMap(int position){
		if(position>=64){//使用扩展位图
			if(!mBitMap.isExtendFlag()){//如需要的位图大于64，则加入扩展位图
				mBitMap.addBitmapExtend();
			}
			mBitMap.setBitmapExtend(position-64);
		}else{//使用扩展位图
			mBitMap.setBitmapBase(position);
		}
	}
	
	/**
	 * 字符串转ASCII
	 * @param s
	 * @param length
	 * @return
	 */
	public byte[] str2ASCII(String s,int length){
		byte[] temp = null;
		try{
			temp = TypeConversion.stringToAscii(s);
			if(temp!=null&&temp.length<length){
				byte[] newByte = new byte[length];
				System.arraycopy(temp, 0, newByte, length - temp.length, temp.length);
				temp  = newByte;
				
			}
		}catch(UnsupportedEncodingException e){
			
		}

		return temp;
	}
	/**
	 * 变长字符串转ACSII码
	 * @param s 字符串
	 * @param lengthBit  变长位数
	 * @return
	 */
	public byte[] floatLengthstr2ASCII(String s,int lengthBit){
		byte[] lengthtemp = null;
		//补足长度位
		String length = String.valueOf(s.length());
		if(length.length()<lengthBit){
			for(int i=lengthBit-length.length();i>0;i--){
				length= "0" + length;
			}
		}
		//获取长度位 BCD码
		lengthtemp = TypeConversion.str2bcd(length);
		//获取字符串的ACSII码
		byte[] temp = null;
		try{
			temp = TypeConversion.stringToAscii(s);
		}catch(UnsupportedEncodingException e){
			
		}
		//组合长度的BCD码与字符串的ASCII
		byte[] result = new byte[lengthtemp.length + temp.length];
		System.arraycopy(lengthtemp, 0, result, 0, lengthtemp.length);
		System.arraycopy(temp, 0, result, lengthtemp.length, temp.length);
		return result;
	}
	/**
	 * /*
	 * 可变长度数字字符串转BDC压缩码
	 * @param s 数据
	 * @param length  数据实际长度
	 * @param rightAlign 对齐方式
	 * @param lengthBit 几位变长
	 * @return
	 */
	public byte[] floatLengthStr2cbcd(String s,String length,boolean rightAlign,int lengthBit) {
		//补足长度位
		if(length.length()<lengthBit){
			for(int i=lengthBit-length.length();i>0;i--){
				length= "0" + length;
			}
		}
		
		if (s.length() % 2 != 0) {
			s = rightAlign?"0" + s:s+"0";//补足偶数的是否都放在左边？？？？？？？？
		}
		
		s = length + s;//在头部加上表示实际长度的数据
		
		if (s.length() % 2 != 0) {
			s = "0" + s;//补足偶数的是否都放在左边？？？？？？？？
		}
		return TypeConversion.str2bcd(s);
	}	
	/**
	 * 固定长度数字字符串转BDC压缩码
	 * @param s
	 * rightAlign 是否右对齐
	 * @return
	 */
	public byte[] fixLengthStr2cbcd(String s,int length,boolean rightAlign) {
		if(s.length()<length){//补足长度
			for(int i=length-s.length();i>0;i--){
				s = rightAlign?"0" + s:s+"0";
			}
		}
	
		return TypeConversion.str2bcd(s);
	}		
}