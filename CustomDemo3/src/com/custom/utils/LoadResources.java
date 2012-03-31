package com.custom.utils;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;

import org.json.JSONArray;
import org.json.JSONObject;

import android.content.Context;
import android.content.res.AssetManager;

import com.custom.update.Constant;

public class LoadResources {
	private static final Logger logger = Logger.getLogger(LoadResources.class);
	static boolean secrete = true;

	public final static HashMap<String,JSONObject> updateInstalledInfo = new HashMap<String,JSONObject>();//升级信息
	public static String lastModifyTime = "";
	public static String noModifyTime = "0";
	private static JSONObject json = null;
	
	public final static HashMap<String,Integer> installedfolds = new HashMap<String,Integer>();
	public final static HashMap<String,Integer> noInstalledfolds = new HashMap<String,Integer>();
	static{
		installedfolds.put("语文", 0);
		installedfolds.put("英语", 0);
		installedfolds.put("数学", 0);
	}
	static{
		noInstalledfolds.put("语文", 0);
		noInstalledfolds.put("英语", 0);
		noInstalledfolds.put("数学", 0);
	}	
	

	public static void loadUpdateInstalledInfo(){
		logger.error("loadUpdateInstalledInfo");
		try{
			String filePath = Constant.getDataPath()+File.separator+Constant.installedInfo;
			byte[] buf = LoadResources.loadFile(filePath);
			if(buf==null){
				return ;
			}
			String info = new String(buf,"GBK");
			logger.error(info);
			json = new JSONObject(info);
			JSONArray list = null;
			try{
				list = json.getJSONArray(Constant.root);
			}catch(Exception e){
				list = new JSONArray();
				json.put(Constant.root, list);
			}
			for(int i=0;i<list.length();i++){
				try{
					JSONObject installed = list.getJSONObject(i);
					if(installed==null)
						continue;
					logger.error(installed.getString(Constant.updateId));
					updateInstalledInfo.put(installed.getString(Constant.updateId), installed);
				}catch(Exception e){
					e.printStackTrace();
				}
			}
			lastModifyTime = json.getString(Constant.modifyTime);
			noModifyTime = String.valueOf((new SimpleDateFormat(Constant.timeFormate).parse(lastModifyTime).getTime()-new Date().getTime())/(24*60*60*1000));

		}catch(Exception e){
			e.printStackTrace();
		}
	}
	
	/**
	 * 查询升级信息
	 */
	public static void initInstalledInfo(){
		logger.error("initInstalledInfo");
		try{
			//清空未安装情况
			try{
		    	Iterator it = noInstalledfolds.keySet().iterator();
		    	while(it.hasNext()){
		    		String name = (String)it.next();
		    		noInstalledfolds.put(name, 0);
		    	}
			}catch(Exception e){
				
			}
			Iterator it = updateInstalledInfo.keySet().iterator();
			while(it.hasNext()){
				try{
					JSONObject installed = updateInstalledInfo.get(it.next());
					if(installed==null)
						continue;
					String unZipflag = null;
					try{
						unZipflag = installed.getString(Constant.fileUnziped);
					}catch(Exception e){}
					if ( unZipflag==null){
						try{
							JSONArray contents = installed.getJSONArray(Constant.fileContent);
							logger.error("installedfolds:"+contents.length()+":"+contents.toString());
							for(int j=0;j<contents.length();j++){
								JSONObject content = contents.getJSONObject(j);
								String name = content.getString("name");
								int value = content.getInt("value");
								if(noInstalledfolds.containsKey(name))
									noInstalledfolds.put(name, noInstalledfolds.get(name)+value);
								else
									noInstalledfolds.put(name, value);
								logger.error("noInstalledfolds:"+noInstalledfolds.get(name));
										
							}
						}catch(Exception e){
							e.printStackTrace();
						}
					}else{
						try{
							JSONArray contents = installed.getJSONArray(Constant.fileContent);
							for(int j=0;j<contents.length();j++){
								logger.error("noInstalledfolds:"+contents.length()+":"+contents.toString());
								try{
									JSONObject content = contents.getJSONObject(j);
									String name = content.getString("name");
									int value = content.getInt("value");
									if(installedfolds.containsKey(name)&&installedfolds.get(name)>0)
										installedfolds.put(name, noInstalledfolds.get(name)+value);
									else
										installedfolds.put(name, value);
									logger.error("installedfolds:"+name+":"+installedfolds.get(name));
								}catch(Exception e){
									e.printStackTrace();
								}		
							}
						}catch(Exception e){
							e.printStackTrace();
						}	
					}
				}catch(Exception e){
					e.printStackTrace();
				}
			}
		}catch(Exception e){
			e.printStackTrace();
		}
	}
	
