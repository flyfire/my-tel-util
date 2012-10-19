package com.xiaoma.piccut.demo;

import java.io.File;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnCancelListener;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.googlecode.tesseract.android.TessBaseAPI;


/*����п���������������QQͷ��Ĳü��ϴ�����ôʵ�ֵģ����С��Ҳû����������֮��ѧϰ�£�������ǰ��Ŀ�������͵Ĺ��ܣ���Ϲٷ��ĵ�����Ľ���
���͸������ˣ���ĩ������ææд�ģ���¼�ڲ����ϣ�������ҽ���ѧϰ��Ҳ��������ܽ��С���ڴ���ע������������ʣ�������û���˻ش�С����
лл�ˣ�һ���ģ��ȿ���Ч��ͼ��Ч��ͼС���������ˣ�ֱ����ˮд��ȥ��С����ֱ����ģ������д�ģ���֤�������ʹ�ã���Ϊ�ܼ򵥣����ٿ���������ôʵ�ֵģ�
һ�������ֽ���
��������ؼ������¼���Ч��ͼ
����������֮��Ч��ͼ
�ģ��ü�����Ч��ͼ
�壺������󷵻ص�ͼƬЧ��ͼ
�����ü�������PICK�ı�����Ч��ͼ


���С������СDEMOԴ����ڸ������棬����Ҫ�����ѿ���������������ͬ����ѧϰ��Ҳ������˻ش���С��������ע������������⣬лл��������С������ææ�ڼ�д�ģ�
�����ɷ��ģ���...�������ж��ù���������꣬�������꣬�����Ҿ����ƣ���������ģ�˳���������£���𣬸����ʱ���������棬�ù�����ʱ�������Ĺ���������ʱ���
���˲���ڣ���𣬼��ͼ��ͣ���ҹ�����Ҳע�����彡�����ٺ٣��㶮�ģ�������...����

*/

/**
 * @Title: PicCutDemoActivity.java
 * @Package com.xiaoma.piccut.demo
 * @Description: ͼƬ�ü����ܲ���
 * @author XiaoMa
 */
public class PicCutDemoActivity extends Activity implements OnClickListener {

    private static final String TESSBASE_PATH = "/mnt/sdcard/tesseract/";
    private static final String DEFAULT_LANGUAGE = "eng";
    private static final String EXPECTED_FILE = TESSBASE_PATH + "tessdata/" + DEFAULT_LANGUAGE + ".traineddata";
	
