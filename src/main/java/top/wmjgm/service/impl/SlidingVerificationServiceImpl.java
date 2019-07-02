package top.wmjgm.service.impl;

import io.jsonwebtoken.Claims;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.CacheManager;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import top.wmjgm.response.ResultData;
import top.wmjgm.service.SlidingVerificationService;
import top.wmjgm.utils.JWTUtil;
import top.wmjgm.utils.SlidingVerificationUtil;

import java.io.IOException;
import java.util.Map;

/**
 * @author hanne hll941106@163.com
 * @date 2019-07-02
 **/
@Service
public class SlidingVerificationServiceImpl implements SlidingVerificationService {

    @Autowired
    private CacheManager cacheManager;

    /**
     * 校验
     * @param token 校验token令牌，通过解析获取验证码数据
     * @param secretKey 秘钥 解析token
     * @param coox X坐标
     * @param cooy Y坐标
     * @return
     */
    @Override
    public ResultData slidingCheck(String token, String secretKey, int coox, int cooy){
        if(JWTUtil.isTokenExpired(token, secretKey)){
            return ResultData.builder().code(HttpStatus.UNAUTHORIZED.value()).message("验证码已过期，请刷新重试！")
                    .timestamp(System.currentTimeMillis()).build();
        }else {
            if(JWTUtil.parseToken(token, secretKey)){
                Claims claims = JWTUtil.getClaimFromToken(token, secretKey);
                int Y = Integer.parseInt(claims.get("cooy").toString());
                int X = Integer.parseInt(claims.get("coox").toString());
                if (Y != cooy) {
                    return ResultData.builder().code(HttpStatus.UNAUTHORIZED.value()).message("验证不通过，请重试！")
                            .timestamp(System.currentTimeMillis()).build();
                } else {
                    if (Math.abs(X - coox) <= 6) {
                        return ResultData.builder().code(HttpStatus.OK.value()).message("验证通过！")
                                .timestamp(System.currentTimeMillis()).build();
                    } else {
                        return ResultData.builder().code(HttpStatus.UNAUTHORIZED.value()).message("验证不通过，请重试！")
                                .timestamp(System.currentTimeMillis()).build();
                    }
                }
            }else {
                return ResultData.builder().code(HttpStatus.UNAUTHORIZED.value()).message("请求失败，请重试！")
                        .timestamp(System.currentTimeMillis()).build();
            }
        }
    }

    /**
     * 滑块验证码获取
     * @return
     */
    @Override
    public Map<String, Object> slidingVerificationImage(){
        Map<String, Object> verificationImages = null;
        try {
            String randomKey = JWTUtil.getRandomKey();
            verificationImages = SlidingVerificationUtil.createSlidingVerificationImages(randomKey);
            cacheManager.getCache("token").put(verificationImages.get("chet"), randomKey);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return verificationImages;
    }
}
