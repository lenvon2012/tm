package underup.frame.industry;

import models.item.ItemCatPlay;
import underup.frame.industry.CatTopSaleItemSQL;

import org.codehaus.jackson.annotate.JsonAutoDetect;
import org.codehaus.jackson.annotate.JsonProperty;
import org.slf4j.*;

import java.util.*;

public class ItemPropsArrange {
    private static final Logger log = LoggerFactory.getLogger(ItemPropsArrange.class);

    private long cid;

    private long year;

    private long month;

    public ItemPropsArrange(long cid, long year, long month) {
        this.cid = cid;
        this.year = year;
        this.month = month;
    }
    public ItemPropsArrange(){
        
    }
    public List<PInfo> getPInfos() {
        List<PInfo> pInfos = new ArrayList<PInfo>();
        // 得到满足商品条件目录的属性
        ItemCatPlay itemCatPlay = ItemCatPlay.findByCid(cid);
        List<Long> cids = new ArrayList<Long>();
        if(!itemCatPlay.isParent){
            cids.add(this.cid);
        }else{
            cids = ItemsCatArrange.getChildrenCids(cid, year, month);
        }
        List<ItemProps> itemProps = ItemProps.getItemProps(cids, this.year, this.month);
        if (itemProps != null) {
            // 对属性进行遍历
            for (ItemProps itemProp : itemProps) {
                // 得到特定类目属性的id
                long pid = itemProp.getPid();
                // 得到特定类目属性的名称
                String pname = itemProp.getPName();
                if (pInfos.size() == 0) {
                    PInfo pName = new PInfo(pid, pname);
                    pName.list.add(new VInfo(itemProp.getVid(), itemProp.getVname(), itemProp.getSale(), itemProp
                            .getTotalPrice()));
                    pInfos.add(pName);
                } else {
                    int i = 0;
                    for (; i < pInfos.size(); ++i) {
                        if (pInfos.get(i).getPname().trim().equals(pname.trim())) {
                            VInfo vInfoTemp = new VInfo(itemProp.getVid(), itemProp.getVname(),
                                    itemProp.getSale(), itemProp.getTotalPrice());
                            List<VInfo> vInfos = new ArrayList<VInfo>(pInfos.get(i).list);
                            int j = 0;
                            for(; j < vInfos.size(); ++j){
                                if(vInfos.get(j).getName().trim().equals(vInfoTemp.getName().trim())){
                                    vInfos.get(j).setPV(vInfos.get(j).getPV() + vInfoTemp.getPV());
                                    vInfos.get(j).setTotalPrice(vInfos.get(j).getTotalPrice() + vInfoTemp.getTotalPrice());
                                    break;
                                }
                            }
                            if(j == vInfos.size())
                                pInfos.get(i).list.add(vInfoTemp);
                            break;
                        }
                    }
                    if (i == pInfos.size()) {
                        PInfo pName = new PInfo(pid, pname);
                        pName.list.add(new VInfo(itemProp.getVid(), itemProp.getVname(), itemProp.getSale(), itemProp
                                .getTotalPrice()));
                        pInfos.add(pName);
                    }
                }
            }
        }
      
        // 合并同一属性中的中有相同名称的属性信息
        Map<String, PInfo> pInfoMap = new HashMap<String, PInfo>();
        for (PInfo pInfo : pInfos) {
            // 如果含有属性相同名称
            if (pInfoMap.containsKey(pInfo.getPname())) {
                // 合并属性值信息
                List<VInfo> list1 = pInfo.getVInfo();
                if (list1 == null)
                    continue;
                for (VInfo vInfo : list1) {
                    List<VInfo> listTemp = pInfoMap.get(pInfo.getPname()).list;
                    boolean flag = true;
                    // 是否属性值相等，属性是否在其中有相等的
                    for (VInfo vTemp : listTemp) {
                        if (vTemp.getName().equals(vInfo.getName())) {
                            vTemp.setPV(vTemp.getPV() + vInfo.getPV());
                            vTemp.setTotalPrice(vTemp.getTotalPrice() + vInfo.getTotalPrice());
                            flag = false;
                            break;
                        }
                    }
                    if (flag == true) {
                        pInfoMap.get(pInfo.getPname()).list.add(vInfo);
                    }
                }
            } else
                pInfoMap.put(pInfo.getPname(), pInfo);
        }
        pInfos.clear();
        List<String> listString = new ArrayList<String>(pInfoMap.keySet());
        for (String s : listString) {
            pInfos.add(pInfoMap.get(s));
        }
        if (pInfos.size() != 0) {
        	for(int i = 0 ; i < pInfos.size(); ++i){
        		Collections.sort(pInfos.get(i).getVInfo());
        		if(pInfos.get(i).getVInfo().size() > 200){
        			pInfos.get(i).setVInfo(pInfos.get(i).getVInfo().subList(0, 200));
        		}
        	}
        }
        //只展现100个
        return pInfos;
    }

