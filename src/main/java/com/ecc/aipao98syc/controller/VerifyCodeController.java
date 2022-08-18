package com.ecc.aipao98syc.controller;




import com.ecc.aipao98syc.dao.SignupDetailDao;
import com.ecc.aipao98syc.dao.SmsLogDao;
import com.ecc.aipao98syc.pojo.SignupDetail;
import com.ecc.aipao98syc.pojo.SmsLog;
import com.ecc.aipao98syc.until.HttpResult;
import com.ecc.aipao98syc.until.ImageUtil;
import com.ecc.aipao98syc.until.NetworkUtil;
import com.ecc.aipao98syc.until.R;
import com.google.gson.Gson;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.RandomStringUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.*;


@RestController
@Slf4j
public class VerifyCodeController {
    @Autowired
    private SignupDetailDao signupDetailDao;//自动注入报名表

    @Autowired
    private SmsLogDao smsLogDao;

    /**
     * 去除 l、o、I、O、0、1
     */
    private static final char[] CHAR_ARRAY = "abcdefghijkmnpqrstuvwxyzABCDEFGHJKLMNPQRSTUVWXYZ23456789".toCharArray();
    /**登录图片验证码
     * @param session
     * @return
     */
    @GetMapping("/getVerifyImage")
    public void getVerifyImage(HttpServletRequest request, HttpServletResponse res, HttpSession session) throws IOException {

        String checkCode = RandomStringUtils.random(4, CHAR_ARRAY);
        //获取访问用户的ip地址
        String ipAddress = NetworkUtil.getIpAddress(request);
        log.error("----getVerifyImage-- 获取用户的ip地址："+ipAddress);
        log.error("----getVerifyImage-- 开始生产验证码checkCode="+checkCode);

        //TODO 直接返回byte数据
        ByteArrayOutputStream os = (ByteArrayOutputStream) ImageUtil.generator(102, 25, checkCode);
        byte[] bytes = os.toByteArray();
        try {
            os.close();
        } catch (IOException e) {
        }
        session.setAttribute("checkCode:"+ipAddress, checkCode);//将验证码值存入HttpSession中
        log.error("----getVerifyImage--存入session域中：=checkCode:"+ipAddress);
        res.setContentType("image/jpg");
        res.setContentLength(bytes.length);
        res.getOutputStream().write(bytes);
    }

