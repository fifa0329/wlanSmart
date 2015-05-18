package com.enice.wlan.selector.ui;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.InputStreamEntity;
import org.apache.http.impl.client.DefaultHttpClient;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Handler;
import android.os.Message;
import android.widget.Toast;

public class Downloader {
	public static final int CURRENT_VERSION = 10;
	public static final String VERSION_URL = "http://218.94.107.4:8000/central/version";
	public static final String APK_URL = "http://218.94.107.4:8000/central/WLANSelector.apk";
	public static final String DATABASE_URL = "http://218.94.107.4:8000/central/WLAN.zip";
	public static final String UPLOAD_LOG_URL = "http://218.94.107.4:7000/WLANServer/LogCollector";
	public static final String DB_DIR = "/sdcard/WLANSelector/";
	public static final String ZIP_FILE = "WLAN.zip";
	public static final String APK_FILE = "WLANSelector.apk";
	public static final String INDEX_FILE = "index.dat";
	
	private static final int MESSAGE_FAILED		= 0;
	private static final int MESSAGE_FINISH		= 1;
	private static final int MESSAGE_LENGTH		= 2;
	private static final int MESSAGE_PROGRESS	= 3;
	private static final int MESSAGE_UNZIP		= 4;
	private static final int MESSAGE_UPDATE		= 5;
	private static final int MESSAGE_NO_UPDATE	= 6;
	
	private Context context;
	private ProgressDialog progress;
	private Handler progressHandler;
	
	public Downloader(Context context) {
		this.context = context;
		this.progressHandler = new ProgressHandler();
	}

	public void checkNewVersion() {
		progress = new ProgressDialog(context);
		progress.setTitle("检查更新");
		progress.setMessage("正在检查，请等待...");
		progress.setProgressStyle(ProgressDialog.STYLE_SPINNER);
		progress.show();
		
		new Thread(new Runnable() {
			@Override
			public void run() {
		        try {
		    		HttpClient client = new DefaultHttpClient();
		            HttpGet get = new HttpGet(VERSION_URL);
		            HttpResponse response = client.execute(get);
		            int statusCode = response.getStatusLine().getStatusCode();
		            if (statusCode == 200) {
			            HttpEntity entity = response.getEntity();
			            InputStream is = entity.getContent();
			            
			            byte[] buf = new byte[1024];
			            StringBuffer textBuffer = new StringBuffer();
			            while ((is.read(buf)) > 0) {
			            	textBuffer.append(new String(buf));
			            }
			            int latest_version = Integer.parseInt(textBuffer.toString().trim());
			            if (latest_version > CURRENT_VERSION) {
			            	sendMesssage(MESSAGE_UPDATE, 0);
			            } else {
			            	sendMesssage(MESSAGE_NO_UPDATE, 0);
			            }
		            } else {
		            	sendMesssage(MESSAGE_FAILED, 0);
		            }
		        } catch (Exception e) {
		            e.printStackTrace();
		            sendMesssage(MESSAGE_FAILED, 0);
		        }
			}
		}).start();
	}
	
	public void downloadNewApk() {
		startDownloadProcess("更新程序", APK_URL, DB_DIR + APK_FILE, true);
	}
	
	public void installNewApk() {
		Intent intent = new Intent(Intent.ACTION_VIEW);
		intent.setDataAndType(Uri.fromFile(new File(DB_DIR + APK_FILE)), "application/vnd.android.package-archive");
		context.startActivity(intent);
	}
	
	public boolean checkDatabase() {
        File dir = new File(DB_DIR);
        if (!dir.exists()) {
        	dir.mkdir();
        }
        
		return new File(DB_DIR + INDEX_FILE).exists();
	}
	
	private void extractFile(ZipFile zipFile, String name) throws IOException {
		ZipEntry entry = zipFile.getEntry(name);
		if (entry == null) {
			return;
		}
		
        InputStream istream = zipFile.getInputStream(entry);        
        FileOutputStream oStream = new FileOutputStream(new File(DB_DIR + name));
        int ch = -1;
        byte[] buf = new byte[1024];
        while ((ch = istream.read(buf)) > 0) {
        	oStream.write(buf, 0, ch);
        }
        oStream.flush();
        oStream.close();
	}
	
	private void unzipDatabase() throws IOException {
        ZipFile zipFile = new ZipFile(DB_DIR + ZIP_FILE);
        extractFile(zipFile, INDEX_FILE);
        extractFile(zipFile, "0.dat");
        extractFile(zipFile, "1.dat");
        extractFile(zipFile, "2.dat");
        extractFile(zipFile, "1.db");
        extractFile(zipFile, "2.db");
        if (!new File(DB_DIR + "USER.db").exists()) {
        	extractFile(zipFile, "USER.db");
        }
        zipFile.close();
        new File(DB_DIR + ZIP_FILE).delete();
	}
	
