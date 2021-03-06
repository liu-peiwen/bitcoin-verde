package com.softwareverde.bitcoin.server.module.node.sync;

import com.softwareverde.bitcoin.block.Block;
import com.softwareverde.bitcoin.block.BlockId;
import com.softwareverde.bitcoin.block.header.BlockHeader;
import com.softwareverde.bitcoin.block.header.BlockHeaderWithTransactionCount;
import com.softwareverde.bitcoin.block.validator.BlockHeaderValidator;
import com.softwareverde.bitcoin.chain.segment.BlockChainSegmentId;
import com.softwareverde.bitcoin.chain.time.MutableMedianBlockTime;
import com.softwareverde.bitcoin.server.database.BlockChainDatabaseManager;
import com.softwareverde.bitcoin.server.database.BlockDatabaseManager;
import com.softwareverde.bitcoin.server.module.node.BitcoinNodeManager;
import com.softwareverde.bitcoin.server.node.BitcoinNode;
import com.softwareverde.bitcoin.type.hash.sha256.Sha256Hash;
import com.softwareverde.constable.list.List;
import com.softwareverde.database.DatabaseException;
import com.softwareverde.database.mysql.MysqlDatabaseConnection;
import com.softwareverde.database.mysql.MysqlDatabaseConnectionFactory;
import com.softwareverde.database.util.TransactionUtil;
import com.softwareverde.io.Logger;
import com.softwareverde.util.Container;
import com.softwareverde.util.Util;

public class BlockHeaderDownloader {
    protected final MysqlDatabaseConnectionFactory _databaseConnectionFactory;
    protected final BitcoinNodeManager _nodeManager;
    protected final MutableMedianBlockTime _medianBlockTime;

    protected Long _startTime;
    protected Long _blockHeaderCount = 0L;
    protected final Container<Float> _averageBlockHeadersPerSecond = new Container<Float>(0F);

    protected volatile boolean _shouldStop = false;

    protected Boolean _hasGenesisBlockHeader() {
        try (final MysqlDatabaseConnection databaseConnection = _databaseConnectionFactory.newConnection()) {
            final BlockDatabaseManager blockDatabaseManager = new BlockDatabaseManager(databaseConnection);
            final Sha256Hash lastKnownHash = blockDatabaseManager.getHeadBlockHeaderHash();
            return (lastKnownHash != null);
        }
        catch (final DatabaseException exception) {
            Logger.log(exception);
            return false;
        }
    }

    protected void _downloadAllBlockHeaders() {
        final Sha256Hash resumeAfterHash;
        {
            Sha256Hash lastKnownHash = null;
            try (final MysqlDatabaseConnection databaseConnection = _databaseConnectionFactory.newConnection()) {
                final BlockDatabaseManager blockDatabaseManager = new BlockDatabaseManager(databaseConnection);
                lastKnownHash = blockDatabaseManager.getHeadBlockHeaderHash();
            }
            catch (final DatabaseException e) { }

            resumeAfterHash = Util.coalesce(lastKnownHash, Block.GENESIS_BLOCK_HASH);
        }

        final Container<Sha256Hash> lastBlockHash = new Container<Sha256Hash>(resumeAfterHash);

        final BitcoinNode.DownloadBlockHeadersCallback downloadBlockHeadersCallback = new BitcoinNode.DownloadBlockHeadersCallback() {
            @Override
            public void onResult(final List<BlockHeaderWithTransactionCount> blockHeaders) {
                if (_shouldStop) { return; }

                final BlockHeader firstBlockHeader = blockHeaders.get(0);
                Logger.log("DOWNLOADED BLOCK HEADERS: "+ firstBlockHeader.getHash() + " + " + blockHeaders.getSize());

                try (final MysqlDatabaseConnection databaseConnection = _databaseConnectionFactory.newConnection()) {
                    final BlockChainDatabaseManager blockChainDatabaseManager = new BlockChainDatabaseManager(databaseConnection);
                    final BlockDatabaseManager blockDatabaseManager = new BlockDatabaseManager(databaseConnection);

                    for (final BlockHeader blockHeader : blockHeaders) {
                        final Sha256Hash blockHash = blockHeader.getHash();

                        if (! blockHeader.isValid()) {
                            Logger.log("Invalid BlockHeader received: " + blockHash);
                            break;
                        }

                        final BlockId blockId;
                        synchronized (BlockDatabaseManager.MUTEX) {
                            TransactionUtil.startTransaction(databaseConnection);
                            blockId = blockDatabaseManager.storeBlockHeader(blockHeader);
                            blockChainDatabaseManager.updateBlockChainsForNewBlock(blockHeader);
                            TransactionUtil.commitTransaction(databaseConnection);
                        }

                        if (blockId == null) {
                            Logger.log("Error storing BlockHeader: " + blockHash);
                            break;
                        }
                        else {
                            _blockHeaderCount += 1L;
                            final Long now = System.currentTimeMillis();
                            final Long millisecondsElapsed = (now - _startTime);
                            _averageBlockHeadersPerSecond.value = ( (_blockHeaderCount.floatValue() / millisecondsElapsed) * 1000L );
                        }

                        lastBlockHash.value = blockHash;
                    }
                }
                catch (final DatabaseException exception) {
                    Logger.log(exception);
                    Logger.log("Syncing BlockHeaders aborting.");
                    return;
                }

                Logger.log("Stored Block Headers: " + firstBlockHeader.getHash() + " - " + lastBlockHash.value);

                _nodeManager.requestBlockHeadersAfter(lastBlockHash.value, this);
            }
        };

        _nodeManager.requestBlockHeadersAfter(lastBlockHash.value, downloadBlockHeadersCallback);
    }