	/**
	 * 更新数据
	 * @param installed
	 */
	public static void updateInstalledInfo(JSONObject installed){
		logger.error("updateInstalledInfo");
		String filePath = Constant.getDataPath()+File.separator+Constant.installedInfo;
		try{
			if(json==null){
				json = new JSONObject();
				json.put(Constant.root, new JSONArray());
				logger.error("new json:"+json.toString());
			}
			
			if(updateInstalledInfo.containsKey(installed.getString(Constant.updateId))){
				JSONArray list = json.getJSONArray(Constant.root);
				for(int i=0;i<list.length();i++){//如果已经存在，则替换
					JSONObject temp = list.getJSONObject(i);
					logger.error("installedInfo json:"+list.length());
					if(temp.getString(Constant.updateId).equals(Constant.updateId)){
						list.put(i, updateInstalledInfo);
						break;
					}
				}
			}else{
				json.getJSONArray(Constant.root).put(installed);
			}
			
			updateInstalledInfo.put(installed.getString(Constant.updateId), installed);
			
			json.put(Constant.modifyTime, new SimpleDateFormat(Constant.timeFormate).format(new Date()));
			logger.error("update json:"+json.toString());
			writeFile(filePath,json.toString());
			
		}catch(Exception e){
			e.printStackTrace();
		}
	}	
	
	/**
	 * 清空已经下载了多少业务记录
	 *
	 */
	public static void clearInstalledFoldInfo(Context context){
		logger.error("clearInstalledFoldInfo");
		FileOutputStream out = null;
        try{
    		out = context.openFileOutput(Constant.installedFold,Context.MODE_WORLD_READABLE);
    		out.write(("").getBytes());
    		out.flush();
        }catch(Exception e){
        	e.printStackTrace();
        }finally{
        	try{
        		if(out!=null)
        			out.close();
            }catch(Exception e){}
        }
        //queryInstalledFoldInfo(context);
	}
	/**
	 * 查询已经下载了多少业务
	 */
	public static HashMap<String,Integer> queryInstalledFoldInfo(Context context){
		logger.error("queryInstalledFoldInfo");
		//清空未安装情况
		try{
	    	Iterator it = installedfolds.keySet().iterator();
	    	while(it.hasNext()){
	    		String name = (String)it.next();
	    		installedfolds.put(name, 0);
	    	}
		}catch(Exception e){
			
		}
		//读取数据
		
		FileInputStream in = null;
		
		try{
			in = context.openFileInput(Constant.installedFold);
			BufferedReader fin = new BufferedReader(new InputStreamReader(in,"GBK"));
			String line = fin.readLine();
			int count = 0;
			logger.error("queryDownedFold:"+line);
			while(line!=null){
				logger.error(line);
				line = line.substring(line.indexOf('=')+1);
				
				if(line.indexOf("=")>0){
					try{
						count = Integer.parseInt(line.substring(line.indexOf("=")+1).trim());
					}catch(Exception e){
						count = 0;
					}
					installedfolds.put(line.substring(0,line.indexOf("=")), count);
				}
				line = fin.readLine();
			}
		}catch(Exception e){
			e.printStackTrace();
		}finally{
			try{
				if(in!=null)
					in.close();
			}catch(Exception e){}
		}
		loadUpdateInstalledInfo();
		initInstalledInfo();//分析下载文件中的情况
		
		return installedfolds;
	}
	
	/**
	 * 根据路径和路径类型读取文件
	 * @param context
	 * @param filePath
	 * @param dirType
	 * @return
	 */
	public static byte[] loadFile(String filePath){
		InputStream in= null;
		try{
			in= new FileInputStream(filePath);
			byte[] buf = new byte[in.available()];
			in.read(buf,0,buf.length);
			return buf;
		}catch(Exception e){
			return null;
		}finally{
			if(in!=null){
				try{
					in.close();
				}catch(Exception e){
					
				}
				
			}
		}
	}	
	
	/**
	 * 根据路径和路径类型读取文件
	 * @param context
	 * @param filePath
	 * @param dirType
	 * @return
	 */
	public static byte[] loadFile(Context context,String filePath,int dirType){
		InputStream in= null;
		logger.error("filePath:"+filePath+":dirType:"+dirType);
		try{
			if(dirType==0){
				AssetManager assetManager = context.getAssets();
				in= assetManager.open(filePath);
			}else if(dirType==1){
				//in= new FileInputStream(Constant.getDataPath()+File.separator+filePath);
				in= new FileInputStream(context.getFilesDir().getAbsoluteFile()+File.separator+filePath);
			}else if(dirType==2){
				in= new FileInputStream(Constant.getSdPath()+File.separator+filePath);
			}
			byte[] buf = new byte[in.available()];
			in.read(buf,0,buf.length);
			return buf;
		}catch(Exception e){
			return null;
		}finally{
			if(in!=null){
				try{
					in.close();
				}catch(Exception e){
					
				}
				
			}
		}
	}
	

	
	/**
	 * 根据路径和路径类型读取文件
	 * @param context
	 * @param filePath
	 * @param dirType
	 * @return
	 */
	public static void writeFile(String filePath,String value){
		FileOutputStream in= null;
		try{
			in= new FileOutputStream(filePath);
			byte[] buff = value.getBytes("GBK");
			in.write(buff);
			in.flush();
		}catch(Exception e){
		}finally{
			if(in!=null){
				try{
					in.close();
				}catch(Exception e){
					
				}
				
			}
		}
	}
}
