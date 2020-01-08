package com.source;

import java.awt.Image;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.InputStream;

import javax.imageio.ImageIO;

import org.bytedeco.javacv.FFmpegFrameGrabber;
import org.bytedeco.javacv.FFmpegFrameRecorder;
import org.bytedeco.javacv.Frame;
import org.bytedeco.javacv.Java2DFrameConverter;

/**
 * JavaCv视频媒体流处理工具类
 * @author wubf
 *
 */
public class VideoUtil {
	
	public static boolean isStart = false;

	/**
	 * 截取视频帧，保存为jpg图片到本地
	 * @param file  文件对象
	 * @param framefile 保存文件路径
	 * @param second
	 * @throws Exception
	 */
	public static void fetchPic(File file, String framefile) throws Exception{
        FFmpegFrameGrabber ff = new FFmpegFrameGrabber(file);
        ff.start();
        int lenght = ff.getLengthInAudioFrames();
        System.out.println(ff.getFrameRate());//输出视频帧数以及相关信息

        int i = 0;
        Frame frame = null;
        int second = 0;
        while (i < lenght) {
            // 过滤前5帧，避免出现全黑的图片，依自己情况而定
            frame = ff.grabImage();
//            if (i>=(int) (ff.getFrameRate()*second)&&frame.image != null) {
//            System.out.print(i+",");
//            if(i==50){
//                int a =1;
//            }
            if(frame!=null&&frame.image!=null) {
//                System.out.println(i);
                writeToFile(frame, i);
            }
//                second++;
//            }
            i++;
        }
        ff.stop();
    }
	
	/**
	 * 根据输入流信息解码H264编码信息的视频流
	 * 截取每帧数据图片保存到指定位置
	 * @param io
	 * @param filePath
	 */
	public static void fetchPic(InputStream io,String filePath) {
        try {
        	FFmpegFrameGrabber frameGrabber = new FFmpegFrameGrabber(io);
    		frameGrabber.setFrameRate(1);//设置视频帧数
            frameGrabber.setFormat("h264");//编码格式
            frameGrabber.setVideoBitrate(15);//
            frameGrabber.setVideoOption("preset", "ultrafast");
            frameGrabber.setNumBuffers(25000000);
        	
			frameGrabber.start();
//			Java2DFrameConverter converter = new Java2DFrameConverter();  
//			BufferedImage bufferedImage = converter.convert(frame);
			
			Frame frame = frameGrabber.grab();
			int count = 0;
			while (frame != null) {
				writeToFile(frame, count++);
            }
			
		} catch (Exception e) {
			e.printStackTrace();
		}
		
	}
	
	/**
	 * 根据视频帧保存为图片
	 * @param frame
	 * @param second
	 */
	public static void writeToFile(Frame frame,int second){
        File targetFile = new File("E:/video/framePic/"+second+"_"+System.currentTimeMillis()+".jpg");
        String imgSuffix = "jpg";

        Java2DFrameConverter converter =new Java2DFrameConverter();
        BufferedImage srcBi =converter.getBufferedImage(frame);
        int owidth = srcBi.getWidth();
        int oheight = srcBi.getHeight();
        // 对截取的帧进行等比例缩放
        /*int width = 800;
        int height = (int) (((double) width / owidth) * oheight);
        BufferedImage bi = new BufferedImage(width, height, BufferedImage.TYPE_3BYTE_BGR);
        bi.getGraphics().drawImage(srcBi.getScaledInstance(width, height, Image.SCALE_SMOOTH),0, 0, null);*/
        try {
            ImageIO.write(srcBi, imgSuffix, targetFile);
        }catch (Exception e) {
            e.printStackTrace();
        }
    }
	
	/**
	 * 根据RTSP串流或本地视频，截取视频每帧图片
	 * @param inputFile
	 * @throws org.bytedeco.javacv.FrameGrabber.Exception
	 */
	public static void fetchPic(String inputFile) throws org.bytedeco.javacv.FrameGrabber.Exception {
		// 获取视频源
        FFmpegFrameGrabber grabber = new FFmpegFrameGrabber(inputFile);
        grabber.setOption("rtsp_transport","tcp");
        grabber.setFrameRate(30);
        grabber.setVideoBitrate(3000000);
		
        grabber.start();
        System.out.println(grabber.getLengthInAudioFrames());
        int lenght = grabber.getLengthInAudioFrames();
        int i = 0;
        Frame frame  = null;
        while (i < lenght) {
        	frame = grabber.grabImage();
        	if(frame!=null&&frame.image!=null) {
                System.out.println(i);
                writeToFile(frame, i);
            }
        	i++;
        }
        writeToFile(frame, 123);
        
	}

	
	/**
     * 按帧录制视频
     * 
     * @param inputFile-该地址可以是网络直播/录播地址，也可以是远程/本地文件路径
     * @param outputFile
     *            -该地址只能是文件地址，如果使用该方法推送流媒体服务器会报错，原因是没有设置编码格式
     * @throws FrameGrabber.Exception
     * @throws FrameRecorder.Exception
     * @throws org.bytedeco.javacv.FrameRecorder.Exception
     */
	public static void frameRecord(String inputFile, String outputFile, int audioChannel) throws Exception, org.bytedeco.javacv.FrameRecorder.Exception {

        isStart = true;// 该变量建议设置为全局控制变量，用于控制录制结束
        // 获取视频源
        FFmpegFrameGrabber grabber = new FFmpegFrameGrabber(inputFile);
//        grabber.setOption("rtsp_transport","tcp");
//        grabber.setFrameRate(30);
//        grabber.setVideoBitrate(3000000);
//        grabber.setNumBuffers(250000);
        // 流媒体输出地址，分辨率（长，高），是否录制音频（0:不录制/1:录制）
        FFmpegFrameRecorder recorder = new FFmpegFrameRecorder(outputFile, 1280, 720,audioChannel);
//        recorder.setFrameRate(30);
//        recorder.setVideoBitrate(3000000);
        recordByFrame(grabber, recorder, isStart);
    }
    
    
    private static void recordByFrame(FFmpegFrameGrabber grabber, FFmpegFrameRecorder recorder, Boolean status)
            throws Exception, org.bytedeco.javacv.FrameRecorder.Exception {
        try {//建议在线程中使用该方法
            grabber.start();
            recorder.start();
            Frame frame = null;
            while (status&& (frame = grabber.grabFrame()) != null) {
                recorder.record(frame);
            }
            recorder.stop();
            grabber.stop();
        } finally {
            if (grabber != null) {
                grabber.stop();
            }
            isStart = false;
        }
    }
	

	
}
