package org.xman.nosql.sort;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

/**
 * lexicographical order
 */
public class DicTester {

    public static void main(String[] args) {
        ArrayList<String> list = new ArrayList<>();
        list.add("110");
        list.add("东海湾");
        list.add("傲来");
        list.add("AoLai");
        list.add("东海湾-岩洞");
        list.add("傲来药店");
        list.add("南海");
        list.add("NanHai");
        list.add("西海");
        list.add("中华");

        //运用Collections的sort（）方法对其进行排序 sort（）方法需要传 连个参数，
        // 一个是需要进行排序的Collection 另一个是一个Comparator 。

        Collections.sort(list, new SpellComparator());
        for (int i = 0; i < list.size(); i++) {
            System.out.println(list.get(i));
        }
    }
}

class SpellComparator implements Comparator<String> {
    private static final String ENCODING = "GB2312";

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