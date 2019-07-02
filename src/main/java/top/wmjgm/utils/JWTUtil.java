package top.wmjgm.utils;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Date;
import java.util.Map;
import java.util.Random;

/**
 * JWT工具
 * @author hanne hll941106@163.com
 * @date 2019-07-02
 **/
public class JWTUtil {

    private static final String MD5_SALT = "http://www.magicmote.com";

    /**
     * 获取用户名从token中
     * @param token
     * @param secretKey
     * @return
     */
    public static String getUsernameFromToken(String token, String secretKey) {
        return getClaimFromToken(token, secretKey).getSubject();
    }

    /**
     * 获取jwt发布时间
     * @param token
     * @param secretKey
     * @return
     */
    public static Date getIssuedAtDateFromToken(String token, String secretKey) {
        return getClaimFromToken(token, secretKey).getIssuedAt();
    }

    /**
     * 获取jwt失效时间
     * @param token
     * @param secretKey
     * @return
     */
    public static Date getExpirationDateFromToken(String token, String secretKey) {
        return getClaimFromToken(token, secretKey).getExpiration();
    }

    /**
     * 获取jwt接收者
     * @param token
     * @param secretKey
     * @return
     */
    public static String getAudienceFromToken(String token, String secretKey) {
        return getClaimFromToken(token, secretKey).getAudience();
    }

    /**
     * 获取私有的jwt claim
     * @param token
     * @param key
     * @param secretKey
     * @return
     */
    public static String getPrivateClaimFromToken(String token, String key, String secretKey) {
        return getClaimFromToken(token, secretKey).get(key).toString();
    }

    /**
     * 获取jwt的payload部分
     * @param token
     * @param secretKey
     * @return
     */
    public static Claims getClaimFromToken(String token, String secretKey) {
        return Jwts.parser()
                .setSigningKey(secretKey)
                .parseClaimsJws(token)
                .getBody();
    }

    /**
     * 解析token是否正确,不正确会报异常<br>
     * @param token
     * @param secretKey
     * @return
     */
    public static Boolean parseToken(String token, String secretKey){
        try{
            Jwts.parser().setSigningKey(secretKey).parseClaimsJws(token).getBody();
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * <pre>
     *  验证token是否失效
     *  true:过期   false:没过期
     * </pre>
     * @param token
     * @param secretKey
     * @return
     */
    public static Boolean isTokenExpired(String token, String secretKey) {
        try {
            final Date expiration = getExpirationDateFromToken(token, secretKey);
            return expiration.before(new Date());
        } catch (Exception e) {
            e.printStackTrace();
            return true;
        }
    }

    /**
     * 生成token(通过用户名和签名时候用的随机数)
     * @param claims 数据
     * @param subject 用作标识,可以是用户名,也可以是null
     * @param secretKey
     * @return
     */
    public static String generateToken(Map<String, Object> claims, String subject, String secretKey, long tokenEXP) {
        return doGenerateToken(claims, subject, secretKey, tokenEXP);
    }

    /**
     * 生成token
     * @param claims 数据声明payload
     * @param subject 用作标识,可以是用户名,也可以是null
     * @param secretKey 进行签名的秘钥
     * @return
     */
    private static String doGenerateToken(Map<String, Object> claims, String subject, String secretKey, long tokenEXP) {
        final Date createdDate = new Date();
        final Date expirationDate = new Date(createdDate.getTime() + tokenEXP);

        return Jwts.builder()
                .setClaims(claims)
                .setSubject(subject)
                .setIssuedAt(createdDate)
                .setExpiration(expirationDate)
                .signWith(SignatureAlgorithm.HS512, secretKey)
                .compact();
    }

    /**
     * 随机生成字符串MD5签名
     * @return
     */
    public static String getRandomKey() {
        return getRandomString(32);
    }

    /**
     * 随机生成MD5加密字符串
     * @param length
     * @return
     */
    public static String getRandomString(int length) {
        String base = "abcdefghijklmnopqrstuvwxyz0123456789";
        Random random = new Random();
        StringBuffer sb = new StringBuffer();

        for(int i = 0; i < length; ++i) {
            int number = random.nextInt(base.length());
            sb.append(base.charAt(number));
        }

        return sb.toString();
    }

    /**
     * 原项目的MD5加密密码
     * @param paramString
     * @return
     */
    public static String md5(String paramString)
    {
        if (paramString == null) {
            return null;
        }

        try {
            paramString = MD5_SALT + paramString;
            StringBuffer localStringBuffer = new StringBuffer();
            for (byte b : MessageDigest.getInstance("MD5").digest(paramString.getBytes())) {
                localStringBuffer.append(String.format("%02x", new Object[] { Byte.valueOf(b) }));
            }
            String s = localStringBuffer.toString().toUpperCase();
            System.out.println(s);
            return s;
        }
        catch (NoSuchAlgorithmException localNoSuchAlgorithmException) {
            throw new RuntimeException("can't covert to md5!", localNoSuchAlgorithmException);
        }
    }
}
