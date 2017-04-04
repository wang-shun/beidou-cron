package com.baidu.beidou.shrink.service;

import com.baidu.beidou.shrink.vo.ShrinkUnit;
/**
 * 执行数据的转移服务，具体实现从一个表中转移数据到另一个表
 * @author hexiufeng
 *
 */
public interface ShrinkApplyService {
	public class ApplyResult{
		/**
		 * 实际影响table的row count
		 */
		private int affectRealRows;
		/**
		 * 虚拟影响的数据，用于cprogroupinfo
		 */
		private int affectVirtualRows;
		public ApplyResult(){
			
		}
		public ApplyResult(int real,int virtual){
			this.affectRealRows = real;
			this.affectVirtualRows=virtual;
		}
		public int getAffectRealRows() {
			return affectRealRows;
		}
		public void setAffectRealRows(int affectRealRows) {
			this.affectRealRows = affectRealRows;
		}
		public int getAffectVirtualRows() {
			return affectVirtualRows;
		}
		public void setAffectVirtualRows(int affectVirtualRows) {
			this.affectVirtualRows = affectVirtualRows;
		}
	}
	/**
	 * 转移
	 * @param unit
	 * @return
	 */
	ApplyResult apply(ShrinkUnit unit);
	/**
	 * 执行服务的表名称，对于分片表来说，指的是表名前缀
	 * @return
	 */
	String getName();
}
