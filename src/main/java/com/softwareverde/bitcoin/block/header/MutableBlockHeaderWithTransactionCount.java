package com.softwareverde.bitcoin.block.header;

import com.softwareverde.json.Json;

public class MutableBlockHeaderWithTransactionCount extends MutableBlockHeader implements BlockHeaderWithTransactionCount {
    protected final Integer _transactionCount;

    public MutableBlockHeaderWithTransactionCount(final BlockHeader blockHeader, final Integer transactionCount) {
        super(blockHeader);
        _transactionCount = transactionCount;
    }

    @Override
    public Integer getTransactionCount() {
        return _transactionCount;
    }

    @Override
    public Json toJson() {
        final Json json = super.toJson();
        json.put("transactionCount", _transactionCount);
        return json;
    }
}
