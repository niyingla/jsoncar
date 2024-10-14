package com.github.jsoncat.common.util;

import com.github.demo.user.Gender;

import java.beans.PropertyEditor;
import java.beans.PropertyEditorManager;

/**
 * @author shuang.kou
 * @createTime 2020年09月25日 15:52:00
 **/
public class ObjectUtil {
    /**
     * 通过 PropertyEditor 转化 string 内容 到 targetType的类型
     * convert from String to a target type
     *
     * @param targetType the type to be converted 被转换后的类型
     * @param s          the string to be converted 被转化前的类型
     * @throws NumberFormatException When string to number, if string is not a number,then throw NumberFormatException
     */
    public static Object convert(Class<?> targetType, String s) {
        PropertyEditor editor = PropertyEditorManager.findEditor(targetType);
        editor.setAsText(s);
        return editor.getValue();
    }

    public static void main(String[] args) {
        System.out.println(ObjectUtil.convert(String.class, "1"));
        System.out.println(ObjectUtil.convert(Integer.class, "1"));
        System.out.println(ObjectUtil.convert(Boolean.class, "true"));
        System.out.println(ObjectUtil.convert(Gender.class, "MALE"));
    }
}
