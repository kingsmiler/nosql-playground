package org.xman.nosql.sort;

import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

/**
 * lexicographical order
 */
public class DicTester {
    private ArrayList<String> itemList;

    @Before
    public void before() {
        itemList = new ArrayList<>();

        itemList.add("110");
        itemList.add("东海湾");
        itemList.add("傲来");
        itemList.add("AoLai");
        itemList.add("东海湾-岩洞");
        itemList.add("傲来药店");
        itemList.add("南海");
        itemList.add("NanHai");
        itemList.add("西海");
        itemList.add("中华");
    }


    @Test
    public void testDefaultSort() {
        Collections.sort(itemList);

        itemList.forEach(System.out::println);
    }

    @Test
    public void testChineseSort() {
        Collections.sort(itemList, new ChineseComparator());

        itemList.forEach(System.out::println);
    }


    class ChineseComparator implements Comparator<String> {
        private static final String ENCODING = "GBK";

        @Override
        public int compare(String o1, String o2) {
            try {
                // 取得比较对象的汉字编码，并将其转换成字符串
                String s1 = new String(o1.getBytes(ENCODING), "ISO-8859-1");
                String s2 = new String(o2.getBytes(ENCODING), "ISO-8859-1");
                // 运用String类的 compareTo（）方法对两对象进行比较
                return s1.compareTo(s2);
            } catch (Exception e) {
                e.printStackTrace();
            }
            return 0;
        }
    }

}
