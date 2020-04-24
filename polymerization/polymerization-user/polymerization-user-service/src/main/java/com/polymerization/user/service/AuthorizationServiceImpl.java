package com.polymerization.user.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.polymerization.common.domain.BusinessException;
import com.polymerization.common.domain.CommonErrorCode;
import com.polymerization.common.domain.PageVO;
import com.polymerization.user.api.AuthorizationService;
import com.polymerization.user.api.dto.authorization.AuthorizationInfoDTO;
import com.polymerization.user.api.dto.authorization.PrivilegeDTO;
import com.polymerization.user.api.dto.authorization.PrivilegeTreeDTO;
import com.polymerization.user.api.dto.authorization.RoleDTO;
import com.polymerization.user.api.dto.tenant.AccountRoleDTO;
import com.polymerization.user.api.dto.tenant.TenRolePrivilegeDTO;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.apache.dubbo.config.annotation.Service;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.*;
import java.util.stream.Collectors;

@Service
@Slf4j
public class AuthorizationServiceImpl implements AuthorizationService {

    @Autowired
    private com.shanjupay.user.mapper.AuthorizationRoleMapper roleMapper;
    @Autowired
    private com.shanjupay.user.mapper.AuthorizationPrivilegeMapper privilegeMapper;
    @Autowired
    private com.shanjupay.user.mapper.AccountMapper accountMapper;
    @Autowired
    private com.shanjupay.user.mapper.AccountRoleMapper accountRoleMapper;
    @Autowired
    private com.shanjupay.user.mapper.AuthorizationRolePrivilegeMapper rolePrivilegeMapper;
    @Autowired
    private com.shanjupay.user.mapper.AuthorizationPrivilegeGroupMapper groupMapper;


    /**
     * 授权，获取某用户在多个租户下的权限信息
     *
     * @param username  用户名
     * @param tenantIds 租户id列表
     * @return key为租户id  value为租户下的角色权限信息
     */
    @Override
    public Map<Long, AuthorizationInfoDTO> authorize(String username, Long[] tenantIds) {
        List<Long> ids = Arrays.asList(tenantIds);
        List<Long> roleIds = accountRoleMapper.selectRoleByUsernameInTenants(username, ids);
        List<TenRolePrivilegeDTO> list = privilegeMapper.selectPrivilegeRoleInTenant(roleIds);
        if (list == null) {
            throw new BusinessException(CommonErrorCode.E_100104);
        }
        Map<Long, AuthorizationInfoDTO> map = new HashMap<>();// key 是租户id
        /*{
            租户id:{
                r_001:[p_001,p_002],
                r_002:[p_004,p_005]
            }
            ....
        }*/
        for (TenRolePrivilegeDTO dto : list) {
            if (!map.containsKey(dto.getTenantId())) {
                map.put(dto.getTenantId(), new AuthorizationInfoDTO());
            }
            AuthorizationInfoDTO info = map.get(dto.getTenantId());
            if (info.getRolePrivilegeMap().containsKey(dto.getRoleCode())) {
                info.getRolePrivilegeMap().get(dto.getRoleCode()).add(dto.getPrivilegeCode());
            } else {
                List<String> prviList = new ArrayList<>();
                prviList.add(dto.getPrivilegeCode());
                info.getRolePrivilegeMap().put(dto.getRoleCode(), prviList);
            }
        }
        return map;
    }

