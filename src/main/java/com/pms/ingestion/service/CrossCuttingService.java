package com.pms.ingestion.service;

import com.pms.ingestion.entity.DlqTrade;
import com.pms.ingestion.entity.OutboxTrade;
import com.pms.ingestion.entity.SafeStoreTrade;
import com.pms.ingestion.entity.TradeEvent;

public interface CrossCuttingService {

    void recordIngestionSuccess(TradeEvent tradeEvent,
                                SafeStoreTrade safeStoreTrade,
                                OutboxTrade outboxTrade);

    void recordIngestionFailure(TradeEvent tradeEvent,
                                DlqTrade dlqTrade,
                                Exception ex);
}
