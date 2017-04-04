/**
 * 2009-4-27 下午10:59:24
 */
package com.baidu.beidou.unionsite.service.impl;

import com.baidu.beidou.unionsite.service.SiteScaleAlgorithm;

/**
 * @author zengyunfeng
 * @version 1.0.7
 */
public class SiteScaleAlgorithmImpl implements SiteScaleAlgorithm {

	private double n11 = 0.333;
	private double n12 = -0.014;
	private double n13 = -0.012;
	private double n14 = 0.341;
	private double n15 = 0.342;
	private double n21 = -0.005;
	private double n22 = 0.684;
	private double n23 = -0.682;
	private double n24 = 0.001;
	private double n25 = 0.003;
	private double w1 = 0.5802;
	private double w2 = 0.2134;

	/**
	 * 计算F值
	 * 
	 * @author zengyunfeng
	 */
	public double calculateF(double z1, double z2, double z3, double z4,
			double z5) {
		// F=∑Wk*fK=w1*f1+ w2*f2= w1*（z1*n11+ z2*n12+ z3*n13+ z4*n14+ z5*n15）+
		// w2*（z1*n21+ z2*n22+ z3*n23+ z4*n24+ z5*n25）
		double F = w1 * (z1 * n11 + z2 * n12 + z3 * n13 + z4 * n14 + z5 * n15)
				+ w2 * (z1 * n21 + z2 * n22 + z3 * n23 + z4 * n24 + z5 * n25);
		return F;
	}

	/**
	 * @return the n11
	 */
	public double getN11() {
		return n11;
	}

	/**
	 * @param n11
	 *            the n11 to set
	 */
	public void setN11(double n11) {
		this.n11 = n11;
	}

	/**
	 * @return the n12
	 */
	public double getN12() {
		return n12;
	}

	/**
	 * @param n12
	 *            the n12 to set
	 */
	public void setN12(double n12) {
		this.n12 = n12;
	}

	/**
	 * @return the n13
	 */
	public double getN13() {
		return n13;
	}

	/**
	 * @param n13
	 *            the n13 to set
	 */
	public void setN13(double n13) {
		this.n13 = n13;
	}

	/**
	 * @return the n14
	 */
	public double getN14() {
		return n14;
	}

	/**
	 * @param n14
	 *            the n14 to set
	 */
	public void setN14(double n14) {
		this.n14 = n14;
	}

	/**
	 * @return the n15
	 */
	public double getN15() {
		return n15;
	}

	/**
	 * @param n15
	 *            the n15 to set
	 */
	public void setN15(double n15) {
		this.n15 = n15;
	}

	/**
	 * @return the n21
	 */
	public double getN21() {
		return n21;
	}

	/**
	 * @param n21
	 *            the n21 to set
	 */
	public void setN21(double n21) {
		this.n21 = n21;
	}

	/**
	 * @return the n22
	 */
	public double getN22() {
		return n22;
	}

	/**
	 * @param n22
	 *            the n22 to set
	 */
	public void setN22(double n22) {
		this.n22 = n22;
	}

	/**
	 * @return the n23
	 */
	public double getN23() {
		return n23;
	}

	/**
	 * @param n23
	 *            the n23 to set
	 */
	public void setN23(double n23) {
		this.n23 = n23;
	}

	/**
	 * @return the n24
	 */
	public double getN24() {
		return n24;
	}

	/**
	 * @param n24
	 *            the n24 to set
	 */
	public void setN24(double n24) {
		this.n24 = n24;
	}

	/**
	 * @return the n25
	 */
	public double getN25() {
		return n25;
	}

	/**
	 * @param n25
	 *            the n25 to set
	 */
	public void setN25(double n25) {
		this.n25 = n25;
	}

	/**
	 * @return the w1
	 */
	public double getW1() {
		return w1;
	}

	/**
	 * @param w1
	 *            the w1 to set
	 */
	public void setW1(double w1) {
		this.w1 = w1;
	}

	/**
	 * @return the w2
	 */
	public double getW2() {
		return w2;
	}

	/**
	 * @param w2
	 *            the w2 to set
	 */
	public void setW2(double w2) {
		this.w2 = w2;
	}

}
