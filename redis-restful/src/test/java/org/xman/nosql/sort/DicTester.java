package org.xman.nosql.sort;

import org.junit.Test;

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

        Collections.sort(list);
        list.forEach(System.out::println);

        System.out.println("=========");

        Collections.sort(list, new SpellComparator());
        for (int i = 0; i < list.size(); i++) {
            System.out.println(list.get(i));
        }
    }

    @Test
    public void test() {
        String s1="abc";
        String s2="abcd";
        System.out.println("s1.compareTo(s2)"+s1.compareTo(s2));//前缀相同则比较长度，长度差-1
        System.out.println("s1.compareTo(s2)"+s1.compareTo("abcdefgh"));//长度差-5
        String s3="abc";
        String s4="ae";
        System.out.println("s3.compareTo(s4)"+s3.compareTo(s4));//只比较第一个不同的字符处b-e=-3
        String s5="abcdeg";
        String s6="acce";
        System.out.println("s5.compareTo(s6)"+s5.compareTo(s6));//b-c=-1
        String s7="abc";
        String s8="abb";
        System.out.println("s7.compareTo(s8)"+s7.compareTo(s8));//c-b=1
        String s9="abc";
        String s0="abaaaaaaaaaaaaa";
        System.out.println("s9.compareTo(s0)"+s9.compareTo(s0));//c-a=2只比较第一个不同的字符处，与长度无关

        String sa="奥";
        System.out.println("奥.compareTo(中)"+sa.compareTo("中"));
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