package com.totoro.canal.es.scheduler;

import com.alibaba.otter.canal.client.CanalConnector;
import com.alibaba.otter.canal.client.CanalConnectors;
import com.alibaba.otter.canal.common.utils.AddressUtils;
import com.alibaba.otter.canal.protocol.Message;
import com.google.common.util.concurrent.ThreadFactoryBuilder;
import com.totoro.canal.es.select.selector.TotoroSelector;
import com.totoro.canal.es.select.selector.canal.CanalEmbedSelector;

import java.net.InetSocketAddress;
import java.util.concurrent.*;

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
public class TotoroScheduler {

    //����� selector ȡ�������� ,�ȴ� Transform ȥ����
    private LinkedBlockingQueue<Message> selectorMessageQueue = new LinkedBlockingQueue<>(50);


    private volatile boolean runing = true;


    public void start() {

        ThreadFactory threadFactoryBuilder = new ThreadFactoryBuilder()
                .setNameFormat("selector-pool-%d").build();

        ExecutorService selectorExecutor = Executors.newSingleThreadExecutor(threadFactoryBuilder);

        String destination = "totoro";
        String ip = AddressUtils.getHostIp();
        CanalConnector connector = CanalConnectors.newSingleConnector(new InetSocketAddress(ip, 11111),
                destination,
                "",
                "");

        TotoroSelector ts = new CanalEmbedSelector(connector, destination);
        ts.start();


        selectorExecutor.submit(() -> {
            //ȡ����
            while (runing) {
                try {
                    Message message = ts.selector();
                    //����������������ȴ�
                    selectorMessageQueue.put(message);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

            }
        });


    }

}
