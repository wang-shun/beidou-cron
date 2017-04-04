
package com.baidu.beidou.accountmove.ubmc.util;

import org.springframework.beans.factory.FactoryBean;
import org.springframework.beans.factory.InitializingBean;


/**
 * ClassName:McpackProxyFactoryBean
 * Function: 使用Spring的FactoryBean来配置生成调用服务的代理，此处为了兼容原来的功能就用了两级代理的方式
 *
 * @author   <a href="mailto:liangshimu@baidu.com">梁时木</a>
 * @created  2010-7-30
 * @version  $Id: Exp $
 * @see org.springframework.beans.factory.FactoryBean;
 * @see org.springframework.beans.factory.InitializingBean;
 */
public class McpackProxyFactoryBean extends CommonMcpackProxyFactory implements FactoryBean, InitializingBean {

}

