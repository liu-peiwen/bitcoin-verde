package com.softwareverde.bitcoin.transaction;

import com.softwareverde.bitcoin.transaction.input.TransactionInput;
import com.softwareverde.bitcoin.transaction.locktime.LockTime;
import com.softwareverde.bitcoin.transaction.output.TransactionOutput;
import com.softwareverde.bitcoin.util.BitcoinUtil;
import com.softwareverde.bitcoin.util.ByteUtil;
import com.softwareverde.bitcoin.util.bytearray.ByteArrayBuilder;
import com.softwareverde.bitcoin.util.bytearray.Endian;

import java.util.ArrayList;
import java.util.List;

public class Transaction {
    protected Integer _version;
    protected Boolean _hasWitnessData = false;
    protected final List<TransactionInput> _transactionInputs = new ArrayList<TransactionInput>();
    protected final List<TransactionOutput> _transactionOutputs = new ArrayList<TransactionOutput>();
    protected LockTime _lockTime = new LockTime();

    /**
     * NOTE: Math with Satoshis
     *  The maximum number of satoshis is 210,000,000,000,000, which is less than the value a Java Long can hold.
     *  Therefore, using BigInteger is not be necessary any transaction calculation.
     */

    protected byte[] _getBytes() {
        final byte[] versionBytes = new byte[4];
        ByteUtil.setBytes(versionBytes, ByteUtil.integerToBytes(_version));

        final byte[] lockTimeBytes = new byte[4];
        ByteUtil.setBytes(lockTimeBytes, _lockTime.getBytes());

        final ByteArrayBuilder byteArrayBuilder = new ByteArrayBuilder();

        byteArrayBuilder.appendBytes(versionBytes, Endian.LITTLE);

        byteArrayBuilder.appendBytes(ByteUtil.variableLengthIntegerToBytes(_transactionInputs.size()), Endian.BIG);
        for (final TransactionInput transactionInput : _transactionInputs) {
            byteArrayBuilder.appendBytes(transactionInput.getBytes(), Endian.BIG);
        }

        byteArrayBuilder.appendBytes(ByteUtil.variableLengthIntegerToBytes(_transactionOutputs.size()), Endian.BIG);
        for (final TransactionOutput transactionOutput : _transactionOutputs) {
            byteArrayBuilder.appendBytes(transactionOutput.getBytes(), Endian.BIG);
        }

        byteArrayBuilder.appendBytes(lockTimeBytes, Endian.LITTLE);

        return byteArrayBuilder.build();
    }

    public byte[] calculateSha256Hash() {
        return ByteUtil.reverseBytes(BitcoinUtil.sha256(BitcoinUtil.sha256(_getBytes())));
    }

    public Integer getVersion() { return _version; }
    public void setVersion(final Integer version) { _version = version; }

    public Boolean hasWitnessData() { return _hasWitnessData; }
    public void setHasWitnessData(final Boolean hasWitnessData) { _hasWitnessData = hasWitnessData; }

    public final List<TransactionInput> getTransactionInputs() {
        final List<TransactionInput> transactionInputs = new ArrayList<TransactionInput>(_transactionInputs.size());
        for (final TransactionInput transactionInput : _transactionInputs) {
            transactionInputs.add(transactionInput.copy());
        }
        return transactionInputs;
    }
    public void addTransactionInput(final TransactionInput transactionInput) { _transactionInputs.add(transactionInput.copy()); }
    public void clearTransactionInputs() { _transactionInputs.clear(); }

    public final List<TransactionOutput> getTransactionOutputs() {
        final List<TransactionOutput> transactionOutputs = new ArrayList<TransactionOutput>(_transactionOutputs.size());
        for (final TransactionOutput transactionOutput : _transactionOutputs) {
            transactionOutputs.add(transactionOutput.copy());
        }
        return transactionOutputs;
    }
    public void addTransactionOutput(final TransactionOutput transactionOutput) { _transactionOutputs.add(transactionOutput.copy()); }
    public void clearTransactionOutputs() { _transactionOutputs.clear(); }

    public LockTime getLockTime() { return _lockTime.copy(); }
    public void setLockTime(final LockTime lockTime) { _lockTime = lockTime.copy(); }

    public Long getTotalOutputValue() {
        long totalValue = 0L;

        for (final TransactionOutput transactionOutput : _transactionOutputs) {
            totalValue += transactionOutput.getValue();
        }

        return totalValue;
    }

    public Integer getByteCount() {
        final Integer versionByteCount = 4;

        final Integer transactionInputsByteCount;
        {
            Integer byteCount = 0;
            byteCount += ByteUtil.variableLengthIntegerToBytes(_transactionInputs.size()).length;
            for (final TransactionInput transactionInput : _transactionInputs) {
                byteCount += transactionInput.getByteCount();
            }
            transactionInputsByteCount = byteCount;
        }

        final Integer transactionOutputsByteCount;
        {
            Integer byteCount = 0;
            byteCount += ByteUtil.variableLengthIntegerToBytes(_transactionOutputs.size()).length;
            for (final TransactionOutput transactionOutput : _transactionOutputs) {
                byteCount += transactionOutput.getByteCount();
            }
            transactionOutputsByteCount = byteCount;
        }

        final Integer lockTimeByteCount = 4;

        return (versionByteCount + transactionInputsByteCount + transactionOutputsByteCount + lockTimeByteCount);
    }

    public byte[] getBytes() {
        return _getBytes();
    }

    public Transaction copy() {
        final Transaction transaction = new Transaction();
        transaction._version = _version;
        transaction._hasWitnessData = _hasWitnessData;
        for (final TransactionInput transactionInput : _transactionInputs) {
            transaction._transactionInputs.add(transactionInput.copy());
        }
        for (final TransactionOutput transactionOutput : _transactionOutputs) {
            transaction._transactionOutputs.add(transactionOutput.copy());
        }
        transaction._lockTime = _lockTime;
        return transaction;
    }
}
