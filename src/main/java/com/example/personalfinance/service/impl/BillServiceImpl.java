package com.example.personalfinance.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.personalfinance.entity.Bill;
import com.example.personalfinance.mapper.BillMapper;
import com.example.personalfinance.service.IBillService;
import org.springframework.stereotype.Service;

/**
 * Default bill service implementation.
 */
@Service
public class BillServiceImpl extends ServiceImpl<BillMapper, Bill> implements IBillService {
}