	private ImageView ib = null;
	private ImageView iv1 = null;
	private ImageView iv2 = null;
	private ImageView iv3 = null;
	private ImageView iv4 = null;
	private Button btn = null;
	private String tp = null;
	private EditText edit_text = null;
	private TessBaseAPI baseApi = null;//new TessBaseAPI();
   

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);
		//��ʼ��
		init();
	}
	
	/**
	 * ��ʼ������ʵ��
	 */
	private void init() {
		ib = (ImageView) findViewById(R.id.imageButton1);
		iv1 = (ImageView) findViewById(R.id.imageView1);
		iv2 = (ImageView) findViewById(R.id.imageView2);
		iv3 = (ImageView) findViewById(R.id.imageView3);
		iv4 = (ImageView) findViewById(R.id.imageView4);
		btn = (Button) findViewById(R.id.button1);
		edit_text = (EditText)findViewById(R.id.edit_text);

		btn.setOnClickListener(this);
		
		baseApi = new TessBaseAPI();
		baseApi.init(TESSBASE_PATH, DEFAULT_LANGUAGE);
	}

	
	/**
	 * �ؼ�����¼�ʵ��
	 * 
	 * ��Ϊ�������ʲ�ͬ�ؼ��ı���ͼ�ü���ôʵ�֣�
	 * �Ҿ�������ط����������ؼ���ֻΪ���Լ���¼ѧϰ
	 * ��Ҿ���û�õĿ���������
	 */
	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.imageButton1:
			ShowPickDialog();
			break;
		case R.id.imageView1:
			ShowPickDialog();
			break;
		case R.id.button1:
			ShowPickDialog();
			break;

		default:
			break;
		}
	}

	/**
	 * ѡ����ʾ�Ի���
	 */
	private void ShowPickDialog() {
		new AlertDialog.Builder(this)
				.setTitle("����ͷ��...")
				.setNegativeButton("���", new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int which) {
						dialog.dismiss();
						/**
						 * �տ�ʼ�����Լ�Ҳ��֪��ACTION_PICK�Ǹ���ģ�����ֱ�ӿ�IntentԴ�룬
						 * ���Է�������ܶණ����Intent�Ǹ���ǿ��Ķ��������һ����ϸ�Ķ���
						 */
						Intent intent = new Intent(Intent.ACTION_PICK, null);
						
						/**
						 * ������仰����������ʽд��һ����Ч���������
						 * intent.setData(MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
						 * intent.setType(""image/*");������������
						 * ���������Ҫ�����ϴ�����������ͼƬ����ʱ����ֱ��д�磺"image/jpeg �� image/png�ȵ�����"
						 * ����ط�С���и����ʣ�ϣ�����ֽ���£������������URI������ΪʲôҪ��������ʽ��дѽ����ʲô����
						 */
						intent.setDataAndType(
								MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
								"image/*");
						startActivityForResult(intent, 1);

					}
				})
				.setPositiveButton("����", new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int whichButton) {
						dialog.dismiss();
						/**
						 * ������仹�������ӣ����ÿ������չ��ܣ�����Ϊʲô�п������գ���ҿ��Բο����¹ٷ�
						 * �ĵ���you_sdk_path/docs/guide/topics/media/camera.html
						 * �Ҹտ���ʱ����Ϊ̫�������濴����ʵ�Ǵ��ģ�����������õ�̫���ˣ����Դ�Ҳ�Ҫ��Ϊ
						 * �ٷ��ĵ�̫���˾Ͳ����ˣ���ʵ�Ǵ��ģ�����ط�С��Ҳ���ˣ��������
						 */
						Intent intent = new Intent(
								MediaStore.ACTION_IMAGE_CAPTURE);
						//�������ָ������������պ����Ƭ�洢��·��
						intent.putExtra(MediaStore.EXTRA_OUTPUT, Uri
								.fromFile(new File(Environment
										.getExternalStorageDirectory(),
										"xiaoma.jpg")));
						startActivityForResult(intent, 2);
					}
				}).show();
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		switch (requestCode) {
		// �����ֱ�Ӵ�����ȡ
		case 1:
			if(data != null)
				startPhotoZoom(data.getData());
			break;
		// ����ǵ����������ʱ
		case 2:
			File temp = new File(Environment.getExternalStorageDirectory()
					+ "/xiaoma.jpg");
			startPhotoZoom(Uri.fromFile(temp));
			break;
		// ȡ�òü����ͼƬ
		case 3:
			/**
			 * �ǿ��жϴ��һ��Ҫ��֤���������֤�Ļ���
			 * �ڼ���֮��������ֲ����⣬Ҫ���²ü�������
			 * ��ǰ����ʱ���ᱨNullException��С��ֻ
			 * ������ط����£���ҿ��Ը��ݲ�ͬ����ں��ʵ�
			 * �ط����жϴ����������
			 * 
			 */
			if(data != null){
				setPicToView(data);
				
			}
			break;
		default:
			break;

		}
		super.onActivityResult(requestCode, resultCode, data);
	}
	
	/**
	 * �ü�ͼƬ����ʵ��
	 * @param uri
	 */
	public void startPhotoZoom(Uri uri) {
		/*
		 * �����������Intent��ACTION����ô֪���ģ���ҿ��Կ����Լ�·���µ�������ҳ
		 * yourself_sdk_path/docs/reference/android/content/Intent.html
		 * ֱ��������Ctrl+F�ѣ�CROP ��֮ǰС��û��ϸ��������ʵ��׿ϵͳ���Ѿ����Դ�ͼƬ�ü�����,
		 * ��ֱ�ӵ����ؿ�ģ�С������C C++  ���������ϸ�˽�ȥ�ˣ������Ӿ������ӣ������о���������ô
		 * ��������...���
		 */
		Intent intent = new Intent("com.android.camera.action.CROP");
		intent.setDataAndType(uri, "image/*");
		//�������crop=true�������ڿ�����Intent��������ʾ��VIEW�ɲü�
		intent.putExtra("crop", "true");
		// aspectX aspectY �ǿ��ߵı���
//		intent.putExtra("aspectX", 1);
//		intent.putExtra("aspectY", 1);
//		// outputX outputY �ǲü�ͼƬ����
//		intent.putExtra("outputX", 150);
//		intent.putExtra("outputY", 150);
		intent.putExtra("return-data", true);
		startActivityForResult(intent, 3);
	}
	
	/**
	 * ����ü�֮���ͼƬ����
	 * @param picdata
	 */
	private void setPicToView(Intent picdata) {
		Bundle extras = picdata.getExtras();
		if (extras != null) {
			Bitmap photo = extras.getParcelable("data");
			Drawable drawable1 = new BitmapDrawable(photo);
			ib.setBackgroundDrawable(drawable1);
			
			
			
			
			/**
			 * ����ע�͵ķ����ǽ��ü�֮���ͼƬ��Base64Coder���ַ���ʽ��
			 * ������������QQͷ���ϴ����õķ������������
			 */
			
			/*ByteArrayOutputStream stream = new ByteArrayOutputStream();
			photo.compress(Bitmap.CompressFormat.JPEG, 60, stream);
			byte[] b = stream.toByteArray();
			// ��ͼƬ�����ַ�����ʽ�洢����
			
			tp = new String(Base64Coder.encodeLines(b));
			����ط���ҿ���д�¸��������ϴ�ͼƬ��ʵ�֣�ֱ�Ӱ�tpֱ���ϴ��Ϳ����ˣ�
			�����������ķ����Ƿ������Ǳߵ����ˣ����
			
			������ص��ķ����������ݻ�����Base64Coder����ʽ�Ļ������������·�ʽת��
			Ϊ���ǿ����õ�ͼƬ���;�OK��...���
			Bitmap dBitmap = BitmapFactory.decodeFile(tp);
			Drawable drawable = new BitmapDrawable(dBitmap);
			*/
			
			


			Log.e("", photo.getWidth()+":"+photo.getHeight()+"++++++++++++++++++++++++++");
			ImageFilter imageFilter = new ImageFilter(photo);
			
			imageFilter.addAvaGrey();//��Сֵ�Ҷ�
			Bitmap picture = Bitmap.createBitmap(imageFilter.getPixels(), photo.getWidth(), photo.getHeight(), Config.ARGB_8888 );
			Drawable drawable = new BitmapDrawable(picture);
			iv1.setBackgroundDrawable(drawable);
			
//			imageFilter.medianFilter(0,180);
//			picture = Bitmap.createBitmap(imageFilter.getPixels(), photo.getWidth(), photo.getHeight(), Config.ARGB_8888 );
//			drawable = new BitmapDrawable(picture);
//			iv2.setBackgroundDrawable(drawable);
//			
//			imageFilter.avaFilter(180,255);
//			picture = Bitmap.createBitmap(imageFilter.getPixels(), photo.getWidth(), photo.getHeight(), Config.ARGB_8888 );
//			drawable = new BitmapDrawable(picture);
//			iv3.setBackgroundDrawable(drawable);
			
			imageFilter.changeGrey(70);
			picture = Bitmap.createBitmap(imageFilter.getPixels(), photo.getWidth(), photo.getHeight(), Config.ARGB_8888 );
			drawable = new BitmapDrawable(picture);
			iv4.setBackgroundDrawable(drawable);

			
			
			


			
			decodeBitmap(picture);
			
		}
	}
	
	public void decodeBitmap(final Bitmap photo){
		final ProgressDialog progress = new ProgressDialog(this);
    	progress.setTitle("���Ժ�");
    	progress.setMessage( "���ڴ�....");
    	progress.setCancelable(true);
    	progress.setOnCancelListener(new OnCancelListener(){
			public void onCancel(DialogInterface dialog){
				baseApi.clear();
			}
		});
    	progress.show();
    	progress.setCanceledOnTouchOutside(false);
		
		new Handler().post(new Runnable(){
			public void run(){
				long st = System.currentTimeMillis();

				
				baseApi.setImage(photo);
				
				String text = baseApi.getUTF8Text();
				progress.cancel();
				edit_text.setText(text);
				System.err.println("��ʱ(ms):"+(System.currentTimeMillis()-st));
				
			}
		});

	}

}