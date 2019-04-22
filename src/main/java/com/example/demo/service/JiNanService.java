package com.example.demo.service;

import com.alibaba.fastjson.JSONObject;
import com.example.demo.task.JiNanTask;
import com.example.demo.util.HttpUtil;
import com.example.demo.vo.User;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDate;
import java.util.HashMap;

public class JiNanService implements Runnable {
    Logger logger = LoggerFactory.getLogger(JiNanService.class);

    private User user;
    private int max_getToken_times = 30;
    private int max_getSeat_times = 30;

    public JiNanService(User user) {
        this.user = user;
    }

    private String getToken() {
        max_getToken_times--;
        String token = "";
        String url = "http://seat.ujn.edu.cn/rest/auth" + "?username=" + user.getName() + "&password=" + user.getPaswd();
        String s = HttpUtil.sendGet(url);
        JSONObject jsonObject = JSONObject.parseObject(s);

        while (max_getToken_times > 0 && StringUtils.equals("System Maintenance", jsonObject.getString("message"))) {
            logger.info("系统维护中,等待10秒重试");
            try {
                max_getToken_times--;
                Thread.sleep(3);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            s = HttpUtil.sendGet(url);
            jsonObject = JSONObject.parseObject(s);
        }

        if (StringUtils.equals("fail", jsonObject.getString("status"))) {
            token = getToken();
        } else {
            JSONObject data = jsonObject.getJSONObject("data");
            token = data.getString("token");
        }
        logger.info("getToken:" +url+ token);
        return token;
    }

    private void getSeat() throws InterruptedException {

        max_getSeat_times--;
        String token = getToken();
        HashMap<String, Object> map = new HashMap<>();
        map.put("date", LocalDate.now());
        map.put("seat", user.getSeat());
        if (StringUtils.equals(String.valueOf(LocalDate.now().getDayOfWeek()), "MONDAY")) {
            map.put("startTime", 960);
        } else {
            map.put("startTime", 540);
        }
        map.put("endTime", 1320);
        map.put("token", token);

        String s = HttpUtil.sendPost("http://seat.ujn.edu.cn/rest/v2/freeBook", map, null);
        logger.info("getSeat:" + map + s);

        JSONObject jsonObject = JSONObject.parseObject(s);
        String message = jsonObject.getString("message");
        while (max_getSeat_times > 0 && StringUtils.equals("系统可预约时间为 05:00 ~ 23:00", message)) {
            max_getSeat_times--;
            Thread.sleep(3);
            s = HttpUtil.sendPost("http://seat.ujn.edu.cn/rest/v2/freeBook", map, null);
            jsonObject = JSONObject.parseObject(s);
            message = jsonObject.getString("message");
        }
        if (StringUtils.equals("已有1个有效预约，请在使用结束后再次进行选择", message)) {
            return;
        }
        if (StringUtils.equals("登录失败: 用户名或密码不正确", message)) {
            getSeat();
        }
    }

    @Override
    public void run() {
        try {
            getSeat();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {

    }
}
