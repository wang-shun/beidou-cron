package com.baidu.beidou.unionsite.service.impl;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.baidu.beidou.unionsite.bo.IntegerEntry;
import com.baidu.beidou.unionsite.bo.RegionInfo;
import com.baidu.beidou.unionsite.bo.WMSiteBo;
import com.baidu.beidou.unionsite.bo.WMSiteIndexBo;
import com.baidu.beidou.unionsite.constant.SiteConstant;
import com.baidu.beidou.unionsite.dao.BDSiteStatDao;
import com.baidu.beidou.unionsite.dao.BDSiteStatOnAddbDao;
import com.baidu.beidou.unionsite.dao.WM123SiteIndexDao;
import com.baidu.beidou.unionsite.dao.WM123SiteStatDao;
import com.baidu.beidou.unionsite.dao.WM123SiteStatOnCapDao;
import com.baidu.beidou.unionsite.service.WMSiteService;
import com.baidu.beidou.unionsite.vo.SiteElement;
import com.baidu.beidou.unionsite.vo.SiteEntity;
import com.baidu.beidou.unionsite.vo.SiteHeatVo;
import com.baidu.beidou.unionsite.vo.SiteTradeVo;
import com.baidu.beidou.unionsite.vo.TradeSiteElement;
import com.baidu.beidou.unionsite.vo.UserSiteVO;
import com.baidu.beidou.unionsite.vo.WMSiteIndexVo;
import com.baidu.beidou.unionsite.vo.WMSiteVisitorIndexVo;
import com.baidu.beidou.util.LogUtils;
import com.baidu.gson.Gson;

public class WMSiteServiceImpl implements WMSiteService {
	private static final Log LOG = LogFactory.getLog(WMSiteServiceImpl.class);

	// ����ǳ����� rate_compete>=0.9
	// ����Ƚϼ��� 30% 30%*N
	// �����һ�� 55% 25%*N
	// ����Ƚϻ��� 80% 25%*N
	// ������� 100% 20%*N
	public static final double RATE_COMPETE_THRESHOLD = 0.9; // rate_compete>=��ֵ��վ����ȶ����
	public static final double SECOND_LEVEL_THRESHOLD = 0.3;
	public static final double THIRD_LEVEL_THRESHOLD = 0.55;
	public static final double FOURTH_LEVEL_THRESHOLD = 0.8;
	
	/** �ļ���ÿ��Field֮��ķָ��� */
	public static final String FIELD_SEPARATOR = "\t";
	/** ��ֵ��֮��ķָ��� */
	public static final String KVPAIR_SPLITTER_OUTTER = "\\|";
    /** ��ֵ���ڲ��ķָ��� */
    public static final String KVPAIR_SPLITTER_INNER = ",";
    
    /** ���� */
    public static final int CARDINAL_NUMBER = 10000;
    
    /** ����lgA/B���ж��Ƿ����ĳ���Ե���ֵ */
    private double lgabThreshold = 0.1;
    
    /** ����Index����ļ����õ����� */
    private String fileEncoding = "GBK";
    
    /** sysnvtab�д�����Ի���ͳ����Ϣ��key */
    public static final String KEY_SITESTAT_OVERALL = "sitestat_overall";
    
    /** map�������KEY */
    public static final String KEY_AGE = "age";
    /** map���Ա��key */
    public static final String KEY_GENDER = "gender";
    /** map��ѧ���key */
    public static final String KEY_DEGREE = "degree";
    /** map�е����key */
    public static final String KEY_REGION = "region";
    
	private WM123SiteStatDao siteStatDao = null;
	private WM123SiteStatOnCapDao siteStatOnCapDao;
    private WM123SiteIndexDao siteIndexDao = null;

    private BDSiteStatDao bDSiteStatDao = null;
    private BDSiteStatOnAddbDao siteOnAddbDao = null;
    
    public static final Comparator<SiteEntity> siteIdComparator = new Comparator<SiteEntity> () {

        public int compare(SiteEntity o1, SiteEntity o2) {
            if (o1==null) {
                return -1;
            }
            if (o2 == null) {
                return 1;
            }
            return o1.getSiteId() - o2.getSiteId();
        }
    };
    public static final Comparator<IntegerEntry> integerValueCmp = new Comparator<IntegerEntry>(){

        public int compare(IntegerEntry o1, IntegerEntry o2) {
            return Long.valueOf( o2.getValue() ) .compareTo( Long.valueOf(o1.getValue())) ;
        }};

    public void wm123SiteCalculate() {
        
        List<WMSiteBo> list = siteStatDao.loadAllWMSite();
        
        caculate(list);
        
        store(list);
    }
    
    /**
     * store:����
    */
    protected void store(List<WMSiteBo> list) {
        if (list == null || list.size() == 0) {
            return;
        }
        
        //�Ȱ�ID�����ٴ���
        Collections.sort(list, siteIdComparator);
        siteIndexDao.addSiteAdditionalInfo(list);
    }
    
