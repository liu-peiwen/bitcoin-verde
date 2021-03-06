package com.softwareverde.bitcoin.server.module.node.handler;

import com.softwareverde.bitcoin.block.BlockId;
import com.softwareverde.bitcoin.block.header.BlockHeader;
import com.softwareverde.bitcoin.block.header.BlockHeaderWithTransactionCount;
import com.softwareverde.bitcoin.block.header.ImmutableBlockHeaderWithTransactionCount;
import com.softwareverde.bitcoin.server.database.BlockDatabaseManager;
import com.softwareverde.bitcoin.server.message.type.query.response.block.header.BlockHeadersMessage;
import com.softwareverde.bitcoin.server.message.type.request.header.RequestBlockHeadersMessage;
import com.softwareverde.bitcoin.server.node.BitcoinNode;
import com.softwareverde.bitcoin.type.hash.sha256.Sha256Hash;
import com.softwareverde.constable.list.List;
import com.softwareverde.constable.list.mutable.MutableList;
import com.softwareverde.database.DatabaseException;
import com.softwareverde.database.mysql.MysqlDatabaseConnection;
import com.softwareverde.database.mysql.MysqlDatabaseConnectionFactory;
import com.softwareverde.io.Logger;
import com.softwareverde.network.p2p.node.NodeConnection;

public class QueryBlockHeadersHandler extends AbstractQueryBlocksHandler implements BitcoinNode.QueryBlockHeadersCallback {
    public QueryBlockHeadersHandler(final MysqlDatabaseConnectionFactory databaseConnectionFactory) {
        super(databaseConnectionFactory);
    }

    @Override
    public void run(final List<Sha256Hash> blockHashes, final Sha256Hash desiredBlockHash, final NodeConnection nodeConnection) {
        try (final MysqlDatabaseConnection databaseConnection = _databaseConnectionFactory.newConnection()) {
            final BlockDatabaseManager blockDatabaseManager = new BlockDatabaseManager(databaseConnection);

            final StartingBlock startingBlock = _getStartingBlock(blockHashes, desiredBlockHash, databaseConnection);

            if (startingBlock == null) {
                Logger.log("Unable to send headers: No blocks available.");
                return;
            }

            final MutableList<BlockHeaderWithTransactionCount> blockHeaders = new MutableList<BlockHeaderWithTransactionCount>();
            {
                final List<BlockId> childrenBlockIds = _findBlockChildrenIds(startingBlock.startingBlockId, desiredBlockHash, startingBlock.selectedBlockChainSegmentId, RequestBlockHeadersMessage.MAX_BLOCK_HEADER_HASH_COUNT, blockDatabaseManager);
                for (final BlockId blockId : childrenBlockIds) {
                    final BlockHeader blockHeader = blockDatabaseManager.getBlockHeader(blockId);
                    final Integer transactionCount = blockDatabaseManager.getTransactionCount(blockId);
                    final BlockHeaderWithTransactionCount blockHeaderWithTransactionCount = new ImmutableBlockHeaderWithTransactionCount(blockHeader, transactionCount);
                    blockHeaders.add(blockHeaderWithTransactionCount);
                }
            }

            final BlockHeadersMessage responseMessage = new BlockHeadersMessage();
            for (final BlockHeaderWithTransactionCount blockHeader : blockHeaders) {
                responseMessage.addBlockHeader(blockHeader);
            }

            nodeConnection.queueMessage(responseMessage);
        }
        catch (final DatabaseException exception) { Logger.log(exception); }
    }
}