    /**
     * 查找某租户下，多个角色的权限信息集合
     *
     * @param roleCodes
     * @return List<PrivilegeTreeDTO>
     */
    @Override
    public List<PrivilegeDTO> queryPrivilege(Long tenantId, String[] roleCodes) {
        //先获取某租户下的角色
        List<com.shanjupay.user.entity.AuthorizationRole> roles = roleMapper.selectList(new QueryWrapper<com.shanjupay.user.entity.AuthorizationRole>().lambda()
                .eq(com.shanjupay.user.entity.AuthorizationRole::getTenantId, tenantId)
                .in(com.shanjupay.user.entity.AuthorizationRole::getCode, roleCodes));
        //获取多个角色的权限权限集合
        List<com.shanjupay.user.entity.AuthorizationPrivilege> privileges = new ArrayList<>();
        if (!roles.isEmpty()) {
            List<Long> roleIds = roles.stream().map(com.shanjupay.user.entity.AuthorizationRole::getId).collect(Collectors.toList());
            privileges = privilegeMapper.selectPrivilegeByRole(roleIds);
        }
        List<PrivilegeDTO> privilegeDTOS =  new ArrayList<>();//AuthorizationPrivilegeConvert.INSTANCE.entitylist2dto(privileges);
        for (com.shanjupay.user.entity.AuthorizationPrivilege privilege : privileges) {
            PrivilegeDTO privilegeDTO = new PrivilegeDTO();
            BeanUtils.copyProperties(privilege,privilegeDTO);
            privilegeDTOS.add(privilegeDTO);
        }
        return privilegeDTOS;
    }

    /**
     * 获取权限组下所有权限
     *
     * @param privilegeGroupId 权限组ID
     * @return
     */
    @Override
    public List<PrivilegeDTO> queryPrivilegeByGroupId(Long privilegeGroupId) {
        List<com.shanjupay.user.entity.AuthorizationPrivilege> privilegeList = privilegeMapper.selectList(new QueryWrapper<com.shanjupay.user.entity.AuthorizationPrivilege>().lambda()
                .eq(com.shanjupay.user.entity.AuthorizationPrivilege::getPrivilegeGroupId, privilegeGroupId));
        List<PrivilegeDTO> privilegeDTOS =  new ArrayList<>();//AuthorizationPrivilegeConvert.INSTANCE.entitylist2dto(privileges);
        for (com.shanjupay.user.entity.AuthorizationPrivilege privilege : privilegeList) {
            PrivilegeDTO privilegeDTO = new PrivilegeDTO();
            BeanUtils.copyProperties(privilege,privilegeDTO);
            privilegeDTOS.add(privilegeDTO);
        }
        return privilegeDTOS;
    }

    /**
     * 查找某租户下，多个角色的权限信息集合，并根据权限组组装成为权限树
     *
     * @param tenantId
     * @param roleCodes 角色编码列表，为null时代表所有权限
     * @return
     */
    @Override
    public PrivilegeTreeDTO queryPrivilegeTree(Long tenantId, String[] roleCodes) {
        //1.获取租户下角色对应的权限集合
        List<PrivilegeDTO> pList = queryPrivilege(tenantId, roleCodes);
        HashSet<PrivilegeDTO> h = new HashSet<PrivilegeDTO>(pList);
        pList.clear();
        pList.addAll(h);
        //2.获取所有权限组
        List<com.shanjupay.user.entity.AuthorizationPrivilegeGroup> groupList = groupMapper.selectList(null);

        Map<String, PrivilegeTreeDTO> groupsMap = new HashMap<>();
        String topId = "top_1";
        PrivilegeTreeDTO topTree = new PrivilegeTreeDTO();
        topTree.setId(topId);
        topTree.setParentId(null);
        topTree.setName(null);
        topTree.setStatus(0);

        for (com.shanjupay.user.entity.AuthorizationPrivilegeGroup g : groupList) {
            if (g.getParentId() == null) {
                PrivilegeTreeDTO child = new PrivilegeTreeDTO();
                child.setId(String.valueOf(g.getId()));
                child.setParentId(topId);
                child.setName(g.getName());
                child.setGroup(true);
                child.setStatus(1);
                topTree.getChildren().add(child);

                privGroupTree(child, groupList, groupsMap);
            }
        }

        for (PrivilegeDTO priv : pList) {

            String privGroupId = String.valueOf(priv.getPrivilegeGroupId());
            PrivilegeTreeDTO pGroupTreeDto = groupsMap.get(privGroupId);
            if (pGroupTreeDto != null) {

                PrivilegeTreeDTO pTreeDto = new PrivilegeTreeDTO();
                pTreeDto.setGroup(false);
                pTreeDto.setName(priv.getName());
                pTreeDto.setId(priv.getCode());
                pTreeDto.setParentId(privGroupId);
                pTreeDto.setStatus(1);

                pGroupTreeDto.getChildren().add(pTreeDto);
            }
        }
        return topTree;
    }


