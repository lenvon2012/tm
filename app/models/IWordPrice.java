
package models;


public interface IWordPrice {

    public String getWord();

    public Integer getPrice();

    public Long getAdGroupId();
//
//    public static class Convert {
//        public static List<IWordPrice> toList(SubmitItemKeyWordsPrice[] array) {
//            if (ArrayUtils.isEmpty(array)) {
//                return ListUtils.EMPTY_LIST;
//            }
//            List<IWordPrice> res = new ArrayList<IWordPrice>(array.length);
//            for (SubmitItemKeyWordsPrice submitItemKeyWordsPrice : array) {
//                res.add(submitItemKeyWordsPrice);
//            }
//            return res;
//        }
//    }
}
