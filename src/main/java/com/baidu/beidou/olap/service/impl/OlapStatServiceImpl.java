package com.baidu.beidou.olap.service.impl;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.util.List;

import org.slf4j.Logger;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import com.baidu.beidou.olap.service.OlapStatService;
import com.baidu.beidou.olap.service.req.DataRequest;
import com.baidu.unbiz.common.logger.LoggerFactory;
import com.baidu.unbiz.olap.driver.bo.OlapRequest;
import com.baidu.unbiz.olap.exception.OlapException;
import com.baidu.unbiz.olap.service.AbstractOlapService;
import com.baidu.unbiz.olap.service.ResultHandler;

@Service
public class OlapStatServiceImpl extends AbstractOlapService implements OlapStatService {

    private static final Logger LOG = LoggerFactory.getLogger(OlapStatServiceImpl.class);

    @Override
    public void fetchOlapData(final DataRequest req, final String filePath) {
        
        OlapRequest<?> rr = DataRequest.buildOlapRequest(req);
        int batchSize = 100;
        try {
            ResultHandler<String> handler = new ResultHandler<String>() {
                final Writer out = new BufferedWriter(new FileWriter(filePath, false));

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
