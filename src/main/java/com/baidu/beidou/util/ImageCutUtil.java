/**
 * beidou-cron-640#com.baidu.beidou.util.ImageCutUtil.java
 * 上午12:59:13 created by kanghongwei
 */
package com.baidu.beidou.util;

import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Iterator;

import javax.imageio.ImageIO;
import javax.imageio.ImageReadParam;
import javax.imageio.ImageReader;
import javax.imageio.stream.ImageInputStream;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * 
 * @author kanghongwei
 * @fileName ImageCutUtil.java
 * @dateTime 2013-10-30 上午12:59:13
 */
public class ImageCutUtil {

	private static final Log log = LogFactory.getLog(ImageCutUtil.class);

	/**
	 * 对原始图片字节流进行裁剪,返回裁剪后的字节流
	 * 
	 * @param formatName  name of a format e.g "jpeg" or "tiff" or "png".
	 * @param srcBytes
	 * @param xPoint 裁剪x轴起点
	 * @param yPoint 裁剪y轴起点
	 * @param width
	 * @param height
	 * @return
	 * @throws IOException
	 */
	public static byte[] cutPicture(String formatName, byte[] srcBytes, int xPoint, int yPoint, int width, int height) throws IOException {

		byte[] resultBytes = null;

		ImageInputStream imageInputStream = null;
		try {
			Iterator<ImageReader> iterator = ImageIO.getImageReadersByFormatName(formatName);
			ImageReader imageReader = iterator.next();

			imageInputStream = ImageIO.createImageInputStream(new ByteArrayInputStream(srcBytes));
			imageReader.setInput(imageInputStream, true);
			ImageReadParam param = imageReader.getDefaultReadParam();

			Rectangle rectangle = new Rectangle(xPoint, yPoint, width, height);
			param.setSourceRegion(rectangle);

			BufferedImage bufferedImage = imageReader.read(0, param);
			ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
			ImageIO.write(bufferedImage, formatName, outputStream);
			resultBytes = outputStream.toByteArray();

		} catch (Exception e) {
			log.error("error occur when cutPicture " + e.getMessage());
			return null;
		} finally {
			if (imageInputStream != null) {
				imageInputStream.close();
			}
		}
		return resultBytes;
	}

}
