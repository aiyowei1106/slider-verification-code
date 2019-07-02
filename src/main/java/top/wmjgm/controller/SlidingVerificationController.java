package top.wmjgm.controller;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.CacheManager;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import top.wmjgm.response.ResultData;
import top.wmjgm.service.SlidingVerificationService;

/**
 * @author hanne hll941106@163.com
 * @date 2019-07-02
 **/
@RestController
@RequestMapping(value = "/verification")
public class SlidingVerificationController {

    @Autowired
    private SlidingVerificationService slidingVerificationService;
    @Autowired
    private CacheManager cacheManager;

    /**
     * 滑块验证码获取
     * @return 返回参数：tari(目标图)，orii(原图)，slii(滑块)，coox(滑块x坐标)，cooy(滑块y坐标)，chet(滑块校验token)
     */
    @GetMapping("/sliding-verification")
    public Object slidingVerificationImage(){
        return ResponseEntity.ok(ResultData.builder().code(HttpStatus.OK.value())
                .data(slidingVerificationService.slidingVerificationImage()).timestamp(System.currentTimeMillis()).build());
    }

    /**
     * 验证码校验
     * @param chet 滑块校验令牌token
     * @param coox 滑块滑动后的x坐标
     * @param cooy 滑块滑动后的y坐标
     * @return
     */
    @PostMapping("/sliding-check")
    public Object slidingCheck(@RequestParam("chet") String chet, @RequestParam("coox") int coox, @RequestParam("cooy") int cooy){
        String secretKey = cacheManager.getCache("token").get(chet, String.class);
        if(StringUtils.isEmpty(chet) || StringUtils.isEmpty(secretKey)){
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ResultData.builder().code(HttpStatus.UNAUTHORIZED.value())
                            .message("请求错误，请检查请求参数！").timestamp(System.currentTimeMillis()).build());
        }
        return slidingVerificationService.slidingCheck(chet, secretKey, coox, cooy);
    }
}