    private void privGroupTree(
            PrivilegeTreeDTO currChild, List<com.shanjupay.user.entity.AuthorizationPrivilegeGroup> groupList, Map<String, PrivilegeTreeDTO> groupsMap) {
        if (!groupsMap.containsKey(currChild.getId())) {
            groupsMap.put(currChild.getId(), currChild);
        }
        for (com.shanjupay.user.entity.AuthorizationPrivilegeGroup ccGroup : groupList) {
            if (String.valueOf(ccGroup.getParentId()).equals(currChild.getId())) {

                PrivilegeTreeDTO tmp = new PrivilegeTreeDTO();
                tmp.setId(String.valueOf(ccGroup.getId()));
                tmp.setParentId(currChild.getId());
                tmp.setName(ccGroup.getName());
                tmp.setGroup(true);
                tmp.setStatus(1);
                currChild.getChildren().add(tmp);

                //是否是多余的
                if (!groupsMap.containsKey(tmp.getId())) {
                    groupsMap.put(tmp.getId(), tmp);
                }
                privGroupTree(tmp, groupList, groupsMap);
            }
        }
    }

/////////////////////////////////////////////角色、权限/////////////////////////////////////////////

    /**
     * 创建租户内角色(不包含权限)
     *
     * @param tenantId
     * @param role
     */
    @Override
    public void createRole(Long tenantId, RoleDTO role) {
        //此处需要校验 同一租户下角色code不能相同
        if (role == null || StringUtils.isBlank(role.getCode())) {
            throw new BusinessException(CommonErrorCode.E_110003);
        }
        String code = role.getCode();
        if (isExistRoleCode(tenantId, code)) {
            throw new BusinessException(CommonErrorCode.E_110002);
        }
        com.shanjupay.user.entity.AuthorizationRole entity = new com.shanjupay.user.entity.AuthorizationRole();//AuthorizationRoleConvert.INSTANCE.dto2entity(role);
        BeanUtils.copyProperties(role,entity);
        roleMapper.insert(entity);
    }

    /**
     * 判断角色编码是否在某租户下已存在
     *
     * @param tenantId
     * @param roleCode
     * @return
     */
    private boolean isExistRoleCode(Long tenantId, String roleCode) {
        int i = roleMapper.selectRoleCodeInTenant(tenantId, roleCode);
        return i > 0;
    }

    /**
     * 删除租户内角色，如果有账号绑定该角色，禁止删除
     *
     * @param tenantId
     * @param roleCode
     */
    @Override
    public void removeRole(Long tenantId, String roleCode) {
        boolean b = accountMapper.selectAccountByRole(tenantId, roleCode);
        if (b) {
            throw new BusinessException(CommonErrorCode.E_110004);
        }
        com.shanjupay.user.entity.AuthorizationRole role = roleMapper.selectOne(new QueryWrapper<com.shanjupay.user.entity.AuthorizationRole>().lambda()
                .eq(com.shanjupay.user.entity.AuthorizationRole::getTenantId, tenantId).eq(com.shanjupay.user.entity.AuthorizationRole::getCode, roleCode));
        if (role != null && role.getId() != null) {
            removeRole(role.getId());
        }

    }

    /**
     * 删除租户内角色
     *
     * @param id 角色id
     */
    @Override
    public void removeRole(Long id) {

        roleMapper.deleteById(id);
    }

    /**
     * 修改租户内角色(不包含权限)
     *
     * @param role
     */
    @Override
    public void modifyRole(RoleDTO role) {
        //UpdateWrapper<AuthorizationRole> uw = new UpdateWrapper<>();
        //uw.lambda().eq(AuthorizationRole::getId,role.getId()).set(AuthorizationRole::getName,role.getName());
        //roleMapper.update(null,uw);
        com.shanjupay.user.entity.AuthorizationRole entity = new com.shanjupay.user.entity.AuthorizationRole();// AuthorizationRoleConvert.INSTANCE.dto2entity(role);
        BeanUtils.copyProperties(role,entity);
        roleMapper.updateById(entity);
    }

