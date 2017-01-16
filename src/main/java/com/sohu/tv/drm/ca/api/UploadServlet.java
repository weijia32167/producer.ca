package com.sohu.tv.drm.ca.api;

import com.alibaba.rocketmq.client.producer.DefaultMQProducer;
import com.alibaba.rocketmq.client.producer.SendResult;
import com.alibaba.rocketmq.common.message.Message;
import com.sohu.tv.drm.ca.config.SystemConfig;
import com.sun.xml.internal.messaging.saaj.util.ByteInputStream;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.springframework.web.context.support.WebApplicationContextUtils;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.annotation.MultipartConfig;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.Part;
import java.io.*;
import java.text.SimpleDateFormat;
import java.util.Date;


/**
 * Copyright tv.sohu.com
 * Author : jiawei
 * Date   : 2017/1/16
 * Desc   :
 */
@WebServlet(name="UploadServlet",urlPatterns="/upload")
@MultipartConfig
public class UploadServlet extends HttpServlet {

    private static final Logger logger = LogManager.getLogger("UploadServlet");

    public static final String TOPIC = "CA_Key";

    private DefaultMQProducer producer;

    @Override
    public void init(ServletConfig config) throws ServletException {
        super.init(config);
        producer =  WebApplicationContextUtils.getWebApplicationContext(this.getServletContext()).getBean("producer",DefaultMQProducer.class);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        if(check(request,response)){
            Part part = request.getPart("file");
            String index = request.getParameter("index");
            InputStream is = part.getInputStream();
            byte[] bytes = new byte[is.available()];
            is.read(bytes);
            BufferedReader br = new BufferedReader(new InputStreamReader(new ByteInputStream(bytes,bytes.length)));
            String line;
            while((line=br.readLine())!=null){
                if(line.length()!= SystemConfig.KEY_LENGTH){
                    logger.error("file key length must be "+ SystemConfig.KEY_LENGTH+"!");
                    response.sendError(HttpServletResponse.SC_BAD_REQUEST,"file key length must be "+ SystemConfig.KEY_LENGTH+"!");
                    br.close();
                    return;
                }
            }
            File dir = new File(SystemConfig.UPLOAD_DIR);
            if(!dir.exists()){
                dir.mkdir();
            }
            SimpleDateFormat form = new SimpleDateFormat("yyyy_MM_dd_HH_mm_ss");
            Date date = new Date(System.currentTimeMillis());
            File file = new File(dir,index+"." +form.format(date));
            FileOutputStream fos = new FileOutputStream(file);
            fos.write(bytes);
            fos.flush();
            fos.close();
            Message message = new Message(TOPIC, bytes);
            try {
                SendResult sendResult =producer.send(message);
                logger.info("update file["+file.getAbsolutePath()+"]");
                logger.info(sendResult);
                response.setStatus(HttpServletResponse.SC_OK);
                if(SystemConfig.PRINT_DETAIL){
                    response.getWriter().print("update file["+file.getAbsolutePath()+"]<br/>"+sendResult);
                }else{
                    response.getWriter().print("ok!");
                }
                response.flushBuffer();
            } catch (Throwable e) {
                e.printStackTrace();
                logger.error(e);
                response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR,e.getMessage());
            }
        }
    }

    private boolean check(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        Part part = request.getPart("file");
        String indexString = request.getParameter("index");
        boolean result = false;
        if(part==null || indexString ==null){
            result = false;
            logger.error("Missing parameter:file or index!");
            response.sendError(HttpServletResponse.SC_BAD_REQUEST,"Missing parameter:file or index!");
        }else{
            try{
                int index = Integer.parseInt(indexString);
                if(index<1 || index >3){
                    result = false;
                    logger.error("index must number between 1~3!");
                    response.sendError(HttpServletResponse.SC_BAD_REQUEST,"index must number between 1~3!");
                }else{
                    result = true;
                }
            }catch (NumberFormatException e){
                logger.error("index must number between 1~3!");
                response.sendError(HttpServletResponse.SC_BAD_REQUEST,"index must number between 1~3!");
            }
        }
        return result;
    }
}
