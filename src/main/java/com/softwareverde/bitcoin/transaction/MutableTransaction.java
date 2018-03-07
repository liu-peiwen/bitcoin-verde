package com.softwareverde.bitcoin.transaction;

import com.softwareverde.bitcoin.block.header.BlockHeader;
import com.softwareverde.bitcoin.transaction.input.ImmutableTransactionInput;
import com.softwareverde.bitcoin.transaction.input.TransactionInput;
import com.softwareverde.bitcoin.transaction.locktime.ImmutableLockTime;
import com.softwareverde.bitcoin.transaction.locktime.LockTime;
import com.softwareverde.bitcoin.transaction.output.ImmutableTransactionOutput;
import com.softwareverde.bitcoin.transaction.output.TransactionOutput;
import com.softwareverde.bitcoin.type.hash.Hash;
import com.softwareverde.bitcoin.type.hash.MutableHash;
import com.softwareverde.bitcoin.util.BitcoinUtil;
import com.softwareverde.bitcoin.util.ByteUtil;
import com.softwareverde.bitcoin.util.bytearray.ByteArrayBuilder;
import com.softwareverde.constable.list.List;
import com.softwareverde.constable.list.mutable.MutableList;

public class MutableTransaction implements Transaction {
    protected Integer _version = BlockHeader.VERSION;
    protected Boolean _hasWitnessData = false;
    protected final MutableList<TransactionInput> _transactionInputs = new MutableList<TransactionInput>();
    protected final MutableList<TransactionOutput> _transactionOutputs = new MutableList<TransactionOutput>();
    protected LockTime _lockTime = new ImmutableLockTime();

    /**
     * NOTE: Math with Satoshis
     *  The maximum number of satoshis is 210,000,000,000,000, which is less than the value a Java Long can hold.
     *  Therefore, using BigInteger is not be necessary any transaction calculation.
     */

    public MutableTransaction() { }

    @Override
    public Hash getHash() {
        final TransactionDeflater transactionDeflater = new TransactionDeflater();
        final ByteArrayBuilder byteArrayBuilder = transactionDeflater.toByteArrayBuilder(this);
        final byte[] doubleSha256 = BitcoinUtil.sha256(BitcoinUtil.sha256(byteArrayBuilder.build()));
        return new MutableHash(ByteUtil.reverseEndian(doubleSha256));
    }

    @Override
    public Integer getVersion() { return _version; }
    public void setVersion(final Integer version) { _version = version; }

    @Override
    public Boolean hasWitnessData() { return _hasWitnessData; }
    public void setHasWitnessData(final Boolean hasWitnessData) { _hasWitnessData = hasWitnessData; }

    @Override
    public final List<TransactionInput> getTransactionInputs() {
        return _transactionInputs;
    }
    public void addTransactionInput(final TransactionInput transactionInput) { _transactionInputs.add(new ImmutableTransactionInput(transactionInput)); }
    public void clearTransactionInputs() { _transactionInputs.clear(); }

    @Override
    public final List<TransactionOutput> getTransactionOutputs() {
        return _transactionOutputs;
    }
    public void addTransactionOutput(final TransactionOutput transactionOutput) { _transactionOutputs.add(new ImmutableTransactionOutput(transactionOutput)); }
    public void clearTransactionOutputs() { _transactionOutputs.clear(); }

    @Override
    public LockTime getLockTime() { return _lockTime; }
    public void setLockTime(final LockTime lockTime) { _lockTime = lockTime; }

    @Override
    public Long getTotalOutputValue() {
        long totalValue = 0L;

        for (final TransactionOutput transactionOutput : _transactionOutputs) {
            totalValue += transactionOutput.getAmount();
        }

        return totalValue;
    }

    @Override
    public ImmutableTransaction asConst() {
        return new ImmutableTransaction(this);
    }
}