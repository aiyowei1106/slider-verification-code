package top.wmjgm.response;

import lombok.Builder;
import lombok.Data;
import lombok.ToString;

import java.util.Date;

/**
 * 结果返回
 * @author hanne hll941106@163.com
 * @date 2019-07-02
 **/
@Builder
@Data
@ToString
public class ResultData {

    /**
     * 返回状态码
     */
    private Integer code;

    /**
     * 返回信息,一般为请求失败时返回
     */
    private String message;

    /**
     * 返回数据
     */
    private Object data;

    /**
     * 系统当前时间戳
     */
    private Long timestamp = System.currentTimeMillis();
}
