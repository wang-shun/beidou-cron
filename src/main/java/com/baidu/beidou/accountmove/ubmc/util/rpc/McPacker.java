package com.baidu.beidou.accountmove.ubmc.util.rpc;

import java.lang.reflect.Type;

import com.baidu.gson.Gson;
import com.baidu.gson.GsonBuilder;
import com.baidu.gson.JsonElement;
import com.baidu.mcpack.Mcpack;
import com.baidu.mcpack.McpackException;

public class McPacker {
	private static Gson gson = new GsonBuilder().serializeNulls().disableHtmlEscaping().serializeSpecialFloatingPointValues().create();
	private static Mcpack mcpack = new Mcpack();
	private static final String encoding = "GBK"; 
	
	/**
	 * 将对象打包成字节流
	 * @param obj
	 * @return
	 * @throws McpackException
	 * liuzeyin
	 * 2009-12-4
	 */
	public static byte[] pack(Object obj) throws McpackException {
		JsonElement json = gson.toJsonTree(obj);
		return mcpack.toMcpack(encoding, json);
	}
	public static byte[] pack(Object obj, Type type) throws McpackException {
		JsonElement json = gson.toJsonTree(obj, type);
		return mcpack.toMcpack(encoding, json);
	}
	/**
	 * 将字节流解包成对象
	 * @param <T>
	 * @param buffer
	 * @param clazz
	 * @return
	 * @throws McpackException
	 * liuzeyin
	 * 2009-12-4
	 */
	public static <T> T unpack(byte[] buffer, Class<T> clazz) throws McpackException {
		JsonElement json = mcpack.toJsonElement(encoding, buffer);
		return gson.fromJson(json, clazz);
	}

}
