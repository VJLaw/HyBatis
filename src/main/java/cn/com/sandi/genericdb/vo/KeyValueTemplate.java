package cn.com.sandi.genericdb.vo;

import lombok.Data;

@Data
public class KeyValueTemplate{
    private String key;

    private Object value;

    private String operating;

    private String type;
}