	private void startDownloadProcess(String title, final String url, final String fileName, final boolean isApk) {
		progress = new ProgressDialog(context);
		progress.setTitle(title);
		progress.setMessage("正在下载，请等待...");
		progress.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
		progress.show();
		
		new Thread(new Runnable() {
			@Override
			public void run() {
		        try {
		    		HttpClient client = new DefaultHttpClient();
		            HttpGet get = new HttpGet(url);
		            HttpResponse response = client.execute(get);
		            int statusCode = response.getStatusLine().getStatusCode();
		            if (statusCode == 200) {
			            HttpEntity entity = response.getEntity();
			            int length = (int)entity.getContentLength();
			            InputStream istream = entity.getContent();
			            FileOutputStream oStream = new FileOutputStream(fileName); 
			            
			            sendMesssage(MESSAGE_LENGTH, length);
			            int ch = -1;
			            int count = 0;
			            byte[] buf = new byte[1024];
			            while ((ch = istream.read(buf)) > 0) {
			            	oStream.write(buf, 0, ch);
			            	count += ch;
			            	sendMesssage(MESSAGE_PROGRESS, count);
			            }
			            oStream.flush();
			            oStream.close();
			            if (isApk) {
			            	installNewApk();
			            } else {
				            sendMesssage(MESSAGE_UNZIP, 0);
				            unzipDatabase();
			            }
			            sendMesssage(MESSAGE_FINISH, 0);
		            } else {
		            	sendMesssage(MESSAGE_FAILED, 0);
		            }
		        } catch (Exception e) {
		            e.printStackTrace();
		            sendMesssage(MESSAGE_FAILED, 0);
		        }
			}
		}).start();
	}
	
	public void downloadDatabase() {
		startDownloadProcess("下载数据", DATABASE_URL, DB_DIR + ZIP_FILE, false);
	}
	
    public void sendMesssage(int what, int value) {
        Message message = new Message();
        message.what = what;
        message.arg1 = value;
        progressHandler.sendMessage(message);
    }
	
	class ProgressHandler extends Handler {
		@Override
        public void handleMessage(Message msg) {
        	switch (msg.what) {
        	case MESSAGE_FAILED:
        		Toast.makeText(context, "网络访问失败", Toast.LENGTH_LONG).show();
        		progress.dismiss();
        		break;
        	case MESSAGE_FINISH:
        		Toast.makeText(context, "已完成", Toast.LENGTH_LONG).show();
        		progress.dismiss();
        		break;
        	case MESSAGE_LENGTH:
        		progress.setMax(msg.arg1);
        		break;
        	case MESSAGE_PROGRESS:
        		progress.setProgress(msg.arg1);
        		break;
        	case MESSAGE_UNZIP:
        		progress.setMessage("正在解压，请等待...");
        		break;
        	case MESSAGE_UPDATE:
        		progress.dismiss();
    			Dialog dialog = new AlertDialog.Builder(context)
					.setTitle("软件更新")
					.setMessage("发现新版本，是否更新？")
					.setPositiveButton("更新", new DialogInterface.OnClickListener() {
						@Override
						public void onClick(DialogInterface dialog, int which) {
							dialog.dismiss();
							downloadNewApk();
						}})
					.setNegativeButton("取消", new DialogInterface.OnClickListener() {
						@Override
						public void onClick(DialogInterface dialog, int which) {
							dialog.dismiss();
						}})
					.create();
				dialog.show();
        		break;
        	case MESSAGE_NO_UPDATE:
        		Toast.makeText(context, "已经是最新版本", Toast.LENGTH_LONG).show();
        		progress.dismiss();
        		break;
        	}
		}
	}
	
	public void uploadUserData() {
		new Thread(new Runnable() {
			@Override
			public void run() {
				try {
					HttpClient client = new DefaultHttpClient();
					HttpPost post = new HttpPost(UPLOAD_LOG_URL);
					//InputStreamEntity reqEntity = new InputStreamEntity(mLogcatProc.getInputStream(), -1);
					//reqEntity.setContentType("binary/octet-stream");
					//reqEntity.setChunked(true);
					//post.setEntity(reqEntity);
					HttpResponse response = client.execute(post);
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}).start();
	}

	public void uploadLog() {
		progress = new ProgressDialog(context);
		progress.setTitle("上传日志");
		progress.setMessage("正在上传，请等待...");
		progress.setProgressStyle(ProgressDialog.STYLE_SPINNER);
		progress.show();
		
		new Thread(new Runnable() {
			@Override
			public void run() {
				try {
					Process mLogcatProc = Runtime.getRuntime().exec(new String[]{"logcat", "-v", "time", "-d", "AndroidRuntime:E WLANSelector:V *:S"});
					HttpClient client = new DefaultHttpClient();
					HttpPost post = new HttpPost(UPLOAD_LOG_URL);
					InputStreamEntity reqEntity = new InputStreamEntity(mLogcatProc.getInputStream(), -1);
					reqEntity.setContentType("binary/octet-stream");
					reqEntity.setChunked(true);
					post.setEntity(reqEntity);
					HttpResponse response = client.execute(post);
					if (response.getStatusLine().getStatusCode() == 200) {
						sendMesssage(MESSAGE_FINISH, 0);
					} else {
						sendMesssage(MESSAGE_FAILED, 0);
					}
				} catch (IOException e) {
					e.printStackTrace();
					sendMesssage(MESSAGE_FAILED, 0);
				}
			}
		}).start();
	}
}