    /**
     * caculate: ����SiteHeat��IP��䡢UV���
    */
    protected void caculate(List<WMSiteBo> list) {

        if(list == null || list.size() == 0) {
            return;
        }
        
        //ѡ���򣬹���Ϊ��1����cmpLevel����2��scoreCmp��3�����cmpLevel=5��RateCmp
        Collections.sort(list, new Comparator<WMSiteBo> () {

            public int compare(WMSiteBo o1, WMSiteBo o2) {
                if (o1 == null) {
                    return -1;
                } else if (o2 == null) {
                    return 1;
                } else {
                    int result = o1.getCmpLevel() - o2.getCmpLevel();
                    if (result == 0) {

                        if(o1.getCmpLevel() == SiteConstant.CONSTANT_CMP_LEVEL_5) {

                            return Double.compare(o1.getRateCmp(), o2.getRateCmp());
                        }else{
                            return Float.compare(o1.getScoreCmp(), o2.getScoreCmp());
                        }
                    } else {

                        return result;
                    }
                }
            }
            
        });

        int medianIndex = (list.size() + 1)/2;//��λ������
        int itemCountBetweenMinAndMedian = 0;//����Ƚϻ��͸���
        int itemCountBetweenMedianAndTop = 0;//����Ƚϼ��Ҹ���
        for (WMSiteBo bo : list) {
            if (SiteConstant.CONSTANT_CMP_LEVEL_2 == bo.getCmpLevel()) {
                itemCountBetweenMinAndMedian++;
            } else if (SiteConstant.CONSTANT_CMP_LEVEL_4 == bo.getCmpLevel()) {
                itemCountBetweenMedianAndTop++;
            }
        }
        
        double min_rate_compete = list.get(0).getRateCmp();//��С��rateCmp
        double median_rate_compete = list.get(medianIndex).getRateCmp();//�м��rateCmp
		// double stepBetweenMinAndMedian = (median_rate_compete -
		// min_rate_compete) / (itemCountBetweenMinAndMedian +
		// 1);//min/median��Ĳ���
		// double stepBetweenMedianAndTop = (RATE_COMPETE_THRESHOLD -
		// median_rate_compete) / (itemCountBetweenMedianAndTop +
		// 1);////top/median��Ĳ���

		LOG.info("*********����վ���ȶ�ʱ��ͳ�ƣ�min_rate_compete=" + min_rate_compete + "��median_rate_compete" + median_rate_compete);

        //��ȡվ��id���ȶȶ�Ӧ��map
        Map<Integer, Integer> siteid2siteheatMap = caculateSiteHeat();
        
		// int list2Index = 0;//CONSTANT_CMP_LEVEL_2�ĵ�ǰ�������
		// int list4Index = 0;//CONSTANT_CMP_LEVEL_4�ĵ�ǰ�������
        //�˴�List�Ѿ�������õġ�
        for (int seq = 0; seq < list.size(); seq++ ) {
            WMSiteBo bo = list.get(seq);
            
            //����Ip�����������ӳ��ֵ
            bo.setIpLevel(SiteConstant.getIPLevelEnumValue(bo.getIps()));
            //����Uv�����������ӳ��ֵ
            bo.setUvLevel(SiteConstant.getUVLevelEnumValue(bo.getCookies()));
            
            /*����siteHeat����վ�ȶȣ�
            a) ������ǳ����ҡ�������ͼͼ����Ϊÿ��site��rate_compete*1��
            b)  �������һ�㡱������ͼͼ����Ϊ����site��median_rate_compete*1��
            c)  ������Ƚϼ��ҡ��͡�����Ƚϻ��͡���ÿ��site��score_compete�������к�ÿ��site������ͼ���Ƚ��ڸ��ƹ�����ǰ����������ĳ���ֵ֮�䣬
                  ʹ����ͼ���ȵ�����˳������վ������˳����ͬ��
            d)  ��������١�������ͼͼ����Ϊ����site��min��rate_compete��*1
            
            PS����ʾʱ����ֵӳ�䵽��0��100��
            */
            
            //-----------------> add by zhangxu start
            if(siteid2siteheatMap == null){
            	LOG.error("siteid2siteheat is null");
            	continue;
            }
            if(siteid2siteheatMap.get(bo.getSiteId()) != null){
                bo.setSiteHeat(siteid2siteheatMap.get(bo.getSiteId()));
            }
            else
            {
            	LOG.error(bo.getSiteId() + " can not be set to new siteheat because the calculate siteid2siteheat map doesn't contain the degree");
            }
            //-----------------> add by zhangxu end
            
            /* comment by zhangxu since cpweb-263
            switch (bo.getCmpLevel()) {
                case SiteConstant.CONSTANT_CMP_LEVEL_1:
                    bo.setSiteHeat( Double.valueOf(100 * min_rate_compete).intValue());
                    break;
                case SiteConstant.CONSTANT_CMP_LEVEL_2:
                    list2Index++;
                    bo.setSiteHeat( Double.valueOf(100 * (min_rate_compete + list2Index * stepBetweenMinAndMedian)).intValue());
                    break;
                case SiteConstant.CONSTANT_CMP_LEVEL_3:
                    bo.setSiteHeat( Double.valueOf(100 * median_rate_compete).intValue());
                    break;
                    
                case SiteConstant.CONSTANT_CMP_LEVEL_4:
                    list4Index++;
                    bo.setSiteHeat( Double.valueOf(100 * (median_rate_compete + list4Index * stepBetweenMedianAndTop)).intValue());
                    break;
                    
                case SiteConstant.CONSTANT_CMP_LEVEL_5:
                    bo.setSiteHeat( Double.valueOf(100 * bo.getRateCmp()).intValue());
                    break;
            }
            */
            
            /*
             	���㷨
            double temp;
            if (SiteConstant.CONSTANT_CMP_LEVEL_5 > bo.getCmpLevel()) {
                
                if ( seq < medianIndex ) {
                    //����min��median
                    temp = 100 * (min_rate_compete + seq * stepBetweenMinAndMedian);
                } else {
                  //����median��top
                    temp = 100 * (median_rate_compete + (seq - medianIndex) * stepBetweenMedianAndTop);
                }
                bo.setSiteHeat( Double.valueOf(temp).intValue() );
            } else {
                //����ǳ�����
                bo.setSiteHeat( Double.valueOf(100 * bo.getRateCmp()).intValue());
            }*/
        }
    }
    
    
	/**
	 * ����WM123�õ���վ���ȶ� <br>
	 * 
	 * WM123�õ���վ���ȶ���unionsiteadditional���cmpdegree�ֶΣ�����������Ǽ������cmpdegree�ģ�Ҳ����siteheat��
	 * 
	 * @author zhangxu
	 * @param 
	 * @return siteid��siteheat֮���ӳ���ϵ
	 * @since cpweb-263
	 */
    public Map<Integer, Integer> caculateSiteHeat(){
    	Map<Integer, Integer> siteId2SiteheatMap = new HashMap<Integer, Integer>();
    	LOG.info("Begin to caculate siteheat");
		
		Set<Integer> allUser = new HashSet<Integer>();
		Set<Integer> allSiteUser = new HashSet<Integer>();
		Set<Integer> allValidSite = new HashSet<Integer>();
		Map<Integer, Integer> tradeMap = new HashMap<Integer, Integer>();
		Map<Integer, Set<Integer>> siteUser = new HashMap<Integer, Set<Integer>>();
		Map<Integer, Set<Integer>> tradeUser = new HashMap<Integer, Set<Integer>>();
		
		List<Map<String, Object>> tradeList =  siteStatOnCapDao.getTradeList();
		for (Map<String, Object> map : tradeList) {
			int tradeId = Integer.valueOf((map.get("tradeid")).toString());
			int parentId = Integer.valueOf((map.get("parentid")).toString());
			tradeMap.put(tradeId, parentId);
		}
		
		List<SiteTradeVo> siteTradeList = siteStatDao.getSiteTradeList();
		for (SiteTradeVo site : siteTradeList) {
			allValidSite.add(site.getSiteid());
		}
				
		List<UserSiteVO> list = siteOnAddbDao.statSiteUserVo();
		if (CollectionUtils.isNotEmpty(list)) {
			for (int i = 0; i < list.size(); i++) {
				UserSiteVO rs = list.get(i);
				int userId = rs.userId;
				String tradeListStr = rs.siteTradeList;
				String siteListStr = rs.siteList;
				boolean allSite = rs.isallsite;

				allUser.add(userId);
				if (allSite) {
					allSiteUser.add(userId);
					continue;
				}

				String[] fields = null;
				if (!StringUtils.isEmpty(siteListStr)) {
					fields = siteListStr.split(SiteConstant.SITE_SEPERATOR_REX);
					for (String sitestr : fields) {
						if (StringUtils.isEmpty(sitestr)) {
							continue;
						}
						try {
							int siteId = Integer.valueOf(sitestr);
							if (allValidSite.contains(siteId)) {
								Set<Integer> set = siteUser.get(siteId);
								if (set == null) {
									set = new HashSet<Integer>();
									set.add(userId);
									siteUser.put(siteId, set);
								} else {
									set.add(userId);
								}
							}
						} catch (NumberFormatException e) {
							LogUtils.error(LOG, e.getMessage(), e);
						}
					}
				}

				if (!StringUtils.isEmpty(tradeListStr)) {
					fields = tradeListStr.split(SiteConstant.SITE_SEPERATOR_REX);
					for (String tradestr : fields) {
						if (StringUtils.isEmpty(tradestr) || tradestr.equals("NULL")) {
							continue;
						}
						try {
							int tradeId = Integer.valueOf(tradestr);

							Set<Integer> set = tradeUser.get(tradeId);
							if (set == null) {
								set = new HashSet<Integer>();
								set.add(userId);
								tradeUser.put(tradeId, set);
							} else {
								set.add(userId);
							}
						} catch (NumberFormatException e) {
							// LogUtils.error(LOG, e.getMessage(), e);
						}
					}
				}
			}
		}
		
		for (Integer trade : tradeUser.keySet()) {
			Integer parentId = tradeMap.get(trade);
			if (parentId != null) {
				Set<Integer> parentSet = tradeUser.get(parentId);
				Set<Integer> set = tradeUser.get(trade);
				Set<Integer> removeSet = new HashSet<Integer>();
				for (Integer userId : set) {
					if (parentSet.contains(userId)) {
						removeSet.add(userId);
					}
				}
				set.removeAll(removeSet);
			};
			
		}
		
		try {
			int max_site_num = 0;
			int max_trade_num = 0;
			for (SiteTradeVo siteTrade : siteTradeList) {
				int siteId = siteTrade.getSiteid();
				Set<Integer> set = siteUser.get(siteId);
				int countSite = 0;
				if (set != null) {
					countSite += set.size();
				}
				if(countSite > max_site_num){
					max_site_num = countSite;
				}
				
				int countTrade = 0;
				set = tradeUser.get(siteTrade.getFirsttradeid());
				if (set != null) {
					countTrade += set.size();
				}
				set = tradeUser.get(siteTrade.getSecondtradeid());
				if (set != null) {
					countTrade += set.size();
				}
				if(countTrade > max_trade_num){
					max_trade_num = countTrade;
				}
			}
			
			List<SiteHeatVo> allSiteHeatList = new ArrayList<SiteHeatVo>();
			
			// �����ȶ�
			for (SiteTradeVo siteTrade : siteTradeList) {
				int siteId = siteTrade.getSiteid();
				Set<Integer> set = siteUser.get(siteId);
				int countSite = 0;
				if (set != null) {
					countSite += set.size();
				}
				
				int countTrade = 0;
				set = tradeUser.get(siteTrade.getFirsttradeid());
				if (set != null) {
					countTrade += set.size();
				}
				set = tradeUser.get(siteTrade.getSecondtradeid());
				if (set != null) {
					countTrade += set.size();
				}
				int score = jisuanSiteheat(countSite,countTrade,max_site_num,max_trade_num);
				
				SiteHeatVo vo = new SiteHeatVo();
				vo.setSiteid(siteId);
				vo.setSiteUserNum(countSite);
				vo.setTradeUserNum(countTrade);
				vo.setScore(score);
				allSiteHeatList.add(vo);
			}
			
			
			Collections.sort(allSiteHeatList, new Comparator<SiteHeatVo>(){
				public int compare(SiteHeatVo vo1, SiteHeatVo vo2){
					if(vo1.getScore() > vo2.getScore()){
						return -1;
					}
					else if (vo1.getScore() == vo2.getScore())
					{
						if(vo1.getSiteUserNum() > vo2.getSiteUserNum()){
							return -1;
						}
						else if(vo1.getSiteUserNum() == vo2.getSiteUserNum()){
							if(vo1.getTradeUserNum() > vo2.getTradeUserNum()){
								return -1;
							}
							else if(vo1.getTradeUserNum() == vo2.getTradeUserNum()){
								return 0;
							}
							else
							{
								return 1;
							}
						}
						else{
							return 1;
						}
					}
					else
					{
						return 1;
					}
				}
			});
			
			for(SiteHeatVo siteHeatVo : allSiteHeatList)
			{
				siteId2SiteheatMap.put(siteHeatVo.getSiteid(), siteHeatVo.getScore());
			}
			LOG.info("End to caculate siteheat");
			return siteId2SiteheatMap;
		} catch (Exception e) {
			LOG.error(e.getMessage(),e);
			return null;
		}
		
    }
    
