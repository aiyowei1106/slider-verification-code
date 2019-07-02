package top.wmjgm.service;

/**
 * @author hanne hll941106@163.com
 * @data 2019-07-02
 **/
public interface SlidingVerificationService {

    /**
     * 滑块验证码获取
     * @return
     */
    Object slidingVerificationImage();

    /**
     * 校验
     * @param token
     * @param secretKey
     * @param coox X坐标
     * @param cooy Y坐标
     * @return
     */
    Object slidingCheck(String token, String secretKey, int coox, int cooy);
}
