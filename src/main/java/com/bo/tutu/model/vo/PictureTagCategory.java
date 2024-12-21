package com.bo.tutu.model.vo;

import lombok.Data;

import java.io.Serializable;
import java.util.List;

@Data
public class PictureTagCategory implements Serializable {
    /**
     * 标签
     */
    private List<String> TagList;

    /**
     * 分类
     */
    private List<String> CategoryList;
}