    /**
     * ��������
     * @param fenzi ����
     * @param fenmu ��ĸ
     * @return 
     */
    private double jisuanRatio(int fenzi, int fenmu){
    	return (double)((double)fenzi / (double)fenmu);
    }
    
    /**
     * ����վ���ȶ�
     * @param countSite ѡ���վ����û���
     * @param countTrade ѡ���վ����ҵ���û���
     * @param max_site_num ����ѡ���վ����û���
     * @param max_trade_num ����ѡ���վ����ҵ���û���
     * @return
     */
    private int jisuanSiteheat(int countSite, int countTrade, int max_site_num, int max_trade_num){
    	double one = jisuanRatio(countSite,max_site_num);
    	double two = jisuanRatio(countTrade,max_trade_num);
    	return (int)((one * 6/10 + two * 4/10)*100); //����ϵ��ѡ��0.4
    }
    
    
    public void storeOriginIndexInfo(String[] indexFiles) throws IOException {
        //˵������ǰBA��������ݷ�Ϊ�����ļ���һ��(regionFile)��¼������Ϣ��һ��(otherIndexFile)��¼������Ⱥ������Ϣ.
        
        if (indexFiles == null || indexFiles.length < 2) {
            LOG.error("������Ҫ����������ļ����һ��Ϊ���򣬵ڶ���Ϊ��Ⱥ����");
            throw new IOException("������Ҫ����������ļ����һ��Ϊ���򣬵ڶ���Ϊ��Ⱥ����");
        }
        
        String regionFile = indexFiles[0];
        String otherIndexFile = indexFiles[1];
        
        File region = new File(regionFile);
        File other = new File(otherIndexFile);
        List<WMSiteIndexVo> regionList ;//���Region
        List<WMSiteIndexVo> mainIndexList ;//���Age��Degree��Gender
        
        //��ȡSiteId��SiteUrl��Mapping
        Map<Integer, String> checkMap =  siteStatDao.getAllSiteIdUrlMapping();
        
        if (!region.exists()) {

            LOG.error("�Ҳ������������ļ���" + regionFile);
            throw new FileNotFoundException("�Ҳ������������ļ���" + regionFile);
        }
        if (!other.exists()) {
            LOG.error("�Ҳ���index�����ļ���" + otherIndexFile);
            throw new FileNotFoundException("�Ҳ���index�����ļ���" + otherIndexFile);
        }
        
        BufferedReader br = new BufferedReader(
                new InputStreamReader( new FileInputStream(region), fileEncoding));
        regionList = genRegionList(br, checkMap);
        br.close();

        br = new BufferedReader(
                new InputStreamReader( new FileInputStream(other), fileEncoding));
        mainIndexList = genOtherIndexList(br, checkMap);
        br.close();
        
        //�ϲ�����List���������ŵ�����List�С�
        mainIndexList = merge(mainIndexList, regionList);
        
        
        //����
        siteIndexDao.addSiteIndex(mainIndexList);
    }
    
