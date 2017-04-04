package com.baidu.beidou.util;

import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

import javax.imageio.ImageIO;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.sun.image.codec.jpeg.ImageFormatException;
import com.sun.image.codec.jpeg.JPEGCodec;
import com.sun.image.codec.jpeg.JPEGEncodeParam;
import com.sun.image.codec.jpeg.JPEGImageEncoder;

/**
 * 
 * @author kanghongwei
 * @fileName ImageCompressUtil.java
 * @dateTime 2013-10-30 上午1:03:09
 */
@SuppressWarnings("restriction")
public class ImageCompressUtil {

	private static final Log log = LogFactory.getLog(ImageCompressUtil.class);

	/**
	 * 根据提供的图片进行压缩，如果图片无法识别或者压缩失败，直接返回null
	 * 图片格式要求：推荐jgp
	 * 
	 * 
	 * @param imgSrc
	 * @param quality
	 * @return
	 */
	public static byte[] compressImage(byte[] imgSrc, float quality) {

		if (imgSrc == null || imgSrc.length == 0) {
			return imgSrc;
		}
		InputStream in = new ByteArrayInputStream(imgSrc);
		Image img = null;
		try {
			img = ImageIO.read(in);
		} catch (IOException e) {
			log.error("error occur when generate image from array");
		}
		if (img == null) {
			return null;
		}

		int width = img.getWidth(null);
		int height = img.getHeight(null);
		if (width == -1 || height == -1) {
			log.error("can not recognize the image. so can not compress");
			return null;
		}

		BufferedImage buffImg = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);

		Graphics2D graphics = buffImg.createGraphics();
		graphics.drawImage(img, 0, 0, null);

		ByteArrayOutputStream out = new ByteArrayOutputStream();
		JPEGEncodeParam param = JPEGCodec.getDefaultJPEGEncodeParam(buffImg);
		param.setQuality(quality, false);
		JPEGImageEncoder encoder = JPEGCodec.createJPEGEncoder(out, param);
		byte[] result = null;
		try {
			encoder.encode(buffImg);

			result = out.toByteArray();
		} catch (ImageFormatException e) {
			log.error("wrong image format ,can not compress");
		} catch (IOException e) {
			log.error("io exception occured when compress the image");
		} finally {
			if (in != null) {
				try {
					in.close();
					in = null;
				} catch (IOException e) {
					log.error("fail to close inputstream");
					in = null;
				}
			}
			if (out != null) {
				try {
					out.close();
				} catch (IOException e) {
					log.error("fail to close outputstream");
					out = null;
				}
			}
		}
		return result;
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {

		ImageCompressUtil imageCompressUtil = new ImageCompressUtil();

		byte[] imgsrc = imageCompressUtil.getBytesFromFile("E:\\workspaces\\screenShot4_success_360x300.png");
		log.info("img src size:" + imgsrc.length);

		byte[] imgres = compressImage(imgsrc, 0.8f);
		log.info("img res size:" + imgres.length);

		imageCompressUtil.writeToFile(imgres, "E:\\workspaces\\screenShot4_success_360x300_2.png");
	}

	private void writeToFile(byte[] imgres, String filePath) {
		FileOutputStream fos = null;
		try {
			fos = new FileOutputStream(filePath);
			fos.write(imgres);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			if (fos != null) {
				try {
					fos.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
				fos = null;
			}
		}
	}

	private byte[] getBytesFromFile(String filePath) {
		byte[] fileByte = null;
		File file = new File(filePath);
		InputStream in = null;
		try {
			in = new FileInputStream(file);
			long filesize = file.length();
			fileByte = new byte[(int) filesize];
			int offset = 0;
			int readLength = 0;
			while (offset < fileByte.length && (readLength = in.read(fileByte, offset, fileByte.length - offset)) >= 0) {
				offset += readLength;
			}
			if (offset != fileByte.length) {
				System.out.println("read image wrong!!!!!!");
			}

		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			if (in != null) {
				try {
					in.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
				in = null;
			}
		}
		return fileByte;
	}

}
