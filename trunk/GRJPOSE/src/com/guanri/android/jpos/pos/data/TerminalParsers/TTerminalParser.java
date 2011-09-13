package com.guanri.android.jpos.pos.data.TerminalParsers;

import com.guanri.android.jpos.pos.data.Common;
import com.guanri.android.jpos.pos.data.Stream;
import com.guanri.android.jpos.pos.data.Fields.TFieldList.TResult_LoadFromBytes;
//import com.guanri.android.jpos.pos.data.Fields.TFieldList.TResult_SaveToBytes;
import com.guanri.android.jpos.pos.data.TerminalLinks.TTerminalLink;
import com.guanri.android.jpos.pos.data.TerminalMessages.*;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Random;

public class TTerminalParser {
	
	protected TTerminalLink FTerminalLink;
	protected int FIdent = 0;
	protected int FLastSerialNumber = 0;  //最后一次的流水号
	
	public void SetTerminalLink(TTerminalLink Value) {
		FTerminalLink = Value;
	}
	protected int GetNewIdent() {   //获取新的终端识别码
		Random rnd = new Random();
		FIdent = rnd.nextInt(65535);
		return FIdent;
	}
	
	protected String GetNowDateTime(){   // YYYYMMDDhhmmss
		Date date = new Date();
		SimpleDateFormat df = new SimpleDateFormat("yyyyMMddkkmmss");
		return df.format(date);
	}
	
	public void ParseRequest() {
		if (FTerminalLink == null) return;
		
		byte[] Bytes = null;
		
		Bytes = FTerminalLink.RecvPackage();
		if(Common.Length(Bytes) < 3) return;
		byte MsgType = Bytes[0];
		
		if (MsgType == 0) {  //数据传输
			byte CmdID = Bytes[2];
			switch (CmdID) {
			case 6:
				System.out.println("握手, 时间同步");
				
				THandshake_Response Handshake_Response = new THandshake_Response();
				
				Handshake_Response.DateTime().SetAsString(GetNowDateTime());
				Handshake_Response.SerialNumber().SetAsInteger(FLastSerialNumber);
				Handshake_Response.Ident().SetAsInteger(GetNewIdent());
				
				Stream.SetBytes(null);
				Handshake_Response.SaveToBytes();
				FTerminalLink.SendPackage(Stream.Bytes);			
				
				break;
			}
			return;
		}
		
		if (MsgType == 1) {
			System.out.println("交易报文");
			TTransaction Transaction = new TTransaction();
			
			
			Transaction.ClearProcess(); //清空流程
			
			Stream.SetBytes(Bytes);
			if (Transaction.LoadFormBytes() != TResult_LoadFromBytes.rfll_NoError) //报文格式错误
				return; 
			
			if (! Transaction.CheckMAC()){ //MAC签名错误
				System.out.println("MAC签名错误");
				return; 
			}	
			
			
			if (! Transaction.LoadProcess()){ //导入流程错误
				System.out.println("导入流程错误");
				return; 
			}	
			
			
			System.out.println("交易代码: " + Transaction.TransCode().GetAsInteger());
			System.out.println("2磁道数据: " + Transaction.ProcessList.GetTrack2Data());
			System.out.println("2磁道数据: " + Transaction.ProcessList.GetTrack3Data());
			System.out.println("主帐号: " + Transaction.ProcessList.GetPAN());
			
			Transaction.ClearProcess(); //清空流程
			Transaction.ProcessList.Response().SetAsString("00余额为" + 123.45);
			Transaction.SaveProcess();
			Transaction.SaveMAC();
			Stream.SetBytes(null);
			Transaction.SaveToBytes();
			
			FTerminalLink.SendPackage(Stream.Bytes);
			
			
		}
	}

}