    /**
     * genRegionList:���Region��ݶ�Ӧ��WMSiteIndexVo
     *
     * @param br ������
     * @param checkMap ����У���SiteId��SiteUrl��Mapping
     * @return  ����region��WMSiteIndexVo��List,����SiteId��������
     * @throws IOException 
    */
    private List<WMSiteIndexVo> genRegionList(BufferedReader br, Map<Integer, String> checkMap) throws IOException {
        List<WMSiteIndexVo> list = new ArrayList<WMSiteIndexVo>(10000);
        String line;
        String[] fields;
        WMSiteIndexVo vo;
        int ignoreCount = 0;//�Թ����
        int validCount = 0;//��Ч����
        int noMatchCount = 0;//SiteId��SiteUrl����ݿ��в�ͬ�ĸ���
        
        Integer siteIdTemp;//�ݴ�SiteId
        String siteUrlTemp;//�ݴ�SiteUrl
        
        while ( (line = br.readLine()) != null ) {
            if (org.apache.commons.lang.StringUtils.isEmpty(line)) {
                continue;
            }
            fields = line.split(FIELD_SEPARATOR);
            if (fields.length < 3) {
                LOG.info("[" + line + "] is ignored due to fields less than 3");
                ignoreCount ++;
                continue;
            }
            try {
                siteIdTemp = Integer.valueOf(fields[0]);
                siteUrlTemp = fields[1];
                String dbUrl =  checkMap.get(siteIdTemp);
                if (!siteUrlTemp.equals( dbUrl ) ) {
                    //����ļ��е�SiteID��SiteURL����ݿ��е�ӳ���ϵ��ͬ����Ignore�����
                    ignoreCount ++;
                    noMatchCount ++;
                    LOG.info("[" + line + "] is ignored due to siteURL disMatch,urlInFile=[" + siteUrlTemp + "]," +
                            "urlInDb=[" + dbUrl + "], checkMap.size()=" + checkMap.size());
                    continue;
                }
                vo = new WMSiteIndexVo();
                vo.setSiteId( siteIdTemp );
            } catch (Exception e) {
                LOG.info("[" + line + "] is ignored due to following error: " + e.getMessage());
                ignoreCount ++;
                continue;
            }
            vo.setRegion( fields[2] );
            validCount++;
            list.add(vo);
        }
        //���� 
        Collections.sort(list, siteIdComparator);
        LOG.info("*************distribute file dealing result:ignore=" + ignoreCount + ",valid=" + validCount + ", noMatch=" + noMatchCount);
        return list;
    }
    
    /**
     * genOtherIndexList:�������Index��ݶ�Ӧ��WMSiteIndexVo,����SiteId��������
     *
     * @param br ������
     * @param checkMap ����У���SiteId��SiteUrl��Mapping
     * @return  ����gender,age,degree��WMSiteIndexVo��List
     * @throws IOException 
    */
    private List<WMSiteIndexVo> genOtherIndexList(BufferedReader br, Map<Integer, String> checkMap) throws IOException {
        List<WMSiteIndexVo> list = new ArrayList<WMSiteIndexVo>(10000);
        String line;
        String[] fields;
        WMSiteIndexVo vo;
        int ignoreCount = 0;//�Թ����
        int validCount = 0;//��Ч����
        int noMatchCount = 0;//SiteId��SiteUrl����ݿ��в�ͬ�ĸ���
        
        Integer siteIdTemp;//�ݴ�SiteId
        String siteUrlTemp;//�ݴ�SiteUrl
        
        while ( (line = br.readLine()) != null ) {
            if (org.apache.commons.lang.StringUtils.isEmpty(line)) {
                continue;
            }
            fields = line.split(FIELD_SEPARATOR);
            if (fields.length < 5) {
                ignoreCount ++;
                LOG.info("[" + line + "] is ignored due to fields less than 5");
                continue;
            }
            
            try {
                siteIdTemp = Integer.valueOf(fields[0]);
                siteUrlTemp = fields[1];
                if (!siteUrlTemp.equals( checkMap.get(siteIdTemp)) ) {
                    //����ļ��е�SiteID��SiteURL����ݿ��е�ӳ���ϵ��ͬ����Ignore�����
                    ignoreCount ++;
                    noMatchCount++;
                    LOG.info("[" + line + "] is ignored due to siteURL disMatch,file=[" + siteUrlTemp + "]," +
                    		"db=[" + checkMap.get(siteIdTemp).length() + "]");
                    continue;
                }
                vo = new WMSiteIndexVo();
                vo.setSiteId( siteIdTemp );
            } catch (Exception e) {
                ignoreCount ++;
                LOG.info("[" + line + "] is ignored due to following error: " + e.getMessage());
                continue;
            }
            
            vo.setGender( fields[2] );//������Ϊ�Ա�
            vo.setAge( fields[3] );//����������
            vo.setDegree( fields[4] );//������Ϊѧ��
            validCount++;
            list.add(vo);
        }
        Collections.sort(list, siteIdComparator);
        LOG.info("*************shuxing file dealing result:ignore=" + ignoreCount + ",valid=" + validCount + ", noMatch=" + noMatchCount);
        return list;
    }
    