    /**
     * 角色设置权限(清除+设置)
     *
     * @param tenantId       租户id
     * @param roleCode       角色code
     * @param privilegeCodes 该角色对应的权限集合
     */
    @Override
    public void roleBindPrivilege(Long tenantId, String roleCode, String[] privilegeCodes) {

        //1.获取租户内的某个角色信息(包含权限信息)
        RoleDTO roleDTOS = queryTenantRole(tenantId, roleCode);
        if (privilegeCodes == null || privilegeCodes.length == 0) {
            throw new BusinessException(CommonErrorCode.E_110005);
        }
        //2.根据权限code获取权限实体集合
        List<com.shanjupay.user.entity.AuthorizationPrivilege> privileges = privilegeMapper.selectList(new QueryWrapper<com.shanjupay.user.entity.AuthorizationPrivilege>().lambda()
                .in(com.shanjupay.user.entity.AuthorizationPrivilege::getCode, privilegeCodes));
        if (privileges.isEmpty()) {
            throw new BusinessException(CommonErrorCode.E_110005);
        }

        //组装权限id集合
        List<Long> pids = new ArrayList<>();
        for (com.shanjupay.user.entity.AuthorizationPrivilege p : privileges) {
            pids.add(p.getId());
        }

        //3.删除角色已关联的权限信息
        if (roleDTOS != null && roleDTOS.getId() != null) {
            Long roleId = roleDTOS.getId();
            //若角色已关联权限，清除
            rolePrivilegeMapper.delete(new QueryWrapper<com.shanjupay.user.entity.AuthorizationRolePrivilege>().lambda()
                    .eq(com.shanjupay.user.entity.AuthorizationRolePrivilege::getRoleId, roleId));
        }

        //4.将角色和权限进行关联操作authorization_role_privilege表
        rolePrivilegeMapper.insertRolePrivilege(roleDTOS.getId(), pids);
    }

    /**
     * 查询某租户下角色(不分页，不包含权限)
     *
     * @param tenantId
     * @return
     */
    @Override
    public List<RoleDTO> queryRole(Long tenantId) {
        QueryWrapper<com.shanjupay.user.entity.AuthorizationRole> qw = new QueryWrapper<>();
        qw.lambda().eq(com.shanjupay.user.entity.AuthorizationRole::getTenantId, tenantId);
        List<com.shanjupay.user.entity.AuthorizationRole> authorizationRoles = roleMapper.selectList(qw);
        List<RoleDTO> roleDTOS = new ArrayList<>(); //AuthorizationRoleConvert.INSTANCE.entitylist2dto(authorizationRoles);
        for (com.shanjupay.user.entity.AuthorizationRole authorizationRole : authorizationRoles) {
            RoleDTO roleDTO = new RoleDTO();
            BeanUtils.copyProperties(authorizationRole,roleDTO);
            roleDTOS.add(roleDTO);
        }
        return roleDTOS;
    }

    /**
     * 根据roleCode获取角色(不包含权限)
     *
     * @param tenantId
     * @param roleCodes
     * @return
     */
    @Override
    public List<RoleDTO> queryRole(Long tenantId, String... roleCodes) {
        List<String> codes = Arrays.asList(roleCodes);
        List<com.shanjupay.user.entity.AuthorizationRole> authorizationRoles = roleMapper.selectList(new QueryWrapper<com.shanjupay.user.entity.AuthorizationRole>().lambda()
                .eq(com.shanjupay.user.entity.AuthorizationRole::getTenantId, tenantId)
                .in(com.shanjupay.user.entity.AuthorizationRole::getCode, codes));
        List<RoleDTO> roleDTOS = new ArrayList<>();// AuthorizationRoleConvert.INSTANCE.entitylist2dto(authorizationRoles);
        for (com.shanjupay.user.entity.AuthorizationRole authorizationRole : authorizationRoles) {
            RoleDTO roleDTO = new RoleDTO();
            BeanUtils.copyProperties(authorizationRole,roleDTO);
            roleDTOS.add(roleDTO);
        }
        return roleDTOS;
    }


