package com.xuecheng.ucenter.service.impl;

import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.xuecheng.ucenter.mapper.XcUserMapper;
import com.xuecheng.ucenter.mapper.XcUserRoleMapper;
import com.xuecheng.ucenter.model.dto.AuthParamsDto;
import com.xuecheng.ucenter.model.dto.XcUserExt;
import com.xuecheng.ucenter.model.po.XcUser;
import com.xuecheng.ucenter.model.po.XcUserRole;
import com.xuecheng.ucenter.service.AuthService;
import com.xuecheng.ucenter.service.WxAuthService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import javax.annotation.Resource;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;

@Slf4j
@Service("wx_authService")
public class WxAuthServiceImpl implements AuthService, WxAuthService {
    @Resource
    XcUserMapper xcUserMapper;

    @Resource
    XcUserRoleMapper xcUserRoleMapper;

    @Resource
    WxAuthServiceImpl currentProxy;

    @Resource
    RestTemplate restTemplate;

    @Value("${weixin.secret}")
    private String secret;
    @Value("${weixin.appid}")
    private String appid;

    @Override
    public XcUserExt execute(AuthParamsDto authParamsDto) {
        String username = authParamsDto.getUsername();
        XcUser user = xcUserMapper.selectOne(new LambdaQueryWrapper<XcUser>().eq(XcUser::getUsername, username));

        if (user == null) {
            //返回空表示用户不存在
            throw new RuntimeException("账号不存在");
        }
        XcUserExt xcUserExt = new XcUserExt();
        BeanUtils.copyProperties(user, xcUserExt);
        return xcUserExt;
    }

    @Override
    public XcUser wxAuth(String code) {
        // 获取令牌
        Map<String, String> access_token_map = getAccess_token(code);
        if (access_token_map == null) {
            return null;
        }
        if (access_token_map.get("errcode") != null) {
            throw new RuntimeException(access_token_map.get("errmsg"));
        }
        String accessToken = access_token_map.get("access_token");
        String openId = access_token_map.get("openid");
        String unionId = access_token_map.get("unionid");
        // 拿access_token查询用户信息
        Map<String, String> userInfo_map = getUserinfo(accessToken, openId);
        if (userInfo_map == null) {
            return null;
        }
        // 把用户存储到数据库
        XcUser xcUser = currentProxy.addWxUser(userInfo_map);

        return xcUser;
    }

    @Transactional
    public XcUser addWxUser(Map<String, String> userInfo_map){
        String unionid = userInfo_map.get("unionid");
        //根据unionid查询数据库
        XcUser xcUser = xcUserMapper.selectOne(new LambdaQueryWrapper<XcUser>().eq(XcUser::getWxUnionid, unionid));
        if (xcUser != null) {
            return xcUser;
        }

        // 添加用户
        XcUser newXcUser = new XcUser();
        String userId = UUID.randomUUID().toString();
        newXcUser.setId(userId);
        newXcUser.setUsername(userInfo_map.get("nickname"));
        newXcUser.setUserpic(userInfo_map.get("headimgurl"));
        newXcUser.setName(userInfo_map.get("nickname"));
        newXcUser.setPassword(unionid);
        newXcUser.setUtype("101001");//学生类型
        newXcUser.setStatus("1");//用户状态
        newXcUser.setCreateTime(LocalDateTime.now());
        newXcUser.setWxUnionid(unionid);
        xcUserMapper.insert(newXcUser);

        //添加用户角色
        XcUserRole xcUserRole = new XcUserRole();
        xcUserRole.setId(UUID.randomUUID().toString());
        xcUserRole.setRoleId("17");
        xcUserRole.setUserId(userId);
        xcUserRoleMapper.insert(xcUserRole);

        return newXcUser;
    }

    /**
     * 获取用户信息，示例如下：
     * {
     * "openid":"OPENID",
     * "nickname":"NICKNAME",
     * "sex":1,
     * "province":"PROVINCE",
     * "city":"CITY",
     * "country":"COUNTRY",
     * "headimgurl": "https://thirdwx.qlogo.cn/mmopen/g3MonUZtNHkdmzicIlibx6iaFqAc56vxLSUfpb6n5WKSYVY0ChQKkiaJSgQ1dZuTOgvLLrhJbERQQ4eMsv84eavHiaiceqxibJxCfHe/0",
     * "privilege":[
     * "PRIVILEGE1",
     * "PRIVILEGE2"
     * ],
     * "unionid": " o6_bmasdasdsad6_2sgVt7hMZOPfL"
     * }
     */
    private Map<String, String> getUserinfo(String access_token, String openid) {
        String wxUrl_template = "https://api.weixin.qq.com/sns/userinfo?access_token=%s&openid=%s";
        String url = String.format(wxUrl_template, access_token, openid);

        log.info("调用微信接口申请access_token, url:{}", url);

        ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.GET, null, String.class);
        //防止乱码进行转码
        String result = new String(response.getBody().getBytes(StandardCharsets.ISO_8859_1), StandardCharsets.UTF_8);
        log.info("调用微信接口申请access_token: 返回值:{}", result);
        Map<String, String> resultMap = JSON.parseObject(result, Map.class);
        return resultMap;
    }

    /**
     * 申请访问令牌,响应示例
     * {
     * "access_token":"ACCESS_TOKEN",
     * "expires_in":7200,
     * "refresh_token":"REFRESH_TOKEN",
     * "openid":"OPENID",
     * "scope":"SCOPE",
     * "unionid": "o6_bmasdasdsad6_2sgVt7hMZOPfL"
     * }
     */

    private Map<String, String> getAccess_token(String code) {
        String wxUrl_template = "https://api.weixin.qq.com/sns/oauth2/access_token?appid=%s&secret=%s&code=%s&grant_type=authorization_code";
        String url = String.format(wxUrl_template, appid, secret, code);

        log.info("调用微信接口申请access_token, url:{}", url);

        ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.POST, null, String.class);
        String body = response.getBody();
        Map<String, String> resultMap = JSON.parseObject(body, Map.class);
        return resultMap;
    }
}