    /**
     * merge:�ϲ�����List�����SiteId��������
     *
     * @param main ��List,otherSiteIndex���Ѿ�����
     * @param sub  ��ϲ���List������List���Ѿ�����    
     * @return list �ϲ���Ľ��
    */
    protected List<WMSiteIndexVo> merge(List<WMSiteIndexVo> main, List<WMSiteIndexVo> sub) {
        List<WMSiteIndexVo> result = new ArrayList<WMSiteIndexVo>();//���ڴ�Ž��
        int mainIndex = 0;//main��ָ��
        int subIndex = 0; //sub��ָ��
        WMSiteIndexVo mainSite;   //��List�ĶԶ���
        WMSiteIndexVo subSite;    //��List�Ķ���
        
        for (; mainIndex < main.size() && subIndex < sub.size();) {
            mainSite = main.get(mainIndex);
            subSite = sub.get(subIndex);
            if (mainSite.getSiteId() == subSite.getSiteId()) {
                //��ȣ�ֱ�Ӻϲ�
                mainSite.setRegion(subSite.getRegion());
                
                result.add(mainSite);
                
                mainIndex++;
                subIndex++;
                
            } else if (mainSite.getSiteId() < subSite.getSiteId()) {
                //main��SiteId��С
                result.add(mainSite);
                mainIndex++;
            } else {
                //main��SiteId�ϴ�

                result.add(subSite);
                subIndex++;
            }
        }
        
        //��ʣ�µ�sub����main�����ݺϲ���result��
        if (subIndex < sub.size() ) {
            result.addAll(sub.subList(subIndex, sub.size()));
        }
        if (mainIndex < main.size()) {
            result.addAll(main.subList(mainIndex, main.size()));
        }
        
        return result;
    }

    class Stat {
        long count;//���������Ϊ0
        double amount;//�ۼ���
        double percent;//ռͬ�������İٷֱ�
    }
    
    private Set<Integer> makeSet(int min, int max) {
        Set<Integer> set = new HashSet<Integer>();
        for (int i = min; i <= max; i++) {
            set.add(i);
        }
        return set;
    }
    
    public void wm123SiteIndexCalculate() {

        /**
         * ˵������ǰBA���Region��Index��������еĶ��������һ�������ͳ������
         * ����һ������ĵ�ǰͳ�����ټ��������������ж��������ͳ������Ϊ��һ���������ͳ������
         * ������ǰһ�������ͳ����ʵΪ�õ����¡�����������������
         * ��ǰ�������ֵΪ��7--34
         */
        LOG.info("***********Start to prepare basic datas ");
        
        List<Integer> exceptionRegIds = siteStatOnCapDao.findExceptionalRegInfoId();
        Set<Integer> exceptionRegIdSet = new HashSet<Integer>(40);
        exceptionRegIdSet.addAll(exceptionRegIds);

        Set<Integer> genderCandiates = makeSet(SiteConstant.MIN_GENDER_VALUE, SiteConstant.MAX_GENDER_VALUE);//��Ч���Ա�ö��ֵ[1,2]
        Set<Integer> ageCandiates = makeSet(SiteConstant.MIN_AGE_VALUE, SiteConstant.MAX_AGE_VALUE);//��Ч���������ö��ֵ[1-7]
        Set<Integer> eduCandiates = makeSet(SiteConstant.MIN_EDU_VALUE, SiteConstant.MAX_EDU_VALUE);//��Ч��ѧ��ö��ֵ[1-8]
        Map<Integer, Integer> second2First = new HashMap<Integer, Integer>(700);//key��ʾ��Ч��RegId

        //ȡtype=1��region
        List<RegionInfo> allType1Regions = siteStatOnCapDao.findAllRegInfoByType(1);
        for (RegionInfo ri : allType1Regions) {
            //����secondregid��firstregid��ӳ�䣬�������
            Integer rid = ri.getSecnodRegId();
            
            /**
             * ��ǰFirstRegId��ΧΪ1-38���Ժ������չ�˹���Ļ���ע��˴�ӳ���Ƿ�Ϻ����߼�
             */
            if (rid > 0) {
                //��������ID��Ϊ0�Ľ���ӳ��
                second2First.put(ri.getSecnodRegId(), ri.getFirstRegId());
            } else {
                //һ��ֱ��ӳ�䵽�Լ�
                second2First.put(ri.getFirstRegId(), ri.getFirstRegId());
            }
                
        }
        
        List<WMSiteIndexVo> voList = siteIndexDao.loadAllSiteIndexInfo();
        List<WMSiteIndexBo>  boList = new ArrayList<WMSiteIndexBo>(voList.size());
        Map<Integer, Stat> regionStat = new HashMap<Integer, Stat>(60);//ͳ��region��Ϣ��
        Map<Integer, Stat> genderStat = new HashMap<Integer, Stat>();//ͳ��gender��Ϣ��
        Map<Integer, Stat> ageStat = new HashMap<Integer, Stat>();//ͳ��age��Ϣ��
        Map<Integer, Stat> degreeStat = new HashMap<Integer, Stat>();//ͳ��education��Ϣ��

        LOG.info("***********End to prepare basic datas");
        
        LOG.info("***********Start to digest string ");
        
        //����һ������ͳ����Ϣ
        for (WMSiteIndexVo vo : voList) {
            WMSiteIndexBo bo = new WMSiteIndexBo();
            bo.setSiteId(vo.getSiteId());
            
            //����region
            digestString(vo.getRegion(), regionStat, bo.getCityValue(), second2First.keySet());
            
            /**----------------------------->��ʼͳ��һ�����������*/
            Map<Integer, Long> provinceStat = new HashMap<Integer, Long>();//ͳ��һ�����������
            for( IntegerEntry entry : bo.getCityValue()) {
                Integer key = entry.getKey();
                Integer itsParent = second2First.get(key);
                
                //�˴���BA������У����صĶ�������һ�����ж�Ӧ��һ���������Ϊһ�������?
                //�˴�����Ҳ���itsParent��˵��Ϊһ������
                if ( itsParent != null ) {
                    //�����Ϊ�����������
                    
                    Long value = provinceStat.get(itsParent);
                    if (value == null) {
                        value = entry.getValue();
                    } else {
                        value += entry.getValue();
                    }
                    provinceStat.put(itsParent, value);
                } else {
                    
                    //���������ݡ�
                    LOG.warn("Error: regionField[" + key +"] is invalid��and siteId=" + vo.getSiteId());
                }
            }
            List<IntegerEntry> provinces = new ArrayList<IntegerEntry>();
            Iterator<Integer> it = provinceStat.keySet().iterator();
            while (it.hasNext()) {
                Integer key = it.next();
                provinces.add(new IntegerEntry(key, provinceStat.get(key)));
            }
            Collections.sort(provinces, integerValueCmp);
            bo.setProvinceStatList(provinces);
            
            /**��һ�������ͳ��Top����ʱ�á���ΪBA���һ����������ơ���ƽ�ء��Ķ������룬��ʽ����в����д�����Ϣ��
             * Very important���öδ��벻��������ʱʹ��
            for(IntegerEntry entry : provinces) {
                if (entry.getKey() < 7) {
                    Stat stat = regionStat.get(entry.getKey());
                    if (stat == null) {
                        stat = new Stat();
                        regionStat.put(entry.getKey(), stat);
                    }
                    stat.amount += entry.getValue();
                    stat.count++;
                }
            }*/
            
            /**<-----------------------------ͳ����һ�����������*/
            
            //����gender
            digestString(vo.getGender(), genderStat, bo.getGenderValue(), genderCandiates);
            //����age
            digestString(vo.getAge(), ageStat, bo.getAgeValue(), ageCandiates);
            //����education
            digestString(vo.getDegree(), degreeStat, bo.getEducationValue(), eduCandiates);
            
            // ������Ⱥ������ֵ���ˣ�
            // ����Ա������ѧ������һ�����Ե���cookies��С��CROWDS_FILTER_THRESHOLD���򱻹��˵�
            if (!isFiltered(bo.getGenderValue()) && !isFiltered(bo.getAgeValue()) 
            		&& !isFiltered(bo.getEducationValue())) {
            	boList.add(bo);
            }
            
        }
        LOG.info("***********End to digest string ");
        
        /**�ڶ�����ͳ��Top����ʱ�á�
         * Very important���öδ��벻��������ʱʹ��
         * -------------------------->��һ��Top10����
        List<IntegerEntry> citypair = new ArrayList<IntegerEntry>();
        Iterator<Integer> i  = regionStat.keySet().iterator();
        while (i.hasNext()) {
            Integer key = i.next();
            if(key < 7) {
                System.out.println(key + ":" +regionStat.get(key).amount);
            }
            if(!exceptionRegIds.contains(key)) {
                citypair.add(new IntegerEntry(key, Double.valueOf(regionStat.get(key).amount).intValue()));
            }
        }
        Collections.sort(citypair, new Comparator<IntegerEntry>(){

            public int compare(IntegerEntry o1, IntegerEntry o2) {
                return o2.getValue() - o1.getValue();
            }}
        );
        try {
            BufferedWriter bw = new BufferedWriter(new FileWriter("E:\\regionStat.txt"));
            for (int index = 0; index < citypair.size(); index++) {
                String regionname = "unknown";
                IntegerEntry entry = citypair.get(index);
                for (int re = 0; re < allType1Regions.size(); re++) {
                    RegionInfo ri = allType1Regions.get(re);
                    if(ri.getSecnodRegId() == entry.getKey() 
                            || (ri.getSecnodRegId() == 0 && ri.getFirstRegId() == entry.getKey())) {
                        regionname = ri.getName();
                    }
                }
                bw.write(entry.getKey() + "\t" + regionname + "\t" + entry.getValue() + "\r\n");
                bw.flush();
            }
        } catch (IOException e) {
            e.printStackTrace();
        } 
//        <-----------------------------------------Top10��*/
        
        
        
        //����ƽ��ֵ
        calculateStat(regionStat);
        calculateStat(genderStat);
        calculateStat(ageStat);
        calculateStat(degreeStat);

        LOG.info("***********Start to calc lgA/B and percentage");
        //ͳ����ȫ�����֮������ĸ�Index���Ը��Ծ��е�����ID����ö���б�ֵ��
        for (WMSiteIndexBo bo : boList) {
            /**
             * ע�⣺����ٷֱ�ֵ�����ڼ���Index����֮����С�
             * ��Ϊ֮ǰMap��value����Ǿ�����ֵ��Ҫ������lga/b��ʽ�ġ�����֮����ܱ�����������ʾ�ٷֱȡ�
             */
            
            bo.setGenderList( calIncludesAndPercent(bo.getGenderValue(), genderStat) );
            bo.setCityList( calIncludesAndPercent(bo.getCityValue(), regionStat, exceptionRegIdSet) );
            bo.setAgeList( calIncludesAndPercent(bo.getAgeValue(), ageStat) );
            bo.setEducationList( calIncludesAndPercent(bo.getEducationValue(), degreeStat) );
        }

        LOG.info("***********End to calc lgA/B and percentage");
        //

        //������ֶ��и�ö��ֵ��ռ�İٷֱ�
        
        //����󱣴�
        LOG.info("***********Start to Save����" + boList.size() + "��");

        siteIndexDao.addSiteIndexStat(boList);
        
        Wrap wrap = new Wrap();
        wrap.map.put(KEY_AGE, genStatMap(ageStat));
        wrap.map.put(KEY_GENDER, genStatMap(genderStat));
        wrap.map.put(KEY_DEGREE, genStatMap(degreeStat));
        
        siteStatOnCapDao.updateSysnvtab(KEY_SITESTAT_OVERALL, new Gson().toJson(wrap));
        LOG.info("***********End  to Save");
    }
    static class Wrap {
        Map<String, Map<Integer, Double>> map = new HashMap<String, Map<Integer,Double>>();
    }
    
