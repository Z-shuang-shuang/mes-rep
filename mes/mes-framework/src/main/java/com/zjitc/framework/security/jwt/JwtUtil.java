package com.zjitc.framework.security.jwt;


import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;


@Component
public class JwtUtil {

    private long expiration = 24 * 60 * 60 * 1000;
    private String secret = "11111111111111111111111111111111";

    /**
     * 生成token
     * @param userid
     * @param username
     * @return
     */
    public String generateToken(String userid, String username){

        Date now = new Date();//现在时间
        Date expiryDate = new Date(now.getTime() + expiration);//过期时间

        //token的载荷
        Map<String,Object> map = new HashMap<>();
//        map.put("userid",userid);
        map.put("username",username);

        //token签名的秘钥
        SecretKey key = Keys.hmacShaKeyFor(secret.getBytes());

        String token = Jwts.builder()
                .setSubject(userid)//载荷的sub键
                .addClaims(map)
                .setIssuedAt(now)
                .setExpiration(expiryDate)
                .signWith(key,  SignatureAlgorithm.HS256)
                .compact();
        return token;
    }

    /**
     * 从token中解析出Claims
     * @param token
     * @return
     */
    public Claims parseToken(String token){
        SecretKey key = Keys.hmacShaKeyFor(secret.getBytes());

        // 【唯一修改点】将错误的 Jwts.SetSignKey 替换为标准的 parserBuilder 链式调用
        return Jwts.parserBuilder()
                .setSigningKey(key)
                .build()
                .parseClaimsJws(token)
                .getBody();
    }


    /**
     * 从Claims中取出userid
     * @param token
     * @return
     */
    public String getUserId(String token){
        return parseToken(token).getSubject();
    }

    /**
     * 从CLaims中取出username
     * @param token
     * @return
     */
    public String getUsername(String token){
        return parseToken(token).get("username", String.class);
    }

    /**
     * 取出其他存在Claims中的键值对（按照键来取值）
     * @param token
     * @param name
     * @return
     */
    public String getData(String token, String name){
        return parseToken(token).get(name, String.class);
    }

    /**
     * 判断token是否过期
     * @param token
     * @return
     */
    public boolean isExp(String token){
        return parseToken(token).getExpiration().before(new Date());
    }




}
