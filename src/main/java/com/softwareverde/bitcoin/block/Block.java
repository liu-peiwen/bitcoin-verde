package com.softwareverde.bitcoin.block;

import com.softwareverde.bitcoin.block.header.BlockHeaderWithTransactionCount;
import com.softwareverde.bitcoin.transaction.Transaction;
import com.softwareverde.bitcoin.transaction.coinbase.CoinbaseTransaction;
import com.softwareverde.bitcoin.type.hash.sha256.Sha256Hash;
import com.softwareverde.constable.list.List;

public interface Block extends BlockHeaderWithTransactionCount {
    List<Transaction> getTransactions();
    CoinbaseTransaction getCoinbaseTransaction();
    List<Sha256Hash> getPartialMerkleTree(int transactionIndex);

    @Override
    ImmutableBlock asConst();
}
