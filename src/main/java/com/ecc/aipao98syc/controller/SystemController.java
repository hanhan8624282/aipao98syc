package com.ecc.aipao98syc.controller;



import com.ecc.aipao98syc.dao.SignupDetailDao;
import com.ecc.aipao98syc.dao.SmsLogDao;
import com.ecc.aipao98syc.until.NetworkUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;

import javax.imageio.ImageIO;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * @author sunyc
 * @create 2022-08-16 8:14
 */
@RestController
@Slf4j
public class SystemController {

    @Autowired
    private SignupDetailDao signupDetailDao;//自动注入报名表

    @Autowired
    private SmsLogDao smsLogDao;

    @GetMapping
    @RequestMapping("/index/{provinceid}")
    public ModelAndView index(ModelAndView modelAndView,
                              @PathVariable String provinceid,
                              HttpServletRequest request){
        //用户来源省份id
        String ipAddress = NetworkUtil.getIpAddress(request);
        request.getSession().setAttribute("provinceid:"+ipAddress,provinceid);
        log.error("----SystemController.index---首页---来源省份："+provinceid);
        modelAndView.setViewName("index");
        return modelAndView;
    }


}
