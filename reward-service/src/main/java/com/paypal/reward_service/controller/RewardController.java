package com.paypal.reward_service.controller;

import com.paypal.reward_service.entity.Reward;
import com.paypal.reward_service.service.RewardService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/reward")
public class RewardController {


    @Autowired
    private RewardService rewardService;


    @PostMapping
    public Reward sendReward(@RequestBody Reward reward){
        return rewardService.sendReward(reward);
    }

    @GetMapping("/{userId}")
    public List<Reward> getRewardByUserId(@PathVariable String userId){
        return  rewardService.getRewardByUserId(userId);
    }
}
