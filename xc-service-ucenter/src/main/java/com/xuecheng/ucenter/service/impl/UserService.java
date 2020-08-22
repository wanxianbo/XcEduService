package com.xuecheng.ucenter.service.impl;

import com.xuecheng.framework.domain.ucenter.XcCompanyUser;
import com.xuecheng.framework.domain.ucenter.XcMenu;
import com.xuecheng.framework.domain.ucenter.XcUser;
import com.xuecheng.framework.domain.ucenter.ext.XcUserExt;
import com.xuecheng.ucenter.dao.XcCompanyUserRepository;
import com.xuecheng.ucenter.dao.XcMenuMapper;
import com.xuecheng.ucenter.dao.XcUserRepository;
import com.xuecheng.ucenter.service.IUserService;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class UserService implements IUserService {

    @Autowired
    private XcUserRepository xcUserRepository;
    @Autowired
    private XcCompanyUserRepository xcCompanyUserRepository;
    @Autowired
    private XcMenuMapper xcMenuMapper;


    /**
     * 查询用户信息
     *
     * @param username 用户名
     * @return 用户信息XcUserExt
     */
    @Override
    public XcUserExt getUserExt(String username) {
        //查询用户信息
        XcUser xcUser = xcUserRepository.findByUsername(username);
        if (xcUser == null) {
            return null;
        }
        XcUserExt xcUserExt = new XcUserExt();
        //用户id，查询所属公司
        BeanUtils.copyProperties(xcUser, xcUserExt);
        String userId = xcUser.getId();
        XcCompanyUser companyUser = xcCompanyUserRepository.findByUserId(userId);
        if (companyUser != null) {
            xcUserExt.setCompanyId(companyUser.getCompanyId());
        }
        List<XcMenu> xcMenus = xcMenuMapper.selectPermissionByUserId(userId);
        //用户权限
        xcUserExt.setPermissions(xcMenus);
        return xcUserExt;
    }
}
