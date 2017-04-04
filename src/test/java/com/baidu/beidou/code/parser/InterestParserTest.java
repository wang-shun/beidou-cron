/*
 * Copyright (C) 2016 Baidu, Inc. All Rights Reserved.
 */
package com.baidu.beidou.code.parser;

import com.baidu.beidou.code.bo.Interest;

import junit.framework.Assert;
import junit.framework.TestCase;

/**
 * Created by hewei18 on 2016-03-29.
 */
public class InterestParserTest extends TestCase {

    String source = "968\t343\t兴趣属性/钟表配饰和箱包/眼镜\t眼镜\t上线\t云峰\tA720\t\t\t\t1\n"
            + "970\t357\t兴趣属性/音乐/语言/韩语\t韩语\t上线\t云峰\tA720\t\t\t\t1\n"
            + "973\t272\t兴趣属性/食品餐饮/电商食品/饮料乳品\t饮料乳品\t上线\t云峰\tA720\t\t\t\t1\n"
            + "974\t804\t兴趣属性/影视/电影/类型/恐怖\t恐怖\t上线\t云峰\tA720\t\t\t\t1\n"
            + "976\t698\t兴趣属性/个护美妆/产品类别/男士专区\t男士专区\t上线\t云峰\tA720\t\t\t\t1\n"
            + "977\t522\t兴趣属性/旅游出行/第三方平台\t第三方平台\t上线\t云峰\tA720\t\t\t\t0\n"
            + "978\t609\t兴趣属性/汽车/关注点\t关注点\t上线\t云峰\tA720\t\t\t\t0\n"
            + "980\t804\t兴趣属性/影视/电影/类型/犯罪\t犯罪\t上线\t云峰\tA720\t\t\t\t1\n"
            + "984\t210\t兴趣属性/人生特殊时期/居住\t居住\t上线\t云峰\tA720\t\t\t\t0\n"
            + "985\t83\t兴趣属性/金融理财/借贷/担保\t担保\t上线\t云峰\tA720\t\t\t\t1\n"
            + "987\t973\t兴趣属性/食品餐饮/电商食品/饮料乳品/乳品\t乳品\t上线\t云峰\tA720\t\t\t\t1\n"
            + "988\t978\t兴趣属性/汽车/关注点/购车流程\t购车流程\t上线\t云峰\tA720\t\t\t\t1\n"
            + "989\t91\t兴趣属性/教育/留学\t留学\t上线\t云峰\tA720\t\t\t\t1\n"
            + "990\t812\t兴趣属性/母婴/母婴用品/营养辅食\t营养辅食\t上线\t云峰\tA720\t\t\t\t1\n"
            + "993\t659\t兴趣属性/影视/娱乐综艺/类型/脱口秀\t脱口秀\t上线\t云峰\tA720\t\t\t\t1\n"
            + "994\t297\t兴趣属性/汽车/汽车价格/8-12万\t8-12万\t上线\t云峰\tA720\t\t\t\t1\n"
            + "995\t144\t兴趣属性/教育/商务及留学英语/雅思\t雅思\t上线\t云峰\tA720\t\t\t\t1\n"
            + "996\t144\t兴趣属性/教育/商务及留学英语/托福\t托福\t上线\t云峰\tA720\t\t\t\t1\n"
            + "998\t149\t兴趣属性/游戏/题材/仙侠\t仙侠\t上线\t云峰\tA720\t\t\t\t1";

    public void testParse() throws Exception {
        Interest interest = InterestParser.parse("id\tparent_id\t全路径\t当前路径\t状态\t负责人\t应用方\t映射源\tP/S\t排序id\t命名空间层级");
        Assert.assertNull(interest);
        interest = InterestParser.parse("0\t-1\t分类体系\t分类体系\t上线\t伟明\tIT\t\t\t1\t0");
        Assert.assertNotNull(interest);
        Assert.assertEquals(new Interest(0, "分类体系", -1, 1, 0), interest);
        interest = InterestParser.parse("999\t343\t兴趣属性/钟表配饰和箱包/珠宝首饰\t珠宝首饰\t上线\t云峰\tA720\t\t\t\t1");
        Assert.assertEquals(new Interest(999, "珠宝首饰", 343, 0, 1), interest);
        String[] itStrArr = source.split("\\n");
        for (String itStr : itStrArr) {
            interest = InterestParser.parse(itStr);
            Assert.assertNotNull(interest);
            System.out.println(interest);
        }
    }
}