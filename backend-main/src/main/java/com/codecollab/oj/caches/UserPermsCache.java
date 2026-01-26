package com.codecollab.oj.caches;

import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
public class UserPermsCache {
    private final Map<Integer, List<String>> userPermsCache;
    public UserPermsCache(){
        userPermsCache = new HashMap<>();
    }

    public List<String> getPerms(Integer userId){
        return userPermsCache.get(userId);
    }
    public void addPerms(Integer userId, List<String> perms){
        userPermsCache.put(userId,perms);
    }


}
