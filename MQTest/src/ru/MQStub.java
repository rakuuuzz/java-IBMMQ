package ru;

import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.Queue;
import javax.jms.Session;
import javax.jms.TextMessage;

import com.ibm.mq.jms.MQQueue;
import com.ibm.mq.jms.MQQueueConnection;
import com.ibm.mq.jms.MQQueueConnectionFactory;
import com.ibm.mq.jms.MQQueueReceiver;
import com.ibm.mq.jms.MQQueueSender;
import com.ibm.mq.jms.MQQueueSession;


public class MQStub {

    public static void main(String[] args) {
        try {
            MQQueueConnection mqConn;
            MQQueueConnectionFactory mqCf;
            final MQQueueSession mqSession;
            MQQueue mqIn;
            MQQueueReceiver mqReceiver;
            MQQueue mqOut;
            MQQueueSender mqSender; 

            mqCf = new MQQueueConnectionFactory();
            mqCf.setHostName("localhost");
            mqCf.setPort(1415);
            mqCf.setQueueManager("MQtester");
            mqCf.setChannel("SYSTEM.DEF.SVRCONN");

            mqConn = (MQQueueConnection) mqCf.createQueueConnection();
            mqSession = (MQQueueSession) mqConn.createQueueSession(true, Session.AUTO_ACKNOWLEDGE);

            mqIn = (MQQueue) mqSession.createQueue("Mq.IN");
            mqReceiver = (MQQueueReceiver) mqSession.createReceiver(mqIn);
            
            mqOut = (MQQueue) mqSession.createQueue("Mq.OUT");
            mqSender = (MQQueueSender) mqSession.createSender(mqOut);

            javax.jms.MessageListener Listener = new javax.jms.MessageListener() {
                @Override
                public void onMessage(Message msg) {
                    System.out.println("Got message!");
                    if (msg instanceof TextMessage) {
                        try {
                            TextMessage tMsg = (TextMessage) msg;
                            String msgText = tMsg.getText();
                            System.out.println("Message Text: " + msgText);
                            
                            TextMessage outMsg = mqSession.createTextMessage(msgText);
                            outMsg.setText(msgText);
                            mqSender.send(outMsg);
                            System.out.println("Sent message: " + outMsg.getText());
                        } catch (JMSException e) {
                            e.printStackTrace();
                        }
                    }
                }
            };

            mqReceiver.setMessageListener(Listener);
            mqConn.start();
            System.out.println("Stub Started");

            try {
                Thread.sleep(60000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

        } catch (JMSException e) {
            e.printStackTrace();
        }
    }
}
