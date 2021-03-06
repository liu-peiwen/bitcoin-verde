package com.softwareverde.bitcoin.block.header;

import com.softwareverde.bitcoin.block.header.difficulty.Difficulty;
import com.softwareverde.bitcoin.block.merkleroot.Hashable;
import com.softwareverde.bitcoin.transaction.Transaction;
import com.softwareverde.bitcoin.type.hash.sha256.ImmutableSha256Hash;
import com.softwareverde.bitcoin.type.hash.sha256.Sha256Hash;
import com.softwareverde.bitcoin.type.merkleroot.MerkleRoot;
import com.softwareverde.constable.Constable;
import com.softwareverde.json.Jsonable;
import com.softwareverde.util.HexUtil;

public interface BlockHeader extends Hashable, Constable<ImmutableBlockHeader>, Jsonable {
    Long VERSION = 0x04L;
    ImmutableSha256Hash GENESIS_BLOCK_HASH = ImmutableSha256Hash.copyOf(HexUtil.hexStringToByteArray("000000000019D6689C085AE165831E934FF763AE46A2A6C172B3F1B60A8CE26F"));

    static Long calculateBlockReward(final Long blockHeight) {
        return ((50 * Transaction.SATOSHIS_PER_BITCOIN) >> (blockHeight / 210000));
    }

    Sha256Hash getPreviousBlockHash();
    MerkleRoot getMerkleRoot();
    Difficulty getDifficulty();
    Long getVersion();
    Long getTimestamp();
    Long getNonce();

    Boolean isValid();

    @Override
    ImmutableBlockHeader asConst();
}
