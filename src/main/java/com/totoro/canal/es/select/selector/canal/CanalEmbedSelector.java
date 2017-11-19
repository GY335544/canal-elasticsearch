package com.totoro.canal.es.select.selector.canal;

import com.alibaba.otter.canal.client.CanalConnector;
import com.alibaba.otter.canal.protocol.Message;
import com.totoro.canal.es.select.selector.TotoroSelector;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.LockSupport;

/**
 * ���⡢��Ҫ˵��. <br>
 * ����ϸ˵��.
 * <p>
 * Copyright: Copyright (c)
 * <p>
 * Company: xx
 * <p>
 *
 * @author zhongcheng_m@yeah.net
 * @version 1.0.0
 */
public class CanalEmbedSelector implements TotoroSelector {

    private static final Logger logger = LoggerFactory.getLogger(CanalEmbedSelector.class);

    private volatile boolean running = false;

    private CanalConnector connector; // instance client

    private String destination;  //��Ҫ���ĵ� canal instance

    private volatile long lastEntryTime = 0;

    //����ѭ������
    private static final int maxEmptyTimes = 10;

    public CanalEmbedSelector(CanalConnector connector, String destination) {
        this.connector = connector;
        this.destination = destination;
    }

    //ÿ���� ���� �Ĵ�С
    private int batchSize = 5 * 1024;

    private String FILTER_PATTEN = ".*\\..*";

    //�Ƿ���Ҫ��ʱ����
    private long batchTimeout = -1L;

    /**
     * �߳��쳣����
     */
    protected Thread.UncaughtExceptionHandler handler = (t, e) -> logger.error("parse events has an error", e);

    public void start() {
        if (running) {
            return;
        }
        connector.connect();
        connector.subscribe(FILTER_PATTEN);
        running = true;
    }

    public boolean isStart() {
        return running;
    }

    public void stop() {
        connector.disconnect();
    }

    public Message selector() throws InterruptedException {
        if (!running) {
            throw new RuntimeException("CanalEmbedSelector has benn not start");
        }

        Message message = null;
        int emptyTimes = 0;

        if (batchTimeout < 0) {// ������ѯ����
            while (running) {
                message = connector.getWithoutAck(batchSize);
                if (message == null || message.getId() == -1L) { // ����û����
                    applyWait(emptyTimes++);
                } else {
                    break;
                }
            }
            if (!running) {
                throw new InterruptedException();
            }
        } else {

            while (running) {
                message = connector.getWithoutAck(batchSize, batchTimeout, TimeUnit.SECONDS);
                if (message == null || message.getId() == -1L) { // ����û����
                    continue;
                } else {
                    break;
                }
            }
            if (!running) {
                throw new InterruptedException();
            }
        }

        return message;
    }

    public Long lastEntryTime() {
        return null;
    }

    public List<Long> unAckBatchs() {
        return null;
    }

    public void rollback(Long batchId) {
        connector.rollback(batchId);
    }

    public void rollback() {
        connector.rollback();
    }

    public void ack(Long batchId) {
        connector.ack(batchId);
    }


    // ���������ݵ�����������ѭ������
    private void applyWait(int emptyTimes) {
        int newEmptyTimes = emptyTimes > maxEmptyTimes ? maxEmptyTimes : emptyTimes;
        if (emptyTimes <= 3) { // 3������
            Thread.yield();
        } else { // ����3�Σ����ֻsleep 10ms
            LockSupport.parkNanos(1000 * 1000L * newEmptyTimes);
        }
    }


}