    @GetMapping("/getCheckCode")//该方法为验证码值的获取方法
    public Object getCheckCode(HttpSession session,HttpServletRequest request) {
        //获取访问用户的ip地址
        String ipAddress = NetworkUtil.getIpAddress(request);
        log.error("获取验证码=="+session.getAttribute("checkCode:"+ipAddress));
        return session.getAttribute("checkCode");
    }
    //获取手机验证码以及报名
    @PostMapping
    @RequestMapping("/VerifiCodeImageCheck")
    public R checkCode(HttpServletRequest request,HttpServletResponse response){
        log.error("--------VerifyCodeController.checkCode--- 开始验证check----------- 。。。。。。");
        //获取用户输入的图片验证码
        String userLoginCode = request.getParameter("code");
        //用户的手机号
        String userPhone = request.getParameter("phone");
        //获取用户的手机验证码
        String phonecode = request.getParameter("phonecode");
        //status="0";//表示获取手机验证码  status="1";//表示报名
        String status = request.getParameter("status");

        //获取访问用户的ip地址
        String ipAddress = NetworkUtil.getIpAddress(request);

        //获取保存在session域中的图片验证码
        String sessionCode = (String)request.getSession().getAttribute("checkCode:"+ipAddress);
        //日志
        log.error("-----VerifyCodeController.checkCode 。。。获取用户输入的图片验证码:"+userLoginCode);
        log.error("-----VerifyCodeController.checkCode 。。。sessionCode的图片验证码:"+sessionCode);

        log.error("-----VerifyCodeController.checkCode 。。。用户的手机号:"+userPhone);
        log.error("-----VerifyCodeController.checkCode 。。。获取用户的手机验证码:"+phonecode);
        log.error("-----VerifyCodeController.checkCode 。。。status=0,表示获取手机验证码.status=1;//表示报名:"+status);


        //判断图片验证码是否为空
        if(userLoginCode==null || sessionCode==null){
            log.error("-----VerifyCodeController.checkCode------ 验证码为空，或无效:");

            return R.error().message("验证码为空，或无效。。");
            //return Result.fail().message("验证码为空，或无效。。");
        }
        //判断图片验证码是否通过
        if(!sessionCode.equalsIgnoreCase(userLoginCode)){
            log.error("-----VerifyCodeController.checkCode------ 验证码输入错误请再次输入:");
            return R.error().message("验证码输入错误请再次输入。。。");
        }


        //创建一个map 存放用户数
        Map<String,Object> map=new LinkedHashMap<String,Object>();
        //如果status=o 并且phonecode 为空 那么就走获取手机验证码的逻辑
        if("0".equals(status) && StringUtils.isBlank(phonecode)){
            //判断该手机号-每日获取的验证码次数
            int codeCount = smsLogDao.checkCount(userPhone);
            if(codeCount>5){
                log.error("-----VerifyCodeController.checkCode------ 每日获取的验证码次数达到上限:");
                return R.error().message("每日获取的验证码次数达到上限。。。");
            }
            //验证码五分钟有效; 0=超过五分钟

            int flagcount = smsLogDao.codeFlag(userPhone);
            if(flagcount>0){
                log.error("-----VerifyCodeController.checkCode------ 上个验证码五分钟有效:");
                return R.error().message("上个验证码五分钟有效。。。");
            }
            //调用短信接口发送短信
            //生成验证码
            int messageCode= (int)((Math.random()*9+1)*1000);
            System.out.println("code="+messageCode);
            log.error("-----VerifyCodeController.checkCode------ 生成手机验证码:"+messageCode);
            //模拟短信调用success
            HttpResult httpResult=new HttpResult();
            httpResult.setResultCode("1");//返回错误代码。1-表示成功
            httpResult.setResultMsg("success");
            httpResult.setStatusCode(1);//返回错误代码。1-表示成功
            //success
            if("1".equals(httpResult.getResultCode())){
                log.error("短信接口调用成功。。。");
                SmsLog smsLog=new SmsLog();
                smsLog.setCode(messageCode);
                smsLog.setCellPhone(userPhone);
                smsLog.setCreateTime(new Date());
                smsLog.setIp(ipAddress);
                smsLog.setResultId("1234567890");
                smsLog.setResultMessage("提交成功");
                smsLog.setResultStatus(String.valueOf(httpResult.getStatusCode()));
                int insert = smsLogDao.insert(smsLog);

                map.put("smslog", smsLog);

                if(insert>0){
                    log.error("------VerifyCodeController.checkCode----- 插入短信流水成功。。。");

                }
            }else{//调用接口失败
                log.error("-------VerifyCodeController.checkCode---- 。。。短信接口调用失败。。。");
                return R.error().message("短信接口调用失败...");
            }
            return   R.ok().data("map",map);

        }
        //如果status=1 并且phonecode 不为空 那么就走获取手机验证码的逻辑
        if ("1".equals(status) && StringUtils.isNotBlank(phonecode)){
            //手机验证码的check
            int checkCode = smsLogDao.checkPhoneFlag(userPhone, phonecode);
            if(checkCode>0){
                //查看该用户是否报名
                int phoneCount = signupDetailDao.checkPhone(userPhone);
                if(phoneCount>0){
                    log.error("-----VerifyCodeController.checkCode------ 该用户手机:"+userPhone+"-已报名");
                    try {
                        response.sendRedirect("/index/"+ipAddress);
                    } catch (IOException e) {
                        new RuntimeException("已报名，转发请求到首页。。");
                    }
                    return R.error().message("该用户手机-已报名");
                }else {
                    SignupDetail signupDetail=new SignupDetail();
                    signupDetail.setCellPhone(userPhone);
                    signupDetail.setCreateDate(new Date());
                    String provinceid = String.valueOf(request.getSession().getAttribute("provinceid:"+ipAddress));
                    log.error("-----VerifyCodeController.checkCode------ 用户来源省份:"+provinceid);
                    signupDetail.setSourceChannel("wechat");
                    signupDetail.setProvinceId(provinceid);
                    log.error("-----VerifyCodeController.checkCode-----报名用户的信息："+signupDetail);
                    int insert = signupDetailDao.insert(signupDetail);
                    if(insert>0){
                        //验证通过后清除掉session域中的验证码
                        request.getSession().removeAttribute("checkCodecheckCode:"+ipAddress);
                        log.error("------VerifyCodeController.checkCode------报名成功----"+userPhone);
                        return R.ok().data("signupDetail",signupDetail);
                    }
                    log.error("------VerifyCodeController.checkCode------系统错误----"+userPhone);
                    return R.error().message("------------系统错误----");

                }

            }


        }



        return   R.ok();
    }

}