    /**
     * ������Ⱥ���Թ�����ֵ�жϣ���cookies��С��CROWDS_FILTER_THRESHOLD���򱻹��˵�
     */
    private boolean isFiltered(List<IntegerEntry> crowdList) {
    	long total = 0;
    	for (IntegerEntry crowd : crowdList) {
    		total += crowd.getValue();
    	}
    	if (total < SiteConstant.CROWDS_FILTER_THRESHOLD) {
    		return true;
    	}
    	
    	return false;
    }
    
/*    public void testIsFiltered() {
    	List<IntegerEntry> crowdList = new ArrayList<IntegerEntry>();
    	System.out.println("size: " + crowdList.size() 
    			+ ", result: " + isFiltered(crowdList));
    	
    	IntegerEntry entry1 = new IntegerEntry(1, 1);
    	IntegerEntry entry2 = new IntegerEntry(2, 2);
    	IntegerEntry entry3 = new IntegerEntry(3, 3);
    	IntegerEntry entry10 = new IntegerEntry(10, 10);
    	
    	
    	crowdList.add(entry10);
    	System.out.println("size: " + crowdList.size() 
    			+ ", result: " + isFiltered(crowdList));
    	
    	crowdList.add(entry1);
    	System.out.println("size: " + crowdList.size() 
    			+ ", result: " + isFiltered(crowdList));
    	
    	crowdList.remove(entry10);
    	System.out.println("size: " + crowdList.size() 
    			+ ", result: " + isFiltered(crowdList));
    	
    	crowdList.add(entry2);
    	System.out.println("size: " + crowdList.size() 
    			+ ", result: " + isFiltered(crowdList));
    	
    	crowdList.remove(entry1);
    	crowdList.add(entry3);
    	System.out.println("size: " + crowdList.size() 
    			+ ", result: " + isFiltered(crowdList));
    	
    	crowdList.remove(entry2);
    	System.out.println("size: " + crowdList.size() 
    			+ ", result: " + isFiltered(crowdList));
    }*/
    
    private Map<Integer, Double> genStatMap(Map<Integer, Stat> map) {
        Map<Integer, Double> overall = new HashMap<Integer, Double>();
        Iterator<Integer> it = map.keySet().iterator();
    	Integer key;
        while(it.hasNext()) {
        	key = it.next();
        	overall.put(key, map.get(key).percent);
        }
        return overall;
    }
    
