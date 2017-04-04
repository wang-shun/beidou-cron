package com.baidu.beidou.indexgrade.olap.service.impl;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.util.List;

import org.slf4j.Logger;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import com.baidu.beidou.indexgrade.olap.request.OlapDataRequest;
import com.baidu.beidou.indexgrade.olap.service.OlapStatService;
import com.baidu.unbiz.common.logger.LoggerFactory;
import com.baidu.unbiz.olap.driver.bo.OlapRequest;
import com.baidu.unbiz.olap.exception.OlapException;
import com.baidu.unbiz.olap.service.AbstractOlapService;
import com.baidu.unbiz.olap.service.ResultHandler;

/**
 * 关键词索引分级中获取olap统计数据的service
 * 
 * @author wangxiongjie
 *
 */

@Service("indexGradeOlapStatServiceImpl")
public class IndexGradeOlapStatServiceImpl extends AbstractOlapService implements OlapStatService {

    private static final Logger LOG = LoggerFactory.getLogger(IndexGradeOlapStatServiceImpl.class);

    @Override
    public void fetchOlapData(final OlapDataRequest req) {

        OlapRequest<?> rr = OlapDataRequest.buildOlapRequest(req);
        int batchSize = 3000;
        try {
            ResultHandler<String> handler = new ResultHandler<String>() {
                final Writer out = new BufferedWriter(new FileWriter(req.getFilePath(), false));

                @Override
                public void process(List<String> itemList) throws OlapException {
                    if (!CollectionUtils.isEmpty(itemList)) {
                        try {
                            for (String line : itemList) {
                                out.write(line + "\n");
                            }
                        } catch (IOException e) {
                            LOG.error(req.toString(), e);
                        }
                    }
                }

                @Override
                public void cleanup() {
                    try {
                        out.flush();
                    } catch (IOException e1) {
                        LOG.error(req.toString(), e1);
                    }
                    try {
                        out.close();
                    } catch (IOException e) {
                        // do nothing
                    }
                }
            };

            // batch fetch data into file
            super.getBatchStorageData(rr, batchSize, handler);
        } catch (IOException e) {
            LOG.error(req.toString(), e);
        }
    }

}
