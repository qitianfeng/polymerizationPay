package com.polymerization.merchant.vo;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;

@ApiModel(value = "FastDFSFile", description = "文件信息")
@Data
public class FastDFSFile implements Serializable {

    //文件名字
    @ApiModelProperty("文件名字")
    private String name;


    //文件内容
    @ApiModelProperty("文件内容")
    private byte[] content;

    //文件扩展名
    @ApiModelProperty("文件扩展名")
    private String ext;

    //文件MD5摘要值
    @ApiModelProperty("文件MD5摘要值")
    private String md5;

    //文件创建作者
    @ApiModelProperty("文件创建作者")
    private String author;

    public FastDFSFile(String name, byte[] content, String ext, String md5, String author) {
        this.name = name;
        this.content = content;
        this.ext = ext;
        this.md5 = md5;
        this.author = author;
    }

    public FastDFSFile(String name, byte[] content, String ext) {
        this.name = name;
        this.content = content;
        this.ext = ext;
    }

    public FastDFSFile() {
    }


}
