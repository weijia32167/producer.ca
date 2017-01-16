package com.sohu.tv.drm.ca.config;
import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.PropertiesConfiguration;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import java.util.NoSuchElementException;

/**
 * Copyright tv.sohu.com
 * Author : jiawei
 * Date   : 2017/1/16
 * Desc   :
 */
public final class SystemConfig {

    private static final Logger logger = LogManager.getLogger("SystemConfig");

    public static String UPLOAD_DIR;
    public static int KEY_LENGTH;
    public static boolean PRINT_DETAIL;

   static {
       PropertiesConfiguration config = null;
       try {
           config = new PropertiesConfiguration("config.txt");
       }catch (ConfigurationException e){
           logger.error("config.txt can't load,exit!");
           System.exit(0);
       }
       try {
           UPLOAD_DIR = config.getString("upload_dir");
       } catch (NoSuchElementException e){
           UPLOAD_DIR = "/data/upload";
           logger.error("upload_dir config error,use default(upload_dir=/data/upload)!");
       }
       try {
           KEY_LENGTH = config.getInt("key_length");
       } catch (NoSuchElementException e){
           KEY_LENGTH = 16;
           logger.error("key_length config error,use default(key_length=16)!");
       }
       try {
           PRINT_DETAIL = config.getBoolean("printDetail");
       } catch (NoSuchElementException e){
           PRINT_DETAIL= true;
           logger.error("printDetail config error,use default(printDetail=true)!");
       }

   }
}
