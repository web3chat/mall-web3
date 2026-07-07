package com.fzm.mall.service;

import com.fzm.mall.entity.dataobject.ChainContractDO;
import com.fzm.mall.mapper.ChainContractMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class ChainService {
    private final ChainContractMapper chainContractMapper;

    public ChainContractDO getContractByCtId(Integer ctId) {
        if (ctId == null) {
            return null;
        }
        return chainContractMapper.getByCtId(ctId);
    }

    public List<ChainContractDO> listContract() {
        return chainContractMapper.list();
    }

}
