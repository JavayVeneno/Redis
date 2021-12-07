package com.veneno.RateLimiter;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class FunnelRateLimiter {

    static class Funnel{

        int capacity; //漏斗容积
        float leakingRate; //漏失率
        int leftQuota; // 剩余空间
        long leakingTs; //

        public Funnel(int capacity,float leakRate){
            this.capacity= capacity;
            this.leakingRate = leakRate;
            this.leftQuota = capacity;
            this.leakingTs = System.currentTimeMillis();
        }

        // 触发漏水,以释放漏斗中的空间
        void makeSpace(){
            long nowTs = System.currentTimeMillis();
            long deltaTs = nowTs-leakingTs;//漏水时长
            int deltaQuota = (int)(deltaTs * leakingRate);//释放空间
            // 为负数表明漏水时长为负数,还未开始(或者间隔时间长整型溢出),重置
            if(deltaQuota<0){
                this.leftQuota = capacity;
                this.leakingTs = nowTs;
                return ;
            }
            // 小于1,最少为1,释放空间太少
            if(deltaQuota<1){
                return ;
            }
            //除此之外,剩余空间将累加计算结果,并重置时间
            this.leftQuota += deltaQuota;
            this.leakingTs = nowTs;
            //释放空间累不得大于容积
            if(this.leftQuota>this.capacity){
                this.leftQuota = this.capacity;
            }
        }
        // 灌水
        boolean watering(int quota) {
            makeSpace();
            if (this.leftQuota >= quota) {
                this.leftQuota -= quota;
                return true;
            }
            return false;
        }
    }
    private Map<String, Funnel> funnels = new HashMap<>();

    public boolean isActionAllowed(String userId, String actionKey, int capacity, float leakingRate) {
        String key = String.format("%s:%s", userId, actionKey);
        Funnel funnel = funnels.get(key);
        if (funnel == null) {
            funnel = new Funnel(capacity, leakingRate);
            funnels.put(key, funnel);
        }
        return funnel.watering(1); // 需要1个quota
    }

    public static void main(String[] args) {
        FunnelRateLimiter funnelRateLimiter = new FunnelRateLimiter();
        ExecutorService es = Executors.newFixedThreadPool(20);



        for (int i = 0; i < 20; i++) {
            es.execute(()->{
                System.out.println(
                        funnelRateLimiter.isActionAllowed
                                ("老刘", "看书", 1, 0.9f));


            });

        }
        es.shutdown();
    }
}