    /**
     * 获取租户内的某个角色信息(包含权限信息)
     *
     * @param tenantId
     * @param roleCode
     * @return
     */
    @Override
    public RoleDTO queryTenantRole(Long tenantId, String roleCode) {
        com.shanjupay.user.entity.AuthorizationRole role = roleMapper.selectOne(new QueryWrapper<com.shanjupay.user.entity.AuthorizationRole>().lambda()
                .eq(com.shanjupay.user.entity.AuthorizationRole::getTenantId, tenantId).eq(com.shanjupay.user.entity.AuthorizationRole::getCode, roleCode));
        if (role == null) {
            throw new BusinessException(CommonErrorCode.E_110003);
        }
        Long id = role.getId();
//        RoleDTO roleDTO = AuthorizationRoleConvert.INSTANCE.entity2dto(role);
        RoleDTO roleDTO = new RoleDTO();
        BeanUtils.copyProperties(role,roleDTO);
        roleDTO.setPrivilegeCodes(roleMapper.selectPrivilegeByRole(id));

        return roleDTO;
    }

    /**
     * 绑定角色
     *
     * @param username  用户名
     * @param tenantId  租户id
     * @param roleCodes 角色列表
     * @return
     */
    @Override
    public void bindAccountRole(String username, Long tenantId, String[] roleCodes) {
        //批量插入
        List<String> roleList = new ArrayList<>(Arrays.asList(roleCodes));
        accountRoleMapper.insertAccountRole(username, tenantId, roleList);
    }

    /**
     * 解绑角色
     *
     * @param username  用户名
     * @param tenantId  租户id
     * @param roleCodes 角色列表
     * @return
     */
    @Override
    public void unbindAccountRole(String username, Long tenantId, String[] roleCodes) {
        List<String> roleList = new ArrayList<>(Arrays.asList(roleCodes));
        //根据查询到的角色id清除掉对应租户
        List<com.shanjupay.user.entity.AuthorizationRole> roles = roleMapper.selectList(new QueryWrapper<com.shanjupay.user.entity.AuthorizationRole>().lambda()
                .eq(com.shanjupay.user.entity.AuthorizationRole::getTenantId, tenantId)
                .in(com.shanjupay.user.entity.AuthorizationRole::getCode, roleList));
        if (roles.isEmpty()) {
            throw new BusinessException(CommonErrorCode.E_100104);
        }
        List<Long> roleIds = new ArrayList<>();
        for (com.shanjupay.user.entity.AuthorizationRole role : roles) {
            Long id = role.getId();
            roleIds.add(id);
        }
        roleMapper.update(null, new UpdateWrapper<com.shanjupay.user.entity.AuthorizationRole>().lambda()
                .in(com.shanjupay.user.entity.AuthorizationRole::getId, roleIds).set(com.shanjupay.user.entity.AuthorizationRole::getTenantId, null));
        //根据账号-角色表id清除关系
        List<com.shanjupay.user.entity.AccountRole> accountRoles = accountRoleMapper.selectList(new QueryWrapper<com.shanjupay.user.entity.AccountRole>().lambda()
                .eq(com.shanjupay.user.entity.AccountRole::getUsername, username).eq(com.shanjupay.user.entity.AccountRole::getTenantId, tenantId)
                .in(com.shanjupay.user.entity.AccountRole::getId, roleList));
        if (accountRoles.isEmpty()) {
            throw new BusinessException(CommonErrorCode.E_100104);
        }
        List<Long> ids = new ArrayList<>();
        for (com.shanjupay.user.entity.AccountRole accountRole : accountRoles) {
            Long id = accountRole.getId();
            ids.add(id);
        }
        accountRoleMapper.update(null, new UpdateWrapper<com.shanjupay.user.entity.AccountRole>().lambda()
                .in(com.shanjupay.user.entity.AccountRole::getId, ids).set(com.shanjupay.user.entity.AccountRole::getRoleCode, null));

    }

