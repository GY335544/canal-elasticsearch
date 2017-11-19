package com.totoro.canal.es.select.selector;

import com.alibaba.otter.canal.protocol.Message;

import java.util.List;

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
public interface TotoroSelector {

    /**
     * ����
     */
    void start();

    /**
     * �Ƿ�����
     */
    boolean isStart();

    /**
     * �ر�
     */
    void stop();

    /**
     * ��ȡһ�������������
     */
    Message selector() throws InterruptedException;


    /**
     * �������һ��entry���ݵ�ʱ���
     */
    Long lastEntryTime();

    /**
     * ����δ��ack������
     */
    List<Long> unAckBatchs();

    /**
     * ����һ�����ݴ���ʧ�ܣ���Ҫ�´����±�����
     */
    void rollback(Long batchId);

    /**
     * �������е�batch������Ҫ�����´���
     */
    void rollback();

    /**
     * ����һ�����ݴ������
     */
    void ack(Long batchId);
}