    @JsonAutoDetect
    public static class PInfo {
        @JsonProperty
        private long pid;

        @JsonProperty
        private String pname;

        @JsonProperty
        private List<VInfo> list = new ArrayList<VInfo>();

        public PInfo(long pid, String pname) {
            this.pid = pid;
            this.pname = pname;
            // this.list = list;
        }

        public long getPid() {
            return this.pid;
        }

        public void setPid(long pid) {
            this.pid = pid;
        }

        public String getPname() {
            return pname;
        }

        public void setPname(String pname) {
            this.pname = pname;
        }

        public List<VInfo> getVInfo() {
            return this.list;
        }

        public void setVInfo(List<VInfo> vInfos) {
            this.list = vInfos;
        }
    }

    @JsonAutoDetect
    public static class VInfo implements Comparable {
        @JsonProperty
        private long vid;

        @JsonProperty
        private String name;

        @JsonProperty
        private long pv;

        @JsonProperty
        private long totalPrice;

        public VInfo(long vid, String name, long pv, long totalPrice) {
            this.vid = vid;
            this.name = name;
            this.pv = pv;
            this.totalPrice = totalPrice;
        }

        public long getVid() {
            return this.vid;
        }

        public void setVid(long vid) {
            this.vid = vid;
        }

        public String getName() {
            return this.name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public long getPV() {
            return this.pv;
        }

        public void setPV(long pv) {
            this.pv = pv;
        }

        public long getTotalPrice() {
            return this.totalPrice;
        }

        public void setTotalPrice(long totalPrice) {
            this.totalPrice = totalPrice;
        }
        
		@Override
		public int compareTo(Object o) {
			return (int)(((VInfo)o).getPV() - this.pv);
		}

    }

    public class VInfoComparator implements Comparator {
        @Override
        public int compare(Object o1, Object o2) {
            VInfo vInfo1 = (VInfo) o1;
            VInfo vInfo2 = (VInfo) o2;
            if (vInfo1.getPV() < vInfo2.getPV()) {
                return 1;
            } else {
                return 0;
            }
        }

    }
    //通过父目录得到子目录的信息
    public static List<PInfo> getPInfosByParentCid(long cid, long year, long month){
        List<PInfo> pInfos = new ArrayList<PInfo>();
        //得到所有子目录的cid
        List<Long> childrenCids = ItemsCatArrange.getChildrenCids(cid, year, month);
        for(long childCid:childrenCids){
            List<PInfo> pInfo = new ItemPropsArrange(childCid, year, month).getPInfos();
            pInfos.addAll(pInfo);   
        }
        
        // 合并同一属性中的中有相同名称的属性信息
        Map<String, PInfo> pInfoMap = new HashMap<String, PInfo>();
        for (PInfo pInfo : pInfos) {
            // 如果含有属性相同名称
            if (pInfoMap.containsKey(pInfo.getPname())) {
                // 合并属性值信息
                List<VInfo> list1 = pInfo.getVInfo();
                if (list1 == null)
                    continue;
                for (VInfo vInfo : list1) {
                    List<VInfo> listTemp = pInfoMap.get(pInfo.getPname()).list;
                    boolean flag = true;
                    // 是否属性值相等，属性是否在其中有相等的
                    for (VInfo vTemp : listTemp) {
                        if (vTemp.getName().equals(vInfo.getName())) {
                            vTemp.setPV(vTemp.getPV() + vInfo.getPV());
                            vTemp.setTotalPrice(vTemp.getTotalPrice() + vInfo.getTotalPrice());
                            flag = false;
                            break;
                        }
                    }
                    if (flag == true) {
                        pInfoMap.get(pInfo.getPname()).list.add(vInfo);
                    }
                }
            } else
                pInfoMap.put(pInfo.getPname(), pInfo);
        }
        pInfos.clear();
        List<String> listString = new ArrayList<String>(pInfoMap.keySet());
        for (String s : listString) {
            pInfos.add(pInfoMap.get(s));
        }
        if (pInfos.size() != 0) {
            ItemPropsArrange itemPropsArrange = new ItemPropsArrange();
            VInfoComparator comp = itemPropsArrange.new VInfoComparator();
            for (PInfo pInfo : pInfos) {
                Collections.sort(pInfo.getVInfo(), comp);
            }
        }
        return pInfos;
    }

}