    @Override
    public PageVO<RoleDTO> queryRoleByPage(RoleDTO roleDTO, Integer pageNo, Integer pageSize) {
        return buildRoleQuery(roleDTO, pageNo, pageSize);
    }

    @Override
    public List<AccountRoleDTO> queryAccountBindRole(String username, Long tenantId, String[] roleCodes) {
        List<com.shanjupay.user.entity.AccountRole> accountRoles = accountRoleMapper.selectList(new QueryWrapper<com.shanjupay.user.entity.AccountRole>().lambda()
                .eq(com.shanjupay.user.entity.AccountRole::getUsername, username).eq(com.shanjupay.user.entity.AccountRole::getTenantId, tenantId)
                .in(com.shanjupay.user.entity.AccountRole::getRoleCode, roleCodes));
         List<AccountRoleDTO> list = new ArrayList<>();
        for (com.shanjupay.user.entity.AccountRole accountRole : accountRoles) {

            AccountRoleDTO accountRoleDTO = new AccountRoleDTO();
            BeanUtils.copyProperties(accountRole,accountRoleDTO);
            list.add(accountRoleDTO);
        }
        return list;
    }
    @Override
    public List<AccountRoleDTO> queryAccountRole(String username, Long tenantId) {
        List<com.shanjupay.user.entity.AccountRole> accountRoles = accountRoleMapper.selectList(new QueryWrapper<com.shanjupay.user.entity.AccountRole>().lambda()
                .eq(com.shanjupay.user.entity.AccountRole::getUsername, username).eq(com.shanjupay.user.entity.AccountRole::getTenantId, tenantId));
        List<AccountRoleDTO> accountRoleDTOS = new ArrayList<>();
        for (com.shanjupay.user.entity.AccountRole accountRole : accountRoles) {

            AccountRoleDTO accountRoleDTO = new AccountRoleDTO();
            BeanUtils.copyProperties(accountRole,accountRoleDTO);

            accountRoleDTOS.add(accountRoleDTO);
        }
        return accountRoleDTOS;
    }
    @Override
    public List<RoleDTO> queryRolesByUsername(String username, Long tenantId) {
//获取员工用户名，即账号的用户名
        List<AccountRoleDTO> list = queryAccountRole(username, tenantId);
        List<RoleDTO> roleDTOS = null;
        if (!list.isEmpty()) {
            List<String> codes = new ArrayList<>();
            list.forEach(ar -> {
                String roleCode = ar.getRoleCode();
                codes.add(roleCode);
            });
            String[] c = codes.toArray(new String[codes.size()]);
            roleDTOS = queryRole(tenantId, c);
        }
        return roleDTOS;
    }


    private PageVO<RoleDTO> buildRoleQuery(RoleDTO roleDTO, Integer pageNo, Integer pageSize) {
        // 创建分页
        Page<com.shanjupay.user.entity.AuthorizationRole> page = new Page<>(pageNo, pageSize);
        // 构造查询条件
        QueryWrapper<com.shanjupay.user.entity.AuthorizationRole> qw = new QueryWrapper();
        if (null != roleDTO && null != roleDTO.getTenantId()) {
            qw.lambda().eq(com.shanjupay.user.entity.AuthorizationRole::getTenantId, roleDTO.getTenantId());
        }
        if (null != roleDTO && null != roleDTO.getName()) {
            qw.lambda().eq(com.shanjupay.user.entity.AuthorizationRole::getName, roleDTO.getName());
        }
        // 执行查询
        IPage<com.shanjupay.user.entity.AuthorizationRole> roleIPage = roleMapper.selectPage(page, qw);
        // entity List转DTO List
        List<RoleDTO> roleList = new ArrayList<>();//AuthorizationRoleConvert.INSTANCE.entitylist2dto(roleIPage.getRecords());
        for (com.shanjupay.user.entity.AuthorizationRole record : roleIPage.getRecords()) {
            RoleDTO roleDTO1 = new RoleDTO();
            BeanUtils.copyProperties(record,roleDTO1);
            roleList.add(roleDTO1);
        }
        // 封装结果集
        return new PageVO<>(roleList, roleIPage.getTotal(), pageNo, pageSize);
    }

}