    /**
     * calculateStat: ����ÿ��������ռ�����İٷֱȡ��磺�����ķ�����ռ�ܵķ������İٷֱȣ����ڼ���lgA/B��
     *
     * @param statMap      
     * @since 
    */
    private void calculateStat(Map<Integer, Stat> statMap) {
        double sum = 0.0;//����(���Ա���)ͳ�Ƶ�����
        for (Stat stat : statMap.values()) {
            sum += stat.amount;
        }
        
        Iterator<Integer> it = statMap.keySet().iterator();
        Stat stat ;
        Integer key;
        while (it.hasNext()) {
            key = it.next();
            stat = statMap.get(key);
            stat.percent = stat.amount / sum;
        }
    }
    
    /**
     * calBitAndPercent:1���������Bo���еĸ�����ֵ�����ص���List��2��������ֶ��и�ö��ֵ��ռ�ĵİٷֱ�ֵ
     * �����Ա�������ԭʼֵΪ1,2|2,8����1�������Ƿ�����������ԣ��Ƿ����Ů�����ԣ�Ȼ����λ���㣻
     *                             2��������԰ٷֱȣ�1,2000|2,8000����3���԰ٷֱȽ����н�������
     * @param boStat   
     * @param totalstatMap
     * @since 
    */
    public List<Integer> calIncludesAndPercent(List<IntegerEntry> boStat, Map<Integer, Stat> totalstatMap) {
        return calIncludesAndPercent(boStat, totalstatMap, null);
    }
    public List<Integer> calIncludesAndPercent(List<IntegerEntry> boStat, 
            Map<Integer, Stat> totalstatMap, Collection<Integer> excludes) {
        
        double percent;//һ�������ĳ���Եķ�������ռ�ٷֱ�
        double sum = 0;//ͳ������
        
        List<Integer> result = new ArrayList<Integer>();//������

        boolean exclude ;  
        IntegerEntry kvPari;
        for ( int index = 0; index < boStat.size();  ) {
            kvPari = boStat.get(index) ;
            sum += kvPari.getValue();
          //����ù��˸�ֵ ����м��㣬�˴���Ҫ�����ڵ�����е�һ�����򣨲���4��ֱϽ�к�2�����������
            exclude = excludes == null ? false : excludes.contains(kvPari.getKey());  
            if (exclude) {
                boStat.remove(kvPari);
            } else {
                index++;
            }
        }
        if(sum<=0) {
            return result;
        }
        
        for ( IntegerEntry kv : boStat ) {
            Integer key = kv.getKey();
            Long value = kv.getValue();
            
            //��*CARDINAL_NUMBER�����⾫�Ⱥ�Խ�����⡣
            percent = new BigDecimal(value).multiply( new BigDecimal(CARDINAL_NUMBER) ).
            divide( new BigDecimal(sum), 10, BigDecimal.ROUND_HALF_DOWN).doubleValue();
            /*------------------------��Ҫ----------------
              -����С������2λ����34.21%��ʾ��3421
              */
            value = Double.valueOf( percent ).longValue();
            kv.setValue(value);
            
            if (value < 0) {
                System.out.println(value);
            }
            //��������ֵ
            double lgAB = Math.log10( (percent/CARDINAL_NUMBER) / totalstatMap.get(key).percent );
            if ( lgAB > lgabThreshold) {
                
                result.add(key);
            }
        }
        
        //�԰ٷֱȽ������򣬰�IntegerEntry.value��������
        Collections.sort(boStat, new Comparator<IntegerEntry>() {

            public int compare(IntegerEntry o1, IntegerEntry o2) {
                
                int desc = -1;//����
                
                if (o1 == null) {
                    return -1 * desc;
                }
                if (o2 == null) {
                    return 1 * desc;
                }
                return Long.valueOf( o1.getValue() ) .compareTo( Long.valueOf(o2.getValue())) * desc;
            }
            
        });
        return result;
    }
    

    /**
     * digestString:����Index�ַ�����֮�洢����Ӧ���ֶε�Map�С�
     * ֻ����candidates�е���ݽ��д��?�������candidates��ʾ��Ҫͳ�Ƶĵ�����Ϣ
     * @param temp ���磺1,2|2,2|3,|...
     * @param totalstatMap ȫ��ͳ��map
     * @param boStatEntry    bo��ͳ��List  
     * @param candidates   ȡֵ�ĺ�ѡ��  
     * @since 
    */
    protected void digestString(String temp, Map<Integer, Stat> totalstatMap, 
            List<IntegerEntry> boStatEntry, Set<Integer> candidates) {

        if (!StringUtils.isEmpty(temp)) {
            
            //���磺1,2|2,2
            String[] kvPairs = temp.split(KVPAIR_SPLITTER_OUTTER);
            for (String kvPair : kvPairs) {
                
                String[] kv = kvPair.split(KVPAIR_SPLITTER_INNER);
                if ( kv.length != 2 || StringUtils.isEmpty(kv[1]) ) {
                    continue;
                }
                try {
                    Integer key = Integer.valueOf(kv[0]);
                    Integer value = Integer.valueOf(kv[1]);
                    
                    //���ڲ��ں�ѡ���еģ���999�ı�ʾ����)��������
                    if ( candidates != null && !candidates.contains(key)) {
                        //��ǰֻ�Ե�����ˣ��˴�ֻ�ǰѡ���ѡ������������֪�����ĸ��ֶγ������ˡ�
//                        LOG.warn("[" + key + "]���ں�ѡ���С���ѡ������Ϊ��" + candidates.size());
                        continue;
                        
                    }
                    /***************************************************************
                     * ע�⣺�˴���ʱ���ھ�������ֵ��֮���ͨ�����Ѱٷֱ�ֵ���ڴ�Map��Value��
                     */
                    boStatEntry.add(new IntegerEntry(key, value));
                    
                    
                    Stat stat = totalstatMap.get(key);
                    if (stat == null) {
                        stat = new Stat();
                        stat.amount = 0;
                        stat.count = 0;
                        totalstatMap.put(key, stat);
                    }
                    
                    stat.count++;
                    stat.amount += value;
                } catch (Exception e) {
                    //���������ֳ��?��ignore
                    e.printStackTrace();
                    continue;
                }
            }
        }
    }
    
