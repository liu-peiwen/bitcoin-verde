package com.softwareverde.network.p2p.node.manager;

import com.softwareverde.io.Logger;
import com.softwareverde.network.p2p.node.manager.health.NodeHealth;
import com.softwareverde.util.Container;

public class RequestTimeoutThread extends Thread {
    public static final Integer MAX_REPLAY_COUNT = 3;
    public static final Long REQUEST_TIMEOUT_THRESHOLD = 10_000L;

    public final Object mutex = new Object();

    private final Container<Boolean> _didMessageTimeOut;
    private final NodeHealth _nodeHealth;
    private final ReplayInvocation _replayInvocation;

    public RequestTimeoutThread(final Container<Boolean> didMessageTimeoutContainer, final NodeHealth nodeHealth, final ReplayInvocation replayInvocation) {
        _didMessageTimeOut = didMessageTimeoutContainer;
        _nodeHealth = nodeHealth;
        _replayInvocation = replayInvocation;

        this.setName("Node Manager - Request Timeout Thread - " + this.getId());
    }

    @Override
    public void run() {
        try { Thread.sleep(REQUEST_TIMEOUT_THRESHOLD); }
        catch (final Exception exception) { return; }

        synchronized (this.mutex) {
            if (_didMessageTimeOut.value != null) { return; }
            _didMessageTimeOut.value = true;
        }

        _nodeHealth.onMessageReceived(false);
        if (NodeManager.LOGGING_ENABLED) {
            Logger.log("P2P: NOTICE: Node " + _nodeHealth.getNodeId() + ": Request timed out.");
        }

        if (_replayInvocation != null) {
            final Integer attemptCount = _replayInvocation.getReplayCount();
            if (attemptCount < MAX_REPLAY_COUNT) {
                _replayInvocation.run();
            }
            else {
                _replayInvocation.fail();
            }
        }
    }
}