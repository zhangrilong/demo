package com.example.demo.task;

import com.example.demo.config.UserConfig;
import com.example.demo.service.JiNanService;
import com.example.demo.vo.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Component
public class JiNanTask {
    @Autowired
    UserConfig userConfig;

    private static ExecutorService executorService = Executors.newFixedThreadPool(10);

    @Scheduled(cron = "59 59 4 * * *")
    public void getSear() {

        List<User> users = userConfig.getUsers();
        for (User user : users) {
            executorService.submit(new JiNanService(user));
        }
    }
}