    public BlockHeaderDownloader(final MysqlDatabaseConnectionFactory databaseConnectionFactory, final BitcoinNodeManager nodeManager, final MutableMedianBlockTime medianBlockTime) {
        _databaseConnectionFactory = databaseConnectionFactory;
        _nodeManager = nodeManager;
        _medianBlockTime = medianBlockTime;
    }

    public void start() {
        _shouldStop = false;

        _blockHeaderCount = 0L;
        _startTime = System.currentTimeMillis();

        if (_hasGenesisBlockHeader()) {
            _downloadAllBlockHeaders();
            return;
        }

        _nodeManager.requestBlock(Block.GENESIS_BLOCK_HASH, new BitcoinNode.DownloadBlockCallback() {
            @Override
            public void onResult(final Block blockHeader) {
                Logger.log("GENESIS RECEIVED: " + blockHeader.getHash());
                if (! _hasGenesisBlockHeader()) {
                    // NOTE: Can happen if the NodeModule received GenesisBlock from another node...
                    try (final MysqlDatabaseConnection databaseConnection = _databaseConnectionFactory.newConnection()) {
                        final BlockHeaderValidator blockValidator = new BlockHeaderValidator(databaseConnection, _nodeManager.getNetworkTime(), _medianBlockTime);
                        final BlockChainDatabaseManager blockChainDatabaseManager = new BlockChainDatabaseManager(databaseConnection);
                        final BlockDatabaseManager blockDatabaseManager = new BlockDatabaseManager(databaseConnection);

                        TransactionUtil.startTransaction(databaseConnection);
                        final BlockId blockId = blockDatabaseManager.storeBlockHeader(blockHeader);
                        blockChainDatabaseManager.updateBlockChainsForNewBlock(blockHeader);

                        final BlockChainSegmentId blockChainSegmentId = blockDatabaseManager.getBlockChainSegmentId(blockId);
                        final Boolean blockHeaderIsValid = blockValidator.validateBlockHeader(blockChainSegmentId, blockHeader);
                        if (! blockHeaderIsValid) {
                            Logger.log("Invalid BlockHeader: " + blockHeader.getHash());
                            TransactionUtil.rollbackTransaction(databaseConnection);
                            return;
                        }

                        TransactionUtil.commitTransaction(databaseConnection);

                        if (blockId == null) {
                            Logger.log("Error storing genesis block header.");
                            return;
                        }

                        Logger.log("GENESIS STORED: " + blockHeader.getHash());
                    }
                    catch (final DatabaseException exception) {
                        Logger.log(exception);
                        return;
                    }
                }

                _downloadAllBlockHeaders();
            }
        });
    }

    public void stop() {
        _shouldStop = true;
    }

    public Container<Float> getAverageBlockHeadersPerSecondContainer() {
        return _averageBlockHeadersPerSecond;
    }
}