    public List<TradeSiteElement> getTopNSitesByTrade(final int topN){
    	    	
    	List<TradeSiteElement> result = siteStatOnCapDao.findAllFirstTradeIdName();
    	
    	if(CollectionUtils.isEmpty(result)){
    		return result;
    	}
    	
    	for(TradeSiteElement trade : result){
    		
    		List<SiteElement> sites = siteStatDao.findSearchTopNSitesByFirstTradeId(trade.getId(), topN);
    		trade.setSites(sites);  		
    		
    	}
    	
    	return result;
    }
    
    
    /**
	 * storeVisitorIndexInfo: �洢˾�ϴ��ݹ�����ԭ��ÿ�������ݣ��˴������㡣
	 * �������WM123�������������
	 * ע�⣺�˴�Ҫ��SiteId��SiteUrl��ӳ�䡣
	 * 
	 * @param files Ҫ������ļ���
	 * @since cpweb-263
	*/
    public void storeVisitorIndexInfo(String indexFile) throws IOException {
        
        if (indexFile == null || indexFile.equals("")) {
            LOG.error("Please input file path to be import");
            throw new IOException("Please input file path to be import");
        }
        
        File visitorIndexFile = new File(indexFile);
        List<WMSiteVisitorIndexVo> visitorIndexList ;//��ŷÿ�����
        
        //��ȡSiteUrl��SiteId��Mapping
        Map<String,Integer> checkMap =  siteStatDao.getAllSiteUrl2IdMapping();
        
        if (!visitorIndexFile.exists()) {

            LOG.error("input file path is wrong for " + visitorIndexFile);
            throw new FileNotFoundException("input file path is wrong for " + visitorIndexFile);
        }

        
        BufferedReader br = new BufferedReader(
                new InputStreamReader( new FileInputStream(visitorIndexFile), fileEncoding));
        visitorIndexList = genVisitorIndexList(br, checkMap);
        br.close();
        
        //����
        siteIndexDao.addSiteVistorIndex(visitorIndexList);
    }
    
    /**
     * genRegionList:���visitor��ݶ�Ӧ��WMSiteVisitorIndexVo
     *
     * @param br ������
     * @param checkMap ����У���SiteUrl��SiteId��Mapping
     * @return  ����visitor��WMSiteVisitorIndexVo��List,����SiteId��������
     * @throws IOException 
    */
    private List<WMSiteVisitorIndexVo> genVisitorIndexList(BufferedReader br, Map<String,Integer> checkMap) throws IOException {
        List<WMSiteVisitorIndexVo> list = new ArrayList<WMSiteVisitorIndexVo>(10000);
        String line;
        String[] fields;
        WMSiteVisitorIndexVo vo;
        int ignoreCount = 0;//�Թ����
        int validCount = 0;//��Ч����
        int noMatchCount = 0;//SiteId��SiteUrl����ݿ��в�ͬ�ĸ���
        
        
        String siteUrlTemp;//�ݴ�SiteUrl
        String site;//�ݴ����վ��
        String keyword;//�ݴ�ؼ��
        String interest;//�ݴ���Ȥ��
        Integer siteIdTemp;//�ݴ�SiteId
        
        while ( (line = br.readLine()) != null ) {
            if (org.apache.commons.lang.StringUtils.isEmpty(line)) {
                continue;
            }
            fields = line.split(FIELD_SEPARATOR);
            if (fields.length != 5) {
                LOG.info("[" + line + "] is ignored due to fields not equal to 5");
                ignoreCount ++;
                continue;
            }
            try {
            	if(org.apache.commons.lang.StringUtils.isEmpty(fields[0])){
                	LOG.info("[" + line + "] tid is null");
                    ignoreCount ++;
                    continue;
            	}
                int tid = Integer.valueOf(fields[0]);
                siteUrlTemp = fields[1];
                site = fields[2];
                keyword = fields[3];
                interest = fields[4];
                if(org.apache.commons.lang.StringUtils.isEmpty(siteUrlTemp)){
                	LOG.info("[" + line + "] siteurl is null");
                    ignoreCount ++;
                    continue;
                }
                if(org.apache.commons.lang.StringUtils.isEmpty(site)){
                	LOG.info("[" + line + "] site is null");
                    ignoreCount ++;
                    continue;
                }
                if(org.apache.commons.lang.StringUtils.isEmpty(keyword)){
                	LOG.info("[" + line + "] keyword is null");
                    ignoreCount ++;
                    continue;
                }
                if(org.apache.commons.lang.StringUtils.isEmpty(interest)){
                	LOG.info("[" + line + "] interest is null");
                    ignoreCount ++;
                    continue;
                }
                // ����˾�����siteurl�����˴��?����һ������Ҫ����www�����Ǳ����ﶼ�ǲ���www.��ͷ�ģ����Դ˴�Ҫȥ����
                if(siteUrlTemp.startsWith(SiteConstant.WWW_PREFIX)){
                	siteUrlTemp = siteUrlTemp.substring(4);
                }
                siteIdTemp =  checkMap.get(siteUrlTemp);
                if(siteIdTemp == null){
                	//LOG.info("[" + line + "] siteurl is not in wm map");
                	noMatchCount ++;
                    continue;
                }
                vo = new WMSiteVisitorIndexVo();
                vo.setTid(tid);
                vo.setSiteId(siteIdTemp);
                vo.setSiteurl(siteUrlTemp);
                vo.setInterest(interest);
                vo.setKeyword(keyword);
                vo.setSite(site);
            } catch (Exception e) {
                LOG.info("[" + line + "] is ignored due to following error: " + e.getMessage());
                ignoreCount ++;
                continue;
            }
            validCount++;
            list.add(vo);
        }
        //���� 
        Collections.sort(list, siteIdComparator);
        LOG.info("*************visitor file dealing result:ignore=" + ignoreCount + ",valid=" + validCount + ", noMatch=" + noMatchCount);
        return list;
    }

	public WM123SiteStatDao getSiteStatDao() {
		return siteStatDao;
	}

	public void setSiteStatDao(WM123SiteStatDao siteStatDao) {
		this.siteStatDao = siteStatDao;
	}

	public WM123SiteIndexDao getSiteIndexDao() {
		return siteIndexDao;
	}

	public void setSiteIndexDao(WM123SiteIndexDao siteIndexDao) {
		this.siteIndexDao = siteIndexDao;
	}

	public String getFileEncoding() {
		return fileEncoding;
	}

	public void setFileEncoding(String fileEncoding) {
		this.fileEncoding = fileEncoding;
	}

	public static int getCARDINAL_NUMBER() {
		return CARDINAL_NUMBER;
	}

	public double getLgabThreshold() {
		return lgabThreshold;
	}

	public void setLgabThreshold(double lgabThreshold) {
		this.lgabThreshold = lgabThreshold;
	}

	public BDSiteStatDao getbDSiteStatDao() {
		return bDSiteStatDao;
	}

	public void setbDSiteStatDao(BDSiteStatDao bDSiteStatDao) {
		this.bDSiteStatDao = bDSiteStatDao;
	}

	public void setSiteOnAddbDao(BDSiteStatOnAddbDao siteOnAddbDao) {
		this.siteOnAddbDao = siteOnAddbDao;
	}

	public void setSiteStatOnCapDao(WM123SiteStatOnCapDao siteStatOnCapDao) {
		this.siteStatOnCapDao = siteStatOnCapDao;
	}

}
